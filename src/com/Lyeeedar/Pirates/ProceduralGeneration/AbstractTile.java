package com.Lyeeedar.Pirates.ProceduralGeneration;

public class AbstractTile {
	
	float x, y;
	float height;
	int influence;
	
	float gn, gs, ge, gw;
	
	boolean landmark;
	boolean path;
	boolean gate;
	
	float ground = 0.0f;
	
	float[] slope = new float[4];
	
}
