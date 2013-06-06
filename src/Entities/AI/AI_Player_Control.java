package Entities.AI;

import Entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.GLOBALS;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private boolean jump = false;
	private boolean animationLock = false;
	
	public AI_Player_Control(Entity entity, Controls controls)
	{
		super(entity);
		
		this.controls = controls;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityState);
		
		entityState.positionalData.Xrotate(-controls.getDeltaX());
		
		if (!animationLock)
		{
			byte speed = 10;
			if (controls.sprint()) speed = 100;
			
			if (controls.up()) entityState.positionalData.forward_backward(speed);
			if (controls.down()) entityState.positionalData.forward_backward(-speed);
			
			if (controls.left()) entityState.positionalData.left_right(speed);
			if (controls.right()) entityState.positionalData.left_right(-speed);
			
			if (controls.sprint()) {
				if (entityState.animationData.animation != 6) entityState.animationData.updateAnimations = true;
				else entityState.animationData.updateAnimations = false;
				entityState.animationData.animation = 6;
				entityState.animationData.useDirection = false;
			}
			else {
				if (entityState.animationData.animation != 0) entityState.animationData.updateAnimations = true;
				else entityState.animationData.updateAnimations = false;

				entityState.animationData.animation = 0;
				entityState.animationData.useDirection = true;
			}
			
			entityState.animationData.anim = "move";
			
			if (controls.up() || controls.down() || controls.left() || controls.right()) {
				if (entityState.animationData.animate) entityState.animationData.updateAnimations = true;
				else entityState.animationData.updateAnimations = false;
				entityState.animationData.animate = true;
			}
			else {
				if (!entityState.animationData.animate) entityState.animationData.updateAnimations = true;
				else entityState.animationData.updateAnimations = false;
				entityState.animationData.animate = false;
			}
		}
		
		if (controls.esc()) Gdx.app.exit();
		
		if (controls.jump() && entityState.positionalData.jumpToken > 0 && !jump) {
			entityState.positionalData.velocity.set(0, 30, 0);
			entityState.positionalData.jumpToken--;
			jump = true;
		}
		else if (!controls.jump())
		{
			jump = false;
		}
		if (Gdx.input.isKeyPressed(Keys.J)) entityState.positionalData.velocity.set(0, 50f, 0);
		
		entityState.positionalData.applyVelocity(delta);
		entityState.positionalData.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		if (!animationLock && !entityState.animationData.animationLock && controls.leftClick())
		{
			animationLock = true;
			entityState.animationData.playAnim = "attack_1";
			entityState.animationData.playAnimation = 2;
			entityState.animationData.nextAnim = entityState.animationData.anim;
			entityState.animationData.nextAnimation = entityState.animationData.animation;
			entityState.animationData.startFrame = 0;
			entityState.animationData.endFrame = 7;
			entityState.animationData.informable = this;
		}
		entityState.animationData.animationLock = animationLock;
		
		entity.writeData(entityState);
	}

	@Override
	public void inform() {
		animationLock = false;
	}
}
