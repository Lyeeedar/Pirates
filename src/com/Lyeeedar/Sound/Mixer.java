package com.Lyeeedar.Sound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class Mixer {
	String loc;
	Music music;
	float volume;
	
	String mixLoc;
	Music mix;
	float mixTime;
	float time;
	
	public Mixer(String loc, float volume)
	{
		this.loc = loc;
		this.volume = volume;
		
		music = Gdx.audio.newMusic(Gdx.files.internal(loc));
		music.play();
		music.setLooping(true);
		music.setVolume(volume);
	}
	
	public void mix(String loc, float time)
	{
		mixLoc = loc;
		mix = Gdx.audio.newMusic(Gdx.files.internal(mixLoc));
		mix.play();
		mix.setLooping(true);
		mix.setVolume(0);
		
		mixTime = time;
		this.time = 0;
	}
	
	public void update(float delta)
	{
		if (mix != null)
		{
			time += delta;
			
			float vol = volume * (time/mixTime);
			mix.setVolume(vol);
			music.setVolume(volume-vol);
			
			if (time >= mixTime)
			{
				loc = mixLoc;
				music.stop();
				music.dispose();
				music = mix;
				mix = null;
				music.setVolume(volume);
			}
		}
	}
}
