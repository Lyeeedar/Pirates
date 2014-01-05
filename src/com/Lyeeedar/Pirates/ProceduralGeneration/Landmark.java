package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.Lyeeedar.Util.Bag;

public class Landmark {
	int x;
	int y;
	
	int width;
	int height;
	float elevation;
	
	Bag<int[]> entrances = new Bag<int[]>();
	
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
