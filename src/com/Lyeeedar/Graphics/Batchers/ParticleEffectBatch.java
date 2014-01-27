package com.Lyeeedar.Graphics.Batchers;

import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.badlogic.gdx.graphics.Camera;

public class ParticleEffectBatch implements Batch
{
	public final PriorityQueue<ParticleEmitter> emitters = new PriorityQueue<ParticleEmitter>();
	public int particleNum;
	
	public ParticleEffectBatch()
	{
		
	}
	
	public void add(ParticleEmitter emitter)
	{
		emitters.add(emitter);
	}
	
	public void render(Camera cam)
	{
		ParticleEmitter.begin(cam);
		particleNum = 0;
		
		while (!emitters.isEmpty())
		{
			ParticleEmitter emitter = emitters.poll();
			emitter.render();
			particleNum += emitter.getActiveParticles();
		}
		
		ParticleEmitter.end();
	}
}
