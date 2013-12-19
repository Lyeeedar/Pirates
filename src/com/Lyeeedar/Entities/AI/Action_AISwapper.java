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
		
		AI_Package tmp = activator.getAI();
		activator.setAI(swapr);
		swapr = tmp;
		
		tmp = activatee.getAI();
		activatee.setAI(swape);
		swape = tmp;
	}

}
