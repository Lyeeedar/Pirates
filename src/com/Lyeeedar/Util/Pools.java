package com.Lyeeedar.Util;

import com.badlogic.gdx.utils.ObjectMap;

public class Pools {

	private static ObjectMap<Class, ThreadsafePool> pools = new ObjectMap();
	
	public static <T> ThreadsafePool<T> get (Class<T> type) {
		ThreadsafePool<T> pool = pools.get(type);
		if (pool == null) {
			pool = new ThreadsafePool<T>(type);
			pools.put(type, pool);
		}
		return pool;
	}

	/** Obtains an object from the {@link #get(Class) pool}. */
	public static <T> T obtain (Class<T> type) {
		return (T)get(type).obtain();
	}

	/** Frees an object from the {@link #get(Class) pool}. */
	public static void free (Object object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		ThreadsafePool pool = pools.get(object.getClass());
		if (pool == null)
		{
			pool = new ThreadsafePool(object.getClass());
			pools.put(object.getClass(), pool);
		}
		pool.free(object);
	}

	private Pools () {
	}

}
