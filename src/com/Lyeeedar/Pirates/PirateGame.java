package com.Lyeeedar.Pirates;

import java.util.HashMap;
import java.util.Map.Entry;

import com.Lyeeedar.Collision.BulletTest;
import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Screens.AbstractScreen;
import com.Lyeeedar.Screens.GameScreen;
import com.Lyeeedar.Screens.InventoryScreen;
import com.Lyeeedar.Screens.MainMenuScreen;
import com.Lyeeedar.Sound.Mixer;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;

public class PirateGame extends Game {
	
	public enum Screen {
		MAINMENU,
		GAME,
		GAMEMENU,
		INVENTORY,
		OPTIONS
	}
	
	public final HashMap<Screen, AbstractScreen> screens = new HashMap<Screen, AbstractScreen>();

	@Override
	public void create() {
		
		Bullet.init();
//		GLOBALS.queue_all_assets();
//		GLOBALS.ASSET_MANAGER.finishLoading();
		
		//GLOBALS.mixer = new Mixer("data/bgm/Skye Cuillin.mp3", 1.0f);
		
		GLOBALS.RESOLUTION[0] = Gdx.graphics.getWidth();
		GLOBALS.RESOLUTION[1] = Gdx.graphics.getHeight();
		
		GLOBALS.LIGHTS.ambientColour.set(0.8f, 0.9f, 0.7f);
		GLOBALS.LIGHTS.directionalLight.colour.set(0.01f, 0.01f, 0.01f);
		GLOBALS.LIGHTS.directionalLight.direction.set(0.0f, 1.0f, 0.0f);
		
		Light l = new Light(new Vector3(), new Vector3(0.4f, 0.4f, 0.4f), 0.2f);
		
		GLOBALS.LIGHTS.add(l);
		
		screens.put(Screen.GAME, new GameScreen(this));
		screens.put(Screen.MAINMENU, new MainMenuScreen(this));
		screens.put(Screen.INVENTORY, new InventoryScreen(this));
		
		for (Entry<Screen, AbstractScreen> entry : screens.entrySet())
		{
			entry.getValue().create();
		}
				
		switchScreen(Screen.MAINMENU);
	}

	public void switchScreen(Screen screen)
	{
		this.setScreen(screens.get(screen));
	}
}
