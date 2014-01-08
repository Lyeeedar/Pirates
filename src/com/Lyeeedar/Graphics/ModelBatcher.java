package com.Lyeeedar.Graphics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelBatcher implements Renderable {
	
	public static final int MAX_INSTANCES = 1;
		
	private final Mesh mesh;
	private final int primitive_type;
	private final Texture texture;
	private final Vector3 colour = new Vector3();
	
	private final PriorityQueue<BatchedInstance> instances = new PriorityQueue<BatchedInstance>();
		
	private static ShaderProgram shader;
		
	private Camera cam;
	
	boolean queued = false;
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public ModelBatcher(Mesh mesh, int primitive_type, Texture texture, Vector3 colour)
	{
		this.mesh = mesh;//makeInstanceable(mesh, mi);
		this.primitive_type = primitive_type;
		this.texture = texture;
		this.colour.set(colour);
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
	public Renderable copy() {
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
			
			if (fade > 0.0f) instances.add(pool.obtain().set(position, cam, fade));
		}
	}
	
	public void render()
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
				shader.setUniformf("u_fade", bi.fade);
				fade = bi.fade;
			}
			shader.setUniformf("instance_position", p);
			
			mesh.render(shader, primitive_type);
			
			pool.free(bi);
		}
		
		queued = false;
	}
	
	public static void begin(LightManager lights, Camera cam)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		if (shader == null) loadShader();
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
	}
	
	public static void loadShader()
	{
		String vert = Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		shader = new ShaderProgram(vert, frag);
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
		
		public void render(LightManager lights, Camera cam)
		{
			ModelBatcher.begin(lights, cam);
			for (ModelBatcher mb : modelBatchers)
			{
				mb.render();
			}
			ModelBatcher.end();
			modelBatchers.clear();
		}
	}
	
	class BatchedInstance implements Comparable<BatchedInstance>
	{
		private float dist;
		public final Vector3 position = new Vector3();
		public float fade;
		
		public BatchedInstance set(Vector3 position, Camera cam, float fade)
		{
			this.position.set(position);
			this.dist = cam.position.dst2(position);
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
			return eq(position, ((BatchedInstance)obj).position);
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (equals(bi)) return 0;
			return (int) ((bi.dist - dist)*100);
		}	
	}
}
