package com.Lyeeedar.Entities.Spells;

import com.badlogic.gdx.graphics.Camera;

public abstract class SpellAI {
	
	public abstract boolean update(float delta, Spell spell, Camera cam);
	public abstract void dispose();

}
