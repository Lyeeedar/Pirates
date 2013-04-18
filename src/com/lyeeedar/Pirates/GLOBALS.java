package com.lyeeedar.Pirates;

import Entities.NavMesh;

import com.badlogic.gdx.math.Vector3;

public class GLOBALS {
	
	public static final Vector3 DEFAULT_UP = new Vector3(0, 1, 0);
	public static final Vector3 DEFAULT_ROTATION = new Vector3(0, 0, 1);
	
	public static final float GRAVITY = -15;
	
	public static final int[] RESOLUTION = {800, 600};
	public static final int[] SCREEN_SIZE = {800, 600};

	public static boolean ANDROID = false;
	
	public static NavMesh TEST_NAV_MESH;
}
