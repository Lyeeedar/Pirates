package com.Lyeeedar.Graphics.Batchers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.UniformBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;

public class ModelBatcher implements Queueable {
	
	public static int MAX_INSTANCES;
	public static final int BLOCK_SIZE = 16;
		
	private final Mesh mesh;
	private final int primitive_type;
	private final Texture[] textures;
	private final boolean transparent;
	private final boolean canCull;
	
	private final PriorityQueue<BatchedInstance> solidInstances = new PriorityQueue<BatchedInstance>();
	private final PriorityQueue<BatchedInstance> transparentInstances = new PriorityQueue<BatchedInstance>();
		
	private static ShaderProgram solidShader;
	private static ShaderProgram transparentShader;
	private static ShaderProgram shader;
	
	private static UniformBufferObject ubo;
		
	private Camera cam;
	
	boolean queued = false;
	
	private final Vector3 tmpVec = new Vector3();
	private final Matrix4 tmpMat = new Matrix4();
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public ModelBatcher(Mesh mesh, int primitive_type, Texture[] textures, boolean transparent, boolean canCull)
	{
		this.mesh = mesh;
		this.primitive_type = primitive_type;
		this.textures = textures;
		this.transparent = transparent;
		this.canCull = canCull;
		
		IntBuffer ib = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL30.GL_MAX_UNIFORM_BLOCK_SIZE, ib);
		int limitBytes = ib.get(0);
		System.out.println("Uniform Block limit (bytes): " + limitBytes);
		System.out.println("Uniform Block limit (floats): " + (limitBytes / 4));
		System.out.println("Uniform Block limit (vec4): " + (limitBytes / 16));
		System.out.println("Uniform Block limit (mat4): " + (limitBytes / 64));
		int supportedMaxFloats = (limitBytes / 4);
		int supportedBlocks = supportedMaxFloats / Math.max(4, BLOCK_SIZE) ;
		MAX_INSTANCES = supportedBlocks ;
		
		loadSolidShader();
		loadTransparentShader();
		
