package com.Lyeeedar.Util;

import com.Lyeeedar.Entities.Entity;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class FollowCam extends PerspectiveCamera {
	
	private final Controls controls;
	private final Vector3 tmp = new Vector3();
	
	public FollowCam(Controls controls)
	{
		this.controls = controls;
	}
	
	private float angle = -25;
	
	public void update(Entity.EntityData entityState)
	{
		angle -= controls.getDeltaY();
		
		if (angle > 0) angle = 0;
		if (angle < -65) angle = -65;
		
		up.set(entityState.positionalData.up);
		direction.set(entityState.positionalData.rotation);
		Yrotate(angle);
		position.set(entityState.positionalData.position).add(0, 1, 0).sub(direction).sub(direction).sub(direction).sub(direction).sub(direction);
		
		update();
	}

	public void Yrotate (float angle) {	
		Vector3 dir = tmp.set(direction).nor();
		if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
		{
			Vector3 localAxisX = dir;
			localAxisX.crs(up).nor();
			rotate(angle, localAxisX.x, localAxisX.y, localAxisX.z);
		}
	}

}
