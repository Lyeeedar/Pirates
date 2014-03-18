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
import com.Lyeeedar.Collision.Octtree.OcttreeFrustum;
import com.Lyeeedar.Graphics.LineRenderer;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ChunkedTerrainBatch;
import com.Lyeeedar.Graphics.Batchers.DecalBatcher;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Batchers.MotionTrailBatch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor.Effect;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Graphics.Renderers.DeferredRenderer;
import com.Lyeeedar.Graphics.Renderers.ForwardRenderer;
import com.Lyeeedar.Graphics.Renderers.Renderer;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.Controls;
import com.Lyeeedar.Util.DiscardCameraGroupStrategy;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
 

public abstract class AbstractScreen implements Screen {
	
	protected final PirateGame game;
	
	protected int screen_width;
	protected int screen_height;

	protected final SpriteBatch spriteBatch;
	protected final BitmapFont font;
	protected final Stage stage;
	
	protected Renderer renderer;
	
	protected final FollowCam cam;
	protected final Controls controls;

	private long startTime;
	private long time;
	private long frameTime;
	private long averageFrame;
	private long averageUpdate;
	private long averageQueue;
	
	private final OcttreeFrustum octtreeFrustum;
	
	public AbstractScreen(PirateGame game)
	{
		this.game = game;
		controls = new Controls(GLOBALS.ANDROID);
		
		cam = new FollowCam(controls, new OcttreeBox(new Vector3(), new Vector3(GLOBALS.FOG_MAX/2, GLOBALS.FOG_MAX/2, GLOBALS.FOG_MAX/2), null), 50);
		
		//renderer = new ForwardRenderer(cam);
		renderer = new DeferredRenderer(cam);
		
		font = new BitmapFont();
		
		spriteBatch = new SpriteBatch();
		
		stage = new Stage(0, 0, true, spriteBatch);
		
		if (GLOBALS.lineRenderer == null) GLOBALS.lineRenderer = new LineRenderer();
		
		octtreeFrustum = new OcttreeFrustum(cam, -1);
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
		
		GLOBALS.PROGRAM_TIME += delta;
		
		frameTime = System.nanoTime();
		
		time = System.nanoTime();
		update(delta);
		averageUpdate += System.nanoTime()-time;
		averageUpdate /= 2;
		
		RenderType renderType = renderer instanceof DeferredRenderer ? RenderType.DEFERRED : RenderType.FORWARD;
		GLOBALS.LIGHTS.sort(octtreeFrustum, cam, renderType);
		
		stage.act(delta);
		
		time = System.nanoTime();
		queueRenderables(delta, renderer.getBatches());
		averageQueue += System.nanoTime()-time;
		averageQueue /= 2;
		
		renderer.render();
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		spriteBatch.begin();
		drawOrthogonals(delta, spriteBatch);
		spriteBatch.end();
		stage.draw();

        if (System.currentTimeMillis() - startTime > 1000) {
        	Gdx.app.log("Update", "");
			Gdx.app.log("	FPS         ", ""+Gdx.graphics.getFramesPerSecond());
			Gdx.app.log("	Frame Time  ", ""+averageFrame/1000000f);
			Gdx.app.log("	Delta       ", ""+delta);
	        Gdx.app.log("	Memory Usage", ""+(Gdx.app.getJavaHeap()/1000000)+" mb");
	        Gdx.app.log("	Update      ", ""+averageUpdate);
	        Gdx.app.log("	Queue       ", ""+averageQueue);
//	        Gdx.app.log("	Model       ", ""+averageModel);
//	        Gdx.app.log("	Decal       ", ""+averageDecal);
//	        Gdx.app.log("	Trail       ", ""+averageTrail);
//	        Gdx.app.log("	Orthogonal  ", ""+averageOrthogonal);
//	        Gdx.app.log("	Particles   ", ""+averageParticles);
//	        Gdx.app.log("	No Particles", ""+particleNum);
//	        Gdx.app.log("	PostProcess ", ""+averagePost);
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
		
		renderer.resize(width, height);
		
		resized(width, height);
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
	
	public abstract void queueRenderables(float delta, HashMap<Class, Batch> batches);
	
	public abstract void drawOrthogonals(float delta, SpriteBatch batch);

	public abstract void update(float delta);
	
	public abstract void superDispose();

	public abstract void resized(int width, int height);
}
