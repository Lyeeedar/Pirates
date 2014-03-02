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
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
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
import com.badlogic.gdx.math.BSpline;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MotionTrail implements Queueable {
	
	private static final int VERT_SIZE = 5;
	
	private final CircularArrayRing<Vector3> trailRing;
	private final int vertNum;
	private final int vertNum2;
	public Mesh mesh;
	public final Color colour;
	public final Texture texture;
	public final long texHash;
	private final float[] vertices;
	public final boolean managed;
	public final Vector3 offsetBot = new Vector3();
	public final Vector3 offsetTop = new Vector3();
	public final Matrix4 transform = new Matrix4();
	public final float updateTime;
	private float time = 0;
	private Vector3[] top = {new Vector3(), new Vector3(), new Vector3(), new Vector3()};
	private Vector3[] bot = {new Vector3(), new Vector3(), new Vector3(), new Vector3()};
	
	private boolean up = false;
	
	private boolean shouldDraw = false;
	private boolean drawing = false; 
	private final Vector3 tmpVec = new Vector3();
	private final Vector3 tmpVec2 = new Vector3();
	private final Matrix4 tmpMat = new Matrix4();
	private final CatmullRomSpline<Vector3> splineTop = new CatmullRomSpline<Vector3>();
	private final CatmullRomSpline<Vector3> splineBot = new CatmullRomSpline<Vector3>();
	
	public int num_active = 0;
	
	public MotionTrail(int vertsNum, float updateTime, Color colour, Texture texture) 
	{		
		this.colour = colour;
		this.texture = texture;
		this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		this.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.texHash = texture.hashCode();
		this.vertNum = vertsNum * 2;
		this.vertNum2 = vertsNum;
		this.trailRing = new CircularArrayRing<Vector3>(this.vertNum);
		this.managed = true;
		this.updateTime = updateTime;
		
		for (int i = 0; i < this.vertNum; i++)
		{
			trailRing.add(new Vector3());
		}
		
		this.mesh = new Mesh(false, this.vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 2, "a_texCoord0"));
		this.vertices = new float[this.vertNum * VERT_SIZE];
	}
	
	public MotionTrail(int vertsNum, float updateTime, Color colour, Texture texture, Vector3 offsetBot, Vector3 offsetTop) 
	{		
		this.colour = colour;
		this.texture = texture;
		this.texture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		this.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.texHash = texture.hashCode();
		this.vertNum = vertsNum * 2;
		this.vertNum2 = vertsNum;
		this.trailRing = new CircularArrayRing<Vector3>(this.vertNum);
		this.managed = false;
		this.offsetBot.set(offsetBot);
		this.offsetTop.set(offsetTop);
		this.updateTime = updateTime;
		
		for (int i = 0; i < this.vertNum; i++)
		{
			trailRing.add(new Vector3());
		}
		
		this.mesh = new Mesh(false, this.vertNum, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 2, "a_texCoord0"));
		this.vertices = new float[this.vertNum * VERT_SIZE];
	}
	
	public void stopDraw()
	{
		shouldDraw = false;
	}
		
	protected void addVert(Vector3 vert)
	{
		trailRing.peekAndMove().set(vert);
		if (num_active < vertNum2) num_active++;
	}

	protected void updateVerts()
	{
		float step = num_active > 0 ? 1.0f / ((float) num_active / 2.0f) : 1 ;
		for (int i = 0; i < num_active; i++)
		{
			Vector3 vert = trailRing.get(i);
			vertices[(i*VERT_SIZE)+0] = vert.x;
			vertices[(i*VERT_SIZE)+1] = vert.y;
			vertices[(i*VERT_SIZE)+2] = vert.z;
			vertices[(i*VERT_SIZE)+3] = (float) (i / 2) * step;
			vertices[(i*VERT_SIZE)+4] = (up) ? 1.0f : 0.0f;
			
			up = (!up);
		}
		
		mesh.setVertices(vertices);
		//mesh.updateVertices(0, vertices, 0, num_active * VERT_SIZE);
	}
	
	public void update(Vector3 bot, Vector3 top)
	{
		if (!drawing || !shouldDraw)
		{
			num_active = 0;
			for (Vector3 v : this.top) v.set(top);
			for (Vector3 v : this.bot) v.set(bot);
		}
		shouldDraw = true;
		drawing = true;
		
		this.top[this.top.length-1].set(top);
		this.bot[this.bot.length-1].set(bot);
		
		transform.setToTranslation(top);
	}
	
	public void dispose()
	{
		if (mesh != null) mesh.dispose();
		mesh = null;
		drawing = false;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		if (drawing) if (batches.containsKey(MotionTrailBatch.class)) ((MotionTrailBatch) batches.get(MotionTrailBatch.class)).add(this);
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			PositionalData pData = source.readOnlyRead(PositionalData.class);
			for (int i = 0; i < num_active; i++)
			{
				Vector3 vert = trailRing.get(i);
				vert.add(pData.position.x - pData.lastPos1.x, pData.position.y - pData.lastPos1.y, pData.position.z - pData.lastPos1.z);
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
				
				update(tmpVec, tmpVec2);
			}
			else
			{
				MinimalPositionalData pData = source.readOnlyRead(MinimalPositionalData.class);
				tmpVec.set(pData.position).add(offsetBot).add(offset);
				tmpVec2.set(pData.position).add(offsetTop).add(offset);
				
				update(tmpVec, tmpVec2);
			}
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		if (drawing && !shouldDraw)
		{
			time += delta;
			float t = 0;
			for (; t < time-updateTime; t += updateTime)
			{
				if (num_active >= 2) num_active -= 2;
				
				if (num_active == 0)
				{
					drawing = false;
				}
			}
			
			time -= t;
		}
		
		if (drawing) 
		{
			time += delta;
			float t = 0;
			splineTop.set(top, false);
			splineBot.set(bot, false);
			for (; t < time-updateTime; t += updateTime)
			{
				float tt = t/time;
				Vector3 tp = splineTop.valueAt(tmpVec, tt);
				Vector3 bp = splineBot.valueAt(tmpVec2, tt);
				
				if (tp.epsilonEquals(trailRing.get(1), 0.0001f)) continue;
				if (bp.epsilonEquals(trailRing.get(0), 0.0001f)) continue;
				
				addVert(tp);
				addVert(bp);
			}
			
			time -= t;
			
			for (int i = 0; i < top.length-1; i++) top[i].set(top[i+1]);
			for (int i = 0; i < bot.length-1; i++) bot[i].set(bot[i+1]);
			
			updateVerts();
		}
	}

	@Override
	public Queueable copy() {
		if (managed) return new MotionTrail(vertNum2, updateTime, colour, texture);
		else return new MotionTrail(vertNum2, updateTime, colour, texture, offsetBot, offsetTop);
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
	public float[][] getVertexArray()
	{
		return new float[][]{new float[]{0}};
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		return out.set(0, 0, 0).mul(transform);
	}
}
