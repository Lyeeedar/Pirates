package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class ActionMove extends Action
{
	private final PositionalData pData = new PositionalData();
	public final Vector3 velocity = new Vector3();
	
	private final Vector3 tmpVec = new Vector3();
	
	public ActionMove(Vector3 velocity)
	{
		this.velocity.set(velocity);
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		float delta = (Float) getData("delta", 0);
		
		entity.readData(pData, PositionalData.class);
		
		pData.velocity.add(velocity);
		pData.rotation.set(pData.velocity).nor();
		
		tmpVec.set(pData.rotation).crs(GLOBALS.DEFAULT_UP);
		
		pData.up.set(pData.rotation).crs(tmpVec);
		
		entity.writeData(pData, PositionalData.class);
		
		parent.setDataTree("moved", true);
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

}
