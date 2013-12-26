package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.Random;

import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Family;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Person;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Relationship;
import com.Lyeeedar.Pirates.ProceduralGeneration.Society.Village;

public class SocietyGenerator {
	
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
