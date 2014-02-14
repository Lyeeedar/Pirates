package com.Lyeeedar.Entities.Items;

import java.util.HashMap;
import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.AllHitsRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.MotionTrail;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.CircularArrayRing;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> implements AnimationListener {

	public ATTACK_STAGE[] attacks;
	public ATTACK_STAGE charge;
	
	private float linkTime;
	private float linkCD = -1;
	
	private float hitCD = 0;
	private float hitSpeed;
	
	public boolean inSwing = false;
	public boolean shouldSwing = false;
	public int animationStage = 0;
	public boolean needsAnimUpdate = false;
	public boolean charging = false;
	public final HashMap<String, Object> data = new HashMap<String, Object>();
	
	private final AnimationData aData = new AnimationData();	

	private Entity holder;

	public Weapon()
	{
		super();
	}
	
	public Weapon(ATTACK_STAGE[] attacks, SPRITESHEET spritesheet, DESCRIPTION desc, float speed, float linkTime, ATTACK_STAGE charge)
	{
		super(spritesheet, desc);
		this.hitSpeed = speed;
		this.linkTime = linkTime;
		this.attacks = attacks;
		this.charge = charge;
	}

	@Override
	public Item set(Item other) {
		super.set(other);
		
		Weapon cother = (Weapon) other;
		
		shouldSwing = cother.shouldSwing;
		linkCD = cother.linkCD;
		linkTime = cother.linkTime;
		hitCD = cother.hitCD;
		hitSpeed = cother.hitSpeed;
		attacks = cother.attacks;
		charge = cother.charge;
		
		return this;
	}

	@Override
	public Item copy() {
		return Pools.obtain(Weapon.class).set(this);
	}

	@Override
	public void update(float delta, Entity entity) {
		holder = entity;
		
		entity.readData(aData);
		if (aData.animationLock && aData.animationLocker != this) 
		{
			if (charging) 
			{
				if (charge.middle != null) charge.middle.end(entity, data);
				if (charge.end != null) charge.end.begin(entity, data);
				if (charge.end != null) charge.end.evaluate(delta, entity, data);
				if (charge.end != null) charge.end.end(entity, data);
			}
			animationStage = 0;
			linkCD = -1;
			data.clear();
			return;
		}
		
		if (linkCD > 0)
		{
			linkCD -= delta;
			
			if (shouldSwing)
			{
				animationStage = attacks[animationStage].nextAnim;
				needsAnimUpdate = true;
				inSwing = true;
			}
			
			if (linkCD <= 0 || animationStage == -1)
			{
				hitCD = hitSpeed;
				animationStage = 0;
				needsAnimUpdate = false;
				linkCD = -1;
				inSwing = false;
				data.clear();
			}
		}
		
		hitCD -= delta;
		
		if (!inSwing)
		{
			aData.animationLock = false;
		}
		else
		{
			if (attacks[animationStage].middle != null) attacks[animationStage].middle.evaluate(delta, entity, data);
		}
		
		if (charging)
		{
			if (charge.middle != null) charge.middle.evaluate(delta, entity, data);
			if (!shouldSwing)
			{
				charging = false;
				
				if (charge.middle != null) charge.middle.end(entity, data);
				if (charge.end != null) charge.end.begin(entity, data);
				if (charge.end != null) charge.end.evaluate(delta, entity, data);
				if (charge.end != null) charge.end.end(entity, data);
				
				shouldSwing = true;
				inSwing = true;
				needsAnimUpdate = true;
				animationStage = 0;
				linkCD = -1;
			}
		}
		
		if (!shouldSwing)
		{
			entity.writeData(aData);
			return;
		}
		
		if (inSwing)
		{
			
		}
		else if (charging)
		{
			
		}
		else if (animationStage == 0 && charge != null)
		{
			if (charge.begin != null) charge.begin.begin(entity, data);
			if (charge.begin != null) charge.begin.evaluate(delta, entity, data);
			if (charge.begin != null) charge.begin.end(entity, data);
			if (charge.middle != null) charge.middle.begin(entity, data);
			charging = true;
		}
		else if (hitCD < 0 && linkCD < 0)
		{
			inSwing = true;
			needsAnimUpdate = true;
			animationStage = 0;
			linkCD = -1;
		}
		
		if (needsAnimUpdate)
		{
			linkCD = -1;
			
			playAnimation(aData, attacks[animationStage].animationName);
			aData.listener = this;
			aData.animate_speed = attacks[animationStage].speed;
			aData.base_anim = "attack";
			aData.animationLocker = this;
			
			if (attacks[animationStage].begin != null) attacks[animationStage].begin.begin(entity, data);
			if (attacks[animationStage].begin != null) attacks[animationStage].begin.evaluate(delta, entity, data);
			if (attacks[animationStage].begin != null) attacks[animationStage].begin.end(entity, data);
			if (attacks[animationStage].middle != null) attacks[animationStage].middle.begin(entity, data);
			
			needsAnimUpdate = false;
		}
		
		entity.writeData(aData);
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
		aData.dispose();
	}

	@Override
	public void onEnd(AnimationDesc animation) {
		linkCD = linkTime;
		inSwing = false;
		if (attacks[animationStage].middle != null) attacks[animationStage].middle.end(holder, data);
		if (attacks[animationStage].end != null) attacks[animationStage].end.begin(holder, data);
		if (attacks[animationStage].end != null) attacks[animationStage].end.evaluate(0, holder, data);
		if (attacks[animationStage].end != null) attacks[animationStage].end.end(holder, data);
	}

	@Override
	public void onLoop(AnimationDesc animation) {
		
	}
	
	public static class ATTACK_STAGE
	{
		public String animationName;
		public float speed;
		public int nextAnim;
		public final AttackAction begin;
		public final AttackAction middle;
		public final AttackAction end;
		
		public final boolean hasAnim;
		
		public ATTACK_STAGE(AttackAction begin, AttackAction middle, AttackAction end)
		{
			this.begin = begin;
			this.middle = middle;
			this.end = end;
			
			this.hasAnim = false;
		}
		
		public ATTACK_STAGE(String animationName, float speed, int nextAnim, AttackAction begin, AttackAction middle, AttackAction end)
		{
			this.animationName = animationName;
			this.speed = speed;
			this.nextAnim = nextAnim;
			this.begin = begin;
			this.middle = middle;
			this.end = end;
			
			this.hasAnim = true;
		}
	}
	
	public static interface AttackAction
	{
		public void begin(Entity entity, HashMap<String, Object> data);
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data);
		public void end(Entity entity, HashMap<String, Object> data);
	}
	
	public static class AttackActionLockOn implements AttackAction
	{
		private final CircularArrayRing<Entity> output;
		private final Camera cam;
		private final float range;
		private final int numHits;
		private final boolean allies;
		private final float pickSpeed;
		private final Vector3 tintColour = new Vector3();
				
		private boolean begin = false;
		
		public AttackActionLockOn(Camera cam, float range, int numHits, boolean allies, float pickSpeed, Vector3 tintColour)
		{
			this.cam = cam;
			this.range = range;
			this.numHits = numHits;
			this.allies = allies;
			this.pickSpeed = pickSpeed;
			this.tintColour.set(tintColour);
			
			this.output = new CircularArrayRing<Entity>(numHits);
		}

		@Override
		public void evaluate(float delta, Entity entity,
				HashMap<String, Object> data)
		{
			
		}

		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			GLOBALS.picker.set(entity, output, cam, range, numHits, allies, pickSpeed, tintColour);
			GLOBALS.picker.begin();
		}

		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			GLOBALS.picker.end();
			data.put("targetted", GLOBALS.picker.output);
		}
	}
	
	public static class AttackSpellCast implements AttackAction
	{
		Entity baseSpell;
		AnimatedModel model;
		
		boolean shouldCast;
		HashMap<String, Object> data;
		
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		private final PositionalData pData = new PositionalData();
		private final PositionalData pData2 = new PositionalData();
		private final Matrix4 tmp = new Matrix4();
		
		private int i;
		
		public AttackSpellCast(Entity baseSpell, AnimatedModel model)
		{
			this.baseSpell = baseSpell;
			baseSpell.readData(pData);
			pData.createSensor();
			baseSpell.writeData(pData);
			this.model = model;
		}
		
		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			this.data = data;
			shouldCast = true;
			i = 0;
		}

		@Override
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data)
		{
			if (shouldCast)
			{
				shouldCast = false;
				((BehaviourTree) baseSpell.getAI()).setData("Enemy", null);
				
				if (data != null)
				{
					CircularArrayRing<Entity> targetted = (CircularArrayRing<Entity>) data.get("targetted");
					if (targetted != null && targetted.size() != 0)
					{
						((BehaviourTree) baseSpell.getAI()).setData("Enemy", targetted.get(i));
						i++;
						if (i != targetted.size())
						{
							shouldCast = true;
						}
					}
				}
				
				entity.readData(sData);
				entity.readData(pData);
				baseSpell.readData(sData2);
				baseSpell.readData(pData2);
				
				sData2.factions = sData.factions;
				
				pData2.position.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform));
				pData2.sensor.setSkipObject(pData.physicsBody);
				pData2.rotation.set(pData.rotation);
				
				baseSpell.writeData(sData2);
				baseSpell.writeData(pData2);
				
				GLOBALS.pendingEntities.add(baseSpell);
			}
		}

		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
		}
		
	}
	
	public static class AttackMotionTrail implements AttackAction
	{
		public final int damage;
		public final int damageVar;
		
		private final Random ran = new Random();
		
		private AnimatedModel model;
		private MotionTrail mt;
		
		private final Vector3 bot = new Vector3();
		private final Vector3 top = new Vector3();
		private final Matrix4 tmp = new Matrix4();
		
		public final AllHitsRayResultSkippingCallback ray = new AllHitsRayResultSkippingCallback(new Vector3(), new Vector3());
		public final Array<Entity> entities = new Array<Entity>();
		
		private final PositionalData pData = new PositionalData();
		private final StatusData sData = new StatusData();
		
		public AttackMotionTrail(AnimatedModel model, MotionTrail mt, int damage, int damageVar)
		{
			this.model = model;
			this.mt = mt;
			this.damage = damage;
			this.damageVar = damageVar;
		}
		
		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			ray.clearSkips();
		}
		
		@Override
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data)
		{
			bot.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("bottom").globalTransform));
			top.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform));
			mt.draw(bot, top);
			mt.update(bot, top);
			
			entity.readData(pData);
			
			ray.getRayFromWorld().setValue(bot.x, bot.y, bot.z);
			ray.getRayToWorld().setValue(top.x, top.y, top.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
			ray.setSkipObject(pData.physicsBody);
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
						if (e.readData(sData))
						{
							sData.damage = damage + ran.nextInt(damageVar);
							e.writeData(sData);
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
		
		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			mt.stopDraw();
		}
	}
}
