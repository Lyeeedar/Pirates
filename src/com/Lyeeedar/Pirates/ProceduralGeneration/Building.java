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
	
	private int ix;
	private int iy;
	
	public byte lotVal;
	
	public Matrix4 rot = new Matrix4();
	Vector3 temp = new Vector3();
	
	public Building(int width, int height, String grammarName, byte lotVal)
	{
		this.width = width;
		this.height = height;
		
		this.w2 = (int) (width/2.0f);
		this.h2 = (int) (height/2.0f);
		
		this.grammarName = grammarName;
		
		this.lotVal = lotVal;
	}
	
	public void reset()
	{
		ix = -w2;
		iy = -h2;
	}
	
	public Vector3 nextPos()
	{
		ix++;
		if (ix == w2)
		{
			ix = -w2;
			iy++;
			if (iy == h2)
			{
				return null;
			}
		}
		
		return temp.set(ix, 0, iy).mul(rot);
	}

	public Building copy()
	{
		Building nb = new Building(width, height, grammarName, lotVal);
		nb.x = x;
		nb.y = y;
		
		nb.rot.set(rot);
		
		return nb;
	}
}
