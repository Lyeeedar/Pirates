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
		float size;
		float x;
		float y;
		float z;
		
		public NoiseGenerator(float offset, float frequency, float amplitude, int octaves, float scale, float size, float x, float y, float z)
		{
			this.offset = offset;
			this.frequency = frequency;
			this.amplitude = amplitude;
			this.octaves = octaves;
			this.scale = scale;
			this.size = size;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public float generate(float x, float y, float z, float[] normal)
		{
			return FastSimplexNoise.noise(x+offset, y, z+offset, frequency, amplitude, octaves, scale, true, normal);
		}
	}
	
	public static Mesh generateTerrain(int x, int y, int z, float scale)
	{
		float offset = MathUtils.random(8008135);
		
		NoiseGenerator noise = new NoiseGenerator(offset, 2, 0.5f, 8, 0.006f, scale, x, y, z);
		
		Point[][][] pointGrid = generatePointGrid(x, y, z, scale, noise, 3);
		smoothGrid(pointGrid); 
		
		for (int ix = 0; ix < x; ix++)
		{
			for (int iy = 0; iy < y; iy++)
			{
				for (int iz = 0; iz < z; iz++)
				{
					if (pointGrid[ix][iy][iz] == null) continue;
					
					pointGrid[ix][iy][iz].scalar -= ((float) iy / (float) y); 
					
					// Gradient 
					if (pointGrid[ix][iy][iz].scalar < 0) 
					{
						pointGrid[ix][iy][iz] = null;
						continue;
					}
				}
			}
		}
		
		Object[] data = generateFaces(pointGrid);
		Face[] faces = (Face[]) data[0];
		Vertex[] vertices = (Vertex[]) data[1];
		
		processFaces(faces, vertices, noise);
		
		return facesToMesh(faces, vertices);
	}
	
	public static void processFaces(Face[] faces, Vertex[] vertices, NoiseGenerator noise)
	{
		Vector3 normal = Pools.obtain(Vector3.class);
		
		for (Vertex v : vertices)
		{
			v.x = ((noise.x/-2)+v.x)*noise.size;
			v.y = ((noise.y/-2)+v.y)*noise.size;
			v.z = ((noise.z/-2)+v.z)*noise.size;
			
			v.tex1 = 1;
		}
		
		for (Face face : faces)
		{
			face.addNormal(normal);
		}
		Pools.free(normal);
		
		for (Vertex vertex : vertices)
		{
			vertex.norNormal();
			vertex.norTex();
		}
	}
	
	public static Point[][][] generatePointGrid(int x, int y, int z, float scale, NoiseGenerator noise, int step)
	{		
		Point[][][] pointGrid = new Point[x][y][z];
		boolean skip = false;
		
		for (int ix = 0; ix < x; ix++)
		{
			skip = !skip;
			for (int iy = 0; iy < y; iy++)
			{
				skip = !skip;
				int iz = skip ? step / 2 : 0 ;
				for (; iz < z; iz += step)
				{
					float[] normal = new float[3];
					float scalar = noise.generate(ix, iy, iz, normal);
					if (scalar >= 0) pointGrid[ix][iy][iz] = new Point(ix, iy, iz, scalar, normal);
					
				}
			}
		}
		
		return pointGrid;
	}
	
	public static void smoothGrid(Point[][][] pointGrid)
	{
		int x = pointGrid.length;
		int y = pointGrid[0].length;
		int z = pointGrid[0][0].length;
		Array<Point> neighbours = new Array<Point>(false, 26);
		
		for (int ix = 0; ix < x; ix++)
		{
			for (int iy = 0; iy < y; iy++)
			{
				for (int iz = 0; iz < z; iz++)
				{
					if (pointGrid[ix][iy][iz] == null)
					{
						neighbours.clear();
						// walk neighbours
						
						for (int nx = -1; nx < 2; nx++)
						{
							if (ix+nx < 0 || ix+nx >= x-1) continue;
							for (int ny = -1; ny < 2; ny++)
							{
								if (iy+ny < 0 || iy+ny >= y-1) continue;
								for (int nz = -1; nz < 2; nz++)
								{
									if (iz+nz < 0 || iz+nz >= z-1) continue;
									if (nx == 0 && ny == 0 && nz == 0) continue;
									
									if (pointGrid[ix+nx][iy+ny][iz+nz] != null) neighbours.add(pointGrid[ix+nx][iy+ny][iz+nz]);
								}
							}
						}
												
						if (neighbours.size >= 4)
						{
							Vector3 pos = Pools.obtain(Vector3.class).set(0, 0, 0);
							Vector3 nor = Pools.obtain(Vector3.class).set(0, 0, 0);
							float scalar = 0;
							
							for (Point p : neighbours)
							{
								pos.add(p.x, p.y, p.z);
								nor.add(p.normal[0], p.normal[1], p.normal[2]);
								scalar += p.scalar;
							}
							
							pos.x /= (float) neighbours.size;
							pos.y /= (float) neighbours.size;
							pos.z /= (float) neighbours.size;
							nor.nor();
							scalar /= (float) neighbours.size;
							
							pointGrid[ix][iy][iz] = new Point(pos.x, pos.y, pos.z, scalar, new float[]{nor.x, nor.y, nor.z});
							
							Pools.free(pos);
							Pools.free(nor);
						}
					}
				}
			}
		}
	}
	
	public static Object[] generateFaces(Point[][][] pointGrid)
	{
		int x = pointGrid.length;
		int y = pointGrid[0].length;
		int z = pointGrid[0][0].length;
		
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
						
						cellGrid[ix][iy][iz].setPoint(i, pointGrid[px][py][pz]);
						
					}
					
					if (!(ix == x-2 || iy == y-2 || iz == z-2) && cellGrid[ix][iy][iz].hasIntersect()) intersectList.add(cellGrid[ix][iy][iz]);
				}
			}
		}
		
		System.out.println("Intersecting cells: "+intersectList.size);
		
		Array<Face> faceArray = new Array<Face>(false, intersectList.size*6);
		Array<Vertex> vertexArray = new Array<Vertex>(false, intersectList.size);
		
		for (Cell cell : intersectList)
		{
			cell.buildFaces(faceArray, vertexArray, cellGrid);
		}
		
		Face[] faces = new Face[faceArray.size];
		for (int i = 0; i < faces.length; i++) faces[i] = faceArray.get(i);
		
		Vertex[] vertices = new Vertex[vertexArray.size];
		for (int i = 0; i < vertices.length; i++) vertices[i] = vertexArray.get(i);
		return new Object[]{faces, vertices};
	}
	
	public static Mesh facesToMesh(Face[] faces, Vertex[] vertices)
	{
		boolean useIndices = vertices.length < (int) Short.MAX_VALUE;
		int numVertices = useIndices ? vertices.length : faces.length * 6 ;
		int numIndices = useIndices ? faces.length * 6 : 0 ;
		
		float[] vertexArray = new float[numVertices * Vertex.VERTEX_SIZE];
		short[] indices = null;
		
		if (useIndices)
		{
			int i = 0;
			for (Vertex v : vertices)
			{
				vertexArray[i++] = v.x;
				vertexArray[i++] = v.y;
				vertexArray[i++] = v.z;
				
				vertexArray[i++] = v.nx;
				vertexArray[i++] = v.ny;
				vertexArray[i++] = v.nz;
				
				vertexArray[i++] = v.tex1;
				vertexArray[i++] = v.tex2;
				vertexArray[i++] = v.tex3;
			}
			
			indices = new short[numIndices];
			i = 0;
			for (Face face : faces)
			{
				for (int[] tri : face.tris)
				{
					for (int vi : tri)
					{
						Vertex v = face.vertices[vi];
						indices[i++] = v.index;
					}
				}
			}
		}
		else
		{
			int i = 0;
			for (Face face : faces)
			{
				for (int[] tri : face.tris)
				{
					for (int vi : tri)
					{
						Vertex v = face.vertices[vi];
						vertexArray[i++] = v.x;
						vertexArray[i++] = v.y;
						vertexArray[i++] = v.z;
						
						vertexArray[i++] = v.nx;
						vertexArray[i++] = v.ny;
						vertexArray[i++] = v.nz;
						
						vertexArray[i++] = v.tex1;
						vertexArray[i++] = v.tex2;
						vertexArray[i++] = v.tex3;
					}
				}
			}
		}
		
		System.out.println("Mesh made! Num Vertices: "+numVertices+" Num Indices: "+numIndices);
		
		Mesh mesh = new Mesh(true, numVertices, numIndices, 
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.Normal, 3, "a_normal"), 
				new VertexAttribute(Usage.Generic, 1, "a_texA0"),
				new VertexAttribute(Usage.Generic, 1, "a_texA1"),
				new VertexAttribute(Usage.Generic, 1, "a_texA2")
		);
		
		mesh.setVertices(vertexArray);
		if (useIndices) mesh.setIndices(indices);
		
		return mesh;
	}
	
	private static class Point
	{
		float x, y, z;
		float[] normal;
		float scalar;
		
		public Point(float x, float y, float z, float scalar, float[] normal)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.scalar = scalar;
			this.normal = normal;
		}
	}
	
	private static class Face
	{
		Vertex[] vertices = new Vertex[4];
		int[][] tris = new int[2][];
		
		public Face(Vertex v1, Vertex v2, Vertex v3, Vertex v4)
		{
			this.vertices[0] = v1;
			this.vertices[1] = v2;
			this.vertices[2] = v3;
			this.vertices[3] = v4;
		}
		
		public void setTri(int index, int v1, int v2, int v3)
		{
			tris[index] = new int[]{v1, v2, v3};
		}
		
		public void addNormal(Vector3 optionalOut)
		{					
			Vector3 U = Pools.obtain(Vector3.class).set(vertices[1].x-vertices[0].x, vertices[1].y-vertices[0].y, vertices[1].z-vertices[0].z);
			Vector3 V = Pools.obtain(Vector3.class).set(vertices[2].x-vertices[0].x, vertices[2].y-vertices[0].y, vertices[2].z-vertices[0].z);
			
			U.crs(V).nor();
			
			vertices[0].addNormal(U.x, U.y, U.z);
			vertices[1].addNormal(U.x, U.y, U.z);
			vertices[2].addNormal(U.x, U.y, U.z);
			
			if (optionalOut != null) optionalOut.set(U);
			
			Pools.free(U);
			Pools.free(V);
		}
		
		public void addVertices(Array<Vertex> vertexArray)
		{
			for (Vertex v : vertices)
			{
				if (v.index == -1)
				{
					v.index = (short) vertexArray.size;
					vertexArray.add(v);
				}
			}
		}
	}
	
	private static class Vertex
	{
		public static final int VERTEX_SIZE = 9;
		
		float x, y, z;
		float nx, ny, nz;
		float tex1;
		float tex2;
		float tex3;
		short index = -1;
		
		public Vertex(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void setNormal(float nx, float ny, float nz)
		{
			this.nx = nx;
			this.ny = ny;
			this.nz = nz;
		}
		
		public void addNormal(float nx, float ny, float nz)
		{
			this.nx += nx;
			this.ny += ny;
			this.nz += nz;
		}
		
		public void norNormal()
		{
			float len = Vector3.len(nx, ny, nz);
			nx /= len;
			ny /= len;
			nz /= len;
		}
		
		public void setTex(float tex1, float tex2, float tex3)
		{
			this.tex1 = tex1;
			this.tex2 = tex2;
			this.tex3 = tex3;
		}
		
		public void addTex(float tex1, float tex2, float tex3)
		{
			this.tex1 += tex1;
			this.tex2 += tex2;
			this.tex3 += tex3;
		}
		
		public void norTex()
		{
			float len = Vector3.len(tex1, tex2, tex3);
			tex1 /= len;
			tex2 /= len;
			tex3 /= len;
			
			tex1 = 1;
			tex2 = 0;
			tex3 = 0;
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
		
		Vertex featurePoint = null;
		
		public Point getPoint(int index)
		{
			return points[indices[index][0]][indices[index][1]][indices[index][2]];
		}
		
		public void setPoint(int index, Point p)
		{
			points[indices[index][0]][indices[index][1]][indices[index][2]] = p;
		}
		
		public void buildFaces(Array<Face> faceArray, Array<Vertex> vertexArray, Cell[][][] cellGrid)
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

                    flip_if_nonzero = getPoint(6) == null;
                } 
				else if (e == 1 && (bitmask & (1 << 6)) != 0) 
				{
					cells[1] = cellGrid[x][y][z+1];
					cells[2] = cellGrid[x][y+1][z+1];
					cells[3] = cellGrid[x][y+1][z];
					
                    flip_if_nonzero = getPoint(7) == null;
                } 
				else if (e == 2 && (bitmask & (1 << 5)) != 0) 
				{
					cells[1] = cellGrid[x+1][y][z];
					cells[2] = cellGrid[x+1][y][z+1];
					cells[3] = cellGrid[x][y][z+1];
					
                    flip_if_nonzero = getPoint(5) == null;
                } 
				else
                    continue;
				
				Face face = new Face(cells[0].getFeaturePoint(), cells[1].getFeaturePoint(), cells[2].getFeaturePoint(), cells[3].getFeaturePoint());
				face.addVertices(vertexArray);
				faceArray.add(face);
				
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
                    
                    face.setTri(j, 0, ja, jb);
				}
			}
		}
		
		public Vertex getFeaturePoint()
		{					
			if (featurePoint == null)
			{
				Array<Vector3> intersects = new Array<Vector3>(false, 16);
				Array<Vector3> normals = new Array<Vector3>(false, 16);
				float[] nor = new float[3];
				float[] texa = new float[3];
				
				for (int i = 0; i < edges.length; i++)
				{
					if ((bitmask & (1 << i)) == 0) continue;
					
					int[] edge = edges[i];
					int[] a1 = indices[edge[0]];
					int[] a2 = indices[edge[1]];
					int[] axis = new int[3];
					Point p = null;
					if (getPoint(edge[0]) == null)
					{
						p = getPoint(edge[1]);
						axis[0] = a1[0] - a2[0];
						axis[1] = a1[1] - a2[1];
						axis[2] = a1[2] - a2[2];
					}
					else
					{
						p = getPoint(edge[0]);
						axis[0] = a2[0] - a1[0];
						axis[1] = a2[1] - a1[1];
						axis[2] = a2[2] - a1[2];
					}
					
					Vector3 intersect = Pools.obtain(Vector3.class).set(p.x, p.y, p.z);
					intersect.add((float) axis[0] * (1.0f-p.scalar), (float) axis[1] * (1.0f-p.scalar), (float) axis[2] * (1.0f-p.scalar));
					//System.out.println("Intersect: "+intersect);
					
					Vector3 normal = Pools.obtain(Vector3.class).set(p.normal[0], p.normal[1], p.normal[2]);
					
					nor[0] += normal.x;
					nor[1] += normal.y;
					nor[2] += normal.z;
					
					normal.nor();
					
					//System.out.println("Normal: "+normal);
					
					intersects.add(intersect);
					normals.add(normal);
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
				
				featurePoint = new Vertex(fp.x, fp.y, fp.z);
				featurePoint.setTex(texa[0], texa[1], texa[2]);
				//, nor[0], nor[1], nor[2], texa[0], texa[1], texa[2]};
				
				for (Vector3 v : intersects) Pools.free(v);
				for (Vector3 v : normals) Pools.free(v);
				Pools.free(fp);
			}
						
			return featurePoint;
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
				
				if ((p1 == null) == (p2 == null)) continue;
				
				intersects = true;
				bitmask = bitmask | (1 << i);
			}
			
			return intersects;
		}
	}
}
