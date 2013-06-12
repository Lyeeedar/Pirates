package com.Lyeeedar.Entities;

import java.util.HashSet;
import java.util.LinkedList;

import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class EntityGraph {

	private final Entity entity;
	public EntityGraph parent;
	public HashSet<EntityGraph> children;
	
	public EntityGraph(Entity entity, EntityGraph parent)
	{
		this.entity = entity;
		this.parent = parent;
		parent.children.add(this);
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

	public void queueRenderables(Camera cam, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		entity.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
		for (EntityGraph eg : children) eg.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
	}
	
	public void getRunnable(LinkedList<Runnable> list, float delta)
	{
		list.add(entity.getRunnable(delta));
		for (EntityGraph eg : children) eg.getRunnable(list, delta);
	}
	
	public boolean collide(CollisionShape<?> source, long hash)
	{
		if (hash != this.hashCode() && entity.collide(source)) return true;
		
		for (EntityGraph eg : children) eg.collide(source, hash);
		
		return false;
	}
}
