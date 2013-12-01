package com.Lyeeedar.Entities.Items;

import java.util.ArrayList;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Entities.Spells.SpellAI_Bolt;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> {

	public float dist = 1;
	private float hitCD = 0;
	private float hitSpeed = 0.5f;
	private int damageMin = 1;
	private int damageVar = 1;
	
	public boolean swinging = false;
	
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	private AnimationData aData = new AnimationData();
	private Random ran = new Random();
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	
	private Box box = new Box(new Vector3(), 0.5f, 0.5f, 0.5f);

	public Weapon()
	{
		super();
	}
	
	public Weapon(String animationName, float dist, Vector3 hitBox, float speed, int damageMin, int damageVar)
	{
		super(animationName);
		this.dist = dist;
		this.box.set(box.center, hitBox.x, hitBox.y, hitBox.z);
		this.hitSpeed = speed;
		this.damageMin = damageMin;
		this.damageVar = damageVar;
	}

	@Override
	public Weapon set(Weapon other) {
		animationName = other.animationName;
		dist = other.dist;
		swinging = other.swinging;
		hitCD = other.hitCD;
		hitSpeed = other.hitSpeed;
		box.set(other.box);
		damageMin = other.damageMin;
		damageVar = other.damageVar;
		
		return this;
	}

	@Override
	public Weapon copy() {
		return Pools.obtain(Weapon.class).set(this);
	}

	@Override
	public void update(float delta, Entity entity) {
		
		hitCD -= delta;
		
		if (!swinging) 
		{
			return;
		}
		
		if (hitCD < 0)
		{
			entity.readData(aData, AnimationData.class);
			playAnimation(aData);
			entity.writeData(aData, AnimationData.class);
			
			entity.readData(pData, PositionalData.class);
			
			box.center.set(pData.rotation).scl(dist).add(pData.position);
			
			entities.clear();
			if (GLOBALS.WORLD.collide(box, pData.graph, entities))
			{
				for (EntityGraph graph : entities)
				{
					if (graph != null && graph.entity != null) 
					{
						graph.entity.readData(sData, StatusData.class);
						sData.damage = damageMin + ran.nextInt(damageVar);
						graph.entity.writeData(sData, StatusData.class);
					}
				}
			}
			
			hitCD = hitSpeed;
			
			SpellAI_Bolt ai = new SpellAI_Bolt(pData.rotation.scl(10), 1);
			ParticleEffect effect = new ParticleEffect(5);
			ParticleEmitter flame = new ParticleEmitter(0.5f, 0.5f, 0.005f, 0.4f, 0.4f, 0.4f, 0, GL20.GL_SRC_ALPHA, GL20.GL_ONE, "data/atlases/f.atlas", "flame");
			flame.createBasicEmitter(1, 1, new Color(0.99f, 0.99f, 0.0f, 0.7f), new Color(0.99f, 0.0f, 0.0f, 0.7f), 0, 0, 0);
			flame.calculateParticles();
			effect.addEmitter(flame, 
					0, 0, 0);
			Spell s = new Spell(pData.position.add(0, 1, 0), ai, effect, entity);
			GLOBALS.SPELLS.add(s);
		}
	}

	@Override
	public void use() {
		swinging = true;
	}

	@Override
	public void stopUsing() {
		swinging = false;
	}

	@Override
	public void dispose() {
		pData.dispose();
		sData.dispose();
		aData.dispose();
	}
	
}
