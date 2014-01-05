package com.Lyeeedar.Graphics;

import java.util.Comparator;
import java.util.HashMap;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ModelBatcher implements Renderable {
	
	public static final int MAX_INSTANCES = 1;
	
	public final int max_instances;
	
	private static final SolidComparator solidComparator = new SolidComparator();
	private static final TransparentComparator transparentComparator = new TransparentComparator();
	
	private final int indexLen;
	private final Mesh mesh;
	private final int primitive_type;
	private final Texture texture;
	private final Vector3 colour = new Vector3();
	private final boolean transparent;
	
	private final Array<BatchedInstance> instances = new Array<BatchedInstance>(true, 100);
	
	private final float[] pos_array;
	
	private static ShaderProgram shader;
	
	private int index;
	
	private static Camera cam;
	
	boolean queued = false;
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public ModelBatcher(Mesh mesh, int primitive_type, Texture texture, Vector3 colour, boolean transparent)
	{
		this.indexLen = mesh.getNumIndices();
		int[] mi = new int[1];
		this.mesh = makeInstanceable(mesh, mi);
		this.max_instances = mi[0];
		this.primitive_type = primitive_type;
		this.texture = texture;
		this.colour.set(colour);
		this.transparent = transparent;
		
		pos_array = new float[max_instances*3];
	}
	

	@Override
	public void queue(float delta, HashMap<Class, Batch> batches) 
	{
		if (!queued) 
		{
			((ModelBatchers) batches.get(ModelBatchers.class)).add(this);
			queued = true;
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
		if (cam != null) instances.add(pool.obtain().set(position, cam));
	}
	
	public void render()
	{
		if (transparent) instances.sort(transparentComparator);
		else instances.sort(solidComparator);
		
		if (transparent)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
		}
		else
		{
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
		
		shader.setUniformi("u_texture", 0);
		texture.bind(0);

		shader.setUniformf("u_colour", colour);
		
		index = 0;
		
		while(index < instances.size)
		{
			int i = 0;
			for (; index < instances.size && i/3 < max_instances; index++)
			{
				Vector3 p = instances.get(index).position;
				pos_array[i++] = p.x;
				pos_array[i++] = p.y;
				pos_array[i++] = p.z;
			}
			
			shader.setUniform3fv("instance_positions", pos_array, 0, i);
			
			mesh.render(shader, primitive_type, 0, indexLen*(i/3));
		}
		
		pool.freeAll(instances);
		instances.clear();
		
		queued = false;
	}
	
	public static void begin(LightManager lights, Camera cam)
	{
		if (shader == null) loadShader();
		ModelBatcher.cam = cam;
		shader.begin();
		
		lights.applyLights(shader);
		shader.setUniformf("fog_col", lights.ambientColour);
		shader.setUniformMatrix("u_pv", cam.combined);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		shader.setUniformf("u_viewPos", cam.position);
	}
	
	public static void end()
	{
		shader.end();
	}
	
	public static void loadShader()
	{
		String vert = "#define MAX_INSTANCES "+MAX_INSTANCES+"\n"+Gdx.files.internal("data/shaders/modelbatcher.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/modelbatcher.fragment.glsl").readString();
		shader = new ShaderProgram(vert, frag);
	}
	
	public static Mesh makeInstanceable(Mesh mesh, int[] max_instances)
	{
		VertexAttributes attributes = mesh.getVertexAttributes();
		final int vertCount = mesh.getNumVertices();
		final int vertexSize = attributes.vertexSize / 4;
		
		VertexAttribute[] newAttributes = new VertexAttribute[attributes.size()+1];
		for (int i = 0; i < attributes.size(); i++)
		{
			newAttributes[i] = attributes.get(i);
		}
		newAttributes[attributes.size()] = new VertexAttribute(Usage.Generic, 1, "a_instance_index");
		
		final int newVertexSize = vertexSize + 1;
		
		float[] verts = new float[vertexSize * vertCount]; 
		mesh.getVertices(verts);
		short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);
		
		short maxIndex = 0;
		for (short s : indices)
		{
			if (s > maxIndex) maxIndex = s;
		}
		
		final int mesh_size = newVertexSize * vertCount;
		final int num_instances = Math.min(MAX_INSTANCES, Short.MAX_VALUE / maxIndex);
		
		max_instances[0] = num_instances;
		
		float[] newVerts = new float[newVertexSize * vertCount * num_instances];
		short[] newIndices = new short[num_instances * indices.length];

		for (int r = 0; r < num_instances; r++)
		{
			for (int i = 0; i < vertCount; i++)
			{
				int j = 0;
				for (; j < vertexSize; j++)
				{
					newVerts[ (mesh_size * r) + (i*newVertexSize) + j ] = verts[ (i*vertexSize) + j ];
				}
				
				newVerts[ (mesh_size * r) + (i*newVertexSize) + j] = r;
			}
		}
		
		for (int r = 0; r < num_instances; r++)
		{
			for (int i = 0; i < indices.length; i++)
			{
				newIndices[ (indices.length * r) + i ] = (short) (indices[ i ] + (indices.length * r));
			}
		}
		
		Mesh newMesh = new Mesh(true, vertCount * num_instances, num_instances * indices.length, newAttributes);
		newMesh.setVertices(newVerts);
		newMesh.setIndices(newIndices);
		
		return newMesh;
	}
	
//	public static Mesh makeInstanceable(Mesh mesh)
//	{
//		final VertexAttributes attributes = mesh.getVertexAttributes();
//		final int vertCount = mesh.getNumVertices();
//		final int vertexSize = attributes.vertexSize / 4;
//		final int triangles = mesh.getNumIndices() / 3;
//		
//		System.out.println("tris: "+triangles+" verts: "+vertCount);
//
//		final float[] verts = new float[vertexSize * vertCount];
//		mesh.getVertices(verts);	
//		final short[] indices = new short[mesh.getNumIndices()];
//		mesh.getIndices(indices);
//
//		final float[] newVerts = new float[max_instances * (triangles * 3 * (vertexSize+1))];
//		int i = 0;
//		for (int r = 0; r < max_instances; r++)
//		{
//			for (int t = 0; t < triangles; t++)
//			{
//				for (int ind = 0; ind < 3; ind++)
//				{
//					for (int v = 0; v < vertexSize; v++)
//					{
//						newVerts[i++] = verts[indices[(t*3)+ind]*vertexSize+v];
//					}
//					newVerts[i++] = r;
//				}
//			}
//		}
//		
//		VertexAttribute[] newAttributes = new VertexAttribute[attributes.size()+1];
//		for (i = 0; i < attributes.size(); i++)
//		{
//			newAttributes[i] = attributes.get(i);
//		}
//		newAttributes[attributes.size()] = new VertexAttribute(Usage.Generic, 1, "a_instance_index");
//		
//		Mesh newMesh = new Mesh(true, max_instances * triangles * 3, 0, newAttributes);
//		newMesh.setVertices(newVerts);
//		
//		return newMesh;
//	}
	
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
	
	class BatchedInstance
	{
		private static final int PRIORITY_DISCRETE_STEPS = 256;

		private int dist;
		public final Vector3 position = new Vector3();
		
		public BatchedInstance set(Vector3 position, Camera cam)
		{
			this.position.set(position);
			this.dist = (int) (PRIORITY_DISCRETE_STEPS * cam.position.dst2(position));
			return this;
		}	
	}
	
	private static class SolidComparator implements Comparator<BatchedInstance>
	{
		@Override
		public int compare(BatchedInstance o1, BatchedInstance o2) {
			return o1.dist-o2.dist;
		}
	}
	
	private static class TransparentComparator implements Comparator<BatchedInstance>
	{
		@Override
		public int compare(BatchedInstance o1, BatchedInstance o2) {
			return o2.dist-o1.dist;
		}
	}
}
