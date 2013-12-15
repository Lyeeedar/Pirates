package com.Lyeeedar.Entities.Spells;

public class SpellAI_FadeOut extends SpellAI {

	float cd = 0;
	boolean done = false;
	
	public SpellAI_FadeOut()
	{

	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		if (!done)
		{
			cd = spell.effect.getMaxLife();
			spell.effect.setEmit(false);
			done = true;
		}
		
		cd -= delta;
		
		return cd > 0;
		
	}

	@Override
	public void dispose() {
	}

}
