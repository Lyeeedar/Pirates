package com.Lyeeedar.Graphics.Batchers;

import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class DecalBatcher implements Batch {

	public final DecalBatch batch;
	
	public DecalBatcher(DecalBatch batch)
	{
		this.batch = batch;
	}
	
	public void add(Decal d)
	{
		batch.add(d);
	}
	
	public void flush()
	{
		batch.flush();
	}
}
