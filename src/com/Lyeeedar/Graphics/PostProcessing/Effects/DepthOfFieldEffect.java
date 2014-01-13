package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DepthOfFieldEffect extends PostProcessingEffect {
	
private final BlurEffect blur;
	
	public final int BUFFER_WIDTH;
	public final int BUFFER_HEIGHT;
	
	private final FrameBuffer downsampleBuffer;
	private final FrameBuffer dofBuffer;
	
	public DepthOfFieldEffect(int BUFFER_WIDTH, int BUFFER_HEIGHT) 
	{
		this.BUFFER_WIDTH = BUFFER_WIDTH;
		this.BUFFER_HEIGHT = BUFFER_HEIGHT;
		
		downsampleBuffer = new FrameBuffer(Format.RGBA8888, BUFFER_WIDTH, BUFFER_HEIGHT, false);
		dofBuffer = new FrameBuffer(Format.RGBA8888, BUFFER_WIDTH, BUFFER_HEIGHT, false); 
		blur = new BlurEffect(1.05f, 1.5f, BUFFER_WIDTH, BUFFER_HEIGHT);
	}

	@Override
	public void render(Texture texture, FrameBuffer buffer, Texture depthTexture)
	{
		blur.render(texture, downsampleBuffer, depthTexture);
		
		dofBuffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setShader(shader);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, BUFFER_WIDTH, BUFFER_HEIGHT);
		
		batch.begin();
		
		shader.setUniformf("u_far", GLOBALS.FOG_MAX-1000f);
		shader.setUniformi("u_depth", 1);
		depthTexture.bind(1);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		downsampleBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		batch.draw(downsampleBuffer.getColorBufferTexture(), 0, 0, BUFFER_WIDTH, BUFFER_HEIGHT,
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		batch.end();
		
		dofBuffer.end();
		
		buffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);

		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
		
		batch.begin();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.draw(texture, 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		batch.draw(dofBuffer.getColorBufferTexture(), 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, dofBuffer.getColorBufferTexture().getWidth(), dofBuffer.getColorBufferTexture().getHeight(),
				false, true);
		
		batch.end();

		buffer.end();
	}
	
	@Override
	public void create() {
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/postprocessing/default.vertex.glsl"),
				Gdx.files.internal("data/shaders/postprocessing/depth.fragment.glsl")
				);
		if (!shader.isCompiled()) Gdx.app.log("Problem loading shader:", shader.getLog());
	}

}
