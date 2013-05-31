package com.lyeeedar.Pirates;

import java.util.HashMap;
import java.util.Map.Entry;

import Screens.AbstractScreen;
import Screens.GameScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

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
		
		screens.put(Screen.GAME, new GameScreen());
		
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
