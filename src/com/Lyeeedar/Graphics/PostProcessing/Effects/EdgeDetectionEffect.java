package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class EdgeDetectionEffect extends PostProcessingEffect {

	public final int BUFFER_WIDTH;
	public final int BUFFER_HEIGHT;
	
	private final FrameBuffer downsampleBuffer;
	
	private final BlurEffect blur;
	
	public EdgeDetectionEffect(int BUFFER_WIDTH, int BUFFER_HEIGHT) 
	{
		this.BUFFER_WIDTH = BUFFER_WIDTH;
		this.BUFFER_HEIGHT = BUFFER_HEIGHT;
		
		downsampleBuffer = new FrameBuffer(Format.RGBA8888, BUFFER_WIDTH, BUFFER_HEIGHT, false);
		blur = new BlurEffect(0.94f, 1.5f, BUFFER_WIDTH, BUFFER_HEIGHT);
	}
	
	@Override
	public void render(Texture texture, FrameBuffer buffer)
	{		
		downsampleBuffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);

		batch.setShader(shader);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, BUFFER_WIDTH, BUFFER_HEIGHT);
		
//		shader.setUniformf("limit", -1.0f);
//		shader.setUniformf("intensity", 10.0f);
//		shader.setUniformf("imageWidthFactor", 100.0f);
//		shader.setUniformf("imageHeightFactor", 100.0f);
		
		batch.begin();
				
		batch.draw(texture, 0, 0, BUFFER_WIDTH, BUFFER_HEIGHT,
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		batch.end();
		
		downsampleBuffer.end();
		
		//blur.render(downsampleBuffer.getColorBufferTexture(), downsampleBuffer);
				
		buffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);

		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
		
		batch.begin();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		
		batch.draw(texture, 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
		
		batch.draw(downsampleBuffer.getColorBufferTexture(), 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, downsampleBuffer.getColorBufferTexture().getWidth(), downsampleBuffer.getColorBufferTexture().getHeight(),
				false, true);
		
		batch.end();

		buffer.end();
	}
	
	@Override
	public void create() {
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/postprocessing/edge_detect.vertex.glsl"),
				Gdx.files.internal("data/shaders/postprocessing/sobel.fragment.glsl")
				);
		if (!shader.isCompiled()) Gdx.app.log("Problem loading shader:", shader.getLog());
	}

}
