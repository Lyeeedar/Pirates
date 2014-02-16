/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Graphics.Particles;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.Queueable;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pools;

public class ParticleEmitter implements Comparable<ParticleEmitter> {

	private transient static final int VERTEX_SIZE = 9;

	public enum ParticleAttribute {
		SPRITE,
		SIZE,
		COLOUR,
		VELOCITY,
		EMISSIONRATE,
		EMISSIONAREA,
		EMISSIONTYPE,
		MASS,
		ROTATIONTYPE
	}

	public final String UID;
	public String name;

	// ----- Particle Parameters ----- //
	private EnumMap<ParticleAttribute, TimelineValue[]> timelines = new EnumMap<ParticleAttribute, TimelineValue[]>(ParticleAttribute.class);
	// ----- End Particle Parameters ----- //

	// ----- Emitter parameters ----- //
	public int maxParticles;
	public float duration;
	public float particleLifetime;
	public int blendFuncSRC;
	public int blendFuncDST;
	public String atlasName;
	// ----- End Emitter parameters ----- //

	// ----- Light ----- //
	private float lightAttenuation;
	private float lightPower;
	private boolean isLightStatic;
	private Color lightColour;
	private boolean lightFlicker;
	private float lightx, lighty, lightz;
	// ----- End Light ----- //

	// ----- Transient Variables ----- //
	public transient float distance = 0;
	private transient static ShaderProgram shader;
	private transient static String currentAtlas;
	private transient static int currentBlendSRC, currentBlendDST;
	public transient TextureAtlas atlas;
	public transient Texture atlasTexture;
	public transient int texHash;
	private transient float[][] topLeftTexCoords;
	private transient float[][] topRightTexCoords;
	private transient float[][] botLeftTexCoords;
	private transient float[][] botRightTexCoords;
	private transient Array<Particle> active;
	private transient Array<Particle> inactive;
	private transient Vector3 quad;
	private transient float[] vertices;
	private transient Mesh mesh;
	private transient Random ran;
	private transient Matrix4 tmpMat;
	private transient Matrix4 tmpRot;
	private transient Vector3 tmpVec;
	private transient Vector3 tmpVec2;
	private transient Vector3 pos;
	private transient float signx;
	private transient float signy;
	private transient float signz;
	public transient float emissionVal;
	private transient Light light;
	private transient int i;
	private transient int i2;
	private transient int arrayLen;
	public transient boolean created = false;
	public transient Vector3[] emissionMesh;
	public transient Queueable emissionObject;
	public transient Entity base;
	
	public transient float time = 0;
	// ----- End Transient Variables ----- //

	// ----- Non-Essential Variables ----- //
	private float x, y, z;
	private float radius;
	// ----- End Non-Essential Variables ----- //

	public ParticleEmitter(float particleLifetime, float duration,
			int blendFuncSRC, int blendFuncDST,
			String atlasName,
			String name)
	{
		this.UID = this.toString()+this.hashCode()+System.currentTimeMillis()+System.nanoTime();
		this.name = name;

		this.particleLifetime = particleLifetime;
		this.duration = duration;
		this.blendFuncSRC = blendFuncSRC;
		this.blendFuncDST = blendFuncDST;
		this.atlasName = atlasName;
	}

	public int getActiveParticles() {
		return active.size;
	}

	public float getRadius()
	{
		return radius;
	}

	public Vector3 getPosition()
	{
		return pos.set(x, y, z);
	}

	public void setPosition(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		if (light != null) light.position.set(x+lightx, y+lighty, z+lightz);
	}

	public TimelineValue[] getTimeline(ParticleAttribute pa)
	{
		return timelines.get(pa);
	}
	
	public void setTimeline(ParticleAttribute pa, boolean interpolated, float[]... values)
	{
		TimelineValue[] timeline = new TimelineValue[values.length];

		for (int i = 0; i < values.length; i++)
		{
			float[] nvals = new float[values[i].length-1];
			System.arraycopy(values[i], 1, nvals, 0, nvals.length);
			timeline[i] = new TimelineValue(values[i][0], nvals);
		}

		for (int i = 0; i < values.length-1; i++)
		{
			timeline[i].setInterpolated(true, timeline[i+1]);
		}
		
		timelines.put(pa, timeline);
	}

