package com.Lyeeedar.Entities;

import java.util.HashSet;
import java.util.LinkedList;

import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class EntityGraph {

	private Entity entity;
	public EntityGraph parent;
	public HashSet<EntityGraph> children = new HashSet<EntityGraph>();
	private final Entity.PositionalData pData = new  Entity.PositionalData();
	
	public EntityGraph(Entity entity, EntityGraph parent)
	{
		this.entity = entity;
		this.parent = parent;
		if (parent != null) parent.children.add(this);
		entity.setGraph(this);
	}
	
	public void pop()
	{
		parent.children.remove(this);
		parent = null;
	}
	
	public void insert(EntityGraph parent)
	{
		this.parent = parent;
		parent.children.add(this);
	}
	
	public void popAndInsert(EntityGraph parent)
	{
		if (this.parent.equals(parent)) return;
		pop();
		insert(parent);
	}

	public void queueRenderables(Camera cam, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		entity.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
		
		for (EntityGraph eg : children) {
			eg.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
		}
	}
	
	public void getRunnable(LinkedList<Runnable> list, float delta)
	{
		list.add(entity.getRunnable(delta));
		for (EntityGraph eg : children) eg.getRunnable(list, delta);
	}
	
	public EntityGraph collide(CollisionShape<?> source, EntityGraph graph)
	{
		if (graph.equals(this)) return null;
		
		EntityGraph collide = null;
		if (entity.collide(source)) collide = this;
		
		for (EntityGraph eg : children) 
		{
			EntityGraph temp = eg.collide(source, graph);
			if (temp != null) collide = temp;
		}
		
		return collide;
	}
	
	public Entity getEntity()
	{
		return entity;
	}
	
	public void setEntity(Entity e)
	{
		this.entity = e;
	}
	
	public void addEntity(Entity e)
	{
		new EntityGraph(e, this);
	}
	
	public void getDeltaPos(Vector3 deltaPos)
	{
		if (entity == null) return;
		
		entity.readData(pData, Entity.PositionalData.class);
		
		deltaPos.set(pData.position).sub(pData.lastPos);
	}

}
