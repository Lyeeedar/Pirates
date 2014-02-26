package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SPRITESHEET;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;


public abstract class Equipment<E extends Equipment<E>> extends Item 
{
	public StatusData statusModifier = new StatusData();
	public EquipmentGraphics equipmentGraphics;
	public Equipment_Slot equipped;
	
	public Equipment()
	{
		super();
	}
	
	public Equipment(EquipmentGraphics equipmentGraphics, DESCRIPTION desc)
	{
		super(desc);
		this.equipmentGraphics = equipmentGraphics;
	}
	
	public void playAnimation(AnimationData aData, String animationName)
	{
		if (animationName == null) return;
		
		aData.animationLock = true;
		aData.anim = animationName;
		aData.playAnimation = 0;
		aData.nextAnimation = aData.animation;
		aData.useDirection = true;
	}
	
	@Override
	public Item set(Item other)
	{
		super.set(other);
		Equipment<E> cother = (Equipment<E>) other;
		cother.equipmentGraphics = equipmentGraphics;
		return this;
	}
	
	public abstract void update(float delta, Entity entity);
	public abstract void use();
	public abstract void stopUsing();
	public abstract void dispose();
	public abstract void addRequiredQueueables(AnimatedModel model);
	
	public static class EquipmentGraphics
	{
		public Array<String> textureNames = new Array<String>();
		public Array<EquipmentModel> models = new Array<EquipmentModel>();
		
		public EquipmentGraphics(String tex, EquipmentModel model)
		{
			if (tex != null) textureNames.add(tex);
			if (model != null) models.add(model);
		}
	}
	
	public static class EquipmentModel
	{
		public final String modelName;
		public final String[] textureNames;
		public final String defaultAnim;
		public final Vector3 colour = new Vector3();
		public final String nodeName;
		public final Matrix4 transform = new Matrix4();
		
		public EquipmentModel(String modelName, String[] textureNames, String defaultAnim, Vector3 colour, String nodeName, Matrix4 transform)
		{
			this.modelName = modelName;
			this.textureNames = textureNames;
			this.defaultAnim = defaultAnim;
			this.colour.set(colour);
			this.nodeName = nodeName;
			this.transform.set(transform);
		}
	}
}
