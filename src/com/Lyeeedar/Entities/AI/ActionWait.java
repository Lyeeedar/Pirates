package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;

public class ActionWait extends Action
{
	public final float time;
	public float cd = -1;
	
	public ActionWait(float time)
	{
		this.time = time;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		float delta = (Float) getData("delta", 0);
		
		if (cd < 0)
		{
			cd = time;
		}
		
		cd -= delta;
		
		parent.setDataTree("moved", false);
		
		state = (cd < 0) ? BehaviourTreeState.FINISHED : BehaviourTreeState.RUNNING;
		return state;
	}

	@Override
	public void cancel()
	{
		cd = -1;
	}

	@Override
	public Action copy()
	{
		return new ActionWait(time);
	}

	@Override
	public void dispose()
	{
		
	}

}
