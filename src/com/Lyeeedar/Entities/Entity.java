package com.Lyeeedar.Entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Collision.BulletWorld.CollisionCallback;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.utils.Array;

public class Entity {
	
	public enum Equipment_Slot
	{
		HEAD,
		BODY,
		TORSO,
		LEGS,
		FEET,
		LARM,
		RARM,
		MISC,
		
		TEMPERATURE,
		LIGHT,
		LIFE,
		GAIA,
		FORCE
	}
	
	private final HashMap<Class<? extends EntityData<?>>, EntityData<? extends EntityData<?>>> entityData = new HashMap<Class<? extends EntityData<?>>, EntityData<? extends EntityData<?>>>();
	
	public final boolean walkable;
	
	private AI ai;
	public static interface AI
	{
		public void update(float delta);
		public void dispose();
	}
	
	private final EntityRunnable runnable = new EntityRunnable(this);
	private final EntityRenderables renderables = new EntityRenderables();
	
	public Entity(boolean walkable, EntityData<?>... data)
	{
		this.walkable = walkable;
		for (EntityData<?> d : data) entityData.put((Class<? extends EntityData<?>>) d.getClass(),  d);
	}
	
	public void addRenderable(Queueable r, Vector3 position)
	{
		renderables.add(new EntityRenderable(r, position));
	}
	
	public void queueRenderables(Camera cam, LightManager lights, float delta, HashMap<Class, Batch> batches)
	{
		renderables.set(this);
		renderables.update(delta, cam, lights);
		renderables.queue(delta, cam, batches);
	}
	
	public void setAI(AI ai)
	{
		this.ai = ai;
		
		if (ai instanceof BehaviourTree)
		{
			((BehaviourTree) ai).setData("entity", this);
		}
	}
	
	public AI getAI() {
		return ai;
	}
	
	public void update(float delta)
	{	
		if (ai != null) ai.update(delta);
		if (entityData.containsKey(EquipmentData.class)) ((EquipmentData) entityData.get(EquipmentData.class)).update(delta, this);
	}
	
	public <E extends EntityData<E>> boolean writeData(E data, Class<E> type)
	{
		if (!entityData.containsKey(type)) return false;
		
		E target = (E) entityData.get(type);
		synchronized(target)
		{
			target.write(data);
		}
		
		return true;
	}	
	public <E extends EntityData<E>> boolean readData(E target, Class<E> type)
	{
		if (!entityData.containsKey(type)) return false;
		
		E data = (E) entityData.get(type);
		synchronized(data)
		{
			data.read(target);
		}
		
		return true;
	}

	public <E extends EntityData<E>> E readOnlyRead(Class<E> type)
	{
		if (!entityData.containsKey(type)) return null;
		
		return (E) entityData.get(type);
	}
	
	public Vector3 getPosition()
	{
		if (entityData.containsKey(PositionalData.class))
		{
			return ((PositionalData)entityData.get(PositionalData.class)).position;
		}
		else
		{
			return ((MinimalPositionalData)entityData.get(MinimalPositionalData.class)).position;
		}
	}
	
	public Runnable getRunnable(float delta)
	{
		runnable.set(delta);
		return runnable;
	}
	public static class EntityRunnable implements Runnable
	{
		public float delta;
		public Entity entity;
		
		public EntityRunnable(Entity e)
		{
			this.entity = e;
			this.delta = 0;
		}
		
		public void set(float delta)
		{
			this.delta = delta;
		}
		
		@Override
		public void run() {
			entity.update(delta);
		}
	}
	
	public static class EntityRenderables 
	{
		private final ArrayList<EntityRenderable> renderables = new ArrayList<EntityRenderable>();
		
		public EntityRenderables()
		{
		}
		
		public void add(EntityRenderable r)
		{
			renderables.add(r);
		}
		
		public void remove(EntityRenderable r)
		{
			renderables.remove(r);
		}
		
		public void set(Entity source)
		{
			for (int i = 0; i < renderables.size(); i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.set(source, r.position);
			}
		}
		
		public void update(float delta, Camera cam, LightManager lights)
		{
			for (int i = 0; i < renderables.size(); i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.update(delta, cam, lights);
			}
		}
		
