package com.Lyeeedar.Graphics;
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

import com.Lyeeedar.Util.CircularArrayRing;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;

public class MotionTrail {
	
	private final CircularArrayRing<Vector3> trailRing;
	private final int vertNum;
	private final int vertNum2;
	public final Mesh mesh;
	public final Color colour;
	public final Texture texture;
	public final long texHash;
	private final float[] vertices;
	
	private boolean up = false;
	private short side = 0;

	public MotionTrail(int vertsNum, Color colour, Texture texture) 
	{		
		this.colour = colour;
		this.texture = texture;
		this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		this.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.texHash = texture.hashCode();
		this.vertNum = vertsNum * 2;
		this.vertNum2 = vertsNum;
		this.trailRing = new CircularArrayRing<Vector3>(this.vertNum);
		
		for (int i = 0; i < this.vertNum; i++)
		{
			trailRing.add(new Vector3());
		}
		
		this.mesh = new Mesh(false, this.vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 2, "a_texCoord0"));
		this.vertices = new float[this.vertNum * 5];
	}
	
	public void reset(Vector3 bottom, Vector3 top)
	{
		for (int i = 0; i < vertNum2; i++)
		{
			addVert(bottom);
			addVert(top);
		}
	}
	
	protected void addVert(Vector3 vert)
	{
		trailRing.peekAndMove().set(vert);
	}

	protected void updateVerts()
	{
		for (int i = 0; i < vertNum; i++)
		{
			Vector3 vert = trailRing.get(i);
			vertices[i*5] = vert.x;
			vertices[(i*5)+1] = vert.y;
			vertices[(i*5)+2] = vert.z;
			vertices[(i*5)+3] = (side < 0) ? 1.0f : 0.0f;
			vertices[(i*5)+4] = (up) ? 1.0f : 0.0f;
			
			up = (!up);
			side++;
			if (side == 2) side = -2;
		}
		
		mesh.setVertices(vertices);
	}
	
	public void update(Vector3 bottom, Vector3 top)
	{
		addVert(bottom);
		addVert(top);
		
		updateVerts();
	}
	
	public void dispose()
	{
		mesh.dispose();
	}
}
