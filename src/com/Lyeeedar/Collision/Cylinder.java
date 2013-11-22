package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Cylinder extends CollisionShape<Cylinder> {

	public final Vector3 center = new Vector3();
	public final Vector3 rotation = new Vector3();
	public float radius;
	public float height;
	
	public Cylinder()
	{
		
	}
	
	public Cylinder(Vector3 center, Vector3 rotation, float radius, float height)
	{
		this.center.set(center);
		this.rotation.set(rotation);
		this.radius = radius;
		this.height = height;
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
	public Cylinder set(Cylinder other) {
		center.set(other.center);
		rotation.set(other.rotation);
		radius = other.radius;
		height = other.height;
		
		return this;
	}

	@Override
	public Cylinder copy() {
		return new Cylinder(center, rotation, radius, height);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		center.mul(matrix);
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		rotation.mul(matrix);
	}

	@Override
	public Cylinder obtain() {
		return Pools.obtain(Cylinder.class).set(this);
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
		rotation.set(rotation);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String string() {
		return "Center: "+center+" R: "+radius+" H: "+height;
	}

}
