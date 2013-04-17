package com.lyeeedar.Pirates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;

public class Controls {
	
	final boolean isAndroid;
	
	public Controls (boolean isAndroid)
	{
		this.isAndroid = isAndroid;
	}
	
	public boolean getActivate()
	{
		if (isAndroid)
		{
			return Gdx.input.justTouched();
		}
		else
		{
			return Gdx.input.isKeyPressed(Keys.E);
		}
	}
	
	public float getDeltaX()
	{
		if (isAndroid)
		{
			if (!Gdx.input.isTouched()) return 0;
			
			float max = 0;
			
			for (int i = 0; i < 3; i++)
			{
				if (!Gdx.input.isTouched(i)) continue;
				if (Math.abs(max) < Math.abs(Gdx.input.getX(i)-GLOBALS.SCREEN_SIZE[0]/2)) max = Gdx.input.getX(i)-GLOBALS.SCREEN_SIZE[0]/2;
			}
			
			if (Math.abs(max) < GLOBALS.SCREEN_SIZE[0]/6) max = 0;
			
			return max * Gdx.graphics.getDeltaTime();
		}
		else
		{
			return Gdx.input.getDeltaX();
		}
	}
	
	public float getDeltaY()
	{
		if (isAndroid)
		{
			return 0;
		}
		else
		{
			return Gdx.input.getDeltaY();
		}
	}
	
	public boolean leftClick()
	{
		if (isAndroid)
		{
			return false;
		}
		else
		{
			return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		}
	}
	
	public boolean rightClick()
	{
		if (isAndroid)
		{
			return false;
		}
		else
		{
			return Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
		}
	}
	
	public boolean left()
	{
		if (isAndroid)
		{
			return false;
		}
		else
		{
			return Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A);
		}
	}
	
	public boolean right()
	{
		if (isAndroid)
		{
			return false;
		}
		else
		{
			return Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D);
		}
	}
	
	public boolean up()
	{
		if (isAndroid)
		{
			if (!Gdx.input.isTouched()) return false;
			
			float max = 0;
			
			for (int i = 0; i < 3; i++)
			{
				if (!Gdx.input.isTouched(i)) continue;
				if (Math.abs(max) < Math.abs(Gdx.input.getY(i)-GLOBALS.SCREEN_SIZE[1]/2)) max = Gdx.input.getY(i)-GLOBALS.SCREEN_SIZE[1]/2;
			}
			
			return (max < -GLOBALS.SCREEN_SIZE[1]/6);
		}
		else
		{
			return Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W);
		}
	}
	
	public boolean down()
	{
		if (isAndroid)
		{
			if (!Gdx.input.isTouched()) return false;
			
			float max = 0;
			
			for (int i = 0; i < 3; i++)
			{
				if (!Gdx.input.isTouched(i)) continue;
				if (Math.abs(max) < Math.abs(Gdx.input.getY(i)-GLOBALS.SCREEN_SIZE[1]/2)) max = Gdx.input.getY(i)-GLOBALS.SCREEN_SIZE[1]/2;
			}
			
			return (max > GLOBALS.SCREEN_SIZE[1]/6);
		}
		else
		{
			return Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S);
		}
	}
	
	public boolean esc()
	{
		if (isAndroid)
		{
			return false;
		}
		else
		{
			return  Gdx.input.isKeyPressed(Keys.ESCAPE);
		}
	}

}
