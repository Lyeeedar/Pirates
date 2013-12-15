package com.Lyeeedar.Util;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;

public class Shapes {
	
	public static Mesh getFrustumMesh(Frustum frustum, int xs, int ys, float yoffset)
	{
		final int vertexSize = 3;
		final int vertNum = 2*xs*(ys-1);
		float[] vertices = new float[vertNum*vertexSize];
		
		float[] xmin = new float[ys];
		float[] xstep = new float[ys];
	
		float[] ypoint = new float[ys];
		
		Vector3 vmin = Pools.obtain(Vector3.class).set(frustum.planePoints[0].x, 0, frustum.planePoints[0].z);
		Vector3 vmax = Pools.obtain(Vector3.class).set(frustum.planePoints[4].x, 0, frustum.planePoints[0].z);
		Vector3 tmp = Pools.obtain(Vector3.class);
		for (int y = 0; y < ys; y++)
		{
			float alpha = 1.0f - (float)y / (float)ys;//1.0f / (float) y;
			tmp.set(vmin).lerp(vmax, alpha);
			
			ypoint[y] = alpha * (frustum.planePoints[4].z - frustum.planePoints[0].z) + yoffset;
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
