package com.Lyeeedar.Entities;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.BulletWorld.ClosestConvexResultSkippingCallback;
import com.Lyeeedar.Collision.BulletWorld.ClosestRayResultSkippingCallback;
import com.Lyeeedar.Collision.BulletWorld.ContactSensorSkippingCallback;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.Lyeeedar.Entities.Entity.EntityData.EntityDataType;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Entities.Items.Equipment.EquipmentModel;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Entities.Items.Spells.SpellEffect;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.GLOBALS.ELEMENTS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationListener;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestNotMeConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Entity {
	
	public enum Equipment_Slot
	{
		BODY,
		HAIR,
		
		HEAD,
		TORSO,
		LEGS,
		FEET,
		
		LARM, LARMOFF1, LARMOFF2, LARMOFF3,
		RARM, RARMOFF1, RARMOFF2, RARMOFF3
	}
	
	public boolean DISPOSED = false;
	
	private final EnumMap<EntityDataType, EntityData<? extends EntityData<?>>> entityData = new EnumMap<EntityDataType, EntityData<? extends EntityData<?>>>(EntityDataType.class);
	
	public final boolean walkable;
	
	private AI ai;
	public static interface AI
	{
		public void update(float delta);
		public AI copy();
		public void dispose();
	}
	
	private final EntityRunnable runnable = new EntityRunnable(this);
	private final EntityRenderables renderables = new EntityRenderables();
	
	public Entity(boolean walkable, EntityData<?>... data)
	{
		this.walkable = walkable;
		for (EntityData<?> d : data) 
		{
			entityData.put(d.getType(), d);
			d.parent = this;
		}
	}
	
	public Queueable getRenderable(int i)
	{
		return renderables.renderables.get(i).renderable;
	}
	
	public void addRenderable(Queueable r, Vector3 position)
	{
		renderables.add(new EntityRenderable(r, position));
	}
	
	public void queueRenderables(Camera cam, LightManager lights, float delta, HashMap<Class, Batch> batches, boolean update)
	{
		if (DISPOSED) return;
		
		if (entityData.containsKey(EntityDataType.EQUIPMENT)) ((EquipmentData) entityData.get(EntityDataType.EQUIPMENT)).doGraphicsUpdates();
		
		if (update) renderables.set(this);
		if (update) renderables.update(delta, cam, lights);
		renderables.queue(delta, cam, batches);
	}
	
	public void queueRenderables(Camera cam, LightManager lights, float delta, HashMap<Class, Batch> batches)
	{
		this.queueRenderables(cam, lights, delta, batches, true);
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
		if (entityData.containsKey(EntityDataType.EQUIPMENT)) ((EquipmentData) entityData.get(EntityDataType.EQUIPMENT)).update(delta, this);
		if (entityData.containsKey(EntityDataType.STATUS)) ((StatusData) entityData.get(EntityDataType.STATUS)).update(delta, this);
	}
	
	public <E extends EntityData<E>> boolean writeData(E data)
	{
		if (!entityData.containsKey(data.getType())) return false;
		
		E target = (E) entityData.get(data.getType());
		synchronized(target)
		{
			target.write(data);
		}
		
		return true;
	}	
	public <E extends EntityData<E>> boolean readData(E target)
	{
		if (!entityData.containsKey(target.getType())) return false;
		
		E data = (E) entityData.get(target.getType());
		synchronized(data)
		{
			data.read(target);
		}
		
		return true;
	}

	public <E extends EntityData<E>> E readOnlyRead(Class<E> ctype)
	{
		EntityDataType type = EntityData.getTypeFromClass(ctype);
		
		if (!entityData.containsKey(type)) return null;
		
		return (E) entityData.get(type);
	}
	
	public Vector3 getPosition()
	{
		if (entityData.containsKey(EntityDataType.POSITIONAL))
		{
			return ((PositionalData) entityData.get(EntityDataType.POSITIONAL)).position;
		}
		else
		{
			return ((MinimalPositionalData) entityData.get(EntityDataType.MINIMALPOSITIONAL)).position;
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
		private final Array<EntityRenderable> renderables = new Array<EntityRenderable>();
		
		public EntityRenderables()
		{
		}
		
		public void add(EntityRenderable r)
		{
			renderables.add(r);
		}
				
		public void set(Entity source)
		{
			for (int i = 0; i < renderables.size; i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.set(source, r.position);
			}
		}
		
		public void update(float delta, Camera cam, LightManager lights)
		{
			for (int i = 0; i < renderables.size; i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.update(delta, cam, lights);
			}
		}
		
		public void queue(float delta, Camera cam, HashMap<Class, Batch> batches)
		{
			for (int i = 0; i < renderables.size; i++)
			{
				EntityRenderable r = renderables.get(i);
				r.renderable.queue(delta, cam, batches);
			}
		}
		
		public void copy(EntityRenderables other)
		{
			for (int i = 0; i < renderables.size; i++)
			{
				EntityRenderable r = renderables.get(i);
				other.add(new EntityRenderable(r.renderable.copy(), r.position));
			}
		}
		
		public void dispose()
		{
			for (int i = 0; i < renderables.size; i++)
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
		
		DISPOSED = true;
	}

	public Entity copy()
	{
		Collection<EntityData<? extends EntityData<?>>> values = entityData.values();
		EntityData[] nd = new EntityData[values.size()];
		values.toArray(nd);
		for (int i = 0; i < values.size(); i++)
		{
			nd[i] = nd[i].copy();
		}
		
		Entity ne = new Entity(walkable, nd);
		
		if (ai != null) ne.setAI(ai.copy());
		
		renderables.copy(ne.renderables);
		
		return ne;
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
	
	public static abstract class EntityData<E extends EntityData<E>>
	{
		public Entity parent;
		
		public enum EntityDataType
		{
			ANIMATION,
			MINIMALPOSITIONAL,
			POSITIONAL,
			EQUIPMENT,
			STATUS
		}
		
		public static EntityDataType getTypeFromClass(Class type)
		{
			if (type == AnimationData.class)
			{
				return EntityDataType.ANIMATION;
			}
			else if (type == MinimalPositionalData.class)
			{
				return EntityDataType.MINIMALPOSITIONAL;
			}
			else if (type == PositionalData.class)
			{
				return EntityDataType.POSITIONAL;
			}
			else if (type == EquipmentData.class)
			{
				return EntityDataType.EQUIPMENT;
			}
			else if (type == StatusData.class)
			{
				return EntityDataType.STATUS;
			}
			else return null;
		}
		
		public abstract void write(E data);
		public abstract void read(E target);
		public abstract E copy();
		public abstract void dispose();
		public abstract EntityDataType getType();
	}
	public static class AnimationData extends EntityData<AnimationData>
	{
		public boolean updateAnimations = false;
		public String anim = "";
		public String base_anim = "";
		public byte animation = 0;
		public float animate_speed = 1f;
		public boolean useDirection = true;
		public boolean animate = true;
		
		public Object animationLocker;
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
			
			animationLocker = data.animationLocker;
			animationLock = data.animationLock;
			playAnimation = data.playAnimation;
			nextAnimation = data.nextAnimation;
			startFrame = data.startFrame;
			endFrame = data.endFrame;
			listener = data.listener;
			
			colour.set(data.colour);
			alpha = data.alpha;
			
			parent = data.parent;
		}
		
		@Override
		public void read(AnimationData target)
		{
			target.write(this);
		}

		public AnimationData copy()
		{
			AnimationData ed = new AnimationData();
			ed.write(this);
			return ed;
		}
		
		@Override
		public void dispose() {
			
		}

		@Override
		public EntityDataType getType()
		{
			return EntityDataType.ANIMATION;
		}
	}
	public static class MinimalPositionalData extends EntityData<MinimalPositionalData>
	{
		public final Vector3 position = new Vector3();
		
		@Override
		public void write(MinimalPositionalData data) {
			position.set(data.position);
			
			parent = data.parent;
		}

		@Override
		public void read(MinimalPositionalData target) {
			target.write(this);
		}

		public MinimalPositionalData copy()
		{
			MinimalPositionalData ed = new MinimalPositionalData();
			ed.write(this);
			return ed;
		}
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public EntityDataType getType()
		{
			return EntityDataType.MINIMALPOSITIONAL;
		}
	}
	public static class PositionalData extends EntityData<PositionalData>
	{
		public enum COLLISION_TYPE
		{
			COMPLEX,
			SIMPLE
		}
		public COLLISION_TYPE collisionType = COLLISION_TYPE.COMPLEX;
		
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
		public final Vector3 deltaPos = new Vector3();
		public final Vector3 deltaRot = new Vector3();
		public final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
		public final Vector3 up = new Vector3(GLOBALS.DEFAULT_UP);
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
		private final Matrix4 tmpMat2 = new Matrix4();
		private final Vector3 v = new Vector3();

		public boolean needsOffset = true;
		public btRigidBody physicsBody;
		public btConvexShape collisionShape;
		public OcttreeEntry<Entity> octtreeEntry;
		
		public float locationCD = 0;
		
		public ClosestConvexResultSkippingCallback sweep;
		
		public Entity base;
		
		@Override
		public void write(PositionalData data)
		{
			collisionType = data.collisionType;
			
			location = data.location;
			locationCD = data.locationCD;
			
			lastPos.set(data.lastPos);
			position.set(data.position);
			lastRot1.set(data.lastRot1);
			lastRot2.set(data.lastRot2);
			deltaPos.set(data.deltaPos);
			deltaRot.set(data.deltaRot);
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
			
			needsOffset = data.needsOffset;
			sweep = data.sweep;
			physicsBody = data.physicsBody;
			collisionShape = data.collisionShape;
			octtreeEntry = data.octtreeEntry;
			
			base = data.base;
			
			parent = data.parent;
		}
		
		@Override
		public void read(PositionalData target)
		{
			target.write(this);
		}
		
		public PositionalData copy()
		{
			PositionalData ed = new PositionalData();
			ed.write(this);
			if (ed.sweep != null)
			{
				ed.sweep = new ClosestConvexResultSkippingCallback(new Vector3(), new Vector3());
				for (long l : sweep.skipObjects)
				{
					ed.sweep.skipObjects.add(l);
				}
			}
			return ed;
		}
		
		public void calculateComposed()
		{
			tmpMat.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
			composed.setToTranslation(position).mul(tmpMat);
			rotationTra.set(tmpMat).tra();
			inverse.set(rotationTra).translate(-position.x, -position.y, -position.z);
		}
		
		// ------------------------- ROTATE ------------------------- //
		public void Yrotate (float angle) {	
			Vector3 localAxis = tmpVec.set(rotation);
			localAxis.crs(up);
			rotate(localAxis.x, localAxis.y, localAxis.z, angle);
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
		
		public void setRotation(Vector3 rotation)
		{
			setRotation(rotation.x, rotation.y, rotation.z);
		}
		
		public void setRotation(float x, float y, float z)
		{
			rotation.set(x, y, z);
			up.set(rotation).crs(GLOBALS.DEFAULT_UP).crs(rotation);
		}
		// ------------------------- ROTATE ------------------------- //
		
		// ------------------------- MOVE ------------------------- //
		public void left_right(float mag)
		{
			velocity.x += rotation.z * mag;
			velocity.z += -rotation.x * mag;
		}

		public void forward_backward(float mag)
		{
			velocity.x += rotation.x * mag;
			velocity.z += rotation.z * mag;
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
			
			if (collisionType == COLLISION_TYPE.COMPLEX)
			{
				collisionComplex(delta, mass);
			}
			else if (collisionType == COLLISION_TYPE.SIMPLE)
			{
				collisionSimple(delta, mass);
			}
			
			calculateComposed();
			
			float py = needsOffset ? position.y+octtreeEntry.box.extents.y : position.y ;
			tmpMat.setToTranslation(position.x, py, position.z).rotate(GLOBALS.DEFAULT_ROTATION, rotation);
			if (physicsBody != null) physicsBody.setWorldTransform(tmpMat);
			octtreeEntry.box.pos.set(position.x, position.y+octtreeEntry.box.extents.y, position.z);
			octtreeEntry.updatePosition();
		}
		
		public void createCallback()
		{
			sweep = new ClosestConvexResultSkippingCallback(new Vector3(), new Vector3());
			if (physicsBody != null) sweep.setSkipObject(physicsBody);
		}
		
		private void collisionSimple(float delta, float mass)
		{
			if (sweep == null)
			{
				createCallback();
			}
			
			if (velocity.len2() == 0) 
			{
				return;
			}
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta*mass), velocity.z);
			v.scl(delta);
			
			if (v.x != 0 || v.z != 0)
			{
				tmpVec.set(position);
				tmpVec2.set(position).add(v);

				tmpMat.setToTranslation(tmpVec);
				tmpMat2.setToTranslation(tmpVec2);
				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
				sweep.setClosestHitFraction(1f);
				sweep.setHitCollisionObject(null);
				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);

				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);

				if (sweep.hasHit())
				{
					Xcollide = true;
					Ycollide = true;
					Zcollide = true;
					
					v.set(0, 0, 0);
					velocity.set(0, 0, 0);
				}
			}
			
			position.add(v);
			
			float waveHeight = GLOBALS.SKYBOX.sea.waveHeight(position.x, position.z);
			
			if (position.y < waveHeight)
			{
				Xcollide = true;
				Ycollide = true;
				Zcollide = true;
				
				v.set(0, 0, 0);
				velocity.set(0, 0, 0);
				
				position.y = waveHeight;
			}
		}
		
		private void collisionComplex(float delta, float mass)
		{
			if (sweep == null)
			{
				createCallback();
			}
			
			locationCD -= delta;
			
			if (base != null)
			{
				PositionalData pData = base.readOnlyRead(PositionalData.class);
				tmpMat.set(pData.composed).translate(tmpVec.set(position).mul(pData.lastInv));
				position.set(0, 0, 0).mul(tmpMat);
				deltaPos.set(position).sub(lastPos);
				
				tmpVec.set(pData.lastRot2);
				tmpVec.y = 0; tmpVec.nor();
				tmpVec2.set(pData.rotation);
				tmpVec2.y = 0; tmpVec2.nor();
				tmpMat.setToRotation(tmpVec, tmpVec2);
				
				rotation.mul(tmpMat);
				up.mul(tmpMat);
				
				deltaRot.set(rotation).sub(lastRot1);
			}
			
			if (velocity.len2() == 0) 
			{
				return;
			}
			
			v.set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta*mass), velocity.z);
			v.scl(delta);
			
			if (v.x != 0)
			{
				tmpVec.set(position).add(0, GLOBALS.STEP+octtreeEntry.box.extents.y, 0);
				tmpVec2.set(position).add(v.x, GLOBALS.STEP+octtreeEntry.box.extents.y, 0);
				
				tmpMat.setToTranslation(tmpVec);
				tmpMat2.setToTranslation(tmpVec2);
				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
				sweep.setClosestHitFraction(1f);
				sweep.setHitCollisionObject(null);
				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
							
				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);
				
				if (sweep.hasHit())
				{
					float offset = octtreeEntry.box.extents.x + 0.1f;
					//position.x = sweep.getHitPointWorld().x();
					//position.x += (v.x < 0) ? -offset : offset ;
					v.x = 0;
					Xcollide = true;
				}
			}
			
			if (v.z != 0)
			{
				tmpVec.set(position).add(v.x, GLOBALS.STEP+octtreeEntry.box.extents.y, 0);
				tmpVec2.set(position).add(v.x, GLOBALS.STEP+octtreeEntry.box.extents.y, v.z);
				
				tmpMat.setToTranslation(tmpVec);
				tmpMat2.setToTranslation(tmpVec2);
				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
				sweep.setClosestHitFraction(1f);
				sweep.setHitCollisionObject(null);
				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
							
				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);
				
				if (sweep.hasHit())
				{
					float offset = octtreeEntry.box.extents.z + 0.1f;
					//position.z = sweep.getHitPointWorld().z();
					//position.z += (v.z < 0) ? -offset : offset ;
					v.z = 0;
					Zcollide = true;
				}
			}
			
