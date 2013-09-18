package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Box extends CollisionShape<Box> {
	
	public final Vector3 center = new Vector3();
	public float width;
	public float height;
	public float depth;
	
	public Box()
	{
		
	}
	
	public Box(Vector3 center, float width, float height, float depth)
	{
		this.center.set(center);
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	public Box(Vector3 min, Vector3 max)
	{
		float minx = min.x;
		float miny = min.y;
		float minz = min.z;
		
		float maxx = max.x;
		float maxy = max.y;
		float maxz = max.z;
		
		float midx = minx+((maxx-minx)/2f);
		float midy = miny+((maxy-miny)/2f);
		float midz = minz+((maxz-minz)/2f);
		
		this.center.set(midx, midy, midz);
		this.width = midx-minx;
		this.height = midy-miny;
		this.depth = midz-minz;
	}
	
	public Box set(Vector3 center, float width, float height, float depth)
	{
		this.center.set(center);
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		return this;
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
	public Box set(Box other) {
		this.center.set(other.center);
		this.width = other.width;
		this.height = other.height;
		this.depth = other.depth;
		
		return this;
	}

	@Override
	public Box copy() {
		return new Box(center, width, height, depth);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		center.mul(matrix);
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		
	}

	@Override
	public Box obtain() {
		return Pools.obtain(Box.class).set(this);
	}

	@Override
	public void free() {
		Pools.free(this);
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
		return "Center: "+center+" W: "+width+" H: "+height+" D: "+depth;
	}

}
