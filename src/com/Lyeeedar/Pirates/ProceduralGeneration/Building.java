package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Building {
	
	public final int width;
	public final int height;
	
	public final int w2;
	public final int h2;
	
	int x;
	int y;
		
	Matrix4 rot = new Matrix4();
	Vector3 temp = new Vector3();
	
	public Building(int width, int height)
	{
		this.width = width;
		this.height = height;
		
		this.w2 = (int) (width/2.0f);
		this.h2 = (int) (height/2.0f);
	}
	
	public void reset()
	{
		x = -w2;
		y = -h2;
	}
	
	public Vector3 nextPos()
	{
		x++;
		if (x == w2)
		{
			x = -w2;
			y++;
			if (y == h2)
			{
				return null;
			}
		}
		
		return temp.set(x, 0, y).mul(rot);
	}

}
