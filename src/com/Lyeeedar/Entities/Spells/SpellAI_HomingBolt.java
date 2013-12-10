package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.Sphere;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class SpellAI_HomingBolt extends SpellAI {

	public Vector3 rotation = new Vector3();
	private Vector3 tmpVec = new Vector3();
	//public Sphere shape = Pools.obtain(Sphere.class);
	public Box shape = Pools.obtain(Box.class);
	public CollisionRay ray = Pools.obtain(CollisionRay.class);
	
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	private PositionalData pData = new PositionalData();
	private StatusData sData1 = new StatusData();
	private StatusData sData2 = new StatusData();
	
	public PerspectiveCamera cam;
	public ArrayList<EntityGraph> list = new ArrayList<EntityGraph>();
	public Vector3 tmp = new Vector3();
	
	public float speed;
	public float home;
	
	public SpellAI_HomingBolt(Vector3 rotation, float radius, float speed, float home)
	{
		this.rotation.set(rotation);
		this.ray.ray.direction.set(rotation);
		//this.sphere.radius = radius;
		shape.width = radius;
		shape.height = radius;
		shape.depth = radius;
		this.speed = speed;
		this.home = home;
		
		cam = new PerspectiveCamera(45*home, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]);
	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		cam.position.set(spell.position);
		cam.direction.set(rotation);
		cam.update();
		
		GLOBALS.WORLD.getVisible(cam, list);
		System.out.println(list.size());
		if (list.size() > 0)
		{
			spell.caster.readData(sData1, StatusData.class);
			
			Entity found = null;
			float minDist = Float.MAX_VALUE;
			for (EntityGraph eg : list)
			{
				eg.entity.readData(sData2, StatusData.class);
				
				if (sData2.factions.size() > 0)
				{
					boolean fail = false;
					for (String faction : sData2.factions)
					{
						if (fail) break;
						for (String faction2 : sData1.factions)
						{
							if (fail) break;
							if (faction.equals(faction2)) fail = true;
						}
					}
					if (fail) continue;
				}
				else
				{
					continue;
				}
				
				eg.entity.readData(pData, PositionalData.class);
				float dist = spell.position.dst2(pData.position);
				if (dist < minDist)
				{
					found = eg.entity;
					minDist = dist;
				}
			}
			
			if (found != null)
			{
				found.readData(pData, PositionalData.class);
				rotateTowards(pData.position.sub(spell.position), delta*home);
			}
			
			list.clear();
		}
		
		tmpVec.set(rotation).scl(delta).scl(speed);
		
		ray.ray.origin.set(spell.position);
		spell.position.add(tmpVec);
		ray.len = ray.ray.origin.dst(spell.position);
		ray.reset();
				
		shape.setPosition(spell.position);
		
		spell.caster.readData(pData, PositionalData.class);
		
		if (GLOBALS.WORLD.collide(shape, pData.graph) != null) return false;
		if (GLOBALS.WORLD.collide(ray, pData.graph) != null) return false;
		
		return true;
		
	}
	
	public void rotateTowards(Vector3 dst, float amount) {
		rotation.add(dst.sub(rotation).scl(amount)).nor();
	}
	
	private final Vector3 up = new Vector3(0, 1, 0);

	@Override
	public void dispose() {
		Pools.free(shape);
		Pools.free(ray);
		pData.dispose();
		sData2.dispose();
	}

}
