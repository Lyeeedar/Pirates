package Entities.AI;

import Entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.GLOBALS;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private boolean jump = false;
	
	public AI_Player_Control(Entity entity, Controls controls)
	{
		super(entity);
		
		this.controls = controls;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityState);
		
		entityState.Xrotate(-controls.getDeltaX());
		
		if (controls.up()) entityState.forward_backward(10);
		if (controls.down()) entityState.forward_backward(-10);
		
		if (controls.left()) entityState.left_right(10);
		if (controls.right()) entityState.left_right(-10);
		
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
		
		entity.writeData(entityState);
	}
}
