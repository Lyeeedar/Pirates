package com.Lyeeedar.Graphics.Particles;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

public class ParticleEffect implements Queueable {
	
	public String name;
	
	public final Array<Emitter> emitters = new Array<Emitter>(false, 16);
	
	public final Vector3 pos = new Vector3();
	public final Matrix4 transform = new Matrix4();
	
	private boolean playing = false;
	private boolean repeat = false;
	
	public float duration;
	private float time;
	
	public ParticleEffect() {
	}
	
	public void play(boolean repeat)
	{
		this.playing = true;
		this.repeat = repeat;
		duration = 0;
		time = 0;
		for (Emitter e : emitters)
		{
			if (e.emitter.duration > duration) duration = e.emitter.duration;
		}
	}
	
	public boolean isPlaying()
	{
		return playing;
	}
	
	public void setPosition(Vector3 pos) {
		setPosition(pos.x, pos.y, pos.z);
	}
	
	public void setPosition(float x, float y, float z) {
		this.pos.set(x, y, z);

		for (Emitter e : emitters)
		{
			e.emitter.setPosition(x+e.x, y+e.y, z+e.z);
		}
	}
	
	public void deleteEmitter(int index)
	{
		Emitter emitter = emitters.removeIndex(index);
		emitter.emitter.dispose();
	}
	
	public void deleteEmitter(String name)
	{
		Iterator<Emitter> itr = emitters.iterator();
		
		while (itr.hasNext())
		{
			Emitter e = itr.next();
			
			if (e.emitter.name.equals(name))
			{
				itr.remove();
				
				e.emitter.dispose();
			}
		}
	}
	
	public ParticleEmitter getEmitter(int index)
	{
		return emitters.get(index).emitter;
	}
	
	public ParticleEmitter getEmitter(String name)
	{
		for (Emitter e : emitters) if (e.emitter.name.equals(name)) return e.emitter;
		
		return null;
	}
	
	public void getEmitters(List<ParticleEmitter> list)
	{
		for (Emitter e : emitters) list.add(e.emitter);
	}
	
	public ParticleEmitter getFirstEmitter()
	{
		return emitters.get(0).emitter;
	}
	
	public void addEmitter(ParticleEmitter emitter,
			float x, float y, float z)
	{
		Emitter e = new Emitter(emitter, x, y, z);
		emitters.add(e);
	}
	
	public void update(float delta, Camera cam)
	{
		if (!playing) return;
		time += delta;
		if (time > duration)
		{
			if (repeat)
			{
				time = 0;
				for (Emitter e : emitters)
				{
					e.emitter.time = 0;
					e.emitter.emissionVal = 0;
				}
			}
			else
			{
				playing = false;
				return;
			}
		}
		duration = 0;
		for (Emitter e : emitters)
		{
			if (e.emitter.duration > duration) duration = e.emitter.duration;
			e.emitter.time = time;
			if (e.emitter.duration < time) continue;
			e.emitter.update(delta, cam);
		}
	}
	
	public Vector3 getEmitterPosition(int index, Vector3 position)
	{
		Emitter e = emitters.get(index);
		return position.set(e.x, e.y, e.z);
	}
	
	public void setEmitterPosition(int index, Vector3 position)
	{
		Emitter e = emitters.get(index);
		e.x = position.x;
		e.y = position.y;
		e.z = position.z;
		
		setPosition(pos);
	}
	
	public void render()
	{
		for (Emitter e : emitters)
		{
			e.emitter.render();
		}
	}
	
	public void getLight(LightManager lightManager)
	{
		for (Emitter e : emitters)
		{
			e.emitter.getLight(lightManager);
		}
	}
	
	public void create()
	{
		for (Emitter e : emitters)
		{
			e.emitter.create();
		}
	}
	
	public void dispose()
	{
		for (Emitter e : emitters)
		{
			e.emitter.dispose();
		}
		time = 0;
		duration = 0;
		playing = false;
		repeat = false;
	}
	
	public ParticleEffect copy()
	{
		ParticleEffect effect = new ParticleEffect();
		effect.name = name;
		if (playing) effect.play(repeat);
		
		for (Emitter e : emitters)
		{
			effect.addEmitter(e.emitter.copy(), e.x, e.y, e.z);
		}
		
		return effect;
	}

	private static class Emitter implements Json.Serializable {

		ParticleEmitter emitter;
		float x;
		float y;
		float z;
		
		@SuppressWarnings("unused")
		public Emitter(){}
		
		public Emitter(ParticleEmitter emitter,
				float x, float y, float z)
		{
			this.emitter = emitter;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public void write(Json json) {
			ParticleEmitter.getJson(json);
			json.writeValue("emitter", emitter);
			json.writeValue("x", x);
			json.writeValue("y", y);
			json.writeValue("z", z);
		}

		@Override
		public void read(Json json, JsonValue jsonData) {
			ParticleEmitter.getJson(json);
			
			x = jsonData.getFloat("x");
			y = jsonData.getFloat("y");
			z = jsonData.getFloat("z");
			
			emitter = json.readValue(ParticleEmitter.class, jsonData.get("emitter"));
		}
	}

	public int getActiveParticles() {
		int active = 0;
		
		for (Emitter e : emitters) active += e.emitter.getActiveParticles();
		
		return active;
	}
	
	public void setBase(Entity base)
	{
		for (Emitter e : emitters)
		{
			e.emitter.base = base;
		}
	}
	
	public void setEmission(float[][] emissionArray, Queueable emissionObject)
	{
		if (emissionArray.length == 0) return;
		for (Emitter e : emitters)
		{
			e.emitter.emissionMesh = emissionArray;
			e.emitter.emissionObject = emissionObject;
		}
	}

	public void setHomeTarget(Queueable target)
	{
		for (Emitter e : emitters)
		{
			e.emitter.homeTarget = target;
		}
	}
	
	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches)
	{
		if (!batches.containsKey(ParticleEffectBatch.class)) return;
		
		if (!playing) return;
		for (Emitter e : emitters)
		{
			if (e.emitter.duration < time) continue;
			if (!e.emitter.created) e.emitter.create();
			if (!cam.frustum.sphereInFrustum(e.emitter.getPosition(), e.emitter.getRadius())) continue;
	
			e.emitter.distance = cam.position.dst2(e.emitter.getPosition());
			
			((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).add(e.emitter);
		}
	}

	@Override
	public void set(Entity source, Matrix4 offset)
	{
		Vector3 tmp = Pools.obtain(Vector3.class);
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			tmp.set(0, 0, 0).mul(source.readOnlyRead(PositionalData.class).composed).mul(offset);
			setBase(source);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			tmp.set(data.position).mul(offset);
		}
		setPosition(tmp);
		Pools.free(tmp);
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights)
	{
		update(delta, cam);
	}

	@Override
	public void set(Matrix4 transform)
	{
		Vector3 tmp = Pools.obtain(Vector3.class);
		tmp.set(0, 0, 0).mul(transform);
		setPosition(tmp);
		Pools.free(tmp);
	}

	@Override
	public void transform(Matrix4 mat)
	{
		Vector3 tmp = Pools.obtain(Vector3.class);
		tmp.set(0, 0, 0).mul(mat);
		pos.add(tmp);
		setPosition(pos);
		Pools.free(tmp);
	}

	@Override
	public Matrix4 getTransform()
	{
		transform.setToTranslation(pos);
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
		return out.set(0, 0, 0).add(pos);
	}

}
