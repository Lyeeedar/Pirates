package com.Lyeeedar.Graphics;

import com.Lyeeedar.Util.FollowCam;


public class SkyBox {
	
	public Sea sea;
	public Weather weather;
	
	// Cloud clouds;
	
	public SkyBox(Sea sea, Weather weather)
	{
		this.sea = sea;
		this.weather = weather;
	}
	
	public void update(float delta, FollowCam cam)
	{
		weather.update(delta, cam);
	}
}
