package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Camera;

public class SpellAI_SimpleDamage extends SpellAI {

	public Box shape = Pools.obtain(Box.class);

	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	int dam;
		
	public SpellAI_SimpleDamage(int dam, float radius)
	{
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		
		this.dam = dam;
	}
	
	@Override
	public boolean update(float delta, Spell spell, Camera cam) {
		
		shape.setPosition(spell.position);
		spell.caster.readData(pData, PositionalData.class);
		GLOBALS.WORLD.collide(shape, pData.graph, entities);
		
		for (EntityGraph eg : entities)
		{
			eg.entity.readData(sData, StatusData.class);
			sData.damage = dam;
			eg.entity.writeData(sData, StatusData.class);
			
		}
		
		return false;
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		pData.dispose();
		sData.dispose();
	}
}
