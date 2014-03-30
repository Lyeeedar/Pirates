package com.Lyeeedar.Util;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Shapes {
	
	private static short[] linkDisks(int s1, int s2, int phi)
	{
		short[] indices = new short[phi*6];
		int i = 0;
		for (short s = 0; s < phi; s++)
		{
			indices[i++] = (short) (s1+s+0);
			indices[i++] = (short) (s2+s+0);
			indices[i++] = (short) (s2+((s+1) % phi));
			
			indices[i++] = (short) (s1+s+0);
			indices[i++] = (short) (s2+((s+1) % phi));
			indices[i++] = (short) (s1+((s+1) % phi));
		}
		
		return indices;
	}
	
	public static Mesh getCylinderMesh(int phi, boolean hollow, float innerScale, boolean normals, boolean texCoords)
	{
		int vertSize = 3;
		if (normals) vertSize += 3;
		if (texCoords) vertSize += 2;
		
		int numVertices = hollow ? phi*8 : phi*6 ;
		int numIndices = hollow ? phi*6*4 : phi*6*3 ;
		
		double d_theta = Math.PI / 3.0;
		double d_phi = (2.0*Math.PI) / (double)phi;

		float[][] disk = new float[phi][3];

		double c_phi = 0;

		for (int p = 0; p < phi; p++)
		{
			c_phi += d_phi;
			float x = (float) (Math.sin(d_theta) * Math.cos(c_phi));
			float y = 0;
			float z = (float) (Math.sin(d_theta) * Math.sin(c_phi));

			disk[p][0] = x;
			disk[p][1] = y;
			disk[p][2] = z;
		}
		
		float[] vertices = new float[numVertices*vertSize];
		
		int i = 0;
		
		// TopCap
		
		// Top outer
		for (float[] d : disk)
		{
			vertices[i++] = d[0];
			vertices[i++] = 1;
			vertices[i++] = d[2];
			
			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
			
			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}

		// Top Inner
		for (float[] d : disk)
		{
			vertices[i++] = d[0]*innerScale;
			vertices[i++] = 1;
			vertices[i++] = d[2]*innerScale;

			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}

			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}
		
		// End TopCap
		
		// OuterSurface
		
		// Top outer
		for (float[] d : disk)
		{
			vertices[i++] = d[0];
			vertices[i++] = 1;
			vertices[i++] = d[2];

			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}

			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}

		// Bot outer
		for (float[] d : disk)
		{
			vertices[i++] = d[0];
			vertices[i++] = -1;
			vertices[i++] = d[2];

			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}

			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}
		
		// End OuterSurface
		
		// BotCap
		
		// Bot outer
		for (float[] d : disk)
		{
			vertices[i++] = d[0];
			vertices[i++] = -1;
			vertices[i++] = d[2];

			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}

			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}

		// Bot Inner
		for (float[] d : disk)
		{
			vertices[i++] = d[0]*innerScale;
			vertices[i++] = -1;
			vertices[i++] = d[2]*innerScale;

			if (normals)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
				vertices[i++] = 0;
			}

			if (texCoords)
			{
				vertices[i++] = 0;
				vertices[i++] = 0;
			}
		}
		
		// End BotCap
		
		// InnerSurface
		
		if (hollow)
		{
			// Top Inner
			for (float[] d : disk)
			{
				vertices[i++] = d[0]*innerScale;
				vertices[i++] = 1;
				vertices[i++] = d[2]*innerScale;

				if (normals)
				{
					vertices[i++] = 0;
					vertices[i++] = 0;
					vertices[i++] = 0;
				}

				if (texCoords)
				{
					vertices[i++] = 0;
					vertices[i++] = 0;
				}
			}
			
			// Bot Inner
			for (float[] d : disk)
			{
				vertices[i++] = d[0]*innerScale;
				vertices[i++] = -1;
				vertices[i++] = d[2]*innerScale;

				if (normals)
				{
					vertices[i++] = 0;
					vertices[i++] = 0;
					vertices[i++] = 0;
				}

				if (texCoords)
				{
					vertices[i++] = 0;
					vertices[i++] = 0;
				}
			}
		}
		
		// End InnerSurface
		
		short[] indices = new short[numIndices];
		i = 0;
		// Top
		System.arraycopy(linkDisks(0, phi, phi), 0, indices, i, phi*6);
		i += phi*6;
		// Outer
		System.arraycopy(linkDisks(phi*3, phi*2, phi), 0, indices, i, phi*6);
		i += phi*6;
		// Bot
		System.arraycopy(linkDisks(phi*5, phi*4, phi), 0, indices, i, phi*6);
		i += phi*6;
		// Inner
		if (hollow)
		{
			System.arraycopy(linkDisks(phi*6, phi*7, phi), 0, indices, i, phi*6);
			i += phi*6;
		}
		
		if (normals)
		{	
			Vector3 U = Pools.obtain(Vector3.class);
			Vector3 V = Pools.obtain(Vector3.class);
			Vector3 N = Pools.obtain(Vector3.class);
			
			for (int tri = 0; tri < numIndices/3; tri++)
			{
				short i1 = indices[tri*3+0];
				short i2 = indices[tri*3+1];
				short i3 = indices[tri*3+2];
				
				float v1x = vertices[i1*vertSize+0];
				float v1y = vertices[i1*vertSize+1];
				float v1z = vertices[i1*vertSize+2];
				
				float v2x = vertices[i2*vertSize+0];
				float v2y = vertices[i2*vertSize+1];
				float v2z = vertices[i2*vertSize+2];
				
				float v3x = vertices[i3*vertSize+0];
				float v3y = vertices[i3*vertSize+1];
				float v3z = vertices[i3*vertSize+2];
				
				U.set(v2x, v2y, v2z).sub(v1x, v1y, v1z);
				V.set(v3x, v3y, v3z).sub(v1x, v1y, v1z);
				
				N.x = U.y*V.z - U.z*V.y;
				N.y = U.z*V.x - U.x*V.z;
				N.z = U.x*V.y - U.y*V.x;
				
				float n1x = vertices[i1*vertSize+3];
				float n1y = vertices[i1*vertSize+4];
				float n1z = vertices[i1*vertSize+5];
				
				float n2x = vertices[i2*vertSize+3];
				float n2y = vertices[i2*vertSize+4];
				float n2z = vertices[i2*vertSize+5];
				
				float n3x = vertices[i3*vertSize+3];
				float n3y = vertices[i3*vertSize+4];
				float n3z = vertices[i3*vertSize+5];
				
				if (n1x == 0 && n1y == 0 && n1z == 0)
				{
					vertices[i1*vertSize+3] = N.x;
					vertices[i1*vertSize+4] = N.y;
					vertices[i1*vertSize+5] = N.z;
				}
				else
				{
					vertices[i1*vertSize+3] += N.x;
					vertices[i1*vertSize+4] += N.y;
					vertices[i1*vertSize+5] += N.z;
					
					vertices[i1*vertSize+3] /= 2.0f;
					vertices[i1*vertSize+4] /= 2.0f;
					vertices[i1*vertSize+5] /= 2.0f;
				}
				
				if (n2x == 0 && n2y == 0 && n2z == 0)
				{
					vertices[i2*vertSize+3] = N.x;
					vertices[i2*vertSize+4] = N.y;
					vertices[i2*vertSize+5] = N.z;
				}
				else
				{
					vertices[i2*vertSize+3] += N.x;
					vertices[i2*vertSize+4] += N.y;
					vertices[i2*vertSize+5] += N.z;
					
					vertices[i2*vertSize+3] /= 2.0f;
					vertices[i2*vertSize+4] /= 2.0f;
					vertices[i2*vertSize+5] /= 2.0f;
				}
				
				if (n3x == 0 && n3y == 0 && n3z == 0)
				{
					vertices[i3*vertSize+3] = N.x;
					vertices[i3*vertSize+4] = N.y;
					vertices[i3*vertSize+5] = N.z;
				}
				else
				{
					vertices[i3*vertSize+3] += N.x;
					vertices[i3*vertSize+4] += N.y;
					vertices[i3*vertSize+5] += N.z;
					
					vertices[i3*vertSize+3] /= 2.0f;
					vertices[i3*vertSize+4] /= 2.0f;
					vertices[i3*vertSize+5] /= 2.0f;
				}
			}
			
			for (int v = 0; v < numVertices; v++)
			{
				N.set(
					vertices[v*vertSize+3],
					vertices[v*vertSize+4],
					vertices[v*vertSize+5]
				).nor();
				
				vertices[v*vertSize+3] = N.x;
				vertices[v*vertSize+4] = N.y;
				vertices[v*vertSize+5] = N.z;
			}
			
			Pools.free(U);
			Pools.free(V);
			Pools.free(N);
		}
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (normals) vasa.add(new VertexAttribute(Usage.Normal, 3, "a_normal"));
		if (texCoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh mesh = new Mesh(true, numVertices, numIndices, vas);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}

	public static Mesh getHemiSphereMesh(int theta, int phi, float scale, boolean normals, boolean texCoords)
	{
		int vertexSize = 3;
		if (normals) vertexSize += 3;
		if (texCoords) vertexSize += 2;
		
		// Calculate the delta step
		double d_theta = Math.PI / ((theta*2)+2);
		double d_phi = (2*Math.PI) / phi;

		// Setup vertices buffer
		int num_vertices = 1 + ((theta)*phi);
		float[] vertices = new float[num_vertices*vertexSize];

		int i = 0;

		// Insert the top vertex
		vertices[i++] = 0.0f;
		vertices[i++] = 0.0f;
		vertices[i++] = 1.0f*scale;

		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 1.0f;
		
		if (texCoords)
		{
			vertices[i++] = 0;
			vertices[i++] = 0;
		}

		double c_theta = 0;
		double c_phi = 0;

		// Insert the vertex at each division specified by theta and phi
		for (int reps = 0; reps < theta; reps++)
		{
			c_theta += d_theta;
			c_phi = 0;
			for (int p = 0; p < phi; p++)
			{
				c_phi += d_phi;
				float x = (float) (Math.sin(c_theta) * Math.cos(c_phi));
				float y = (float) (Math.sin(c_theta) * Math.sin(c_phi));
				float z = (float) Math.cos(c_theta);

				vertices[i++] = x*scale;
				vertices[i++] = y*scale;
				vertices[i++] = z*scale;

				if (normals) vertices[i++] = x;
				if (normals) vertices[i++] = y;
				if (normals) vertices[i++] = z;
				
				if (texCoords)
				{
					float r = (float) Math.sqrt(1.0f - z * z);
					if (r < 0.001f)
					{
						vertices[i++] = 0;
						vertices[i++] = 0;
					}
					else
					{
						float angle;
						float refAngle = (float) Math.acos(Math.abs(x) / r);
						if (x >= 0 && y >= 0)
						{
							angle = refAngle;
						}
						else if (x < 0 && y >= 0)
						{
							angle = (float) (Math.PI - refAngle);
						}
						else if (x < 0 && y < 0)
						{
							angle = (float) (Math.PI + refAngle);
						}
						else
						{
							angle = (float) (2.0f * Math.PI - refAngle);
						}
						float t = (z + 1.0f) / 2.0f;
						float s = (float) (angle / (2.0f * Math.PI));

						vertices[i++] = s;
						vertices[i++] = t;
					}
				}
			}
		}

		// Setup the indices buffer
		int num_indices = (phi + ((theta-1)*(2*(phi)))) * 3;
		short[] indices = new short[num_indices];

		i = 0;

		// Top cap
		for (int r = 0; r < phi; r++)
		{
			indices[i++] = (short) (r+1);
			indices[i++] = 0;
			if (r+2 > phi) indices[i++] = 1;
			else indices[i++] = (short) (r+2);
		}

		// Rings
		for (int s = 0; s < theta-1; s++)
		{
			for (int r = 0; r < phi; r++)
			{
				indices[i++] = (short) (r+1+((s+1)*phi)+0);
				indices[i++] = (short) (r+1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+((s+1)*phi)+1);
				else indices[i++] = (short) (1+((s+1)*phi)+0);

				indices[i++] = (short) (r+1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+(s*phi)+1);
				else indices[i++] = (short) (1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+((s+1)*phi)+1);
				else indices[i++] = (short) (1+((s+1)*phi)+0);
			}
		}
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (normals) vasa.add(new VertexAttribute(Usage.Normal, 3, "a_normal"));
		if (texCoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh mesh = new Mesh(true, num_vertices, num_indices, vas);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	public static Mesh getSphereMesh(int theta, int phi, float scale, boolean normals, boolean texCoords)
	{
		int vertexSize = 3;
		if (normals) vertexSize += 3;
		if (texCoords) vertexSize += 2;
		
		// Calculate the delta step
		double d_theta = Math.PI / (theta+2);
		double d_phi = (2*Math.PI) / phi;

		// Setup vertices buffer
		int num_vertices = 2 + ((theta)*phi);
		float[] vertices = new float[num_vertices*vertexSize];

		int i = 0;

		// Insert the top vertex
		vertices[i++] = 0.0f;
		vertices[i++] = 0.0f;
		vertices[i++] = 1.0f*scale;

		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 1.0f;
		
		if (texCoords)
		{
			vertices[i++] = 0;
			vertices[i++] = 0;
		}

		double c_theta = 0;
		double c_phi = 0;

		// Insert the vertex at each division specified by theta and phi
		for (int reps = 0; reps < theta; reps++)
		{
			c_theta += d_theta;
			c_phi = 0;
			for (int p = 0; p < phi; p++)
			{
				c_phi += d_phi;
				float x = (float) (Math.sin(c_theta) * Math.cos(c_phi));
				float y = (float) (Math.sin(c_theta) * Math.sin(c_phi));
				float z = (float) Math.cos(c_theta);

				vertices[i++] = x*scale;
				vertices[i++] = y*scale;
				vertices[i++] = z*scale;

				if (normals) vertices[i++] = x;
				if (normals) vertices[i++] = y;
				if (normals) vertices[i++] = z;
				
				if (texCoords)
				{
					float r = (float) Math.sqrt(1.0f - z * z);
					if (r < 0.001f)
					{
						vertices[i++] = 0;
						vertices[i++] = 0;
					}
					else
					{
						float angle;
						float refAngle = (float) Math.acos(Math.abs(x) / r);
						if (x >= 0 && y >= 0)
						{
							angle = refAngle;
						}
						else if (x < 0 && y >= 0)
						{
							angle = (float) (Math.PI - refAngle);
						}
						else if (x < 0 && y < 0)
						{
							angle = (float) (Math.PI + refAngle);
						}
						else
						{
							angle = (float) (2.0f * Math.PI - refAngle);
						}
						float t = (z + 1.0f) / 2.0f;
						float s = (float) (angle / (2.0f * Math.PI));

						vertices[i++] = s;
						vertices[i++] = t;
					}
				}
			}
		}

		// Insert the bottom vertex
		vertices[i++] = 0.0f;
		vertices[i++] = 0.0f;
		vertices[i++] = -1.0f*scale;

		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = -1.0f;
		
		if (texCoords)
		{
			vertices[i++] = 1;
			vertices[i++] = 1;
		}

		// Setup the indices buffer
		int num_indices = ((2*phi) + ((theta-1)*(2*(phi)))) * 3;
		short[] indices = new short[num_indices];

		i = 0;

		// Top cap
		for (int r = 0; r < phi; r++)
		{
			indices[i++] = (short) (r+1);
			indices[i++] = 0;
			if (r+2 > phi) indices[i++] = 1;
			else indices[i++] = (short) (r+2);
		}

		// Rings
		for (int s = 0; s < theta-1; s++)
		{
			for (int r = 0; r < phi; r++)
			{
				indices[i++] = (short) (r+1+((s+1)*phi)+0);
				indices[i++] = (short) (r+1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+((s+1)*phi)+1);
				else indices[i++] = (short) (1+((s+1)*phi)+0);

				indices[i++] = (short) (r+1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+(s*phi)+1);
				else indices[i++] = (short) (1+(s*phi)+0);
				if (r!= phi-1) indices[i++] = (short) (r+1+((s+1)*phi)+1);
				else indices[i++] = (short) (1+((s+1)*phi)+0);
			}
		}

		// Bottom Cap
		for (int r = 0; r < phi; r++)
		{
			indices[i++] = (short) (num_vertices-1);
			indices[i++] = (short) (num_vertices-phi-1+r);
			if (r+2 > phi) indices[i++] = (short) (num_vertices-phi-1);
			else indices[i++] = (short) (num_vertices-phi-1+r+1);
		}
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (normals) vasa.add(new VertexAttribute(Usage.Normal, 3, "a_normal"));
		if (texCoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh mesh = new Mesh(true, num_vertices, num_indices, vas);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	public static Mesh getCurvedPlaneMesh(int size, float step, float edge, float mid, boolean texcoords)
	{
		int n = (int) ((size*2.0f)/step)+1;
		
		int vertexSize = 3;
		if (texcoords) vertexSize += 2;

		final float[] vertices = new float[n*n*vertexSize];
		final short[] indices = new short[(n-1)*(n-1)*6];
		
		int i = 0;
		for (int ix = 0; ix < n; ix++)
		{
			for (int iz = 0; iz < n; iz++)
			{
				vertices[i++] = ix*step-size;
				vertices[i++] = ImageUtils.lerp(mid, edge, MathUtils.clamp(Vector3.dst(ix*step-size, 0, iz*step-size, 0, 0, 0)/(float)size, 0.0f, 1.0f));
				vertices[i++] = iz*step-size;

				if (texcoords) vertices[i++] = (float)ix/(float)(n);
				if (texcoords) vertices[i++] = (float)iz/(float)(n);
			}
		}
		
		i = 0;
		for (int ix = 0; ix < n-1; ix++)
		{
			for (int iz = 0; iz < n-1; iz++)
			{
				int t1p1 = iz+(ix*n);
				int t1p2 = iz+1+((ix+1)*n);
				int t1p3 = iz+((ix+1)*n);

				indices[i++] = (short) t1p1;
				indices[i++] = (short) t1p2;
				indices[i++] = (short) t1p3;
				
				int t2p1 = iz+(ix*n);
				int t2p2 = iz+1+(ix*n);
				int t2p3 = iz+1+((ix+1)*n);

				indices[i++] = (short) t2p1;
				indices[i++] = (short) t2p2;
				indices[i++] = (short) t2p3;
			}
		}
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (texcoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh mesh = new Mesh(true, n*n, (n-1)*(n-1)*6, vas);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	public static Mesh getPlaneMesh(float x, float z, boolean texcoords)
	{
		int vertSize = 3;
		if (texcoords) vertSize += 2;
		
		float[] verts = {
			-x, 0, -z,
			-x, 0, z,
			x, 0, z,
			x, 0, -z
		};

		float[] texCoords = {
			0, 0,
			0, 1,
			1, 1,
			1, 0
		};
		
		float[] vertices = new float[4 * vertSize];
		int pIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = verts[pIdx++];
			vertices[i++] = verts[pIdx++];
			vertices[i++] = verts[pIdx++];
			if (texcoords) vertices[i++] = texCoords[tIdx++];
			if (texcoords) vertices[i++] = texCoords[tIdx++];
		}
		
		short[] indices = {
			0, 2, 1,
			0, 3, 2
		};
		
		VertexAttribute[] vas = null;
		if (texcoords)
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0")
				};
		else
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				};
		
		Mesh mesh = new Mesh(true, 4, 6, vas);
		
		mesh.setVertices(vertices);
		mesh.setIndices(indices);
		
		return mesh;
	}
	
	public static float[] getBoxVertices(float x, float tx, int snapx, float y, float z, float tz, int snapz, boolean normals, boolean texcoords)
	{
		int vertSize = 3;
		
		if (normals) vertSize += 3;
		if (texcoords) vertSize += 2;
		
		float tminx = -tx;
		float tmaxx = +tx;
		
		if (snapx == 1)
		{
			tmaxx = +x;
			tminx = x - (tx*2);
		}
		else if (snapx == -1)
		{
			tminx = -x;
			tmaxx = (-x) + (tx*2);
		}
		
		float tminz = -tz;
		float tmaxz = +tz;
		
		if (snapz == 1)
		{
			tmaxz = +z;
			tminz = z - (tz*2);
		}
		else if (snapz == -1)
		{
			tminz = -z;
			tmaxz = (-z) + (tz*2);
		}
		
		float[] points = {
				-x, -y, -z, // bottom
				-x, -y, +z,
				+x, -y, +z,
				+x, -y, -z,

				tminx, +y, tminz, // top
				tminx, +y, tmaxz,
				tmaxx, +y, tmaxz,
				tmaxx, +y, tminz,
		};

		float[] cubeVerts = {
				points[0], points[1], points[2], // bottom
				points[3], points[4], points[5],
				points[6], points[7], points[8],
				points[9], points[10], points[11],

				points[12], points[13], points[14], // top
				points[15], points[16], points[17],
				points[18], points[19], points[20],
				points[21], points[22], points[23],

				points[0], points[1], points[2], // back
				points[12], points[13], points[14],
				points[21], points[22], points[23],
				points[9], points[10], points[11],

				points[3], points[4], points[5], // front
				points[15], points[16], points[17],
				points[18], points[19], points[20],
				points[6], points[7], points[8],

				points[0], points[1], points[2], // left
				points[3], points[4], points[5],
				points[15], points[16], points[17],
				points[12], points[13], points[14],

				points[9], points[10], points[11], // right
				points[6], points[7], points[8],
				points[18], points[19], points[20],
				points[21], points[22], points[23]
		};
		
		float[] cubeTex = {

				0, 0, // bottom
				0, 1,
				1, 1,
				1, 0,

				1, 0, // top
				1, 1,
				0, 1,
				0, 0,

				1, 1, // back
				1, 0,
				0, 0,
				0, 1,

				1, 1, // front
				1, 0,
				0, 0,
				0, 1,

				1, 1, // left
				0, 1,
				0, 0,
				1, 0,

				1, 1, // right
				0, 1,
				0, 0,
				1, 0
		};
		
		short[] indices = {
				0, 2, 1, // bottom
				0, 3, 2,

				4, 5, 6, // top
				4, 6, 7,

				8, 9, 10, // back
				8, 10, 11,

				12, 15, 14, // front
				12, 14, 13,

				16, 17, 18, // left
				16, 18, 19,

				20, 23, 22, // right
				20, 22, 21
		};
		
		Vector3 nFront = Pools.obtain(Vector3.class);
		Vector3 nBack = Pools.obtain(Vector3.class);
		Vector3 nLeft = Pools.obtain(Vector3.class);
		Vector3 nRight = Pools.obtain(Vector3.class);
		
		Vector3 U = Pools.obtain(Vector3.class);
		Vector3 V = Pools.obtain(Vector3.class);
		
		// FRONT
		int index = 18;
		int p1 = indices[index+0]*3;
		int p2 = indices[index+1]*3;
		int p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		Vector3 N = U.crs(V).nor();		
		nFront.set(N);
		
		// BACK
		index = 12;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nBack.set(N);

		// LEFT
		index = 24;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nLeft.set(N);

		// RIGHT
		index = 30;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nRight.set(N);
		
		Pools.free(U);
		Pools.free(V);
		
		float[] cubeNormals = {
				0, -1, 0, // bottom
				0, -1, 0,
				0, -1, 0,
				0, -1, 0,

				0, 1, 0, // top
				0, 1, 0,
				0, 1, 0,
				0, 1, 0,

				nBack.x, nBack.y, nBack.z, // back
				nBack.x, nBack.y, nBack.z,
				nBack.x, nBack.y, nBack.z,
				nBack.x, nBack.y, nBack.z,

				nFront.x, nFront.y, nFront.z, // front
				nFront.x, nFront.y, nFront.z,
				nFront.x, nFront.y, nFront.z,
				nFront.x, nFront.y, nFront.z,

				nLeft.x, nLeft.y, nLeft.z, // left
				nLeft.x, nLeft.y, nLeft.z,
				nLeft.x, nLeft.y, nLeft.z,
				nLeft.x, nLeft.y, nLeft.z,

				nRight.x, nRight.y, nRight.z, // right
				nRight.x, nRight.y, nRight.z,
				nRight.x, nRight.y, nRight.z,
				nRight.x, nRight.y, nRight.z
		};

		float[] vertices = new float[24 * vertSize];
		int pIdx = 0;
		int nIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			
			if (normals)
			{
				vertices[i++] = cubeNormals[nIdx++];
				vertices[i++] = cubeNormals[nIdx++];
				vertices[i++] = cubeNormals[nIdx++];
			}
			
			if (texcoords)
			{
				vertices[i++] = cubeTex[tIdx++];
				vertices[i++] = cubeTex[tIdx++];
			}
		}
		
		return vertices;
	}
	
	public static Mesh getBoxMesh(float x, float tx, int snapx, float y, float z, float tz, int snapz, boolean normals, boolean texcoords)
	{
		float[] vertices = getBoxVertices(x, tx, snapx, y, z, tz, snapz, normals, texcoords);

		short[] indices = {
				0, 2, 1, // bottom
				0, 3, 2,

				4, 5, 6, // top
				4, 6, 7,

				8, 9, 10, // back
				8, 10, 11,

				12, 15, 14, // front
				12, 14, 13,

				16, 17, 18, // left
				16, 18, 19,

				20, 23, 22, // right
				20, 22, 21
		};
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (normals) vasa.add(new VertexAttribute(Usage.Normal, 3, "a_normal"));
		if (texcoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh box = new Mesh(true, 24, 36, vas);
		
		box.setVertices(vertices);
		box.setIndices(indices);
		
		return box;
	}
	
	public static Mesh getBoxMesh(float x, float y, float z, boolean normals, boolean texcoords)
	{
		return getBoxMesh(x, x, 0, y, z, z, 0, normals, texcoords);
	}
	
	public static Mesh getPrismMesh(float x, float tx, float y, float z, boolean normals, boolean texcoords)
	{
		short[] indices = {
				0, 2, 1, // bottom - 0
				0, 3, 2,

				4, 5, 6, // back - 6
				4, 6, 7,

				8, 11, 10, // front - 12
				8, 10, 9,

				12, 13, 14, // left - 18

				15, 17, 16, // right - 21
		};
		
		int vertSize = 3;
		
		if (normals) vertSize += 3;
		if (texcoords) vertSize += 2;
		
		float[] cubeVerts = {
				-x, -y, -z, // bottom - 0
				-x, -y, +z,
				+x, -y, +z,
				+x, -y, -z,

				-x, -y, -z, // back - 12
				-tx, +y, 0,
				+tx, +y, 0,
				+x, -y, -z,

				-x, -y, +z, // front - 24
				-tx, +y, 0,
				+tx, +y, 0,
				+x, -y, +z,

				-x, -y, -z, // left - 36
				-x, -y, +z,
				-tx, +y, 0,

				+x, -y, -z, // right - 45
				+x, -y, +z,
				+tx, +y, 0,
				};
		
		Vector3 nFront = Pools.obtain(Vector3.class);
		Vector3 nBack = Pools.obtain(Vector3.class);
		Vector3 nLeft = Pools.obtain(Vector3.class);
		Vector3 nRight = Pools.obtain(Vector3.class);
		
		Vector3 U = Pools.obtain(Vector3.class);
		Vector3 V = Pools.obtain(Vector3.class);
		
		// FRONT
		int index = 12;
		int p1 = indices[index+0]*3;
		int p2 = indices[index+1]*3;
		int p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		Vector3 N = U.crs(V).nor();		
		nFront.set(N);
		
		// BACK
		index = 6;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nBack.set(N);

		// LEFT
		index = 18;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nLeft.set(N);

		// RIGHT
		index = 21;
		p1 = indices[index+0]*3;
		p2 = indices[index+1]*3;
		p3 = indices[index+2]*3;
		U.set(cubeVerts[p2+0]-cubeVerts[p1+0], cubeVerts[p2+1]-cubeVerts[p1+1], cubeVerts[p2+2]-cubeVerts[p1+2]);
		V.set(cubeVerts[p3+0]-cubeVerts[p1+0], cubeVerts[p3+1]-cubeVerts[p1+1], cubeVerts[p3+2]-cubeVerts[p1+2]);		
		N = U.crs(V).nor();		
		nRight.set(N);
		
		Pools.free(U);
		Pools.free(V);
		
		float[] cubeNormals = {
				0, -1, 0, // bottom
				0, -1, 0,
				0, -1, 0,
				0, -1, 0,

				nBack.x, nBack.y, nBack.z, // back
				nBack.x, nBack.y, nBack.z,
				nBack.x, nBack.y, nBack.z,
				nBack.x, nBack.y, nBack.z,

				nFront.x, nFront.y, nFront.z, // front
				nFront.x, nFront.y, nFront.z,
				nFront.x, nFront.y, nFront.z,
				nFront.x, nFront.y, nFront.z,

				nLeft.x, nLeft.y, nLeft.z, // left
				nLeft.x, nLeft.y, nLeft.z,
				nLeft.x, nLeft.y, nLeft.z,
				nLeft.x, nLeft.y, nLeft.z,

				nRight.x, nRight.y, nRight.z, // right
				nRight.x, nRight.y, nRight.z,
				nRight.x, nRight.y, nRight.z,
				nRight.x, nRight.y, nRight.z,
				};
		
		Pools.free(nFront);
		Pools.free(nBack);
		Pools.free(nLeft);
		Pools.free(nRight);
		
		float[] cubeTex = {

				0, 0, // bottom
				0, 1,
				1, 1,
				1, 0,

				1, 1, // back
				1, 0,
				0, 0,
				0, 1,

				1, 1, // front
				1, 0,
				0, 0,
				0, 1,

				1, 1, // left
				0, 1,
				0, 0.5f,

				1, 1, // right
				0, 1,
				0, 0.5f,

		};

		float[] vertices = new float[18 * vertSize];
		int pIdx = 0;
		int nIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			
			if (normals)
			{
				vertices[i++] = cubeNormals[nIdx++];
				vertices[i++] = cubeNormals[nIdx++];
				vertices[i++] = cubeNormals[nIdx++];
			}
			
			if (texcoords)
			{
				vertices[i++] = cubeTex[tIdx++];
				vertices[i++] = cubeTex[tIdx++];
			}
		}
		
		VertexAttribute[] vas = null;
		ArrayList<VertexAttribute> vasa = new ArrayList<VertexAttribute>();
		vasa.add(new VertexAttribute(Usage.Position, 3, "a_position"));
		if (normals) vasa.add(new VertexAttribute(Usage.Normal, 3, "a_normal"));
		if (texcoords) vasa.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		vas = new VertexAttribute[vasa.size()];
		vasa.toArray(vas);
		
		Mesh prism = new Mesh(true, 18, 24, vas);
		
		prism.setVertices(vertices);
		prism.setIndices(indices);
		
		return prism;
	}
	
	public static Mesh getFrustumMesh(Frustum frustum, int xs, int ys, float yoffset, boolean tops, boolean tope)
	{
		final int s = (tops) ? 3 : 0;
		final int e = (tope) ? 7 : 4;
		
		final int vertexSize = 3;
		final int vertNum = 2*xs*(ys-1);
		float[] vertices = new float[vertNum*vertexSize];
		
		float[] xmin = new float[ys];
		float[] xstep = new float[ys];
	
		float[] ypoint = new float[ys];
		
		Vector3 vmin = Pools.obtain(Vector3.class).set(frustum.planePoints[s].x, 0, frustum.planePoints[s].z);
		Vector3 vmax = Pools.obtain(Vector3.class).set(frustum.planePoints[e].x, 0, frustum.planePoints[s].z);
		Vector3 tmp = Pools.obtain(Vector3.class);
		for (int y = 0; y < ys; y++)
		{
			float alpha = 1.0f - (float)y / (float)ys;
			tmp.set(vmin).lerp(vmax, alpha);
			
			ypoint[y] = alpha * (frustum.planePoints[e].z - frustum.planePoints[s].z) + yoffset;
			xmin[y] = tmp.x;
			xstep[y] = (-tmp.x*2.0f) / ((float)xs-1);
		}
		
		int i = 0;
		for (int y = 0; y < ys-2; y++)
		{
			for (int x = 0; x < xs; x++)
			{
				vertices[i++] = xmin[y]+(x*xstep[y]);
				vertices[i++] = 0;
				vertices[i++] = ypoint[y];
				
				vertices[i++] = xmin[y+1]+(x*xstep[y+1]);
				vertices[i++] = 0;
				vertices[i++] = ypoint[y+1];
			}
			y++;
			for (int x = xs-1; x >= 0; x--)
			{
				vertices[i++] = xmin[y+1]+(x*xstep[y+1]);
				vertices[i++] = 0;
				vertices[i++] = ypoint[y+1];
				
				vertices[i++] = xmin[y]+(x*xstep[y]);
				vertices[i++] = 0;
				vertices[i++] = ypoint[y];
			}
		}
		
		Pools.free(vmin);
		Pools.free(vmax);
		Pools.free(tmp);
		
		Mesh mesh = new Mesh(true, vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		mesh.setVertices(vertices);
		return mesh;
	}
	
	public static Mesh[] getArea(int size, int scale)
	{	
		float offsets[][] = {
				// High res close
				{0, 0, 1},
				{(-size*scale)+scale, 0, 1},
				{0, (-size*scale)+scale, 1},
				{(-size*scale)+scale, (-size*scale)+scale, 1},
				
				// Low res distance
				// Edges
				{(-size*scale)+2*scale, (size*scale)-2*scale, 2},
				{(-size*scale)+2*scale, (-3*size*scale)+6*scale, 2},
				{(size*scale)-2*scale, (-size*scale)+2*scale, 2},
				{(-3*size*scale)+6*scale, (-size*scale)+2*scale, 2},
				// Corners
				{(size*scale)-2*scale, (size*scale)-2*scale, 2},
				{(size*scale)-2*scale, (-3*size*scale)+6*scale, 2},
				{(-3*size*scale)+6*scale, (size*scale)-2*scale, 2},
				{(-3*size*scale)+6*scale, (-3*size*scale)+6*scale, 2}
		};
		
		Mesh mesh[] = new Mesh[offsets.length];
		float[] vertices = new float[size*size*3];
		int i = 0;
		
		final short[] indices = new short[(size-1)*(size-1)*6];
		
		i = 0;
		for (int ix = 0; ix < size-1; ix++)
		{
			for (int iz = 0; iz < size-1; iz++)
			{
				short start = (short) (ix+(iz*size));

				indices[i++] = (short) (start);
				indices[i++] = (short) (start+1);
				indices[i++] = (short) (start+size);
				
				indices[i++] = (short) (start+1);
				indices[i++] = (short) (start+1+size);
				indices[i++] = (short) (start+size);
			
			}
		}
		
		for (int index = 0; index < offsets.length; index++)
		{
			i = 0;
			for (int ix = 0; ix < size; ix++)
			{
				for (int iz = 0; iz < size; iz++)
				{
					vertices[i++] = offsets[index][0]+(ix*(offsets[index][2]*scale));
					vertices[i++] = 0;
					vertices[i++] = offsets[index][1]+(iz*(offsets[index][2]*scale));
				}
			}
			mesh[index] = new Mesh(true, size*size, indices.length, 
					new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
			mesh[index].setVertices(vertices);
			mesh[index].setIndices(indices);
		}

		return mesh;
	}

}
