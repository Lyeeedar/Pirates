package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.AbstractModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public interface Queueable {

	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches);
	
	public void set(Entity source, Vector3 offset);
	public void set(Matrix4 transform);
	
	public void transform(Matrix4 mat);
	
	public void update(float delta, Camera cam, LightManager lights);
	
	public Queueable copy();
	
	public void dispose();
}
