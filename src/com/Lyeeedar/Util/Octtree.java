package com.Lyeeedar.Util;

import java.util.Collection;

import com.Lyeeedar.Collision.BulletWorld.SimpleContactCallback;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;

public class Octtree <E> {
	
	private static final int CASCADE_THRESHOLD = 10;
	
	public final Octtree<E> parent;
	public final Vector3 min = new Vector3();
	public final Vector3 mid = new Vector3();
	public final Vector3 max = new Vector3();
	public final btCollisionObject box = new btCollisionObject();
	public int numChildElements;
	public final Bag<OcttreeEntry<E, btCollisionObject>> elements = new Bag<OcttreeEntry<E, btCollisionObject>>();
	private final SimpleContactCallback result = new SimpleContactCallback();
	private final btCollisionWorld world;
	
	public Octtree<E>[] children = null;
	
	public Octtree(Octtree<E> parent, Vector3 min, Vector3 max, btCollisionWorld world)
	{
		this.parent = parent;
		this.min.set(min);
		this.max.set(max);
		mid.set(max).sub(min).scl(0.5f);
		this.box.setCollisionShape(new btBoxShape(new Vector3(mid)));
		this.world = world;
	
		mid.add(min);
		
		this.box.setWorldTransform(new Matrix4().setToTranslation(mid));
	}
	
	public void divide(int num)
	{
		if (children == null && num > 0)
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
				
				children[i] = getOcttree(this, tmp1, tmp2, world);
				
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
		else 
		{
			for (int i = 0; i < elements.size; i++)
			{
				if (elements.get(i).e.equals(e))
				{
					elements.remove(i);
					break;
				}
			}
		}
		
		if (parent != null) parent.remove(null);
	}

	public void add(E e, btCollisionObject shape) {
		if (children == null && elements.size > CASCADE_THRESHOLD)
		{
			cascade();
		}
		
		result.hasCollided = false;
		world.contactPairTest(box, shape, result);
		if (!result.hasCollided) return;
		
		if (children == null)
		{
			elements.add(new OcttreeEntry<E, btCollisionObject>(e, shape));
			informPlacement(e, this);
			return;
		}
		
		int collisions = 0;
		Octtree<E> chosen = null;
		for (Octtree<E> o : children)
		{
			result.hasCollided = false;
			world.contactPairTest(o.box, shape, result);
			if (result.hasCollided) 
			{
				collisions++;
				chosen = o;
			}
		}
		
		if (collisions == 1)
		{
			numChildElements++;
			chosen.add(e, shape);
		}
		else
		{
			elements.add(new OcttreeEntry<E, btCollisionObject>(e, shape));
			informPlacement(e, this);
		}
	}
	
	public void collectAll(Collection<E> list, btCollisionObject shape)
	{
		result.hasCollided = false;
		world.contactPairTest(box, shape, result);
		if (!result.hasCollided) return;
		
		for (OcttreeEntry<E, btCollisionObject> oe : elements)
		{
			result.hasCollided = false;
			world.contactPairTest(oe.c, shape, result);
			if (result.hasCollided) 
			{
				list.add(oe.e);
			}
		}
		
		if (children != null) for (Octtree<E> o : children)
		{
			o.collectAll(list, shape);
		}
	}
	
	public void informPlacement(E e, Octtree<E> o)
	{
		
	}
	
	public void cascade()
	{
		divide(1);
		
		Bag<OcttreeEntry<E, btCollisionObject>> tempBag = new Bag<OcttreeEntry<E, btCollisionObject>>(0);
		tempBag.set(elements);
		elements.clear();
		
		for (OcttreeEntry<E, btCollisionObject> e : tempBag)
		{
			add(e.e, e.c);
		}
	}
	
	public Octtree<E> getOcttree(Octtree<E> parent, Vector3 min, Vector3 max, btCollisionWorld world)
	{
		return new Octtree<E>(parent, min, max, world);
	}
	
	public static class OcttreeEntry<E, C extends btCollisionObject>
	{
		public E e;
		public C c;
		
		public OcttreeEntry(E e, C c)
		{
			this.e = e;
			this.c = c;
		}
	}
}
