package com.Lyeeedar.Util;

import java.lang.reflect.Constructor;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.utils.GdxRuntimeException;

public class ThreadsafePool<E> {

	private final LinkedBlockingQueue<E> pools = new LinkedBlockingQueue<E>();
	private final Class<E> type;

	public ThreadsafePool(Class<E> type)
	{
		this.type = type;
	}
	
	public E obtain()
	{
		E e = pools.poll();
		
		if (e == null)
		{
			try {
				return type.newInstance();
			} catch (Exception ex) {
				Constructor ctor;
				try {
					ctor = type.getConstructor((Class[])null);
				} catch (Exception ex2) {
					try {
						ctor = type.getDeclaredConstructor((Class[])null);
						ctor.setAccessible(true);
					} catch (NoSuchMethodException ex3) {
						throw new RuntimeException("Class cannot be created (missing no-arg constructor): " + type.getName());
					}
				}
				try {
					return (E)ctor.newInstance();
				} catch (Exception ex3) {
					throw new GdxRuntimeException("Unable to create new instance: " + type.getName(), ex);
				}
			}
		}

		return e;
	}
	
	public void free(E e)
	{
		pools.add(e);
	}
}
