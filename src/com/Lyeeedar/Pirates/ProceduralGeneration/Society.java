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
	
	public enum AGE_GROUP
	{
		CHILD,
		TEENAGER,
		ADULT,
		OAP
	}
	
	public String name;
	public Relationship defaultAttitude;
	public HashMap<Society, Relationship> relationships;
	
	public static class Village extends Society
	{
		public Society[] families;		
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
