package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;

public class ActionMoveToClosest extends Action
{
	public final float dst;
	
	public boolean towards;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final StatusData sData = new StatusData();
	
	private final Vector3 tmpVec = new Vector3();
	
	public ActionMoveToClosest(boolean towards, float dst)
	{
		this.towards = towards;
		this.dst = dst == Float.MAX_VALUE ? dst : dst*dst;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity closest = (Entity) getData("closest", null);
		
		if (closest == null)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData, PositionalData.class);
		closest.readData(pData2, PositionalData.class);
		
		if (towards)
		{
			if (pData.position.dst2(pData2.position) < dst)
			{
				state = BehaviourTreeState.FAILED;
				return BehaviourTreeState.FAILED;
			}
		}
		else
		{
			if (pData.position.dst2(pData2.position) > dst)
			{
				state = BehaviourTreeState.FAILED;
				return BehaviourTreeState.FAILED;
			}
		}
		
		entity.readData(sData, StatusData.class);
		
		tmpVec.set(pData2.position).sub(pData.position);
		tmpVec.y = 0;
		if (!towards)
		{
			tmpVec.x *= -1;
			tmpVec.z *= -1;
		}
		tmpVec.nor();
		pData.rotation.set(tmpVec);
		pData.up.set(GLOBALS.DEFAULT_UP);
		pData.forward_backward(sData.speed);
		
		parent.setDataTree("moved", true);
		
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
