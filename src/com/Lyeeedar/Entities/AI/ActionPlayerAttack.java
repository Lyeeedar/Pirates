package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class ActionPlayerAttack extends Action
{
	private final Controls controls;
		
	private final EquipmentData eData = new EquipmentData();
	private final StatusData sData = new StatusData();
				
	public ActionPlayerAttack(Controls controls)
	{
		super();
		
		this.controls = controls;
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
		
		entity.readData(eData);
		entity.readData(sData);

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
		return new ActionPlayerAttack(controls);
	}

	@Override
	public void dispose()
	{
		eData.dispose();
		sData.dispose();		
	}

}