		if (ubo == null) ubo = new UniformBufferObject(4 * BLOCK_SIZE * MAX_INSTANCES, 1);
		solidShader.registerUniformBlock("InstanceBlock", 1);
		transparentShader.registerUniformBlock("InstanceBlock", 1);
	}
	
	public Mesh getMesh()
	{
		return mesh;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) 
	{
		if (!queued) 
		{
			((ModelBatchers) batches.get(ModelBatchers.class)).add(this);
			queued = true;
			this.cam = cam;
		}
	}

	@Override
	public void set(Entity source, Matrix4 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			add(tmpMat.set(source.readOnlyRead(PositionalData.class).composed).mul(offset));
		}
		else
		{
			add(tmpMat.setToTranslation(source.readOnlyRead(MinimalPositionalData.class).position).mul(offset));
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
	}

	@Override
	public Queueable copy() {
		return this;
	}

	@Override
	public void dispose() {
	}
	
	public void add(Matrix4 transform)
	{
		if (cam == null) return;
		
		Vector3 pos = tmpVec.set(0, 0, 0).mul(transform);
		float d = cam.position.dst(pos);
		if (d > GLOBALS.FOG_MAX || d > cam.far) return;
		
		if (!canCull)
		{
			if (transparent) transparentInstances.add(pool.obtain().set(transform, d, 1.0f));
			else solidInstances.add(pool.obtain().set(transform, -d, 1.0f));
			return;
		}
		
		float cam2 = cam.far;
		float quarter = (cam2)/4.0f;
		float ncam = cam2-quarter;
		
		float dd = Math.max(d-quarter, 0.0f);
		
		float threshold = 1.0f - (dd / (ncam));
		float a = Math.abs(pos.x);
		float dst = a-MathUtils.floor(a);
				
		if (dst <= threshold) 
		{
			float fadestart = (1.0f-dst)*(ncam)+quarter;
			float fade = Math.min((fadestart-d)/(quarter/2.0f), 1.0f);
			
			if (fade > 0.0f) 
			{
				if (transparent || fade < 1.0f) transparentInstances.add(pool.obtain().set(transform, d, fade));
				else solidInstances.add(pool.obtain().set(transform, -d, fade));
			}
		}
	}
	
	public void renderSolid()
	{
		flush(solidInstances, false);
	}
	public void renderTransparent()
	{
		flush(transparentInstances, true);
	}
	
	private void flush(PriorityQueue<BatchedInstance> instances, boolean transparent)
	{
		shader.setUniformi("u_texNum", textures.length);
		
		for (int i = 0; i < textures.length; i++)
		{
			shader.setUniformi("u_texture"+i, i);
			textures[i].bind(i);
		}
						
		int i = 0;
		FloatBuffer floatbuffer = ubo.getDataBuffer().asFloatBuffer();
		while (!instances.isEmpty())
		{			
			BatchedInstance bi = instances.poll();
			floatbuffer.put(bi.transform.val);
			
			i++;
			if (i == MAX_INSTANCES)
			{
				ubo.bind();
				mesh.renderInstanced(shader, primitive_type, i);
				i = 0;
				floatbuffer = ubo.getDataBuffer().asFloatBuffer();
			}
			
			pool.free(bi);
		}
		
		if (i > 0)
		{
			ubo.bind();
			mesh.renderInstanced(shader, primitive_type, i);
		}
		
		queued = false;
	}
	
	public static void beginSolid(LightManager lights, Camera cam)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		if (solidShader == null) loadSolidShader();
		shader = solidShader;
		shader.begin();
		
		lights.applyLights(shader);
		shader.setUniformf("fog_col", lights.ambientColour);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		shader.setUniformMatrix("u_pv", cam.combined);
		shader.setUniformf("u_viewPos", cam.position);
	}
	
	public static void beginTransparent(LightManager lights, Camera cam)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		if (transparentShader == null) loadTransparentShader();
		shader = transparentShader;
		shader.begin();
		
		lights.applyLights(shader);
		shader.setUniformf("fog_col", lights.ambientColour);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		shader.setUniformMatrix("u_pv", cam.combined);
		shader.setUniformf("u_viewPos", cam.position);
	}
	
	public static void end()
	{
		if (shader != null) shader.end();
		shader = null;
	}
	
	public static void loadSolidShader()
	{
		String vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		solidShader = new ShaderProgram(vert, frag);
		if (!solidShader.isCompiled()) System.err.println(solidShader.getLog());
	}
	
	public static void loadTransparentShader()
	{
		String vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = "#define HAS_TRANSPARENT\n" + Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		transparentShader = new ShaderProgram(vert, frag);
		if (!transparentShader.isCompiled()) System.err.println(transparentShader.getLog());
	}
	
	public static class ModelBatchers implements Batch
	{
		Array<ModelBatcher> modelBatchers;
		
		public ModelBatchers()
		{
			this.modelBatchers = new Array<ModelBatcher>();
		}
		
		public void add(ModelBatcher mb)
		{
			modelBatchers.add(mb);
		}
		
		public void renderSolid(LightManager lights, Camera cam)
		{
			if (ModelBatcher.solidShader == null) return;
			ModelBatcher.beginSolid(lights, cam);
			for (ModelBatcher mb : modelBatchers)
			{
				mb.renderSolid();
			}
			ModelBatcher.end();
		}
		
		public void renderTransparent(LightManager lights, Camera cam)
		{
			if (ModelBatcher.transparentShader == null) return;
			ModelBatcher.beginTransparent(lights, cam);
			for (ModelBatcher mb : modelBatchers)
			{
				mb.renderTransparent();
			}
			ModelBatcher.end();
			modelBatchers.clear();
		}
	}
	
	private static class BatchedInstance implements Comparable<BatchedInstance>
	{
		private float dist;
		public final Matrix4 transform = new Matrix4();
		public float fade;
		
		public BatchedInstance set(Matrix4 transform, float dist, float fade)
		{
			this.transform.set(transform);
			this.fade = fade;
			return this;
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (equals(bi)) return 0;
			return (int) ((bi.dist - dist)*100);
		}	
	}

	@Override
	public void set(Matrix4 transform)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public void transform(Matrix4 mat)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public Matrix4 getTransform()
	{
		return null;
	}


	@Override
	public float[][] getVertexArray()
	{
		return new float[][]{new float[]{0}};
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		return out.set(0, 0, 0);
	}
}
