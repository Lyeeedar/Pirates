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
package com.Lyeeedar.Screens;

import java.util.ArrayList;

import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Graphics.Renderers.CellShadingModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
 

public abstract class AbstractScreen implements Screen {
	
	int screen_width;
	int screen_height;

	private SpriteBatch spriteBatch;
	private DecalBatch decalBatch;
	private MotionTrailBatch trailBatch;
	private AbstractModelBatch renderer;

	protected BitmapFont font;
	protected final Stage stage;

	protected Controls controls;
	protected FollowCam cam;
	
	protected LightManager lights;
	
	private long startTime;
	private long time;
	private long frameTime;
	private long averageFrame;
	private long averageUpdate;
	private long averageQueue;
	private long averageModel;
	private long averageTrail;
	private long averageDecal;
	private long averageOrthogonal;
	private long averageParticles;
	private int particleNum;
	protected ArrayList<ParticleEffect> visibleEmitters = new ArrayList<ParticleEffect>();

	public AbstractScreen(LightManager lights)
	{
		this.lights = lights;
		font = new BitmapFont();
		spriteBatch = new SpriteBatch();
		trailBatch = new MotionTrailBatch();
		renderer = new CellShadingModelBatch();

		stage = new Stage(0, 0, true, spriteBatch);
		controls = new Controls(GLOBALS.ANDROID);
		cam = new FollowCam(controls);
		renderer.cam = cam;
		decalBatch = new DecalBatch(new CameraGroupStrategy(cam));
		
		ParticleEffect effect = new ParticleEffect(5);
		ParticleEmitter flame = new ParticleEmitter(1.5f, 1, 0.0005f, 10.4f, 10.4f, 10.4f, 0, GL20.GL_SRC_ALPHA, GL20.GL_ONE, "f", "flame");
		flame.createBasicEmitter(1, 1, new Color(0.3f, 0.3f, 0.4f, 0.5f), new Color(0.3f, 0.3f, 0.4f, 0.5f), 0, -75, 0);
		flame.calculateParticles();
		flame.create();
		effect.addEmitter(flame, 
				0, 10, 0);
		visibleEmitters.add(effect);
	}

	@Override
	public void render(float delta) 
	{
		GLOBALS.PROGRAM_TIME += delta;
		
		frameTime = System.nanoTime();
		
		time = System.nanoTime();
		update(delta);
		averageUpdate += System.nanoTime()-time;
		averageUpdate /= 2;
		
		time = System.nanoTime();
		queueRenderables(delta, renderer, decalBatch, trailBatch);
		averageQueue += System.nanoTime()-time;
		averageQueue /= 2;
		
		stage.act(delta);

		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		time = System.nanoTime();
		renderer.flush(lights);
		drawSkybox(delta);
		averageModel += System.nanoTime()-time;
		averageModel /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(false);
		decalBatch.flush();
		averageDecal += System.nanoTime()-time;
		averageDecal /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glDepthMask(false);
		trailBatch.flush(cam);
		averageTrail += System.nanoTime()-time;
		averageTrail /= 2;
		
		time = System.nanoTime();
		particleNum = 0;
		ParticleEmitter.begin(cam);
		for (ParticleEffect p : visibleEmitters)
		{
			p.update(delta, cam);
			particleNum += p.getActiveParticles();
			p.render();
		}
		ParticleEmitter.end();
		averageParticles += System.nanoTime()-time;
		averageParticles /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		drawOrthogonals(delta, spriteBatch);
		averageOrthogonal += System.nanoTime()-time;
		averageOrthogonal /= 2;

        if (System.currentTimeMillis() - startTime > 1000) {
        	Gdx.app.log("Update", "");
			Gdx.app.log("	FPS         ", ""+Gdx.graphics.getFramesPerSecond());
			Gdx.app.log("	Frame Time  ", ""+averageFrame/1000000f);
	        Gdx.app.log("	Memory Usage", ""+(Gdx.app.getJavaHeap()/1000000)+" mb");
	        Gdx.app.log("	Update      ", ""+averageUpdate);
	        Gdx.app.log("	Queue       ", ""+averageQueue);
	        Gdx.app.log("	Model       ", ""+averageModel);
	        Gdx.app.log("	Decal       ", ""+averageDecal);
	        Gdx.app.log("	Trail       ", ""+averageTrail);
	        Gdx.app.log("	Orthogonal  ", ""+averageOrthogonal);
	        Gdx.app.log("	Particles   ", ""+averageParticles);
	        Gdx.app.log("	No Particles", ""+particleNum);
			startTime = System.currentTimeMillis();
		}
		
        spriteBatch.begin();
        font.draw(spriteBatch, ""+Gdx.app.getGraphics().getFramesPerSecond(), 20, screen_height-40);
        spriteBatch.end();
        
        averageFrame += System.nanoTime()-frameTime;
		averageFrame /= 2;
	}

	@Override
	public void resize(int width, int height) {
		
		GLOBALS.SCREEN_SIZE[0] = width;
		GLOBALS.SCREEN_SIZE[1] = height;
		
		width = GLOBALS.RESOLUTION[0];
		height = GLOBALS.RESOLUTION[1];

		screen_width = width;
		screen_height = height;

        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.near = 2f;
        cam.far = (GLOBALS.ANDROID) ? 202f : 2502f ;

		stage.setViewport( width, height, true);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		font.dispose();
		stage.dispose();
		
		superDispose();
	}
	
	/**
	 * Put all the creation of the objects used by the screen in here to avoid reloading everything on a screenswap
	 */
	public abstract void create();

	public abstract void drawSkybox(float delta);
	
	public abstract void queueRenderables(float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch);

	public abstract void drawOrthogonals(float delta, SpriteBatch batch);

	public abstract void update(float delta);
	
	public abstract void superDispose();

}
