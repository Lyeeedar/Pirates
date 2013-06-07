package com.Lyeeedar.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Entities.AI.AI_Package;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Renderable;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Informable;
import com.Lyeeedar.Util.ThreadSafePlane;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

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
	
	public Entity()
	{
		entityData.put(PositionalData.class, new PositionalData());
		entityData.put(AnimationData.class, new AnimationData());
		entityData.put(EquipmentData.class, new EquipmentData());
		entityData.put(StatusData.class, new StatusData());
	}
	
	public void addRenderable(Renderable r, Equipment_Slot slot)
	{
		renderables.add(r, slot);
	}
	
	public void queueRenderables(Camera cam, float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
	{
		renderables.set(this);
		renderables.update(delta, cam);
		renderables.queue(delta, modelBatch, decalBatch, trailBatch);
	}
	
	public void setAI(AI_Package ai)
	{
		this.ai = ai;
	}
	
	public void update(float delta)
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
		private final HashMap<Equipment_Slot, ArrayList<Renderable>> renderables = new HashMap<Equipment_Slot, ArrayList<Renderable>>();
		
		public EntityRenderables()
		{
			for (Equipment_Slot es : Equipment_Slot.values())
			{
				renderables.put(es, new ArrayList<Renderable>());
			}
		}
		
		public void add(Renderable r, Equipment_Slot slot)
		{
			renderables.get(slot).add(r);
		}
		
		public void set(Entity source)
		{
			for (Map.Entry<Equipment_Slot, ArrayList<Renderable>> entry : renderables.entrySet())
			{
				for (Renderable r : entry.getValue())
				{
					r.set(source);
				}
			}
		}
		
		public void update(float delta, Camera cam)
		{
			for (Map.Entry<Equipment_Slot, ArrayList<Renderable>> entry : renderables.entrySet())
			{
				for (Renderable r : entry.getValue())
				{
					r.update(delta, cam);
				}
			}
		}
		
		public void queue(float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch)
		{
			for (Map.Entry<Equipment_Slot, ArrayList<Renderable>> entry : renderables.entrySet())
			{
				for (Renderable r : entry.getValue())
				{
					r.queue(delta, modelBatch, decalBatch, trailBatch);
				}
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
		
		public int jumpToken = 0;
		
		private final Vector3 tmpVec = new Vector3();
		private final Matrix4 tmpMat = new Matrix4();
		private final Vector3 nPos = new Vector3();
		private final Vector3 v = new Vector3();
		private final Vector3 dest = new Vector3();
		private final Ray ray = new Ray(new Vector3(), new Vector3());
		private final Vector3 collision = new Vector3();
		private final float[] min_dist = {Float.MAX_VALUE};
		private final Vector3[] tmp = {new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3()};
		private final Plane plane = new ThreadSafePlane(new Vector3(), 1);
		
		public void write(PositionalData data)
		{
			position.set(data.position);
			rotation.set(data.rotation);		
			velocity.set(data.velocity);
			up.set(data.up);
			jumpToken = data.jumpToken;
			radius = data.radius;
			radius2 = data.radius2;
			radius2y = data.radius2y;
		}
		
		public void read(PositionalData target)
		{
			target.write(this);
		}
		
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
		}
		
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
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(v.x, 0, 0).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.x != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, position.y, position.z), collision, min_dist, tmp, plane) && min_dist[0] < radius2)
			{
				velocity.x = 0;
				v.x = 0;
			}
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(0, 0, v.z).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.z != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, position.y, nPos.z), collision, min_dist, tmp, plane) && min_dist[0]  < radius2)
			{
				velocity.z = 0;
				v.z = 0;
			}
			
			ray.origin.set(position).add(0, GLOBALS.STEP, 0);
			nPos.set(position).add(v);
			ray.direction.set(0, v.y, 0).nor();
			min_dist[0] = Float.MAX_VALUE;

			if (v.y != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(ray, dest.set(nPos.x, nPos.y, nPos.z), collision, min_dist, tmp, plane) && min_dist[0]  < radius2y)
			{
				if (v.y < 0) jumpToken = 2;
				velocity.y = 0;
				v.y = 0;
				position.y = collision.y;
			}
			else if (nPos.y < -0.5f)
			{
				velocity.y = 0;
				v.y = 0;
				position.y = -0.5f;
				jumpToken = 2;
			}
			
			position.add(v.x, v.y, v.z);
			
			velocity.x = 0;
			velocity.z = 0;
		}
	}
	public static class EquipmentData implements EntityData<EquipmentData>
	{
		
		private final HashMap<Equipment_Slot, ArrayList<Equipment<?>>> equipment = new HashMap<Equipment_Slot, ArrayList<Equipment<?>>>();
		
		public EquipmentData()
		{
			for (Equipment_Slot es : Equipment_Slot.values())
			{
				equipment.put(es, new ArrayList<Equipment<?>>());
			}
		}

		@Override
		public void write(EquipmentData data) {
			equipment.clear();
			for (Map.Entry<Equipment_Slot, ArrayList<Equipment<?>>> entry : data.equipment.entrySet())
			{
				ArrayList<Equipment<?>> equip = new ArrayList<Equipment<?>>();
				equipment.put(entry.getKey(), equip);
				for (Equipment<?> e : entry.getValue())
				{
					equip.add(e.copy());
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
