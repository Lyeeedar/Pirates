package com.Lyeeedar.Entities.Spells;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.badlogic.gdx.math.Vector3;

public class Spell {
	
	public Vector3 position = new Vector3();
	public SpellAI aimove;
	public SpellAI aidam;
	public ParticleEffect effect;
	public Entity caster;
	
	int stage = 0;
	
	public Spell(Vector3 position, SpellAI aimove, SpellAI aidam, ParticleEffect effect, Entity caster)
	{
		this.position.set(position);
		this.aimove = aimove;
		this.aidam = aidam;
		this.effect = effect;
		this.caster = caster;
	}
	
	public boolean update(float delta)
	{
		boolean alive = true;
		if (stage == 0)
		{
			boolean collide = aimove.update(delta, this);
			if (!collide) stage = 1;
		}
		else if (stage == 1)
		{
			alive = aidam.update(delta, this);
		}
		
		effect.setPosition(position);
		return alive;
	}
	
	public void dispose()
	{
		aimove.dispose();
		aidam.dispose();
		effect.dispose();
	}
}
