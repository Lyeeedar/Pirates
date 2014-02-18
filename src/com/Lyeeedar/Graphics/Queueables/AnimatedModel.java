package com.Lyeeedar.Graphics.Queueables;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController.AnimationDesc;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class AnimatedModel implements Queueable {
	
	public ModelInstance model;
	public AnimationController anim;
	public Texture[] textures;
	public Vector3 colour = new Vector3();
	public Array<ATTACHED_MODEL> attachedModels = new Array<ATTACHED_MODEL>();
	public String defaultAnim;
	public String currentAnim = "";
	public Matrix4 transform = new Matrix4();
	
	private AnimationDesc current;
	
	private final Matrix4 tmpMat = new Matrix4();
	
	public AnimatedModel(Model model, Texture[] textures, Vector3 colour, String defaultAnim)
	{		
		this.model = new ModelInstance(model);
		
		anim = new AnimationController(this.model);
		if (defaultAnim != null)
		{
			anim.setAnimation(defaultAnim, -1);
		}
		this.textures = textures;
		this.colour.set(colour);
		this.defaultAnim = defaultAnim;
	}
	
	public void attach(String nodeName, Queueable model, Matrix4 offset)
	{
		Node node = null;
		if (nodeName != null)
		{
			 node = this.model.getNode(nodeName, true);
			 if (node == null) System.err.println("Failed to find node "+nodeName);
		}
		attachedModels.add(new ATTACHED_MODEL(node, model, offset));
	}
	
	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).add(model, textures, colour);
		
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
		float dur = ca.duration;
		return ( ( ca.time ) / ( ( dur - ca.offset ) / ca.speed ) ) * 100.0f;
	}

	@Override
	public void set(Entity source, Vector3 offset) 
	{
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			transform.set(source.readOnlyRead(PositionalData.class).composed).translate(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			transform.setToTranslation(data.position.x, data.position.y, data.position.z).translate(offset);
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
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.queueable.update(delta, cam, lights);
		}
	}

	@Override
	public Queueable copy() {
		AnimatedModel nm = new AnimatedModel(model.model, textures, colour, defaultAnim);
		for (ATTACHED_MODEL am : attachedModels)
		{
			nm.attach(am.node.id, am.queueable.copy(), am.offset);
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
		
		public ATTACHED_MODEL(Node node, Queueable model, Matrix4 offset)
		{
			this.node = node;
			this.queueable = model;
			this.offset.set(offset);
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
	public Vector3[] getVertexArray()
	{

		Vector3[][] varrays = new Vector3[model.model.meshes.size][];
		int total = 0;
		
		for (int r = 0; r < model.model.meshes.size; r++)
		{
			Mesh mesh = model.model.meshes.get(r);
			
			final int nverts = mesh.getNumVertices();
			final int vsize = mesh.getVertexSize();
			float[] vertices = mesh.getVertices(new float[nverts*vsize]);
			int poff = mesh.getVertexAttributes().getOffset(Usage.Position);
			
			Vector3[] varray = new Vector3[nverts];
			
			for (int i = 0; i < nverts; i++)
			{
				varray[i] = new Vector3(
						vertices[poff+(i*vsize)+0],
						vertices[poff+(i*vsize)+1],
						vertices[poff+(i*vsize)+2]
						);
			}
			
			varrays[r] = varray;
			total += varray.length;
		}
		
		Vector3[] varray = new Vector3[total];
		int index = 0;
		for (int i = 0; i < varrays.length; i++)
		{
			System.arraycopy(varrays[i], 0, varray, index, varrays[i].length);
			index += varrays[i].length;
		}
		
		return varray;
	}
}
