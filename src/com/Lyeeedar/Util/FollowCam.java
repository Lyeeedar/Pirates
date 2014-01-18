package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class FollowCam extends PerspectiveCamera {
	
	private final float limit = 60;
	
	private final Controls controls;
	private final Vector3 tmp = new Vector3();
	private final CollisionRay ray = new CollisionRay();
	public float followDist = 10.0f;
	public float followHeight = 5.0f;
	
	private final Vector3 lPos = new Vector3();
	
	public FollowCam(Controls controls)
	{
		this.controls = controls;
	}
	
	private float Yangle = -15;
	private float Xangle = 0;
	
	public void setYAngle(float angle)
	{
		this.Yangle = angle;
	}
	
	public void setXAngle(float angle)
	{
		this.Xangle = angle;
	}
	
	public void setFollowDist(float dist)
	{
		this.followDist = dist;
	}
	
	public void updateBasic(PositionalData pData)
	{
		up.set(pData.up);
		direction.set(pData.rotation);
		Yrotate(Yangle);
		
		ray.ray.origin.set(pData.position).add(0, followHeight, 0);
		ray.ray.direction.set(direction).scl(-1.0f);
		ray.len = followDist;
		ray.reset();
		
		position.set(ray.intersection);
		update();
	}
	
	public void update(Entity entity)
	{
		followDist += controls.scrolled();
		followDist = MathUtils.clamp(followDist, 5, 50);
		
		Xangle -= controls.getDeltaX();
		Yangle -= controls.getDeltaY();
		
		if (Yangle > limit) Yangle = limit;
		if (Yangle < -limit) Yangle = -limit;
		
		PositionalData pData = entity.readOnlyRead(PositionalData.class);
		
		if (lPos.dst2(pData.position) > 1)
		{
			pData.Xrotate(Xangle);
			Xangle = 0;
			lPos.set(pData.position);
		}
		
		up.set(pData.up);
		direction.set(pData.rotation.x, 0, pData.rotation.z).nor();
		direction.rotate(Xangle, 0, 1, 0);
		Yrotate(Yangle);
		
		ray.ray.origin.set(pData.position).add(0, followHeight, 0);
		ray.ray.direction.set(direction).scl(-1.0f);
		ray.len = followDist;
		ray.reset();
		
		GLOBALS.WORLD.collideWalkables(ray, pData.graph);
		
		position.set(ray.intersection);
		update();
		
		for (int i = 0; i < 4; i++)
		{
			ray.ray.direction.set(frustum.planePoints[i]).sub(pData.position).nor();
			ray.reset();
			
			tmp.set(frustum.planePoints[i]).sub(position);
			
			if (GLOBALS.WORLD.collideWalkables(ray, pData.graph) != null)
				position.add(tmp.add(ray.intersection).sub(ray.ray.direction)).scl(0.5f);
		}
		update();
		
		float seaY = 0;
		
		for (int i = 0; i < 2; i++)
		{
			float seaHeight = GLOBALS.SKYBOX.sea.waveHeight(frustum.planePoints[i].x, frustum.planePoints[i].z)+0.1f;
			float diff = seaHeight - frustum.planePoints[i].y;
			if (diff > seaY) seaY = diff;
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
