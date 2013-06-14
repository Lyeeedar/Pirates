package com.Lyeeedar.Collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;



public abstract class CollisionShape<E extends CollisionShape<E>> {

	@SuppressWarnings("rawtypes")
	public boolean collide(CollisionShape shape)
	{
		if (shape instanceof Sphere) return collide((Sphere) shape);
		else if (shape instanceof Cylinder) return collide((Cylinder) shape);
		else if (shape instanceof Box) return collide((Box) shape);
		else if (shape instanceof Triangle) return collide((Triangle) shape);
		else if (shape instanceof CollisionRay) return collide((CollisionRay) shape);
		
		return false;
	}
	
	public abstract boolean collide(Sphere sphere);
	public abstract boolean collide(Cylinder cylinder);
	public abstract boolean collide(Box rect);
	public abstract boolean collide(Triangle tri);
	public abstract boolean collide(CollisionRay ray);
	
	public abstract void setPosition(Vector3 position);
	public abstract void setRotation(Vector3 rotation);
	
	public abstract void transformPosition(Matrix4 matrix);
	public abstract void transformDirection(Matrix4 matrix);
	
	public abstract void reset();
	
	public abstract E set(E other);
	public abstract E copy();
	
	public abstract E obtain();
	public abstract void free();
	
	@Override
	public String toString()
	{
		return string();
	}
	
	protected abstract String string();
}
