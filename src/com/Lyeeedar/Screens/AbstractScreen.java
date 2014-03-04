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

import java.util.HashMap;

import com.Lyeeedar.Collision.Octtree.OcttreeBox;
import com.Lyeeedar.Graphics.LineRenderer;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.DecalBatcher;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher.ModelBatchers;
import com.Lyeeedar.Graphics.Batchers.MotionTrailBatch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor.Effect;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.DiscardCameraGroupStrategy;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
 

public abstract class AbstractScreen implements Screen {
	
	protected final PirateGame game;
	
	protected int screen_width;
	protected int screen_height;

	protected final SpriteBatch spriteBatch;
	protected final DecalBatch decalBatch;
	protected final MotionTrailBatch trailBatch;
	protected final TexturedMeshBatch renderer;
	protected final AnimatedModelBatch modelBatch;
	protected final ParticleEffectBatch particleBatch;
	protected final BitmapFont font;
	protected final Stage stage;
	protected final PostProcessor postprocessor;
	
	protected final HashMap<Class, Batch> batches;
	
	protected final FollowCam cam;
	protected final Controls controls;

	private long startTime;
	private long time;
	private long frameTime;
	private long averageFrame;
	private long averageUpdate;
	private long averageQueue;
	private long averageModel;
	//private long averageTrail;
	private long averageDecal;
	private long averageOrthogonal;
	private long averageParticles;
	private long averagePost;
	protected int particleNum;
	
	public AbstractScreen(PirateGame game)
	{
		this.game = game;
		controls = new Controls(GLOBALS.ANDROID);
		
		cam = new FollowCam(controls, new OcttreeBox(new Vector3(), new Vector3(GLOBALS.FOG_MAX/2, GLOBALS.FOG_MAX/2, GLOBALS.FOG_MAX/2), null), 50);
		
		font = new BitmapFont();
		
		spriteBatch = new SpriteBatch();
		decalBatch = new DecalBatch(new DiscardCameraGroupStrategy(cam));
		trailBatch = new MotionTrailBatch();
		renderer = new TexturedMeshBatch(false);
		particleBatch = new ParticleEffectBatch();
		
		modelBatch = new AnimatedModelBatch(12);
		
		batches = new HashMap<Class, Batch>();
		batches.put(TexturedMeshBatch.class, renderer);
		batches.put(AnimatedModelBatch.class, modelBatch);
		batches.put(DecalBatcher.class, new DecalBatcher(decalBatch));
		batches.put(ModelBatchers.class, new ModelBatchers());
		batches.put(MotionTrailBatch.class, trailBatch);
		batches.put(ParticleEffectBatch.class, particleBatch);
		
		stage = new Stage(0, 0, true, spriteBatch);
		postprocessor = new PostProcessor(Format.RGBA8888, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], cam);
		
		if (GLOBALS.lineRenderer == null) GLOBALS.lineRenderer = new LineRenderer();
		
		postprocessor.addEffect(Effect.BLOOM);
		//postprocessor.addEffect(Effect.SILHOUETTE);
		postprocessor.addEffect(Effect.UNDERWATER);
		
