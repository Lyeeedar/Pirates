package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Triangle extends CollisionShape<Triangle> {
	
	public final Vector3 v1 = new Vector3();
	public final Vector3 v2 = new Vector3();
	public final Vector3 v3 = new Vector3();
	
	public final Box box = new Box();
	
	public Triangle()
	{
		
	}
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this.v1.set(v1);
		this.v2.set(v2);
		this.v3.set(v3);
		calculateBoundingBox();
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
		this.box.set(other.box);
		
		return this;
	}
	
	public Triangle set(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this.v1.set(v1);
		this.v2.set(v2);
		this.v3.set(v3);
		calculateBoundingBox();
		
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
		calculateBoundingBox();
		
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

	@Override
	public void calculateBoundingBox() {
		float minx = v1.x;
		float maxx = v1.x;
		
		float miny = v1.y;
		float maxy = v1.y;
		
		float minz = v1.z;
		float maxz = v1.z;
		
		if (v2.x < minx) minx = v2.x;
		if (v2.x < maxx) maxx = v2.x;
		
		if (v2.y < miny) miny = v2.y;
		if (v2.y < maxy) maxy = v2.y;
		
		if (v2.z < minz) minz = v2.z;
		if (v2.z < maxz) maxz = v2.z;
		
		if (v3.x < minx) minx = v3.x;
		if (v3.x < maxx) maxx = v3.x;
		
		if (v3.y < miny) miny = v3.y;
		if (v3.y < maxy) maxy = v3.y;
		
		if (v3.z < minz) minz = v3.z;
		if (v3.z < maxz) maxz = v3.z;
		
		box.width = (maxx-minx)/2.0f;
		box.height = (maxy-miny)/2.0f;
		box.depth = (maxz-minz)/2.0f;
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

	@Override
	public void setGeneric(CollisionShape<?> other) {
		set((Triangle)other);
	}
}
