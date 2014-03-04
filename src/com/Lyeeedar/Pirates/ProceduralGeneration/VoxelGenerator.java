package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayList;
import java.util.Iterator;

import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.FastSimplexNoise;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class VoxelGenerator
{
	public static Mesh generateTerrain(int x, int y, int z, float scale)
	{
		float offset = MathUtils.random(8008135);
		
		Point[][][] pointGrid = new Point[x][y][z];
		Cell[][][] cellGrid = new Cell[x-1][y-1][z-1];
		Array<Cell> intersectList = new Array<Cell>(false, (x-1)*(y-1)*(z-1));
		
		for (int ix = 0; ix < x-1; ix++)
		{
			for (int iy = 0; iy < y-1; iy++)
			{
				for (int iz = 0; iz < z-1; iz++)
				{
					cellGrid[ix][iy][iz] = new Cell(ix, iy, iz);
					for (int i = 0; i < 8; i++)
					{
						int px = ix+Cell.poffsets[i][0];
						int py = iy+Cell.poffsets[i][1];
						int pz = iz+Cell.poffsets[i][2];
						
						if (pointGrid[px][py][pz] == null)
						{
							pointGrid[px][py][pz] = new Point((x/-2+px)*scale, (y/-2+py)*scale, (z/-2+pz)*scale, FastSimplexNoise.noise(px+offset, py, pz+offset, 2, 0.5f, 8, 0.006f, false, null));
						}
						
						cellGrid[ix][iy][iz].points[Cell.poffsets[i][0]][Cell.poffsets[i][1]][Cell.poffsets[i][2]] = pointGrid[px][py][pz];
						
					}
					
					if (cellGrid[ix][iy][iz].hasIntersect()) intersectList.add(cellGrid[ix][iy][iz]);
				}
			}
		}
		
		System.out.println("Intersecting cells: "+intersectList.size);
		
		Array<float[]> storageArray = new Array<float[]>(false, intersectList.size*12*6);
		
		for (Cell cell : intersectList)
		{
			cell.buildQuad(storageArray, cellGrid);
		}
		
		float[] vertices = new float[storageArray.size * storageArray.get(0).length];
		
		int i = 0;
		for (float[] vertex : storageArray)
		{
			System.arraycopy(vertex, 0, vertices, i, vertex.length);
			i += vertex.length;
		}
		System.out.println("Collated Vertices! Num: "+storageArray.size);
		
		Mesh mesh = new Mesh(true, storageArray.size, 0, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		mesh.setVertices(vertices);
		return mesh;
	}
	
	private static class Point
	{
		// EDGE order = UP LEFT IN OUT RIGHT DOWN
		
		float x, y, z;
		float scalar;
		
		public Point(float x, float y, float z, float scalar)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.scalar = scalar;
		}
	}

	private static class Cell
	{
		// Point offsets in the grid
		public static final int[][] poffsets = { {0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 1}, {0, 1, 1}, {1, 1, 1}, {1, 0, 1} };
		
		// The order to rotate around each edge for making quads
		public static final int[][] xrotation = { {0, -1, 0}, {0, 0, 0}, {0, 0, -1}, {0, -1, 0}, {0, 0, -1}, {0, -1, -1} };
		public static final int[][] yrotation = { {0, 0, 0}, {-1, 0, 0}, {-1, 0, -1}, {0, 0, 0}, {-1, 0, -1}, {0, 0, -1} };
		public static final int[][] zrotation = { {-1, 0, 0}, {-1, -1, 0}, {0, -1, 0}, {-1, 0, 0}, {0, -1, 0}, {0, 0, 0} };
		
		// The edges of the cube
		public static final int[][] xEdges = { {0, 3}, {1, 2}, {4, 7}, {5, 6} };
		public static final int[][] yEdges = { {0, 1}, {3, 2}, {4, 5}, {7, 6} };
		public static final int[][] zEdges = { {0, 4}, {1, 5}, {2, 6}, {3, 7} };
		
		int x, y, z;
		
		public Cell(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		Point[][][] points = new Point[2][2][2];
		
		float[] featurePoint = null;
		
		public void buildQuad(Array<float[]> storageArray, Cell[][][] cellGrid)
		{			
			for (int[] edge : xEdges)
			{
				Point p1 = points[poffsets[edge[0]][0]][poffsets[edge[0]][1]][poffsets[edge[0]][2]];
				Point p2 = points[poffsets[edge[1]][0]][poffsets[edge[1]][1]][poffsets[edge[1]][2]];
				
				if (p1.scalar < 0 && p2.scalar < 0) continue;
				if (p1.scalar >= 0 && p2.scalar >= 0) continue;
				
				for (int[] rot : xrotation)
				{
					int cx = x + poffsets[edge[0]][0] + rot[0];
					int cy = y + poffsets[edge[0]][1] + rot[1];
					int cz = z + poffsets[edge[0]][2] + rot[2];
					
					if (cx < 0 || cy < 0 || cz < 0 || cx >= cellGrid.length || cy >= cellGrid[0].length || cz >= cellGrid[0][0].length)
					{
						float[] center = new float[8];
						
						center[0] += p1.x;
						center[1] += p1.y;
						center[2] += p1.z;
						
						center[0] += p2.x;
						center[1] += p2.y;
						center[2] += p2.z;
						
						center[0] /= 2.0f;
						center[1] /= 2.0f;
						center[2] /= 2.0f;
						
						center[3] = 0;
						center[4] = -1;
						center[5] = 0;
						
						center[6] = 0;
						center[7] = 1;
						
						storageArray.add(center);
					}
					else
					{
						storageArray.add(cellGrid[cx][cy][cz].getFeaturePoint());
					}
				}
			}
			
			for (int[] edge : yEdges)
			{
				Point p1 = points[poffsets[edge[0]][0]][poffsets[edge[0]][1]][poffsets[edge[0]][2]];
				Point p2 = points[poffsets[edge[1]][0]][poffsets[edge[1]][1]][poffsets[edge[1]][2]];
				
				if (p1.scalar < 0 && p2.scalar < 0) continue;
				if (p1.scalar >= 0 && p2.scalar >= 0) continue;
				
				for (int[] rot : yrotation)
				{
					int cx = x + poffsets[edge[0]][0] + rot[0];
					int cy = y + poffsets[edge[0]][1] + rot[1];
					int cz = z + poffsets[edge[0]][2] + rot[2];
					
					if (cx < 0 || cy < 0 || cz < 0 || cx >= cellGrid.length || cy >= cellGrid[0].length || cz >= cellGrid[0][0].length)
					{
						float[] center = new float[8];
						
						center[0] += p1.x;
						center[1] += p1.y;
						center[2] += p1.z;
						
						center[0] += p2.x;
						center[1] += p2.y;
						center[2] += p2.z;
						
						center[0] /= 2.0f;
						center[1] /= 2.0f;
						center[2] /= 2.0f;
						
						center[3] = 0;
						center[4] = -1;
						center[5] = 0;
						
						center[6] = 0;
						center[7] = 1;
						
						storageArray.add(center);
					}
					else
					{
						storageArray.add(cellGrid[cx][cy][cz].getFeaturePoint());
					}
				}
			}
			
			for (int[] edge : zEdges)
			{
				Point p1 = points[poffsets[edge[0]][0]][poffsets[edge[0]][1]][poffsets[edge[0]][2]];
				Point p2 = points[poffsets[edge[1]][0]][poffsets[edge[1]][1]][poffsets[edge[1]][2]];
				
				if (p1.scalar < 0 && p2.scalar < 0) continue;
				if (p1.scalar >= 0 && p2.scalar >= 0) continue;
				
				for (int[] rot : zrotation)
				{
					int cx = x + poffsets[edge[0]][0] + rot[0];
					int cy = y + poffsets[edge[0]][1] + rot[1];
					int cz = z + poffsets[edge[0]][2] + rot[2];
					
					if (cx < 0 || cy < 0 || cz < 0 || cx >= cellGrid.length || cy >= cellGrid[0].length || cz >= cellGrid[0][0].length)
					{
						float[] center = new float[8];
						
						center[0] += p1.x;
						center[1] += p1.y;
						center[2] += p1.z;
						
						center[0] += p2.x;
						center[1] += p2.y;
						center[2] += p2.z;
						
						center[0] /= 2.0f;
						center[1] /= 2.0f;
						center[2] /= 2.0f;
						
						center[3] = 0;
						center[4] = -1;
						center[5] = 0;
						
						center[6] = 0;
						center[7] = 1;
						
						storageArray.add(center);
					}
					else
					{
						storageArray.add(cellGrid[cx][cy][cz].getFeaturePoint());
					}
				}
			}
		}
		
		public float[] getFeaturePoint()
		{
			if (featurePoint != null) return featurePoint;
					
			float[] center = new float[8];
			for (int[] pindex : poffsets)
			{
				Point p = points[pindex[0]][pindex[1]][pindex[2]];
				center[0] += p.x;
				center[1] += p.y;
				center[2] += p.z;
			}
			center[0] /= 8.0f;
			center[1] /= 8.0f;
			center[2] /= 8.0f;
			
			center[3] = 0;
			center[4] = 1;
			center[5] = 0;
			
			center[6] = 0;
			center[7] = 1;
			
			featurePoint = center;
			
			return center;
		}
		
		public boolean hasIntersect()
		{
			boolean lessThan = false;
			boolean set = false;
			for (int[] pindex : poffsets)
			{
				Point p = points[pindex[0]][pindex[1]][pindex[2]];
				
				if (!set)
				{
					lessThan = p.scalar < 0;
					set = true;
				}
				else if (lessThan && p.scalar >= 0) return true;
				else if (!lessThan && p.scalar < 0) return true;
			}
			
			return false;
		}
	}
}
