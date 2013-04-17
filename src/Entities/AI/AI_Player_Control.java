package Entities.AI;

import Entities.GameEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.GLOBALS;

public class AI_Player_Control extends AI_Package {
	
	private final Controls controls;
	private final Camera cam;
	private float angle = -25;
	
	public AI_Player_Control(GameEntity entity, Controls controls, Camera cam)
	{
		super(entity);
		
		this.cam = cam;
		this.controls = controls;
	}

	@Override
	public void update(float delta) {
		
		entity.Xrotate(-controls.getDeltaX());
		angle -= controls.getDeltaY();
		
		if (angle > 0) angle = 0;
		if (angle < -65) angle = -65;
		
		//angle = -25;
		
		if (controls.up()) entity.forward_backward(10);
		if (controls.down()) entity.forward_backward(-10);
		
		if (controls.left()) entity.left_right(10);
		if (controls.right()) entity.left_right(-10);
		
		if (controls.esc()) Gdx.app.exit();
		
		entity.applyVelocity(delta);
		entity.getVelocity().add(0, GLOBALS.GRAVITY*delta, 0);
		
		cam.up.set(entity.getUP());
		cam.direction.set(entity.getRotation());
		Yrotate(angle, cam);
		cam.position.set(entity.getPosition()).sub(cam.direction).sub(cam.direction).sub(cam.direction).sub(cam.direction).sub(cam.direction);
		cam.update();
	}

	public Camera getCamera()
	{
		return cam;
	}
	
	public void Yrotate (float angle, Camera cam) {	
		Vector3 dir = Pools.obtain(Vector3.class);
		dir.set(cam.direction).nor();
		if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
		{
			Vector3 localAxisX = Pools.obtain(Vector3.class).set(cam.direction);
			localAxisX.crs(cam.up).nor();
			cam.rotate(angle, localAxisX.x, localAxisX.y, localAxisX.z);
			Pools.free(localAxisX);
		}
		Pools.free(dir);
	}
}