	public void setTimeline(ParticleAttribute pa, List<TimelineValue> values)
	{
		TimelineValue[] array = new TimelineValue[values.size()];
		values.toArray(array);
		
		timelines.put(pa, array);
	}

	/**
	 * Method to create a basic particle emitter. <p>
	 * The particles in this emitter will have a constant width and height,
	 *  a constant velocity (vx, vy, vz)
	 *   and will interpolate its colour from the start colour to the end over the particles lifetime.
	 *   The image used will be the sprite in the atlas designated as 'sprite0'.
	 * @param width
	 * @param height
	 * @param start
	 * @param end
	 * @param vx
	 * @param vy
	 * @param vz
	 */
	public void createBasicEmitter(float width, float height, Color colour, float vx, float vy, float vz)
	{
		timelines.put(ParticleAttribute.SPRITE, new TimelineValue[]{new TimelineValue(0, 0)});

		timelines.put(ParticleAttribute.SIZE, new TimelineValue[]{new TimelineValue(0, width, height)});

		timelines.put(ParticleAttribute.COLOUR, new TimelineValue[]{new TimelineValue(0, colour.r, colour.g, colour.b, colour.a)});

		timelines.put(ParticleAttribute.VELOCITY, new TimelineValue[]{new TimelineValue(0, vx, vy, vz)});
		
		timelines.put(ParticleAttribute.EMISSIONRATE, new TimelineValue[]{new TimelineValue(0, 10)});
		
		timelines.put(ParticleAttribute.EMISSIONAREA, new TimelineValue[]{new TimelineValue(0, 1, 1, 1)});
		
		timelines.put(ParticleAttribute.EMISSIONTYPE, new TimelineValue[]{new TimelineValue(0, 0)});
		
		timelines.put(ParticleAttribute.MASS, new TimelineValue[]{new TimelineValue(0, 1)});
		
		timelines.put(ParticleAttribute.ROTATIONTYPE, new TimelineValue[]{new TimelineValue(0, 0)});
	}

	public void addLight(boolean isStatic, float attenuation, float power, Color colour, boolean flicker, float x, float y, float z)
	{
		this.lightAttenuation = attenuation;
		this.lightPower = power;
		this.isLightStatic = isStatic;
		this.lightColour = colour;
		this.lightFlicker = flicker;
		this.lightx = x;
		this.lighty = y;
		this.lightz = z;
	}

	public void create() 
	{
		if (shader == null)
		{
			shader = new ShaderProgram(SHADER_VERTEX, SHADER_FRAGMENT);
		}

		ran = new Random();

		quad = Pools.obtain(Vector3.class).set(0, 0, 0);
		tmpMat = Pools.obtain(Matrix4.class).idt();
		tmpRot = Pools.obtain(Matrix4.class).idt();
		pos = Pools.obtain(Vector3.class).set(0, 0, 0);
		tmpVec = Pools.obtain(Vector3.class);
		tmpVec2 = Pools.obtain(Vector3.class);

		calculateRadius();
		reloadParticles();
		reloadTextures();

		created = true;
	}
	
	public void dispose()
	{
		if (quad != null) Pools.free(quad);
		quad = null;
		if (tmpMat != null) Pools.free(tmpMat);
		tmpMat = null;
		if (tmpRot != null) Pools.free(tmpRot);
		tmpRot = null;
		if (pos != null) Pools.free(pos);
		pos = null;
		if (tmpVec != null) Pools.free(tmpVec);
		tmpVec = null;
		if (tmpVec2 != null) Pools.free(tmpVec2);
		tmpVec2 = null;
		if (mesh != null) mesh.dispose();
		mesh = null;
		if (light != null) light.delete();
		light = null;
		time = 0;
		if (active != null) Pools.freeAll(active);
		active = null;
		if (inactive != null) Pools.freeAll(inactive);
		inactive = null;
		created = false;
	}

