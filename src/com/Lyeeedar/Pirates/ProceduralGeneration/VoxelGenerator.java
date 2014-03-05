package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayList;
import java.util.Iterator;

import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.FastSimplexNoise;
import com.Lyeeedar.Util.Pools;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class VoxelGenerator
{
	private static final class NoiseGenerator
	{
		float offset;
		float frequency;
		float amplitude;
		int octaves;
		float scale;
		
		public NoiseGenerator(float offset, float frequency, float amplitude, int octaves, float scale)
		{
			this.offset = offset;
			this.frequency = frequency;
			this.amplitude = amplitude;
			this.octaves = octaves;
			this.scale = scale;
		}
		
		public float generate(float x, float y, float z, float[] normal)
		{
			return FastSimplexNoise.noise(x+offset, y, z+offset, frequency, amplitude, octaves, scale, true, normal);
		}
	}
	
	public static Mesh generateTerrain(int x, int y, int z, float scale)
	{
		float offset = MathUtils.random(8008135);
		
		NoiseGenerator noise = new NoiseGenerator(offset, 2, 0.5f, 8, 0.006f);
		
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
						int px = ix+Cell.indices[i][0];
						int py = iy+Cell.indices[i][1];
						int pz = iz+Cell.indices[i][2];
						
						if (pointGrid[px][py][pz] == null)
						{
							float[] normal = new float[3];
							pointGrid[px][py][pz] = new Point((x/-2+px)*scale, (y/-2+py)*scale, (z/-2+pz)*scale, px, py, pz, noise.generate(px, py, pz, normal), normal);
							if (pointGrid[px][py][pz].y < 10) pointGrid[px][py][pz].texnum = 1;
						}
						
						cellGrid[ix][iy][iz].setPoint(i, pointGrid[px][py][pz]);
						
					}
					
					if (!(ix == x-2 || iy == y-2 || iz == z-2) && cellGrid[ix][iy][iz].hasIntersect()) intersectList.add(cellGrid[ix][iy][iz]);
				}
			}
		}
		
		System.out.println("Intersecting cells: "+intersectList.size);
		
		Array<float[]> storageArray = new Array<float[]>(false, intersectList.size*12*6);
		
		for (Cell cell : intersectList)
		{
			cell.buildQuad(storageArray, cellGrid, noise);
		}
		
		float[] vertices = new float[storageArray.size * storageArray.get(0).length];
		
		int i = 0;
		for (float[] vertex : storageArray)
		{
			System.arraycopy(vertex, 0, vertices, i, vertex.length);
			i += vertex.length;
		}
		System.out.println("Collated Vertices! Num: "+storageArray.size);
		
		Mesh mesh = new Mesh(true, storageArray.size, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.Normal, 3, "a_normal"), 
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"), 
				new VertexAttribute(Usage.Generic, 1, "a_texA0"),
				new VertexAttribute(Usage.Generic, 1, "a_texA1"),
				new VertexAttribute(Usage.Generic, 1, "a_texA2")
		);
		mesh.setVertices(vertices);
		return mesh;
	}
	
	private static class Point
	{
		float x, y, z;
		float tx, ty, tz;
		float[] normal;
		float scalar;
		int texnum = 0;
		
		public Point(float x, float y, float z, float tx, float ty, float tz, float scalar, float[] normal)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.tx = tx;
			this.ty = ty;
			this.tz = tz;
			this.scalar = scalar;
			this.normal = normal;
		}
	}

	/**
	 * Reference http://www.sandboxie.com/misc/isosurf/isosurfaces.html
	 * @author Philip
	 *
	 */
	private static class Cell
	{
		int bitmask = 0;
		
		// Point offsets in the grid
		public static final int[][] indices = { 
			{0, 0, 0}, // 0
			{1, 0, 0}, // 1
			{1, 1, 0}, // 2
			{0, 1, 0}, // 3
			{0, 0, 1}, // 4
			{1, 0, 1}, // 5
			{1, 1, 1}, // 6
			{0, 1, 1}  // 7
			};

		public static final float[][][] texcoords = { 
			{ {0, 0}, {1, 0}, {1, 1}, {0, 1} },
			{ {0, 0}, {1, 0}, {1, 1}, {0, 1} },
			{ {0, 0}, {1, 0}, {1, 1}, {0, 1} }
			};
		
		// The edges of the cube
		public static final int[][] edges = { 
			{0, 1}, // 0
			{1, 2}, // 1
			{2, 3}, // 2
			{3, 0}, // 3
			{4, 5}, // 4
			{5, 6}, // 5
			{6, 7}, // 6
			{7, 4}, // 7
			{0, 4}, // 8
			{1, 5}, // 9
			{2, 6}, // 10
			{3, 7}, // 11
		};
		
		int x, y, z;
		
		public Cell(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		Point[][][] points = new Point[2][2][2];
		
		float[] featurePoint = null;
		
		public Point getPoint(int index)
		{
			return points[indices[index][0]][indices[index][1]][indices[index][2]];
		}
		
		public void setPoint(int index, Point p)
		{
			points[indices[index][0]][indices[index][1]][indices[index][2]] = p;
		}
		
		public void buildQuad(Array<float[]> storageArray, Cell[][][] cellGrid, NoiseGenerator noise)
		{			
			Cell[] cells = new Cell[4];
			cells[0] = this;
			boolean flip_if_nonzero = false;
			
			for (int e = 0; e < 3; e++)
			{				
				if (e == 0 && (bitmask & (1 << 10)) != 0) 
				{
					cells[1] = cellGrid[x+1][y][z];
					cells[2] = cellGrid[x+1][y+1][z];
					cells[3] = cellGrid[x][y+1][z];

                    flip_if_nonzero = getPoint(6).scalar < 0;
                } 
				else if (e == 1 && (bitmask & (1 << 6)) != 0) 
				{
					cells[1] = cellGrid[x][y][z+1];
					cells[2] = cellGrid[x][y+1][z+1];
					cells[3] = cellGrid[x][y+1][z];
					
                    flip_if_nonzero = getPoint(7).scalar < 0;
                } 
				else if (e == 2 && (bitmask & (1 << 5)) != 0) 
				{
					cells[1] = cellGrid[x+1][y][z];
					cells[2] = cellGrid[x+1][y][z+1];
					cells[3] = cellGrid[x][y][z+1];
					
                    flip_if_nonzero = getPoint(5).scalar < 0;
                } 
				else
                    continue;
				
				for (int j = 0; j < 2; ++j) 
				{
                    int ja, jb;
                    if (flip_if_nonzero) 
                    {
                        ja = j + 1;
                        jb = j + 2;
                    } 
                    else 
                    {
                        ja = j + 2;
                        jb = j + 1;
                    }

                    float[] p0 = cells[0].getFeaturePoint(texcoords[e][0]);
                    float[] p1 = cells[ja].getFeaturePoint(texcoords[e][ja]);
                    float[] p2 = cells[jb].getFeaturePoint(texcoords[e][jb]);
                    
                    storageArray.add(p0);
                    storageArray.add(p1);
                    storageArray.add(p2);
				}
			}
		}
		
		public float[] getFeaturePoint(float[] texcoords)
		{					
			if (featurePoint == null)
			{
				Array<Vector3> intersects = new Array<Vector3>(false, 16);
				Array<Vector3> normals = new Array<Vector3>(false, 16);
				float[] nor = new float[3];
				float[] texa = new float[3];
				
				for (int i = 0; i < edges.length; i++)
				{
					int[] edge = edges[i];
					Point p1 = getPoint(edge[0]);
					Point p2 = getPoint(edge[1]);
						
					if ((bitmask & (1 << i)) == 0) continue;
					
					float max = Math.abs(p1.scalar) + Math.abs(p2.scalar);
					float p1s = 1.0f - Math.abs(p1.scalar) / max;
					float p2s = 1.0f - Math.abs(p2.scalar) / max;
					
					Vector3 intersect = Pools.obtain(Vector3.class).set(p1.x * p1s, p1.y * p1s, p1.z * p1s);
					intersect.add(p2.x * p2s, p2.y * p2s, p2.z * p2s);
					//System.out.println("Intersect: "+intersect);
					
					Vector3 normal = Pools.obtain(Vector3.class).set(p1.normal[0] * p1s, p1.normal[1] * p1s, p1.normal[2] * p1s);
					normal.add(p2.normal[0] * p2s, p2.normal[1] * p2s, p2.normal[2] * p2s);
					
					nor[0] += normal.x;
					nor[1] += normal.y;
					nor[2] += normal.z;
					
					normal.nor();
					
					//System.out.println("Normal: "+normal);
					
					intersects.add(intersect);
					normals.add(normal);
					
					texa[0] += p1.texnum == 0 ? 1.0f * p1s : 0.0f ;
					texa[0] += p2.texnum == 0 ? 1.0f * p2s : 0.0f ;
					
					texa[1] += p1.texnum == 1 ? 1.0f * p1s : 0.0f ;
					texa[1] += p2.texnum == 1 ? 1.0f * p2s : 0.0f ;
					
					texa[2] += p1.texnum == 2 ? 1.0f * p1s : 0.0f ;
					texa[2] += p2.texnum == 2 ? 1.0f * p2s : 0.0f ;	

				}
				Vector3 fp = Pools.obtain(Vector3.class).set(0, 0, 0);
				computeFeaturePoint(intersects, normals, 0.001f, fp);
				//System.out.println("Feature Point: "+fp+"\n");
				
				float len = Vector3.len(nor[0], nor[1], nor[2]);
				nor[0] /= len;
				nor[1] /= len;
				nor[2] /= len;
				
				len = Vector3.len(texa[0], texa[1], texa[2]);
				texa[0] /= len;
				texa[1] /= len;
				texa[2] /= len;
				
				featurePoint = new float[]{fp.x, fp.y, fp.z, nor[0], nor[1], nor[2], texa[0], texa[1], texa[2]};
				
				for (Vector3 v : intersects) Pools.free(v);
				for (Vector3 v : normals) Pools.free(v);
				Pools.free(fp);
			}
			
			float[] center = new float[11];
			
			center[0] = featurePoint[0]; // position
			center[1] = featurePoint[1];
			center[2] = featurePoint[2];
			
			center[3] = featurePoint[3] * -1; // normal
			center[4] = featurePoint[4] * -1;
			center[5] = featurePoint[5] * -1;
			
			center[6] = texcoords[0];
			center[7] = texcoords[1];

			center[8] = featurePoint[6];
			center[9] = featurePoint[7];
			center[10] = featurePoint[8];
						
			return center;
		}
		
	    public static Vector3 computeFeaturePoint(Array<Vector3> intersectionPoints, Array<Vector3> intersectionNormals, float threshold, Vector3 out)
	    {
	        threshold *= threshold;

	        // Center the particle on the masspoint.
	        Vector3 tmp = Pools.obtain(Vector3.class).set(0, 0, 0);
	        for (Vector3 v : intersectionPoints)
	        {
	           tmp.add(v);
	        }
	        tmp.scl(1.0f / (float)intersectionPoints.size);
	        Vector3 particlePosition = out.set(tmp);

	        // Start iterating:
	        Vector3 force = Pools.obtain(Vector3.class).set(0, 0, 0);
	        int iteration;
	        for (iteration = 0; iteration < 20; iteration++)
	        {
	            force.set(0, 0, 0);

	            // For each intersection point:
	            for (int i = 0; i < intersectionPoints.size; i++)
	            {
	                Vector3 planePoint = intersectionPoints.get(i);
	                Vector3 planeNormal = intersectionNormals.get(i);

	                // Compute distance vector to plane.
	                // To do that, compute the normal.dot(AX).
	                float d = planeNormal.dot(tmp.set(particlePosition).sub(planePoint));

	                force.add(tmp.set(planeNormal).scl(-d));
	            }

	            // Average the force over all the intersection points, and multiply
	            // with a ratio and some damping to avoid instabilities.
	            float damping = 1f - ((float) iteration) / 20.0f;

	            force.scl(0.75f * damping / (float) intersectionPoints.size);

	            // Apply the force.
	            particlePosition.add(force);

	            // If the force was almost null, break.
	            if (force.isZero(threshold))
	            {
	                break;
	            }
	        }
	        
	        Pools.free(tmp);
	        Pools.free(force);

	        return particlePosition;
	    }

		
		public boolean hasIntersect()
		{
			boolean intersects = false;

			for (int i = 0; i < edges.length; i++)
			{
				int[] edge = edges[i];
				Point p1 = getPoint(edge[0]);
				Point p2 = getPoint(edge[1]);
				
				if (p1.scalar < 0 && p2.scalar < 0) continue;
				if (p1.scalar >= 0 && p2.scalar >= 0) continue;
				
				intersects = true;
				bitmask = bitmask | (1 << i);
			}
			
			return intersects;
		}
	}
}
