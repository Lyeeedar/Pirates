package com.Lyeeedar.Collision;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class CollisionTests {
	
	public static final int TESTS = 100000;
	
	private final Random ran = new Random();

	@Test
	public void testCollideSphereSphere() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Sphere s2 = new Sphere(new Vector3(), 1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(2, 2, 2);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0.5f, 0.5f, 0.5f);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.radius = ran.nextFloat()*20.0f;
			
			s2.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.radius = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideSphereCylinder() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Cylinder s2 = new Cylinder(new Vector3(), new Vector3(0, 1, 0), 1, 1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 2.1f, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 1.9f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 1.99999999f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(2.1f, 0, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.9f, 0, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.99999999999f, 0, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 0, 2.1f);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 0, 1.9f);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 0, 1.99999999999f);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.9f, 1.9f, 1.9f);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.9f, 1.9f, 0);
		s1.radius = 1.414213562f;
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.radius = ran.nextFloat()*20.0f;
			
			s2.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.rotation.set(ran.nextFloat(), ran.nextFloat(), ran.nextFloat()).nor();
			s2.radius = ran.nextFloat()*20.0f;
			s2.height = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideSphereBox() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Box s2 = new Box(new Vector3(), 1, 1, 1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 2.1f, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 1.9f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(2.1f, 0, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.9f, 0, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 0, 2.1f);
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0, 0, 1.9f);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s1.radius = 1.5f;
		
		s2.center.set(1.7f, 1.7f, 1.7f);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(1.9f, 1.9f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.depth = 20;
		s2.height = 20;
		
		s2.center.set(1.9f, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.radius = ran.nextFloat()*20.0f;
			
			s2.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.width = ran.nextFloat()*20.0f;
			s2.height = ran.nextFloat()*20.0f;
			s2.depth = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideSphereTriangle() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Triangle s2 = new Triangle(new Vector3(-1, 0, -1), new Vector3(1, 0, 1), new Vector3(1, 0, 1));
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.v1.set(20, 0, 20);
		s2.v2.set(0, 0, 0);
		s2.v3.set(20, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.v1.set(-20, 0, 20);
		s2.v2.set(20, 0, 0);
		s2.v3.set(-20, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.v1.set(-40, 0, 40);
		s2.v2.set(-40, 0, -40);
		s2.v3.set(40, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.v1.set(-60, 0, 00);
		s2.v2.set(0, 60, 00);
		s2.v3.set(60, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.v1.set(1.1f, 0, 9);
		s2.v2.set(1.1f, 60, 8);
		s2.v3.set(1.1f, 0, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.radius = ran.nextFloat()*20.0f;
			
			s2.v1.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.v2.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.v3.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			
			ThreadSafeIntersector.collide(s1, s2);
		}
		
	}

	@Test
	public void testCollideSphereCollisionRay() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		CollisionRay s2 = new CollisionRay(new Ray(new Vector3(), new Vector3(GLOBALS.DEFAULT_ROTATION)), 100);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.reset();
		s2.ray.origin.set(5, 0, 0);
		s2.ray.direction.set(1, 0, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(-1, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		s2.reset();
		s2.ray.origin.set(0, 5, 0);
		s2.ray.direction.set(0, 1, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(0, -1, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		s2.reset();
		s2.ray.origin.set(0, 0, 5);
		s2.ray.direction.set(0, 0, 1);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(0, 0, -1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		s2.reset();
		s2.ray.origin.set(5, 1, 0);
		s2.ray.direction.set(-1, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 25.0f);

		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.radius = ran.nextFloat()*20.0f;
			
			s2.reset();
			s2.ray.origin.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.ray.direction.set(ran.nextFloat(), ran.nextFloat(), ran.nextFloat()).nor();
			s2.len = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideCylinderCylinder() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideCylinderBox() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideCylinderTriangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideCylinderCollisionRay() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideBoxBox() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideBoxTriangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideBoxCollisionRay() {
		Box s1 = new Box(new Vector3(), 1, 1, 1);
		CollisionRay s2 = new CollisionRay(new Ray(new Vector3(), new Vector3(GLOBALS.DEFAULT_ROTATION)), 100);

		assertTrue(ThreadSafeIntersector.collide(s1, s2));

		s2.reset();
		s2.ray.origin.set(5, 0, 0);
		s2.ray.direction.set(1, 0, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(-1, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		s2.reset();
		s2.ray.origin.set(0, 5, 0);
		s2.ray.direction.set(0, 1, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(0, -1, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		s2.reset();
		s2.ray.origin.set(0, 0, 5);
		s2.ray.direction.set(0, 0, 1);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == Float.MAX_VALUE);
		
		s2.reset();
		s2.ray.direction.set(0, 0, -1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 16.0f);
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.center.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.width = ran.nextFloat()*20.0f;
			s1.height = ran.nextFloat()*20.0f;
			s1.depth = ran.nextFloat()*20.0f;
			
			s2.reset();
			s2.ray.origin.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.ray.direction.set(ran.nextFloat(), ran.nextFloat(), ran.nextFloat()).nor();
			s2.len = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideTriangleTriangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideTriangleCollisionRay() {
		Triangle s1 = new Triangle(new Vector3(1, 1, 1), new Vector3(1, 0, 1), new Vector3(0, 0, 1));
		CollisionRay s2 = new CollisionRay(new Ray(new Vector3(), new Vector3(GLOBALS.DEFAULT_ROTATION)), 100);
		
		s1.v1.set(1, -1, -1);
		s1.v2.set(1, 1, -1);
		s1.v3.set(1, 1, 1);
		s2.reset();
		s2.ray.direction.set(1, 0, 0);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		assertTrue(s2.dist == 1.0f);
		
		s1.v1.set(1, -1, -1);
		s1.v2.set(1, 1, -1);
		s1.v3.set(1, 1, 1);
		s2.reset();
		s2.ray.direction.set(-1, 0, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s1.v1.set(1, 0, 0);
		s1.v2.set(1, 1, 0);
		s1.v3.set(1, 1, 1);
		s2.reset();
		s2.ray.origin.set(0, 0, 1);
		s2.ray.direction.set(1, 0, 0);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		for (int i = 0; i < TESTS; i++)
		{
			s1.v1.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.v2.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s1.v3.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			
			s2.reset();
			s2.ray.origin.set(ran.nextFloat()*50.0f, ran.nextFloat()*50.0f, ran.nextFloat()*50.0f);
			s2.ray.direction.set(ran.nextFloat(), ran.nextFloat(), ran.nextFloat()).nor();
			s2.len = ran.nextFloat()*20.0f;
			
			ThreadSafeIntersector.collide(s1, s2);
		}
	}

	@Test
	public void testCollideCollisionRayCollisionRay() {
		CollisionRay r1 = new CollisionRay();
		CollisionRay r2 = new CollisionRay();
		
		assertFalse(ThreadSafeIntersector.collide(r1, r2));
	}
}
