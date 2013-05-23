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
package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.lyeeedar.Pirates.GLOBALS;
 

public abstract class AbstractScreen implements Screen {
	
	int screen_width;
	int screen_height;

	protected final SpriteBatch spriteBatch;

	protected BitmapFont font;
	protected final Stage stage;

	protected ModelRenderer renderer;
	
	PerspectiveCamera cam;
	
	private long startTime;

	public AbstractScreen()
	{
		font = new BitmapFont();
		spriteBatch = new SpriteBatch();

		stage = new Stage(0, 0, true, spriteBatch);
		
		cam = new PerspectiveCamera(75, 800, 600);
	}

	@Override
	public void render(float delta) {
		
		update(delta);
		stage.act(delta);
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);	

		drawModels(delta);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		drawTransparent(delta);
		
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		drawOrthogonals(delta);

        if (System.currentTimeMillis() - startTime > 1000) {
        	 Gdx.app.log("Update", "");
			Gdx.app.log("	FPS", ""+Gdx.graphics.getFramesPerSecond());
	        Gdx.app.log("	Memory Usage", ""+(Gdx.app.getJavaHeap()/1000000)+" mb");
			startTime = System.currentTimeMillis();
		}
		
        spriteBatch.begin();
        font.draw(spriteBatch, ""+Gdx.app.getGraphics().getFramesPerSecond(), 20, screen_height-40);
        spriteBatch.end();
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
        cam.far = (GLOBALS.ANDROID) ? 202f : 502f ;

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
	/**
	 * Draw models using {@link ForwardRenderer}. Everything drawn in this method will also be passed through the post-processor
	 * @param delta
	 */
	public abstract void drawModels(float delta);
	/**
	 * Draw decals here. Everything drawn in this method will also be passed through the post-processor
	 * @param delta
	 */
	public abstract void drawTransparent(float delta);
	/**
	 * Draw sprites using sprite batch. Everything drawn here will NOT be post-processed
	 * @param delta
	 */
	public abstract void drawOrthogonals(float delta);
	/**
	 * Update game logic
	 * @param delta
	 */
	public abstract void update(float delta);
	
	public abstract void superDispose();

}
