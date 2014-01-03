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
	
	public static Bag<Entity> fillVillage(Landmark landmark)
	{
		Byte[][] grid = new Byte[landmark.width][landmark.height];
		grid[landmark.width/2][landmark.height/2] = 1;
		
		for (int[] entrance : landmark.entrances)
		{
			AStarPathfind<Byte> pathFind = new AStarPathfind<Byte>(grid, entrance[0], entrance[1], landmark.width/2, landmark.height/2, new ByteHeuristic());
			int[][] path = pathFind.getPath();
			
			for (int[] pos : path)
			{
				grid[pos[0]][pos[1]] = 1;
			}
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
	public int getHeuristic(Byte current, Byte dst, int[] currentPos, int[] endPos, int distance) {
		return (int) (Vector3.dst(currentPos[0], currentPos[1], 0, endPos[0], endPos[1], 0) + distance);
	}
	
}