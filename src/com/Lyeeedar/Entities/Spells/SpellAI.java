package com.Lyeeedar.Entities.Spells;

public abstract class SpellAI {
	
	public abstract boolean update(float delta, Spell spell);
	public abstract void dispose();

}
