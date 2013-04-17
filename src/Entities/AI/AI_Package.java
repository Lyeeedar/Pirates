package Entities.AI;

import Entities.GameEntity;

public abstract class AI_Package {
	
	protected final GameEntity entity;

	public AI_Package(GameEntity entity)
	{
		this.entity = entity;
	}
	
	public abstract void update(float delta);
}
