package com.Lyeeedar.Util;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionShape;
import com.badlogic.gdx.math.Vector3;

public class Octtree <E> {
	
	public final Octtree<E> parent;
	public final Vector3 min = new Vector3();
	public final Vector3 mid = new Vector3();
	public final Vector3 max = new Vector3();
	public final Box box = new Box();
	public int numChildElements;
	public final Bag<E> elements = new Bag<E>();
	
	public Octtree<E>[] children = null;
	
	public Octtree(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		this.parent = parent;
		this.min.set(min);
		this.max.set(max);
		this.box.set(min, max);
	
		mid.set(max).sub(min).scl(0.5f).add(min);
	}
	
	public void divide(int num)
	{
		if (num > 0)
		{
			children = new Octtree[8];
			
			Vector3 tmp1 = Pools.obtain(Vector3.class);
			Vector3 tmp2 = Pools.obtain(Vector3.class);
			boolean x = false;
			boolean y = false;
			boolean z = false;
			
			for (int i = 0; i < 8; i++)
			{
				tmp1.x = (x) ? mid.x : min.x;
				tmp1.y = (y) ? mid.y : min.y;
				tmp1.z = (z) ? mid.z : min.z;
				
				tmp2.x = (x) ? max.x : mid.x;
				tmp2.y = (y) ? max.y : mid.y;
				tmp2.z = (z) ? max.z : mid.z;
				
				children[i] = getOcttree(this, tmp1, tmp2);
				
				if (x)
				{
					if (y)
					{
						z = !z;
					}
					y = !y;
				}
				x = !x;
			}
			
			for (Octtree<E> o : children) o.divide(num-1);
			
			Pools.free(tmp1);
			Pools.free(tmp2);
		}
	}
	
	public void remove(E e)
	{
		if (e == null) numChildElements--;
		else elements.remove(e);
		
		if (parent != null) parent.remove(null);
	}

	public Octtree<E> add(E e, CollisionShape<?> shape) {
		
		shape.reset();
		if (!box.collide(shape)) return null;
		if (children == null)
		{
			elements.add(e);
			return this;
		}
		
		int collisions = 0;
		Octtree<E> chosen = null;
		for (Octtree<E> o : children)
		{
			if (o.box.collide(shape)) 
			{
				collisions++;
				chosen = o;
			}
		}
		
		if (collisions == 1)
		{
			numChildElements++;
			return chosen.add(e, shape);
		}
		else
		{
			elements.add(e);
			return this;
		}
	}
	
	public Octtree<E> getOcttree(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		return new Octtree<E>(parent, min, max);
	}
}
