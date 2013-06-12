package com.Lyeeedar.Collision;

import static org.junit.Assert.*;

import org.junit.Test;

import com.badlogic.gdx.math.Vector3;

public class CollisionTests {

	@Test
	public void testCollideSphereSphere() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Sphere s2 = new Sphere(new Vector3(), 1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(2, 2, 2);
		
		assertFalse(ThreadSafeIntersector.collide(s1, s2));
		
		s2.center.set(0.5f, 0.5f, 0.5f);
		
		assertTrue(ThreadSafeIntersector.collide(s1, s2));
	}

	@Test
	public void testCollideSphereCylinder() {
		Sphere s1 = new Sphere(new Vector3(), 1);
		Cylinder c1 = new Cylinder(new Vector3(), new Vector3(0, 1, 0), 1, 1);
		
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 2.1f, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 1.9f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 1.99999999f, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(2.1f, 0, 0);
		assertFalse(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(1.9f, 0, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(1.99999999999f, 0, 0);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 0, 2.1f);
		assertFalse(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 0, 1.9f);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(0, 0, 1.99999999999f);
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(1.9f, 1.9f, 1.9f);
		assertFalse(ThreadSafeIntersector.collide(s1, c1));
		
		c1.center.set(1.9f, 1.9f, 0);
		s1.radius = 1.414213562f;
		assertTrue(ThreadSafeIntersector.collide(s1, c1));
	}

	@Test
	public void testCollideSphereBox() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideSphereTriangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideSphereCollisionRay() {
		fail("Not yet implemented");
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
		fail("Not yet implemented");
	}

	@Test
	public void testCollideTriangleTriangle() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideTriangleCollisionRay() {
		fail("Not yet implemented");
	}

	@Test
	public void testCollideCollisionRayCollisionRay() {
		CollisionRay r1 = new CollisionRay();
		CollisionRay r2 = new CollisionRay();
		
		assertFalse(ThreadSafeIntersector.collide(r1, r2));
	}
}
