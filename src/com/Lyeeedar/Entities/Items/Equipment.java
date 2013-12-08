package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.badlogic.gdx.graphics.Color;


public abstract class Equipment<E extends Equipment<E>> extends Item {
	
	public SPRITESHEET spritesheet = null;
	
	public String animationName = null;
	public static final int START_FRAME = 0;
	public static final int END_FRAME = 3;
	
	public Equipment_Slot equipped = null;
	
	public Equipment()
	{
		super();
	}
	
	public Equipment(String animationName, SPRITESHEET spritesheet, DESCRIPTION desc)
	{
		super(desc);
		this.animationName = animationName;
		this.spritesheet = spritesheet;
	}
	
	public void playAnimation(AnimationData aData)
	{
		if (animationName == null) return;
		
		aData.animationLock = true;
		aData.playAnim = animationName;
		aData.playAnimation = 0;
		aData.nextAnim = aData.anim;
		aData.nextAnimation = aData.animation;
		aData.startFrame = START_FRAME;
		aData.endFrame = END_FRAME;
		aData.useDirection = true;
	}
	
	@Override
	public Item set(Item other)
	{
		super.set(other);
		Equipment<E> cother = (Equipment<E>)other;
		this.animationName = cother.animationName;
		this.spritesheet = cother.spritesheet;
		return this;
	}
	
	public abstract void update(float delta, Entity entity);
	public abstract void use();
	public abstract void stopUsing();
	public abstract void dispose();
}
