package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Graphics.MotionTrail;
import com.badlogic.gdx.math.Vector3;

public class Weapon {

	private final MotionTrail trail;
	private final Vector3 tmp1 = new Vector3();
	private final Vector3 tmp2 = new Vector3();
	
	public Weapon()
	{
		trail = new MotionTrail();
	}

}
