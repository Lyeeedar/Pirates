package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;


public abstract class Equipment<E extends Equipment<E>> {
	
	public String animationName = null;
	public static final int START_FRAME = 0;
	public static final int END_FRAME = 3;
	
	public Equipment()
	{
	}
	
	public Equipment(String animationName)
	{
		this.animationName = animationName;
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
	
	public abstract E copy();
	public abstract E set(E other);
	public abstract void update(float delta, Entity entity);
	public abstract void use();
	public abstract void stopUsing();
	public abstract void dispose();
}
