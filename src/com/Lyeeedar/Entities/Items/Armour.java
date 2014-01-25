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
	
	public Armour(SPRITESHEET spritesheet, DESCRIPTION desc)
	{
		super(spritesheet, desc);
	}
	
	@Override
	public Item set(Item other) {
		super.set(other);
		return this;
	}

	@Override
	public Item copy() {
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