//			if (v.x != 0 || v.z != 0)
//			{
//				tmpVec.set(position).add(0, GLOBALS.STEP+octtreeEntry.box.extents.y, 0);
//				tmpVec2.set(position).add(v.x, GLOBALS.STEP+octtreeEntry.box.extents.y, v.z);
//				
//				tmpMat.setToTranslation(tmpVec);
//				tmpMat2.setToTranslation(tmpVec2);
//				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
//				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
//				sweep.setClosestHitFraction(1f);
//				sweep.setHitCollisionObject(null);
//				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
//				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
//							
//				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);
//				
//				if (sweep.hasHit())
//				{
//					float offset = octtreeEntry.box.extents.x - 0.1f;
//					position.x = sweep.getHitPointWorld().x();
//					position.x += (v.x < 0) ? -offset : offset ;
//					v.x = 0;
//					Xcollide = true;
//					
//					position.z = sweep.getHitPointWorld().z();
//					position.z += (v.z < 0) ? -offset : offset ;
//					v.z = 0;
//					Zcollide = true;
//				}
//			}
			
			position.add(v.x, 0, v.z);
			
			velocity.x = 0;
			velocity.z = 0;
			
			if (v.y < 0)
			{	
				tmpVec.set(position).add(0, GLOBALS.STEP+octtreeEntry.box.extents.y, 0);
				tmpVec2.set(position).add(0, v.y, 0);
	
				tmpMat.setToTranslation(tmpVec);
				tmpMat2.setToTranslation(tmpVec2);
				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
				sweep.setClosestHitFraction(1f);
				sweep.setHitCollisionObject(null);
				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
							
				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);
							
				if (sweep.hasHit())
				{
					Entity base = (Entity) sweep.getHitCollisionObject().userData;
					if (v.y < 0) jumpToken = 2;
					velocity.y = 0;
					v.y = 0;
					position.y = sweep.getHitPointWorld().y();
					location = LOCATION.GROUND;
					locationCD = 0.5f;
					this.base = base.walkable ? base : null ;
				}
				else
				{
					if (locationCD < 0)
					{
						location = LOCATION.AIR;
						locationCD = 0.8f;
						this.base = null;
					}
				}			
			}
			else if (v.y > 0)
			{
				tmpVec.set(position).add(0, octtreeEntry.box.extents.y, 0);
				tmpVec2.set(position).add(0, octtreeEntry.box.extents.y+v.y, 0);
	
				tmpMat.setToTranslation(tmpVec);
				tmpMat2.setToTranslation(tmpVec2);
				sweep.setCollisionFilterMask(BulletWorld.FILTER_COLLISION);
				sweep.setCollisionFilterGroup(BulletWorld.FILTER_COLLISION);
				sweep.setClosestHitFraction(1f);
				sweep.setHitCollisionObject(null);
				sweep.getConvexFromWorld().setValue(tmpVec.x, tmpVec.y, tmpVec.z);
				sweep.getConvexToWorld().setValue(tmpVec2.x, tmpVec2.y, tmpVec2.z);
							
				GLOBALS.physicsWorld.world.convexSweepTest(collisionShape, tmpMat, tmpMat2, sweep);
							
				if (sweep.hasHit())
				{
					velocity.y = 0;
					v.y = 0;
					position.y = sweep.getHitPointWorld().y()-(octtreeEntry.box.extents.y*2.0f);
				}
				
				location = LOCATION.AIR;
				locationCD = 0.8f;
				this.base = null;
			}
			
			position.add(0, v.y, 0);
			
			float waveHeight = GLOBALS.SKYBOX.sea.waveHeight(position.x, position.z);
			
			if (position.y < waveHeight)
			{
				velocity.y = 0;
				v.y = 0;
				position.y = waveHeight;
				//GLOBALS.sea.modifyVelocity(v, delta, position.x, position.z);
				//graph.popAndInsert(GLOBALS.WORLD);
				locationCD = 0.5f;
				location = LOCATION.SEA;
				Ycollide = true;
				this.base = null;
			}
			
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
			//if (ray != null) ray.dispose();
		}
	
		@Override
		public EntityDataType getType()
		{
			return EntityDataType.POSITIONAL;
		}
	}
	public static class EquipmentData extends EntityData<EquipmentData>
	{
		private EnumMap<Equipment_Slot, Equipment<?>> equipment = new EnumMap<Equipment_Slot, Equipment<?>>(Equipment_Slot.class);
		private EnumMap<ITEM_TYPE, Array<Item>> items = new EnumMap<ITEM_TYPE, Array<Item>>(ITEM_TYPE.class);
		
		public AnimatedModel am;
		
		private Array<Object[]> needsEquip = new Array<Object[]>(false, 16);
		
		public EquipmentData()
		{
			for (Equipment_Slot es : Equipment_Slot.values())
			{
				equipment.put(es, null);
			}
			
			for (ITEM_TYPE it : ITEM_TYPE.values())
			{
				items.put(it, new Array<Item>(false, 16));
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
		
		public void doGraphicsUpdates()
		{
			for (Object[] pair : needsEquip)
			{
				doEquip( (Equipment_Slot) pair[0], (Equipment<?>) pair[1]);
			}
			needsEquip.clear();
		}
		
		public void addItem(Item item)
		{
			Array<Item> hash = items.get(item.description.item_type);
			Item found = null;
			
			if (item.stackable)
			{
				for (Item i : hash)
				{
					if (i.description.name.equals(item.description.name))
					{
						found = i;
						break;
					}
				}
			}
			
			if (found != null)
			{
				found.num++;
			}
			else
			{
				hash.add(item);
			}
		}
		
		public void equip(Equipment_Slot slot, Equipment<?> e)
		{
			needsEquip.add(new Object[]{slot, e});
		}
		
		private void doEquip(Equipment_Slot slot, Equipment<?> e)
		{
			doUnequip(slot);
			if (e == null) return;
			equipment.put(slot, e);			
			e.equipped = slot;
			
			if (
					slot == Equipment_Slot.LARMOFF1 || slot == Equipment_Slot.LARMOFF2 || slot == Equipment_Slot.LARMOFF3 ||
					slot == Equipment_Slot.RARMOFF1 || slot == Equipment_Slot.RARMOFF2 || slot == Equipment_Slot.RARMOFF3
					)
			{
				return;
			}
			
			parent.readOnlyRead(StatusData.class).add(e.statusModifier);
			
			if (am != null)
			{
				Array<String> textures = Pools.obtain(Array.class);
				textures.clear();
				for (Equipment<?> equip : equipment.values())
				{
					if (equip != null && equip.equipmentGraphics != null)
					{
						textures.addAll(equip.equipmentGraphics.textureNames);
					}
				}
				am.textures = FileUtils.getTextureArray(textures);
				textures.clear();
				Pools.free(textures);
				
				if (e.equipmentGraphics != null) for (EquipmentModel em : e.equipmentGraphics.models)
				{
					AnimatedModel nam = new AnimatedModel(em.modelName, FileUtils.loadModel(em.modelName), FileUtils.getTextureArray(em.textureNames), em.colour, em.defaultAnim);
					am.attach(em.nodeName, nam, em.transform, em.modelName+em.nodeName);
					e.addRequiredQueueables(nam);
				}
			}
		}
		
		private void doUnequip(Equipment_Slot slot)
		{
			Equipment<?> e = equipment.get(slot);
			if (e == null) return;
			e.equipped = null;
			equipment.put(slot, null);
			
			if (
					slot == Equipment_Slot.LARMOFF1 || slot == Equipment_Slot.LARMOFF2 || slot == Equipment_Slot.LARMOFF3 ||
					slot == Equipment_Slot.RARMOFF1 || slot == Equipment_Slot.RARMOFF2 || slot == Equipment_Slot.RARMOFF3
					)
			{
				return;
			}
			
			parent.readOnlyRead(StatusData.class).remove(e.statusModifier);
			
			if (am != null)
			{			
				Array<String> textures = Pools.obtain(Array.class);
				textures.clear();
				for (Equipment<?> equip : equipment.values())
				{
					if (equip != null && equip.equipmentGraphics != null)
					{
						textures.addAll(equip.equipmentGraphics.textureNames);
					}
				}
				am.textures = FileUtils.getTextureArray(textures);
				textures.clear();
				Pools.free(textures);
				
				if (e.equipmentGraphics != null) for (EquipmentModel em : e.equipmentGraphics.models)
				{
					Queueable q = am.remove(em.modelName+em.nodeName);
					q.dispose();
				}			
			}
		}
		
		public Equipment<?> getEquipment(Equipment_Slot slot)
		{
			return equipment.get(slot);
		}

		public Array<Item> getItems(ITEM_TYPE it)
		{
			return items.get(it);
		}
		
		@Override
		public void write(EquipmentData data) {
			equipment = data.equipment;
			items = data.items;
			am = data.am;
			needsEquip = data.needsEquip;
			
			parent = data.parent;
		}

		@Override
		public void read(EquipmentData target) {
			target.write(this);
		}

		public EquipmentData copy()
		{
			EquipmentData ed = new EquipmentData();
			ed.write(this);
			return ed;
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
	
		@Override
		public EntityDataType getType()
		{
			return EntityDataType.EQUIPMENT;
		}
	}
	public static class StatusData extends EntityData<StatusData>
	{
		public static enum STATS
		{
			MAXHEALTH,
			SPEED,
			MASS
		}
		
		public String name;
		
		public boolean ALIVE = true;
		public int DAMAGED = 0;
		public int damage = 0;
		public int currentHealth = 0;
		
		public final EnumMap<STATS, Integer> stats = new EnumMap<STATS, Integer>(STATS.class);
		public final EnumMap<ELEMENTS, Integer> defense = new EnumMap<ELEMENTS, Integer>(ELEMENTS.class);
		public final EnumMap<ELEMENTS, Integer> attack = new EnumMap<ELEMENTS, Integer>(ELEMENTS.class);
		
		public boolean solid = false;
		public boolean blocking = false;
		
		public Array<String> factions = new Array<String>(false, 16);
		
		private final Array<SpellEffect> spellEffects = new Array<SpellEffect>();
		
		public StatusData()
		{
			for (STATS stat : STATS.values())
			{
				stats.put(stat, 0);
			}
			
			for (ELEMENTS element : ELEMENTS.values())
			{
				defense.put(element, 0);
				attack.put(element, 0);
			}
		}
		
		public void addSpellEffect(SpellEffect se)
		{
			spellEffects.add(se);
		}
		
		public void update(float delta, Entity parent)
		{
			for (int i = 0; i < spellEffects.size; i++)
			{
				SpellEffect se = spellEffects.get(i);
				boolean alive = se.update(delta, parent);
				
				if (!alive)
				{
					spellEffects.removeIndex(i);
					i--;
				}
			}
		}
		
		public void setAttack(int FIRE, int WATER, int EARTH, int WOOD, int METAL)
		{
			attack.put(ELEMENTS.FIRE, FIRE);
			attack.put(ELEMENTS.WATER, WATER);
			attack.put(ELEMENTS.EARTH, EARTH);
			attack.put(ELEMENTS.WOOD, WOOD);
			attack.put(ELEMENTS.METAL, METAL);
		}
		
		public void setDefense(int FIRE, int WATER, int EARTH, int WOOD, int METAL)
		{
			defense.put(ELEMENTS.FIRE, FIRE);
			defense.put(ELEMENTS.WATER, WATER);
			defense.put(ELEMENTS.EARTH, EARTH);
			defense.put(ELEMENTS.WOOD, WOOD);
			defense.put(ELEMENTS.METAL, METAL);
		}
		
		public void add(StatusData data)
		{
			combine(data, 1);
		}
		
		public void remove(StatusData data)
		{
			combine(data, -1);
		}
		
		private void combine(StatusData data, int sf)
		{
			for (STATS stat : STATS.values())
			{
				stats.put(stat, stats.get(stat) + data.stats.get(stat) * sf);
			}
			
			for (ELEMENTS element : ELEMENTS.values())
			{
				defense.put(element, defense.get(element) + data.defense.get(element) * sf);
				attack.put(element, attack.get(element) + data.attack.get(element) * sf);
			}
		}
		
		@Override
		public void write(StatusData data) {
			
			name = data.name;
			
			ALIVE = data.ALIVE;
			DAMAGED = data.DAMAGED;
			
			currentHealth = data.currentHealth;
			damage = data.damage;
			factions.clear();
			factions.addAll(data.factions);
			
			spellEffects.clear();
			spellEffects.addAll(data.spellEffects);
			
			blocking = data.blocking;
			
			for (STATS stat : STATS.values())
			{
				stats.put(stat, data.stats.get(stat));
			}
			
			for (ELEMENTS element : ELEMENTS.values())
			{
				defense.put(element, data.defense.get(element));
				attack.put(element, data.attack.get(element));
			}
			
			parent = data.parent;
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
		
		public void damage(int percentage, EnumMap<ELEMENTS, Integer> attackPower)
		{
			for (ELEMENTS element : ELEMENTS.values())
			{
				damage += Math.max( 0, (int) (( (float) attackPower.get(element) / 100.0f ) * (float) percentage - (float) defense.get(element) ) );
			}
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

		public StatusData copy()
		{
			StatusData ed = new StatusData();
			ed.write(this);
			return ed;
		}
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public EntityDataType getType()
		{
			return EntityDataType.STATUS;
		}
	}

}
