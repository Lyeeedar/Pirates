package com.Lyeeedar.Graphics;

import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
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

public class AnimatedModelBatch implements Batch {
	
	private final Vector3 tmp = new Vector3();
	private final PriorityQueue<BatchedInstance> instances = new PriorityQueue<BatchedInstance>();
	private Camera cam;

	private float[] bones;
	private ShaderProgram[] shaders = new ShaderProgram[8];
	
	private Matrix4 idtMatrix = new Matrix4();
	
	public AnimatedModelBatch(int num_bones)
	{
		bones = new float[num_bones * 16];
	}
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public static class RenderablePool extends Pool<Renderable> {
		protected Array<Renderable> obtained = new Array<Renderable>();
		
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.mesh = null;
			renderable.shader = null;
			obtained.add(renderable);
			return renderable;
		}
		
		public void flush() {
			super.freeAll(obtained);
			obtained.clear();
		}
	}
	
	protected final RenderablePool renderablesPool = new RenderablePool();  
	protected final Array<Renderable> renderables = new Array<Renderable>();
	int current_shader = -1;
	int textureHash;
	
	public void render(LightManager lights, Camera cam)
	{
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		this.cam = cam;
		Matrix3 normal_matrix = Pools.obtain(Matrix3.class);

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
				
				lights.applyLights(shaders[current_shader]);
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
			normal_matrix.set(bi.instance.worldTransform).inv().transpose();
			shaders[current_shader].setUniformMatrix("u_nm", normal_matrix);
			
			shaders[current_shader].setUniformf("u_colour", bi.colour);

			if (textureHash != bi.texHash)
			{
				bi.tex.bind(0);
				shaders[current_shader].setUniformi("u_texture", 0);
				textureHash = bi.texHash;
			}
						
			bi.instance.mesh.render(shaders[current_shader], bi.instance.primitiveType, bi.instance.meshPartOffset, bi.instance.meshPartSize);
			
			renderablesPool.free(bi.instance);
			pool.free(bi);
		}
		
		if (current_shader != -1) shaders[current_shader].end();
		current_shader = -1;
		textureHash = 0;
		
		Pools.free(normal_matrix);
	}
	
	public ShaderProgram createShader(int bone_num)
	{
		String prefix = "";
		
		for (int i = 0; i < bone_num; i++)
		{
			prefix += "#define boneWeight"+i+"Flag\n";
		}
		if (bone_num > 0)
		{
			prefix += "#define skinning\n";
			prefix += "#define numBones "+(bones.length/16)+"\n"; 
		}
		
		String vert = prefix + Gdx.files.internal("data/shaders/skinned_model.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/cellshading_body.fragment.glsl").readString();
		
		ShaderProgram shader = new ShaderProgram(vert, frag);
	
		if (!shader.isCompiled()) System.err.println(shader.getLog());
		
		return shader;
	}
	
	public void add(ModelInstance model, Texture tex, Vector3 colour)
	{
		if (cam == null) return;
		
		model.getRenderables(renderables, renderablesPool);
		for (Renderable r : renderables)
		{
			tmp.set(0, 0, 0).mul(r.worldTransform);
			instances.add(pool.obtain().set(r, tex, colour, -tmp.dst2(cam.position)));
		}
		renderables.clear();
	}

	private class BatchedInstance implements Comparable<BatchedInstance>
	{
		public Renderable instance;
		private float dist;
		public int bone_num;
		public Texture tex;
		public Vector3 colour = new Vector3();
		public int texHash;
		
		public BatchedInstance set(Renderable instance, Texture tex, Vector3 colour, float dist)
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
			this.texHash = tex.hashCode();
			this.colour.set(colour);
			return this;
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (equals(bi)) return 0;
			if (bi.dist == dist) 
			{
				if (bone_num == bi.bone_num) return bi.texHash - texHash;
				return bi.bone_num - bone_num;
			}
			return (int) ((bi.dist - dist)*100);
		}	
	}
}
