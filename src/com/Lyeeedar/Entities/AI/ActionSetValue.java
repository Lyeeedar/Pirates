package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;

public class ActionSetValue extends Action
{
	private final Object value;
	private final String key;
	
	public ActionSetValue(String key, Object value)
	{
		this.value = value;
		this.key = key;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		parent.setDataTree(key, value);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{

	}

	@Override
	public Action copy()
	{
		return new ActionSetValue(key, value);
	}

	@Override
	public void dispose()
	{
		
	}

}
