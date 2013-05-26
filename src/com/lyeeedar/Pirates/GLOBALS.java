package com.lyeeedar.Pirates;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import Entities.SymbolicMesh;

import com.badlogic.gdx.math.Vector3;

public final class GLOBALS {
	
	public static final float STEP = 0.5f;
	
	public static final int MAX_SPEED_X = 60;
	public static final int MAX_SPEED_Y = 100;
	public static final int MAX_SPEED_Z = 60;
	
	public static final Vector3 DEFAULT_UP = new Vector3(0, 1, 0);
	public static final Vector3 DEFAULT_ROTATION = new Vector3(0, 0, 1);
	
	public static final float GRAVITY = -85;
	
	public static final int[] RESOLUTION = {800, 600};
	public static final int[] SCREEN_SIZE = {800, 600};

	public static boolean ANDROID = false;
	
	public static SymbolicMesh TEST_NAV_MESH;
	
	public static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	public static LinkedList<Future<?>> futureList = new LinkedList<Future<?>>();
	
	public static void submitTask(Runnable run)
	{
		futureList.add(threadPool.submit(run));
	}
	public static void waitTillTasksComplete()
	{
		for (Future<?> future : futureList)
		{
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		futureList.clear();
	}
	
	public static double angle(Vector3 v1, Vector3 v2, Vector3 tmp)
	{
		Vector3 referenceForward = v1;
		Vector3 referenceRight = tmp.set(GLOBALS.DEFAULT_UP).crs(referenceForward);
		Vector3 newDirection = v2;
		float angle = (float) Math.toDegrees(Math.acos(v1.dot(v2) / (v1.len()*v2.len())));
		float sign = (newDirection.dot(referenceRight) > 0.0f) ? 1.0f : -1.0f;
		float finalAngle = sign * angle;
		return finalAngle;
	}
}
