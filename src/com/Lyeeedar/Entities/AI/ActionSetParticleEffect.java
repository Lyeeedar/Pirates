package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;

public class ActionSetParticleEffect extends Action
{
	public final String effect;
	private final PositionalData pData = new PositionalData();
	
	public ActionSetParticleEffect(String effect)
	{
		this.effect = effect;
	}
	
	@Override
	public BehaviourTreeState evaluate()
	{
		Entity entity = (Entity) getData("entity", null);
		entity.readData(pData);
		
		ParticleEffect npe = FileUtils.obtainParticleEffect(effect);
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
		return new ActionSetParticleEffect(effect);
	}

	@Override
	public void dispose()
	{
		pData.dispose();
	}

}
