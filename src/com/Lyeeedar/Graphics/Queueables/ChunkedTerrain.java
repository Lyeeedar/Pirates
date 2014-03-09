package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ChunkedTerrainBatch;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class ChunkedTerrain implements Queueable
{
	public final Mesh mesh;
	public final int primitive_type;
	public final Texture[] textures;
	public final Vector3 colour;
	public final int type;
	public final Matrix4 model_matrix = new Matrix4();
	public final String name;
	
	public ChunkedTerrain(String name, Mesh mesh, int primitive_type, Texture[] textures, Vector3 colour, int type)
	{
		this.mesh = mesh;
		this.primitive_type = primitive_type;
		this.textures = textures;
		this.colour = colour;
		this.type = type;
		this.name = name;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		if (batches.containsKey(ChunkedTerrainBatch.class)) ((ChunkedTerrainBatch) batches.get(ChunkedTerrainBatch.class)).add(mesh, primitive_type, textures, colour, model_matrix);
	}

	@Override
	public void set(Entity source, Matrix4 offset) {
		
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			model_matrix.set(source.readOnlyRead(PositionalData.class).composed).mul(offset);
		}
		else
		{
			model_matrix.setToTranslation(source.readOnlyRead(MinimalPositionalData.class).position).mul(offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {

	}

	@Override
	public void dispose() {
	}

	@Override
	public Queueable copy() {
		return new TexturedMesh(name, mesh, primitive_type, textures, colour, type);
	}

	@Override
	public void set(Matrix4 mat)
	{
		model_matrix.set(mat);
	}

	@Override
	public void transform(Matrix4 mat)
	{
		model_matrix.mul(mat);
	}

	@Override
	public Matrix4 getTransform()
	{
		return model_matrix;
	}

	@Override
	public float[][] getVertexArray()
	{
		float[][] varray = FileUtils.getVertexArray(name);
		if (varray != null) return varray;
		
		final int nverts = mesh.getNumVertices();
		final int vsize = mesh.getVertexSize() / 4;
		float[] vertices = mesh.getVertices(new float[nverts*vsize]);
		int poff = mesh.getVertexAttributes().getOffset(Usage.Position);
		
		varray = new float[nverts][3];
		
		for (int i = 0; i < nverts; i++)
		{
			varray[i][0] = vertices[poff+(i*vsize)+0];
			varray[i][1] = vertices[poff+(i*vsize)+1];
			varray[i][2] = vertices[poff+(i*vsize)+2];
		}
		
		FileUtils.storeVertexArray(name, varray);
		
		return varray;
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		return out.set(values[0], values[1], values[2]).mul(model_matrix);
	}

}
