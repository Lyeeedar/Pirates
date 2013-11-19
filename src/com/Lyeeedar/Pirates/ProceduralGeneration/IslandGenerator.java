package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.PerlinNoise;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class IslandGenerator {

	public Mesh getIsland(int width, int height, float maxHeight)
	{
		IslandCreator ic = new IslandCreator(width, height);
		return ic.islands.get(0).getMesh(maxHeight);
	}

}

class Island {

	final double[][] heightMap;
	final int w;
	final int h;

	public Island(double[][] heightMap)
	{
		this.heightMap = heightMap;
		w = heightMap.length;
		h = heightMap[0].length;
	}

	public Mesh getMesh(float maxHeight)
	{
		final int vertexSize =
				3 // position
				+ 3 // normal
				+ 2 // texture
				;

		final float[] vertices = new float[w*h*vertexSize];
		final short[] indices = new short[(w-1)*(h-1)*6];

		int i = 0;
		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				vertices[i++] = x*15;
				vertices[i++] = (float) heightMap[x][y]*maxHeight;
				vertices[i++] = y*15;

				vertices[i++] = 0;
				vertices[i++] = 1;
				vertices[i++] = 0;

				vertices[i++] = x;
				vertices[i++] = y;

			}
		}

		Vector3 edge1 = Pools.obtain(Vector3.class);
		Vector3 edge2 = Pools.obtain(Vector3.class);
		Vector3 cross = Pools.obtain(Vector3.class);

		i = 0;
		for (int x = 0; x < w-1; x++)
		{
			for (int y = 0; y < h-1; y++)
			{
				int t1p1 = y+(x*h);
				int t1p2 = y+1+((x+1)*h);
				int t1p3 = y+((x+1)*h);

				indices[i++] = (short) t1p1;
				indices[i++] = (short) t1p2;
				indices[i++] = (short) t1p3;

				edge1
				.set(vertices[(t1p2*vertexSize)+0], vertices[(t1p2*vertexSize)+1], vertices[(t1p2*vertexSize)+2])
				.sub(vertices[(t1p1*vertexSize)+0], vertices[(t1p1*vertexSize)+1], vertices[(t1p1*vertexSize)+2]);

				edge2
				.set(vertices[(t1p3*vertexSize)+0], vertices[(t1p3*vertexSize)+1], vertices[(t1p3*vertexSize)+2])
				.sub(vertices[(t1p1*vertexSize)+0], vertices[(t1p1*vertexSize)+1], vertices[(t1p1*vertexSize)+2]);

				cross.set(edge1).crs(edge2).nor();

				vertices[(t1p1*vertexSize)+3] = (vertices[(t1p1*vertexSize)+3] + cross.x)/2;
				vertices[(t1p1*vertexSize)+4] = (vertices[(t1p1*vertexSize)+4] + cross.y)/2;
				vertices[(t1p1*vertexSize)+5] = (vertices[(t1p1*vertexSize)+5] + cross.z)/2;

				vertices[(t1p2*vertexSize)+3] = (vertices[(t1p2*vertexSize)+3] + cross.x)/2;
				vertices[(t1p2*vertexSize)+4] = (vertices[(t1p2*vertexSize)+4] + cross.y)/2;
				vertices[(t1p2*vertexSize)+5] = (vertices[(t1p2*vertexSize)+5] + cross.z)/2;

				vertices[(t1p3*vertexSize)+3] = (vertices[(t1p3*vertexSize)+3] + cross.x)/2;
				vertices[(t1p3*vertexSize)+4] = (vertices[(t1p3*vertexSize)+4] + cross.y)/2;
				vertices[(t1p3*vertexSize)+5] = (vertices[(t1p3*vertexSize)+5] + cross.z)/2;


				int t2p1 = y+(x*h);
				int t2p2 = y+1+(x*h);
				int t2p3 = y+1+((x+1)*h);

				indices[i++] = (short) t2p1;
				indices[i++] = (short) t2p2;
				indices[i++] = (short) t2p3;

				edge1
				.set(vertices[(t2p2*vertexSize)+0], vertices[(t2p2*vertexSize)+1], vertices[(t2p2*vertexSize)+2])
				.sub(vertices[(t2p1*vertexSize)+0], vertices[(t2p1*vertexSize)+1], vertices[(t2p1*vertexSize)+2]);

				edge2
				.set(vertices[(t2p3*vertexSize)+0], vertices[(t2p3*vertexSize)+1], vertices[(t2p3*vertexSize)+2])
				.sub(vertices[(t2p1*vertexSize)+0], vertices[(t2p1*vertexSize)+1], vertices[(t2p1*vertexSize)+2]);

				cross.set(edge1).crs(edge2).nor();

				vertices[(t2p1*vertexSize)+3] = (vertices[(t2p1*vertexSize)+3] + cross.x)/2;
				vertices[(t2p1*vertexSize)+4] = (vertices[(t2p1*vertexSize)+4] + cross.y)/2;
				vertices[(t2p1*vertexSize)+5] = (vertices[(t2p1*vertexSize)+5] + cross.z)/2;

				vertices[(t2p2*vertexSize)+3] = (vertices[(t2p2*vertexSize)+3] + cross.x)/2;
				vertices[(t2p2*vertexSize)+4] = (vertices[(t2p2*vertexSize)+4] + cross.y)/2;
				vertices[(t2p2*vertexSize)+5] = (vertices[(t2p2*vertexSize)+5] + cross.z)/2;

				vertices[(t2p3*vertexSize)+3] = (vertices[(t2p3*vertexSize)+3] + cross.x)/2;
				vertices[(t2p3*vertexSize)+4] = (vertices[(t2p3*vertexSize)+4] + cross.y)/2;
				vertices[(t2p3*vertexSize)+5] = (vertices[(t2p3*vertexSize)+5] + cross.z)/2;
			}
		}

		Pools.free(edge1);
		Pools.free(edge2);
		Pools.free(cross);

		Mesh mesh = new Mesh(true, w*h, indices.length,
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}

}

