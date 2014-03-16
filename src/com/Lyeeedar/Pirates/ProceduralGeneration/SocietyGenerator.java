package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import javax.swing.Box;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Queueables.TexturedMesh;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Family;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Person;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Relationship;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Village;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class SocietyGenerator {
	
	private static final int[][] offsets = {
		{1, 0, 0},
		{-1, 0, 0},
		{0, 1, 90},
		{0, -1, 90},
		{1, 1, 135},
		{-1, -1, 135},
		{-1, 1, 45},
		{1, -1, 45}
	};
	
	public static void fillVillage(Landmark landmark, Building[] buildings, long seed, byte pathval, byte emptyval)
	{
		Byte[][] grid = landmark.grid;
		
		final Random ran = new Random(seed);
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				grid[x][y] = emptyval;
			}
		}
		grid[landmark.width/2][landmark.height/2] = pathval;
		
		grid[landmark.width/2+1][landmark.height/2] = pathval;
		grid[landmark.width/2-1][landmark.height/2] = pathval;
		grid[landmark.width/2][landmark.height/2+1] = pathval;
		grid[landmark.width/2][landmark.height/2-1] = pathval;
		
		for (int[] entrance : landmark.entrances)
		{
			int ex = MathUtils.clamp(entrance[0], 0, grid.length-1);
			int ey = MathUtils.clamp(entrance[1], 0, grid[0].length-1);
			AStarPathfind<Byte> pathFind = new AStarPathfind<Byte>(grid, ex, ey, landmark.width/2, landmark.height/2, new ByteHeuristic());
			int[][] path = pathFind.getPath();
			
			for (int[] pos : path)
			{
				grid[pos[0]][pos[1]] = pathval;
				for (int[] o : offsets)
				{
					if (pos[0]+o[0] < 0 || pos[0]+o[0] >= grid.length || pos[1]+o[1] < 0 || pos[1]+o[1] >= grid[0].length) continue;
					grid[pos[0]+o[0]][pos[1]+o[1]] = pathval;
				}
			}
		}
		
		ArrayList<int[]> paths = flood(grid, pathval, landmark.width/2, landmark.height/2);
		
		for (int[] p : paths)
		{
			for (int[] o : offsets)
			{
				final Building building = buildings[ran.nextInt(buildings.length)].copy();
				int px = p[0] + o[0]*(building.w2 + 1);
				int py = p[1] + o[1]*(building.h2 + 1);
				building.rot.setToRotation(0, 1, 0, o[2]);
				building.resetGrid();
				building.x = px;
				building.y = py;
				if (check(grid, building, emptyval))
				{
					fill(grid, (byte) building.lotVal, building);
					AStarPathfind<Byte> pathFind = new AStarPathfind<Byte>(grid, px, py, p[0], p[1], new ByteHeuristic());
					int[][] path = pathFind.getPath();
					for (int[] pp : path)
					{
						if (grid[pp[0]][pp[1]] == emptyval) grid[pp[0]][pp[1]] = pathval;
					}
					
					landmark.buildings.add(building);
				}
			}	
		}
	}
	
	public static ArrayList<int[]> flood(Byte[][] grid, byte num, int sx, int sy)
	{
		boolean[][] checked = new boolean[grid.length][grid[0].length];
		ArrayList<int[]> array = new ArrayList<int[]>();
		
		Queue<int[]> queue = new LinkedList<int[]>();
		queue.add(new int[]{sx, sy});
		checked[sx][sy] = true;
		
		while (!queue.isEmpty())
		{
			int[] pos = queue.poll();
			array.add(pos);
			
			for (int i = 0; i < 4; i++)
			{
				int[] o = offsets[i];
				
				int[] t = {pos[0]+o[0], pos[1]+o[1]};
				
				if (t[0] < 0 || t[0] >= grid.length || t[1] < 0 || t[1] >= grid[0].length) continue;
				
				if (!checked[t[0]][t[1]] && grid[t[0]][t[1]] == num)
				{
					queue.add(t);
					checked[t[0]][t[1]] = true;
				}
			}
		}
			
		return array;
	}
	
	public static boolean check(Byte[][] grid, final Building building, final byte emptyval)
	{
		for (int ix = 0; ix < building.len; ix++)
		{
			for (int iy = 0; iy < building.len; iy++)
			{
				int x = building.x-building.len/2+ix;
				int y = building.y-building.len/2+iy;
				
				if (x < 0 || x >= grid.length || y < 0 || y >= grid[0].length) return false;
				
				if (building.grid[ix][ix] && grid[x][y] != emptyval) return false;
			}
		}
		
		return true;
	}
	
	public static void fill(Byte[][] grid, final byte num, final Building building)
	{
		for (int ix = 0; ix < building.len; ix++)
		{
			for (int iy = 0; iy < building.len; iy++)
			{
				int x = building.x-building.len/2+ix;
				int y = building.y-building.len/2+iy;
				
				if (building.grid[ix][ix]) grid[x][y] = building.lotVal;
			}
		}
	}
	
	public static void main(String[] args)
	{
		Landmark l = new Landmark(0, 0, 30, 30, 0);
		l.addEntrance(0,  20);
		l.addEntrance(29, 0);
		l.addEntrance(0,  0);
		l.addEntrance(29,  29);
		//Building[] b = {new Building(50, 50, "", (byte) 1), new Building(10, 50, "", (byte) 1), new Building(20, 40, "", (byte) 1), new Building(30, 30, "", (byte) 1), new Building(70, 70, "", (byte) 1), new Building(40, 20, "", (byte) 1)};
		
		Building[] b = {new Building(5, 5, "", (byte) 1)};
		
		fillVillage(l, b, 1337, (byte) 1, (byte) 0);
		
		Byte[][] grid = l.grid;
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				System.out.print(grid[x][y]+" ");
			}
			System.out.print("\n");
		}
	}
	
	public static Village createVillage(int numHouses, Relationship defaultAttitude, long seed)
	{
		Random ran = new Random(seed);
		
		Village village = new Village();
		village.defaultAttitude = new Relationship().reset().setEmotions(defaultAttitude);
		village.families = new Family[numHouses];
		
		for (int i = 0; i < numHouses; i++)
		{
			Family family = new Family();
			family.defaultAttitude = new Relationship().reset().setEmotions(defaultAttitude);
			
			family.father = new Person();
			family.father.defaultAttitude = new Relationship().reset().setEmotions(defaultAttitude);
			family.mother = new Person();
			family.mother.defaultAttitude = new Relationship().reset().setEmotions(defaultAttitude);
			
			int numChildren = ran.nextInt(5);
			family.children = new Person[numChildren];
			for (int rep = 0; rep < numChildren; rep++)
			{
				family.children[rep] = new Person();
				family.children[rep].defaultAttitude = new Relationship().reset().setEmotions(defaultAttitude);
			}
			
			village.families[i] = family;
		}
		
		return village;
	}

}

class ByteHeuristic implements AStarHeuristic<Byte>
{

	@Override
	public int getHeuristic(Byte current, Byte previous, Byte dst, int[] currentPos, int[] endPos, int distance) {
		return (int) (Vector3.dst(currentPos[0], currentPos[1], 0, endPos[0], endPos[1], 0)*2 + distance);
	}
	
}