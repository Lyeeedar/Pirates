/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.DelaunayPoint;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.DelaunayTriangle;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.Triangulation;
import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.SimplexOctaveGenerator;
import com.Lyeeedar.Util.Bag;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Dungeon generation inspired by: http://forums.tigsource.com/index.php?topic=5174.msg799973#msg799973
 * @author Philip
 *
 */
public class SerkGenerator implements AbstractGenerator{
	
	protected static final float NOISE_PERSISTANCE = 4.0f;
	protected static final int NOISE_OCTAVES = 2;
	protected static final int BLUR_STEPS = 1;
	
	protected static final int LANDMARK_PLACE_ATTEMPTS = 150;

	final Bag<Landmark> landmarks = new Bag<Landmark>();
	final Building[] buildings = {new Building(15, 15), new Building(5, 5), new Building(10, 10), new Building(10, 5)};
	AbstractTile[][] tiles;
	final Random ran;
	final long seed;
	final int size;
	final int scale;
	final int height;
	final int seaBed;
	
	public SerkGenerator(int size, int scale, int height, int seaBed, long seed)
	{
		this.ran = new Random(seed);
		this.seed = seed;
		this.size = size;
		this.scale = scale;
		this.height = height;
		this.seaBed = seaBed;
	}
	
	@Override
	public Color[][] generate(List<Entity> entities)
	 {
		tiles = new AbstractTile[size][size];
		
		setHeight();
		System.out.println("done heights");
		
		int reps = 5;
		for (int i = 0 ; i < reps; i++)
		{
			placeLandmark(5, 5);
		}
		System.out.println("placed landmarks");

		connectLandmarks();
		System.out.println("connected landmarks");
		
		for (Landmark landmark : landmarks)
		{
			Byte[][] lgrid = new Byte[landmark.width][landmark.height];
			List<Entity> houses = SocietyGenerator.fillVillage(lgrid, landmark, buildings, seed);
			for (Entity e : houses)
			{
				PositionalData pData = e.readOnlyRead(PositionalData.class);
				pData.position.x /= (float)size; pData.position.x *= (float)scale;
				pData.position.z /= (float)size; pData.position.z *= (float)scale;
				pData.position.y -= 4;
				pData.calculateComposed();
				e.update(0);
			}
			entities.addAll(houses);
			for (int x = 0; x < lgrid.length; x++)
			{
				for (int y = 0; y < lgrid[0].length; y++)
				{
					tiles[x+landmark.x][y+landmark.y].ground = (lgrid[x][y] != 0) ? 1.0f : 0.0f;
				}
			}
		}
		
		Color[][] array = new Color[size][size];
		int iy = 0;
		int ix = 0;
		boolean b = true;
		boolean cc = true;
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				Color c = new Color(0.0f, 0.0f, 0.0f, 1.0f);
				c.a = (tiles[x][y].height-seaBed) / (height/10);
				float max = 0.0f;
				for (float f : tiles[x][y].slope) if (f > max) max = f;
				c.b = (max < 1.0f) ? 0.0f : (float) (max/(Math.PI/2.0));
				c.g = (1.0f-c.b)*tiles[x][y].ground;
				c.r = (tiles[x][y].height > 30) ? (1.0f-(c.b+c.g)) : 0.0f ;
				
				array[x][y] = c;
				
				iy++;
				if (iy == 10)
				{
					iy = 0;
					b = !b;
				}
				cc = !cc;
			}
			ix++;
			if (ix == 10)
			{
				ix = 0;
				b = !b;
			}
			cc = !cc;
		}
		System.out.println("done colours");
		
		return array;
	}
	
	public Bag<Landmark> getLandmarks()
	{
		return landmarks;
	}
	
	protected void connectLandmarks()
	{
		ArrayList<DelaunayPoint> landmarkPnts = new ArrayList<DelaunayPoint>();
		
		for (Landmark l : landmarks)
		{
			DelaunayPoint p = new DelaunayPoint(l.x+(l.width/2), l.y+(l.height/2));
			landmarkPnts.add(p);
		}

		int dsize = size * 10;
		DelaunayTriangle initialTriangle = new DelaunayTriangle(
				new DelaunayPoint(-dsize, -dsize),
				new DelaunayPoint(dsize, -dsize),
				new DelaunayPoint(0, dsize));
		Triangulation dt = new Triangulation(initialTriangle);
		
		for (DelaunayPoint p : landmarkPnts)
		{
			dt.delaunayPlace(p);
		}
		
		ArrayList<DelaunayPoint[]> paths = new ArrayList<DelaunayPoint[]>();
		
		for (DelaunayTriangle tri : dt)
		{
			calculatePaths(paths, tri);
		}

		for (DelaunayPoint[] p : paths)
		{
			AStarPathfind<AbstractTile> pathFind = new AStarPathfind<AbstractTile>(tiles, (int)p[0].coord(0), (int)p[0].coord(1), (int)p[1].coord(0), (int)p[1].coord(1), new AbstractTile.AbstractTileHeuristic());
			markPath(pathFind.getPath(), 2);
		}
	}
	
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
	
	protected void markPath(int[][] path, int width)
	{
		Landmark landmark = tiles[path[0][0]][path[0][1]].landmark;
		if (landmark == null) System.err.println("Error! Landmark Linking path did not start in a Landmark!");
		
		AbstractTile t = null;
		AbstractTile lt = null;
		for (int[] pos : path)
		{
			lt = t;
			t = tiles[pos[0]][pos[1]];
			if (landmark == null && t.landmark != null)
			{
				lt.gate = true;
				t.path = true;
				t.landmark.addEntrance(pos[0], pos[1]);
			}
			else if (landmark != null && t.landmark == null)
			{
				t.gate = true;
				t.path = true;
				landmark.addEntrance(pos[0], pos[1]);
			}
			else
			{
				t.path = true;
			}
			landmark = t.landmark;
			
			for (int i = 1; i <= width; i++)
			{
				for (int[] offset : offsets)
				{
					AbstractTile at = tiles[pos[0]+offset[0]*i][pos[1]+offset[1]*i];
					
					if (at.ground != 1.0f)
					{
						at.height += t.height;
						at.height /= 2.0f;
					}
					
					at.ground = 1.0f;
				}
			}
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
    	else if (p1.coord(0) > size || p2.coord(0) > size)
    	{
    		ignorePnts.add(new DelaunayPoint[]{p1, p2});
    	}
    	else if (p1.coord(1) > size || p2.coord(1) > size)
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
  
	protected void setHeight()
	{
		SimplexOctaveGenerator noise = new SimplexOctaveGenerator(seed, NOISE_OCTAVES);
		noise.setScale(0.006f);
		
		int s2 = size/2;
		float max = (s2*s2);
		
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				tiles[x][y] = new AbstractTile();
				tiles[x][y].x = x * scale;
				tiles[x][y].y = y * scale;
				tiles[x][y].height = (float) ((noise.noise(x, y, 2, 0.5f, true)+1.0f)/2.0f);
				tiles[x][y].height *= tiles[x][y].height;
				tiles[x][y].height *= (float)height;
				
				int mx = x - s2;
				int my = y - s2;
				
				float dist = (mx*mx) + (my*my);
							
				tiles[x][y].height *= 1.0f - MathUtils.clamp(dist/max, 0.0f, 1.0f);
				
				tiles[x][y].height += seaBed;
				
				System.out.println(tiles[x][y].height);
			}
		}
		
		float step = (float)scale / (float)size;
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				for (int i = 0; i < 4; i++)
				{
					if (x+offsets[i][0] < 0 || x+offsets[i][0] >= size || y+offsets[i][1] < 0 || y+offsets[i][1] >= size) continue;
					tiles[x][y].slope[i] = (float) Math.abs(Math.atan((tiles[x][y].height-tiles[x+offsets[i][0]][y+offsets[i][1]].height)/step));
				}
			}
		}
	}
	
	protected boolean placeLandmark(int width, int height)
	{
		int px = 0;
		int py = 0;
		
		for (int i = 0; i < LANDMARK_PLACE_ATTEMPTS; i++)
		{
			 px = BLUR_STEPS+ran.nextInt(size-width-BLUR_STEPS-BLUR_STEPS);
			 py = BLUR_STEPS+ran.nextInt(size-height-BLUR_STEPS-BLUR_STEPS);
			
			 if (checkLandmark(px, py, width, height))
			 {
					addLandmark(px, py, width, height);
					return true;
			 }
		}
		System.err.println("Failed to place landmark!");
		return false;
	}
	
	protected float lerp(float s, float e, float a)
	{
		return s + (e-s)*a;
	}
	
	protected void addLandmark(int px, int py, int width, int height)
	{
		int mx = px+(width/2);
		int my = py+(height/2);
		
		float h = tiles[mx][my].height;
		
		
		Landmark landmark = new Landmark(px, py, width, height, h);
		landmarks.add(landmark);
		
		for (int x = px; x < px+width; x++)
		{
			for (int y = py; y < py+height; y++)
			{
				tiles[x][y].landmark = landmark;
				tiles[x][y].height = h;
				tiles[x][y].ground = 1.0f;
			}
		}
		
		// Blur x--
		for (int y = py-BLUR_STEPS+1; y < py+height+BLUR_STEPS-1; y++)
		{
			float dh = tiles[px-BLUR_STEPS][y].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[px-i][y].height = lerp(h, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur x++
		for (int y = py-BLUR_STEPS+1; y < py+height+BLUR_STEPS-1; y++)
		{
			float dh = tiles[px+width+BLUR_STEPS][y].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[px+width+i][y].height = lerp(h, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y--
		for (int x = px; x < px+width; x++)
		{
			float dh = tiles[x][py-BLUR_STEPS].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py-i].height = lerp(h, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y++
		for (int x = px; x < px+width; x++)
		{
			float dh = tiles[x][py+height+BLUR_STEPS].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py+height+i].height = lerp(h, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y-- x0
		for (int x = px-BLUR_STEPS; x < px; x++)
		{
			float dh = tiles[x][py-BLUR_STEPS].height;
			float nh = tiles[x][py].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py-i].height =lerp(nh, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y-- x1
		for (int x = px+width; x < px+width+BLUR_STEPS; x++)
		{
			float dh = tiles[x][py-BLUR_STEPS].height;
			float nh = tiles[x][py].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py-i].height = lerp(nh, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y++ x0
		for (int x = px-BLUR_STEPS; x < px; x++)
		{
			float dh = tiles[x][py+height+BLUR_STEPS].height;
			float nh = tiles[x][py+height].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py+height+i].height = lerp(nh, dh, (float)i / (float)BLUR_STEPS);
			}
		}
		
		// Blur y++ x1
		for (int x = px+width; x < px+width+BLUR_STEPS; x++)
		{
			float dh = tiles[x][py+height+BLUR_STEPS].height;
			float nh = tiles[x][py+height].height;
			for (int i = 0; i < BLUR_STEPS; i++)
			{
				tiles[x][py+height+i].height = lerp(nh, dh, (float)i / (float)BLUR_STEPS);
			}
		}
	}
	
	protected boolean checkLandmark(int px, int py, int rwidth, int rheight)
	{
		for (int x = px-BLUR_STEPS; x < px+rwidth+BLUR_STEPS; x++)
		{
			for (int y = py-BLUR_STEPS; y < py+rheight+BLUR_STEPS; y++)
			{
				if (tiles[x][y].landmark != null)
				{
					return false;
				}
				else if (tiles[x][y].height <= 0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		SerkGenerator sg = new SerkGenerator(1024, 10, 1000, 10, 15);
		Color[][] array = sg.generate(new ArrayList<Entity>());
		BufferedImage image = ImageUtils.arrayToImage(array);
		System.err.println("done");
		try {
		    File outputfile = new File("saved.png");
		    ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
		}
		
	}
}
