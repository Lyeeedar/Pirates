package com.Lyeeedar.Collision;

import java.util.HashSet;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AI;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactResultCallback;
import com.badlogic.gdx.physics.bullet.collision.LocalConvexResult;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Array;

public class BulletWorld {

	public static final short FILTER_COLLISION = 1 << 14;
	public static final short FILTER_AI = 1 << 13;
	public static final short FILTER_RENDER = 1 << 12;
	public static final short FILTER_GHOST = 1 << 11;

	DebugDrawer debugDrawer = null;

	public btBroadphaseInterface broadphase;

	public btDefaultCollisionConfiguration config;
	public btCollisionDispatcher dispatcher;

	public btConstraintSolver solver;

	public btDynamicsWorld world;

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
		
		world.getPairCache().setInternalGhostPairCallback(new btGhostPairCallback());
	}

	public boolean collide(final ContactSensorSkippingCallback sensor) 
	{		
		world.contactTest(sensor.object, sensor);
		return sensor.array.size != 0;
	}
	
	public void remove(btRigidBody body)
	{
		world.removeRigidBody(body);
	}
	
	public void remove(btCollisionObject body)
	{
		world.removeCollisionObject(body);
	}

	public void add(btCollisionShape shape, Matrix4 transform, Entity entity, short group, short mask)
	{
		btRigidBody rigidBody = getRigidBody(shape, transform, entity);

		world.addRigidBody(rigidBody, group, mask);
	}
	
	public btRigidBody getRigidBody(btCollisionShape shape, Matrix4 transform, Entity entity)
	{
		btDefaultMotionState fallMotionState = new btDefaultMotionState(transform);
		btRigidBody rigidBody = new btRigidBody(0, fallMotionState, shape);

		rigidBody.userData = entity;
		PositionalData pData = entity.readOnlyRead(PositionalData.class);
		if (pData != null) 
		{
			pData.physicsBody = rigidBody;
		}
		
		return rigidBody;
	}

	public void add(btRigidBody object, short group, short mask)
	{
		world.addRigidBody(object, group, mask);
	}
	
	public void add(btCollisionObject object, short group, short mask)
	{
		world.addCollisionObject(object, group, mask);
	}

	public void update(float delta)
	{
		//world.stepSimulation(delta, 10);
		world.performDiscreteCollisionDetection();
	}

	public void render(PerspectiveCamera cam)
	{
		debugDrawer.lineRenderer.setProjectionMatrix(cam.combined);

		if (debugDrawer.getDebugMode() > 0) {
//			debugDrawer.begin();
//			world.debugDrawWorld();
//			debugDrawer.end();
		}
	}

	public static class ClosestRayResultSkippingCallback extends ClosestRayResultCallback
	{
		public HashSet<Long> skipObjects = new HashSet<Long>();
		
		public ClosestRayResultSkippingCallback()
		{
			this(new Vector3(), new Vector3());
		}

		public ClosestRayResultSkippingCallback(Vector3 rayFromWorld, Vector3 rayToWorld) {
			super(rayFromWorld, rayToWorld);
		}
		
		public void clearSkips()
		{
			skipObjects.clear();
		}
		
		public boolean hasSkip(long skip)
		{
			return skipObjects.contains(skip);
		}

		public void setSkipObject(btCollisionObject object)
		{
			if (!skipObjects.contains(object.getCPointer()))
			{
				skipObjects.add(object.getCPointer());
			}
		}

		@Override
		public boolean needsCollision(btBroadphaseProxy proxy)
		{
			if (skipObjects.contains(proxy.getClientObject())) return false;
			return super.needsCollision(proxy);
		}
	}
	
	public static class AllHitsRayResultSkippingCallback extends AllHitsRayResultCallback
	{
		public HashSet<Long> skipObjects = new HashSet<Long>();

		public AllHitsRayResultSkippingCallback(Vector3 rayFromWorld, Vector3 rayToWorld) {
			super(rayFromWorld, rayToWorld);
		}
		
		public void clearSkips()
		{
			skipObjects.clear();
		}
		
		public boolean hasSkip(long skip)
		{
			return skipObjects.contains(skip);
		}

		public void setSkipObject(btCollisionObject object)
		{
			if (!skipObjects.contains(object.getCPointer()))
			{
				skipObjects.add(object.getCPointer());
			}
		}

		@Override
		public boolean needsCollision(btBroadphaseProxy proxy)
		{
			if (skipObjects.contains(proxy.getClientObject())) return false;
			return super.needsCollision(proxy);
		}
	}
	
	public static class SimpleContactTestCallback extends ContactResultCallback
	{
		public boolean collided = false;
		
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) 

		{
			collided = true;
			return 0f;
		}
	}
	
	public static class ClosestConvexResultSkippingCallback extends ClosestConvexResultCallback
	{
		public HashSet<Long> skipObjects = new HashSet<Long>();
		
		public void clearSkips()
		{
			skipObjects.clear();
		}
		
		public boolean hasSkip(long skip)
		{
			return skipObjects.contains(skip);
		}
		
		public void setSkipObject(btCollisionObject object)
		{
			if (!skipObjects.contains(object.getCPointer()))
			{
				skipObjects.add(object.getCPointer());
			}

		}

		public ClosestConvexResultSkippingCallback(Vector3 convexFromWorld,
				Vector3 convexToWorld)
		{
			super(convexFromWorld, convexToWorld);
		}
		
		@Override
		public boolean needsCollision(btBroadphaseProxy proxy)
		{
			if (skipObjects.contains(proxy.getClientObject())) return false;
			return super.needsCollision(proxy);
		}
	}
	
	public static class ContactSensorSkippingCallback extends ContactResultCallback
	{
		public HashSet<Long> skipObjects = new HashSet<Long>();
		
		public btCollisionObject object;
		public final Array<btCollisionObject> array = new Array<btCollisionObject>();

		public void clearSkips()
		{
			skipObjects.clear();
		}
		
		public boolean hasSkip(long skip)
		{
			return skipObjects.contains(skip);
		}
		
		public void setSkipObject(btCollisionObject object)
		{
			if (!skipObjects.contains(object.getCPointer()))
			{
				skipObjects.add(object.getCPointer());
			}
		}
		
		@Override
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) 

		{
			btCollisionObject other = colObj0Wrap.getCollisionObject() == object ? colObj1Wrap.getCollisionObject() : colObj0Wrap.getCollisionObject();

			if (skipObjects.contains(other.getCPointer()))
			{
				return 0f;
			}
			
			if (other != null) {
				array.add(other);
			}

			return 0f;
		}

	}
}
