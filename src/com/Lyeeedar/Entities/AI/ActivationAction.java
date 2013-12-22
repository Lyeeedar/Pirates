package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Util.Informable;

public class ActivationAction implements Informable {
	
	String desc;
	Entity e;
	
	public ActivationAction(String desc)
	{
		this.desc = desc;
	}
	
	public void set(Entity e)
	{
		this.e = e;
	}
	
	public String getDesc()
	{
		return desc;
	}
	
	public void activate(Entity activator, Entity Activatee)
	{
		
	}

	@Override
	public void inform() {
		
	}

}
