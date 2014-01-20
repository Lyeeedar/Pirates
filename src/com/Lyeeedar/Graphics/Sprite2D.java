package com.Lyeeedar.Graphics;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Sprite2D implements Queueable {
	
	private final Decal decal;
	private final Vector3 position = new Vector3();
	
	private final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
	private float alpha = 1.0f;
	private final Vector3 finalColour = new Vector3();
	
	PositionalData pData = new PositionalData();
	
	public Sprite2D(Decal decal)
	{
		this.decal = decal;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) 
	{
		decal.setColor(finalColour.x, finalColour.y, finalColour.z, alpha);
		((DecalBatcher) batches.get(DecalBatcher.class)).add(decal);
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		source.readData(pData, PositionalData.class);
		position.set(pData.position).add(offset);
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
		
		lights.getLight(position, finalColour).scl(colour);

	}

	@Override
	public Queueable copy() {
		return new Sprite2D(decal);
	}

	@Override
	public void dispose() {
		pData.dispose();
	}

}
