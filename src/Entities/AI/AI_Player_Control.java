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
		
		entityState.Xrotate(-controls.getDeltaX());
		
		if (!animationLock)
		{
			byte speed = 10;
			if (controls.sprint()) speed = 100;
			
			if (controls.up()) entityState.forward_backward(speed);
			if (controls.down()) entityState.forward_backward(-speed);
			
			if (controls.left()) entityState.left_right(speed);
			if (controls.right()) entityState.left_right(-speed);
			
			if (controls.sprint()) {
				if (entityState.animation != 6) entityState.updateAnimations = true;
				else entityState.updateAnimations = false;
				entityState.animation = 6;
				entityState.useDirection = false;
			}
			else {
				if (entityState.animation != 0) entityState.updateAnimations = true;
				else entityState.updateAnimations = false;

				entityState.animation = 0;
				entityState.useDirection = true;
			}
			
			entityState.anim = "move";
			
			if (controls.up() || controls.down() || controls.left() || controls.right()) {
				if (entityState.animate) entityState.updateAnimations = true;
				else entityState.updateAnimations = false;
				entityState.animate = true;
			}
			else {
				if (!entityState.animate) entityState.updateAnimations = true;
				else entityState.updateAnimations = false;
				entityState.animate = false;
			}
		}
		
		if (controls.esc()) Gdx.app.exit();
		
		if (controls.jump() && entityState.jumpToken > 0 && !jump) {
			entityState.velocity.set(0, 30, 0);
			entityState.jumpToken--;
			jump = true;
		}
		else if (!controls.jump())
		{
			jump = false;
		}
		if (Gdx.input.isKeyPressed(Keys.J)) entityState.velocity.set(0, 50f, 0);
		
		entityState.applyVelocity(delta);
		entityState.velocity.add(0, GLOBALS.GRAVITY*delta, 0);
		
		if (!animationLock && !entityState.animationLock && controls.leftClick())
		{
			animationLock = true;
			entityState.playAnim = "attack_1";
			entityState.playAnimation = 2;
			entityState.nextAnim = entityState.anim;
			entityState.nextAnimation = entityState.animation;
			entityState.startFrame = 0;
			entityState.endFrame = 7;
			entityState.informable = this;
		}
		entityState.animationLock = animationLock;
		
		entity.writeData(entityState);
	}

	@Override
	public void inform() {
		animationLock = false;
	}
}
