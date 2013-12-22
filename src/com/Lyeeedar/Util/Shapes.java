package com.Lyeeedar.Util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

public class Shapes {
	
	public static Mesh getHemiSphereMesh(int theta, int phi, float scale, boolean normals)
	{
		int vertexSize = 3;
		if (normals) vertexSize += 3;
		
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

		double c_theta = 0;
		double c_phi = 0;

		// Insert the vertex at each division specified by theta and phi
		for (int r = 0; r < theta; r++)
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
		
		if (normals)
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Normal, 3, "a_normal")
				};
		else
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				};
		
		Mesh mesh = new Mesh(true, num_vertices, num_indices, vas);

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	public static Mesh getSphereMesh(int theta, int phi, float scale, boolean normals)
	{
		int vertexSize = 3;
		if (normals) vertexSize += 3;
		
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

		double c_theta = 0;
		double c_phi = 0;

		// Insert the vertex at each division specified by theta and phi
		for (int r = 0; r < theta; r++)
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
			}
		}

		// Insert the bottom vertex
		vertices[i++] = 0.0f;
		vertices[i++] = 0.0f;
		vertices[i++] = -1.0f*scale;

		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = 0.0f;
		if (normals) vertices[i++] = -1.0f;

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
		
		if (normals)
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Normal, 3, "a_normal")
				};
		else
			vas = new VertexAttribute[]{
				new VertexAttribute(Usage.Position, 3, "a_position"),
				};
		
		Mesh mesh = new Mesh(true, num_vertices, num_indices, vas);

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
	
	public static Mesh getBoxMesh(float x, float y, float z, boolean normals, boolean texcoords)
	{
		int vertSize = 3;
		
		float[] cubeVerts = {
				-x, -y, -z, // bottom
				-x, -y, z,
				x, -y, z,
				x, -y, -z,

				-x, y, -z, // top
				-x, y, z,
				x, y, z,
				x, y, -z,

				-x, -y, -z, // back
				-x, y, -z,
				x, y, -z,
				x, -y, -z,

				-x, -y, z, // front
				-x, y, z,
				x, y, z,
				x, -y, z,

				-x, -y, -z, // left
				-x, -y, z,
				-x, y, z,
				-x, y, -z,

				x, -y, -z, // right
				x, -y, z,
				x, y, z,
				x, y, -z};

		float[] vertices = new float[24 * vertSize];
		int pIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
		}

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
		
		Mesh box = new Mesh(true, 24, 36, 
				new VertexAttribute(Usage.Position, 3, "a_position"));
		
		box.setVertices(vertices);
		box.setIndices(indices);
		
		return box;
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
