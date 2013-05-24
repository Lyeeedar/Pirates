package Entities.AI;

import Entities.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.GLOBALS;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private final Camera cam;
	private float angle = -25;
	private boolean jump = false;
	private Vector3 tmp = new Vector3();
	
	public AI_Player_Control(Entity entity, Controls controls, Camera cam)
	{
		super(entity);
		
		this.cam = cam;
		this.controls = controls;
	}

	@Override
	public void update(float delta) {
		
		entity.readData(entityState);
		
		entityState.Xrotate(-controls.getDeltaX());
		angle -= controls.getDeltaY();
		
		if (angle > 0) angle = 0;
		if (angle < -65) angle = -65;
		
		//angle = -25;
		
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
		
		cam.up.set(entityState.up);
		cam.direction.set(entityState.rotation);
		Yrotate(angle, cam);
		cam.position.set(entityState.position).add(0, 1, 0).sub(cam.direction).sub(cam.direction).sub(cam.direction).sub(cam.direction).sub(cam.direction);
		
		cam.update();
	}

	public Camera getCamera()
	{
		return cam;
	}
	
	public void Yrotate (float angle, Camera cam) {	
		Vector3 dir = tmp.set(cam.direction).nor();
		if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
		{
			Vector3 localAxisX = dir;
			localAxisX.crs(cam.up).nor();
			cam.rotate(angle, localAxisX.x, localAxisX.y, localAxisX.z);
		}
	}
}
