package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Building {
	
	public final String grammarName;
	
	public final int width;
	public final int height;
	
	public final int w2;
	public final int h2;
	
	public int x;
	public int y;
	
	public final int len;
	
	public byte lotVal;
	
	public Matrix4 rot = new Matrix4();
	public Matrix4 invrot = new Matrix4();
	Vector3 temp = new Vector3();
	
	public final boolean[][] grid;
	
	public Building(int width, int height, String grammarName, byte lotVal)
	{
		this.width = width;
		this.height = height;
		
		this.w2 = width/2;
		this.h2 = height/2;
		
		this.grammarName = grammarName;
		
		this.lotVal = lotVal;
		
		len = (int) Vector3.dst(0, 0, 0, width, height, 0)+2;
		
		grid = new boolean[len][len];
	}
	
	public void resetGrid()
	{
		invrot.set(rot).inv();
		
		for (int x = 0; x < len; x++)
		{
			for (int y = 0; y < len; y++)
			{
				grid[x][y] = inBounds(x-len/2, y-len/2);
			}
		}
	}
	
	public boolean inBounds(int x, int y)
	{
		temp.set(x, 0, y).mul(invrot);	
		return temp.x >= -w2 && temp.x <= w2 && temp.z >= -h2 && temp.z <= h2;
	}

	public Building copy()
	{
		Building nb = new Building(width, height, grammarName, lotVal);
		nb.x = x;
		nb.y = y;
		
		nb.rot.set(rot);
		nb.resetGrid();
		
		return nb;
	}
}
