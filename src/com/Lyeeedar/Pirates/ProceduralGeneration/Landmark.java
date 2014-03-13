package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.utils.Array;


public class Landmark {
	public float x;
	public float y;
	
	public int width;
	public int height;
	public float elevation;
	
	Array<int[]> entrances = new Array<int[]>();
	
	public Landmark(float x, float y, int width, int height, float elevation)
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
	
	public boolean intersects(Landmark other)
	{
		return
				other.x < x+width &&
				other.x+other.width > x &&
				other.y < y+height &&
				other.y+other.height < y;
	}
	
	public boolean inBounds(float x, float z)
	{
		return x > this.x && x < this.x+width && z > y && z < y+height;
	}
}
