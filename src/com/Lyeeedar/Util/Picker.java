package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Picker
{
	public Camera cam;
	public float range;
	public ClosestRayResultSkippingCallback ray;
	public int numHits;
	
	public Entity source;
	public CircularArrayRing<Entity> output;
	
	public boolean allies;
	
	public float pickSpeed;
	public float pickCD;
	
	public Vector3 tintColour;
	
	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	private final StatusData sData2 = new StatusData();
	private final Vector3 tmpVec = new Vector3();
	private final AnimationData aData = new AnimationData();

	public boolean RUNNING = false;
	
	public Picker()
	{
		ray = new ClosestRayResultSkippingCallback();
	}
	
	public void begin()
	{
		RUNNING = true;
	}
	
	public void end()
	{
		RUNNING = false;
	}

	public void set(Entity source, CircularArrayRing<Entity> output, Camera cam, float range, int numHits, boolean allies, float pickSpeed, Vector3 tintColour)
	{
		this.source = source;
		this.output = output;
		this.cam = cam;
		this.range = range;
		this.numHits = numHits;
		this.allies = allies;
		this.pickSpeed = pickSpeed;
		this.pickCD = 0;
		this.tintColour = tintColour;
		
		ray.clearSkips();
		source.readData(pData);
		ray.setSkipObject(pData.physicsBody);
	}
	
	public void update(float delta)
	{		
		pickCD -= delta;
		if (pickCD > 0) return;
		
		source.readData(sData);

		ray.setCollisionObject(null);
        ray.setClosestHitFraction(1f);

		tmpVec.set(cam.direction).scl(range).add(cam.position);
		
		ray.getRayFromWorld().setValue(cam.position.x, cam.position.y, cam.position.z);
		ray.getRayToWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
		ray.setCollisionFilterMask(BulletWorld.FILTER_AI);
		ray.setCollisionFilterGroup(BulletWorld.FILTER_AI);
		
		GLOBALS.physicsWorld.world.rayTest(cam.position, tmpVec, ray);
		if (ray.hasHit())
		{
			Entity hit = (Entity) ray.getCollisionObject().userData;
			hit.readData(sData2);
			
			boolean valid = allies ? sData2.isAlly(sData) : !sData2.isAlly(sData);
			
			if (valid)
			{
				if (!output.contains(hit))
				{
					output.add(hit);
					
					ray.clearSkips();
					source.readData(pData);
					ray.setSkipObject(pData.physicsBody);
					for (int i = 0; i < output.size(); i++)
					{
						Entity e = output.get(i);
						e.readData(pData);
						ray.setSkipObject(pData.physicsBody);
					}
				}
				pickCD = pickSpeed;
			}
		}
	}
	
	public void tint()
	{
		for (int i = 0; i < output.size(); i++)
		{
			Entity e = output.get(i);
			e.readData(aData);
			aData.colour.set(tintColour);
			e.writeData(aData);
		}
	}
}
