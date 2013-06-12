package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.Items.Blade;
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

	Mesh model;
	Mesh character;
	Mesh plane;
	Texture texture;
	Texture texture1;
	Matrix4 tmpMat = new Matrix4();
	Matrix4 tmpMat1 = new Matrix4();
	Matrix4 tmpMat2 = new Matrix4();
	Vector3 colour = new Vector3();
	Vector3 tmp = new Vector3();
	PositionalData dataPos = new PositionalData();
	int numEntities = 5;
	
	ArrayList<Entity> entities = new ArrayList<Entity>();
	Light l;
	
	long time = System.currentTimeMillis();

	public GameScreen(LightManager lights) {
		super(lights);
		this.lights = lights;
	}

	@Override
	public void create() {
		
		long time = System.nanoTime();
		
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/models/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/textures/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/textures/blank.png"));
		
		IslandGenerator ig = new IslandGenerator();
		model = ig.getIsland(40, 40, 40);
		//texture = new Texture(Gdx.files.internal("data/shipTex.png"));
		//model = loader.loadObj(Gdx.files.internal("data/shipMesh.obj"), true).subMeshes[0].mesh;
		GLOBALS.TEST_NAV_MESH = SymbolicMesh.getSymbolicMesh(model, 1f);

		Texture tex = new Texture(Gdx.files.internal("data/textures/test.png"));
		skyBox = new SkyBox(tex, new Vector3(0.0f, 0.79f, 1), new Vector3(1, 1, 1));
		
		Entity player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		Sprite3D s = new Sprite3D(1, 2);
		s.setGender(false);
		s.addAnimation("move");
		s.addAnimation("attack_1");
		s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
		s.create(GLOBALS.ASSET_MANAGER);
		player.addRenderable(s);
		player.addRenderable(new WeaponTrail(Equipment_Slot.RARM, 100, Color.WHITE, GLOBALS.ASSET_MANAGER.get("data/textures/gradient.png", Texture.class), 0.01f));
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.addEquipment(Equipment_Slot.RARM, new Blade(1));
		player.writeData(eData, EquipmentData.class);
		
		entities.add(player);
		
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			
			entities.add(ge);
			
			s = new Sprite3D(1, 2);
			s.setGender(false);
			s.addAnimation("move");
			s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
			s.create(GLOBALS.ASSET_MANAGER);
			
			ge.addRenderable(s);
		}

//		trail = new MotionTrail(100, Color.WHITE, GLOBALS.ASSET_MANAGER.get("data/textures/gradient.png", Texture.class));
//		
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
		
		GLOBALS.TEST_NAV_MESH.setPosition(0, -1, 0);
		GLOBALS.TEST_NAV_MESH.setRotation(GLOBALS.DEFAULT_ROTATION, GLOBALS.DEFAULT_ROTATION);
		GLOBALS.TEST_NAV_MESH.updateMatrixes();
		
		entities.get(0).readData(dataPos, PositionalData.class);
		
		cam.update(dataPos);
	}

}
