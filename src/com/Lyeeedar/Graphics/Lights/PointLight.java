package com.Lyeeedar.Graphics.Lights;

import com.Lyeeedar.Collision.Octtree;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class PointLight extends Light
{
	Vector3 position = new Vector3();
	float attenuation;
	public PointLight(Vector3 position, Vector3 colour, float attenuation)
	{
		super(colour);
		
		this.position.set(position);
		this.attenuation = attenuation;
	}

	@Override
	public void createEntry(Octtree<Light> octtree)
	{
		float len = colour.len();
		
		float dst = len / ( 0.1f * attenuation ) ;
		
		dst /= 2.0f;
		
		this.entry = octtree.createEntry(this, position, new Vector3(dst, dst, dst), Octtree.MASK_POINT_LIGHT);
	}

	@Override
	public void computeShadowMap(Camera cam)
	{
		// TODO Auto-generated method stub
		
	}
}
