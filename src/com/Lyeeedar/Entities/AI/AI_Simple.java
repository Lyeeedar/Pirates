package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;

public class AI_Simple extends AI_Package {

	public AI_Simple() {
		super();
	}
	
	private final PositionalData entityPos = new PositionalData();

	@Override
	public void update(float delta) {
		entity.readData(entityPos, PositionalData.class);
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		entity.writeData(entityPos, PositionalData.class);


	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
