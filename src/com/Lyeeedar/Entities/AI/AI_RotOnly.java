package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;

public class AI_RotOnly extends AI_Package {

	Controls controls;
	
	public AI_RotOnly(Controls controls) {
		super();
		this.controls = controls;
	}
	
	private final PositionalData entityPos = new PositionalData();


	@Override
	public void inform() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float delta, Entity entity) {
		entity.readData(entityPos, PositionalData.class);
		
		entityPos.Xrotate(-controls.getDeltaX());
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		entity.writeData(entityPos, PositionalData.class);


	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

}
