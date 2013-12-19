package com.Lyeeedar.Entities.AI;

import java.util.List;

import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Informable;
import com.badlogic.gdx.math.Vector3;

public abstract class AI_Package implements Informable {
	
	public AI_Package()
	{
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
	
	protected Entity activate(CollisionShape<?> shape, EntityGraph graph, List<EntityGraph> list, Vector3 pos, PositionalData pData)
	{
		boolean found = GLOBALS.WORLD.collide(shape, graph, list);
		if (!found) return null;
		
		float min = Float.MAX_VALUE;
		Entity chosen = null;
		
		for (EntityGraph eg : list)
		{
			if (eg.entity != null && eg.entity.hasActivationAction())
			{
				eg.entity.readData(pData, PositionalData.class);
				float dist = pos.dst2(pData.position);
				if (dist < min) 
				{
					min = dist;
					chosen = eg.entity;
				}
			}
		}
		list.clear();
		
		return chosen;
	}
	
	protected void use(Equipment_Slot slot, EquipmentData eData)
	{
		Equipment<?> e = eData.getEquipment(slot);
		if (e != null) e.use();
	}
	
	protected void stopUsing(Equipment_Slot slot, EquipmentData eData)
	{
		Equipment<?> e = eData.getEquipment(slot);
		if (e != null) e.stopUsing();
	}
	
	@Override
	public void inform() {
		animationLock = false;
	}
	
	public abstract void update(float delta, Entity entity);
	public abstract void dispose();
}
