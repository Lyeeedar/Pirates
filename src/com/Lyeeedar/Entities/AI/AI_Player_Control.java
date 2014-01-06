package com.Lyeeedar.Entities.AI;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private boolean jump = false;
	
	private final PositionalData entityPos = new PositionalData();
	private final AnimationData entityAnim = new AnimationData();
	private final EquipmentData entityEquip = new EquipmentData();
	private final StatusData entityStatus = new StatusData();
	
	private final PositionalData pData = new PositionalData();
	private final ArrayList<EntityGraph> list = new ArrayList<EntityGraph>();
	private Box box = new Box(new Vector3(), 0.5f, 0.5f, 0.5f);
	
	boolean activatecd = false;
	
	public AI_Player_Control(Controls controls)
	{
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
		
		//entityPos.Xrotate(-controls.getDeltaX());
		if (controls.left()) entityPos.Xrotate(-delta*100);
		if (controls.right()) entityPos.Xrotate(delta*100);
		
		if (entityStatus.currentHealth > 0)
		{
			// Evaluate controls
			int speed = 10;
			if (controls.sprint()) speed = 30;
			if (Gdx.input.isKeyPressed(Keys.ALT_LEFT))
			{
				speed = 1500;
			}
			
			if (controls.up()) entityPos.forward_backward(speed);
			else if (controls.down()) entityPos.forward_backward(-speed);
			
			//if (controls.left()) entityPos.left_right(speed);
			//else if (controls.right()) entityPos.left_right(-speed);
			
			if (controls.jump() && entityPos.jumpToken > 0 && !jump) {
				entityPos.velocity.set(0, 30, 0);
				entityPos.jumpToken--;
				jump = true;
			}
			else if (!controls.jump())
			{
				jump = false;
			}
			
			if (Gdx.input.isKeyPressed(Keys.B))
			{
				entityPos.position.y += 10;
			}
			
			if (controls.leftClick()) use(Equipment_Slot.RARM, entityEquip);
			else 
			{
				stopUsing(Equipment_Slot.RARM, entityEquip);
			}
			
			if (activatecd && Gdx.input.isKeyPressed(Keys.E))
			{
				box.center.set(entityPos.rotation).scl(2).add(entityPos.position);
				Entity e = activate(box, entityPos.graph, list, entityPos.position, pData);
				if (e != null) e.activate(entity);
				activatecd = false;
			}
			else if (!Gdx.input.isKeyPressed(Keys.E))
			{
				activatecd = true;
			}
			
			// Update animations
			
			if (controls.sprint()) {
				if (entityAnim.animate_speed != 0.05f) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;

				entityAnim.animate_speed = 0.05f;
			}
			else
			{
				if (entityAnim.animate_speed != 0.1f) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;

				entityAnim.animate_speed = 0.1f;
			}
			
			entityAnim.anim = "move";
			
			if (controls.up() || controls.down() || controls.left() || controls.right()) {
				if (entityAnim.animate) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;
				entityAnim.animate = true;
			}
			else {
				if (!entityAnim.animate) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;
				entityAnim.animate = false;
			}
			
			entityAnim.animationLock = false;

		}
		
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
