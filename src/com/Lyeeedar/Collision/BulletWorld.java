package com.Lyeeedar.Collision;

import java.util.HashSet;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactResultCallback;
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

	private final ContactSensorCallback sensor = new ContactSensorCallback();

	public static class ContactSensorCallback extends ContactResultCallback
	{
		public btCollisionObject object;
		public final Array<btCollisionObject> array = new Array<btCollisionObject>();

		@Override
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) 

		{
			btCollisionObject other = colObj0Wrap.getCollisionObject() == object ? colObj1Wrap.getCollisionObject() : colObj0Wrap.getCollisionObject();

			if (other != null) {
				array.add(other);
			}

			return 0f;
		}

	}

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

	public Array<Entity> getEntitiesCollidingWithObject(final btGhostObject shape, final Array<Entity> out, short group, short mask) 
	{
		sensor.setCollisionFilterGroup(group);
		sensor.setCollisionFilterMask(mask);
		sensor.object = shape;
		sensor.array.clear();
		
		world.contactTest(shape, sensor);

		out.clear();
		for (btCollisionObject o : sensor.array)
		{
			if (o.userData != null) out.add((Entity) o.userData);
			else System.out.println("null");
		}
		
//		btCollisionObjectArray arr = shape.getOverlappingPairs();
//		
//		out.clear();
//		for (int i = 0; i < arr.size(); i++)
//		{
//			btCollisionObject o = arr.at(i);
//			if (o.userData != null) out.add((Entity) o.userData);
//			else System.out.println("null");
//		}

		return out;
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
			//debugDrawer.begin();
			//world.debugDrawWorld();
			//debugDrawer.end();
		}
	}

	public static class ClosestRayResultSkippingCallback extends ClosestRayResultCallback
	{
		HashSet<Long> skipObjects = new HashSet<Long>();

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
		HashSet<Long> skipObjects = new HashSet<Long>();

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
	
	public static class SimpleContactCallback extends ContactResultCallback
	{
		public boolean hasCollided = false;

		public SimpleContactCallback()
		{
			super();
		}

		public float addSingleResult(btManifoldPoint cp,
				btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1)
		{

			hasCollided = true;

			return 1.f;
		}
	}
}