		//postprocessor.addEffect(Effect.BLUR);
		//postprocessor.addEffect(Effect.BLUR);
		//postprocessor.addEffect(Effect.EDGE_DETECT);
		
	}

	float[] deltas = new float[10];
	
	@Override
	public void render(float delta) 
	{
		for (int i = 0; i < deltas.length-1; i++)
		{
			deltas[i] = deltas[i+1];
			delta += deltas[i];
		}
		delta /= (float)deltas.length;
		deltas[deltas.length-1] = delta;
		
		if (controls.esc()) game.switchScreen(PirateGame.Screen.MAINMENU);
		
		postprocessor.begin();
		
		GLOBALS.PROGRAM_TIME += delta;
		
		frameTime = System.nanoTime();
		
		time = System.nanoTime();
		update(delta);
		averageUpdate += System.nanoTime()-time;
		averageUpdate /= 2;
		
		stage.act(delta);
		
		time = System.nanoTime();
		queueRenderables(delta, batches);
		averageQueue += System.nanoTime()-time;
		averageQueue /= 2;
		
		GLOBALS.LIGHTS.sort(cam.position);
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		//Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		//Gdx.gl.glEnable(GL20.GL_BLEND);
		//Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(GLOBALS.LIGHTS, cam);
		((ModelBatchers) batches.get(ModelBatchers.class)).renderSolid(GLOBALS.LIGHTS, cam);
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(GLOBALS.LIGHTS, cam);
		GLOBALS.physicsWorld.render((PerspectiveCamera) cam);
		GLOBALS.lineRenderer.render(cam);
		averageModel += System.nanoTime()-time;
		averageModel /= 2;
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		drawSkybox(delta);
		((ModelBatchers) batches.get(ModelBatchers.class)).renderTransparent(GLOBALS.LIGHTS, cam);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glDepthMask(false);
		((DecalBatcher) batches.get(DecalBatcher.class)).flush();
		averageDecal += System.nanoTime()-time;
		averageDecal /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(false);
		((MotionTrailBatch) batches.get(MotionTrailBatch.class)).flush(cam);
		((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).render(cam);
		this.particleNum = ((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).particleNum;
		averageParticles += System.nanoTime()-time;
		averageParticles /= 2;
		
		Gdx.gl20.glBlendEquation(GL20.GL_FUNC_ADD);
		
		time = System.nanoTime();
		postprocessor.end();
		averagePost += System.nanoTime()-time;
		averagePost /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		spriteBatch.begin();
		drawOrthogonals(delta, spriteBatch);
		spriteBatch.end();
		stage.draw();
		averageOrthogonal += System.nanoTime()-time;
		averageOrthogonal /= 2;

        if (System.currentTimeMillis() - startTime > 1000) {
        	Gdx.app.log("Update", "");
			Gdx.app.log("	FPS         ", ""+Gdx.graphics.getFramesPerSecond());
			Gdx.app.log("	Frame Time  ", ""+averageFrame/1000000f);
			Gdx.app.log("	Delta       ", ""+delta);
	        Gdx.app.log("	Memory Usage", ""+(Gdx.app.getJavaHeap()/1000000)+" mb");
	        Gdx.app.log("	Update      ", ""+averageUpdate);
	        Gdx.app.log("	Queue       ", ""+averageQueue);
	        Gdx.app.log("	Model       ", ""+averageModel);
	        Gdx.app.log("	Decal       ", ""+averageDecal);
	        //Gdx.app.log("	Trail       ", ""+averageTrail);
	        Gdx.app.log("	Orthogonal  ", ""+averageOrthogonal);
	        Gdx.app.log("	Particles   ", ""+averageParticles);
	        Gdx.app.log("	No Particles", ""+particleNum);
	        Gdx.app.log("	PostProcess ", ""+averagePost);
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
        cam.far = (GLOBALS.ANDROID) ? 202f : GLOBALS.FOG_MAX ;
        cam.update();
        
		stage.setViewport(width, height, false);
		
		postprocessor.updateBufferSettings(Format.RGBA8888, width, height);
		
		resized(width, height);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		font.dispose();
		stage.dispose();
		trailBatch.dispose();
		decalBatch.dispose();
		
		superDispose();
	}
	
	/**
	 * Put all the creation of the objects used by the screen in here to avoid reloading everything on a screenswap
	 */
	public abstract void create();

	public abstract void drawSkybox(float delta);
	
	public abstract void queueRenderables(float delta, HashMap<Class, Batch> batches);
	
	public abstract void drawOrthogonals(float delta, SpriteBatch batch);

	public abstract void update(float delta);
	
	public abstract void superDispose();

	public abstract void resized(int width, int height);
}
