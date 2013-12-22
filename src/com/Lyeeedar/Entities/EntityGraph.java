package com.Lyeeedar.Entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pools;

public class EntityGraph {

	public Entity entity;
	public EntityGraph parent;
	public HashSet<EntityGraph> children = new HashSet<EntityGraph>();
	private final Entity.PositionalData pData = new  Entity.PositionalData();
	private final Entity.StatusData sData = new  Entity.StatusData();
	public final Box bounds = new Box();
	public final BoundingBox bb = new BoundingBox();
	public boolean walkable = false;
	
	public EntityGraph(Entity entity, EntityGraph parent, boolean walkable)
	{
		this.entity = entity;
		this.parent = parent;
		this.walkable = walkable;
		if (parent != null) parent.children.add(this);
		if (entity != null) entity.setGraph(this);
	}
	
	public void remove()
	{
		parent.children.remove(this);
		for (EntityGraph graph : children) graph.insert(parent);
		
		pData.dispose();
		if (entity != null) entity.dispose();
	}
	
	public void pop()
	{
		parent.children.remove(this);
		parent = null;
	}
	
	public void insert(EntityGraph parent)
	{
		if (!parent.walkable) return;
		
		this.parent = parent;
		parent.children.add(this);
	}
	
	public void popAndInsert(EntityGraph parent)
	{
		if (this.parent == null || this.parent.equals(parent) || !parent.walkable) return;
		pop();
		insert(parent);
	}
	
	public void collectDead(List<EntityGraph> list)
	{
		if (entity != null)
		{
			entity.readData(sData, Entity.StatusData.class);
			
			if (!sData.ALIVE) list.add(this);
		}
		for (EntityGraph eg : children) {
			eg.collectDead(list);
		}
	}

	public void queueRenderables(Camera cam, LightManager lights, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		if (entity != null 
				//&&
				//cam.frustum.sphereInFrustum(bounds.center, Math.max(bounds.width, Math.max(bounds.height, bounds.depth)))
				//cam.frustum.boundsInFrustum(bounds.getBoundingBox(bb))
				) 
			entity.queueRenderables(cam, lights, delta, modelBatch, decalBatch, trailBatch);
		
		for (EntityGraph eg : children) {
			eg.queueRenderables(cam, lights, delta, modelBatch, decalBatch, trailBatch);
		}
	}
	
	public void getText(List<TextParticle> list, SpriteBatch sB, BitmapFont font)
	{
		if (entity != null)
		{
			entity.readData(sData, Entity.StatusData.class);
			
			if (sData.DAMAGED > 0)
			{
				float mag = 1.0f - ((float)sData.DAMAGED) / ((float)sData.MAX_HEALTH/2);
				if (mag > 1.0f) mag = 1.0f;
				
				entity.readData(pData, Entity.PositionalData.class);
				Decal decal = ImageUtils.getTextDecal(0.25f*GLOBALS.numDigits(sData.DAMAGED), 0.8f, sB, font, null, ""+sData.DAMAGED);
				list.add(new TextParticle(decal, 3.0f, pData.position.add(0, 2, 0), new Vector3(0, 0.6f, 0), new Vector3(1.0f, mag, 0.0f)));
				sData.DAMAGED = 0;
				entity.writeData(sData, Entity.StatusData.class);
			}
		}
		for (EntityGraph eg : children) eg.getText(list, sB, font);
	}
	
	public void getRunnable(LinkedList<Runnable> list, float delta)
	{
		if (entity != null) list.add(entity.getRunnable(delta));
		for (EntityGraph eg : children) eg.getRunnable(list, delta);
	}
	
	public boolean getVisible(Camera cam, List<EntityGraph> list)
	{
		boolean collide = false;
		
		if (entity != null)
		{
			entity.readData(pData, PositionalData.class);
			if (cam.frustum.pointInFrustum(pData.position))
			{
				list.add(this);
				collide = true;
			}
		}
		
		for (EntityGraph eg : children) 
		{
			if (eg.getVisible(cam, list)) collide = true;
		}
		
		return collide;
	}
	
