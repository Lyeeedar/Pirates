package com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation;

import java.util.ArrayList;

import com.badlogic.gdx.utils.Array;

public class DelaunayMinimalGrapher
{
	public void connectPoints(Array<float[]> points, Array<int[][]> output)
	{		
		DelaunayTriangle initialTriangle = new DelaunayTriangle(
				new DelaunayPoint(-100000, -100000),
				new DelaunayPoint(100000, -100000),
				new DelaunayPoint(0, 100000));
		Triangulation dt = new Triangulation(initialTriangle);
		
		
		for (float[] pnt : points)
		{
			DelaunayPoint p = new DelaunayPoint(pnt[0], pnt[1]);
			dt.delaunayPlace(p);
		}
		
		ArrayList<DelaunayPoint[]> paths = new ArrayList<DelaunayPoint[]>();
		
		for (DelaunayTriangle tri : dt)
		{
			calculatePaths(paths, tri);
		}

		for (DelaunayPoint[] p : paths)
		{
			output.add(new int[][]{new int[]{(int)p[0].coord(0), (int)p[0].coord(1)}, new int[]{(int)p[1].coord(0), (int)p[1].coord(1)}});
		}
	}
	
	protected void calculatePaths(ArrayList<DelaunayPoint[]> paths, DelaunayTriangle triangle)
	{
		DelaunayPoint[] vertices = triangle.toArray(new DelaunayPoint[0]);
		
		int ignore = 0;
        double dist = 0;
        
        dist = Math.pow(2, vertices[0].coord(0)-vertices[1].coord(0))+Math.pow(2, vertices[0].coord(1)-vertices[1].coord(1));
        
        double temp = Math.pow(2, vertices[0].coord(0)-vertices[2].coord(0))+Math.pow(2, vertices[0].coord(1)-vertices[2].coord(1));
        if (dist < temp)
        {
        	dist = temp;
        	ignore = 1;		
        }
        
        temp = Math.pow(2, vertices[1].coord(0)-vertices[2].coord(0))+Math.pow(2, vertices[1].coord(1)-vertices[2].coord(1));
        if (dist < temp)
        {
        	dist = temp;
        	ignore = 2;		
        }
        
        if (ignore != 0 && checkIgnored(vertices[0], vertices[1]) && !checkAdded(vertices[0], vertices[1]))
        {
        	addPath(vertices[0], vertices[1], paths);
        }
        else
        {
        	ignorePnts.add(new DelaunayPoint[]{vertices[0], vertices[1]});
        }
        if (ignore != 1 && checkIgnored(vertices[0], vertices[2]) && !checkAdded(vertices[0], vertices[2]))
        {
        	addPath(vertices[0], vertices[2], paths);
        }
        else
        {
        	ignorePnts.add(new DelaunayPoint[]{vertices[0], vertices[2]});
        }
        if (ignore != 2 && checkIgnored(vertices[1], vertices[2]) && !checkAdded(vertices[1], vertices[2]))
        {
        	addPath(vertices[1], vertices[2], paths);
        }
        else
        {
        	ignorePnts.add(new DelaunayPoint[]{vertices[1], vertices[2]});
        }
	}
	
    protected void addPath(DelaunayPoint p1, DelaunayPoint p2, ArrayList<DelaunayPoint[]> paths)
    {
    	if (p1.coord(0) < 0 || p2.coord(0) < 0)
    	{
    		ignorePnts.add(new DelaunayPoint[]{p1, p2});
    	}
    	else if (p1.coord(1) < 0 || p2.coord(1) < 0)
    	{
    		ignorePnts.add(new DelaunayPoint[]{p1, p2});
    	}
    	else if (p1.coord(0) > 1000 || p2.coord(0) > 1000)
    	{
    		ignorePnts.add(new DelaunayPoint[]{p1, p2});
    	}
    	else if (p1.coord(1) > 1000 || p2.coord(1) > 1000)
    	{
    		ignorePnts.add(new DelaunayPoint[]{p1, p2});
    	}
    	else
    	{
        	addedPnts.add(new DelaunayPoint[]{p1, p2});
        	paths.add(new DelaunayPoint[]{p1, p2});
    	}
    }
	
	ArrayList<DelaunayPoint[]> ignorePnts = new ArrayList<DelaunayPoint[]>();
	ArrayList<DelaunayPoint[]> addedPnts = new ArrayList<DelaunayPoint[]>();
    
    protected boolean checkIgnored(DelaunayPoint p1, DelaunayPoint p2)
    {
    	for (DelaunayPoint[] p : ignorePnts)
    	{
    		if (p[0].equals(p1) && p[1].equals(p2))
    		{
    			return false;
    		}
    		else if (p[0].equals(p2) && p[1].equals(p1))
    		{
    			return false;
    		}
    	}
    	return true;
    }
    
    protected boolean checkAdded(DelaunayPoint p1, DelaunayPoint p2)
    {
    	for (DelaunayPoint[] p : addedPnts)
    	{
    		if (p[0].equals(p1) && p[1].equals(p2))
    		{
    			return true;
    		}
    		else if (p[0].equals(p2) && p[1].equals(p1))
    		{
    			return true;
    		}
    	}
    	return false;
    }
}
