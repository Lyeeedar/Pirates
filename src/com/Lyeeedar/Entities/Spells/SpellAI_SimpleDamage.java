package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;

public class SpellAI_SimpleDamage extends SpellAI {

	public Box shape = Pools.obtain(Box.class);

	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	int dam;
	float cd;
	
	boolean done = false;
	
	public SpellAI_SimpleDamage(int dam, float radius, float cd)
	{
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		
		this.cd = cd;
		this.dam = dam;
	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		if (!done)
		{
			shape.setPosition(spell.position);
			spell.caster.readData(pData, PositionalData.class);
			GLOBALS.WORLD.collide(shape, pData.graph, entities);
			
			for (EntityGraph eg : entities)
			{
				eg.entity.readData(sData, StatusData.class);
				sData.damage = dam;
				eg.entity.writeData(sData, StatusData.class);
				
			}
			done = true;
		}
		
		cd -= delta;
		
		spell.effect.modEmissionTime(200);
		
		return cd > 0;
		
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		pData.dispose();
		sData.dispose();
	}

}
