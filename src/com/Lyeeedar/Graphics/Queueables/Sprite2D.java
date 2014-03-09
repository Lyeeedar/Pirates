package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.DecalBatcher;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Sprite2D implements Queueable {
	
	private final Decal decal;
	private final Vector3 position = new Vector3();
	private final Matrix4 transform = new Matrix4();
	private final float x;
	private final float y;
	
	private final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
	private float alpha = 1.0f;
	
	PositionalData pData = new PositionalData();
	
	public Sprite2D(Decal decal, float x, float y)
	{
		this.decal = decal;
		this.x = x;
		this.y = y;
		
		decal.setDimensions(x, y);
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) 
	{
		decal.setColor(colour.x, colour.y, colour.z, alpha);
		if (batches.containsKey(DecalBatcher.class)) ((DecalBatcher) batches.get(DecalBatcher.class)).add(decal);
	}

	@Override
	public void set(Entity source, Matrix4 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			position.set(0, 0, 0).mul(source.readOnlyRead(PositionalData.class).composed).mul(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			position.set(data.position).mul(offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
	}

	@Override
	public Queueable copy() {
		return new Sprite2D(Decal.newDecal(decal.getTextureRegion()), x, y);
	}

	@Override
	public void dispose() {
		pData.dispose();
	}

	@Override
	public void set(Matrix4 transform)
	{
		position.set(0, 0, 0).mul(transform);
		
	}

	@Override
	public void transform(Matrix4 mat)
	{
		position.mul(mat);
	}

	@Override
	public Matrix4 getTransform()
	{
		transform.setToTranslation(position);
		return transform;
	}
	
	@Override
	public float[][] getVertexArray()
	{
		return new float[][]{new float[]{0}};
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		return out.set(0, 0, 0).add(position);
	}

}
