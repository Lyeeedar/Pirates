package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Sea;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.AI.AI_Simple;
import com.Lyeeedar.Entities.Items.Blade;
import com.Lyeeedar.Graphics.Model;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Sprite3D;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Graphics.WeaponTrail;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.ProceduralGeneration.IslandGenerator;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.math.Vector3;

public class GameScreen extends AbstractScreen {
	
	SkyBox skyBox;
	PositionalData dataPos = new PositionalData();
	PositionalData pData = new PositionalData();
	
	Terrain terrain;

	int numEntities = 15;
	
	long time = System.currentTimeMillis();
	
	Entity player;

	public GameScreen(LightManager lights) {
		super(lights);
		this.lights = lights;
	}

	@Override
	public void create() {
		
		long time = System.nanoTime();
		
		Texture texture = FileUtils.loadTexture("data/textures/grass.png", true);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

		IslandGenerator ig = new IslandGenerator();
		Mesh model = ig.getIsland(75, 75, 53);
		
		ObjLoader loader = new ObjLoader();
		texture = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        model = loader.loadObj(Gdx.files.internal("data/models/shipMesh.obj"), true).subMeshes[0].mesh;

		SymbolicMesh mesh = SymbolicMesh.getSymbolicMesh(model);
		mesh.setPosition(new Vector3(-20, 0, 0));
		
		Entity island = new Entity();
		island.readData(pData, PositionalData.class);
		pData.position.x = 10;
		//pData.scale.set(2.2f, 2.2f, 2.2f);
		pData.calculateComposed();
		island.writeData(pData, PositionalData.class);
		island.setAI(new AI_Simple(island));
		
		island.addRenderable(new Model(model, GL20.GL_TRIANGLES, texture, new Vector3(1, 1, 1), 1));
		island.setCollisionShapeInternal(mesh);
		
		texture = FileUtils.loadTexture("data/textures/sand.png", true);
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		Texture hm = new Texture(Gdx.files.internal("data/textures/heightmap.png"));
		terrain = new Terrain(texture, -100.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 500.0f, 5000, -100.0f)});
		GLOBALS.WORLD.addEntity(island, true);
		GLOBALS.WORLD.setEntity(terrain, true);

		Texture skytex = new Texture(Gdx.files.internal("data/textures/sky.png"));
		Texture glowtex = new Texture(Gdx.files.internal("data/textures/glow.png"));
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		skyBox = new SkyBox(skytex, glowtex);
		GLOBALS.sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f));
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		Sprite3D s = new Sprite3D(2, 2, 4, 4);
		s.setGender(true);
		s.addAnimation("move", "move");
		s.addAnimation("attack_1", "attack", "_1");
		s.addLayer("Human", Color.WHITE, 0, SpriteLayer.BODY);
		//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
		s.addLayer("sword", Color.WHITE, 0, SpriteLayer.OTHER);
		s.create();
		player.addRenderable(s);
		//player.addRenderable(new WeaponTrail(Equipment_Slot.RARM, 20, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true), 0.01f));
		CollisionRay ray = new CollisionRay();
		ray.len = 1;
		player.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
		
		player.readData(pData, PositionalData.class);
		pData.position.set(4, 5, 0);
		player.writeData(pData, PositionalData.class);
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.addEquipment(Equipment_Slot.RARM, new Blade(1));
		player.writeData(eData, EquipmentData.class);
		
		GLOBALS.WORLD.addEntity(player, false);
		
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			//ge.setCollisionShape(new Sphere(new Vector3(0, 0, 0), 0.1f));
			ge.readData(pData, PositionalData.class);
			pData.position.set(4, 15, 0);
			ge.writeData(pData, PositionalData.class);
			ge.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
			
			GLOBALS.WORLD.addEntity(ge, false);
			
			s = new Sprite3D(2, 2, 4, 4);
			s.setGender(false);
			s.addAnimation("move", "move");
			//s.addAnimation("attack_1");
			s.addLayer("Human", Color.WHITE, 0, SpriteLayer.BODY);
			//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
			s.create();
			
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
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		skyBox.render(cam, lights);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		player.readData(pData, PositionalData.class);
		terrain.render(cam, pData.position, lights);
		GLOBALS.sea.render(cam, pData.position, lights);
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
	ArrayList<EntityGraph> deadList = new ArrayList<EntityGraph>();
	boolean increase = true;
	
	float strike_time = 0.0f;
	Random ran = new Random();
	@Override
	public void update(float delta) {
		
		GLOBALS.WORLD.getRunnable(list, delta);
		GLOBALS.submitTasks(list);
		list.clear();
		GLOBALS.waitTillTasksComplete();
		
		skyBox.update(delta);
		
		player.readData(dataPos, PositionalData.class);
				
		cam.update(dataPos);
		this.visibleEmitters.get(0).setPosition(dataPos.position);
		
		GLOBALS.WORLD.collectDead(deadList);
		for (EntityGraph eg : deadList) eg.remove();
		deadList.clear();
		
		//lights.lights.get(0).position.set(pData.position).add(0, 1, 0);
		
		//delta /= 10;
		
//		if (increase) 
//		{
//			lights.directionalLight.direction.y += delta;
//			
//			if (lights.directionalLight.direction.y < 0.0f) 
//			{
//				lights.directionalLight.direction.z += delta;
//			}
//			else
//			{
//				lights.directionalLight.direction.z -= delta;
//			}
//		}
//		else 
//		{
//			lights.directionalLight.direction.y -= delta;
//			
//			if (lights.directionalLight.direction.y < 0.0f) 
//			{
//				lights.directionalLight.direction.z += delta;
//			}
//			else
//			{
//				lights.directionalLight.direction.z -= delta;
//			}
//		}
//		
//		if (lights.directionalLight.direction.y >= 1.0f) increase = false;
//		if (lights.directionalLight.direction.y < -1) increase = true;
//		
//		lights.ambientColour.x = (lights.directionalLight.direction.y+1)/2;
//		lights.ambientColour.y = (lights.directionalLight.direction.y+1)/2;
//		lights.ambientColour.z = (lights.directionalLight.direction.y+1)/2;
//		
//		strike_time -= delta;
//		if (strike_time < 0.0f)
//		{
//			lights.ambientColour.set(0.05f, 0.07f, 0.12f);
//		}
//		else
//		{
//			lights.ambientColour.set(0.1f, 0.1f, 0.7f);
//		}
//		
//		if (ran.nextInt(500) == 1)
//		{
//			strike_time = 0.1f;
//		}
	}

}
