package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NameGenerator {
	
	private static final char postfix = '>';
	private static final char prefix = '<';
	private final String fullPrefix;
	
	private final HashMap<String, Cluster> clusters = new HashMap<String, Cluster>();
	public final int order;
	public final float prior;
	
	public final Random ran;
	
	public NameGenerator(String[] names, int order, float prior, long seed)
	{
		this.order = order;
		this.prior = prior;
		
		this.ran = new Random(seed);
		
		String fp = "";
		for (int i = 0; i < order; i++) fp += prefix;
		this.fullPrefix = fp;
		
		for (String name : names)
		{
			addName(name);
		}
		
	}
	
	public String generate(int min, int max)
	{	
		String name = fullPrefix;
		
		while (name.charAt(name.length()-1) != postfix && name.length() < max+order)
		{
			String substr = name.substring(name.length()-order);
			name += backoff(substr);
		}
		
		if (name.length() > 0)
		{
			name = name.substring(name.lastIndexOf(prefix)+1, name.length()-1).toLowerCase();
			String first = name.substring(0, 1);
			name = name.replaceFirst(first, first.toUpperCase());
		}
		
		if (name.length() < min) return generate(min, max);
		
		return name;
	}
	
	public char backoff(String cluster)
	{
		if (cluster.length() == 1) return postfix;
		if (clusters.containsKey(cluster))
		{
			return clusters.get(cluster).getLetter(ran);
		}
		else return backoff(cluster.substring(1));
	}
	
	public void addName(String name)
	{
		name = fullPrefix + name + postfix;
		
		for (int i = order; i < name.length(); i++)
		{
			String substr = name.substring(i-order, i);
			char letter = name.charAt(i-1);
			for (int j = 0; j < substr.length()-1; j++)
			{
				String precond = substr.substring(j, substr.length()-1);
				
				if (clusters.containsKey(precond))
				{
					clusters.get(precond).addLetter(letter);
				}
				else
				{
					Cluster cluster = new Cluster(precond);
					cluster.addLetter(letter);
					clusters.put(precond, cluster);
				}
			}
		}
	}
	
//	public void print()
//	{
//		for (Map.Entry<String, Cluster> entry : clusters.entrySet())
//		{
//			System.out.println(entry.getKey());
//			break;
//		}		
//	}
	
	public final class Cluster
	{
		public final String precondition;
		public final HashMap<Character, ClusterChar> map = new HashMap<Character, ClusterChar>();
		public final ArrayList<ClusterChar> array = new ArrayList<ClusterChar>();
		public int max = 1;
		
		public Cluster(String precondition)
		{
			this.precondition = precondition;
		}
		
		public void addLetter(char c)
		{
			max++;
			if (map.containsKey(c))
			{
				ClusterChar cc = map.get(c);
				cc.count++;
			}
			else
			{
				ClusterChar cc = new ClusterChar(c);
				map.put(c, cc);
				array.add(cc);
			}
		}
		
		public char getLetter(Random ran)
		{
			int threshold = ran.nextInt(max);
			int total = 0;
			for (ClusterChar cc : array)
			{
				total += cc.count;
				if (total >= threshold) return cc.c;
			}
			return '@';
		}
		
//		public void print()
//		{
//			for (ClusterChar cc : array)
//			{
//				System.out.println(cc.c+" "+cc.count);
//			}
//		}
	}
	
	public final class ClusterChar implements Comparable<ClusterChar>
	{
		final char c;
		int count = 1;
		
		public ClusterChar(char c)
		{
			this.c = c;
		}

		@Override
		public int compareTo(ClusterChar cc) {
			if (cc.count == count) return c - cc.c;
			return count - cc.count; 
		}
	}
	
	public static void main(String[] args)
	{
		String[] names = {};
		NameGenerator namegen = new NameGenerator(names, 3, 0, 8008135);
		
		BufferedReader br = null;
		 
		try {
 
			String cl;
 
			br = new BufferedReader(new FileReader("male.txt"));
 
			while ((cl = br.readLine()) != null) {
				String[] split = cl.split(" +");
				namegen.addName(split[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		//namegen.print();
		
		for (int i = 0; i < 20; i++)
		{
			System.out.println(namegen.generate(3, 8)+" "+namegen.generate(5, 12));
		}
	}
}
