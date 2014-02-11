package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.BehaviourTree.Conditional;
import com.Lyeeedar.Entities.Entity.AnimationData;

public class ConditionalAnimationLock extends Conditional
{
	private final AnimationData aData = new AnimationData();
	private final BehaviourTreeState succeed;
	private final BehaviourTreeState fail;
	
	public ConditionalAnimationLock(BehaviourTreeState succeed, BehaviourTreeState fail)
	{
		this.succeed = succeed;
		this.fail = fail;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(aData);
		
		state = aData.animationLock ? succeed : fail;
		return state;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Conditional copy()
	{
		return new ConditionalAnimationLock(succeed, fail);
	}

	@Override
	public void dispose()
	{
		aData.dispose();
	}

}