		public void queue(float delta, Camera cam, HashMap<Class, Batch> batches)
		{
			for (int i = 0; i < renderables.size(); i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.queue(delta, cam, batches);
			}
		}
		
		public void dispose()
		{
			for (int i = 0; i < renderables.size(); i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.dispose();
			}
		}
	}
	
	public void dispose()
	{
		for (EntityData<? extends EntityData<?>> entry : entityData.values())
		{
			entry.dispose();
		}
		renderables.dispose();
		if (ai != null) ai.dispose();
	}
	
	public static class EntityRenderable
	{
		public Queueable renderable;
		public final Vector3 position = new Vector3();
		
		public EntityRenderable(Queueable renderable, Vector3 position)
		{
			this.renderable = renderable;
			this.position.set(position);
		}
	}
	
	public interface EntityData<E extends EntityData<E>>
	{
		public void write(E data);
		public void read(E target);
		public void dispose();
	}
	public static class AnimationData implements EntityData<AnimationData>
	{
		public boolean updateAnimations = false;
		public String anim = "";
		public String base_anim = "";
		public byte animation = 0;
		public float animate_speed = 1f;
		public boolean useDirection = true;
		public boolean animate = true;
		
		public boolean animationLock = false;
		public byte playAnimation = 0;
		public byte nextAnimation = 0;
		public byte startFrame = 0;
		public byte endFrame = 0;
		public AnimationListener listener;
		
		public final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
		public float alpha = 1.0f;

		@Override
		public void write(AnimationData data)
		{
			updateAnimations = data.updateAnimations;
			anim = data.anim;
			base_anim = data.base_anim;
			animation = data.animation;
			animate_speed = data.animate_speed;
			useDirection = data.useDirection;
			animate = data.animate;
			
			animationLock = data.animationLock;
			playAnimation = data.playAnimation;
			nextAnimation = data.nextAnimation;
			startFrame = data.startFrame;
			endFrame = data.endFrame;
			listener = data.listener;
			
			colour.set(data.colour);
			alpha = data.alpha;
		}
		
		@Override
		public void read(AnimationData target)
		{
			target.write(this);
		}

		
		@Override
		public void dispose() {
			
		}
	}
	
	public static class MinimalPositionalData implements EntityData<MinimalPositionalData>
	{
		public final Vector3 position = new Vector3();
		
		@Override
		public void write(MinimalPositionalData data) {
			position.set(data.position);
		}

