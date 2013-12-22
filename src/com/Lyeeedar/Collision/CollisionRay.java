package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class CollisionRay extends CollisionShape<CollisionRay> {
	
	public final Ray ray = new Ray(new Vector3(), new Vector3());
	public float len;
	
	public final Vector3 intersection = new Vector3();
	public float dist;
	
	public final Box box = new Box();
	
	public CollisionRay()
	{
		reset();
		calculateBoundingBox();
	}
	
	public CollisionRay(Ray ray, float len)
	{
		this.ray.set(ray);
		this.len = len;
		
		reset();
		
		calculateBoundingBox();
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
		intersection.set(ray.direction).scl(len).add(ray.origin);
		dist = len;
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
	public boolean collide(SymbolicMesh mesh) {
		return mesh.collide(this);
	}

	@Override
	public CollisionRay set(CollisionRay other) {
		ray.set(other.ray);
		len = other.len;
		intersection.set(other.intersection);
		dist = other.dist;
		box.set(other.box);
		
		return this;
	}

	@Override
	public CollisionRay copy() {
		return Pools.obtain(CollisionRay.class).set(this);
	}

	@Override
	public void transformPosition(Matrix4 matrix) {
		ray.origin.mul(matrix);
		intersection.mul(matrix);
		box.transformPosition(matrix);
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
	public Vector3 getPosition()
	{
		return ray.origin;
	}

	@Override
	public void setPosition(Vector3 position) {
		ray.origin.set(position);
		box.setPosition(position);
	}

	@Override
	public void setRotation(Vector3 rotation) {
		ray.direction.set(rotation);
	}

	@Override
	protected String string() {
		return ""+ray+" Len: "+len+" I: "+intersection+" Dist: "+dist;
	}

	@Override
	public void calculateBoundingBox() {

		float minx = ray.origin.x;
		float miny = ray.origin.y;
		float minz = ray.origin.z;
		
		float maxx = ray.origin.x;
		float maxy = ray.origin.y;
		float maxz = ray.origin.z;
		
		if (intersection.x < minx) minx = intersection.x;
		if (intersection.x > maxx) maxx = intersection.x;
		
		if (intersection.y < miny) miny = intersection.y;
		if (intersection.y > maxy) maxy = intersection.y;
		
		if (intersection.z < minz) minz = intersection.z;
		if (intersection.z > maxz) maxz = intersection.z;
		
		box.width = (maxx-minx)/2.0f;
		box.height = (maxy-miny)/2.0f;
		box.depth = (maxz-minz)/2.0f;
		
		box.center.set(minx+box.width, miny+box.height, minz+box.depth);
		
		box.reset();
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
		len *= scale;
		dist *= scale;
		
		//ray.origin.scl(scale);
		//intersection.scl(scale);
		
	}

	public CollisionShape<?> set(Ray ray2, float f) {

		ray.set(ray2);
		len = f;
		
		return this;
	}

	@Override
	public void setGeneric(CollisionShape<?> other) {
		set((CollisionRay)other);
	}

	@Override
	public BoundingBox getBoundingBox(BoundingBox bb) {
		return box.getBoundingBox(bb);
	}
}
