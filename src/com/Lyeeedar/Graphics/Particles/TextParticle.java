package com.Lyeeedar.Graphics.Particles;

import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

public class TextParticle {
	
	public Vector3 velocity;
	public Vector3 position;
	public Decal text;
	public float lifeTime;
	
	private Vector3 tmpVec = new Vector3();
	
	private float fadeLimit;
	private float fadeStep;
	
	public TextParticle (Decal text, float lifeTime, Vector3 position, Vector3 velocity)
	{
		this.text = text;
		this.lifeTime = lifeTime;
		this.position = new Vector3().set(position);
		this.velocity = new Vector3().set(velocity);
		
		fadeLimit = lifeTime/10.0f;
		fadeStep = 1.0f/fadeLimit;
	}
	
	public void update(float delta, Camera cam)
	{
		lifeTime -= delta;
		tmpVec.set(velocity).scl(delta);
		position.add(tmpVec);
		
		text.setPosition(position.x, position.y, position.z);
		text.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		
		if (lifeTime < fadeLimit)
		{
			text.setColor(1.0f, 0.0f, 0.0f, 1.0f+(fadeStep*lifeTime));
		}
	}
	
	public void render(DecalBatch batch)
	{
		batch.add(text);
	}

	public void dispose()
	{
		text.getTextureRegion().getTexture().dispose();
	}
	
}
