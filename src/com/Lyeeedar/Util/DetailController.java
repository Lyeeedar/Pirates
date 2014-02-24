package com.Lyeeedar.Util;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;

public class DetailController
{
	Texture[] mouths;
	Texture[] eyes;
	
	int mi = 0;
	int ei = 0;
	
	float time = 0;
	
	public DetailController()
	{
		mouths = new Texture[]{
				FileUtils.loadTexture("data/textures/mouth_etc.png", true, null, null),
				FileUtils.loadTexture("data/textures/mouth_fv.png", true, null, null),
				FileUtils.loadTexture("data/textures/mouth_l.png", true, null, null),
				FileUtils.loadTexture("data/textures/mouth_rest.png", true, null, null)
				};
		
		eyes = new Texture[]{
				FileUtils.loadTexture("data/textures/eyes_angry.png", true, null, null),
				FileUtils.loadTexture("data/textures/eyes_closed.png", true, null, null),
				FileUtils.loadTexture("data/textures/eyes_normal.png", true, null, null)
				};
	}
	
	public void update(float delta, Camera cam, LightManager lights, AnimatedModel target)
	{
		time += delta;
		if (time > 1.5f)
		{
			time = 0;
			
			mi++;
			ei++;
			
			if (mi >= mouths.length) mi = 0;
			if (ei >= eyes.length) ei = 0;
			
			target.detail[0] = mouths[mi];
			target.detail[1] = eyes[ei];
		}
	}
}
