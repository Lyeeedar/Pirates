package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.Random;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Family;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Person;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Relationship;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Village;
import com.Lyeeedar.Util.Bag;
import com.badlogic.gdx.math.Vector3;

public class SocietyGenerator {
	
	private static final int[][] offsets = {
		{1, 0},
		{-1, 0},
		{0, 1},
		{0, -1},
		{1, 1},
		{1, -1},
		{-1, 1},
		{-1, -1}
	};
	
	public static Bag<Entity> fillVillage(Landmark landmark, Building building)
	{
		Byte[][] grid = new Byte[landmark.width][landmark.height];
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				grid[x][y] = 0;
			}
		}
		grid[landmark.width/2][landmark.height/2] = 1;
		int[][][] paths = new int[landmark.entrances.size][0][0];
		int i = 0;
		
		for (int[] entrance : landmark.entrances)
		{
			AStarPathfind<Byte> pathFind = new AStarPathfind<Byte>(grid, entrance[0], entrance[1], landmark.width/2, landmark.height/2, new ByteHeuristic());
			int[][] path = pathFind.getPath();
			
			paths[i++] = path;
			
			for (int[] pos : path)
			{
				grid[pos[0]][pos[1]] = 1;
			}
		}
		
		for (int[][] path : paths)
		{
			for (int[] p : path)
			{
				for (int[] o : offsets)
				{
					int px = p[0] + o[0]*(building.width/2 + 1);
					int py = p[1] + o[1]*(building.height/2 + 1);
					if (check(grid, px, py, building.width, building.height))
					{
						fill(grid, px, py, building.width, building.height);
					}
				}
			}	
		}
		
		
		for (int x = 0; x < grid.length; x++)
		{
			for (int y = 0; y < grid[0].length; y++)
			{
				System.out.print(grid[x][y]+"\t");
			}
			System.out.print("\n");
		}
		return null;
	}
	
	public static boolean check(Byte[][] grid, final int x, final int y, final int width, final int height)
	{
		int w2 = width/2;
		int h2 = height/2;
		for (int nx = x-w2; nx < x+w2; nx++)
		{
			if (nx < 0 || nx >= grid.length) return false;
			for (int ny = y-h2; ny < y+h2; ny++)
			{
				if (ny < 0 || ny >= grid[0].length) return false;
				if (grid[nx][ny] != 0) return false;
			}
		}
		return true;
	}
	
	public static void fill(Byte[][] grid, final int x, final int y, final int width, final int height)
	{
		int w2 = width/2;
		int h2 = height/2;
		for (int nx = x-w2; nx < x+w2; nx++)
		{
			if (nx < 0 || nx >= grid.length) continue;
			for (int ny = y-h2; ny < y+h2; ny++)
			{
				if (ny < 0 || ny >= grid[0].length) continue;
				grid[nx][ny] = 2;
			}
		}
	}
	
	public static void main(String[] args)
	{
		Landmark l = new Landmark(0, 0, 30, 30, 0);
		l.addEntrance(0,  20);
		Building b = new Building();
		b.width = 5;
		b.height = 5;
		
		fillVillage(l, b);
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