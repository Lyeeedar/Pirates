package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;

public class FollowCam extends PerspectiveCamera {
	
	private final float limit = 60;
	
	private final Controls controls;
	private final Vector3 tmp = new Vector3();
	public float followDist = 10.0f;
	public float followHeight = 7.0f;
	
	public btGhostObject renderObject;
	public btGhostObject aiObject;
	
	private final Vector3 lPos = new Vector3();
	private final Matrix4 tmpMat = new Matrix4();
	private final Vector3 tmpVec = new Vector3();
	private final Vector3 tmpVec2 = new Vector3();
	
	public final ClosestRayResultSkippingCallback ray = new ClosestRayResultSkippingCallback(new Vector3(), new Vector3());
	
	public FollowCam(Controls controls, btGhostObject renderObject, btGhostObject aiObject)
	{
		this.controls = controls;
		this.renderObject = renderObject;
		this.aiObject = aiObject;
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
	
	@Override
	public void update()
	{
		if (renderObject != null) renderObject.setWorldTransform(tmpMat.setToTranslation(position).rotate(GLOBALS.DEFAULT_ROTATION, direction));
		if (aiObject != null) aiObject.setWorldTransform(tmpMat.setToTranslation(position).rotate(GLOBALS.DEFAULT_ROTATION, direction));
		super.update();
	}
	
	public void updateBasic(PositionalData pData)
	{
		up.set(pData.up);
		direction.set(pData.rotation);
		Yrotate(Yangle);
		
		tmp.set(direction).scl(-1*followDist).add(pData.position).add(0, followHeight, 0);
		
		position.set(tmp);
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

		tmp.set(direction).scl(-1*followDist).add(pData.position).add(0, followHeight, 0);
		position.set(tmp);
		
		ray.setCollisionObject(null);
        ray.setClosestHitFraction(1f);

		tmpVec.set(pData.position).add(0, followHeight, 0);
		tmpVec2.set(position);
		
		ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
		ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
		ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
		ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
		ray.setSkipObject(pData.physicsBody);
		
		GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
		if (ray.hasHit())
		{
			position.set(ray.getHitPointWorld().x(), ray.getHitPointWorld().y(), ray.getHitPointWorld().z());
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
	
	public static btGhostObject createSphere(float radius)
	{
		final btPairCachingGhostObject result = new TestPairCachingGhostObject();

		result.setCollisionShape(new btSphereShape(radius));
		result.setCollisionFlags(btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
		
		return result;
	}
	
	public static class TestPairCachingGhostObject extends btPairCachingGhostObject {
        public btCollisionShape shape;
        @Override
        public void setCollisionShape (btCollisionShape collisionShape) {
                shape = collisionShape;
                super.setCollisionShape(collisionShape);
        }
        @Override
        public void dispose () {
                super.dispose();
                if (shape != null)
                        shape.dispose();
                shape = null;
        }
}

}
