package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Color;

public class Armour extends Equipment<Armour> {

	public Armour()
	{
		super();
	}
	
	public Armour(String animationName, SPRITESHEET spritesheet, DESCRIPTION desc)
	{
		super(animationName, spritesheet, desc);
	}
	
	@Override
	public Armour set(Armour other) {
		super.sset(other);
		return this;
	}

	@Override
	public Armour copy() {
		return Pools.obtain(Armour.class).set(this);
	}

	@Override
	public void update(float delta, Entity entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void use() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopUsing() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
