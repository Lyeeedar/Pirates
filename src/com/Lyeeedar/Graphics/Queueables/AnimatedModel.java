package com.Lyeeedar.Graphics.Queueables;

import java.util.ArrayList;
import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Util.DetailController;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class AnimatedModel implements Queueable {
	
	public static class RenderablePool extends Pool<Renderable> {
		protected Array<Renderable> obtained = new Array<Renderable>();
		
		@Override
		protected Renderable newObject () {
			return new Renderable();
		}

		@Override
		public Renderable obtain () {
			Renderable renderable = super.obtain();
			renderable.environment = null;
			renderable.material = null;
			renderable.mesh = null;
			renderable.shader = null;
			obtained.add(renderable);
			return renderable;
		}
		
		public void flush() {
			super.freeAll(obtained);
			obtained.clear();
		}
	}
	
	protected static final RenderablePool renderablesPool = new RenderablePool();  
	public final Array<Renderable> renderables = new Array<Renderable>();
	public ModelInstance model;
	public AnimatedModel parent;
	public AnimationController anim;
	public Texture[] textures;
	public Texture[] detail = new Texture[3];
	public DetailController detailController;
	public Vector3 colour = new Vector3();
	public Array<ATTACHED_MODEL> attachedModels = new Array<ATTACHED_MODEL>();
	public String defaultAnim;
	public String currentAnim = "";
	public Matrix4 transform = new Matrix4();
	public final String name;
	
	private AnimationDesc current;
	
	private final Matrix4 tmpMat = new Matrix4();
	private final Matrix4 tmpMat2 = new Matrix4();
	
	public AnimatedModel(String name, Model model, Texture[] textures, Vector3 colour, String defaultAnim)
	{		
		this.model = new ModelInstance(model);
		this.model.getRenderables(renderables, renderablesPool);
		this.name = name;
	
		anim = new AnimationController(this.model);
		if (defaultAnim != null)
		{
			anim.setAnimation(defaultAnim, -1);
		}
		this.textures = textures;
		this.colour.set(colour);
		this.defaultAnim = defaultAnim;
	}
	
	public void setDetailController(DetailController dc)
	{
		this.detailController = dc;
	}
	
	public void attach(String nodeName, Queueable model, Matrix4 offset, String name)
	{
		Node node = null;
		if (nodeName != null)
		{
			 node = this.model.getNode(nodeName, true);
			 if (node == null) System.err.println("Failed to find node "+nodeName);
		}
		attachedModels.add(new ATTACHED_MODEL(node, model, offset, name));
		if (model instanceof AnimatedModel) ((AnimatedModel) model).parent = this;
	}
	
	public Queueable remove(String name)
	{
		for (int i = 0; i < attachedModels.size; i++)
		{
			ATTACHED_MODEL am = attachedModels.get(i);
			if (am.name.equals(name))
			{
				attachedModels.removeIndex(i);
				if (am.queueable instanceof AnimatedModel) ((AnimatedModel) am.queueable).parent = null;
				return am.queueable;
			}
		}
		return null;
	}
	
	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		if (batches.containsKey(AnimatedModelBatch.class)) ((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).add(renderables, textures, detail, colour);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.queue(delta, cam, batches);
		}
	}
	
	@Override
	public Matrix4 getTransform()
	{
		return transform;
	}
	
	public float animationProgress()
	{
		if (current == null) return 0;
		
		AnimationDesc ca = current;
		float dur = ca.animation.duration;
		return ( ( ca.time ) / ( ( dur - ca.offset ) / ca.speed ) ) * 100.0f;
	}

	@Override
	public void set(Entity source, Matrix4 offset) 
	{
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			transform.set(source.readOnlyRead(PositionalData.class).composed).mul(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			transform.setToTranslation(data.position.x, data.position.y, data.position.z).mul(offset);
		}
		
		model.transform.set(transform);
		
		AnimationData aData = source.readOnlyRead(AnimationData.class);
		colour.set(aData.colour);
		
		if (currentAnim.equals(aData.anim))
		{
			
		}
		else if (aData.anim.equals(""))
		{
			current = null;
			currentAnim = "";
		}
		else if (aData.animationLock && model.getAnimation(aData.anim) != null)
		{
			current = anim.animate(aData.anim, 1, aData.animate_speed, aData.listener, 0.1f);
			currentAnim = aData.anim;
			aData.listener = null;
		}
		else
		{
			if (model.getAnimation(aData.anim) != null)
			{
				current = anim.animate(aData.anim, -1, aData.animate_speed, aData.listener, 0.1f);
				currentAnim = aData.anim;
			}
			else if (!currentAnim.equals(aData.base_anim) && model.getAnimation(aData.base_anim) != null)
			{
				current = anim.animate(aData.base_anim, -1, aData.animate_speed, aData.listener, 0.1f);
				currentAnim = aData.base_anim;
			}
		}
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.set(source, offset);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.queueable.set(tmpMat);
			}
			am.queueable.transform(am.offset);
		}
	}
	
	@Override
	public void transform(Matrix4 mat)
	{
		transform.mul(mat);
		
		model.transform.set(transform);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.transform(mat);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.queueable.set(tmpMat);
			}
			am.queueable.transform(am.offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		anim.update(delta);
		
		renderablesPool.freeAll(renderables);
		renderables.clear();
		model.getRenderables(renderables, renderablesPool);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.update(delta, cam, lights);
		}

		if (detailController != null) detailController.update(delta, cam, lights, this);
	}

	@Override
	public Queueable copy() {
		AnimatedModel nm = new AnimatedModel(name, model.model, textures, colour, defaultAnim);
		for (ATTACHED_MODEL am : attachedModels)
		{
			nm.attach(am.node.id, am.queueable.copy(), am.offset, am.name);
		}
		return nm;
	}

	@Override
	public void dispose() {
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.dispose();
		}
	}

	private static class ATTACHED_MODEL
	{
		public Node node;
		public Queueable queueable;
		public Matrix4 offset = new Matrix4();
		public String name;
		
		public ATTACHED_MODEL(Node node, Queueable model, Matrix4 offset, String name)
		{
			this.node = node;
			this.queueable = model;
			this.offset.set(offset);
			this.name = name;
		}
	}

	@Override
	public void set(Matrix4 transform)
	{
		this.transform.set(transform);
		
		model.transform.set(transform);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.set(transform);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.queueable.set(tmpMat);
			}
			am.queueable.transform(am.offset);
		}
		
	}
	
	@Override
	public float[][] getVertexArray()
	{
		float[][] varray = FileUtils.getVertexArray(name);
		if (varray != null) return varray;
		
		float[][][] varrays = new float[renderables.size][][];
		int total = 0;
		for (int r = 0; r < renderables.size; r++)
		{
			Renderable ren = renderables.get(r);
			Mesh mesh = ren.mesh;
						
			final int nverts = mesh.getNumVertices();
			final int nindices = mesh.getNumIndices();
			final int vsize = mesh.getVertexSize() / 4;
			
			float[] vertices = mesh.getVertices(new float[nverts*vsize]);
			boolean[] used = new boolean[nverts];
			short[] indices = new short[nindices];
			mesh.getIndices(indices);
			
			int poff = mesh.getVertexAttributes().getOffset(Usage.Position);
			final int n = mesh.getVertexAttributes().size();
			int bone_num = 0;
			for (int i = 0; i < n; i++) {
				final VertexAttribute attr = mesh.getVertexAttributes().get(i);
				if (attr.usage == Usage.BoneWeight)
					bone_num++;
			}
					
			ArrayList<float[]> vList = new ArrayList<float[]>();
			
			for (int index = ren.meshPartOffset; index < ren.meshPartOffset+ren.meshPartSize; index++)
			{
				int i = indices[index];
				if (used[i]) continue;
				used[i] = true;
				
				float[] varr = new float[4+bone_num*2];
				varr[0] = r;
				varr[1] = vertices[poff+(i*vsize)+0];
				varr[2] = vertices[poff+(i*vsize)+1];
				varr[3] = vertices[poff+(i*vsize)+2];
				
				if (bone_num > 0)
				{
					int idx = 4;
					for (int ii = 0; ii < n; ii++) {
						final VertexAttribute attr = mesh.getVertexAttributes().get(ii);
						int offset = attr.offset / 4;
						if (attr.usage == Usage.BoneWeight)
						{
							varr[idx++] = vertices[offset+(i*vsize)+0];
							varr[idx++] = vertices[offset+(i*vsize)+1];
						}
					}
				}
				vList.add(varr);
			}
			
			varrays[r] = vList.toArray(new float[vList.size()][]);
			total += vList.size();
		}
		
		varray = new float[total][];
		int index = 0;
		for (int i = 0; i < varrays.length; i++)
		{
			for (int j = 0; j < varrays[i].length; j++)
			{
				varray[index++] = varrays[i][j];
			}
		}
		
		FileUtils.storeVertexArray(name, varray);
		
		return varray;
	}

	@Override
	public Vector3 getTransformedVertex(float[] values, Vector3 out)
	{
		Matrix4 idtMatrix = tmpMat2.idt();
		Matrix4 skinningMat = tmpMat;
		
		for (int i = 0; i < 16; i++) skinningMat.val[i] = 0;
		Renderable r = renderables.get((int) values[0]);
		int numbones = ( values.length - 4 ) / 2;
		
		float weight = 0;
		for (int b = 0; b < numbones; b++)
		{
			int bmatx = (int) values[4+(b*2)+0];
			Matrix4 bmat = r.bones == null || bmatx >= r.bones.length || r.bones[bmatx] == null ? idtMatrix : r.bones[bmatx];
			weight += values[4+(b*2)+1];
			for (int i = 0; i < 16; i++)
			{
				skinningMat.val[i] += values[4+(b*2)+1] * bmat.val[i];
			}
		}
		
		weight = 1.0f - weight;
		
		for (int i = 0; i < 16; i++)
		{
			skinningMat.val[i] += weight * idtMatrix.val[i];
		}
		 
		out.set(values[1], values[2], values[3]).mul(skinningMat).mul(r.worldTransform);
		
		return out;
	}
}
