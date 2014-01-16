package com.Lyeeedar.Graphics;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;

public class AnimatedModel implements Renderable {
	
	ModelInstance model;
	AnimationController anim;
	
	public AnimatedModel(Model model)
	{
		this.model = new ModelInstance(model);
		anim = new AnimationController(this.model);
		anim.setAnimation("walk", -1);
	}

	@Override
	public void queue(float delta, Camera cam, HashMap<Class, Batch> batches) {
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).add(model);
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
		return new AnimatedModel(model.model);
	}

	@Override
	public void dispose() {
	}

}
