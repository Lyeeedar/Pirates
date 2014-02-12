package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Collision.BulletWorld.SimpleContactTestCallback;
import com.Lyeeedar.Collision.Octtree.OcttreeBox;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.GetEntities.GetEnemies;
import com.Lyeeedar.Entities.Items.Spells.SpellEffect;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.utils.Array;

public class ActionApplySpellEffect extends Action
{
	public final SpellEffect effect;
	public final OcttreeBox box;
	
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	private final StatusData sData = new StatusData();
	private final SimpleContactTestCallback sensor = new SimpleContactTestCallback();
	
	private final GetEnemies getEnemies;
	
	public ActionApplySpellEffect(SpellEffect effect, OcttreeBox box)
	{
		this.effect = effect;
		this.box = box;
		this.getEnemies = new GetEnemies(box);
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(pData);
		
		Array<Entity> entities = getEnemies.getEntities(this);
		
		for (Entity e : entities)
		{
			e.readData(pData2);
			
			e.readData(sData);
			sData.addSpellEffect(effect.copy());
			e.writeData(sData);
			
//			sensor.collided = false;
//			GLOBALS.physicsWorld.world.contactPairTest(pData.physicsBody, pData2.physicsBody, sensor);
//			if (sensor.collided)
//			{
//				e.readData(sData);
//				sData.damage = damage;
//				e.writeData(sData);
//			}
		}
		
		state = BehaviourTreeState.FINISHED;
		return state;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public BehaviourTreeNode copy()
	{
		return new ActionApplySpellEffect(effect, box);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
		pData2.dispose();
		sData.dispose();
		sensor.dispose();
		getEnemies.dispose();
	}
	
}
