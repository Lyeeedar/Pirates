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

	private ATTACK_STAGE[] attacks;
	
	private float hitCD = 0;
	private float hitSpeed = 1f;
	
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
	
	public Weapon(ATTACK_STAGE[] attacks, SPRITESHEET spritesheet, DESCRIPTION desc, float speed, AnimatedModel model, MotionTrail mt)
	{
		super(spritesheet, desc);
		this.hitSpeed = speed;
		this.model = model;
		this.mt = mt;
		this.attacks = attacks;
	}

	@Override
	public Item set(Item other) {
		super.set(other);
		
		Weapon cother = (Weapon) other;
		
		shouldSwing = cother.shouldSwing;
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
							sData.damage = attacks[animationStage].damage + ran.nextInt(attacks[animationStage].damVar);
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
		
		if (!shouldSwing) 
		{
			return;
		}
		
		if (needsUpdate)
		{
			entity.readData(aData, AnimationData.class);
			playAnimation(aData, attacks[animationStage].animationName);
			aData.listener = this;
			aData.animate_speed = attacks[animationStage].speed;
			entity.writeData(aData, AnimationData.class);
			
			needsUpdate = false;
		}
		else if (!inSwing && hitCD < 0)
		{
			this.inSwing = true;
			
			entity.readData(aData, AnimationData.class);
			playAnimation(aData, attacks[0].animationName);
			aData.listener = this;
			aData.animate_speed = attacks[0].speed;
			entity.writeData(aData, AnimationData.class);
			
			entity.readData(pData, PositionalData.class);
			
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
		ray.clearSkips();
		
		if (shouldSwing)
		{
			animationStage = attacks[animationStage].nextAnim;
			needsUpdate = true;
		}
		
		if (!shouldSwing || animationStage == -1)
		{
			inSwing = false;
			hitCD = hitSpeed;
			animationStage = 0;
			needsUpdate = false;
		}
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
