package com.Lyeeedar.Graphics;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public interface Renderable {

	public void queue(float delta, HashMap<Class, Batch> batches);
	
	public void set(Entity source, Vector3 offset);
	
	public void update(float delta, Camera cam, LightManager lights);
	
	public Renderable copy();
	
	public void dispose();
}
