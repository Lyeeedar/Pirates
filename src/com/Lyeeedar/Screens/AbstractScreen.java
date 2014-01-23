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

import com.Lyeeedar.Graphics.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batch;
import com.Lyeeedar.Graphics.DecalBatcher;
import com.Lyeeedar.Graphics.ModelBatcher;
import com.Lyeeedar.Graphics.ModelBatcher.ModelBatchers;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor.Effect;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Graphics.Renderers.CellShadingModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.DiscardCameraGroupStrategy;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
 

public abstract class AbstractScreen implements Screen {
	
	protected final PirateGame game;
	
	protected int screen_width;
	protected int screen_height;

	protected final SpriteBatch spriteBatch;
	protected final DecalBatch decalBatch;
	protected final MotionTrailBatch trailBatch;
	protected final AbstractModelBatch renderer;
	protected final AnimatedModelBatch modelBatch;
	protected final BitmapFont font;
	protected final Stage stage;
	protected final PostProcessor postprocessor;
	protected final Array<ModelBatcher> modelBatchers;
	
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
		
		
		
		cam = new FollowCam(controls, FollowCam.createSphere(GLOBALS.FOG_MAX));
		
		font = new BitmapFont();
		
		spriteBatch = new SpriteBatch();
		decalBatch = new DecalBatch(new DiscardCameraGroupStrategy(cam));
		trailBatch = new MotionTrailBatch();
		renderer = new CellShadingModelBatch();
		modelBatchers = new Array<ModelBatcher>();
		
		modelBatch = new AnimatedModelBatch(12);
		
		batches = new HashMap<Class, Batch>();
		batches.put(AbstractModelBatch.class, renderer);
		batches.put(AnimatedModelBatch.class, modelBatch);
		batches.put(DecalBatcher.class, new DecalBatcher(decalBatch));
		batches.put(ModelBatchers.class, new ModelBatchers());
		batches.put(MotionTrailBatch.class, trailBatch);
		
		stage = new Stage(0, 0, true, spriteBatch);
		postprocessor = new PostProcessor(Format.RGBA8888, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]);
		
		postprocessor.addEffect(Effect.BLOOM);
		//postprocessor.addEffect(Effect.DOF);
		
		//postprocessor.addEffect(Effect.BLUR);
		//postprocessor.addEffect(Effect.BLUR);
		//postprocessor.addEffect(Effect.EDGE_DETECT);
		
	}

	@Override
	public void render(float delta) 
	{
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
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		//Gdx.gl.glEnable(GL20.GL_BLEND);
		//Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		time = System.nanoTime();
		((AbstractModelBatch) batches.get(AbstractModelBatch.class)).flush(GLOBALS.LIGHTS, cam);
		((ModelBatchers) batches.get(ModelBatchers.class)).renderSolid(GLOBALS.LIGHTS, cam);
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(GLOBALS.LIGHTS, cam);
		GLOBALS.physicsWorld.render((PerspectiveCamera) cam);
		averageModel += System.nanoTime()-time;
		averageModel /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		((DecalBatcher) batches.get(DecalBatcher.class)).flush();
		averageDecal += System.nanoTime()-time;
		averageDecal /= 2;
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		drawSkybox(delta);
		((ModelBatchers) batches.get(ModelBatchers.class)).renderTransparent(GLOBALS.LIGHTS, cam);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		((MotionTrailBatch) batches.get(MotionTrailBatch.class)).flush(cam);
		drawParticles(delta);
		averageParticles += System.nanoTime()-time;
		averageParticles /= 2;
		
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

		stage.setViewport(width, height, false);
		
		postprocessor.updateBufferSettings(Format.RGBA8888, width, height);
		
		resized(width, height);
	}

	@Override
	public void dispose() {
		spriteBatch.dispose();
		font.dispose();
		stage.dispose();
		renderer.dispose();
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
	
	public abstract void drawParticles(float delta);

	public abstract void drawOrthogonals(float delta, SpriteBatch batch);

	public abstract void update(float delta);
	
	public abstract void superDispose();

	public abstract void resized(int width, int height);
}
