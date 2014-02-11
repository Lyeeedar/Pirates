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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
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
			am.model.queue(delta, cam, batches);
		}
	}

	@Override
	public void set(Entity source, Vector3 offset) {
		if (source.readOnlyRead(PositionalData.class) != null)
		{
			model.transform.set(source.readOnlyRead(PositionalData.class).composed).translate(offset);
		}
		else
		{
			MinimalPositionalData data = source.readOnlyRead(MinimalPositionalData.class);
			model.transform.setToTranslation(data.position.x, data.position.y, data.position.z).translate(offset);
		}
		
		AnimationData aData = source.readOnlyRead(AnimationData.class);
		colour.set(aData.colour);
		if(aData.animationLock && model.getAnimation(aData.anim) != null)
		{
			anim.animate(aData.anim, 1, aData.animate_speed, aData.listener, 0.1f);
			currentAnim = "";
		}
		else if (!currentAnim.equals(aData.anim))
		{
			if (model.getAnimation(aData.anim) != null)
			{
				anim.animate(aData.anim, -1, aData.animate_speed, aData.listener, 0.1f);
				currentAnim = aData.anim;
			}
			else if (!currentAnim.equals(aData.base_anim) && model.getAnimation(aData.base_anim) != null)
			{
				anim.animate(aData.base_anim, -1, aData.animate_speed, aData.listener, 0.1f);
				currentAnim = aData.base_anim;
			}
		}
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.set(source, offset);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.model.set(tmpMat);
			}
			am.model.transform(am.offset);
		}
	}
	
	@Override
	public void transform(Matrix4 mat)
	{
		model.transform.mul(mat);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.transform(mat);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.model.set(tmpMat);
			}
			am.model.transform(am.offset);
		}
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		anim.update(delta);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.update(delta, cam, lights);
		}
	}

	@Override
	public Queueable copy() {
		AnimatedModel nm = new AnimatedModel(model.model, textures, colour, defaultAnim);
		for (ATTACHED_MODEL am : attachedModels)
		{
			nm.attach(am.node.id, am.model.copy(), am.offset);
		}
		return nm;
	}

	@Override
	public void dispose() {
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.dispose();
		}
	}

	private static class ATTACHED_MODEL
	{
		public Node node;
		public Queueable model;
		public Matrix4 offset = new Matrix4();
		
		public ATTACHED_MODEL(Node node, Queueable model, Matrix4 offset)
		{
			this.node = node;
			this.model = model;
			this.offset.set(offset);
		}
	}

	@Override
	public void set(Matrix4 transform)
	{
		model.transform.set(transform);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.set(transform);
			if(am.node != null) 
			{
				tmpMat.set(model.transform).mul(am.node.globalTransform);
				am.model.set(tmpMat);
			}
			am.model.transform(am.offset);
		}
		
	}
}
