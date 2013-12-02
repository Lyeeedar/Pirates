package com.Lyeeedar.Screens;

import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.PirateGame.Screen;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class MainMenuScreen extends AbstractScreen {
	
	private Table table;
	private PositionalData pData = new PositionalData();
	private SkyBox skybox;
	
	public MainMenuScreen(PirateGame game)
	{
		super(game);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
		GLOBALS.SKYBOX = skybox;
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		Label lblTitle = new Label("Pirates! Arrrrrr!", skin);
		lblTitle.setFontScale(5);
		
		TextButton btnContinue = new TextButton("Continue", skin);
		btnContinue.getLabel().setAlignment(Align.left, Align.left);
		btnContinue.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return false;
			}
		});
		
		TextButton btnNewGame = new TextButton("New Game", skin);
		btnNewGame.getLabel().setAlignment(Align.left, Align.left);
		btnNewGame.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(Screen.GAME);
				return false;
			}
		});

		
		TextButton btnOptions = new TextButton("Options", skin);
		btnOptions.getLabel().setAlignment(Align.left, Align.left);
		btnOptions.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return false;
			}
		});
		
		TextButton btnCredits = new TextButton("Credits", skin);
		btnCredits.getLabel().setAlignment(Align.left, Align.left);
		btnCredits.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return false;
			}
		});
		
		TextButton btnExit = new TextButton("Exit", skin);
		btnExit.getLabel().setAlignment(Align.left, Align.left);
		btnExit.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				Gdx.app.exit();
				return false;
			}
		});

		int width = 300;
		int height = 50;
		int lpad = 30;
		int bpad = 15;
		
		table = new Table();
		
		Table btable = new Table();
		//table.debug();
		//btable.debug();
		
		table.add(lblTitle).center().pad(50).expandX().top();
		table.row();
		
		btable.add(btnContinue).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnNewGame).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnOptions).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnCredits).width(width).height(height).padBottom(bpad).padLeft(lpad);
		btable.row();
		btable.add(btnExit).width(width).height(height).padBottom(bpad).padLeft(lpad);
		
		table.add(btable).left().expandY();
		table.row();
		
		table.add(new Label("Version: Alpha 01", skin)).padTop(25).bottom();

		table.setFillParent(true);
		stage.addActor(table);
		
		Texture skytex = new Texture(Gdx.files.internal("data/textures/sky.png"));
		Texture glowtex = new Texture(Gdx.files.internal("data/textures/glow.png"));
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Weather weather = new Weather(skytex, glowtex);
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f));
		skybox = new SkyBox(sea, weather);
		
		pData.position.set(0, 10, 0);
		((FollowCam)cam).setAngle(0);
	}

	@Override
	public void drawSkybox(float delta) {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		GLOBALS.SKYBOX.sea.render(cam, cam.position, GLOBALS.LIGHTS);
	}

	@Override
	public void queueRenderables(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawParticles(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
		Table.drawDebug(stage);
	}

	boolean increase = true;
	@Override
	public void update(float delta) {
		GLOBALS.SKYBOX.update(delta);
		((FollowCam)cam).updateBasic(pData);
		
		delta /= 5;
		
		if (increase) 
		{
			GLOBALS.LIGHTS.directionalLight.direction.y += delta;
			
			if (GLOBALS.LIGHTS.directionalLight.direction.y < 0.0f) 
			{
				GLOBALS.LIGHTS.directionalLight.direction.z += delta;
			}
			else
			{
				GLOBALS.LIGHTS.directionalLight.direction.z -= delta;
			}
		}
		else 
		{
			GLOBALS.LIGHTS.directionalLight.direction.y -= delta;
			
			if (GLOBALS.LIGHTS.directionalLight.direction.y < 0.0f) 
			{
				GLOBALS.LIGHTS.directionalLight.direction.z += delta;
			}
			else
			{
				GLOBALS.LIGHTS.directionalLight.direction.z -= delta;
			}
		}
		
		if (GLOBALS.LIGHTS.directionalLight.direction.y >= 1.0f) increase = false;
		if (GLOBALS.LIGHTS.directionalLight.direction.y < -1) increase = true;
		
		if (GLOBALS.LIGHTS.directionalLight.direction.y > 0.1f)
		{
			GLOBALS.LIGHTS.ambientColour.set(1.0f, 1.0f, 1.0f);
		}
		else if (GLOBALS.LIGHTS.directionalLight.direction.y < -0.5f)
		{
			GLOBALS.LIGHTS.ambientColour.set(0.0f, 0.0f, 0.0f);
		}
		else
		{
			float brightness = (GLOBALS.LIGHTS.directionalLight.direction.y+0.5f) / 0.6f;
			GLOBALS.LIGHTS.ambientColour.set(brightness, brightness, brightness);
		}
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub

	}

}
