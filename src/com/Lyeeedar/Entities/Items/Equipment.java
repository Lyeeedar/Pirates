package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Util.Factory;


public abstract class Equipment<E extends Equipment<E>> implements Factory<E> {
	
	@SuppressWarnings("unchecked")
	public E copy()
	{
		E n = newInstance();
		n.set((E) this);
		
		return n;
	}
	
	public abstract void set(E other);
	public abstract void update(float delta);
}
