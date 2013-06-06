package com.Lyeeedar.Pirates;

import java.util.HashMap;
import java.util.Map.Entry;

import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Screens.AbstractScreen;
import com.Lyeeedar.Screens.GameScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class PirateGame extends Game {
	
	public enum Screen {
		MAINMENU,
		GAME,
		GAMEMENU,
		OPTIONS
	}
	
	HashMap<Screen, AbstractScreen> screens = new HashMap<Screen, AbstractScreen>();

	@Override
	public void create() {
		
		GLOBALS.queue_all_assets();
		GLOBALS.ASSET_MANAGER.finishLoading();
		
		GLOBALS.RESOLUTION[0] = Gdx.graphics.getWidth();
		GLOBALS.RESOLUTION[1] = Gdx.graphics.getHeight();
		
		LightManager lights = new LightManager();
		lights.ambientColour.set(0.8f, 0.9f, 0.7f);
		lights.directionalLight.colour.set(0.2f, 0.3f, 0.2f);
		lights.directionalLight.direction.set(0, 0.5f, -0.5f);
		
		Light l = new Light(new Vector3(), new Vector3(0.4f, 0.4f, 0.4f), 0.5f);
		
		lights.add(l);
		
		screens.put(Screen.GAME, new GameScreen(lights));
		
		for (Entry<Screen, AbstractScreen> entry : screens.entrySet())
		{
			entry.getValue().create();
		}
		
		switchScreen(Screen.GAME);
	}

	public void switchScreen(Screen screen)
	{
		this.setScreen(screens.get(screen));
	}
}
