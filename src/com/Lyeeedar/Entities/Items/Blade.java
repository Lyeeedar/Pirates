package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;


public class Blade extends Weapon<Blade> {
	
	public final Ray edge = new Ray(new Vector3(), new Vector3(GLOBALS.DEFAULT_ROTATION));
	public float dist;
	
	private boolean swinging = false;
	private float angle = 0;
	
	private final Vector3 axis = new Vector3(0, 1, 0);
	
	private final Matrix4 tmpMat = new Matrix4();

	public Blade(float dist)
	{
		this.dist = dist;
	}

	@Override
	public void set(Blade other) {
		System.out.println("copy");
		System.out.println(edge+"     "+other.edge);
		edge.set(other.edge);
		dist = other.dist;
		swinging = other.swinging;
	}

	@Override
	public Blade newInstance() {
		return new Blade(dist);
	}

	@Override
	public void update(float delta) {
		angle += delta*100;
		if (angle > Math.PI*2) angle = 0;
		
		tmpMat.setToRotation(axis, angle);
		
		edge.direction.set(0, 1, 0).mul(tmpMat).nor();
		System.out.println(edge.direction);
	}

}
