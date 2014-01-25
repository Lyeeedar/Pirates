package com.Lyeeedar.Entities.Items;

import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.AllHitsRayResultSkippingCallback;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Graphics.AnimatedModel;
import com.Lyeeedar.Graphics.MotionTrail;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> implements AnimationListener {

	private float hitCD = 0;
	private float hitSpeed = 1f;
	private int damageMin = 1;
	private int damageVar = 1;
	
	public boolean inSwing = false;
	public boolean shouldSwing = false;
	
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	private AnimationData aData = new AnimationData();
	private Random ran = new Random();
		
	private AnimatedModel model;
	private MotionTrail mt;
	
	private final Vector3 bot = new Vector3();
	private final Vector3 top = new Vector3();
	private final Matrix4 tmp = new Matrix4();
	
	public final AllHitsRayResultSkippingCallback ray = new AllHitsRayResultSkippingCallback(new Vector3(), new Vector3());
	public final Array<Entity> entities = new Array<Entity>();

	public Weapon()
	{
		super();
	}
	
	public Weapon(String animationName, SPRITESHEET spritesheet, DESCRIPTION desc, float speed, int damageMin, int damageVar, AnimatedModel model, MotionTrail mt)
	{
		super(animationName, spritesheet, desc);
		this.hitSpeed = speed;
		this.damageMin = damageMin;
		this.damageVar = damageVar;
		this.model = model;
		this.mt = mt;
	}

	@Override
	public Item set(Item other) {
		super.set(other);
		
		Weapon cother = (Weapon) other;
		
		shouldSwing = cother.shouldSwing;
		hitCD = cother.hitCD;
		hitSpeed = cother.hitSpeed;
		damageMin = cother.damageMin;
		damageVar = cother.damageVar;
		model = cother.model;
		mt = cother.mt;
		
		return this;
	}

	@Override
	public Item copy() {
		return Pools.obtain(Weapon.class).set(this);
	}

	@Override
	public void update(float delta, Entity entity) {
		
		hitCD -= delta;
		
		if (!inSwing)
		{
			if (mt != null) mt.stopDraw();
			entity.readData(aData, AnimationData.class);
			aData.animationLock = false;
			entity.writeData(aData, AnimationData.class);
		}
		else if (model != null && mt != null)
		{
			bot.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("bottom").globalTransform));
			top.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform));
			mt.draw(bot, top);
			mt.update(bot, top);
			
			entity.readData(pData, PositionalData.class);
			
			ray.getRayFromWorld().setValue(bot.x, bot.y, bot.z);
			ray.getRayToWorld().setValue(top.x, top.y, top.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
			ray.setSkipObject(pData.physicsBody);
			//ray.setCollisionObjects(null);
			ray.setCollisionObject(null);
			
			GLOBALS.physicsWorld.world.rayTest(bot, top, ray);
			if (ray.hasHit())
			{
				btCollisionObjectConstArray arr = ray.getCollisionObjects();
				for (int i = 0; i < arr.size(); i++)
				{
					if (!ray.hasSkip(arr.at(i).getCPointer()))
					{
						Entity e = (Entity) arr.at(i).userData;
						if (e.readData(sData, StatusData.class))
						{
							sData.damage = damageMin + ran.nextInt(damageVar);
							e.writeData(sData, StatusData.class);
							ray.setSkipObject(arr.at(i));
						}
	//					else
	//					{
	//						shouldSwing = false;
	//						inSwing = false;
	//						ray.clearSkips();
	//					}
					}
				}
			}
		}
		
		if (!shouldSwing || inSwing) 
		{
			return;
		}
		
		if (hitCD < 0)
		{
			this.inSwing = true;
			
			entity.readData(aData, AnimationData.class);
			playAnimation(aData);
			aData.listener = this;
			aData.animate_speed = hitSpeed;
			entity.writeData(aData, AnimationData.class);
			
			entity.readData(pData, PositionalData.class);
					
//			entities.clear();
//			if (GLOBALS.WORLD.collide(box, pData.graph, entities))
//			{
//				for (EntityGraph graph : entities)
//				{
//					if (graph != null && graph.entity != null) 
//					{
//						graph.entity.readData(sData, StatusData.class);
//						sData.damage = damageMin + ran.nextInt(damageVar);
//						graph.entity.writeData(sData, StatusData.class);
//					}
//				}
//			}
			
			hitCD = hitSpeed;
			
//			SpellAI aimove = new SpellAI_Bolt(pData.rotation.scl(20), 1, new Sound3D(FileUtils.loadSound("data/sfx/fire_loop_01.mp3"), 0.1f, 1.0f, true));
//			SpellAI aidam = new SpellAI_Explosion(5, 0.5f, 2, 0.3f, 6, new Sound3D(FileUtils.loadSound("data/sfx/explosion_distant_002.mp3"), 0.01f, 1.0f, false));
//			ParticleEffect effect = FileUtils.loadParticleEffect("data/effects/boom.effect");
//			Spell s = new Spell(pData.position.add(0, 2, 0), effect, entity, aimove, aidam, new SpellAI_FadeOut());
//			s.effect.getEmitter(0).addLight(false, 1.0f, 1.0f, Color.YELLOW, false, 0, 0, 0);
//			GLOBALS.SPELLS.add(s);
//			s.effect.getEmitter(0).getLight(GLOBALS.LIGHTS);
		}
	}

	@Override
	public void use() {
		shouldSwing = true;
	}

	@Override
	public void stopUsing() {
		shouldSwing = false;
	}

	@Override
	public void dispose() {
		pData.dispose();
		sData.dispose();
		aData.dispose();
	}

	@Override
	public void onEnd(AnimationDesc animation) {
		inSwing = false;
		ray.clearSkips();
	}

	@Override
	public void onLoop(AnimationDesc animation) {
		// TODO Auto-generated method stub
		
	}
	
}
