package com.Lyeeedar.Graphics;

import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class AnimatedModelBatch implements Batch {
	
	private final Vector3 tmp = new Vector3();
	private final PriorityQueue<BatchedInstance> instances = new PriorityQueue<BatchedInstance>();
	private final ModelBatch batch;
	private Camera cam;
	
	private Environment environment;
	
	public AnimatedModelBatch()
	{
		batch = new ModelBatch();
		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
	}
	
	public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
		@Override
		protected BatchedInstance newObject() {
			return new BatchedInstance();
		}
	};
	
	public void render(LightManager lights, Camera cam)
	{
		this.cam = cam;
		
		batch.begin(cam);
		
		while (!instances.isEmpty())
		{
			BatchedInstance bi = instances.poll();
			batch.render(bi.instance);
			pool.free(bi);
		}
		
		batch.end();
	}
	
	public void add(ModelInstance model)
	{
		if (cam == null) return;
		tmp.set(0, 0, 0).mul(model.transform);
		instances.add(pool.obtain().set(model, -tmp.dst2(cam.position)));
	}

	private class BatchedInstance implements Comparable<BatchedInstance>
	{
		public ModelInstance instance;
		private float dist;
		
		public BatchedInstance set(ModelInstance instance, float dist)
		{
			this.instance = instance;
			return this;
		}

		@Override
		public int compareTo(BatchedInstance bi) {
			if (equals(bi)) return 0;
			return (int) ((bi.dist - dist)*100);
		}	
	}
}
