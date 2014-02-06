package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Entity.PositionalData.LOCATION;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class ActionPlayerControl extends Action
{
	private final Controls controls;
	private final FollowCam cam;
	
	private boolean jump = false;
	
	private final PositionalData pData = new PositionalData();
	private final EquipmentData eData = new EquipmentData();
	private final StatusData sData = new StatusData();
		
	boolean activatecd = false;
	
	private final Vector3 nextRot = new Vector3();
	private final Vector3 tmpVec = new Vector3();
	
	public ActionPlayerControl(Controls controls, FollowCam cam)
	{
		super();
		
		this.controls = controls;
		this.cam = cam;
	}
	
	protected void use(Equipment_Slot slot, EquipmentData eData)
	{
		Equipment<?> e = eData.getEquipment(slot);
		if (e != null) e.use();
	}
	
	protected void stopUsing(Equipment_Slot slot, EquipmentData eData)
	{
		Equipment<?> e = eData.getEquipment(slot);
		if (e != null) e.stopUsing();
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData, PositionalData.class);
		entity.readData(eData, EquipmentData.class);
		entity.readData(sData, StatusData.class);

		// Evaluate controls
		int speed = sData.speed;
		if (controls.sprint()) speed = 10;
		if (Gdx.input.isKeyPressed(Keys.ALT_LEFT))
		{
			speed = 1500;
		}

		nextRot.set(0, 0, 0);

		if (controls.up()) 
		{
			tmpVec.set(cam.direction.x, 0, cam.direction.z).nor();
			if (nextRot.isZero()) nextRot.add(tmpVec);
			else nextRot.add(tmpVec).scl(0.5f);
		}
		if (controls.down()) 
		{
			tmpVec.set(-cam.direction.x, 0, -cam.direction.z).nor();
			if (nextRot.isZero()) nextRot.add(tmpVec);
			else nextRot.add(tmpVec).scl(0.5f);
		}

		if (controls.left()) 
		{
			tmpVec.set(-cam.direction.x, 0, -cam.direction.z).nor();
			tmpVec.crs(GLOBALS.DEFAULT_UP);
			if (nextRot.isZero()) nextRot.add(tmpVec);
			else nextRot.add(tmpVec).scl(0.5f);
		}
		if (controls.right()) 
		{
			tmpVec.set(cam.direction.x, 0, cam.direction.z).nor();
			tmpVec.crs(GLOBALS.DEFAULT_UP);
			if (nextRot.isZero()) nextRot.add(tmpVec);
			else nextRot.add(tmpVec).scl(0.5f);
		}

		parent.setDataTree("moved", !nextRot.isZero());
		if (!nextRot.isZero())
		{
			pData.rotation.set(nextRot.nor());
			pData.up.set(GLOBALS.DEFAULT_UP);
			pData.forward_backward(speed);
		}

		if (controls.jump() && !jump) {
			pData.velocity.y = 70;
			pData.jumpToken--;
			jump = true;
		}
		else if (!controls.jump())
		{
			jump = false;
		}


		if (controls.rightClick()) use(Equipment_Slot.RARM, eData);
		else 
		{
			stopUsing(Equipment_Slot.RARM, eData);
		}
		
		entity.writeData(pData, PositionalData.class);
		entity.writeData(eData, EquipmentData.class);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(eData, EquipmentData.class);
		stopUsing(Equipment_Slot.RARM, eData);
		entity.writeData(eData, EquipmentData.class);
	}

}
