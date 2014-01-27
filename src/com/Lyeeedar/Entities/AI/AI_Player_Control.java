package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData.LOCATION;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private boolean jump = false;
	
	private final PositionalData pData = new PositionalData();
	private final AnimationData aData = new AnimationData();
	private final EquipmentData eData = new EquipmentData();
	private final StatusData sData = new StatusData();
		
	boolean activatecd = false;
	
	public AI_Player_Control(Controls controls)
	{
		super();
		
		this.controls = controls;
	}

	@Override
	public void update(float delta, Entity entity) {
		
		entity.readData(pData, PositionalData.class);
		entity.readData(aData, AnimationData.class);
		entity.readData(eData, EquipmentData.class);
		entity.readData(sData, StatusData.class);
		
		evaluateDamage(sData, aData, delta);
		
		if (sData.currentHealth > 0)
		{
			if (!aData.animationLock)
			{
				// Evaluate controls
				int speed = 50;
				if (controls.sprint()) speed = 10;
				if (Gdx.input.isKeyPressed(Keys.ALT_LEFT))
				{
					speed = 1500;
				}
				
				if (controls.up()) pData.forward_backward(speed);
				else if (controls.down()) pData.forward_backward(-speed);
				
				if (controls.left()) pData.left_right(speed);
				else if (controls.right()) pData.left_right(-speed);
				
				if (controls.jump() && !jump) {
					pData.velocity.y = 70;
					pData.jumpToken--;
					jump = true;
				}
				else if (!controls.jump())
				{
					jump = false;
				}
				
			}

			if (controls.rightClick()) use(Equipment_Slot.RARM, eData);
			else 
			{
				stopUsing(Equipment_Slot.RARM, eData);
			}

//			if (activatecd && Gdx.input.isKeyPressed(Keys.E))
//			{
//				box.center.set(entityPos.rotation).scl(2).add(entityPos.position);
//				Entity e = activate(box, entityPos.graph, list, entityPos.position, pData);
//				if (e != null) e.activate(entity);
//				activatecd = false;
//			}
//			else if (!Gdx.input.isKeyPressed(Keys.E))
//			{
//				activatecd = true;
//			}
			
			// Update animations
			
			if (aData.animationLock)
			{
				
			}
			else if (pData.location == LOCATION.AIR)
			{
				if (!aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = false;
				aData.animate_speed = 1f;
				aData.anim = "fall";
			}
			else if (pData.location == LOCATION.SEA)
			{
				if (aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = true;
				aData.animate_speed = 1f;
				aData.anim = "swim";
			}
			else if (controls.up() || controls.down() || controls.left() || controls.right()) {
				if (aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = true;
				aData.animate_speed = 1f;
				if (!controls.sprint()) aData.anim = "run";
				else aData.anim = "walk";
			}
			else {
				if (!aData.animate) aData.updateAnimations = true;
				else aData.updateAnimations = false;
				aData.animate = false;
				aData.anim = "idle";
				aData.animate_speed = 0.5f;
			}

		}
		
		pData.applyVelocity(delta);
		pData.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
				
		entity.writeData(pData, PositionalData.class);
		entity.writeData(aData, AnimationData.class);
		entity.writeData(eData, EquipmentData.class);
		entity.writeData(sData, StatusData.class);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
