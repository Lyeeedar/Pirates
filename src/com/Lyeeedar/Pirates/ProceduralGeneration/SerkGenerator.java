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
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.DelaunayMinimalGrapher;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.DelaunayPoint;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.DelaunayTriangle;
import com.Lyeeedar.Pirates.ProceduralGeneration.DelaunayTriangulation.Triangulation;
import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.SimplexOctaveGenerator;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Dungeon generation inspired by: http://forums.tigsource.com/index.php?topic=5174.msg799973#msg799973
 * @author Philip
 *
 */
public class SerkGenerator implements AbstractGenerator{
	
	protected static final float NOISE_PERSISTANCE = 4.0f;
	protected static final int NOISE_OCTAVES = 2;
	protected static final int BLUR_STEPS = 50;
	
	protected static final int LANDMARK_PLACE_ATTEMPTS = 150;

	final Array<Landmark> landmarks = new Array<Landmark>();
	final Building[] buildings = {new Building(15, 15, "", (byte) 1), new Building(5, 5, "", (byte) 1), new Building(10, 10, "", (byte) 1), new Building(10, 5, "", (byte) 1)};
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
			placeLandmark(50, 50);
		}
		System.out.println("placed landmarks");

		connectLandmarks();
		System.out.println("connected landmarks");
		
		for (Landmark landmark : landmarks)
		{
			SocietyGenerator.fillVillage(landmark, buildings, seed, (byte) 1, (byte) 0);
			Byte[][] lgrid = landmark.grid;

			for (int x = 0; x < lgrid.length; x++)
			{
				for (int y = 0; y < lgrid[0].length; y++)
				{
					tiles[(int) (x+landmark.x)][(int) (y+landmark.y)].ground = (lgrid[x][y] != 0) ? 1.0f : 0.0f;
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
				c.a = (tiles[x][y].height-seaBed) / height;
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
	
	public Array<Landmark> getLandmarks()
	{
		return landmarks;
	}
	
	protected void connectLandmarks()
	{
		Array<float[]> points = new Array<float[]>(false, landmarks.size);
		
		for (Landmark l : landmarks)
		{
			points.add(new float[]{l.x+(l.width/2), l.y+(l.height/2)});
		}

		DelaunayMinimalGrapher dmg = new DelaunayMinimalGrapher();
		
		Array<int[][]> paths = new Array<int[][]>(false, 16);
		dmg.connectPoints(points, paths);

		for (int[][] p : paths)
		{
			AStarPathfind<AbstractTile> pathFind = new AStarPathfind<AbstractTile>(tiles, p[0][0], p[0][1], p[1][0], p[1][1], new AbstractTile.AbstractTileHeuristic());
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
				tiles[x][y].height *= height;
				
				int mx = x - s2;
				int my = y - s2;
				
				float dist = (mx*mx) + (my*my);
							
				tiles[x][y].height *= 1.0f - MathUtils.clamp(dist/max, 0.0f, 1.0f);
				
				tiles[x][y].height += seaBed;
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
