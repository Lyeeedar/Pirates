package Entities.AI;

import Entities.Entity;

import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Pirates.GLOBALS;

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
		
		double a = GLOBALS.angle(entityState.rotation, tmp.set(entityState.position).sub(followState.position).nor(), up);

		if (Math.abs(a) < delta*100)
		{
			entityState.rotate(0,  1, 0, (float) a);
		}
		else if (a > 0)
		{
			entityState.rotate(0, 1, 0, (float) (-delta*100*Math.random()));
		}
		else
		{
			entityState.rotate(0, 1, 0, (float) (delta*100*Math.random()));
		}
		
		entityState.forward_backward(8);
		
		entityState.applyVelocity(delta);
		entityState.velocity.add(0, GLOBALS.GRAVITY*delta, 0);

		entity.writeData(entityState);
	}
	
	private final Vector3 up = new Vector3(0, 1, 0);

	@Override
	public void inform() {
		// TODO Auto-generated method stub
		
	}
}
