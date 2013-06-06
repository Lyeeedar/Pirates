package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class AI_Follow extends AI_Package {
	Entity follow;
	Entity.EntityData followState = new Entity.EntityData();
	
	Vector3 tmp = new Vector3();

	public AI_Follow(Entity entity) {
		super(entity);
	}
	
	public void setFollowTarget(Entity entity)
	{
		this.follow = entity;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityState);	
		follow.readData(followState);
		
		entityState.animationData.updateAnimations = true;
		entityState.animationData.animation = 3;
		entityState.animationData.anim = "move";
		
		double a = GLOBALS.angle(entityState.positionalData.rotation, tmp.set(entityState.positionalData.position).sub(followState.positionalData.position).nor(), up);

		if (Math.abs(a) < delta*100)
		{
			entityState.positionalData.rotate(0,  1, 0, (float) a);
		}
		else if (a > 0)
		{
			entityState.positionalData.rotate(0, 1, 0, (float) (-delta*100*Math.random()));
		}
		else
		{
			entityState.positionalData.rotate(0, 1, 0, (float) (delta*100*Math.random()));
		}
		
		entityState.positionalData.forward_backward(8);
		
		entityState.positionalData.applyVelocity(delta);
		entityState.positionalData.velocity.add(0, GLOBALS.GRAVITY*delta, 0);

		entity.writeData(entityState);
	}
	
	private final Vector3 up = new Vector3(0, 1, 0);

	@Override
	public void inform() {
		// TODO Auto-generated method stub
		
	}
}
