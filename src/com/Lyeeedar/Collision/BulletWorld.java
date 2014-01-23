package com.Lyeeedar.Collision;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;

public class BulletWorld {
	
	DebugDrawer debugDrawer = null;
	
	public btBroadphaseInterface broadphase;

	public btDefaultCollisionConfiguration config;
	public btCollisionDispatcher dispatcher;

	public btConstraintSolver solver;

	public btDynamicsWorld world;
	
	public BulletWorld()
	{
		Vector3 worldMin = new Vector3(-1000,-1000,-1000);
		Vector3 worldMax = new Vector3(1000,1000,1000);
		broadphase = new btAxisSweep3(worldMin,worldMax);
		
		//broadphase = new btDbvtBroadphase();

        config = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(config);

        solver = new btSequentialImpulseConstraintSolver();

        world = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, config);
        world.setGravity(new Vector3(0, -10, 0));
        
        debugDrawer = new DebugDrawer();
        world.setDebugDrawer(debugDrawer);
	}

	public btRigidBody addDynamic(btCollisionShape shape, Matrix4 transform, float mass, float bounce)
	{
		btDefaultMotionState fallMotionState = new btDefaultMotionState(transform);
		Vector3 inertia = new Vector3();
        shape.calculateLocalInertia(mass, inertia);
        
        btRigidBody rigidBody = new btRigidBody(mass, fallMotionState, shape, inertia);
        world.addRigidBody(rigidBody);
        
        rigidBody.setFriction(1);
        rigidBody.setRestitution(bounce);
        rigidBody.setAngularFactor(bounce);
                
        return rigidBody;
	}
	
	public void addStatic(btCollisionShape shape, Matrix4 transform)
	{  
		btCollisionObject co = new btCollisionObject();
		co.setCollisionShape(shape);
		co.setWorldTransform(transform);
        world.addCollisionObject(co);

	}
	
	public btKinematicCharacterController addKinematic(btConvexShape shape, Matrix4 transform)
	{
		btPairCachingGhostObject ghost = new btPairCachingGhostObject();
		ghost.setWorldTransform(transform);
		ghost.setCollisionShape(shape);
		
		btKinematicCharacterController controller = new btKinematicCharacterController(ghost, shape, 0.5f);
		world.addCollisionObject(ghost);
		world.addAction(controller);
		
		return controller;
	}
	
	public void update(float delta)
	{
		//world.stepSimulation(delta, 10);
	}
	
	PerspectiveCamera pc = new PerspectiveCamera();
	public void render(PerspectiveCamera cam)
	{
		pc.near = 1;
		pc.far = 200;
		pc.fieldOfView = cam.fieldOfView;
		pc.position.set(cam.position);
		pc.direction.set(cam.direction);
		pc.update();
		
		debugDrawer.lineRenderer.setProjectionMatrix(cam.combined);
		
		if (debugDrawer != null && debugDrawer.getDebugMode() > 0) {
            //debugDrawer.begin();
            //world.debugDrawWorld();
            //debugDrawer.end();
    }
	}
}
