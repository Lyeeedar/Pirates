package com.lyeeedar.Pirates;

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
}
