package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.StatusData;

public class ActionEvaluateDamage extends Action
{
	private final StatusData sData = new StatusData();
	private final AnimationData aData = new AnimationData();
	
	protected float damageTime = 0.3f;
	protected float damageCD = 0f;
	
	protected float deathTime = 1.5f;
	protected float deathCD = deathTime;
	
	protected void evaluateDamage(StatusData sData, AnimationData aData, float delta)
	{
		if (sData.damage != 0)
		{
			sData.applyDamage();
			damageCD = damageTime;
		}
		
		damageCD -= delta;
		if (damageCD > 0.0f)
		{
			aData.colour.set(1.0f, 0.0f, 0.0f);
		}
		else
		{
			aData.colour.set(1.0f, 1.0f, 1.0f);
		}
		
		if (sData.currentHealth <= 0) 
		{
			aData.updateAnimations = true;
			aData.animate = false;
			if (deathCD > 0) deathCD -= delta;

			aData.alpha = deathCD/deathTime;
			
			if (deathCD < 0) sData.ALIVE = false;
		}
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		float delta = (Float) getData("delta", 0);
		
		entity.readData(sData);
		entity.readData(aData);
		
		evaluateDamage(sData, aData, delta);
		
		entity.writeData(aData);
		entity.writeData(sData);
		
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
		return new ActionEvaluateDamage();
	}

	@Override
	public void dispose()
	{
		sData.dispose();
		aData.dispose();
	}

}
