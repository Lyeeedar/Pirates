package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;

public class ActionGravityAndMovement extends Action
{

	private final PositionalData pData = new PositionalData();
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		float delta = (Float) getData("delta", 0);
		
		entity.readData(pData, PositionalData.class);
		
		pData.applyVelocity(delta);
		pData.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		entity.writeData(pData, PositionalData.class);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

}
