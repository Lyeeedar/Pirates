package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.BehaviourTree.Conditional;

public class ConditionalCollided extends Conditional
{
	private final PositionalData pData = new PositionalData();
	
	public final BehaviourTreeState succeed;
	public final BehaviourTreeState fail;
	
	public ConditionalCollided(BehaviourTreeState succeed, BehaviourTreeState fail)
	{
		this.succeed = succeed;
		this.fail = fail;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(pData);
		
		state = (pData.Xcollide || pData.Ycollide || pData.Zcollide) ? succeed : fail;
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public Conditional copy()
	{
		return new ConditionalCollided(succeed, fail);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
	}

}
