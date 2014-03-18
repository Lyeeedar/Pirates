package com.Lyeeedar.Graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class LineRenderer
{
	public static final int MAX_VERTICES = 100;
	Array<Line> lines = new Array<Line>(false, 50);
	float[] vertices;
	Mesh mesh;
	ShaderProgram shader;
	
	public LineRenderer()
	{
		mesh = new Mesh(false, MAX_VERTICES, 0, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Color, 3, "a_colour"));
		vertices = new float[MAX_VERTICES*6];
		
		shader = new ShaderProgram(Gdx.files.internal("data/shaders/forward/simple.vertex.glsl"), Gdx.files.internal("data/shaders/forward/simple.fragment.glsl"));
		
		if (!shader.isCompiled()) System.err.println(shader.getLog());
	}
	
	public void set(float sx, float sy, float sz, float ex, float ey, float ez, float cr, float cg, float cb)
	{
		lines.add(Pools.obtain(Line.class).set(sx, sy, sz, ex, ey, ez, cr, cg, cb));
	}
	
	public void render(Camera cam)
	{
		shader.begin();
		shader.setUniformMatrix("u_pv", cam.combined);
		
		int i = 0;
		for (Line line : lines)
		{
			vertices[i++] = line.start.x;
			vertices[i++] = line.start.y;
			vertices[i++] = line.start.z;
			
			vertices[i++] = line.colour.x;
			vertices[i++] = line.colour.y;
			vertices[i++] = line.colour.z;
			
			vertices[i++] = line.end.x;
			vertices[i++] = line.end.y;
			vertices[i++] = line.end.z;
			
			vertices[i++] = line.colour.x;
			vertices[i++] = line.colour.y;
			vertices[i++] = line.colour.z;
						
			if (i/6 >= MAX_VERTICES)
			{
				mesh.setVertices(vertices);
				mesh.render(shader, GL20.GL_LINES);
				
				i = 0;
			}
		}
		
		if (i != 0)
		{
			mesh.setVertices(vertices);
			mesh.render(shader, GL20.GL_LINES, 0, i/6);
		}
				
		shader.end();
		
		if (lines.size > MAX_VERTICES*50)
		{
			for (Line line : lines)
			{
				Pools.free(line);
			}
			lines.clear();
		}
	}
	
	public static class Line
	{
		Vector3 start = new Vector3();
		Vector3 end = new Vector3();
		Vector3 colour = new Vector3();
		
		public Line set(float sx, float sy, float sz, float ex, float ey, float ez, float cr, float cg, float cb)
		{
			start.set(sx, sy, sz);
			end.set(ex, ey, ez);
			colour.set(cr, cg, cb);
			return this;
		}
	}
}
