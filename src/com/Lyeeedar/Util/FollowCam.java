package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class FollowCam extends PerspectiveCamera {
	
	private final Controls controls;
	private final Vector3 tmp = new Vector3();
	private final CollisionRay ray = new CollisionRay();
	public float followDist = 4.0f;
	
	public FollowCam(Controls controls)
	{
		this.controls = controls;
	}
	
	private float angle = -25;
	private float minDist = 0;
	
	public void setAngle(float angle)
	{
		this.angle = angle;
	}
	
	public void setFollowDist(float dist)
	{
		this.followDist = dist;
	}
	
	public void updateBasic(PositionalData entityState)
	{
		up.set(entityState.up);
		direction.set(entityState.rotation);
		Yrotate(angle);
		
		ray.ray.origin.set(entityState.position).add(0, 1, 0);
		ray.ray.direction.set(direction).scl(-1.0f);
		ray.len = followDist;
		ray.reset();
		
		position.set(ray.intersection);
		update();
	}
	
	public void update(PositionalData entityState)
	{
		angle -= controls.getDeltaY();
		if (angle > 60) angle = 60;
		if (angle < -65) angle = -65;
		
		up.set(entityState.up);
		direction.set(entityState.rotation.x, 0, entityState.rotation.z).nor();
		Yrotate(angle);
		
		ray.ray.origin.set(entityState.position).add(0, 1, 0);
		ray.ray.direction.set(direction).scl(-1.0f);
		ray.len = followDist;
		ray.reset();
		
		position.set(ray.intersection);
		update();
		
		minDist = ray.dist;
		
		for (int i = 0; i < 4; i++)
		{
			ray.ray.direction.set(frustum.planePoints[i]).sub(entityState.position).nor();
			ray.reset();
			
			GLOBALS.WORLD.collideWalkables(ray, entityState.graph);
			
			if ( ray.dist < minDist) minDist = ray.dist;
		}
		
		ray.len = minDist;
		ray.ray.direction.set(direction).scl(-1.0f);
		ray.reset();
		
		position.set(ray.intersection);
		update();
		
		float seaY = 0;
		
		for (int i = 0; i < 2; i++)
		{
//			float seaHeight = GLOBALS.SKYBOX.sea.waveHeight(frustum.planePoints[i].x, frustum.planePoints[i].z)+0.1f;
//			float diff = seaHeight - frustum.planePoints[i].y;
//			if (diff > seaY) seaY = diff;
		}
		
		if (seaY > 0)
		{
			position.y += seaY;
			update();
		}
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
