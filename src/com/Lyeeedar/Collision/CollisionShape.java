package com.Lyeeedar.Collision;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;



public abstract class CollisionShape<E extends CollisionShape<E>> {
	
	public boolean collide(CollisionShape<?> shape)
	{
//		//System.out.println(shape.getClass()+": "+shape.getBoundingBox()+"     " + this.getClass() + ": " + getBoundingBox());
//		if (!checkBoundingBox(shape.getBoundingBox())) 
//		{
//			//System.out.println("fail");
//			return false;
//		}
		
		if      (shape instanceof Sphere)       return collide((Sphere) shape);
		else if (shape instanceof Cylinder)    	return collide((Cylinder) shape);
		else if (shape instanceof Box)          return collide((Box) shape);
		else if (shape instanceof Triangle)     return collide((Triangle) shape);
		else if (shape instanceof CollisionRay) return collide((CollisionRay) shape);
		else if (shape instanceof SymbolicMesh) return collide((SymbolicMesh) shape);
		else throw new RuntimeException("Invalid Shape types:\n 	This: "+this+"\n	That: "+shape);
	}
	
	public abstract void calculateBoundingBox();
	
	public abstract boolean checkBoundingBox(Box box);
	public abstract Box getBoundingBox();
	
	public abstract boolean collide(Sphere sphere);
	public abstract boolean collide(Cylinder cylinder);
	public abstract boolean collide(Box rect);
	public abstract boolean collide(Triangle tri);
	public abstract boolean collide(CollisionRay ray);
	public abstract boolean collide(SymbolicMesh mesh);
	
	public abstract void setScaling(Vector3 scale);
	public abstract void setPosition(Vector3 position);
	public abstract void setRotation(Vector3 rotation);
	
	public abstract Vector3 getPosition();
	
	public abstract void transformPosition(Matrix4 matrix);
	public abstract void transformDirection(Matrix4 matrix);
	public abstract void transformScaling(float scale);
	
	public abstract BoundingBox getBoundingBox(BoundingBox bb);
	
	public abstract void reset();
	
	public abstract E set(E other);
	public abstract void setGeneric(CollisionShape<?> other);
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
