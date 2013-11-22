package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Triangle extends CollisionShape<Triangle> {
	
	public final Vector3 v1 = new Vector3();
	public final Vector3 v2 = new Vector3();
	public final Vector3 v3 = new Vector3();
	
	public Triangle()
	{
		
	}
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this.v1.set(v1);
		this.v2.set(v2);
		this.v3.set(v3);
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
	public Triangle set(Triangle other)
	{
		this.v1.set(other.v1);
		this.v2.set(other.v2);
		this.v3.set(other.v3);
		
		return this;
	}
	
	public Triangle set(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this.v1.set(v1);
		this.v2.set(v2);
		this.v3.set(v3);
		
		return this;
	}
	
	public Triangle set(
			float v1x, float v1y, float v1z,
			float v2x, float v2y, float v2z,
			float v3x, float v3y, float v3z
			)
	{
		this.v1.set(v1x, v1y, v1z);
		this.v2.set(v2x, v2y, v2z);
		this.v3.set(v3x, v3y, v3z);
		
		return this;
	}
	
	@Override
	public Triangle copy() {
		return new Triangle(v1, v2, v3);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		v1.mul(matrix);
		v2.mul(matrix);
		v3.mul(matrix);
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		v1.mul(matrix);
		v2.mul(matrix);
		v3.mul(matrix);
	}

	@Override
	public Triangle obtain() {
		return Pools.obtain(Triangle.class).set(this);
	}

	@Override
	public void free() {
		Pools.free(this);
	}
	
	@Override
	public Vector3 getPosition()
	{
		return v1;
	}

	@Override
	public void setPosition(Vector3 position) {
		
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
		return "V1: "+v1+" V2: "+v2+" V3: "+v3;
	}

}
