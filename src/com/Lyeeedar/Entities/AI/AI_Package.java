package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Informable;

public abstract class AI_Package implements Informable {
	
	protected final Entity entity;

	public AI_Package(Entity entity)
	{
		this.entity = entity;
	}
	
	protected float damageTime = 0.3f;
	protected float damageCD = 0f;
	
	protected float deathTime = 1.5f;
	protected float deathCD = deathTime;
	
	protected void evaluateDamage(StatusData sData, AnimationData aData, float delta)
	{
		if (sData.damage != 0)
		{
			sData.applyDamage();
			damageCD = damageTime;
		}
		
		damageCD -= delta;
		if (damageCD > 0.0f)
		{
			aData.colour.set(1.0f, 0.0f, 0.0f);
		}
		else
		{
			aData.colour.set(1.0f, 1.0f, 1.0f);
		}
		
		if (sData.currentHealth <= 0) 
		{
			aData.updateAnimations = true;
			aData.animate = false;
			if (deathCD > 0) deathCD -= delta;

			aData.alpha = deathCD/deathTime;
			
			if (deathCD < 0) sData.ALIVE = false;
		}
	}
	
	protected boolean animationLock = false;
	
	protected void basicAttack(boolean attack, Equipment_Slot slot, EquipmentData eData)
	{
		if (attack)
		{
			eData.getEquipment(slot).use();
		}
		else
		{
			eData.getEquipment(slot).stopUsing();
		}
	}
	
	@Override
	public void inform() {
		animationLock = false;
	}
	
	public abstract void update(float delta);
	public abstract void dispose();
}
