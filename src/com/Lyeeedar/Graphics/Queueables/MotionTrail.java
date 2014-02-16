package com.Lyeeedar.Graphics.Queueables;
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

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.MotionTrailBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.CircularArrayRing;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MotionTrail implements Queueable {
	
	private final CircularArrayRing<Vector3> trailRing;
	private final int vertNum;
	private final int vertNum2;
	public final Mesh mesh;
	public final Color colour;
	public final Texture texture;
	public final long texHash;
	private final float[] vertices;
	private final float step;
	public final boolean managed;
	public final Vector3 offsetBot = new Vector3();
	public final Vector3 offsetTop = new Vector3();
	public final Matrix4 transform = new Matrix4();
	
	private boolean up = false;
	
	private boolean shouldDraw = false;
	private boolean drawing = false; 
	private int drawCooldown = 0;
	private final Vector3 tmpVec = new Vector3();
	private final Vector3 tmpVec2 = new Vector3();
	private final Matrix4 tmpMat = new Matrix4();

	public MotionTrail(int vertsNum, Color colour, Texture texture) 
	{		
		this.colour = colour;
		this.texture = texture;
		this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		this.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.texHash = texture.hashCode();
		this.vertNum = vertsNum * 2;
		this.vertNum2 = vertsNum;
		this.step = 1.0f / (float) vertNum2;
		this.trailRing = new CircularArrayRing<Vector3>(this.vertNum);
		this.managed = true;
		
		for (int i = 0; i < this.vertNum; i++)
		{
			trailRing.add(new Vector3());
		}
		
		this.mesh = new Mesh(false, this.vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 2, "a_texCoord0"));
		this.vertices = new float[this.vertNum * 5];
	}
	
	public MotionTrail(int vertsNum, Color colour, Texture texture, Vector3 offsetBot, Vector3 offsetTop) 
	{		
		this.colour = colour;
		this.texture = texture;
		this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		this.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.texHash = texture.hashCode();
		this.vertNum = vertsNum * 2;
		this.vertNum2 = vertsNum;
		this.step = 1.0f / (float) vertNum2;
		this.trailRing = new CircularArrayRing<Vector3>(this.vertNum);
		this.managed = false;
		this.offsetBot.set(offsetBot);
		this.offsetTop.set(offsetTop);
		
		for (int i = 0; i < this.vertNum; i++)
		{
			trailRing.add(new Vector3());
		}
		
		this.mesh = new Mesh(false, this.vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 2, "a_texCoord0"));
		this.vertices = new float[this.vertNum * 5];
	}
	
	public void draw(Vector3 bottom, Vector3 top)
	{
		if (!drawing)
		{
			reset(bottom, top);
		}
		drawCooldown = vertNum2;
		shouldDraw = true;
		drawing = true;
	}
	
	public void stopDraw()
	{
		shouldDraw = false;
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
			vertices[(i*5)+3] = (float) (i / 2) * step;
			vertices[(i*5)+4] = (up) ? 1.0f : 0.0f;
			
			up = (!up);
		}
		
		mesh.setVertices(vertices);
	}
	
	public void update(Vector3 bottom, Vector3 top)
	{
		addVert(bottom);
		addVert(top);
		
		transform.setToTranslation(top);
	}
	
	public void dispose()
	{
		mesh.dispose();
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		if (drawing) ((MotionTrailBatch) batches.get(MotionTrailBatch.class)).add(this);
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			PositionalData pData = source.readOnlyRead(PositionalData.class);
			for (int i = 0; i < vertNum; i++)
			{
				Vector3 vert = trailRing.get(i);
				vert.add(pData.deltaPos);
			}
		}
		
		if (!managed)
		{			
			if (source.readOnlyRead(PositionalData.class) != null)
			{
				PositionalData pData = source.readOnlyRead(PositionalData.class);
				tmpMat.setToRotation(GLOBALS.DEFAULT_ROTATION, pData.rotation);
				tmpVec.set(offsetBot).rot(tmpMat).add(pData.position).add(offset);
				tmpVec2.set(offsetTop).rot(tmpMat).add(pData.position).add(offset);
				
				draw(tmpVec, tmpVec2);
				update(tmpVec, tmpVec2);
			}
			else
			{
				MinimalPositionalData pData = source.readOnlyRead(MinimalPositionalData.class);
				tmpVec.set(pData.position).add(offsetBot).add(offset);
				tmpVec2.set(pData.position).add(offsetTop).add(offset);
				
				draw(tmpVec, tmpVec2);
				update(tmpVec, tmpVec2);
			}
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		if (drawing && !shouldDraw)
		{
			Vector3 b = trailRing.get(1);
			Vector3 t = trailRing.get(0);
			
			addVert(b);
			addVert(t);
			
			drawCooldown--;
			if (drawCooldown == 0)
			{
				drawing = false;
			}
		}
		
		if (drawing) updateVerts();
	}

	@Override
	public Queueable copy() {
		if (managed) return new MotionTrail(vertNum2, colour, texture);
		else return new MotionTrail(vertNum2, colour, texture, offsetBot, offsetTop);
	}

	@Override
	public void set(Matrix4 transform)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transform(Matrix4 mat)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Matrix4 getTransform()
	{
		return transform;
	}

	@Override
	public Vector3[] getVertexArray()
	{
		return new Vector3[]{new Vector3()};
	}
}