		@Override
		public void read(MinimalPositionalData target) {
			target.write(this);
		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class PositionalData implements EntityData<PositionalData>
	{
		public enum COLLISION_TYPE
		{
			RAY,
			SHAPE
		}
		public COLLISION_TYPE collisionType = COLLISION_TYPE.RAY;
		
		public enum LOCATION 
		{
			GROUND,
			SEA,
			AIR
		}
		public LOCATION location = LOCATION.AIR;
		
		public final Vector3 lastPos = new Vector3();
		public final Vector3 position = new Vector3();
		public final Vector3 lastRot1 = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 lastRot2 = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 up = new Vector3(GLOBALS.DEFAULT_UP);
		public final Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
		public final Vector3 velocity = new Vector3();
		
		public final Matrix4 composed = new Matrix4();
		public final Matrix4 rotationTra = new Matrix4();
		public final Matrix4 inverse = new Matrix4();
		public final Matrix4 lastInv = new Matrix4();
		
		public boolean Xcollide = false;
		public boolean Ycollide = false;
		public boolean Zcollide = false;
		
		public int jumpToken = 0;
				
		private final Vector3 tmpVec = new Vector3();
		private final Vector3 tmpVec2 = new Vector3();
		private final Matrix4 tmpMat = new Matrix4();
		private final Vector3 v = new Vector3();

		public btRigidBody physicsBody;
		public OcttreeEntry<Entity> octtreeEntry;
		
		public float locationCD = 0;
		
		public ClosestRayResultSkippingCallback ray = new ClosestRayResultSkippingCallback();
		public CollisionCallback collisionCallback;
		
		public Entity base;
		
		@Override
		public void write(PositionalData data)
		{
			collisionType = data.collisionType;
			
			location = data.location;
			
			lastPos.set(data.lastPos);
			position.set(data.position);
			lastRot1.set(data.lastRot1);
			lastRot2.set(data.lastRot2);
			rotation.set(data.rotation);		
			velocity.set(data.velocity);
			up.set(data.up);
			composed.set(data.composed);
			rotationTra.set(data.rotationTra);
			inverse.set(data.inverse);
			lastInv.set(data.lastInv);
			
			Xcollide = data.Xcollide;
			Ycollide = data.Ycollide;
			Zcollide = data.Zcollide;
			
			jumpToken = data.jumpToken;
			scale.set(data.scale);
			
			collisionCallback = data.collisionCallback;
			ray = data.ray;			
			physicsBody = data.physicsBody;
			octtreeEntry = data.octtreeEntry;
			
			base = data.base;
		}
		
		@Override
		public void read(PositionalData target)
		{
			target.write(this);
		}
		
		public void calculateComposed()
		{
			tmpMat.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
			composed.setToTranslationAndScaling(position, scale).mul(tmpMat);
			rotationTra.set(tmpMat).tra();
			inverse.set(rotationTra).scale(1.0f/scale.x, 1.0f/scale.y, 1.0f/scale.z).translate(-position.x, -position.y, -position.z);
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
		
		public void applyVelocity(float delta, float mass)
		{
			lastPos.set(position);
			lastRot2.set(lastRot1);
			lastRot1.set(rotation);
			lastInv.set(inverse);
			
			Xcollide = false;
			Ycollide = false;
			Zcollide = false;
			
			if (collisionType == COLLISION_TYPE.RAY)
			{
				collisionRay(delta, mass);
			}
			else if (collisionType == COLLISION_TYPE.SHAPE)
			{
				collisionShape(delta, mass);
			}
			
			calculateComposed();
			
			physicsBody.setWorldTransform(composed);
			octtreeEntry.box.pos.set(position);
			octtreeEntry.updatePosition();
			
			if (collisionCallback != null && (Xcollide || Ycollide || Zcollide)) collisionCallback.collided(); 
		}
		
		private void collisionShape(float delta, float mass)
		{
			if (velocity.len2() == 0) 
			{
				return;
			}
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta*mass), velocity.z);
			v.scl(delta);
			
			position.add(v);
			velocity.x = 0;
			velocity.y = 0;
			velocity.z = 0;
			
			if (GLOBALS.physicsWorld.collide(physicsBody, BulletWorld.FILTER_COLLISION, BulletWorld.FILTER_COLLISION))
			{
				Xcollide = true;
				Ycollide = true;
				Zcollide = true;
			}
		}
		
		private void collisionRay(float delta, float mass)
		{
			locationCD -= delta;
			
			if (base != null)
			{
				PositionalData pData = base.readOnlyRead(PositionalData.class);
				tmpMat.set(pData.composed).translate(tmpVec.set(position).mul(pData.lastInv));
				position.set(0, 0, 0).mul(tmpMat);
				
				tmpVec.set(pData.lastRot2);
				tmpVec.y = 0; tmpVec.nor();
				tmpVec2.set(pData.rotation);
				tmpVec2.y = 0; tmpVec2.nor();
				tmpMat.setToRotation(tmpVec, tmpVec2);
				
				rotation.mul(tmpMat);
				up.mul(tmpMat);
			}
			
			if (velocity.len2() == 0) 
			{
				return;
			}
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta*mass), velocity.z);
			v.scl(delta);
			
			if (v.x != 0)
			{
				ray.setCollisionObject(null);
	            ray.setClosestHitFraction(1f);

				tmpVec.set(position).add(0, GLOBALS.STEP, 0);
				tmpVec2.set(position).add(v.x, GLOBALS.STEP, 0);
				
				ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
				
				GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
				if (ray.hasHit())
				{
					v.x = 0;
					//position.x = ray.getHitPointWorld().x();
					Xcollide = true;
				}
			}
			
			if (v.z != 0)
			{
				ray.setCollisionObject(null);
	            ray.setClosestHitFraction(1f);

				tmpVec.set(position).add(v.x, GLOBALS.STEP, 0);
				tmpVec2.set(position).add(v.x, GLOBALS.STEP, v.z);
				
				ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
				
				GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
				if (ray.hasHit())
				{
					v.z = 0;
					//position.z = ray.getHitPointWorld().z();
					Zcollide = true;
				}
			}
			
