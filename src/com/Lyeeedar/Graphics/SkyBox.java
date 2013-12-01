package com.Lyeeedar.Graphics;


public class SkyBox {
	
	public Sea sea;
	public Weather weather;
	
	// Cloud clouds;
	
	public SkyBox(Sea sea, Weather weather)
	{
		this.sea = sea;
		this.weather = weather;
	}
	
	public void update(float delta)
	{
		weather.update(delta);
	}
}
