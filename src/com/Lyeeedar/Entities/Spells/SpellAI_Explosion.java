package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;

public class SpellAI_Explosion extends SpellAI {

	public Box shape = Pools.obtain(Box.class);

	private ArrayList<EntityGraph> hit = new ArrayList<EntityGraph>();
	
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	int dam;
	float cd1;
	float cd2;
		
	public SpellAI_Explosion(int dam, float radius, float cd1, float cd2)
	{
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		
		this.cd1 = cd1;
		this.cd2 = cd2;
		this.dam = dam;
	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		if (cd1 > 0)
		{
			cd1 -= delta;
			spell.effect.modEmissionTime(-delta);
			spell.effect.modEmissionArea(delta, delta, delta);
			
			shape.depth += delta;
			shape.width += delta;
			shape.height += delta;
			
			shape.setPosition(spell.position);
			spell.caster.readData(pData, PositionalData.class);
			GLOBALS.WORLD.collide(shape, pData.graph, entities);
			
			for (EntityGraph eg : entities)
			{
				if (hit.contains(eg)) continue;
				eg.entity.readData(sData, StatusData.class);
				sData.damage = dam;
				eg.entity.writeData(sData, StatusData.class);
				hit.add(eg);
			}
		}
		else
		{
			cd2 -= delta;
			spell.effect.modEmissionTime(200*delta);
		}
				
		return cd2 > 0;
		
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		pData.dispose();
		sData.dispose();
	}

}
