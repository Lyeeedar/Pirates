package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class UnderwaterEffect extends PostProcessingEffect 
{
	private Texture texture;
	private final FollowCam cam;
	
	public UnderwaterEffect(int BUFFER_WIDTH, int BUFFER_HEIGHT, FollowCam cam) 
	{
		this.cam = cam;
	}

	@Override
	public void render(Texture texture, FrameBuffer buffer, Texture depthTexture)
	{		
		buffer.begin();
		
		Gdx.graphics.getGL20().glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setShader(null);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, buffer.getWidth(), buffer.getHeight());
		
		batch.begin();
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		batch.draw(texture, 0, 0, buffer.getWidth(), buffer.getHeight(),
				0, 0, texture.getWidth(), texture.getHeight(),
				false, true);
		
		if (cam.underwater)
		{
			batch.setColor(0, 0.4f, 0.6f, 0.6f);
			batch.draw(this.texture, 0, 0, buffer.getWidth(), buffer.getHeight());
		}
		batch.setColor(1, 1, 1, 1);
		
		batch.end();

		buffer.end();
	}
	
	@Override
	public void create() 
	{
		texture = FileUtils.loadTexture("data/textures/blank.png", true, null, null);
	}

}
