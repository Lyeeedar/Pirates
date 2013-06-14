package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.Sphere;
import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.Items.Blade;
import com.Lyeeedar.Graphics.Model;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Sprite3D;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Graphics.WeaponTrail;
import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.ProceduralGeneration.IslandGenerator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class GameScreen extends AbstractScreen {
	
	SkyBox skyBox;
	PositionalData dataPos = new PositionalData();
	int numEntities = 5;
	
	long time = System.currentTimeMillis();
	
	Entity player;

	public GameScreen(LightManager lights) {
		super(lights);
		this.lights = lights;
	}

	@Override
	public void create() {
		
		long time = System.nanoTime();
		
		Texture texture = GLOBALS.ASSET_MANAGER.get("data/textures/grass.png", Texture.class);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		IslandGenerator ig = new IslandGenerator();
		Mesh model = ig.getIsland(40, 40, 40);

		SymbolicMesh mesh = SymbolicMesh.getSymbolicMesh(model, 1f);
		
		Entity island = new Entity();
		island.addRenderable(new Model(model, GL20.GL_TRIANGLES, texture, new Vector3(1, 1, 1), 1));
		island.setCollisionShapeInternal(mesh);
		
		GLOBALS.WORLD.addEntity(island);

		Texture tex = new Texture(Gdx.files.internal("data/textures/test.png"));
		skyBox = new SkyBox(tex, new Vector3(0.0f, 0.79f, 1), new Vector3(1, 1, 1));
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		Sprite3D s = new Sprite3D(1, 2);
		s.setGender(false);
		s.addAnimation("move");
		s.addAnimation("attack_1");
		s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
		s.create(GLOBALS.ASSET_MANAGER);
		player.addRenderable(s);
		player.addRenderable(new WeaponTrail(Equipment_Slot.RARM, 100, Color.WHITE, GLOBALS.ASSET_MANAGER.get("data/textures/gradient.png", Texture.class), 0.01f));
		player.setCollisionShape(new Sphere(new Vector3(0, 0, 0), 0.1f));
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.addEquipment(Equipment_Slot.RARM, new Blade(1));
		player.writeData(eData, EquipmentData.class);
		
		GLOBALS.WORLD.addEntity(player);
		
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			//ge.setAI(ai);
			ge.setCollisionShape(new Sphere(new Vector3(0, 0, 0), 0.1f));
			
			//GLOBALS.WORLD.addEntity(ge);
			
			s = new Sprite3D(1, 2);
			s.setGender(false);
			s.addAnimation("move");
			s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
			s.create(GLOBALS.ASSET_MANAGER);
			
			ge.addRenderable(s);
		}

		System.out.println("Load time: "+(System.nanoTime()-time)/1000000);
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
	
	}

	@Override
	public void drawSkybox(float delta)
	{
		skyBox.render(cam);
	}
	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueRenderables(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		
		GLOBALS.WORLD.queueRenderables(cam, delta, modelBatch, decalBatch, trailBatch);
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
	@Override
	public void show() {
		Gdx.input.setCursorCatched(true);

	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub
		
	}

	LinkedList<Runnable> list = new LinkedList<Runnable>();
	@Override
	public void update(float delta) {
		
		GLOBALS.WORLD.getRunnable(list, delta);
		GLOBALS.submitTasks(list);
		list.clear();
		GLOBALS.waitTillTasksComplete();
		
		skyBox.update(delta);
		
		player.readData(dataPos, PositionalData.class);
		
		cam.update(dataPos);
	}

}