	public boolean collide(CollisionShape<?> source, EntityGraph graph, List<EntityGraph> list)
	{
		if (graph.equals(this)) return false;
		
//		CollisionShape<?> s2 = source.copy();
//		if (!s2.collide(bounds))
//		{
//			s2.free();
//			return false;
//		}
//		s2.free();
		
		boolean collide = false;
		if (entity != null && entity.collide(source)) 
		{
			list.add(this);
			collide = true;
		}
		
		for (EntityGraph eg : children) 
		{
			if (eg.collide(source, graph, list)) collide = true;
		}
		
		return collide;
	}
	
	public EntityGraph collide(CollisionShape<?> source, EntityGraph graph)
	{
		if (graph.equals(this)) return null;
		
//		CollisionShape<?> s2 = source.copy();
//		if (!s2.collide(bounds))
//		{
//			s2.free();
//			return null;
//		}
//		s2.free();
		
		EntityGraph collide = null;
		if (entity != null && entity.collide(source)) collide = this;
		
		for (EntityGraph eg : children) 
		{
			EntityGraph temp = eg.collide(source, graph);
			if (temp != null) collide = temp;
		}
		
		return collide;
	}
	
	public EntityGraph collideWalkables(CollisionShape<?> source, EntityGraph graph)
	{
		if (!walkable || graph.equals(this)) return null;
		
//		CollisionShape<?> s2 = source.copy();
//		if (!s2.collide(bounds))
//		{
//			s2.free();
//			return null;
//		}
//		s2.free();
		
		EntityGraph collide = null;
		if (entity != null && entity.collide(source)) collide = this;
		
		for (EntityGraph eg : children) 
		{
			EntityGraph temp = eg.collideWalkables(source, graph);
			if (temp != null) collide = temp;
		}
		
		return collide;
	}
	
	public Entity getEntity()
	{
		return entity;
	}
	
	public void setEntity(Entity e, boolean walkable)
	{
		this.entity = e;
		this.walkable = walkable;
	}
	
	public void addEntity(Entity e, boolean walkable)
	{
		new EntityGraph(e, this, walkable);
	}
	
	public void getDeltaPos(Matrix4 mat, Vector3 position)
	{
		if (entity == null) 
		{
			mat.setToTranslation(position);
			return;
		}
		
		entity.readData(pData, Entity.PositionalData.class);
		mat.set(pData.composed).translate(pData.position.set(position).mul(pData.lastInv));
	}
	
	public void getDeltaRot(Matrix4 mat)
	{
		if (entity == null) 
		{
			mat.idt();
			return;
		}
		
		entity.readData(pData, Entity.PositionalData.class);
		pData.lastRot2.y = 0; pData.lastRot2.nor();
		pData.rotation.y = 0; pData.rotation.nor();
		mat.setToRotation(pData.lastRot2, pData.rotation);
	}
	
	public boolean recalculateBounds()
	{
		Iterator<EntityGraph> itr = children.iterator();
		while (itr.hasNext())
		{
			EntityGraph eg = itr.next();
			if (!eg.recalculateBounds()) 
			{
				itr.remove();
			}
		}
		
		if (entity != null) entity.getCollisionShapeInternal().getBoundingBox(bb);
		else if (children.size() == 0) 
		{
			return false;
		}
		else
		{
			children.iterator().next().bounds.getBoundingBox(bb);
		}
		
		BoundingBox bb2 = Pools.obtain(BoundingBox.class);
		for (EntityGraph eg : children) 
		{
			eg.bounds.getBoundingBox(bb2);
			bb.ext(bb2.min);
			bb.ext(bb2.max);
		}
		
		bounds.set(bb.min, bb.max);
		
		Pools.free(bb2);
		return true;
	}

}
