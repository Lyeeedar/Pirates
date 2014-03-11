package com.Lyeeedar.Collision;

import java.util.HashSet;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AI;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.AllHitsRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactResultCallback;
import com.badlogic.gdx.physics.bullet.collision.LocalConvexResult;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
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
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.utils.Array;

public class BulletWorld {

	public static final short FILTER_COLLISION = 1 << 14;
	public static final short FILTER_AI = 1 << 13;
	public static final short FILTER_RENDER = 1 << 12;
	public static final short FILTER_GHOST = 1 << 11;
	public static final short FILTER_WALKABLE = 1 << 10;

	DebugDrawer debugDrawer = null;

	public btBroadphaseInterface broadphase;

	public btDefaultCollisionConfiguration config;
	public btCollisionDispatcher dispatcher;

	public btConstraintSolver solver;

	public btDynamicsWorld world;

	public BulletWorld(Vector3 worldMin, Vector3 worldMax)
	{
		broadphase = new btDbvtBroadphase();

		config = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(config);

		solver = new btSequentialImpulseConstraintSolver();

		world = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, config);

		debugDrawer = new DebugDrawer();
		world.setDebugDrawer(debugDrawer);       
		
		world.getPairCache().setInternalGhostPairCallback(new btGhostPairCallback());
	}

	public boolean collide(final ContactSensorSkippingCallback sensor) 
	{		
		world.contactTest(sensor.me, sensor);
		return sensor.manifolds.size != 0;
	}
	
	public boolean collide(final SimpleSkippingCallback sensor) 
	{		
		sensor.collided = false;
		world.contactTest(sensor.me, sensor);
		return sensor.collided;
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
	
	public static btCollisionShape meshToCollisionShape(Mesh mesh)
	{
		if (mesh.getNumIndices() != 0)
		{
			Array<MeshPart> parts = new Array<MeshPart>();
			parts.add(new MeshPart("", mesh, 0, mesh.getNumIndices(), GL20.GL_TRIANGLES));
			return new btBvhTriangleMeshShape(parts, true);
		}
		else
		{
			btTriangleMesh tm = new btTriangleMesh();
						
			final int nverts = mesh.getNumVertices();
			final int vsize = mesh.getVertexSize() / 4;
			float[] vertices = mesh.getVertices(new float[nverts*vsize]);
			int poff = mesh.getVertexAttributes().getOffset(Usage.Position);
			
			for (int i = 0; i < nverts;)
			{
				Vector3 v1 = new Vector3();
				Vector3 v2 = new Vector3();
				Vector3 v3 = new Vector3();
				
				v1.set(
						vertices[poff+(i*vsize)+0],
						vertices[poff+(i*vsize)+1],
						vertices[poff+(i*vsize)+2]
								);
				i++;
				
				v2.set(
						vertices[poff+(i*vsize)+0],
						vertices[poff+(i*vsize)+1],
						vertices[poff+(i*vsize)+2]
								);
				i++;
				
				v3.set(
						vertices[poff+(i*vsize)+0],
						vertices[poff+(i*vsize)+1],
						vertices[poff+(i*vsize)+2]
								);
				i++;

				tm.addTriangle(v1, v2, v3);
			}
			
			return new btBvhTriangleMeshShape(tm, true);
		}
	}
	
	public static void addTriangles(Mesh mesh, Matrix4 transform, btTriangleMesh triangleMesh)
	{
		final int nverts = mesh.getNumVertices();
		final int vsize = mesh.getVertexSize() / 4;
		float[] vertices = mesh.getVertices(new float[nverts*vsize]);
		int poff = mesh.getVertexAttributes().getOffset(Usage.Position);
		
		for (int i = 0; i < nverts;)
		{
			Vector3 v1 = new Vector3();
			Vector3 v2 = new Vector3();
			Vector3 v3 = new Vector3();
			
			v1.set(
					vertices[poff+(i*vsize)+0],
					vertices[poff+(i*vsize)+1],
					vertices[poff+(i*vsize)+2]
							).mul(transform);
			i++;
			
			v2.set(
					vertices[poff+(i*vsize)+0],
					vertices[poff+(i*vsize)+1],
					vertices[poff+(i*vsize)+2]
							).mul(transform);
			i++;
			
			v3.set(
					vertices[poff+(i*vsize)+0],
					vertices[poff+(i*vsize)+1],
					vertices[poff+(i*vsize)+2]
							).mul(transform);
			i++;

			triangleMesh.addTriangle(v1, v2, v3);
		}
	}

	public static class ClosestRayResultSkippingCallback extends ClosestRayResultCallback
	{
		public HashSet<Long> skipObjects = new HashSet<Long>();
		
		public ClosestRayResultSkippingCallback()
		{
			super(new Vector3(), new Vector3());
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
	
	public static class SimpleSkippingCallback extends ContactResultCallback
	{
		public boolean collided = false;
		
		public btCollisionObject me;
		public HashSet<Long> skipObjects = new HashSet<Long>();
		
		public void setCollisionShape(btCollisionShape shape, Matrix4 trans)
		{
			if (me == null)
			{
				me = new btCollisionObject();
			}
			me.setCollisionShape(shape);
			me.setWorldTransform(trans);
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
			if (collided) return false;
			if (skipObjects.contains(proxy.getClientObject())) return false;
			return super.needsCollision(proxy);
		}
		
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) 

		{
			collided = true;
			return 1f;
		}
	}
	
	public static class KinematicCallback extends ClosestConvexResultSkippingCallback
	{
		public final Vector3 up = new Vector3(GLOBALS.DEFAULT_UP);
		public float minDot;
		
		private final Vector3 hitNormalWorld = new Vector3();

		public KinematicCallback()
		{
			super();
		}
		
		public void set(Vector3 up, float minDot)
		{
			this.up.set(up);
			this.minDot = minDot;
		}
		
		@Override
		public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace)
		{	
			hitNormalWorld.set(convexResult.getHitNormalLocal().x(), convexResult.getHitNormalLocal().y(), convexResult.getHitNormalLocal().z());
			if (!normalInWorldSpace)
			{
				///need to transform normal into worldspace
				hitNormalWorld.mul(convexResult.getHitCollisionObject().getWorldTransform());
			}

			float dotUp = up.dot(hitNormalWorld);
			if (dotUp < minDot) 
			{
				return 1.0f;
			}
			return super.addSingleResult(convexResult, normalInWorldSpace);
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

		public ClosestConvexResultSkippingCallback()
		{
			super(new Vector3(), new Vector3());
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
		
		public btCollisionObject me;
		public final Array<btCollisionObject> objects = new Array<btCollisionObject>();
		public final Array<btManifoldPoint> manifolds = new Array<btManifoldPoint>();
		public final Array<Byte> signs = new Array<Byte>();

		public void setCollisionShape(btCollisionShape shape, Matrix4 trans)
		{
			if (me == null)
			{
				me = new btCollisionObject();
			}
			me.setCollisionShape(shape);
			me.setWorldTransform(trans);
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
		public float addSingleResult (btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
				btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) 

		{
			btCollisionObject other = colObj0Wrap.getCollisionObject() == me ? colObj1Wrap.getCollisionObject() : colObj0Wrap.getCollisionObject();

			if (skipObjects.contains(other.getCPointer()))
			{
				return 1f;
			}
			
			if (other != null) {
				objects.add(other);
			}
			
			manifolds.add(cp);
			byte sign = (byte) (colObj0Wrap.getCollisionObject() == me ? -1 : 1) ;
			signs.add(sign);

			return 1f;
		}

	}
}
