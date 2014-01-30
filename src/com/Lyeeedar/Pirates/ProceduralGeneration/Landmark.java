package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.utils.Array;


public class Landmark {
	int x;
	int y;
	
	int width;
	int height;
	float elevation;
	
	Array<int[]> entrances = new Array<int[]>();
	
	public Landmark(int x, int y, int width, int height, float elevation)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.elevation = elevation;
	}
	
	public void addEntrance(int x, int y)
	{
		entrances.add(new int[]{x, y});
	}
}
