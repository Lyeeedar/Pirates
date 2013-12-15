package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Box extends CollisionShape<Box> {
	
	public final Vector3 center = new Vector3();
	public float width;
	public float height;
	public float depth;

	public Box()
	{
		if (width == 0) width = 0.1f;
		if (height == 0) height = 0.1f;
		if (depth == 0) depth = 0.1f;
		
		if (width < 0) width *= -1;
		if (height < 0) height *= -1;
		if (depth < 0) depth *= -1;
	}
	
	public Box(Vector3 center, float width, float height, float depth)
	{
		set(center, width, height, depth);
	}
	
	public Box(Vector3 min, Vector3 max)
	{
		set(min, max);
	}
	
	public Box set(Vector3 center, float width, float height, float depth)
	{
		this.center.set(center);
		this.width = width;
		this.height = height;
		this.depth = depth;
		
		if (width == 0) width = 0.1f;
		if (height == 0) height = 0.1f;
		if (depth == 0) depth = 0.1f;
		
		if (width < 0) width *= -1;
		if (height < 0) height *= -1;
		if (depth < 0) depth *= -1;
		
		return this;
	}
	
	public Box set(Vector3 min, Vector3 max)
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
		
		if (width == 0) width = 0.1f;
		if (height == 0) height = 0.1f;
		if (depth == 0) depth = 0.1f;
		
		if (width < 0) width *= -1;
		if (height < 0) height *= -1;
		if (depth < 0) depth *= -1;
		
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
		if (width == 0) width = 0.1f;
		if (height == 0) height = 0.1f;
		if (depth == 0) depth = 0.1f;
		
		if (width < 0) width *= -1;
		if (height < 0) height *= -1;
		if (depth < 0) depth *= -1;
		
	}

	@Override
	protected String string() {
		return "Center: "+center+" W: "+width+" H: "+height+" D: "+depth;
	}

	@Override
	public void calculateBoundingBox() {
		
	}

	@Override
	public void setScaling(Vector3 scale) {
	}

	@Override
	public boolean checkBoundingBox(Box box) {
		return true;
	}

	@Override
	public Box getBoundingBox() {
		return this;
	}

	@Override
	public void transformScaling(float scale) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setGeneric(CollisionShape<?> other) {
		set((Box)other);
	}
	
	@Override
	public BoundingBox getBoundingBox(BoundingBox bb) {
		
		bb.min.set(center).sub(width, height, depth);
		bb.min.set(center).add(width, height, depth);
		
		return bb;
	}

}
