package com.Lyeeedar.Collision;

import com.Lyeeedar.Collision.Octtree.OcttreeBox.CollisionType;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Octtree <E> {
	
	private static final int CASCADE_THRESHOLD = 10;
	
	public final Octtree<E> parent;
	
	public final Vector3 min = new Vector3();
	public final Vector3 mid = new Vector3();
	public final Vector3 max = new Vector3();
	public final OcttreeBox box;
	
	public final Array<OcttreeEntry<E>> elements = new Array<OcttreeEntry<E>>(false, CASCADE_THRESHOLD);
	
	public Octtree<E>[] children = null;
	
	public Octtree(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		this.parent = parent;
		this.min.set(min);
		this.max.set(max);
		
		mid.set(max).sub(min).scl(0.5f);
		Vector3 extents = new Vector3(mid);
		mid.add(min);
		
		this.box = new OcttreeBox(mid, extents, this);	
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
			
			Pools.free(tmp1);
			Pools.free(tmp2);
			
			for (Octtree<E> o : children) o.divide(num-1);
		}
	}
	
	public void remove(OcttreeEntry<?> octtreeEntry)
	{
		elements.removeValue((OcttreeEntry<E>) octtreeEntry, true);
	}

	public void add(OcttreeEntry<E> e) {
		if (children == null && elements.size > CASCADE_THRESHOLD)
		{
			cascade();
		}
				
		if (children == null)
		{
			e.box.parent = this;
			elements.add(e);
			return;
		}
		
		int collisions = 0;
		Octtree<E> chosen = null;
		for (int i = 0; i < children.length; i++)
		{
			Octtree<E> o = children[i];
			
			if (o.box.isIntersecting(e.box)) 
			{
				collisions++;
				chosen = o;
			}
		}
		
		if (collisions == 0)
		{
			throw new RuntimeException("Octtree Add failed to find collisions!!!");
		}
		
		if (collisions == 1)
		{
			chosen.add(e);
		}
		else
		{
			e.box.parent = this;
			elements.add(e);
		}
	}
	
	public void collectAll(Array<E> output, OcttreeBox box)
	{		
		for (int i = 0; i < elements.size; i++)
		{
			OcttreeEntry<E> oe = elements.get(i);

			if (oe.box.isIntersecting(box)) 
			{
				output.add(oe.e);
			}
		}
		
		if (children != null) for (Octtree<E> o : children)
		{
			if (o.box.isIntersecting(box))
				o.collectAll(output, box);
		}
	}
	
	public void collectAll(Array<E> output, Frustum frustum, boolean check)
	{		
		for (int i = 0; i < elements.size; i++)
		{
			OcttreeEntry<E> oe = elements.get(i);

			CollisionType collision = check ? oe.box.isIntersecting(frustum) : CollisionType.INSIDE;
			if (collision != CollisionType.OUTSIDE) 
			{
				output.add(oe.e);
			}
		}
		
		if (children != null) for (Octtree<E> o : children)
		{
			CollisionType collision = check ? o.box.isIntersecting(frustum) : CollisionType.INSIDE;
			if (collision == CollisionType.INSIDE)
				o.collectAll(output, frustum, false);
			else if (collision == CollisionType.INTERSECT)
				o.collectAll(output, frustum, true);
		}
	}
	
	public void cascade()
	{
		divide(1);
		
		Array<OcttreeEntry<E>> temp = new Array<OcttreeEntry<E>>(elements.size);
		temp.addAll(elements);
		elements.clear();
		
		for (OcttreeEntry<E> e : temp)
		{
			add(e);
		}
	}
	
	public Octtree<E> getOcttree(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		return new Octtree<E>(parent, min, max);
	}
	
	public OcttreeEntry<E> createEntry(E e, Vector3 pos, Vector3 extents)
	{
		OcttreeBox box = new OcttreeBox(pos, extents, null);
		return new OcttreeEntry<E>(e, box);
	}
	
	public static class OcttreeEntry<E>
	{
		public E e;
		public OcttreeBox box;
		
		public OcttreeEntry(E e, OcttreeBox box)
		{
			this.e = e;
			this.box = box;
		}
		
		public void updatePosition()
		{
			box.parent.remove(this);
			
			Octtree root = box.parent;
			while (root.parent != null) root = root.parent;
			root.add(this);
		}
		
	}
	
	public static class OcttreeBox
	{
		public enum CollisionType
		{
			OUTSIDE,
			INTERSECT,
			INSIDE
		}
		
		public final Vector3 pos = new Vector3();
		public final Vector3 extents = new Vector3();
		
		public Octtree parent;
		
		public OcttreeBox(Vector3 pos, Vector3 extents, Octtree<?> parent)
		{
			this.pos.set(pos);
			this.extents.set(extents);
			this.parent = parent;
		}
		
		public boolean isIntersecting(OcttreeBox box)
		{
			float lx = Math.abs(this.pos.x - box.pos.x);
			float sumx = (this.extents.x) + (box.extents.x); 
			
			float ly = Math.abs(this.pos.y - box.pos.y);
			float sumy = (this.extents.y) + (box.extents.y); 
			
			float lz = Math.abs(this.pos.z - box.pos.z);
			float sumz = (this.extents.z) + (box.extents.z); 
			
			return (lx <= sumx && ly <= sumy && lz <= sumz);  
		}
		
		private int lastFail = 0;
		
		public CollisionType isIntersecting(Frustum frustum)
		{
			float m, n; 
			int i = lastFail;
			CollisionType result = CollisionType.INSIDE;

			while (true)
			{
				Plane p = frustum.planes[i];
				
				m = (pos.x * p.normal.x) + (pos.y * p.normal.y) + (pos.z * p.normal.z) + p.d;
				n = (extents.x *Math.abs(p.normal.x)) + (extents.y * Math.abs(p.normal.y)) + (extents.z * Math.abs(p.normal.z));

				if (m + n < 0) 
				{
					lastFail = i;
					return CollisionType.OUTSIDE;
				}
				if (m - n < 0)
				{
					result = CollisionType.INTERSECT;
				}

				i++;
				if (i == 6) i = 0;
				if (i == lastFail) break;
			} 
			return result;
		}
	}
}
