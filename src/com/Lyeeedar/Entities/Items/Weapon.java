package com.Lyeeedar.Entities.Items;

import java.util.HashMap;
import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.AllHitsRayResultSkippingCallback;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Items.Equipment.EquipmentGraphics;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.MotionTrail;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.CircularArrayRing;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.physics.bullet.collision.btTriangleRaycastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> implements AnimationListener {

	public ATTACK_STAGE[] attacks;
	public ATTACK_STAGE charge;
	public ATTACK_STAGE recoil;
	
	private ATTACK_STAGE currentStage;
	
	private float linkTime;
	private float linkCD = -1;
	
	private float hitCD = 0;
	private float hitSpeed;
	
	public boolean inSwing = false;
	public boolean shouldSwing = false;
	public int animationStage = 0;
	public boolean charging = false;
	public final HashMap<String, Object> data = new HashMap<String, Object>();
	
	private final AnimationData aData = new AnimationData();

	private Entity holder;

	public Weapon()
	{
		super();
	}
	
	public Weapon(ATTACK_STAGE[] attacks, EquipmentGraphics equipmentGraphics, DESCRIPTION desc, float speed, float linkTime, ATTACK_STAGE charge, ATTACK_STAGE recoil)
	{
		super(equipmentGraphics, desc);
		this.hitSpeed = speed;
		this.linkTime = linkTime;
		this.attacks = attacks;
		this.charge = charge;
		this.recoil = recoil;
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
		recoil = cother.recoil;
		
		return this;
	}

	@Override
	public Item copy() {
		return Pools.obtain(Weapon.class).set(this);
	}
	
	private void nextStage()
	{
		animationStage = currentStage != null ? currentStage.nextAnim : 0 ; 
	}
	
	private void switchStage(Entity entity, HashMap<String, Object> data, ATTACK_STAGE nstage, boolean runEnd)
	{		
		if (currentStage != null)
		{
			if (currentStage.middle != null) currentStage.middle.end(entity, data);
			if (runEnd && currentStage.end != null) currentStage.end.begin(entity, data);
			if (runEnd && currentStage.end != null) currentStage.end.evaluate(0, entity, data);
			if (runEnd && currentStage.end != null) currentStage.end.end(entity, data);
		}
		
		currentStage = nstage;
		
		if (currentStage != null)
		{
			if (currentStage.begin != null) currentStage.begin.begin(entity, data);
			if (currentStage.begin != null) currentStage.begin.evaluate(0, entity, data);
			if (currentStage.begin != null) currentStage.begin.end(entity, data);
			if (currentStage.middle != null) currentStage.middle.begin(entity, data);
			
			if (currentStage.hasAnim)
			{
				playAnimation(aData, currentStage.animationName);
				aData.listener = this;
				aData.animate_speed = currentStage.speed;
				aData.base_anim = "attack";
				aData.animationLocker = this;
			}
		}
	}
	
	private void end(Entity entity, HashMap<String, Object> data)
	{
		animationStage = 0;
		linkCD = -1;
		inSwing = false;
		hitCD = hitSpeed;
		charging = false;
		data.clear();
	}

	@Override
	public void update(float delta, Entity entity) {
		holder = entity;
		
		hitCD -= delta;
		
		entity.readData(aData);
		if (aData.animationLock && aData.animationLocker != this) 
		{
			switchStage(entity, data, null, false);
			end(entity, data);
			return;
		}
		
		if (currentStage != null && currentStage.middle != null) currentStage.middle.evaluate(delta, entity, data);
		
		if (linkCD > 0)
		{
			linkCD -= delta;
			
			if (linkCD <= 0 || animationStage == -1)
			{
				switchStage(entity, data, null, true);
				end(entity, data);
			}
		}
		
		if (recoil != null && currentStage != null && currentStage.middle != null)
		{
			if (currentStage.middle.needsRecoil())
			{
				switchStage(entity, data, recoil, false);
				inSwing = true;
				linkCD = -1;
			}
		}
		
		if (shouldSwing && !inSwing)
		{
			switchStage(entity, data, attacks[animationStage], true);
			inSwing = true;
			linkCD = -1;
		}
		
		if (!inSwing)
		{
			aData.animationLock = false;
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
		end(holder, data);
		switchStage(holder, data, null, false);
		
		if (charge != null) charge.dispose();
		if (recoil != null) recoil.dispose();
		
		for (ATTACK_STAGE as : attacks)
		{
			as.dispose();
		}
	}

	@Override
	public void onEnd(AnimationDesc animation) {
		linkCD = linkTime;
		inSwing = false;
		nextStage();
		switchStage(holder, data, null, true);
		
		holder.readData(aData);
		aData.anim = "";
		holder.writeData(aData);
	}

	@Override
	public void onLoop(AnimationDesc animation) {
		
	}
	
	@Override
	public void addRequiredQueueables(AnimatedModel model)
	{
		if (charge != null) charge.addRequiredQueueables(model);		
		if (recoil != null) recoil.addRequiredQueueables(model);
		
		for (ATTACK_STAGE as : attacks)
		{
			as.addRequiredQueueables(model);
		}
	}
	
	public static class ATTACK_STAGE
	{
		public String animationName;
		public float speed;
		public int nextAnim;
		public final AttackAction begin;
		public final AttackAction middle;
		public final AttackAction end;
		public final float lr;
		public final float fb;
		
		public final boolean hasAnim;
		
		public ATTACK_STAGE(AttackAction begin, AttackAction middle, AttackAction end, float lr, float fb)
		{
			this.begin = begin;
			this.middle = middle;
			this.end = end;
			
			this.lr = lr;
			this.fb = fb;
			
			this.hasAnim = false;
		}
		
		public ATTACK_STAGE(String animationName, float speed, int nextAnim, AttackAction begin, AttackAction middle, AttackAction end, float lr, float fb)
		{
			this.animationName = animationName;
			this.speed = speed;
			this.nextAnim = nextAnim;
			this.begin = begin;
			this.middle = middle;
			this.end = end;
			
			this.lr = lr;
			this.fb = fb;
			
			this.hasAnim = true;
		}
		
		public void addRequiredQueueables(AnimatedModel model)
		{
			if (begin != null) begin.addRequiredQueueables(model);
			if (middle != null) middle.addRequiredQueueables(model);
			if (end != null) end.addRequiredQueueables(model);
		}
		
		public void dispose()
		{
			if (begin != null) begin.dispose();
			if (middle != null) middle.dispose();
			if (end != null) end.dispose();
		}
	}
	
	public static interface AttackAction
	{
		public void begin(Entity entity, HashMap<String, Object> data);
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data);
		public void end(Entity entity, HashMap<String, Object> data);
		
		public void addRequiredQueueables(AnimatedModel model);
		
		public boolean needsRecoil();
		
		public void dispose();
	}
	
	public static class AttackActionBlock implements AttackAction
	{

		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			entity.readOnlyRead(StatusData.class).blocking = true;
		}

		@Override
		public void evaluate(float delta, Entity entity,
				HashMap<String, Object> data)
		{
			entity.readOnlyRead(StatusData.class).blocking = true;
		}

		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			entity.readOnlyRead(StatusData.class).blocking = false;
		}

		@Override
		public boolean needsRecoil()
		{
			return false;
		}

		@Override
		public void addRequiredQueueables(AnimatedModel model)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose()
		{
			// TODO Auto-generated method stub
			
		}
		
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
			output.clear();
			GLOBALS.picker.set(entity, output, cam, range, numHits, allies, pickSpeed, tintColour);
			GLOBALS.picker.begin();
		}

		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			GLOBALS.picker.end();
			data.put("targetted", GLOBALS.picker.output);
		}

		@Override
		public boolean needsRecoil()
		{
			return false;
		}

		@Override
		public void addRequiredQueueables(AnimatedModel model)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dispose()
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class AttackSpellCast implements AttackAction
	{
		Entity baseSpell;
		AnimatedModel model;
		
		boolean shouldCast;
		
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		private final PositionalData pData = new PositionalData();
		private final PositionalData pData2 = new PositionalData();
		private final Matrix4 tmp = new Matrix4();
		
		private int i;
		
		public AttackSpellCast(Entity baseSpell)
		{
			this.baseSpell = baseSpell;
			baseSpell.readData(pData);
			pData.createCallback();
			baseSpell.writeData(pData);
		}
		
		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			((BehaviourTree) baseSpell.getAI()).setData("Enemy", null);
			shouldCast = true;
			i = 0;
		}

		@Override
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data)
		{
			if (shouldCast)
			{
				shouldCast = false;
				
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
				pData2.sweep.setSkipObject(pData.physicsBody);
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

		@Override
		public boolean needsRecoil()
		{
			return false;
		}

		@Override
		public void addRequiredQueueables(AnimatedModel model)
		{
			this.model = model;
		}

		@Override
		public void dispose()
		{
			model = null;
		}
		
	}
	
	public static class AttackActionParticleEffect implements AttackAction
	{
		String effect;
		private AnimatedModel model;
		
		Vector3 tmpVec = new Vector3();
		Matrix4 tmp = new Matrix4();
		
		public AttackActionParticleEffect(String effect)
		{
			this.effect = effect;
		}
		
		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			ParticleEffect npe = FileUtils.obtainParticleEffect(effect);
			npe.setPosition(tmpVec.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform)));
			npe.play(false);
			npe.setBase(entity);
			GLOBALS.unanchoredEffects.add(npe);
		}

		@Override
		public void evaluate(float delta, Entity entity,
				HashMap<String, Object> data)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean needsRecoil()
		{
			return false;
		}

		@Override
		public void addRequiredQueueables(AnimatedModel model)
		{
			this.model = model;			
		}

		@Override
		public void dispose()
		{
			model = null;
		}
		
	}
	
	public static class AttackMotionTrail implements AttackAction
	{
		public final int damagePercentage;
		
		private final Random ran = new Random();
		
		private AnimatedModel weaponModel;
		private AnimatedModel animationModel;
		private MotionTrail mt;
		
		private final Vector3 pbot = new Vector3();
		private final Vector3 ptop = new Vector3();
		private final Vector3 bot = new Vector3();
		private final Vector3 top = new Vector3();
		private final Matrix4 tmp = new Matrix4();
		
		private final Vector3 ray1 = new Vector3();
		private final Vector3 ray2 = new Vector3();
		
		private final Vector3 tmpVec = new Vector3();
		
		public final ClosestRayResultSkippingCallback ray = new ClosestRayResultSkippingCallback(new Vector3(), new Vector3());
		
		private final PositionalData pData = new PositionalData();
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		
		private boolean needsRecoil;
		private boolean first = false;
		private boolean canCollide = false;
		
		private final float startCollide;
		private final float endCollide;
		
		public AttackMotionTrail(int damagePercentage, float startCollide, float endCollide)
		{
			this.damagePercentage = damagePercentage;
			
			this.startCollide = startCollide;
			this.endCollide = endCollide;
		}
		
		@Override
		public void begin(Entity entity, HashMap<String, Object> data)
		{
			ray.clearSkips();
			
			entity.readData(pData);
			entity.readData(sData2);
			ray.setSkipObject(pData.physicsBody);
			needsRecoil = false;
			first = true;
		}
		
		private void doCollision(Entity entity)
		{
			if (needsRecoil) return;
			
			ray.getRayFromWorld().setValue(ray1.x, ray1.y, ray1.z);
			ray.getRayToWorld().setValue(ray2.x, ray2.y, ray2.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
			ray.setCollisionObject(null);
			ray.setClosestHitFraction(1f);
			
			GLOBALS.physicsWorld.world.rayTest(ray1, ray2, ray);
			if (ray.hasHit())
			{
				Entity e = (Entity) ray.getCollisionObject().userData;
				boolean hasStatus = e.readData(sData);
				boolean isBlocking = false;
				
				if (hasStatus && sData.blocking)
				{
					double angle = GLOBALS.angle(entity.getPosition(), e.getPosition(), tmpVec);
					
					isBlocking = true;
					System.out.println("blocking");
				}
				
				if (isBlocking || !hasStatus || sData.solid)
				{
//					ParticleEffect npe = FileUtils.obtainParticleEffect("data/effects/sparks.effect");
//					npe.setPosition(ray.getHitPointWorld().x(), ray.getHitPointWorld().y(), ray.getHitPointWorld().z());
//					npe.play(false);
//					npe.setBase(entity);
//					GLOBALS.unanchoredEffects.add(npe);
						
					needsRecoil = true;
					return;
				}
				else if (!sData.isAlly(sData2))
				{
					sData.damage(damagePercentage, sData2.attack);
					e.writeData(sData);
				}

				ray.setSkipObject(ray.getCollisionObject());
			}
			
		}
		
		@Override
		public void evaluate(float delta, Entity entity, HashMap<String, Object> data)
		{
			float progress = animationModel.animationProgress();
			canCollide = progress > startCollide && progress < endCollide;
			
			if (canCollide)
			{	
				animationModel.colour.set(0, 1, 0);
				if (!first)
				{
					pbot.set(bot);
					ptop.set(top);
				}
				
				bot.set(0, 0, 0).mul(tmp.set(weaponModel.model.transform).mul(weaponModel.model.getNode("bottom").globalTransform));
				top.set(0, 0, 0).mul(tmp.set(weaponModel.model.transform).mul(weaponModel.model.getNode("top").globalTransform));
				
				if (first)
				{
					pbot.set(bot);
					ptop.set(top);
				}
				
				mt.update(bot, top);
				
				entity.readData(pData);
				
				ray1.set(pbot);
				ray2.set(bot);
				doCollision(entity);
				
				ray1.set(ptop);
				ray2.set(top);
				doCollision(entity);
				
				ray1.set(bot);
				ray2.set(top);
				doCollision(entity);
				
				ray1.set(pbot);
				ray2.set(top);
				doCollision(entity);
				
				ray1.set(ptop);
				ray2.set(bot);
				doCollision(entity);			
			}
			else
			{
				animationModel.colour.set(1, 0, 0);
				mt.stopDraw();
			}
		}
		
		@Override
		public void end(Entity entity, HashMap<String, Object> data)
		{
			mt.stopDraw();
		}

		@Override
		public boolean needsRecoil()
		{
			return needsRecoil;
		}

		@Override
		public void addRequiredQueueables(AnimatedModel model)
		{
			weaponModel = model;
			animationModel = model.parent;
			mt = new MotionTrail(100, 0.005f, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true, null, null));
			model.attach(null, mt, new Matrix4(), "MotionTrail");
		}

		@Override
		public void dispose()
		{
			if (mt != null) mt.dispose();
			mt = null;
			animationModel = null;
			weaponModel = null;
		}
	}
}
