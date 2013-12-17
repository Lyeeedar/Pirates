package com.Lyeeedar.Graphics;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;

public interface Renderable {

	public void queue(float delta, AbstractModelBatch modelBatch, DecalBatch decalBatch, MotionTrailBatch trailBatch);
	
	public void set(Entity source);
	
	public void update(float delta, Camera cam, LightManager lights);
	
	public Renderable copy();
	
	public void dispose();
}
