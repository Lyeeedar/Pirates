package com.Lyeeedar.Entities.Spells;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Sound.Sound3D;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class Spell {
	
	public Vector3 position = new Vector3();
	public SpellAI[] ais;
	public ParticleEffect effect;
	public Entity caster;
	
	int stage = 0;
	
	public Spell(Vector3 position, ParticleEffect effect, Entity caster,  SpellAI... ais)
	{
		this.position.set(position);
		this.ais = ais;
		this.effect = effect;
		this.caster = caster;
	}
	
	public boolean update(float delta, Camera cam)
	{	
		if (stage < ais.length)
		{
			boolean alive = ais[stage].update(delta, this, cam);
			if (!alive) stage++;
			effect.setPosition(position);
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	public void dispose()
	{
		for (SpellAI ai : ais) ai.dispose();
		effect.dispose();
	}
}
