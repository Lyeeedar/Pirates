package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.utils.Array;


public class Landmark {
	public int x;
	public int y;
	
	public int width;
	public int height;
	public float elevation;
	
	public Byte[][] grid;
	public Array<int[]> entrances = new Array<int[]>();
	public Array<Building> buildings = new Array<Building>();
	
	public Landmark(int x, int y, int width, int height, float elevation)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.elevation = elevation;
		this.grid = new Byte[width][height];
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
		return x > this.x && x < this.x+width && z > this.y && z < this.y+height;
	}
	
	public byte getGridValue(int x, int z)
	{
		//System.out.println(x+" "+z);
		if (!inBounds(x, z)) return -1;
		
		int tx = x-this.x;
		int tz = z-this.y;
		
		return grid[tx][tz];
	}
	
	public void testGrid()
	{
		byte gval = 0;
		for (int ix = 0; ix < width; ix++)
		{
			for (int iy = 0; iy < height; iy++)
			{
				grid[ix][iy] = gval;
			}
			
			gval++;
			if (gval > 5) gval = 0;
		}
	}
	
	public void findEntrance(int x1, int y1, int x2, int y2)
	{		
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);

		int sx = (x1 < x2) ? 1 : -1;
		int sy = (y1 < y2) ? 1 : -1;

		int err = dx - dy;
		
		int px = x1;
		int py = y1;

		while (true) 
		{
		    if (x1 < 0 || x1 >= width || y1 < 0 || y1 >= height)
		    {
		    	entrances.add(new int[]{px, py});
		    	break;
		    }

		    if (x1 == x2 && y1 == y2) {
		        break;
		    }

		    int e2 = 2 * err;

		    if (e2 > -dy) {
		        err = err - dy;
		        px = x1;
		        x1 = x1 + sx;
		    }

		    if (e2 < dx) {
		        err = err + dx;
		        py = y1;
		        y1 = y1 + sy;
		    }
		}
	}
}
