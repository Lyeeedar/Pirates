package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class SSAOEffect extends PostProcessingEffect {

	private final BlurEffect blur;
	
	public final int BUFFER_WIDTH;
	public final int BUFFER_HEIGHT;
	
	private final FrameBuffer downsampleBuffer;
		
	public SSAOEffect(int BUFFER_WIDTH, int BUFFER_HEIGHT) 
	{
		this.BUFFER_WIDTH = BUFFER_WIDTH;
		this.BUFFER_HEIGHT = BUFFER_HEIGHT;
		
		downsampleBuffer = new FrameBuffer(Format.RGBA8888, BUFFER_WIDTH, BUFFER_HEIGHT, false);
		blur = new BlurEffect(1.0f, 1.05f, BUFFER_WIDTH, BUFFER_HEIGHT);
	}

	@Override
	public void render(Texture texture, FrameBuffer buffer, Texture depthTexture)
	{		
		downsampleBuffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.graphics.getGL20().glDisable(GL20.GL_DEPTH_TEST);		
		Gdx.graphics.getGL20().glDisable(GL20.GL_CULL_FACE);

		batch.setShader(shader);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, BUFFER_WIDTH, BUFFER_HEIGHT);
		
		batch.begin();
		batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
		
		shader.setUniformi("bgl_RenderedTexture", 1);
		texture.bind(1);
		
		shader.setUniformf("bgl_RenderedTextureWidth", texture.getWidth());
		shader.setUniformf("bgl_RenderedTextureHeight", texture.getHeight());
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		batch.draw(depthTexture, 0, 0, BUFFER_WIDTH, BUFFER_HEIGHT,
				0, 0, depthTexture.getWidth(), depthTexture.getHeight(),
				false, true);
		
		batch.end();
		
		downsampleBuffer.end();
		
		//blur.render(downsampleBuffer.getColorBufferTexture(), downsampleBuffer, depthTexture);
		
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
		ShaderProgram.pedantic = true;
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/postprocessing/default.vertex.glsl"),
				Gdx.files.internal("data/shaders/postprocessing/ssao.fragment.glsl")
				);
		if (!shader.isCompiled()) Gdx.app.log("Problem loading shader:", shader.getLog());
		
	}

}