			position.add(v.x, 0, v.z);
			
			velocity.x = 0;
			velocity.z = 0;

			ray.setCollisionObject(null);
            ray.setClosestHitFraction(1f);

			tmpVec.set(position).add(0, GLOBALS.STEP, 0);
			tmpVec2.set(position).add(0, v.y, 0);
			
			ray.getRayFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
			ray.getRayToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
			ray.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
			ray.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
			
			GLOBALS.physicsWorld.world.rayTest(tmpVec, tmpVec2, ray);
			if (ray.hasHit())
			{
				Entity base = (Entity) ray.getCollisionObject().userData;
				if (ray.getHitPointWorld().y() != tmpVec.y)
				{
					if (v.y < 0) jumpToken = 2;
					velocity.y = 0;
					v.y = 0;
					position.y = ray.getHitPointWorld().y();
					//graph.popAndInsert(base);
					location = LOCATION.GROUND;
					locationCD = 0.5f;
					this.base = base.walkable ? base : null ;
				}
				Ycollide = true;
			}
			else
			{
				//graph.popAndInsert(GLOBALS.WORLD);
				if (locationCD < 0)
				{
					location = LOCATION.AIR;
					locationCD = 0.5f;
					this.base = null;
				}
			}
			
			float waveHeight = GLOBALS.SKYBOX.sea.waveHeight(position.x+v.x, position.z+v.z);
			
			if (position.y < waveHeight)
			{
				if (velocity.y < 0) velocity.y = 0;
				if (v.y < 0) v.y = 0;
				position.y =  waveHeight;
				//GLOBALS.sea.modifyVelocity(v, delta, position.x, position.z);
				//graph.popAndInsert(GLOBALS.WORLD);
				location = LOCATION.SEA;
				Ycollide = true;
				this.base = null;
			}
			
			position.add(0, v.y, 0);
			
			float angle = 0;
			Vector3 point = new Vector3(rotation).scl(10).add(position).add(v);
			float waveHeight2 = GLOBALS.SKYBOX.sea.waveHeight(point.x, point.z)-1;
			if (point.y < waveHeight2)
			{
				point.y = waveHeight2;
				angle = (float) Math.atan((point.y-position.y)/10);
			}
			else
			{
				angle = (float) Math.atan((position.y-point.y)/10);
			}
			
			point = new Vector3(rotation).scl(-10).add(position).add(v);
			waveHeight2 = GLOBALS.SKYBOX.sea.waveHeight(point.x, point.z)-1;
			if (point.y < waveHeight2)
			{
				point.y = waveHeight2;
				angle += (float) Math.atan((point.y-position.y)/-10);
			}
			else
			{
				angle += (float) Math.atan((position.y-point.y)/-10);
			}
			angle/=2.0f;
			if (angle > .1f) angle = .1f;
			if (angle < -.1f) angle = -.1f;
			Yrotate(angle);			
		}

