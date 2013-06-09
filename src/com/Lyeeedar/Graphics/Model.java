package com.Lyeeedar.Graphics;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public final class Model implements Renderable {

	public final Mesh mesh;
	public final int primitive_type;
	public final Texture texture;
	public final Vector3 colour;
	public final int type;
	public final Matrix4 model_matrix = new Matrix4();
	
	private final PositionalData pData = new PositionalData();
	
	public Model(Mesh mesh, int primitive_type, Texture texture, Vector3 colour, int type)
	{
		this.mesh = mesh;
		this.primitive_type = primitive_type;
		this.texture = texture;
		this.colour = colour;
		this.type = type;
	}

	@Override
	public void queue(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		modelBatch.add(mesh, primitive_type, texture, colour, model_matrix, primitive_type);
	}

	@Override
	public void set(Entity source) {
		
		source.readData(pData, PositionalData.class);
		
		model_matrix.set(pData.composed);
	}

	@Override
	public void update(float delta, Camera cam) {

	}

	@Override
	public void dispose() {
		mesh.dispose();
	}
}
