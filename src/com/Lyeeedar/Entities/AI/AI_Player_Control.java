package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private boolean jump = false;
	private boolean animationLock = false;
	
	private final PositionalData entityPos = new PositionalData();
	private final AnimationData entityAnim = new AnimationData();
	
	public AI_Player_Control(Entity entity, Controls controls)
	{
		super(entity);
		
		this.controls = controls;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityPos, PositionalData.class);
		entity.readData(entityAnim, AnimationData.class);
		
		entityPos.Xrotate(-controls.getDeltaX());
		
		if (!animationLock)
		{
			byte speed = 10;
			if (controls.sprint()) speed = 100;
			
			if (controls.up()) entityPos.forward_backward(speed);
			if (controls.down()) entityPos.forward_backward(-speed);
			
			if (controls.left()) entityPos.left_right(speed);
			if (controls.right()) entityPos.left_right(-speed);
			
			if (controls.sprint()) {
				if (entityAnim.animation != 6) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;
				entityAnim.animation = 6;
				entityAnim.useDirection = false;
			}
			else {
				if (entityAnim.animation != 0) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;

				entityAnim.animation = 0;
				entityAnim.useDirection = true;
			}
			
			entityAnim.anim = "move";
			
			if (controls.up() || controls.down() || controls.left() || controls.right()) {
				if (entityAnim.animate) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;
				entityAnim.animate = true;
			}
			else {
				if (!entityAnim.animate) entityAnim.updateAnimations = true;
				else entityAnim.updateAnimations = false;
				entityAnim.animate = false;
			}
		}
		
		if (controls.esc()) Gdx.app.exit();
		
		if (controls.jump() && entityPos.jumpToken > 0 && !jump) {
			entityPos.velocity.set(0, 30, 0);
			entityPos.jumpToken--;
			jump = true;
		}
		else if (!controls.jump())
		{
			jump = false;
		}
		if (Gdx.input.isKeyPressed(Keys.J)) entityPos.velocity.set(0, 50f, 0);
		
		entityPos.applyVelocity(delta);
		entityPos.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		if (!animationLock && !entityAnim.animationLock && controls.leftClick())
		{
			animationLock = true;
			entityAnim.playAnim = "attack_1";
			entityAnim.playAnimation = 2;
			entityAnim.nextAnim = entityAnim.anim;
			entityAnim.nextAnimation = entityAnim.animation;
			entityAnim.startFrame = 0;
			entityAnim.endFrame = 7;
			entityAnim.informable = this;
		}
		entityAnim.animationLock = animationLock;
		
		entity.writeData(entityPos, PositionalData.class);
		entity.writeData(entityAnim, AnimationData.class);
	}

	@Override
	public void inform() {
		animationLock = false;
	}
}
