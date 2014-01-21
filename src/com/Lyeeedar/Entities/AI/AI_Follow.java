package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class AI_Follow extends AI_Package {
	Entity follow;
	
	PositionalData entityPos = new PositionalData();
	PositionalData followPos = new PositionalData();
	StatusData entityStatus = new StatusData();
	AnimationData entityAnim = new AnimationData();
	EquipmentData entityEquip = new EquipmentData();
	
	Vector3 tmp = new Vector3();
	
	float deathCD = 1f;

	public AI_Follow() {
		super();
	}
	
	public void setFollowTarget(Entity entity)
	{
		this.follow = entity;
	}

	@Override
	public void update(float delta, Entity entity) {
		
		entity.readData(entityPos, PositionalData.class);	
		follow.readData(followPos, PositionalData.class);
		
		entity.readData(entityAnim, AnimationData.class);
		entity.readData(entityStatus, StatusData.class);
		entity.readData(entityEquip, EquipmentData.class);
		
		evaluateDamage(entityStatus, entityAnim, delta);
		
		if (entityStatus.currentHealth > 0)
		{
			entityAnim.updateAnimations = true;
			entityAnim.animation = 0;
			entityAnim.anim = "move";
			
			double a = GLOBALS.angle(entityPos.rotation, tmp.set(entityPos.position).sub(followPos.position).nor(), up);
	
			if (Math.abs(a) < delta*500)
			{
				entityPos.rotate(0,  1, 0, (float) a);
			}
			else if (a > 0)
			{
				entityPos.rotate(0, 1, 0, (float) (-delta*500*Math.random()));
			}
			else
			{
				entityPos.rotate(0, 1, 0, (float) (delta*500*Math.random()));
			}
			
			boolean close = entityPos.position.dst2(followPos.position) < 2f; 
			
			if (close) use(Equipment_Slot.RARM, entityEquip);
			else 
			{
				stopUsing(Equipment_Slot.RARM, entityEquip);
				entityPos.forward_backward(10);
			}
			
		}	
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		entity.writeData(entityEquip, EquipmentData.class);
		entity.writeData(entityPos, PositionalData.class);
		entity.writeData(entityAnim, AnimationData.class);
		entity.writeData(entityStatus, StatusData.class);
	}
	
	private final Vector3 up = new Vector3(0, 1, 0);

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
