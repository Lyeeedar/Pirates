package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class AI_Ship_Control extends AI_Package {
	
	private final Controls controls;
	
	private final PositionalData entityPos = new PositionalData();
	private final AnimationData entityAnim = new AnimationData();
	private final EquipmentData entityEquip = new EquipmentData();
	private final StatusData entityStatus = new StatusData();
	
	float speed = 0;

	boolean activatecd = false;
	
	public AI_Ship_Control(Controls controls) {
		super();
		this.controls = controls;
	}

	@Override
	public void update(float delta, Entity entity) {
		
		entity.readData(entityPos, PositionalData.class);
		entity.readData(entityAnim, AnimationData.class);
		entity.readData(entityEquip, EquipmentData.class);
		entity.readData(entityStatus, StatusData.class);
		
		evaluateDamage(entityStatus, entityAnim, delta);
		
		if (entityStatus.currentHealth > 0)
		{

			if (controls.up()) speed += delta*10;
			else if (controls.down()) speed -= delta*10;
			
			if (speed == 0) 
			{
				
			}
			else if (controls.left()) entityPos.Xrotate(delta*5);
			else if (controls.right()) entityPos.Xrotate(-delta*5);
						
			if (activatecd && Gdx.input.isKeyPressed(Keys.E))
			{
				entity.activate(null);
				activatecd = false;
			}
			else if (!Gdx.input.isKeyPressed(Keys.E))
			{
				activatecd = true;
			}
		}
		
		entityPos.forward_backward(speed);
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
				
		entity.writeData(entityPos, PositionalData.class);
		entity.writeData(entityAnim, AnimationData.class);
		entity.writeData(entityEquip, EquipmentData.class);
		entity.writeData(entityStatus, StatusData.class);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
