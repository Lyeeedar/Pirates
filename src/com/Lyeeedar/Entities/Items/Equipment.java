package com.Lyeeedar.Entities.Items;


public abstract class Equipment<E extends Equipment<E>> {
	
	@SuppressWarnings("unchecked")
	public E copy()
	{
		E n = newInstance();
		n.set((E) this);
		
		return n;
	}
	
	public abstract void set(E other);
	public abstract E newInstance();
	public abstract void update(float delta);
}
