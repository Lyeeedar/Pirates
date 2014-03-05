package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayList;
import java.util.Iterator;

import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.FastSimplexNoise;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
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
						int px = ix+Cell.poffsets[i][0];
						int py = iy+Cell.poffsets[i][1];
						int pz = iz+Cell.poffsets[i][2];
						
						if (pointGrid[px][py][pz] == null)
						{
							float[] normal = new float[3];
							pointGrid[px][py][pz] = new Point((x/-2+px)*scale, (y/-2+py)*scale, (z/-2+pz)*scale, px, py, pz, noise.generate(px, py, pz, normal), normal);
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
		
		Mesh mesh = new Mesh(true, storageArray.size, 0, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal, 3, "a_normal"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		mesh.setVertices(vertices);
		return mesh;
	}
	
	private static class Point
	{
		// EDGE order = UP LEFT IN OUT RIGHT DOWN
		
		float x, y, z;
		float tx, ty, tz;
		float[] normal;
		float scalar;
		
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

	private static class Cell
	{
		// Point offsets in the grid
		public static final int[][] poffsets = { {0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 1}, {0, 1, 1}, {1, 1, 1}, {1, 0, 1} };
		
		// The order to rotate around each edge for making quads
		public static final int[][][][] tris = {
			{ { {0, 0, -1}, {0, -1, 0}, {0, 0, 0} }, { {0, 0, -1}, {0, -1, -1}, {0, -1, 0} } },
			{ { {0, 0, 0}, {-1, 0, -1}, {-1, 0, 0} }, { {0, 0, 0}, {0, 0, -1}, {-1, 0, -1} } },
			{ { {-1, 0, 0}, {0, -1, 0}, {0, 0, 0} }, { {-1, 0, 0}, {-1, -1, 0}, {0, -1, 0} } }
		};
		public static final float[][] texcoords = { {0, 0}, {0, 1}, {1, 1}, {0, 0}, {1, 1}, {1, 0} };
		
		// The edges of the cube
		public static final int[][][] edges = { 
			{ {0, 3}, {1, 2}, {4, 7}, {5, 6} } ,
			{ {0, 1}, {3, 2}, {4, 5}, {7, 6} } ,
			{ {0, 4}, {1, 5}, {2, 6}, {3, 7} }
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
				
		public void buildQuad(Array<float[]> storageArray, Cell[][][] cellGrid, NoiseGenerator noise)
		{
			for (int axis = 0; axis < 3; axis++)
			{
				for (int[] edge : edges[axis])
				{
					Point p1 = points[poffsets[edge[0]][0]][poffsets[edge[0]][1]][poffsets[edge[0]][2]];
					Point p2 = points[poffsets[edge[1]][0]][poffsets[edge[1]][1]][poffsets[edge[1]][2]];
					
					if (p1.scalar < 0 && p2.scalar < 0) continue;
					if (p1.scalar >= 0 && p2.scalar >= 0) continue;
					
					for (int tri = 0; tri < 2; tri++)
					{
						float[][] tfp = new float[3][];
						for (int i = 0; i < 3; i++)
						{
							int[] rot = tris[axis][tri][i];
							int cx = x + poffsets[edge[0]][0] + rot[0];
							int cy = y + poffsets[edge[0]][1] + rot[1];
							int cz = z + poffsets[edge[0]][2] + rot[2];
							
							float[] fp = null;
							
							if (cx < 0 || cy < 0 || cz < 0 || cx >= cellGrid.length || cy >= cellGrid[0].length || cz >= cellGrid[0][0].length)
							{
								fp = getFeaturePoint(p1, p2, noise);
							}
							else
							{
								fp = cellGrid[cx][cy][cz].getFeaturePoint(noise, texcoords[i]);
							}
							
							tfp[i] = fp;
						}
						
//						float Ux = tfp[1][0] - tfp[0][0];
//						float Uy = tfp[1][1] - tfp[0][1];
//						float Uz = tfp[1][2] - tfp[0][2];
//						
//						float Vx = tfp[2][0] - tfp[0][0];
//						float Vy = tfp[2][1] - tfp[0][1];
//						float Vz = tfp[2][2] - tfp[0][2];
//						
//						float Nx = Uy*Vz - Uz*Vy;
//						float Ny = Uz*Vx - Ux*Vz;
//						float Nz = Ux*Vy - Uy*Vx;
//						
//						float len = Vector3.len(Nx, Ny, Nz);
//						
//						Nx /= len;
//						Ny /= len;
//						Nz /= len;
						
						for (int i = 0; i < 3; i++)
						{
//							tfp[i][3] = Nx;
//							tfp[i][4] = Ny;
//							tfp[i][5] = Nz;
							
							storageArray.add(tfp[i]);
						}
					}
				}
			}
		}
		
		public float[] getFeaturePoint(Point p1, Point p2, NoiseGenerator noise)
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
			
			float[] normal = new float[3];
			noise.generate((p1.tx+p2.tx)/2.0f, (p1.ty+p2.ty)/2.0f, (p1.tz+p2.tz)/2.0f, normal);
			
			center[3] = normal[0];
			center[4] = normal[1];
			center[5] = normal[2];
			
			center[6] = 0;
			center[7] = 1;
			
			return center;
		}
		
		public float[] getFeaturePoint(NoiseGenerator noise, float[] texcoords)
		{					
			float[] center = new float[8];

			if (featurePoint == null)
			{
			
				Array<Vector3> intersects = new Array<Vector3>(false, 16);
				Array<Vector3> normals = new Array<Vector3>(false, 16);
				
				for (int[][] dir : edges)
				{
					for (int[] edge : dir)
					{
						Point p1 = points 
								[poffsets[ edge[0] ] [0] ] 
								[poffsets[ edge[0] ] [1] ] 
								[poffsets[ edge[0] ] [2] ] ;
						
						Point p2 = points 
								[poffsets[ edge[1] ] [0] ] 
								[poffsets[ edge[1] ] [1] ] 
								[poffsets[ edge[1] ] [2] ] ;
						
						if (p1.scalar < 0 && p2.scalar < 0) continue;
						if (p1.scalar >= 0 && p2.scalar >= 0) continue;
						
						float max = Math.abs(p1.scalar) + Math.abs(p2.scalar);
						float p1s = 1.0f - Math.abs(p1.scalar) / max;
						float p2s = 1.0f - Math.abs(p2.scalar) / max;
						
						Vector3 intersect = new Vector3(p1.x * p1s, p1.y * p1s, p1.z * p1s);
						intersect.add(p2.x * p2s, p2.y * p2s, p2.z * p2s);
						//System.out.println("Intersect: "+intersect);
						
						Vector3 normal = new Vector3(p1.normal[0] * p1s, p1.normal[1] * p1s, p1.normal[2] * p1s);
						normal.add(p2.normal[0] * p2s, p2.normal[1] * p2s, p2.normal[2] * p2s).nor();
						//System.out.println("Normal: "+normal);
						
						intersects.add(intersect);
						normals.add(normal);
					}
				}
				Vector3 fp = vertexFromParticle(intersects, normals, 0.001f);
				//System.out.println("Feature Point: "+fp+"\n");
				featurePoint = new float[]{fp.x, fp.y, fp.z};
			}
			
			center[0] = featurePoint[0];
			center[1] = featurePoint[1];
			center[2] = featurePoint[2];
			
			float[] normal = new float[3];
			for (int[] poff : poffsets)
			{
				normal[0] += points[poff[0]][poff[1]][poff[2]].normal[0];
				normal[1] += points[poff[0]][poff[1]][poff[2]].normal[1];
				normal[2] += points[poff[0]][poff[1]][poff[2]].normal[2];
			}
			float len = Vector3.len(normal[0], normal[1], normal[2]);
			normal[0] /= len;
			normal[1] /= len;
			normal[2] /= len;
			
			center[3] = normal[0];
			center[4] = normal[1];
			center[5] = normal[2];
			
			center[6] = texcoords[0];
			center[7] = texcoords[1];
						
			return center;
		}
		
		/**
	     * Computes the vertex for the cube, from Hermite data. Uses Leonardo
	     * Augusto Schmitz's excellent method, with exact normal at intersection
	     * points, to reduce complexity.
	     *
	     * @param intersectionPoints : An arraylist containing the positions of
	     * intersections with the isosurface.
	     * @param intersectionNormals : An arraylist containing the normal to the
	     * surface at each of these points.
	     * @return the approximated vertex for this cube.
	     */
	    public static Vector3 vertexFromParticle(Array<Vector3> intersectionPoints, Array<Vector3> intersectionNormals, float threshold)
	    {
	        threshold *= threshold;

	        // Center the particle on the masspoint.
	        Vector3 tmp = new Vector3();
	        for (Vector3 v : intersectionPoints)
	        {
	           tmp.add(v);
	        }
	        tmp.scl(1.0f / (float)intersectionPoints.size);
	        Vector3 particlePosition = new Vector3(tmp);

	        // Start iterating:
	        Vector3 force = new Vector3();
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

	        return particlePosition;
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
