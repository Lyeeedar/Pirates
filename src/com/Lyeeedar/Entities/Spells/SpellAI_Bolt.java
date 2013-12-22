package com.Lyeeedar.Entities.Spells;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Sound.Sound3D;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class SpellAI_Bolt extends SpellAI {

	public Vector3 velocity = new Vector3();
	private Vector3 tmpVec = new Vector3();
	public Box shape = Pools.obtain(Box.class);
	public CollisionRay ray = Pools.obtain(CollisionRay.class);
	
	private PositionalData pData = new PositionalData();
	
	private Sound3D sound;
	
	public SpellAI_Bolt(Vector3 velocity, float radius, Sound3D sound)
	{
		this.velocity.set(velocity);
		this.ray.ray.direction.set(velocity).nor();
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		
		this.sound = sound;
	}
	
	@Override
	public boolean update(float delta, Spell spell, Camera cam) {
		
		sound.play();
		
		tmpVec.set(velocity).scl(delta);
		
		ray.ray.origin.set(spell.position);
		spell.position.add(tmpVec);
		ray.len = ray.ray.origin.dst(spell.position);
		ray.reset();
				
		shape.setPosition(spell.position);
		
		sound.setPosition(spell.position);
		sound.update(delta, cam);
		
		spell.caster.readData(pData, PositionalData.class);
		
		if (GLOBALS.WORLD.collide(shape, pData.graph) != null || GLOBALS.WORLD.collide(ray, pData.graph) != null)
		{
			sound.stop();
			return false;
		}
		
		return true;
		
	}

	@Override
	public void dispose() {
		Pools.free(shape);
		Pools.free(ray);
		pData.dispose();
		sound.dispose();
	}

}