class IslandCreator
{
	int width;
	int height;

	int persistance = 3;
	double[][] grid;
	double[][] mask;
	int[][] city;

	List<Island> islands;

	int seed = new Random().nextInt(999);
	PerlinNoise noise = new PerlinNoise(0.4, 0.07, 2, 8, seed);

	boolean view = false;
	public IslandCreator(int width, int height)
	{
		this.width = width;
		this.height = height;
		grid = new double[width][height];
		mask = new double[width][height];
		city = new int[width][height];
				
		createMask();
		redoNoise(persistance);
		islands = new ArrayList<Island>();
		islands.add(new Island(grid));
		//islands = findIslands();
	}

	public List<Island> findIslands()
	{      
		List<Island> islands = new ArrayList<Island>();
		List<Number[]> map = new ArrayList<Number[]>();
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				map.add(new Number[]{grid[x][y], x, y});
			}
		}
		Collections.sort(map, new Comparator<Number[]>(){
			@Override
			public int compare(Number[] arg0, Number[] arg1) {
				double val1 = (Double)arg0[0];
				int int1 = (int)(val1*100);
				double val2 = (Double)arg1[0];
				int int2 = (int)(val2*100);
				return int2-int1;
			}});

		for (int i = 0; i < map.size(); i++)
		{
			Number[] entry = map.get(i);
			int[] pos = {(Integer) entry[1], (Integer) entry[2]};
			double height = (Double)entry[0];

			if (height < 0.2) break;

			if (city[pos[0]][pos[1]] == 0)
			{
				islands.add(getIsland(pos));
			}
		}

		return islands;
	}

	public Island getIsland(int[] pos)
	{
		List<int[]> tiles = new ArrayList<int[]>();
		ArrayDeque<int[]> queue = new ArrayDeque<int[]>();

		queue.add(pos);

		int[] current = {0, 0};
		while (queue.size() != 0)
		{
			//System.out.println("island" + current[0]+" "+current[1]);
			current = queue.pop();
			tiles.add(current);

			if (grid[current[0]][current[1]] >= 0.2)
			{
				city[current[0]][current[1]] = 1;

				if (current[0]-1 >= 0) {
					int[] n = {current[0]-1, current[1]};
					if (!checkChecked(tiles, queue, n)) queue.add(n);
				}
				if (current[0]+1 < grid.length) {
					int[] n = {current[0]+1, current[1]};
					if (!checkChecked(tiles, queue, n)) queue.add(n);
				}
				if (current[1]-1 >= 0) {
					int[] n = {current[0], current[1]-1};
					if (!checkChecked(tiles, queue, n)) queue.add(n);
				}
				if (current[1]+1 < grid[0].length ) {
					int[] n = {current[0], current[1]+1};
					if (!checkChecked(tiles, queue, n)) queue.add(n);
				}
			}
		}

		int minx = grid.length+1;
		int miny = grid[0].length+1;
		int maxx = 0;
		int maxy = 0;

		for (int[] t : tiles)
		{
			if (t[0] < minx) minx = t[0];
			if (t[0] > maxx) maxx = t[0];
			if (t[1] < miny) miny = t[1];
			if (t[1] > maxy) maxy = t[1];
		}

		double[][] islandTiles = new double[maxx-minx][maxy-miny];

		for (int x = minx; x < maxx; x++)
		{
			for (int y = miny; y < maxy; y++)
			{
				islandTiles[x-minx][y-miny] = grid[x][y];
			}
		}

		return new Island(islandTiles);
	}

	public boolean checkChecked(List<int[]> checked, ArrayDeque<int[]> queue, int[] pos)
	{
		for (int[] i : checked)
		{
			if (pos[0] == i[0] && pos[1] == i[1]) return true;
		}
		for (int[] i : queue)
		{
			if (pos[0] == i[0] && pos[1] == i[1]) return true;
		}
		return false;
	}

	public void createCity()
	{
		//Random ran = new Random();

		// final int cw = city.length;
		//final int ch = city[0].length;

		// final int[] start = {ran.nextInt(cw), ran.nextInt(ch)};

		for (int i = 0; i < 20; i++)
		{

		}
	}

	public void createMask()
	{
		List<Integer> directions = new ArrayList<Integer>();
		final Random ran = new Random();
		final float w = mask.length;
		final float w2 = w/2.0f;
		final float w4 = w/4.0f;
		final float h = mask[0].length;
		final float h2 = h/2.0f;
		final float h4 = h/4.0f;

		double biggest = 0;
		for (int i = 0; i < 10000; i++)
		{
			int ww = (int)(ran.nextFloat()*w);
			if (ww < 0) ww = 0;
			if (ww >= w) ww = (int) (w-1);
			int hh = (int)(ran.nextFloat()*h);
			if (hh < 0) hh = 0;
			if (hh >= h) hh = (int) (h-1);

			int[] pos = {ww, hh};
			for (int ii = 0; ii < 5000; ii++)
			{
				mask[pos[0]][pos[1]]++;
				if (mask[pos[0]][pos[1]] > biggest) biggest = mask[pos[0]][pos[1]];

				//eval move
				if (pos[0]-1 < 0) {break;}
				else if (mask[pos[0]-1][pos[1]] <= mask[pos[0]][pos[1]])
				{
					directions.add(1);
				}
				if (pos[0]+1 >= w) {break;}
				else if (mask[pos[0]+1][pos[1]] <= mask[pos[0]][pos[1]])
				{
					directions.add(3);
				}
				if (pos[1]+1 >= h) {break;}
				else if (mask[pos[0]][pos[1]+1] <= mask[pos[0]][pos[1]])
				{
					directions.add(2);
				}
				if (pos[1]-1 < 0) {break;}
				else if (mask[pos[0]][pos[1]-1] <= mask[pos[0]][pos[1]])
				{
					directions.add(4);
				}

				if (directions.size() == 0) continue;

				int dir = directions.get(ran.nextInt(directions.size()));
				directions.clear();

				if (dir == 1)
				{
					pos[0]--;
				}
				else if (dir == 2)
				{
					pos[1]++;
				}
				else if (dir == 3)
				{
					pos[0]++;
				}
				else
				{
					pos[1]--;
				}
			}
		}

		for (int x = 0; x < w; x++)
		{
			for (int y = 0; y < h; y++)
			{
				mask[x][y] /= biggest;
				if (x == 0 || x == w-1 || y == 0 || y == h-1)
				{
					mask[x][y] = 0;
				}
				else if (x == 1 || x == w-2 || y == 1 || y == h-2)
				{
					mask[x][y] *= 0.3f;
				}
				else if (x == 2 || x == w-3 || y == 2 || y == h-3)
				{
					mask[x][y] *= 0.6f;
				}
			}
		}
	}

	public void redoNoise(int persistance)
	{
		double hw = grid.length/2.0;
		double hh = grid[0].length/2.0;
		noise.Set(persistance/10.0, 0.05, 0.08, 8, seed);
		double maxHeight = 0;

		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				double height = noise.GetHeight(x/(grid.length/100.0), y/(grid[0].length/100.0));

				height *= height;
				height *= height;

				int mx = (int)(((float)x/(float)grid.length)*mask.length);
				int my = (int)(((float)y/(float)grid[0].length)*mask[0].length);
				height *= mask[mx][my];
				//height /= 2;

				if (height > maxHeight) maxHeight = height;

				grid[x][y] = height;
			}
		}

		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				grid[x][y] /= maxHeight;
			}
		}
	}
}