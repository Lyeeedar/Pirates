package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class BlurEffect extends PostProcessingEffect {
	
	public final int BUFFER_WIDTH;
	public final int BUFFER_HEIGHT;
	
	public final float factor;
	public final float radius;
	
	private final FrameBuffer downsampleBuffer1;
	private final FrameBuffer downsampleBuffer2;


	public BlurEffect(float factor, float radius, int BUFFER_WIDTH, int BUFFER_HEIGHT) {
		this.factor = factor;
		this.radius = radius;
		this.BUFFER_WIDTH = BUFFER_WIDTH;
		this.BUFFER_HEIGHT = BUFFER_HEIGHT;
		
		downsampleBuffer1 = new FrameBuffer(Format.RGBA4444, BUFFER_WIDTH, BUFFER_HEIGHT, false);
		downsampleBuffer2 = new FrameBuffer(Format.RGBA4444, BUFFER_WIDTH, BUFFER_HEIGHT, false);
	}
	
	@Override
	public void render(Texture texture, FrameBuffer buffer)
	{
		batch.getProjectionMatrix().setToOrtho2D(0, 0, BUFFER_WIDTH, BUFFER_HEIGHT);
		downsampleBuffer1.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);
		
		batch.begin();
		batch.setShader(shader);
		shader.setUniformf("u_resolution", BUFFER_HEIGHT);
		shader.setUniformf("u_dir", 1.0f, 0.0f);
		shader.setUniformf("u_radius", radius);
		shader.setUniformf("u_factor", factor);
		
		batch.draw(texture, 0, 0, BUFFER_WIDTH, BUFFER_HEIGHT,
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		batch.end();
		
		downsampleBuffer1.end();
		
		downsampleBuffer2.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);
		
		batch.begin();
		shader.setUniformf("u_resolution", BUFFER_WIDTH);
		shader.setUniformf("u_dir", 0.0f, 1.0f);
		shader.setUniformf("u_radius", radius);
		shader.setUniformf("u_factor", factor);
		
		batch.draw(downsampleBuffer1.getColorBufferTexture(), 0, 0, BUFFER_WIDTH, BUFFER_HEIGHT,
				0, 0, downsampleBuffer1.getColorBufferTexture().getWidth(), downsampleBuffer1.getColorBufferTexture().getHeight(),
				false, true);
		
		batch.end();
		
		downsampleBuffer2.end();
		
		buffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);

		
		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
		batch.begin();
		
		batch.draw(texture, 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		batch.draw(downsampleBuffer2.getColorBufferTexture(), 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, downsampleBuffer2.getColorBufferTexture().getWidth(), downsampleBuffer2.getColorBufferTexture().getHeight(),
				false, true);
		
		batch.end();

		buffer.end();
	}

	@Override
	public void create() {
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/postprocessing/blur.vertex.glsl"),
				Gdx.files.internal("data/shaders/postprocessing/blur.fragment.glsl")
				);
		if (!shader.isCompiled()) Gdx.app.log("Problem loading shader:", shader.getLog());
	}

}
