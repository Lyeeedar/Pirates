package com.Lyeeedar.Entities.Items;

import java.util.ArrayList;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Entities.Spells.SpellAI;
import com.Lyeeedar.Entities.Spells.SpellAI_Bolt;
import com.Lyeeedar.Entities.Spells.SpellAI_Explosion;
import com.Lyeeedar.Entities.Spells.SpellAI_FadeOut;
import com.Lyeeedar.Entities.Spells.SpellAI_HomingBolt;
import com.Lyeeedar.Entities.Spells.SpellAI_Launcher;
import com.Lyeeedar.Entities.Spells.SpellAI_SimpleDamage;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
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
	
	public Weapon(String animationName, SPRITESHEET spritesheet, DESCRIPTION desc, float dist, Vector3 hitBox, float speed, int damageMin, int damageVar)
	{
		super(animationName, spritesheet, desc);
		this.dist = dist;
		this.box.set(box.center, hitBox.x, hitBox.y, hitBox.z);
		this.hitSpeed = speed;
		this.damageMin = damageMin;
		this.damageVar = damageVar;
	}

	@Override
	public Item set(Item other) {
		super.set(other);
		
		Weapon cother = (Weapon) other;
		
		dist = cother.dist;
		swinging = cother.swinging;
		hitCD = cother.hitCD;
		hitSpeed = cother.hitSpeed;
		box.set(cother.box);
		damageMin = cother.damageMin;
		damageVar = cother.damageVar;
		
		return this;
	}

	@Override
	public Item copy() {
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
			
			SpellAI aimove = new SpellAI_Bolt(pData.rotation.scl(20), 1);
			SpellAI aidam = new SpellAI_Explosion(5, 0.5f, 2, 0.3f, 6);
			ParticleEffect effect = FileUtils.loadParticleEffect("data/effects/boom.effect");//new ParticleEffect(5);
			Spell s = new Spell(pData.position.add(0, 2, 0), effect, entity, aimove, aidam, new SpellAI_FadeOut());
			s.effect.getEmitter(0).addLight(false, 1.0f, 1.0f, Color.YELLOW, false, 0, 0, 0);
			GLOBALS.SPELLS.add(s);
			s.effect.getEmitter(0).getLight(GLOBALS.LIGHTS);
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
