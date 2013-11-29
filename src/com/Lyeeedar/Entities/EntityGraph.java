package com.Lyeeedar.Entities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

public class EntityGraph {

	public Entity entity;
	public EntityGraph parent;
	public HashSet<EntityGraph> children = new HashSet<EntityGraph>();
	private final Entity.PositionalData pData = new  Entity.PositionalData();
	public boolean walkable;
	
	public EntityGraph(Entity entity, EntityGraph parent, boolean walkable)
	{
		this.entity = entity;
		this.parent = parent;
		this.walkable = walkable;
		if (parent != null) parent.children.add(this);
		entity.setGraph(this);
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
		if (entity != null && entity.ALIVE < 0.0f)
		{
			list.add(this);
		}
		for (EntityGraph eg : children) {
			eg.collectDead(list);
		}
	}

	public void queueRenderables(Camera cam, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		entity.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
		
		for (EntityGraph eg : children) {
			eg.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
		}
	}
	
	public void getText(List<TextParticle> list, SpriteBatch sB, BitmapFont font)
	{
		if (entity != null && entity.DAMAGE > 0)
		{
			entity.readData(pData, Entity.PositionalData.class);
			Decal decal = ImageUtils.getTextDecal(0.5f, 0.8f, sB, font, ""+entity.DAMAGE);
			decal.setColor(1.0f, 0.0f, 0.0f, 1.0f);
			list.add(new TextParticle(decal, 3.0f, pData.position.add(0, 2, 0), new Vector3(0, 0.6f, 0)));
			entity.DAMAGE = 0;
		}
		for (EntityGraph eg : children) eg.getText(list, sB, font);
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
	
	public EntityGraph collideWalkables(CollisionShape<?> source, EntityGraph graph)
	{
		if (!walkable || graph.equals(this)) return null;
		
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
	
	public void setEntity(Entity e, boolean walkable)
	{
		this.entity = e;
		this.walkable = walkable;
	}
	
	public void addEntity(Entity e, boolean walkable)
	{
		new EntityGraph(e, this, walkable);
	}
	
	public void getDeltaPos(Vector3 deltaPos)
	{
		if (entity == null) return;
		
		entity.readData(pData, Entity.PositionalData.class);
		
		deltaPos.set(pData.position).sub(pData.lastPos);
	}

}
