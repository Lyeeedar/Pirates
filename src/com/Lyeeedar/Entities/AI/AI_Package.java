package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Util.Informable;

public abstract class AI_Package implements Informable {
	
	protected final Entity entity;

	public AI_Package(Entity entity)
	{
		this.entity = entity;
	}
	
	public abstract void update(float delta);
	public abstract void dispose();
}