	public void getLight(LightManager lightManager)
	{
		if (lightColour == null) return;

		if (light != null)
		{
			lightManager.remove(light);
			light = null;
		}

		light = new Light(new Vector3(x+lightx, y+lighty, z+lightz), lightColour.cpy(), lightAttenuation);

		lightManager.add(light);
	}

	public void calculateParticles()
	{
		final float stepSize = 0.0001f;
		
		float emissionVal = 0;
		float endTime = 0;
		float temp;
		TimelineValue[] timeline;
		
		// Calculate Duration
		
		timeline = timelines.get(ParticleAttribute.EMISSIONRATE);
		temp = timeline[timeline.length-1].time+particleLifetime;
		if (temp > endTime) endTime = temp;
		
		timeline = timelines.get(ParticleAttribute.EMISSIONAREA);
		temp = timeline[timeline.length-1].time+particleLifetime;
		if (temp > endTime) endTime = temp;
		
		timeline = timelines.get(ParticleAttribute.EMISSIONTYPE);
		temp = timeline[timeline.length-1].time+particleLifetime;
		if (temp > endTime) endTime = temp;
		
		timeline = timelines.get(ParticleAttribute.MASS);
		temp = timeline[timeline.length-1].time+particleLifetime;
		if (temp > endTime) endTime = temp;
		
		timeline = timelines.get(ParticleAttribute.ROTATIONTYPE);
		temp = timeline[timeline.length-1].time+particleLifetime;
		if (temp > endTime) endTime = temp;
		
		float lastFound = 0;
		
		for (float time = 0; time < endTime; time += stepSize)
		{
			float erate = getAttributeValue(time, ParticleAttribute.EMISSIONRATE)[0];
			if (erate > 0)
			{
				emissionVal += erate * stepSize;
			}
			else if (erate < 0)
			{
				erate *= -1;
				int num = (int) erate;
				if (active.size > num) emissionVal = 0;
				else emissionVal = num - active.size;
			}
			while (emissionVal >= 1.0f)
			{
				emissionVal -= 1.0f;
								
				float t = time + particleLifetime;
				if (t > lastFound) lastFound = t;
			}
		}
		
		duration = lastFound;
		
		// Calculate Max Particles
		
		emissionVal = 0;
		Array<Particle> active = new Array<Particle>(false, 16);
		Array<Particle> inactive = new Array<Particle>(false, 16);
		
		for (float time = 0; time < duration; time += stepSize)
		{
			Iterator<Particle> pItr = active.iterator();
			
			while (pItr.hasNext())
			{
				Particle p = pItr.next();
				p.update(stepSize, 0, 0, 0);
				
				if (p.lifetime > particleLifetime)
				{
					pItr.remove();
					inactive.add(p);
				}
			}
			
			float erate = getAttributeValue(time, ParticleAttribute.EMISSIONRATE)[0];
			if (erate > 0)
			{
				emissionVal += erate * stepSize;
			}
			else if (erate < 0)
			{
				erate *= -1;
				int num = (int) erate;
				if (active.size > num) emissionVal = 0;
				else emissionVal = num - active.size;
			}
			while (emissionVal >= 1.0f)
			{
				emissionVal -= 1.0f;
								
				Particle p = null;
				if (inactive.size == 0)
				{
					p = Pools.obtain(Particle.class);
				}
				else
				{
					p = inactive.removeIndex(0);
				}
				p.set(0, 0, 0, 0, 0, 0);
				active.add(p);
			}
		}
		
		maxParticles = active.size + inactive.size;
		
		System.out.println("duration:"+duration+" mp:"+maxParticles);
		
		Pools.freeAll(active);
		Pools.freeAll(inactive);
	}

	public void calculateRadius()
	{
		this.radius = 1;
	}

