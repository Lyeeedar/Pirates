package com.Lyeeedar.Collision;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btBroadphasePairArray;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifoldArray;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Array;

public class BulletWorld {
	
	public static final short FILTER_COLLISION = 1 << 10;
	public static final short FILTER_AI = 1 << 11;
	public static final short FILTER_RENDER = 1 << 12;
	public static final short FILTER_WALKABLE = 1 << 13;
	public static final short FILTER_LAND = 1 << 14;
	
	DebugDrawer debugDrawer = null;
	
	public btDbvtBroadphase broadphase;

	public btDefaultCollisionConfiguration config;
	public btCollisionDispatcher dispatcher;

	public btConstraintSolver solver;

	public btDynamicsWorld world;
	
	private final int ptrs[] = new int[512];
	private final Array<btCollisionObject> tmpArray = new Array<btCollisionObject>();
	
	public BulletWorld(Vector3 worldMin, Vector3 worldMax)
	{
		//broadphase = new btAxisSweep3(worldMin,worldMax);
		broadphase = new btDbvtBroadphase();

        config = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(config);

        solver = new btSequentialImpulseConstraintSolver();

        world = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, config);
        world.setGravity(new Vector3(0, -10, 0));
        
        debugDrawer = new DebugDrawer();
        world.setDebugDrawer(debugDrawer);       
	}
	
	public Array<Entity> getEntitiesCollidingWithObject(final btCollisionObject shape, final Array<Entity> out) 
	{
        btBroadphasePairArray arr = broadphase.getOverlappingPairCache().getOverlappingPairArray();
        
        tmpArray.clear();
        final Array<btCollisionObject> n = arr.getCollisionObjects(tmpArray, shape, ptrs);
        
        out.clear();
        for (btCollisionObject o : n)
        {
        	out.add((Entity) o.userData);
        }
        
        //System.out.println(out.size);
        
        return out;
}

	public void add(btCollisionShape shape, Matrix4 transform, Entity entity, short group, short mask)
	{
		btDefaultMotionState fallMotionState = new btDefaultMotionState(transform);
        btRigidBody rigidBody = new btRigidBody(0, fallMotionState, shape);
        
        rigidBody.userData = entity;
        PositionalData pData = entity.readOnlyRead(PositionalData.class);
        if (pData != null) 
        {
        	pData.ray.setSkipObject(rigidBody);
        	pData.physicsBody = rigidBody;
        }
        
        world.addRigidBody(rigidBody, group, mask);
	}
	
	public void add(btRigidBody object, short group, short mask)
	{
		world.addRigidBody(object, group, mask);
	}
	
	public void update(float delta)
	{
		//world.stepSimulation(delta, 10);
		world.performDiscreteCollisionDetection();
	}
	
	public void render(PerspectiveCamera cam)
	{
		debugDrawer.lineRenderer.setProjectionMatrix(cam.combined);
		
		if (debugDrawer != null && debugDrawer.getDebugMode() > 0) {
            //debugDrawer.begin();
            //world.debugDrawWorld();
            //debugDrawer.end();
		}
	}
	
	public static class ClosestRayResultSkippingCallback extends ClosestRayResultCallback
	{
		Array<btCollisionObject> skipObjects = new Array<btCollisionObject>();

		public ClosestRayResultSkippingCallback(Vector3 rayFromWorld, Vector3 rayToWorld) {
			super(rayFromWorld, rayToWorld);
		}
		
		public void setSkipObject(btCollisionObject object)
		{
			if (!skipObjects.contains(object, true))
			{
				skipObjects.add(object);
			}
		}
		
		@Override
		public boolean needsCollision(btBroadphaseProxy proxy)
		{
			for (btCollisionObject o : skipObjects)
			{
				if (o.getCPointer() == proxy.getClientObject()) return false;
			}
			return super.needsCollision(proxy);
		}
	}
}
