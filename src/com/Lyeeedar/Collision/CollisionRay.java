package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class CollisionRay extends CollisionShape<CollisionRay> {
	
	public final Ray ray = new Ray(new Vector3(), new Vector3());
	public float len;
	
	public final Vector3 intersection = new Vector3();
	public float dist;
	
	public CollisionRay()
	{
		reset();
	}
	
	public CollisionRay(Ray ray, float len)
	{
		this.ray.set(ray);
		this.len = len;
		
		reset();
	}
	
	public CollisionRay set(Vector3 start, Vector3 end)
	{
		ray.origin.set(start);
		ray.direction.set(end).sub(start).nor();
		len = start.dst(end);
		dist = len;
		
		return this;
	}
	
	@Override
	public void reset()
	{
		intersection.set(ray.origin);
		dist = len;
	}

	@Override
	public boolean collide(Sphere sphere) {
		return ThreadSafeIntersector.collide(this, sphere);
	}

	@Override
	public boolean collide(Cylinder cylinder) {
		return ThreadSafeIntersector.collide(this, cylinder);
	}

	@Override
	public boolean collide(Box rect) {
		return ThreadSafeIntersector.collide(this, rect);
	}

	@Override
	public boolean collide(Triangle tri) {
		return ThreadSafeIntersector.collide(this, tri);
	}

	@Override
	public boolean collide(CollisionRay ray) {
		return ThreadSafeIntersector.collide(this, ray);
	}

	@Override
	public CollisionRay set(CollisionRay other) {
		ray.set(other.ray);
		len = other.len;
		intersection.set(other.intersection);
		dist = other.dist;
		
		return this;
	}

	@Override
	public CollisionRay copy() {
		return new CollisionRay(ray, len).set(this);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		ray.origin.mul(matrix);
		intersection.mul(matrix);
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		ray.direction.mul(matrix);
	}

	@Override
	public CollisionRay obtain() {
		return Pools.obtain(CollisionRay.class).set(this);
	}

	@Override
	public void free() {
		Pools.free(this);
	}

	@Override
	public void setPosition(Vector3 position) {
		ray.origin.set(position);
	}

	@Override
	public void setRotation(Vector3 rotation) {
		ray.direction.set(rotation);
	}

	@Override
	protected String string() {
		return ""+ray+" Len: "+len+" I: "+intersection+" Dist: "+dist;
	}
}
