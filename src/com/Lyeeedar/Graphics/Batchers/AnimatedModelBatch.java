package com.Lyeeedar.Graphics.Batchers;

import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class AnimatedModelBatch implements Batch 
{	
	private final RenderType renderType;
	private final Vector3 tmp = new Vector3();
	private final PriorityQueue<BatchedInstance> instances = new PriorityQueue<BatchedInstance>();
	private Camera cam;

	private final float[] bones;
	private final ShaderProgram[] shaders = new ShaderProgram[8];
	
	private final Matrix4 idtMatrix = new Matrix4();
	
	public AnimatedModelBatch(int num_bones)
	{
		this(num_bones, RenderType.FORWARD);
	}
	
	public AnimatedModelBatch(int num_bones, RenderType renderType)
	{
		this.bones = new float[num_bones * 16];
		this.renderType = renderType;
	}
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};

	int current_shader = -1;
	int textureHash;
	
	public void render(LightManager lights, Camera cam)
	{		
		this.cam = cam;

		while (!instances.isEmpty())
		{
			BatchedInstance bi = instances.poll();
			
			if (current_shader != bi.bone_num) 
			{
				if (current_shader != -1)
				{
					shaders[current_shader].end();
				}
				
				current_shader = bi.bone_num;
				
				if (shaders[current_shader] == null)
				{
					shaders[current_shader] = createShader(bi.bone_num);
				}
				
				shaders[current_shader].begin();
				
				shaders[current_shader].setUniformMatrix("u_pv", cam.combined);
				shaders[current_shader].setUniformf("fog_col", lights.ambientColour);
				shaders[current_shader].setUniformf("fog_min", GLOBALS.FOG_MIN);
				shaders[current_shader].setUniformf("fog_max", GLOBALS.FOG_MAX);
				shaders[current_shader].setUniformf("u_viewPos", cam.position);
				
				if (renderType == RenderType.FORWARD) lights.applyLights(shaders[current_shader], 7);
			}
			
			if (bi.bone_num > 0)
			{
				for (int i = 0; i < bones.length; i++) {
					final int idx = i/16;
					bones[i] = (bi.instance.bones == null || idx >= bi.instance.bones.length || bi.instance.bones[idx] == null) ? 
						idtMatrix.val[i%16] : bi.instance.bones[idx].val[i%16];
				}
				shaders[current_shader].setUniformMatrix4fv("u_bones", bones, 0, bones.length);
			}
			
			shaders[current_shader].setUniformMatrix("u_mm", bi.instance.worldTransform);
			shaders[current_shader].setUniformi("u_texNum", bi.tex.length);
			shaders[current_shader].setUniformf("u_colour", bi.colour);

			if (textureHash != bi.texHash)
			{
				for (int i = 0; i < bi.tex.length; i++)
				{
					bi.tex[i].bind(i);
					shaders[current_shader].setUniformi("u_texture"+i, i);
				}
				textureHash = bi.texHash;
			}
			
			if (bi.dtex != null)
			{
				int di = 4;
				for (Texture tex : bi.dtex)
				{
					if (tex != null)
					{
						tex.bind(di);
						shaders[current_shader].setUniformi("u_detail"+(di-4), di);
						di++;
					}
				}
				shaders[current_shader].setUniformi("u_detailNum", di-4);
			}
						
			bi.instance.mesh.render(shaders[current_shader], bi.instance.primitiveType, bi.instance.meshPartOffset, bi.instance.meshPartSize);
			
			pool.free(bi);
		}
		
		if (current_shader != -1) shaders[current_shader].end();
		current_shader = -1;
		textureHash = 0;
		
	}
	
	public void render(Camera cam, int primitiveType, Color colour)
	{		
		this.cam = cam;

		while (!instances.isEmpty())
		{
			BatchedInstance bi = instances.poll();
			
			if (current_shader != bi.bone_num) 
			{
				if (current_shader != -1)
				{
					shaders[current_shader].end();
				}
				
				current_shader = bi.bone_num;
				
				if (shaders[current_shader] == null)
				{
					shaders[current_shader] = createShader(bi.bone_num);
				}
				
				shaders[current_shader].begin();
				
				shaders[current_shader].setUniformMatrix("u_pv", cam.combined);
				
			}
			
			if (bi.bone_num > 0)
			{
				for (int i = 0; i < bones.length; i++) {
					final int idx = i/16;
					bones[i] = (bi.instance.bones == null || idx >= bi.instance.bones.length || bi.instance.bones[idx] == null) ? 
						idtMatrix.val[i%16] : bi.instance.bones[idx].val[i%16];
				}
				shaders[current_shader].setUniformMatrix4fv("u_bones", bones, 0, bones.length);
			}
			
			shaders[current_shader].setUniformMatrix("u_mm", bi.instance.worldTransform);
			shaders[current_shader].setUniformf("u_colour", colour);
						
			bi.instance.mesh.render(shaders[current_shader], primitiveType, bi.instance.meshPartOffset, bi.instance.meshPartSize);
			
			pool.free(bi);
		}
		
		if (current_shader != -1) shaders[current_shader].end();
		current_shader = -1;
		textureHash = 0;		
	}
	
	public ShaderProgram createShader(int bone_num)
	{
		StringBuilder prefix = new StringBuilder();
		
		for (int i = 0; i < bone_num; i++)
		{
			prefix.append("#define boneWeight").append(i).append("Flag\n");
		}
		if (bone_num > 0)
		{
			prefix.append("#define skinning\n");
			prefix.append("#define numBones ").append(bones.length/16).append("\n"); 
		}
		
		String vert = "";
		String frag = "";
		
		if (renderType == RenderType.SIMPLE)
		{
			vert = prefix.toString() + Gdx.files.internal("data/shaders/forward/skinned_model.vertex.glsl").readString();
			frag = Gdx.files.internal("data/shaders/forward/skinned_model_simple.fragment.glsl").readString();
		}
		else if (renderType == RenderType.FORWARD)
		{
			vert = prefix.toString() + Gdx.files.internal("data/shaders/forward/skinned_model.vertex.glsl").readString();
			frag = Gdx.files.internal("data/shaders/forward/skinned_model.fragment.glsl").readString();
		}
		else if (renderType == RenderType.DEFERRED)
		{
			vert = prefix.toString() + Gdx.files.internal("data/shaders/deferred/skinned_model.vertex.glsl").readString();
			frag = Gdx.files.internal("data/shaders/deferred/skinned_model.fragment.glsl").readString();
		}
		
		ShaderProgram shader = new ShaderProgram(vert, frag);
	
		if (!shader.isCompiled()) System.err.println(shader.getLog());
		
		return shader;
	}
	
	public void add(Array<Renderable> renderables, Texture[] tex, Texture[] dtex, Vector3 colour)
	{
		if (cam == null) return;
		
		for (Renderable r : renderables)
		{
			tmp.set(0, 0, 0).mul(r.worldTransform);
			instances.add(pool.obtain().set(r, tex, dtex, colour, -tmp.dst2(cam.position)));
		}
	}

	private static class BatchedInstance implements Comparable<BatchedInstance>
	{
		private static final float tolerance = 0.01f;
		public Renderable instance;
		private float dist;
		public int bone_num;
		public Texture[] tex;
		public Texture[] dtex;
		public Vector3 colour = new Vector3();
		public int texHash;
		
		public BatchedInstance set(Renderable instance, Texture[] tex, Texture[] dtex, Vector3 colour, float dist)
		{
			bone_num = 0;
			final int n = instance.mesh.getVertexAttributes().size();
			for (int i = 0; i < n; i++) {
				final VertexAttribute attr = instance.mesh.getVertexAttributes().get(i);
				if (attr.usage == Usage.BoneWeight)
					bone_num++;
			}
			
			this.instance = instance;
			this.tex = tex;
			this.dtex = dtex;
			this.texHash = tex[0].hashCode();
			this.colour.set(colour);
			this.dist = dist;
			return this;
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (bi.dist-dist < tolerance) 
			{
				if (bone_num == bi.bone_num) return bi.texHash - texHash;
				return bi.bone_num - bone_num;
			}
			return (int) ((bi.dist - dist)*100);
		}	
	}
}
