package com.Lyeeedar.Collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;

public class BulletTest {
	
	public static void doshit()
	{
		Bullet.init();
		
		
		btBroadphaseInterface broadphase = new btDbvtBroadphase();

        btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);

        btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();

        btDiscreteDynamicsWorld dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher,broadphase,solver,collisionConfiguration);

        dynamicsWorld.setGravity(new Vector3(0,-10,0));


        btCollisionShape groundShape = new btStaticPlaneShape(new Vector3(0,1,0),1);

        btCollisionShape fallShape = new btSphereShape(1);


        btDefaultMotionState groundMotionState = new btDefaultMotionState(new Matrix4().translate(0, 1, 0));
        btRigidBody groundRigidBody = new btRigidBody(0,groundMotionState,groundShape, new Vector3(0,0,0));
        dynamicsWorld.addRigidBody(groundRigidBody);


        btDefaultMotionState fallMotionState = new btDefaultMotionState(new Matrix4().translate(0, 50, 0));
        fallShape.calculateLocalInertia(1,new Vector3());
        
        btRigidBody fallRigidBody = new btRigidBody(1,fallMotionState,fallShape,new Vector3());
        dynamicsWorld.addRigidBody(fallRigidBody);
        
        Matrix4 trans = new Matrix4();
        Vector3 p = new Vector3();
        for (int i=0 ; i<300 ; i++) 
        {
            dynamicsWorld.stepSimulation(1/60.f,10);

            fallRigidBody.getMotionState().getWorldTransform(trans);

            p.set(0, 0, 0).mul(trans);
            
            System.out.println(p.y);
        }
	}

}
