package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.Sphere;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Vector3;

public class SpellAI_Bolt extends SpellAI {

	public Vector3 velocity = new Vector3();
	private Vector3 tmpVec = new Vector3();
	//public Sphere shape = Pools.obtain(Sphere.class);
	public Box shape = Pools.obtain(Box.class);
	public CollisionRay ray = Pools.obtain(CollisionRay.class);
	
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	public SpellAI_Bolt(Vector3 velocity, float radius)
	{
		this.velocity.set(velocity);
		this.ray.ray.direction.set(velocity).nor();
		//this.sphere.radius = radius;
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		tmpVec.set(velocity).scl(delta);
		
		ray.ray.origin.set(spell.position);
		spell.position.add(tmpVec);
		ray.len = ray.ray.origin.dst(spell.position);
		ray.reset();
				
		shape.setPosition(spell.position);
		
		spell.caster.readData(pData, PositionalData.class);
		
		GLOBALS.WORLD.collide(shape, pData.graph, entities);
		
		if (entities.size() > 0)
		{
			for (EntityGraph eg : entities)
			{
				eg.entity.readData(sData, StatusData.class);
				sData.damage = 20;
				eg.entity.writeData(sData, StatusData.class);
			}
			return false;
		}
		
		if (GLOBALS.WORLD.collide(ray, pData.graph) != null) return false;
		
		return true;
		
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		Pools.free(ray);
		pData.dispose();
		sData.dispose();
	}

}
