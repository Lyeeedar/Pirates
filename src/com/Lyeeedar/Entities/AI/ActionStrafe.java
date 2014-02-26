package com.Lyeeedar.Entities.AI;

import java.util.Random;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.GLOBALS.DIRECTION;
import com.badlogic.gdx.math.Vector3;

public class ActionStrafe extends Action
{
	public final float time;
	public boolean right;
	public final String key;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final StatusData sData = new StatusData();
	
	private final Vector3 tmpVec = new Vector3();
	
	private final Random ran = new Random();
	
	public float cd = -1;
	
	public ActionStrafe(float time, String key)
	{
		this.time = time;
		this.key = key;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		float delta = (Float) getData("delta", 0);
		
		if (cd < 0)
		{
			cd = time;
			right = ran.nextInt(2) == 1;
		}
		
		cd -= delta;
		
		Entity closest = (Entity) getData(key, null);
		
		if (closest == null)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData);
		closest.readData(pData2);
		
		entity.readData(sData);
		
		tmpVec.set(pData2.position).add(0, pData2.octtreeEntry.box.extents.y/2.0f, 0).sub(pData.position);
		if (sData.stats.get(STATS.MASS) > 1) tmpVec.y = 0;
		tmpVec.nor();
		pData.rotation.set(tmpVec);
		tmpVec.crs(GLOBALS.DEFAULT_UP);
		tmpVec.crs(pData.rotation);
		pData.up.set(tmpVec);
		
		if (right) pData.left_right(sData.stats.get(STATS.SPEED));
		else pData.left_right(-sData.stats.get(STATS.SPEED));
		
		DIRECTION dir = right ? DIRECTION.RIGHT : DIRECTION.LEFT ;
		
		parent.setDataTree("direction", dir);
		
		entity.writeData(pData);
		
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
		return new ActionStrafe(time, key);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		pData2.dispose();
		sData.dispose();
	}

}
