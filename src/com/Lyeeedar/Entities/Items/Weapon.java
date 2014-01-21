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
import com.Lyeeedar.Graphics.AnimatedModel;
import com.Lyeeedar.Graphics.MotionTrail;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Sound.Sound3D;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class Weapon extends Equipment<Weapon> implements AnimationListener {

	public float dist = 1;
	private float hitCD = 0;
	private float hitSpeed = 0.5f;
	private int damageMin = 1;
	private int damageVar = 1;
	
	public boolean inSwing = false;
	public boolean shouldSwing = false;
	
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	private AnimationData aData = new AnimationData();
	private Random ran = new Random();
	private ArrayList<EntityGraph> entities = new ArrayList<EntityGraph>();
	
	private Box box = new Box(new Vector3(), 0.5f, 0.5f, 0.5f);
	
	private AnimatedModel model;
	private MotionTrail mt;
	
	private final Vector3 bot = new Vector3();
	private final Vector3 top = new Vector3();
	private final Matrix4 tmp = new Matrix4();

	public Weapon()
	{
		super();
	}
	
	public Weapon(String animationName, SPRITESHEET spritesheet, DESCRIPTION desc, float dist, Vector3 hitBox, float speed, int damageMin, int damageVar, AnimatedModel model, MotionTrail mt)
	{
		super(animationName, spritesheet, desc);
		this.dist = dist;
		this.box.set(box.center, hitBox.x, hitBox.y, hitBox.z);
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
		
		dist = cother.dist;
		shouldSwing = cother.shouldSwing;
		hitCD = cother.hitCD;
		hitSpeed = cother.hitSpeed;
		box.set(cother.box);
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
			mt.stopDraw();
			entity.readData(aData, AnimationData.class);
			aData.animationLock = false;
			entity.writeData(aData, AnimationData.class);
		}
		else
		{
			bot.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("bottom").globalTransform));
			top.set(0, 0, 0).mul(tmp.set(model.model.transform).mul(model.model.getNode("top").globalTransform));
			mt.draw(bot, top);
			mt.update(bot, top);
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
			aData.animate_speed = 2f;
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
	}

	@Override
	public void onLoop(AnimationDesc animation) {
		// TODO Auto-generated method stub
		
	}
	
}
