package com.Lyeeedar.Graphics;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;

public class AnimatedModel implements Renderable {
	
	public ModelInstance model;
	public AnimationController anim;
	public Texture texture;
	public Vector3 colour = new Vector3();
	
	public AnimatedModel(Model model, Texture texture, Vector3 colour)
	{
		this.model = new ModelInstance(model);
		anim = new AnimationController(this.model);
		anim.setAnimation("walk", -1);
		this.texture = texture;
		this.colour.set(colour);
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).add(model, texture, colour);
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
		if (aData.updateAnimations) anim.animate(aData.anim, -1, aData.animate_speed, null, 0.1f);
	}

	@Override
	public void update(float delta, Camera cam, LightManager lights) {
		if (anim != null) anim.update(delta);
	}

	@Override
	public Renderable copy() {
		return new AnimatedModel(model.model, texture, colour);
	}

	@Override
	public void dispose() {
	}

}
