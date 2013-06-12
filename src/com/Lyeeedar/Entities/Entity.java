package com.Lyeeedar.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Entities.AI.AI_Package;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Renderable;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Informable;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Entity {
	
	public enum Equipment_Slot
	{
		HEAD,
		BODY,
		LEGS,
		FEET,
		LARM,
		RARM,
		MISC
	}
	
	private final HashMap<Class<? extends EntityData<?>>, EntityData<? extends EntityData<?>>> entityData = new HashMap<Class<? extends EntityData<?>>, EntityData<? extends EntityData<?>>>();
	
	private AI_Package ai;
	private final EntityRunnable runnable = new EntityRunnable();
	private final EntityRenderables renderables = new EntityRenderables();
	private EntityGraph entityGraph;
	private CollisionShape<?> collisionShapeInternal;
	private CollisionShape<?> collisionShapeExternal;
	
	public Entity()
	{
		entityData.put(PositionalData.class, new PositionalData());
		entityData.put(AnimationData.class, new AnimationData());
		entityData.put(EquipmentData.class, new EquipmentData());
		entityData.put(StatusData.class, new StatusData());
	}
	
	public void setCollisionShapeInternal(CollisionShape<?> internal)
	{
		this.collisionShapeInternal = internal;
	}
	
	public void setCollisionShapeExternal(CollisionShape<?> external)
	{
		this.collisionShapeExternal = external;
		PositionalData data = new PositionalData();
		readData(data, PositionalData.class);
		data.shape = external.copy();
		writeData(data, PositionalData.class);
	}
	
	public void setCollisionShape(CollisionShape<?> shape)
	{
		setCollisionShapeExternal(shape);
		setCollisionShapeInternal(shape);
	}
	
	public boolean collide(CollisionShape<?> collide)
	{
		return collisionShapeInternal.collide(collide);
	}
	
	public void setGraph(EntityGraph eg)
	{
		this.entityGraph = eg;
		PositionalData data = new PositionalData();
		readData(data, PositionalData.class);
		data.graphHash = entityGraph.hashCode();
		writeData(data, PositionalData.class);
	}
	
	public void addRenderable(Renderable r)
	{
		renderables.add(r);
	}
	
	public void removeRenderable(Renderable r)
	{
		renderables.remove(r);
	}
	
	public void queueRenderables(Camera cam, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		((EquipmentData) entityData.get(EquipmentData.class)).update(delta);
		
		renderables.set(this);
		renderables.update(delta, cam);
		renderables.queue(delta, modelBatch, decalBatch, trailBatch);
	}
	
	public void setAI(AI_Package ai)
	{
		this.ai = ai;
	}
	
	private void update(float delta)
	{
		ai.update(delta);
	}
	
	public <E extends EntityData<E>> void writeData(E data, Class<E> type)
	{
		@SuppressWarnings("unchecked")
		E target = (E) entityData.get(type);
		synchronized(target)
		{
			target.write(data);
		}
	}	
	public <E extends EntityData<E>> E readData(E target, Class<E> type)
	{
		@SuppressWarnings("unchecked")
		E data = (E) entityData.get(type);
		synchronized(data)
		{
			data.read(target);
		}
		
		return target;
	}
	
	public Runnable getRunnable(float delta)
	{
		runnable.set(delta, this);
		return runnable;
	}
	public static class EntityRunnable implements Runnable
	{
		float delta;
		Entity entity;
		
		public void set(float delta, Entity entity)
		{
			this.entity = entity;
			this.delta = delta;
		}
		
		@Override
		public void run() {
			entity.update(delta);
		}
	}
	
	public static class EntityRenderables 
	{
		private final ArrayList<Renderable> renderables = new ArrayList<Renderable>();
		
		public EntityRenderables()
		{
		}
		
		public void add(Renderable r)
		{
			renderables.add(r);
		}
		
		public void remove(Renderable r)
		{
			renderables.remove(r);
		}
		
		public void set(Entity source)
		{
			for (Renderable r : renderables)
			{
				r.set(source);
			}
		}
		
		public void update(float delta, Camera cam)
		{
			for (Renderable r : renderables)
			{
				r.update(delta, cam);
			}
		}
		
		public void queue(float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
		{
			for (Renderable r : renderables)
			{
				r.queue(delta, modelBatch, decalBatch, trailBatch);
			}
		}
	}
	
	public interface EntityData<E extends EntityData<E>>
	{
		public void write(E data);
		public void read(E target);
	}
	public static class AnimationData implements EntityData<AnimationData>
	{
		public boolean updateAnimations = false;
		public String anim = "";
		public byte animation = 0;
		public float animate_speed = 0.1f;
		public boolean useDirection = true;
		public boolean animate = true;
		
		public boolean animationLock = false;
		public String playAnim = "";
		public byte playAnimation = 0;
		public String nextAnim = "";
		public byte nextAnimation = 0;
		public byte startFrame = 0;
		public byte endFrame = 0;
		public Informable informable;

		public void write(AnimationData data)
		{
			updateAnimations = data.updateAnimations;
			anim = data.anim;
			animation = data.animation;
			animate_speed = data.animate_speed;
			useDirection = data.useDirection;
			animate = data.animate;
			
			animationLock = data.animationLock;
			playAnim = data.playAnim;
			playAnimation = data.playAnimation;
			nextAnim = data.nextAnim;
			nextAnimation = data.nextAnimation;
			startFrame = data.startFrame;
			endFrame = data.endFrame;
			informable = data.informable;
		}
		
		public void read(AnimationData target)
		{
			target.write(this);
		}
	}
	public static class PositionalData implements EntityData<PositionalData>
	{
		public float radius = 0.5f;
		public float radius2 = radius*radius;
		public float radius2y = (radius+GLOBALS.STEP)*(radius+GLOBALS.STEP);
		
		public void updateRadius(float radius)
		{
			this.radius = radius;
			this.radius2 = radius * radius;
			this.radius2y = (radius+GLOBALS.STEP)*(radius+GLOBALS.STEP);
		}
		
		public final Vector3 position = new Vector3();
		public final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 up = new Vector3(GLOBALS.DEFAULT_UP);	
		public final Vector3 velocity = new Vector3();
		public final Matrix4 composed = new Matrix4();
		
		public int jumpToken = 0;
		
		private final Vector3 tmpVec = new Vector3();
		private final Matrix4 tmpMat = new Matrix4();
		private final Vector3 v = new Vector3();
		
		public long graphHash;
		
		public CollisionShape<?> shape;
		
		public void write(PositionalData data)
		{
			position.set(data.position);
			rotation.set(data.rotation);		
			velocity.set(data.velocity);
			up.set(data.up);
			composed.set(data.composed);
			jumpToken = data.jumpToken;
			radius = data.radius;
			radius2 = data.radius2;
			radius2y = data.radius2y;
			graphHash = data.graphHash;
			shape = data.shape.copy();
		}
		
		public void read(PositionalData target)
		{
			target.write(this);
		}
		
		public void calculateComposed()
		{
			tmpMat.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
			composed.setToTranslation(position).mul(tmpMat);
		}
		
		// ------------------------- ROTATE ------------------------- //
		public void Yrotate (float angle) {	
			Vector3 dir = tmpVec.set(rotation).nor();
			if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
			{
				Vector3 localAxisX = dir.set(rotation);
				localAxisX.crs(up).nor();
				rotate(localAxisX.x, localAxisX.y, localAxisX.z, angle);
			}
		}
		public void Xrotate (float angle) {
			rotate(0, 1, 0, angle);
		}

		public void rotate (float x, float y, float z, float angle) {
			Vector3 axis = tmpVec.set(x, y, z);
			tmpMat.setToRotation(axis, angle);
			rotation.mul(tmpMat).nor();
			up.mul(tmpMat).nor();
			
			calculateComposed();
		}
		// ------------------------- ROTATE ------------------------- //
		
		// ------------------------- MOVE ------------------------- //
		public void left_right(float mag)
		{
			velocity.x += (float)Math.sin(rotation.z) * mag;
			velocity.z += -(float)Math.sin(rotation.x) * mag;
		}

		public void forward_backward(float mag)
		{
			velocity.x += (float)Math.sin(rotation.x) * mag;
			velocity.z += (float)Math.sin(rotation.z) * mag;
		}
		// ------------------------- MOVE ------------------------- //
		
		public void applyVelocity(float delta)
		{
			if (velocity.len2() == 0) return;
			
			if (velocity.x < -GLOBALS.MAX_SPEED_X) velocity.x = -GLOBALS.MAX_SPEED_X;
			else if (velocity.x > GLOBALS.MAX_SPEED_X) velocity.x = GLOBALS.MAX_SPEED_X;
			
			if (velocity.y < -GLOBALS.MAX_SPEED_Y) velocity.y = -GLOBALS.MAX_SPEED_Y;
			else if (velocity.y > GLOBALS.MAX_SPEED_Y) velocity.y = GLOBALS.MAX_SPEED_Y;
			
			if (velocity.z < -GLOBALS.MAX_SPEED_Z) velocity.z = -GLOBALS.MAX_SPEED_Z;
			else if (velocity.z > GLOBALS.MAX_SPEED_Z) velocity.z = GLOBALS.MAX_SPEED_Z;
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta), velocity.z);
			v.scl(delta);
			
			CollisionShape<?> s1 = shape.obtain();
			
			s1.setPosition(tmpVec.set(position).add(v.x, GLOBALS.STEP, 0));

			if (v.x != 0 && GLOBALS.WORLD.collide(shape, graphHash))
			{
				velocity.x = 0;
				v.x = 0;
			}
			
			s1.setPosition(tmpVec.set(position).add(v.x, GLOBALS.STEP, v.z));

			if (v.z != 0 && GLOBALS.WORLD.collide(shape, graphHash))
			{
				velocity.z = 0;
				v.z = 0;
			}
			
			CollisionRay ray = Pools.obtain(CollisionRay.class);
			ray.dist = Float.MAX_VALUE;
			ray.intersection.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
			ray.ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			ray.ray.direction.set(0, v.y, 0).nor();

			if (v.y != 0 && GLOBALS.WORLD.collide(ray, graphHash) && ray.dist < radius2y)
			{
				if (v.y < 0) jumpToken = 2;
				velocity.y = 0;
				v.y = 0;
				position.y = ray.intersection.y;
			}
			else if (position.y-v.y < -0.5f)
			{
				velocity.y = 0;
				v.y = 0;
				position.y = -0.5f;
				jumpToken = 2;
			}
			
			position.add(v.x, v.y, v.z);
			
			velocity.x = 0;
			velocity.z = 0;
			
			calculateComposed();
		}
	}
	public static class EquipmentData implements EntityData<EquipmentData>
	{
		private final HashMap<Equipment_Slot, Equipment<?>> equipment = new HashMap<Equipment_Slot, Equipment<?>>();
		
		public EquipmentData()
		{
			for (Equipment_Slot es : Equipment_Slot.values())
			{
				equipment.put(es, null);
			}
		}
		
		public void update(float delta)
		{
			for (Map.Entry<Equipment_Slot, Equipment<?>> entry : equipment.entrySet())
			{
				Equipment<?> e = entry.getValue();
				if (e != null) e.update(delta);
			}
		}
		
		public boolean addEquipment(Equipment_Slot slot, Equipment<?> e)
		{
			boolean r = false;
			if (equipment.get(slot) != null)
			{
				r = true;
			}
			
			equipment.put(slot,  e);
			
			return r;
		}
		
		public Equipment<?> getEquipment(Equipment_Slot slot)
		{
			return equipment.get(slot);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void write(EquipmentData data) {
			for (Map.Entry<Equipment_Slot, Equipment<?>> entry : data.equipment.entrySet())
			{
				if (entry.getValue() == null) {
					equipment.put(entry.getKey(), null);
					continue;
				}
				
				@SuppressWarnings("rawtypes")
				Equipment current = equipment.get(entry.getKey());
				
				if (current == null) 
				{
					equipment.put(entry.getKey(), entry.getValue().copy());
				}
				else if (current.getClass().equals(entry.getValue().getClass()))
				{
					current.set(entry.getValue());
				}
				else
				{
					equipment.put(entry.getKey(), entry.getValue().copy());
				}
			}
		}

		@Override
		public void read(EquipmentData target) {
			target.write(this);
		}
	}
	public static class StatusData implements EntityData<StatusData>
	{

		@Override
		public void write(StatusData data) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void read(StatusData target) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
