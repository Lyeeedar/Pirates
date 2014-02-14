package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData.LOCATION;

public class ActionUpdateAnimations extends Action
{
	private final AnimationData aData = new AnimationData();
	private final PositionalData pData = new PositionalData();

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		entity.readData(aData);
		entity.readData(pData);
		boolean moved = (Boolean) getData("moved", false);
		
		if (aData.animationLock)
		{
			
		}
		else if (pData.location == LOCATION.AIR)
		{
			if (!aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = false;
			aData.animate_speed = 1f;
			aData.anim = "fall";
			aData.base_anim = "idle";
		}
		else if (pData.location == LOCATION.SEA)
		{
			if (aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = true;
			aData.animate_speed = 1f;
			aData.anim = "swim";
			aData.base_anim = "idle";
		}
		else if (moved) {
			if (aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = true;
			aData.animate_speed = 1f;
			aData.anim = "run";
			aData.base_anim = "idle";
		}
		else {
			if (!aData.animate) aData.updateAnimations = true;
			else aData.updateAnimations = false;
			aData.animate = false;
			aData.anim = "idle";
			aData.base_anim = "idle";
			aData.animate_speed = 0.5f;
		}
		
		entity.writeData(aData);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Action copy()
	{
		return new ActionUpdateAnimations();
	}

	@Override
	public void dispose()
	{
		aData.dispose();
		pData.dispose();
	}

}