	public void reloadParticles()
	{
		if (active != null) Pools.freeAll(active);
		if (inactive != null) Pools.freeAll(inactive);
		active = new Array<Particle>(false, maxParticles);
		inactive = new Array<Particle>(false, maxParticles);
		
		time = 0;

		for (int i = 0; i < maxParticles; i++)
		{
			Particle p = Pools.obtain(Particle.class);
			inactive.add(p);
		}

		vertices = new float[maxParticles*VERTEX_SIZE*6];

		if (mesh != null) mesh.dispose();
		mesh = new Mesh(false, maxParticles*6, 0,
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Generic, 4, "a_colour"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"));
		mesh.setVertices(vertices);		
	}

	public void reloadTextures()
	{
		atlas = FileUtils.loadAtlas(atlasName);
		ObjectSet<Texture> atlasTextures = atlas.getTextures();
		Iterator<Texture> itr = atlasTextures.iterator();

		atlasTexture = itr.next();
		texHash = atlasTexture.hashCode();
		
		int maxIndex = 0;
		TimelineValue[] sprite = timelines.get(ParticleAttribute.SPRITE);
		for (TimelineValue spriteTL : sprite)
		{
			int index = (int) spriteTL.getValues()[0];

			if (index > maxIndex) maxIndex = index;
		}

		topLeftTexCoords 	= new float[maxIndex+1][2];
		topRightTexCoords   = new float[maxIndex+1][2];
		botLeftTexCoords 	= new float[maxIndex+1][2];
		botRightTexCoords   = new float[maxIndex+1][2];

		for (int i = 0; i < maxIndex+1; i++)
		{
			AtlasRegion region = atlas.findRegion("sprite"+i);

			float[] tl = {(float)region.getRegionX()/(float)atlasTexture.getWidth(), (float)region.getRegionY()/(float)atlasTexture.getHeight()};
			float[] tr = {(float)(region.getRegionX()+region.getRegionWidth())/(float)atlasTexture.getWidth(), (float)region.getRegionY()/(float)atlasTexture.getHeight()};
			float[] bl = {(float)region.getRegionX()/(float)atlasTexture.getWidth(), (float)(region.getRegionY()+region.getRegionHeight())/(float)atlasTexture.getHeight()};
			float[] br = {(float)(region.getRegionX()+region.getRegionWidth())/(float)atlasTexture.getWidth(), (float)(region.getRegionY()+region.getRegionHeight())/(float)atlasTexture.getHeight()};

			topLeftTexCoords[i] = tl;
			topRightTexCoords[i] = tr;
			botLeftTexCoords[i] = bl;
			botRightTexCoords[i] = br;
		}
	}

	public void render()
	{
		if (!created) create();

		if (currentBlendSRC == blendFuncSRC && currentBlendDST == blendFuncDST)
		{}
		else {
			Gdx.gl.glBlendFunc(blendFuncSRC, blendFuncDST);
			currentBlendSRC = blendFuncSRC;
			currentBlendDST = blendFuncDST;
		}

		if (currentAtlas != null && atlasName.equals(currentAtlas)) {

		}
		else
		{
			atlasTexture.bind(0);
			shader.setUniformi("u_texture", 0);

			currentAtlas = atlasName;
		}

		mesh.render(shader, GL20.GL_TRIANGLES, 0, active.size*6);
	}

	public static void begin(Camera cam)
	{
		if (shader == null) return;

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(false);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);

		shader.begin();
		shader.setUniformMatrix("u_pv", cam.combined);
	}

	public static void end()
	{
		if (shader == null) return;

		shader.end();

		Gdx.gl.glDepthMask(true);
		Gdx.gl.glDisable(GL20.GL_BLEND);

		currentAtlas = null;
		currentBlendSRC = currentBlendDST = 0;
	}
	
