package com.Lyeeedar.Entities.AI;

import java.util.Random;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;

public class ActionRandomWalk extends Action
{
	public final Random ran;

	public final float time;
	public float cd = -1;
	
	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	
	public ActionRandomWalk(Random ran, float time)
	{
		this.ran = ran;
		this.time = time;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) data.get("entity");
		float delta = (Float) getData("delta", 0);
		
		entity.readData(pData);
		
		if (pData.Xcollide || pData.Zcollide)
		{
			cd = -1;
			state = BehaviourTreeState.FINISHED;
			return state;
		}
		
		entity.readData(sData);
		
		if (cd < 0)
		{
			cd = time;
			pData.rotation.set(ran.nextFloat() * 2.0f - 1.0f, 0.0f, ran.nextFloat() * 2.0f - 1.0f).nor();
			pData.up.set(GLOBALS.DEFAULT_UP);
		}
		
		pData.forward_backward(sData.speed);
		
		entity.writeData(pData);
		
		cd -= delta;
		
		parent.setDataTree("moved", true);
		
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
		return new ActionRandomWalk(ran, time);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		sData.dispose();
	}

}
