package com.Lyeeedar.Entities.Spells;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.badlogic.gdx.math.Vector3;

public class Spell {
	
	public Vector3 position = new Vector3();
	public SpellAI ai;
	public ParticleEffect effect;
	public Entity caster;
	
	public Spell(Vector3 position, SpellAI ai, ParticleEffect effect, Entity caster)
	{
		this.position.set(position);
		this.ai = ai;
		this.effect = effect;
		this.caster = caster;
	}
	
	public boolean update(float delta)
	{
		boolean alive = ai.update(delta, this);
		effect.setPosition(position);
		return alive;
	}

	public void dispose()
	{
		ai.dispose();
		effect.dispose();
	}
}
