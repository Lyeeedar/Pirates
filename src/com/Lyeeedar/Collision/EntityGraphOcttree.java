package com.Lyeeedar.Collision;

import java.util.LinkedList;
import java.util.List;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Util.Octtree;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class EntityGraphOcttree extends Octtree<EntityGraph> {

	private final BoundingBox bb = new BoundingBox();
	
	public EntityGraphOcttree(Octtree<EntityGraph> parent, Vector3 min, Vector3 max) {
		super(parent, min, max);
	}
	
	@Override
	public Octtree<EntityGraph> getOcttree(Octtree<EntityGraph> parent, Vector3 min, Vector3 max)
	{
		return new EntityGraphOcttree(parent, min, max);
	}
	
	@Override
	public void informPlacement(EntityGraph e, Octtree<EntityGraph> o)
	{
		e.parent = null;
		e.octtree = (EntityGraphOcttree) o;
	}
	
	public void add(Entity e, boolean walkable)
	{
		EntityGraph eg = new EntityGraph(e, null, walkable);
		add(eg, e.getCollisionShapeInternal());
	}
	
	public void insert(EntityGraph eg)
	{
		eg.parent = null;
		add(eg, eg.entity.getCollisionShapeInternal());
	}
	
	public void pop(EntityGraph eg)
	{
		remove(eg);
		eg.octtree = null;
	}
	
	public void queueRenderables(Camera cam, LightManager lights, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		if (numChildElements == 0 && elements.size == 0) return;
		
		if (!cam.frustum.boundsInFrustum(box.getBoundingBox(bb))) return;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements) 
		{ 
			EntityGraph eg = o.e;
			eg.queueRenderables(cam, lights, delta, modelBatch, decalBatch, trailBatch);
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children) 
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			ego.queueRenderables(cam, lights, delta, modelBatch, decalBatch, trailBatch);
		}
	}
	
	public void getText(List<TextParticle> list, SpriteBatch sB, BitmapFont font)
	{
		if (numChildElements == 0 && elements.size == 0) return;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			eg.getText(list, sB, font);
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children) 
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			ego.getText(list, sB, font);
		}
	}
	
	public void getRunnable(LinkedList<Runnable> list, float delta)
	{
		if (numChildElements == 0 && elements.size == 0) return;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			eg.getRunnable(list, delta);
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children) 
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			ego.getRunnable(list, delta);
		}
	}
	
	public void collectDead(List<EntityGraph> list)
	{
		if (numChildElements == 0 && elements.size == 0) return;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			eg.collectDead(list);
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children) 
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			ego.collectDead(list);
		}
	}
	
	public boolean getVisible(Camera cam, List<EntityGraph> list)
	{
		if (numChildElements == 0 && elements.size == 0) return false;
		
		if (!cam.frustum.boundsInFrustum(box.getBoundingBox(bb))) return false;
		
		boolean collide = false;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			if (eg.getVisible(cam, list)) collide = true;
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children) 
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			if (ego.getVisible(cam, list)) collide = true;
		}
		
		return collide;
	}

	public boolean collide(CollisionShape<?> source, EntityGraph graph, List<EntityGraph> list)
	{
		if (numChildElements == 0 && elements.size == 0) return false;
		
		CollisionShape<?> check = source.obtain();
		if (!check.collide(box))
		{
			check.free();
			return false;
		}
		check.free();
		
		boolean collide = false;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			if (eg.collide(source, graph, list)) collide = true;
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children)
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			if (ego.collide(source, graph, list)) collide = true;
		}
		
		return collide;
	}
	
	public EntityGraph collide(CollisionShape<?> source, EntityGraph graph)
	{
		if (numChildElements == 0 && elements.size == 0) return null;
		
		CollisionShape<?> check = source.obtain();
		if (!check.collide(box))
		{
			check.free();
			return null;
		}
		check.free();
		
		EntityGraph collide = null;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			EntityGraph temp = eg.collide(source, graph);
			if (temp != null) collide = temp;
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children)
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			EntityGraph temp = ego.collide(source, graph);
			if (temp != null) collide = temp;
		}
		
		return collide;
	}
	
	public EntityGraph collideWalkables(CollisionShape<?> source, EntityGraph graph)
	{
		if (numChildElements == 0 && elements.size == 0) return null;
		
		CollisionShape<?> check = source.obtain();
		if (!check.collide(box))
		{
			check.free();
			return null;
		}
		check.free();
		
		EntityGraph collide = null;
		
		for (OcttreeEntry<EntityGraph, CollisionShape<?>> o : elements)  
		{ 
			EntityGraph eg = o.e;
			EntityGraph temp = eg.collideWalkables(source, graph);
			if (temp != null) collide = temp;
		}
		
		if (children != null && numChildElements != 0) for (Octtree<EntityGraph> o : children)
		{
			EntityGraphOcttree ego = (EntityGraphOcttree) o;
			EntityGraph temp = ego.collideWalkables(source, graph);
			if (temp != null) collide = temp;
		}
		
		return collide;
	}
}
