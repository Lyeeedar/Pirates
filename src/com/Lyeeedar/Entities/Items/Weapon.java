package com.Lyeeedar.Entities.Items;

import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.AllHitsRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.MotionTrail;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectConstArray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> implements AnimationListener {

	private ATTACK_STAGE[] attacks;
	
	private float linkTime;
	private float linkCD = -1;
	
	private float hitCD = 0;
	private float hitSpeed;
	
	public boolean inSwing = false;
	public boolean shouldSwing = false;
	public int animationStage = 0;
	public boolean needsUpdate = false;
	
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
	
	public Weapon(ATTACK_STAGE[] attacks, SPRITESHEET spritesheet, DESCRIPTION desc, float speed, float linkTime, AnimatedModel model, MotionTrail mt)
	{
		super(spritesheet, desc);
		this.hitSpeed = speed;
		this.linkTime = linkTime;
		this.model = model;
		this.mt = mt;
		this.attacks = attacks;
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
		model = cother.model;
		mt = cother.mt;
		attacks = cother.attacks;
		
		return this;
	}

	@Override
	public Item copy() {
		return Pools.obtain(Weapon.class).set(this);
	}

	@Override
	public void update(float delta, Entity entity) {
		
		if (linkCD > 0)
		{
			linkCD -= delta;
			
			if (shouldSwing)
			{
				animationStage = attacks[animationStage].nextAnim;
				needsUpdate = true;
				inSwing = true;
			}
			
			if (linkCD <= 0 || animationStage == -1)
			{
				hitCD = hitSpeed;
				animationStage = 0;
				needsUpdate = false;
				linkCD = -1;
				inSwing = false;
			}
			
			if (shouldSwing)
			{
				linkCD = -1;
			}
		}
		
		hitCD -= delta;
		
		if (!inSwing)
		{
			if (mt != null) mt.stopDraw();
			entity.readData(aData);
			aData.animationLock = false;
			entity.writeData(aData);
		}
		else if (model != null && mt != null)
		{
			bot.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("bottom").globalTransform));
			top.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform));
			mt.draw(bot, top);
			mt.update(bot, top);
			
			entity.readData(pData);
			
			ray.getRayFromWorld().setValue(bot.x, bot.y, bot.z);
			ray.getRayToWorld().setValue(top.x, top.y, top.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_AI);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_AI);
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
						if (e.readData(sData))
						{
							sData.damage = attacks[animationStage].damage + ran.nextInt(attacks[animationStage].damVar);
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
		
		if (!shouldSwing) 
		{
			return;
		}
		
		if (needsUpdate)
		{
			entity.readData(aData);
			playAnimation(aData, attacks[animationStage].animationName);
			aData.listener = this;
			aData.animate_speed = attacks[animationStage].speed;
			aData.base_anim = "attack";
			entity.writeData(aData);
			
			needsUpdate = false;
		}
		else if (!inSwing && hitCD < 0 && linkCD < 0)
		{
			inSwing = true;
			
			entity.readData(aData);
			playAnimation(aData, attacks[0].animationName);
			aData.listener = this;
			aData.animate_speed = attacks[0].speed;
			aData.base_anim = "attack";
			entity.writeData(aData);
			
			entity.readData(pData);
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
		ray.clearSkips();
		
		linkCD = linkTime;
		inSwing = false;
	}

	@Override
	public void onLoop(AnimationDesc animation) {
		// TODO Auto-generated method stub
		
	}
	
	public static class ATTACK_STAGE
	{
		String animationName;
		float speed;
		int damage;
		int damVar;
		int nextAnim;
		
		public ATTACK_STAGE(String animationName, float speed, int damage, int damVar, int nextAnim)
		{
			this.animationName = animationName;
			this.speed = speed;
			this.damage = damage;
			this.damVar = damVar;
			this.nextAnim = nextAnim;
		}
	}
	
}
