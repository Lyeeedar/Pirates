package com.Lyeeedar.Graphics.Batchers;

import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class ChunkedTerrainBatch implements Batch 
{	
	private final Vector3 tmp = new Vector3();
	private final PriorityQueue<BatchedInstance> instances = new PriorityQueue<BatchedInstance>();
	private Camera cam;

	private ShaderProgram shader;
		
	public ChunkedTerrainBatch()
	{
	}
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};

	int textureHash;
	
	public void render(LightManager lights, Camera cam)
	{		
		this.cam = cam;
		if (shader == null) shader = createShader();
		
		shader.begin();
		
		shader.setUniformMatrix("u_pv", cam.combined);
		shader.setUniformf("fog_col", lights.ambientColour);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		shader.setUniformf("u_viewPos", cam.position);
		
		lights.applyLights(shader);

		while (!instances.isEmpty())
		{
			BatchedInstance bi = instances.poll();
			
			shader.setUniformMatrix("u_mm", bi.model_matrix);
			shader.setUniformf("u_colour", bi.colour);

			if (textureHash != bi.texHash)
			{
				for (int i = 0; i < bi.textures.length; i++)
				{
					bi.textures[i].bind(i);
					shader.setUniformi("u_texture"+i, i);
				}
				textureHash = bi.texHash;
			}
						
			bi.mesh.render(shader, bi.primitiveType);
			
			pool.free(bi);
		}
		
		shader.end();
		textureHash = 0;
		
	}
	
	public ShaderProgram createShader()
	{
		StringBuilder prefix = new StringBuilder();
		
		String vert = prefix.toString() + Gdx.files.internal("data/shaders/chunked_terrain.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/chunked_terrain.fragment.glsl").readString();
		
		ShaderProgram shader = new ShaderProgram(vert, frag);
	
		if (!shader.isCompiled()) System.err.println(shader.getLog());
		
		return shader;
	}
	
	public void add(Mesh mesh, int primitiveType, Texture[] textures, Vector3 colour, Matrix4 model_matrix)
	{
		if (cam == null) return;
		
		tmp.set(0, 0, 0).mul(model_matrix);
		instances.add(pool.obtain().set(mesh, primitiveType, textures, colour, model_matrix, -tmp.dst2(cam.position)));
	}

	private static class BatchedInstance implements Comparable<BatchedInstance>
	{
		private static final float tolerance = 0.01f;
		public Mesh mesh;
		private float dist;
		public Texture[] textures;
		public Vector3 colour = new Vector3();
		public Matrix4 model_matrix = new Matrix4();
		public int primitiveType;
		public int texHash;
		
		public BatchedInstance set(Mesh mesh, int primitiveType, Texture[] textures, Vector3 colour, Matrix4 model_matrix, float dist)
		{			
			this.mesh = mesh;
			this.primitiveType = primitiveType;
			this.textures = textures;
			this.texHash = textures[0].hashCode();
			if (colour != null) this.colour.set(colour);
			else this.colour.set(1.0f, 1.0f, 1.0f);
			this.model_matrix.set(model_matrix);
			this.dist = dist;
			return this;
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (bi.dist-dist < tolerance) 
			{
				return bi.texHash - texHash;
			}
			return (int) ((bi.dist - dist)*100);
		}	
	}
}
