package com.Lyeeedar.Sound;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class Sound3D {
	
	private static final float max = 1.0f;
	private static final float lower = 0.1f;
	
	final Sound sound;
	long id;
	final Vector3 position = new Vector3();
	float attenuation;
	float volume;
	boolean loop;
	
	boolean playing = false;
	
	float ldist;
	
	public Sound3D(Sound sound, float attenuation, float volume, boolean loop)
	{
		this.sound = sound;
		this.attenuation = attenuation;
		this.volume = volume;
		this.loop = loop;
	}
	
	public void play()
	{
		if (playing) return;
		id = sound.play();
		sound.setLooping(id, loop);
		playing = true;
	}
	
	public void stop()
	{
		sound.stop(id);
		playing = false;
	}
	
	public void setPosition(Vector3 position)
	{
		this.position.set(position);
	}

	public void update(float delta, Camera cam)
	{
		float dist = cam.position.dst(position);
		float vol = volume / (dist*attenuation);
		
		if (vol > lower) play();
		else if (vol < lower) stop();
		else if (!playing) return;
		
		float ddif = ldist-dist;
		if (ddif > max) ddif = max;
		if (ddif < -max) ddif = -max;
		ddif /= max;
		
		ddif += 1.0f;
		if (ddif < 0.5f) ddif = 0.5f;
		if (ddif > 1.0f) ddif = 2.0f;
		
		sound.setPitch(id, ddif);
		sound.setVolume(id, vol);
		
		ldist = dist;
	}
	
	public void dispose()
	{
		sound.stop(id);
	}
}
