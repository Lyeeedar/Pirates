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
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.GLOBALS.DIRECTION;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class ActionPlayerControl extends Action
{
	private final Controls controls;
	private final FollowCam cam;
	
	private boolean jump = false;
	private boolean switchL = false;
	private boolean switchR = false;
	
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
		
		entity.readData(pData);
		entity.readData(eData);
		entity.readData(sData);

		// Evaluate controls
		int speed = sData.stats.get(STATS.SPEED);
		if (controls.sprint()) speed = 10;
		if (Gdx.input.isKeyPressed(Keys.ALT_LEFT))
		{
			speed = 1500;
		}

		nextRot.set(0, 0, 0);
		boolean lockon = cam.isLockOn();
		
		if (lockon)
		{
			pData.rotation.set(cam.direction.x, 0, cam.direction.z).nor();
			pData.up.set(GLOBALS.DEFAULT_UP);
			
			if (controls.up()) 
			{
				pData.forward_backward(speed);
				parent.setDataTree("direction", DIRECTION.FORWARD);
			}
			else if (controls.down()) 
			{
				pData.forward_backward(-speed);
				parent.setDataTree("direction", DIRECTION.BACKWARD);
			}
			else if (controls.left()) 
			{
				pData.left_right(speed);
				parent.setDataTree("direction", DIRECTION.LEFT);
			}
			else if (controls.right()) 
			{
				pData.left_right(-speed);
				parent.setDataTree("direction", DIRECTION.RIGHT);
			}
			else
			{
				parent.setDataTree("direction", DIRECTION.NONE);
			}
		}
		else
		{

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
	
			if (!nextRot.isZero())
			{
				pData.rotation.set(nextRot.nor());
				pData.up.set(GLOBALS.DEFAULT_UP);
				pData.forward_backward(speed);
				parent.setDataTree("direction", DIRECTION.FORWARD);
			}
			else
			{
				parent.setDataTree("direction", DIRECTION.NONE);
			}
		
		}

		if (controls.jump() && !jump) 
		{
			pData.velocity.y = 70;
			pData.jumpToken--;
			jump = true;
		}
		else if (!controls.jump())
		{
			jump = false;
		}

		if (controls.switchL() && !switchL) 
		{
			Equipment<?> lmain = eData.getEquipment(Equipment_Slot.LARM);
			Equipment<?> loff1 = eData.getEquipment(Equipment_Slot.LARMOFF1);
			Equipment<?> loff2 = eData.getEquipment(Equipment_Slot.LARMOFF2);
			Equipment<?> loff3 = eData.getEquipment(Equipment_Slot.LARMOFF3);
			
			eData.equip(Equipment_Slot.LARM, loff1);
			eData.equip(Equipment_Slot.LARMOFF1, loff2);
			eData.equip(Equipment_Slot.LARMOFF2, loff3);
			eData.equip(Equipment_Slot.LARMOFF3, lmain);
			
			switchL = true;
		}
		else if (!controls.switchL())
		{
			switchL = false;
		}
		
		if (controls.switchR() && !switchR) 
		{
			Equipment<?> rmain = eData.getEquipment(Equipment_Slot.RARM);
			Equipment<?> roff1 = eData.getEquipment(Equipment_Slot.RARMOFF1);
			Equipment<?> roff2 = eData.getEquipment(Equipment_Slot.RARMOFF2);
			Equipment<?> roff3 = eData.getEquipment(Equipment_Slot.RARMOFF3);
			
			eData.equip(Equipment_Slot.RARM, roff1);
			eData.equip(Equipment_Slot.RARMOFF1, roff2);
			eData.equip(Equipment_Slot.RARMOFF2, roff3);
			eData.equip(Equipment_Slot.RARMOFF3, rmain);
			
			switchR = true;
		}
		else if (!controls.switchR())
		{
			switchR = false;
		}

		if (controls.rightClick()) use(Equipment_Slot.RARM, eData);
		else 
		{
			stopUsing(Equipment_Slot.RARM, eData);
		}
		if (controls.leftClick()) use(Equipment_Slot.LARM, eData);
		else 
		{
			stopUsing(Equipment_Slot.LARM, eData);
		}
		
		entity.writeData(pData);
		entity.writeData(eData);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(eData);
		stopUsing(Equipment_Slot.LARM, eData);
		stopUsing(Equipment_Slot.RARM, eData);
		entity.writeData(eData);
	}

	@Override
	public Action copy()
	{
		return new ActionPlayerControl(controls, cam);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		eData.dispose();
		sData.dispose();		
	}

}
