package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;


public class Blade extends Weapon<Blade> {
	
	public final Ray edge = new Ray(new Vector3(), new Vector3(GLOBALS.DEFAULT_ROTATION));
	public float dist;
	
	private float hitCD = 0;
	private float hitSpeed = 0.2f;
	
	public boolean swinging = false;
	private float angle = 0;
	
	private final Matrix4 tmpMat = new Matrix4();
	
	private PositionalData pData = new PositionalData();
	private StatusData sData = new StatusData();
	
	private Box box = new Box(new Vector3(), 0.5f, 0.5f, 0.5f);

	public Blade(float dist)
	{
		this.dist = dist;
	}

	@Override
	public void set(Blade other) {
		edge.set(other.edge);
		dist = other.dist;
		swinging = other.swinging;
	}

	@Override
	public Blade newInstance() {
		return new Blade(dist);
	}

	@Override
	public void update(float delta, Entity entity) {
		
		if (!swinging) 
		{
			angle = 0;
			return;
		}
		
		hitCD -= delta;
		
		if (hitCD < 0)
		{
			
			entity.readData(pData, PositionalData.class);
			
			box.center.set(pData.rotation).scl(1.0f).add(pData.position);
			
			EntityGraph graph = GLOBALS.WORLD.collide(box, pData.graph);
			
			if (graph != null && graph.entity != null) 
			{
				graph.entity.readData(sData, StatusData.class);
				sData.damage = 1;
				graph.entity.writeData(sData, StatusData.class);
			}
			hitCD = hitSpeed;
		}
		
		angle += delta*1000;
		if (angle > 360) angle = 0;
		
		tmpMat.idt().rotate(-1, 1, 0, -angle);
		
		edge.origin.set(0, 1, 0);
		edge.direction.set(0, 0, -1).mul(tmpMat).nor();
	}

	@Override
	public void use() {
		swinging = true;
	}

	@Override
	public void stopUsing() {
		swinging = false;
	}

	@Override
	public void dispose() {
		pData.dispose();
		sData.dispose();
	}

}
