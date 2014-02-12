package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.BehaviourTree.Conditional;

public class ConditionalTimer extends Conditional
{
	public final float duration;
	public final BehaviourTreeState succeed;
	public final BehaviourTreeState fail;
	
	private float rDur;
	
	public ConditionalTimer(float duration, BehaviourTreeState succeed, BehaviourTreeState fail)
	{
		this.duration = duration;
		this.rDur = duration;
		this.succeed = succeed;
		this.fail = fail;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		float delta = (Float) getData("delta", 0);
		
		rDur -= delta;
		
		state = rDur < 0 ? succeed : fail;
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public BehaviourTreeNode copy()
	{
		return new ConditionalTimer(duration, succeed, fail);
	}

	@Override
	public void dispose()
	{
		
	}

}
