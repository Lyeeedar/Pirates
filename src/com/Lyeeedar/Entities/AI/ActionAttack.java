package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Items.Equipment;

public class ActionAttack extends Action
{
	public final float range;
	public final boolean useLeft;
	public final boolean useRight;
	public final String key;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final EquipmentData eData = new EquipmentData();
	
	public ActionAttack(float range, boolean useLeft, boolean useRight, String key)
	{
		this.range = range*range;
		this.useLeft = useLeft;
		this.useRight = useRight;
		this.key = key;
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
		Entity enemy = (Entity) getData(key, null);
		
		if (enemy == null)
		{
			entity.readData(eData);
			
			if (useLeft) stopUsing(Equipment_Slot.LARM, eData);
			if (useRight) stopUsing(Equipment_Slot.RARM, eData);
			
			entity.writeData(eData);
			
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		entity.readData(pData);
		enemy.readData(pData2);
		
		if (pData.position.dst2(pData2.position) > range)
		{
			entity.readData(eData);
			
			if (useLeft) stopUsing(Equipment_Slot.LARM, eData);
			if (useRight) stopUsing(Equipment_Slot.RARM, eData);
			
			entity.writeData(eData);
			
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		entity.readData(eData);
		
		if (useLeft) use(Equipment_Slot.LARM, eData);
		if (useRight) use(Equipment_Slot.RARM, eData);
		
		entity.writeData(eData);
		
		state = BehaviourTreeState.RUNNING;
		return BehaviourTreeState.RUNNING;
	}

	@Override
	public void cancel()
	{
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(eData);
		
		if (useLeft) stopUsing(Equipment_Slot.LARM, eData);
		if (useRight) stopUsing(Equipment_Slot.RARM, eData);
		
		entity.writeData(eData);
	}

	@Override
	public Action copy()
	{
		return new ActionAttack(range, useLeft, useLeft, key);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		pData2.dispose();
		eData.dispose();
	}

}
