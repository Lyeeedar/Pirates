package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;

public class Spell extends Equipment<Spell> implements AnimationListener
{
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
	
	public Spell(Entity baseSpell, float castTime)
	{
		super();
		this.baseSpell = baseSpell;
		this.castTime = castTime;
		this.castCD = 0;
		
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
			cast(entity);
			casting = false;
			castCD = castTime;
		}
		
		if (!casting)
		{
			entity.readData(aData);
			if (aData.animationLocker == this) aData.animationLock = false;
			entity.writeData(aData);
		}
		
		if (shouldCast && !casting)
		{
			entity.readData(aData);
			playAnimation(aData, "cast");
			aData.listener = this;
			aData.animationLock = false;
			aData.animationLocker = this;
			entity.writeData(aData);
			
			casting = true;
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
