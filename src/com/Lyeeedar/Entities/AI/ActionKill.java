package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;

public class ActionKill extends Action
{
	private final StatusData sData = new StatusData();

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		
		entity.readData(sData);
		sData.ALIVE = false;
		entity.writeData(sData);
				
		state = BehaviourTreeState.FINISHED;
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public Action copy()
	{
		return new ActionKill();
	}

	@Override
	public void dispose()
	{
		sData.dispose();
	}

}
