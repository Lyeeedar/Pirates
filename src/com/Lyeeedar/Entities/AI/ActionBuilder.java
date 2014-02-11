package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.badlogic.gdx.utils.Array;

public class ActionBuilder extends Action
{
	public final GetEntities getEntities;
	public final CheckBest checkBest;
	public final DoAction doAction;
	
	public ActionBuilder(GetEntities getEntities, CheckBest checkBest, DoAction doAction)
	{
		this.getEntities = getEntities;
		this.checkBest = checkBest;
		this.doAction = doAction;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Array<Entity> entities = getEntities.getEntities(this);
		if (entities.size == 0)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		Entity entity = checkBest.checkBest(entities, this);
		
		if (entity == null)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		state = doAction.doAction(entity, this);		
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public Action copy()
	{
		ActionBuilder nab = new ActionBuilder(getEntities.copy(), checkBest.copy(), doAction.copy());
		return nab;
	}

	@Override
	public void dispose()
	{
		getEntities.dispose();
		checkBest.dispose();
		doAction.dispose();
	}
}
