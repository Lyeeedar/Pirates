package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Pirates.GLOBALS.GENDER;

public class Society {
	
	public enum EMOTION
	{
		LOVE,
		HATE,
		JEALOUSY,
		FEAR,
		DUTY
	}
	
	public String name;
	public Relationship defaultAttitude;
	public HashMap<Society, Relationship> relationships;
	public HashMap<String, SocietyAttribute> attributes;
	
	public static class SocietyAttribute
	{
		public enum Scope
		{
			GLOBAL, // Everyone knows
			COMMON, // All entities from one level up know this
			PERSONAL, // Only this entites children know this
			SECRET // Only a select few know
		}
		
		String attribute;
		Scope scope;
		Object[] values;
	}
	
	public static class Island extends Society
	{
		public Village[] villages;
	}
	
	public static class Landmark extends Society
	{
		public Island island;
	}
	
	public static class Village extends Landmark
	{		
		public Family[] families;		
	}
	
	public static class Family extends Society
	{
		public Village village;
		
		public Person mother;
		public Person father;
		public Person[] children;
	}
	
	public static class Person extends Society
	{
		public enum AGE_GROUP
		{
			CHILD,
			TEENAGER,
			ADULT,
			OAP
		}
		
		public Family family;
		
		public GENDER gender;
		public int age;
		public AGE_GROUP age_group;
	}
	
	public static class Relationship
	{
		public Society src;
		public Society dst;
		
		public HashMap<EMOTION, Byte> emotions;
		
		public Relationship setEmotions(Relationship other)
		{
			for (Map.Entry<EMOTION, Byte> entry : other.emotions.entrySet()) emotions.put(entry.getKey(), entry.getValue());
			return this;
		}
		
		public Relationship reset()
		{
			emotions = new HashMap<EMOTION, Byte>();
			for (EMOTION e : EMOTION.values()) emotions.put(e, (byte)0);
			return this;
		}
		
		
		public static Relationship getMarriage()
		{
			Relationship rl = new Relationship().reset();
			rl.emotions.put(EMOTION.LOVE, (byte)100);
			rl.emotions.put(EMOTION.DUTY, (byte)50);
			
			return rl;
		}
		
	}

}
