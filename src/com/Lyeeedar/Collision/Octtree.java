package com.Lyeeedar.Collision;

import com.Lyeeedar.Collision.Octtree.OcttreeBox.CollisionType;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Octtree <E> {
	
	public static final int MASK_RENDER = 1 << 1;
	public static final int MASK_AI = 1 << 2;
	public static final int MASK_ENTITY = 1 << 3;
	public static final int MASK_SPELL = 1 << 4;
	
	private static final int CASCADE_THRESHOLD = 10;
	
	public Octtree<E> parent;
	
	public final Vector3 min = new Vector3();
	public final Vector3 mid = new Vector3();
	public final Vector3 max = new Vector3();
	public final OcttreeBox box = new OcttreeBox(new Vector3(), new Vector3(), this);
	
	public final Array<OcttreeEntry<E>> elements = new Array<OcttreeEntry<E>>(false, CASCADE_THRESHOLD);
	
	public Octtree<E>[] children = null;
	
	public Octtree()
	{
		
	}
	
	public Octtree(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		set(parent, min, max);
	}
	
	public Octtree<E> set(Octtree<E> parent, Vector3 min, Vector3 max)
	{
		this.parent = parent;
		this.min.set(min);
		this.max.set(max);
		
		mid.set(max).sub(min).scl(0.5f);
		Vector3 extents = new Vector3(mid);
		mid.add(min);
		
		this.box.set(mid, extents, this);
		
		return this;
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
	
	public void free()
	{
		if (children == null) return;
		
		boolean empty = true;
		
		for (Octtree<E> o : children)
		{
			if (o.children != null || o.elements.size != 0)
			{
				empty = false;
				break;
			}
		}
		
		if (empty)
		{
			for (Octtree<E> o : children)
			{
				Pools.free(o);
			}
			this.children = null;
			if (parent != null) parent.free();
		}
	}
	
	public void remove(OcttreeEntry<?> octtreeEntry)
	{
		elements.removeValue((OcttreeEntry<E>) octtreeEntry, true);
		if (parent != null) parent.free();
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
			
			if (o.box.isIntersecting(e.box) != CollisionType.OUTSIDE) 
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
	
	public void collectAll(Array<E> output, OcttreeShape shape, int bitmask)
	{
		internalCollectAll(output, shape, true, bitmask);
	}
	
	private void internalCollectAll(Array<E> output, OcttreeShape shape, boolean check, int bitmask)
	{		
		for (int i = 0; i < elements.size; i++)
		{
			OcttreeEntry<E> oe = elements.get(i);
			
			if (!oe.compareBitmask(bitmask)) continue;

			CollisionType collision = shape.isIntersecting(oe.box);
			if (collision != CollisionType.OUTSIDE) 
			{
				output.add(oe.e);
			}
		}
		
		if (children != null) for (Octtree<E> o : children)
		{
			CollisionType collision = check ? shape.isIntersecting(o.box) : CollisionType.INSIDE;
			if (collision == CollisionType.INSIDE)
				o.internalCollectAll(output, shape, false, bitmask);
			else if (collision == CollisionType.INTERSECT)
				o.internalCollectAll(output, shape, true, bitmask);
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
		return Pools.obtain(Octtree.class).set(parent, min, max);
	}
	
	public OcttreeEntry<E> createEntry(E e, Vector3 pos, Vector3 extents, int bitmask)
	{
		OcttreeBox box = new OcttreeBox(pos, extents, null);
		return new OcttreeEntry<E>(e, box, bitmask);
	}
	
	public static class OcttreeEntry<E>
	{
		public E e;
		public OcttreeBox box;
		public int bitmask;
		
		public OcttreeEntry(E e, OcttreeBox box, int bitmask)
		{
			this.e = e;
			this.box = box;
			this.bitmask = bitmask;
		}
		
		public void remove()
		{
			box.parent.remove(this);
		}
		
		public void updatePosition()
		{
			box.parent.remove(this);
			
			Octtree root = box.parent;
			while (root.parent != null) root = root.parent;
			root.add(this);
		}
		
		public boolean compareBitmask(int mask)
		{
			return (bitmask & mask) > 0;
		}
	}
	
	public static interface OcttreeShape
	{
		public CollisionType isIntersecting(OcttreeBox box);
		public void setPosition(Vector3 pos);
		public void setRotation(Vector3 rot);
	}
	
	public static class OcttreeBox implements OcttreeShape
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
		
		public int lastFail = 0;
		
		public OcttreeBox(Vector3 pos, Vector3 extents, Octtree<?> parent)
		{
			this.pos.set(pos);
			this.extents.set(extents);
			this.parent = parent;
		}
		
		public void set(Vector3 pos, Vector3 extents, Octtree<?> parent)
		{
			this.pos.set(pos);
			this.extents.set(extents);
			this.parent = parent;
		}
		
		public CollisionType isIntersecting(OcttreeBox box)
		{
			float lx = Math.abs(this.pos.x - box.pos.x);
			float sumx = (this.extents.x) + (box.extents.x); 
			
			float ly = Math.abs(this.pos.y - box.pos.y);
			float sumy = (this.extents.y) + (box.extents.y); 
			
			float lz = Math.abs(this.pos.z - box.pos.z);
			float sumz = (this.extents.z) + (box.extents.z); 
			
			return (lx <= sumx && ly <= sumy && lz <= sumz) ? CollisionType.INTERSECT : CollisionType.OUTSIDE;  
		}

		@Override
		public void setPosition(Vector3 pos)
		{
			this.pos.set(pos);
		}

		@Override
		public void setRotation(Vector3 rot)
		{

		}
	}
	
	public static class OcttreeFrustum implements OcttreeShape
	{
		PerspectiveCamera frustum;
		float sizeLim;
		
		public OcttreeFrustum(PerspectiveCamera frustum, float sizeLim)
		{
			this.frustum = frustum;
			this.sizeLim = sizeLim;
		}
		
		@Override
		public CollisionType isIntersecting(OcttreeBox box)
		{	
			if (sizeLim > 0)
			{
				float size = box.extents.x * box.extents.y * box.extents.z;
				float dst = frustum.position.dst2(box.pos);
				float far = frustum.far * frustum.far;
				
				float factor = size / sizeLim;
				
				float mdst = far * factor;
				
				if (dst > mdst) return CollisionType.OUTSIDE;
			}
			
			float m, n;
			int i = box.lastFail;
			CollisionType result = CollisionType.INSIDE;
			
			while (true)
			{
				Plane p = frustum.frustum.planes[i];
				
				m = (box.pos.x * p.normal.x) + (box.pos.y * p.normal.y) + (box.pos.z * p.normal.z) + p.d;
				n = (box.extents.x *Math.abs(p.normal.x)) + (box.extents.y * Math.abs(p.normal.y)) + (box.extents.z * Math.abs(p.normal.z));

				if (m + n < 0) 
				{
					box.lastFail = i;
					return CollisionType.OUTSIDE;
				}
				if (m - n < 0)
				{
					result = CollisionType.INTERSECT;
				}

				i++;
				if (i == 6) i = 0;
				if (i == box.lastFail) break;
			} 
			return result;
		}

		@Override
		public void setPosition(Vector3 pos)
		{
			frustum.position.set(pos);
			frustum.update();
		}

		@Override
		public void setRotation(Vector3 rot)
		{
			frustum.direction.set(rot);
			frustum.update();
		}
	}
	
}
