package com.Lyeeedar.Graphics;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Items.Blade;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class WeaponTrail implements Renderable {
	
	private final EquipmentData eData = new EquipmentData();
	private final PositionalData pData = new PositionalData();
	
	private final Equipment_Slot slot;
	private final MotionTrail trail;
	private final float CD;
	
	private final Vector3 tmp1 = new Vector3();
	private final Vector3 tmp2 = new Vector3();
	
	private boolean swinging = false;
	
	private boolean update = false;
	private float cooldown = 0;
	private int count = 0;
	
	private int steps;
	
	public WeaponTrail(Equipment_Slot slot, int steps, Color tint, Texture texture, float cooldown)
	{
		this.slot = slot;
		this.trail = new MotionTrail(steps, tint, texture);
		this.CD = cooldown;
		
		this.steps = steps;
	}

	@Override
	public void queue(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		if (count <= steps) trailBatch.add(trail);
	}

	@Override
	public void set(Entity source) {
		if (update)
		{
			update = false;
			
			source.readData(eData, EquipmentData.class);
			source.readData(pData, PositionalData.class);
			
			@SuppressWarnings("rawtypes")
			Equipment e = eData.getEquipment(slot);
			if ((e == null) || !(e instanceof Blade))
			{
				String c = (e != null) ? ""+e.getClass() : "null" ;
				Gdx.app.error("WeaponTrail", "Equipped Item no longer a Blade. Instead it's a: " + c);
				this.dispose();
				source.removeRenderable(this);
			}
			
			Blade b = (Blade) e;
			
			if (swinging || b.swinging)
			{
				tmp1.set(b.edge.origin);
				tmp2.set(b.edge.direction).scl(b.dist).add(tmp1);
				
				tmp1.mul(pData.composed);
				tmp2.mul(pData.composed);
			}
			
			trail.update(tmp1, tmp2);
			
			if (b.swinging && !swinging) trail.reset(tmp1, tmp2);
			
			swinging = b.swinging;
			
			count ++;
			if (swinging) count = 0;
		}
	}

	@Override
	public void update(float delta, Camera cam) {
		cooldown -= delta;
		
		if (cooldown < 0) {
			update = true;
			cooldown = CD;
		}
	}

	@Override
	public void dispose()
	{
		trail.dispose();
	}
}
