package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;

public class AI_Simple extends AI_Package {

	public AI_Simple(Entity entity) {
		super(entity);
	}
	
	private final PositionalData entityPos = new PositionalData();
	private final AnimationData entityAnim = new AnimationData();


	@Override
	public void inform() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float delta) {
		entity.readData(entityPos, PositionalData.class);
		entity.readData(entityAnim, AnimationData.class);
		
		entityAnim.updateAnimations = true;
		entityAnim.animation = 3;
		entityAnim.anim = "move";
		
		entityPos.forward_backward(1);
		entityPos.rotate(0,  1, 0, delta);
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		entity.writeData(entityPos, PositionalData.class);
		entity.writeData(entityAnim, AnimationData.class);


	}

}
