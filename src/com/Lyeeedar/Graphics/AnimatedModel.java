package com.Lyeeedar.Graphics;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.AnimatedModelBatch.RenderablePool;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
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
	public Texture texture;
	public Vector3 colour = new Vector3();
	public Array<ATTACHED_MODEL> attachedModels = new Array<ATTACHED_MODEL>();
	public String defaultAnim;
	public Matrix4 tmp = new Matrix4();
	
	public AnimatedModel(Model model, Texture texture, Vector3 colour, String defaultAnim)
	{
		for (Node n : model.nodes)
		{
			print_nodes(n);
		}
		
		Animation a = model.getAnimation("idle");
		if (a != null)
		{
			System.out.println("anim");
			for (NodeAnimation an : a.nodeAnimations)
			{
				System.out.println(an.node.id);
			}
			System.out.println("anim");
		}
		
		this.model = new ModelInstance(model);
		
		anim = new AnimationController(this.model);
		if (defaultAnim != null)
		{
			anim.setAnimation(defaultAnim, -1);
		}
		this.texture = texture;
		this.colour.set(colour);
		this.defaultAnim = defaultAnim;
	}

	public void print_nodes(Node n)
	{
		System.out.println(n.id + " " + n.parts.size);
		for (Node nn : n.children)
		{
			print_nodes(nn);
		}
	}
	
	public void attachModel(String node, AnimatedModel model, Matrix4 offset)
	{
		attachedModels.add(new ATTACHED_MODEL(this.model.getNode(node, true), model, offset));
	}
	
	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).add(model, texture, colour);
		
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
		if (anim != null && defaultAnim != null && aData.updateAnimations && model.getAnimation(aData.anim) != null) anim.animate(aData.anim, -1, aData.animate_speed, null, 0.1f);
		
		for (ATTACHED_MODEL am : attachedModels)
		{
			am.model.set(source, offset);
			am.model.transform(am.node.globalTransform);
			am.model.transform(am.offset);
		}
	}
	
	public void transform(Matrix4 mat)
	{
		model.transform.mul(mat);
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
		return new AnimatedModel(model.model, texture, colour, defaultAnim);
	}

	@Override
	public void dispose() {
	}

	private class ATTACHED_MODEL
	{
		public Node node;
		public AnimatedModel model;
		public Matrix4 offset = new Matrix4();
		
		public ATTACHED_MODEL(Node node, AnimatedModel model, Matrix4 offset)
		{
			this.node = node;
			this.model = model;
			this.offset.set(offset);
		}
	}
}
