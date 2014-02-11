package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Spell extends Equipment<Spell> implements AnimationListener
{
	public Camera cam;
	public float range;
	public int numHits;
	public boolean allies;
	public float pickSpeed;
	public Vector3 tintColour = new Vector3();
	public boolean hasTargetter;
	public final Entity baseSpell;
	public final float castTime;
	public float castCD;
	
	private final StatusData sData = new StatusData();
	private final StatusData sData2 = new StatusData();
	private final AnimationData aData = new AnimationData();
	private final PositionalData pData = new PositionalData();
	private final PositionalData pData2 = new PositionalData();
	
	private boolean shouldCast = false;
	private boolean casting = false;
	private int targetting = 0;
	private final Array<Entity> entities = new Array<Entity>(false, 16);
	
	public Spell(Entity baseSpell, float castTime)
	{
		super();
		this.baseSpell = baseSpell;
		this.castTime = castTime;
		this.castCD = 0;
		this.hasTargetter = cam != null;
		
		baseSpell.readOnlyRead(PositionalData.class).createSensor();
	}
	
	public Spell(Entity baseSpell, float castTime, Camera cam, float range, int numHits, boolean allies, float pickSpeed, Vector3 tintColour)
	{
		super();
		this.baseSpell = baseSpell;
		this.castTime = castTime;
		this.castCD = 0;
		this.hasTargetter = true;
		this.cam = cam;
		this.range = range;
		this.numHits = numHits;
		this.allies = allies;
		this.pickSpeed = pickSpeed;
		this.tintColour.set(tintColour);
		
		baseSpell.readOnlyRead(PositionalData.class).createSensor();
	}
	
	@Override
	public void onEnd(AnimationDesc animation)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLoop(AnimationDesc animation)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(float delta, Entity entity)
	{
		castCD -= delta;
		
		if (!shouldCast && casting)
		{
			casting = false;
			castCD = castTime;
		}
		
		if (targetting == 2 && entities.size == 0)
		{
			targetting = 0;
			casting = false;
			castCD = castTime;
		}
		else if (targetting == 2)
		{
			Entity e = entities.removeIndex(0);
			((BehaviourTree) baseSpell.getAI()).setData("Enemy", e);
			cast(entity);
		}
		
		if (!casting)
		{
			entity.readData(aData);
			if (aData.animationLocker == this) aData.animationLock = false;
			entity.writeData(aData);
		}
		
		if (shouldCast && !casting && castCD < 0)
		{
			entity.readData(aData);
			playAnimation(aData, "cast");
			aData.listener = this;
			aData.animationLock = false;
			aData.animationLocker = this;
			entity.writeData(aData);
			
			casting = true;
			
			if (!hasTargetter)
			{
				cast(entity);
			}
			else if (targetting == 0)
			{
				entities.clear();
				GLOBALS.picker.set(entity, entities, cam, range, numHits, allies, pickSpeed, tintColour);
				GLOBALS.picker.begin();
				targetting = 1;
			}
			else if (targetting == 1)
			{
				GLOBALS.picker.end();
				
				targetting = 2;			
			}

		}
	}
	
	private void cast(Entity caster)
	{
		caster.readData(sData);
		caster.readData(pData);
		baseSpell.readData(sData2);
		baseSpell.readData(pData2);
		
		sData2.factions = sData.factions;
		
		pData2.position.set(pData.position);
		pData2.sensor.setSkipObject(pData.physicsBody);
		pData2.rotation.set(pData.rotation);
		
		baseSpell.writeData(sData2);
		baseSpell.writeData(pData2);
		
		GLOBALS.pendingEntities.add(baseSpell);
	}

	@Override
	public void use()
	{
		shouldCast = true;
	}

	@Override
	public void stopUsing()
	{
		shouldCast = false;
	}

	@Override
	public void dispose()
	{
		baseSpell.dispose();
		sData.dispose();
		sData2.dispose();
	}

}
