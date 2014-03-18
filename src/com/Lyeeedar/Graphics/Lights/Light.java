package com.Lyeeedar.Graphics.Lights;

import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector3;

public abstract class Light {
	
	public final Vector3 colour = new Vector3();
		
	public OcttreeEntry<Light> entry;
	
	public Light(Vector3 colour)
	{
		this.colour.set(colour);
	}
	
	public abstract void createEntry(Octtree<Light> octtree);
	public abstract void render();
}
