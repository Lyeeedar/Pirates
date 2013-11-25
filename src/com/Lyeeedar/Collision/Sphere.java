package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Sphere extends CollisionShape<Sphere> {

	public final Vector3 center = new Vector3();
	public float radius;
	
	public final Box box = new Box();
	
	public Sphere()
	{
		
	}
	
	public Sphere(Vector3 center, float radius)
	{
		this.center.set(center);
		this.radius = radius;
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
	public Sphere set(Sphere other)
	{
		center.set(other.center);
		radius = other.radius;
		
		return this;
	}
	
	@Override
	public Sphere copy() {
		return new Sphere(center, radius);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		center.mul(matrix);
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		
	}

	@Override
	public Sphere obtain() {
		return Pools.obtain(Sphere.class).set(this);
	}

	@Override
	public void free() {
		Pools.free(this);
	}
	
	@Override
	public Vector3 getPosition()
	{
		return center;
	}

	@Override
	public void setPosition(Vector3 position) {
		center.set(position);
	}

	@Override
	public void setRotation(Vector3 rotation) {
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String string() {
		return "Center: "+center+" R: "+radius;
	}

	@Override
	public void calculateBoundingBox() {
		box.width = radius;
		box.height = radius;
		box.depth = radius;
	}

	@Override
	public void setScaling(Vector3 scale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkBoundingBox(Box box) {
		return this.box.collide(box);
	}

	@Override
	public Box getBoundingBox() {
		return box;
	}

	@Override
	public void transformScaling(float scale) {
		// TODO Auto-generated method stub
		
	}
}