		@Override
		public void dispose() {
			ray.dispose();
			if (physicsBody != null) GLOBALS.physicsWorld.remove(physicsBody);
			if (octtreeEntry != null) octtreeEntry.remove();
		}
	}
	public static class EquipmentData implements EntityData<EquipmentData>
	{
		private HashMap<Equipment_Slot, Equipment<?>> equipment = new HashMap<Equipment_Slot, Equipment<?>>();
		private final HashMap<ITEM_TYPE, HashMap<String, Item>> items = new HashMap<ITEM_TYPE, HashMap<String, Item>>();
		
		public EquipmentData()
		{
			for (Equipment_Slot es : Equipment_Slot.values())
			{
				equipment.put(es, null);
			}
			
			for (ITEM_TYPE it : ITEM_TYPE.values())
			{
				items.put(it, new HashMap<String, Item>());
			}
		}
		
		public void update(float delta, Entity entity)
		{
			for (Map.Entry<Equipment_Slot, Equipment<?>> entry : equipment.entrySet())
			{
				Equipment<?> e = entry.getValue();
				if (e != null) e.update(delta, entity);
			}
		}
		
		public void addItem(Item item)
		{
			HashMap<String, Item> hash = items.get(item.description.item_type);
			if (hash.containsKey(item.description.name))
			{
				hash.get(item.description.name).num++;
			}
			else
			{
				hash.put(item.description.name, item);
			}
		}
		
		public void equip(Equipment_Slot slot, Equipment<?> e)
		{
			if (equipment.get(slot) != null)
			{
				equipment.get(slot).equipped = null;
			}
			equipment.put(slot, e);
			e.equipped = slot;
		}
		
		public void unequip(Equipment_Slot slot)
		{
			equipment.get(slot).equipped = null;
			equipment.put(slot, null);
		}
		
		public Equipment<?> getEquipment(Equipment_Slot slot)
		{
			return equipment.get(slot);
		}

		public HashMap<String, Item> getItems(ITEM_TYPE it)
		{
			return items.get(it);
		}
		
		@Override
		public void write(EquipmentData data) {
//			for (Map.Entry<Equipment_Slot, Equipment<?>> entry : data.equipment.entrySet())
//			{
//				@SuppressWarnings("rawtypes")
//				Equipment current = equipment.get(entry.getKey());
//				
//				if (entry.getValue() == null) {
//					if (current != null)
//					{
//						Pools.free(current);
//					}
//					equipment.put(entry.getKey(), null);
//					continue;
//				}
//				
//				if (current == null) 
//				{
//					equipment.put(entry.getKey(), (Equipment<?>) entry.getValue().copy());
//				}
//				else if (current.getClass().equals(entry.getValue().getClass()))
//				{
//					current.set(entry.getValue());
//				}
//				else
//				{
//					Pools.free(current);
//					equipment.put(entry.getKey(), (Equipment<?>) entry.getValue().copy());
//				}
//			}
//			
//			
//			for (ITEM_TYPE it : ITEM_TYPE.values())
//			{
//				HashMap<String, Item> iitems = items.get(it);
//				HashMap<String, Item> ditems = data.items.get(it);
//				
//				for (Item i : iitems.values())
//				{
//					if (!ditems.containsKey(i.description.name))
//					{
//						Item rm = iitems.remove(i.description.name);
//						Pools.free(rm);
//					}
//				}
//				
//				for (Item i : ditems.values())
//				{
//					if (!iitems.containsKey(i.description.name))
//					{
//						iitems.put(i.description.name, i.copy());
//					}
//					else
//					{
//						iitems.get(i.description.name).set(i);
//					}
//				}
//			}
			equipment = data.equipment;
		}

		@Override
		public void read(EquipmentData target) {
			target.write(this);
		}

		public Iterator<Equipment<?>> iterator()
		{
			return equipment.values().iterator();
		}
		
		@Override
		public void dispose() {
			for (Map.Entry<Equipment_Slot, Equipment<?>> entry : equipment.entrySet())
			{
				if (entry.getValue() != null) entry.getValue().dispose();
			}
		}
	}
	public static class StatusData implements EntityData<StatusData>
	{
		public boolean ALIVE = true;
		public int DAMAGED = 0;
		
		public int MAX_HEALTH = 150;
		public int currentHealth = MAX_HEALTH;
		public int damage = 0;
		
		public int speed = 50;
		
		public float mass = 1.0f;
		
		public Array<String> factions = new Array<String>(false, 16);
		
		@Override
		public void write(StatusData data) {
			ALIVE = data.ALIVE;
			DAMAGED = data.DAMAGED;
			
			MAX_HEALTH = data.MAX_HEALTH;
			currentHealth = data.currentHealth;
			damage = data.damage;
			factions.clear();
			factions.addAll(data.factions);
			
			speed = data.speed;
			
			mass = data.mass;
		}
		
		public boolean isAlly(StatusData other)
		{
			boolean ally = false;
			
			for (String faction : other.factions)
			{
				if (factions.contains(faction, false))
				{
					ally = true;
					break;
				}
			}
			
			return ally;
		}
		
		public void applyDamage()
		{
			DAMAGED = damage;
			currentHealth -= damage;
			damage = 0;
		}

		@Override
		public void read(StatusData target) {
			target.write(this);
		}

		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
