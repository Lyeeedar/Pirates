package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;

public class ActivationAction {
	
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

}
