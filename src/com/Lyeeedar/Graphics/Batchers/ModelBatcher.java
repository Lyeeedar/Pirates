package com.Lyeeedar.Graphics.Batchers;

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
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelBatcher implements Queueable {
	
	public static final int MAX_INSTANCES = 1;
		
	private final Mesh mesh;
	private final int primitive_type;
	private final Texture texture;
	private final Vector3 colour = new Vector3();
	private final boolean transparent;
	
	private final PriorityQueue<BatchedInstance> solidInstances = new PriorityQueue<BatchedInstance>();
	private final PriorityQueue<BatchedInstance> transparentInstances = new PriorityQueue<BatchedInstance>();
		
	private static ShaderProgram solidShader;
	private static ShaderProgram transparentShader;
	private static ShaderProgram shader;
		
	private Camera cam;
	
	boolean queued = false;
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public ModelBatcher(Mesh mesh, int primitive_type, Texture texture, Vector3 colour, boolean transparent)
	{
		this.mesh = mesh;
		this.primitive_type = primitive_type;
		this.texture = texture;
		this.colour.set(colour);
		this.transparent = transparent;
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
	public void set(Entity source, Vector3 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			add(source.readOnlyRead(PositionalData.class).position.add(offset));
		}
		else
		{
			add(source.readOnlyRead(MinimalPositionalData.class).position.add(offset));
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
	
	public void add(Vector3 position)
	{
		if (cam == null) return;
		Vector3 pos = position;
		float d = cam.position.dst(pos);
		
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
				if (transparent || fade < 1.0f) transparentInstances.add(pool.obtain().set(position, cam.position.dst2(position), fade));
				else solidInstances.add(pool.obtain().set(position, -cam.position.dst2(position), fade));
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
	
	private void flush(PriorityQueue<BatchedInstance> instances, boolean t)
	{
		shader.setUniformi("u_texture", 0);
		texture.bind(0);

		shader.setUniformf("u_colour", colour);
		
		float fade = -1;
		
		while (!instances.isEmpty())
		{			
			BatchedInstance bi = instances.poll();
			Vector3 p = bi.position;
			
			if (fade != bi.fade) 
			{
				if (t) shader.setUniformf("u_fade", bi.fade);
				fade = bi.fade;
			}
			shader.setUniformf("instance_position", p);
			
			mesh.render(shader, primitive_type);
			
			pool.free(bi);
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
		shader.end();
		shader = null;
	}
	
	public static void loadSolidShader()
	{
		String vert = Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		solidShader = new ShaderProgram(vert, frag);
	}
	
	public static void loadTransparentShader()
	{
		String vert = Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = "#define HAS_TRANSPARENT\n" + Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		transparentShader = new ShaderProgram(vert, frag);
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
			ModelBatcher.beginSolid(lights, cam);
			for (ModelBatcher mb : modelBatchers)
			{
				mb.renderSolid();
			}
			ModelBatcher.end();
		}
		
		public void renderTransparent(LightManager lights, Camera cam)
		{
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
		public final Vector3 position = new Vector3();
		public float fade;
		
		public BatchedInstance set(Vector3 position, float dist, float fade)
		{
			this.position.set(position);
			this.fade = fade;
			return this;
		}
		
		private boolean eq(Vector3 p1, Vector3 p2)
		{
			return (p1.x==p2.x && p1.y==p2.y && p1.z==p2.z);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) return false;
			return eq(position, ((BatchedInstance)obj).position);
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
}
