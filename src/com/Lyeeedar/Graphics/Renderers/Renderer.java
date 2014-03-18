package com.Lyeeedar.Graphics.Renderers;

import java.util.HashMap;

import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.graphics.Camera;

public interface Renderer
{
	public HashMap<Class, Batch> getBatches();
	public void render();
	public void resize(int width, int height);
}
