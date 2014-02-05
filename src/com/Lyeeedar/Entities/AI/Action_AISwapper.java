package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;

public class Action_AISwapper extends ActivationAction {

	AI_Package swape;
	AI_Package swapr;
	
	Entity activator = null;
	
	public Action_AISwapper(String desc, AI_Package swape, AI_Package swapr) {
		super(desc);
		
		this.swape = swape;
		this.swapr = swapr;
	}
	
	@Override
	public void activate(Entity activator, Entity activatee)
	{
		if (activator != null)
		{
			this.activator = activator;
		}
		else
		{
			activator = this.activator;
		}
		
		AI_Package tmp = (AI_Package) activator.getAI();
		activator.setAI(swapr);
		swapr = tmp;
		
		tmp = (AI_Package) activatee.getAI();
		activatee.setAI(swape);
		swape = tmp;
	}

}
