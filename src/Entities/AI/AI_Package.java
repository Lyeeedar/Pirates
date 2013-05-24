package Entities.AI;

import Entities.Entity;

public abstract class AI_Package {
	
	protected final Entity entity;
	Entity.EntityData entityState = new Entity.EntityData();

	public AI_Package(Entity entity)
	{
		this.entity = entity;
	}
	
	public abstract void update(float delta);
}
