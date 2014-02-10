package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;

public class ActionApplyMovement extends Action
{
	private final PositionalData pData = new PositionalData();

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		float delta = (Float) getData("delta", 0);
		
		entity.readData(pData, PositionalData.class);
		
		pData.forward_backward(10);
		
		entity.writeData(pData, PositionalData.class);
		
		parent.setDataTree("moved", true);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

}
