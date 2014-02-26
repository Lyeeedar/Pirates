package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.GLOBALS.DIRECTION;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class ActionMoveTo extends Action
{
	public final float dst;
	public final boolean towards;
	public final String key;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final StatusData sData = new StatusData();
	
	private final Vector3 tmpVec = new Vector3();
	
	public ActionMoveTo(boolean towards, float dst, String key)
	{
		this.towards = towards;
		this.dst = dst == Float.MAX_VALUE ? dst : dst*dst;
		this.key = key;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity closest = (Entity) getData(key, null);
		
		if (closest == null)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData);
		closest.readData(pData2);
		
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
		
		entity.readData(sData);
		
		tmpVec.set(pData2.position).add(0, pData2.octtreeEntry.box.extents.y/2.0f, 0).sub(pData.position);
		if (sData.stats.get(STATS.MASS) > 1) tmpVec.y = 0;
		if (!towards)
		{
			tmpVec.x *= -1;
			tmpVec.y *= -1;
			tmpVec.z *= -1;
		}
		tmpVec.nor();
		pData.rotation.set(tmpVec);
		tmpVec.crs(GLOBALS.DEFAULT_UP);
		tmpVec.crs(pData.rotation);
		pData.up.set(tmpVec);
		
		pData.velocity.set(pData.rotation).scl(sData.stats.get(STATS.SPEED));
		
		parent.setDataTree("direction", DIRECTION.FORWARD);
		
		entity.writeData(pData);
		
		state = BehaviourTreeState.RUNNING;
		return BehaviourTreeState.RUNNING;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Action copy()
	{
		return new ActionMoveTo(towards, dst, key);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		pData2.dispose();
		sData.dispose();
	}

}
