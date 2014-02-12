package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Pirates.GLOBALS;

public class ActionSetParticleEffect extends Action
{
	public final ParticleEffect pe;
	private final PositionalData pData = new PositionalData();
	
	public ActionSetParticleEffect(ParticleEffect pe)
	{
		this.pe = pe;
		pe.dispose();
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(pData);
		
		ParticleEffect npe = pe.copy();
		npe.setPosition(pData.position);
		npe.play(false);
		
		GLOBALS.unanchoredEffects.add(npe);
		
		state = BehaviourTreeState.FINISHED;
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public BehaviourTreeNode copy()
	{
		return new ActionSetParticleEffect(pe);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
	}

}
