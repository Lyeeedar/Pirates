package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Sound.Sound3D;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Camera;

public class SpellAI_Explosion extends SpellAI {

	public Box shape = Pools.obtain(Box.class);

	private ArrayList<EntityGraph> hit = new ArrayList<EntityGraph>();
	
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	int dam;
	float cd1;
	float delta_size;
	float delta_rate;
	
	Sound3D sound;
		
	public SpellAI_Explosion(int dam, float radius, float cd1, float delta_size, float delta_rate, Sound3D sound)
	{
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		
		this.cd1 = cd1;
		this.dam = dam;
		
		this.delta_size = delta_size;
		this.delta_rate = delta_rate;
		
		this.sound = sound;
	}
	
	@Override
	public boolean update(float delta, Spell spell, Camera cam) {
		
		sound.play();
		
		cd1 -= delta;
		spell.effect.modEmissionTime(-delta*delta_rate);
		spell.effect.modEmissionArea(delta*delta_size, delta*delta_size, delta*delta_size);
		
		shape.depth += delta*delta_size;
		shape.width += delta*delta_size;
		shape.height += delta*delta_size;
		
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
		
		sound.setPosition(spell.position);
		sound.update(delta, cam);
		
		boolean alive = cd1 > 0;
		
		if (!alive)
		{
			sound.stop();
		}
		
		return alive;
		
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		pData.dispose();
		sData.dispose();
		sound.dispose();
	}

}
