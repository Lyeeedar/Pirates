package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;
import java.util.PriorityQueue;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class ModelBatchInstance implements Queueable {
	
	public final ModelBatchData data;
	public final Matrix4 transform = new Matrix4();
	
	public ModelBatchInstance(ModelBatchData data)
	{
		this.data = data;
	}
	
	public Mesh getMesh()
	{
		return data.mesh;
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) 
	{
		data.add(transform, cam);
		((ModelBatcher) batches.get(ModelBatcher.class)).add(data);
	}

	@Override
	public void set(Entity source, Matrix4 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			transform.set(source.readOnlyRead(PositionalData.class).composed).mul(offset);
		}
		else
		{
			transform.setToTranslation(source.readOnlyRead(MinimalPositionalData.class).position).mul(offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
	}

	@Override
	public Queueable copy() {
		return new ModelBatchInstance(data);
	}

	@Override
	public void dispose() {
	}
	
	@Override
	public void set(Matrix4 transform)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public void transform(Matrix4 mat)
	{
		// TODO Auto-generated method stub
		
	}


	@Override
	public Matrix4 getTransform()
	{
		return null;
	}


	@Override
	public float[][] getVertexArray()
	{
		return new float[][]{new float[]{0}};
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		return out.set(0, 0, 0);
	}
	
	public static class ModelBatchData
	{
		public final Mesh mesh;
		public final int primitive_type;
		public final Texture[] textures;
		public final boolean useTriplanarSampling;
		public final float triplanarScaling;
		public final boolean transparent;
		public final boolean canCull;
		
		public final PriorityQueue<BatchedInstance> solidInstances = new PriorityQueue<BatchedInstance>();
		public final PriorityQueue<BatchedInstance> transparentInstances = new PriorityQueue<BatchedInstance>();
		
		private final Vector3 tmpVec = new Vector3();
		
		public ModelBatchData(Mesh mesh, int primitive_type, Texture[] textures, boolean transparent, boolean canCull, boolean useTriplanarSampling, float triplanarScaling)
		{
			this.mesh = mesh;
			this.primitive_type = primitive_type;
			this.textures = textures;
			this.transparent = transparent;
			this.canCull = canCull;
			this.useTriplanarSampling = useTriplanarSampling;
			this.triplanarScaling = triplanarScaling;
		}
		
		public void add(Matrix4 transform, Camera cam)
		{
			if (cam == null) return;
			
			Vector3 pos = tmpVec.set(0, 0, 0).mul(transform);
			float d = cam.position.dst(pos);
			if (d > cam.far) return;
			
			if (!canCull)
			{
				if (transparent) transparentInstances.add(pool.obtain().set(transform, d, 1.0f));
				else solidInstances.add(pool.obtain().set(transform, -d, 1.0f));
				return;
			}
			
			float cam2 = cam.far;
			float quarter = (cam2)/4.0f;
			float ncam = cam2-quarter;
			
			float dd = Math.max(d-quarter, 0.0f);
			
			float threshold = 1.0f - (dd / (ncam));
			float a = Math.abs(pos.x);
			float dst = a-MathUtils.floor(a);
					
			if (dst <= threshold) 
			{
				float fadestart = (1.0f-dst)*(ncam)+quarter;
				float fade = Math.min((fadestart-d)/(quarter/2.0f), 1.0f);
				
				if (fade > 0.0f) 
				{
					if (transparent) transparentInstances.add(pool.obtain().set(transform, d, fade));
					else solidInstances.add(pool.obtain().set(transform, -d, fade));
				}
			}
		}
		
		public void clear()
		{
			while (!solidInstances.isEmpty())
			{
				pool.free(solidInstances.poll());
			}
			while (!transparentInstances.isEmpty())
			{
				pool.free(transparentInstances.poll());
			}
		}
		
		public Pool<BatchedInstance> pool = new Pool<BatchedInstance>(){
			@Override
			protected BatchedInstance newObject() {
				return new BatchedInstance();
			}
		};
		
		public static class BatchedInstance implements Comparable<BatchedInstance>
		{
			private float dist;
			public final Matrix4 transform = new Matrix4();
			public float fade;
			
			public BatchedInstance set(Matrix4 transform, float dist, float fade)
			{
				this.transform.set(transform);
				this.fade = fade;
				return this;
			}

			@Override
			public int compareTo(BatchedInstance bi) {
				if (equals(bi)) return 0;
				return (int) ((bi.dist - dist)*100);
			}	
		}
	}
}