	public void update(float delta, Camera cam)
	{
		if (!created) create();
		
		float erate = getAttributeValue(time, ParticleAttribute.EMISSIONRATE)[0];
				
		if (erate > 0)
		{
			emissionVal += erate * delta;
		}
		else if (erate < 0)
		{
			erate *= -1;
			int num = (int) erate;
			if (active.size > num) emissionVal = 0;
			else emissionVal = num - active.size;
		}
		
		while (emissionVal >= 1.0f)
		{			
			emissionVal -= 1.0f;			
			
			if (inactive.size == 0) continue;
			
			Particle p = inactive.removeIndex(0);

			int etype = (int) getAttributeValue(time, ParticleAttribute.EMISSIONTYPE)[0];
			int rotType = (int) getAttributeValue(time, ParticleAttribute.ROTATIONTYPE)[0];
			float[] exyz = getAttributeValue(time, ParticleAttribute.EMISSIONAREA);
			float x = this.x;
			float y = this.y;
			float z = this.z;
			
			if (emissionMesh != null)
			{
				Vector3 vertex = tmpVec.set(emissionMesh[ran.nextInt(emissionMesh.length)]);
				vertex.mul(emissionObject.getTransform());
				x = vertex.x;
				y = vertex.y;
				z = vertex.z;
			}
			
			if (etype == 0)
			{
				signx = (ran.nextInt(2) == 0) ? 1 : -1;
				signy = (ran.nextInt(2) == 0) ? 1 : -1;
				signz = (ran.nextInt(2) == 0) ? 1 : -1;
				
				p.set(0,
						x+(float)(exyz[0]*ran.nextGaussian()*signx), 
						y+(float)(exyz[1]*ran.nextGaussian()*signy),
						z+(float)(exyz[2]*ran.nextGaussian()*signz),
						etype, rotType
						);

			}
			else if (etype == 1)
			{
				float[] velocity = getAttributeValue(0, ParticleAttribute.VELOCITY);
				
				signx = (ran.nextInt(2) == 0) ? 1 : -1;
				signy = (ran.nextInt(2) == 0) ? 1 : -1;
				signz = (ran.nextInt(2) == 0) ? 1 : -1;
				
				p.set(0,
						x+(float)(exyz[0]*ran.nextGaussian()*signx), 
						y+(float)(exyz[1]*ran.nextGaussian()*signy),
						z+(float)(exyz[2]*ran.nextGaussian()*signz),
						etype, rotType,
						velocity[0]*(float)ran.nextFloat()*signx, 
						velocity[1]*(float)ran.nextFloat(),
						velocity[2]*(float)ran.nextFloat()*signz,
						getAttributeValue(0, ParticleAttribute.MASS)[0]
						);
			}
			else
			{
				System.err.println("Invalid emission type! "+etype);
			}
			active.add(p);
		}
				
		if (light != null)
		{
			light.position.set(x+lightx, y+lighty, z+lightz);
			if (lightFlicker) light.attenuation = (float) (lightAttenuation *
					(1-((1-((float)inactive.size / (float)active.size)))/2));
		}

		radius = 0;
		
		tmpRot.setToRotation(GLOBALS.DEFAULT_ROTATION, cam.direction);

		Iterator<Particle> pItr = active.iterator();

		i = 0;
		while (pItr.hasNext())
		{
			Particle p = pItr.next();

			float[] velocity = getAttributeValue(p.lifetime, ParticleAttribute.VELOCITY);

			if (p.emittedType == 0)
			{
				p.update(delta, velocity[0], velocity[1], velocity[2]);
			}
			else if (p.emittedType == 1)
			{
				p.vy += GLOBALS.GRAVITY*delta*p.mass;
				p.update(delta, p.vx, p.vy, p.vz);
			}

			if (base != null)
			{
				PositionalData pData = base.readOnlyRead(PositionalData.class);
				p.x += pData.deltaPos.x;
				p.y += pData.deltaPos.y;
				p.z += pData.deltaPos.z;
			}
			
			if (p.lifetime > particleLifetime)
			{
				pItr.remove();
				inactive.add(p);
				continue;
			}
			
			float dst = Vector3.dst(p.x, p.y, p.z, x, y, z);
			if (dst > radius) radius = dst;

			int rotType = p.rotType;
			
			if (rotType == 0)
			{
				tmpMat.setToTranslation(p.x, p.y, p.z).mul(tmpRot);
			}
			else if (rotType == 1)
			{
				if (p.emittedType == 0)
				{
					tmpVec.set(velocity[0], velocity[1], velocity[2]).nor();
				}
				else if (p.emittedType == 1)
				{
					tmpVec.set(p.vx, p.vy, p.vz).nor();
				}
				
				tmpMat.setToTranslation(p.x, p.y, p.z).rotate(GLOBALS.DEFAULT_ROTATION, tmpVec2.set(tmpVec).crs(GLOBALS.DEFAULT_UP).crs(tmpVec));
			}

			int sprite = (int) getAttributeValue(p.lifetime, ParticleAttribute.SPRITE)[0];
			float[] size = getAttributeValue(p.lifetime, ParticleAttribute.SIZE);
			float[] colour = getAttributeValue(p.lifetime, ParticleAttribute.COLOUR);

			// Triangle 1
			
			quad
			.set(-size[0]/2, size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = topLeftTexCoords[sprite][0];
			vertices[i++] = topLeftTexCoords[sprite][1];

			quad
			.set(size[0]/2, size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = topRightTexCoords[sprite][0];
			vertices[i++] = topRightTexCoords[sprite][1];

			quad
			.set(-size[0]/2, -size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = botLeftTexCoords[sprite][0];
			vertices[i++] = botLeftTexCoords[sprite][1];
			
			// Triangle 2

			quad
			.set(size[0]/2, -size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = botRightTexCoords[sprite][0];
			vertices[i++] = botRightTexCoords[sprite][1];
			
			quad
			.set(-size[0]/2, -size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = botLeftTexCoords[sprite][0];
			vertices[i++] = botLeftTexCoords[sprite][1];
			
			quad
			.set(size[0]/2, size[1]/2, 0)
			.mul(tmpMat);

			vertices[i++] = quad.x;
			vertices[i++] = quad.y;
			vertices[i++] = quad.z;

			vertices[i++] = colour[0];
			vertices[i++] = colour[1];
			vertices[i++] = colour[2];
			vertices[i++] = colour[3];

			vertices[i++] = topRightTexCoords[sprite][0];
			vertices[i++] = topRightTexCoords[sprite][1];
		}
		mesh.updateVertices(0, vertices, 0, active.size*6*VERTEX_SIZE);
	}

	private float[] getAttributeValue(float time, ParticleAttribute pa) {
		TimelineValue tv = searchTimeline(time, timelines.get(pa));

		return tv.getValuesInterpolated(time);
	}

	private TimelineValue searchTimeline(float time, TimelineValue[] value)
	{
		arrayLen = value.length;
		for (i2 = 0; i2 < arrayLen; i2++)
		{
			if (value[i2].time > time)
			{
				return value[i2-1];
			}
		}
		return value[arrayLen-1];
	}

	public ParticleEmitter copy()
	{
		ParticleEmitter copy = new ParticleEmitter(particleLifetime, duration, blendFuncSRC, blendFuncDST, atlasName, name);
		copy.maxParticles = maxParticles;
		
//		for (ParticleAttribute pa : ParticleAttribute.values())
//		{
//			TimelineValue[] tv = timelines.get(pa);
//			TimelineValue[] cpytv = new TimelineValue[tv.length];
//			for (int i = 0; i < tv.length; i++) cpytv[i] = tv[i].copy();
//			copy.timelines.put(pa, cpytv);
//		}
		
		copy.timelines = timelines;

		if (light != null)
			copy.addLight(isLightStatic, lightAttenuation, lightPower, lightColour, lightFlicker, lightx, lighty, lightz);

		return copy;
	}

	/**
	 * A particle, containing its current lifetime and position.
	 * @author Philip
	 *
	 */
	public static class Particle {
		float lifetime;
		float x, y, z;
		float vx, vy, vz;
		float mass;
		int emittedType;
		int rotType;

		public Particle()
		{
			lifetime = 0;
			x = 0;
			y = 0;
			z = 0;
		}

		public void update(float delta, float vx, float vy, float vz)
		{
			lifetime += delta;
			x += vx*delta;
			y += vy*delta;
			z += vz*delta;
		}

		public void set(float lifetime, float x, float y, float z, int emittedType, int rotType)
		{
			this.lifetime = lifetime;
			this.x = x;
			this.y = y;
			this.z = z;
			this.emittedType = emittedType;
			this.rotType = rotType;
		}
		
		public void set(float lifetime, float x, float y, float z, int emittedType, int rotType, float vx, float vy, float vz, float mass)
		{
			this.lifetime = lifetime;
			this.x = x;
			this.y = y;
			this.z = z;
			this.vx = vx;
			this.vy = vy;
			this.vz = vz;
			this.mass = mass;
			this.emittedType = emittedType;
			this.rotType = rotType;
		}
	}

	public static class TimelineValue implements Json.Serializable
	{
		public float[] values;
		public float time;

		public boolean interpolated = false;
		public float[] valueStep;
		public float[] interpolatedValues;

		private transient float timeStep;
		private transient int arrayLen;
		private transient int i;

		public TimelineValue(){}

		public TimelineValue(float time, float... values)
		{
			this.time = time;
			this.values = values;
		}

		public float[] getValues()
		{
			return values;
		}

		protected void setValues(boolean interpolated, float[] valueStep, float[] interpolatedValues)
		{
			this.interpolated = interpolated;
			this.valueStep = valueStep;
			this.interpolatedValues = interpolatedValues;
		}

		public float[] getValuesInterpolated(float currentTime)
		{
			if (!interpolated)
			{
				return values;
			}

			timeStep = currentTime-time;
			arrayLen = values.length;
			for (i = 0; i < arrayLen; i++)
			{
				float value = values[i]+(valueStep[i]*timeStep);
				interpolatedValues[i] = value;
			}

			return interpolatedValues;
		}

		public void setInterpolated(boolean interpolated, TimelineValue nextValue)
		{
			this.interpolated = interpolated;

			if (interpolated)
			{
				interpolatedValues = new float[values.length];
				valueStep = new float[values.length];

				arrayLen = nextValue.values.length;
				for (i = 0; i < arrayLen; i++)
				{
					float value = (nextValue.values[i] - values[i]) / (nextValue.time - time);
					valueStep[i] = value;
				}
			}
		}

		public TimelineValue copy()
		{
			TimelineValue copy = new TimelineValue(time, values);
			copy.setValues(interpolated, valueStep, interpolatedValues);
			return copy;
		}

		public void write (Json json) {
			json.writeValue("time", time);
			json.writeValue("values", values);
			json.writeValue("interpolated", interpolated);
			if (interpolated) json.writeValue("value step", valueStep);
		}

		@Override
		public void read (Json json, JsonValue jsonData) {

			time = jsonData.getFloat("time");

			JsonValue array = jsonData.get("values");
			values = new float[array.size];
			for (int i = 0; i < array.size; i++) values[i] = array.getFloat(i);

			interpolated = jsonData.getBoolean("interpolated");
			if (interpolated)
			{
				interpolatedValues = new float[values.length];
			}

			array = jsonData.get("value step");
			if (array != null)
			{
				valueStep = new float[array.size];
				for (int i = 0; i < array.size; i++) valueStep[i] = array.getFloat(i);
			}
		}
	}

	/**
	 * Write this particle emitter to the given json instance.
	 * @param json
	 */
	private void write (Json json) {
		json.writeObjectStart();
		json.writeValue("name", name);
		json.writeValue("timelines", timelines);

		json.writeValue("max particles", maxParticles);
		json.writeValue("particle lifetime", particleLifetime);
		json.writeValue("duration", duration);
		json.writeValue("blend func SRC", blendFuncSRC);
		json.writeValue("blend func DST", blendFuncDST);
		json.writeValue("atlas name", atlasName);

		if (light != null)
		{
			json.writeValue("light attenuation", lightAttenuation);
			json.writeValue("light power", lightPower);
			json.writeValue("light static", isLightStatic);
			json.writeValue("light flicker", lightFlicker);
			json.writeValue("light colour", lightColour);
			json.writeValue("light offset x", lightx);
			json.writeValue("light offset y", lighty);
			json.writeValue("light offset z", lightz);
		}

		json.writeObjectEnd();
	}

	/**
	 * Get a json instance set up for reading and writing a particle emitter
	 * @return
	 */
	public static Json getJson (Json json) {
		json.setSerializer(ParticleEmitter.class, new Json.Serializer<ParticleEmitter>() {
			@SuppressWarnings("rawtypes")
			public void write (Json json, ParticleEmitter emitter, Class knownType) {
				emitter.write(json);
			}

			@SuppressWarnings({ "rawtypes" })
			public ParticleEmitter read (Json json, JsonValue jsonData, Class type) {

				// ----- Particle Parameters ----- //
				EnumMap<ParticleAttribute, TimelineValue[]> timelines = new EnumMap<ParticleAttribute, TimelineValue[]>(ParticleAttribute.class);
				// ----- End Particle Parameters ----- //

				// ----- Emitter parameters ----- //
				String name = null;
				int maxParticles = 0;
				float particleLifetime = 0;
				float duration = 0;
				int blendFuncSRC = 0;
				int blendFuncDST = 0;
				String atlasName = null;
				// ----- End Emitter parameters ----- //

				// ----- Light ----- //
				float lightAttenuation = 0;
				float lightPower = 0;
				boolean isLightStatic = false;
				Color lightColour = null;
				boolean lightFlicker = false;
				float lightx = 0, lighty = 0, lightz = 0;
				// ----- End Light ----- //

				JsonValue tdata = jsonData.get("timelines");
				for (ParticleAttribute pa : ParticleAttribute.values())
				{
					TimelineValue[] timeline = json.readValue(TimelineValue[].class, tdata.get(pa.toString()));
					if (timeline == null) timeline = new TimelineValue[]{new TimelineValue(0, 0)};
					timelines.put(pa, timeline);
				}

				name = jsonData.getString("name");
				maxParticles = jsonData.getInt("max particles");
				particleLifetime = jsonData.getFloat("particle lifetime");
				duration = jsonData.getFloat("duration");

				blendFuncSRC = jsonData.getInt("blend func SRC");
				blendFuncDST = jsonData.getInt("blend func DST");

				atlasName = jsonData.getString("atlas name");

				JsonValue lc = jsonData.get("light colour");
				if (lc != null)
				{
					lightColour = json.readValue(Color.class, lc);
					lightAttenuation = jsonData.getFloat("light attenuation");
					lightPower = jsonData.getFloat("light power");
					isLightStatic = jsonData.getBoolean("light static");
					lightFlicker = jsonData.getBoolean("light flicker");
					lightx = jsonData.getFloat("light offset x");
					lighty = jsonData.getFloat("light offset y");
					lightz = jsonData.getFloat("light offset z");
				}

				ParticleEmitter emitter = new ParticleEmitter(particleLifetime, duration,
						blendFuncSRC, blendFuncDST,
						atlasName, name);
				emitter.maxParticles = maxParticles;

				emitter.timelines = timelines;

				if (lightColour != null) emitter.addLight(isLightStatic, lightAttenuation, lightPower, lightColour, lightFlicker, lightx, lighty, lightz);

				return emitter;
			}
		});

		return json;
	}

	public static ParticleEmitter load (String file)
	{
		Json json = getJson(new Json());

		return json.fromJson(ParticleEmitter.class, file);
	}

	public static ParticleEmitter load (FileHandle file)
	{
		Json json = getJson(new Json());

		return json.fromJson(ParticleEmitter.class, file);
	}

	private static final String SHADER_VERTEX = 

			"attribute vec3 a_position;" + "\n" +
			"attribute vec4 a_colour;" + "\n" +
			"attribute vec2 a_texCoords;" + "\n" +

			"uniform mat4 u_pv;" + "\n" +

			"varying vec4 v_colour;" + "\n" +
			"varying vec2 v_texCoords;" + "\n" +

			"void main() {" + "\n" +
			"v_colour = a_colour;" + "\n" +
			"v_texCoords = a_texCoords;" + "\n" +
			"gl_Position = u_pv * vec4(a_position, 1.0);" + "\n" +
			"}";

	private static final String SHADER_FRAGMENT = 
			"#ifdef GL_ES\n" +
				"precision highp float;\n" + 
			"#endif\n" + 

			"uniform sampler2D u_texture;" + "\n" +

			"varying vec4 v_colour;" + "\n" +
			"varying vec2 v_texCoords;" + "\n" +

			"void main() {" + "\n" +
			"gl_FragColor = texture2D(u_texture, v_texCoords) * v_colour;" + "\n" +
			"}";

	@Override
	public int compareTo(ParticleEmitter pe)
	{
		if (distance == pe.distance) 
		{
			return texHash - pe.texHash;
		}
		return (int) (distance - pe.distance) * 100;
	}
}