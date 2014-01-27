package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.AbstractModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public final class TexturedMesh implements Queueable {

	public final Mesh mesh;
	public final int primitive_type;
	public final Texture texture;
	public final Vector3 colour;
	public final int type;
	public final Matrix4 model_matrix = new Matrix4();
	
	public TexturedMesh(Mesh mesh, int primitive_type, Texture texture, Vector3 colour, int type)
	{
		this.mesh = mesh;
		this.primitive_type = primitive_type;
		this.texture = texture;
		this.colour = colour;
		this.type = type;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		((AbstractModelBatch) batches.get(AbstractModelBatch.class)).add(mesh, primitive_type, texture, colour, model_matrix, primitive_type, cam);
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			model_matrix.set(source.readOnlyRead(PositionalData.class).composed).translate(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			model_matrix.setToTranslation(data.position.x, data.position.y, data.position.z).translate(offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {

	}

	@Override
	public void dispose() {
		mesh.dispose();
	}

	@Override
	public Queueable copy() {
		return new TexturedMesh(mesh, primitive_type, texture, colour, type);
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
}
