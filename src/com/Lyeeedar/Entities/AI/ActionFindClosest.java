package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeShape;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.utils.Array;

public class ActionFindClosest extends Action
{
	protected final OcttreeShape shape;
	protected final boolean enemy;
	protected final String[] factions;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final StatusData sData = new StatusData();
	private final StatusData sData2 = new StatusData();
	
	private final Array<Entity> entities = new Array<Entity>(false, 16);
	
	public ActionFindClosest(OcttreeShape shape, boolean enemy, String[] factions)
	{
		this.shape = shape;
		this.enemy = enemy;
		this.factions = factions;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		
		entity.readData(pData, PositionalData.class);
		entity.readData(sData, StatusData.class);
		
		shape.setPosition(pData.position);
		shape.setRotation(pData.rotation);
		
		entities.clear();
		GLOBALS.renderTree.collectAll(entities, shape, true, Octtree.MASK_AI);
		if (entities.size == 0)
		{
			state = BehaviourTreeState.FAILED;
			return BehaviourTreeState.FAILED;
		}
		
		Entity closest = null;
		float dst = Float.MAX_VALUE;
		
		for (int i = 0; i < entities.size; i++)
		{
			Entity tmp = entities.get(i);
			
			tmp.readData(sData2, StatusData.class);
			
			if (enemy)
			{
				if (sData.isAlly(sData2)) continue;
			}
			else
			{
				if (!sData.isAlly(sData2)) continue;
			}
			
			if (factions != null)
			{
				boolean fail = false;
				for (String faction : factions)
				{
					if (!sData2.factions.contains(faction, false))
					{
						fail = true;
						break;
					}
				}
				if (fail) continue;
			}
			
			tmp.readData(pData2, PositionalData.class);
			float tdst = pData.position.dst2(pData2.position);
			
			if (tdst < dst)
			{
				dst = tdst;
				closest = tmp;
			}
		}
		
		parent.setDataTree("closest", closest);
		
		if (enemy)
		{
			parent.setDataTree("enemy", closest);
		}
		else
		{
			parent.setDataTree("ally", closest);
		}
		
		state = BehaviourTreeState.FINISHED;
		return BehaviourTreeState.FINISHED;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}
	
}
