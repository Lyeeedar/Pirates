package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.AI.AI_Simple;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Weapon;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Graphics.Model;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Sprite3D;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.math.Vector3;

public class GameScreen extends AbstractScreen {
	
	private EntityGraph world;
	private SkyBox skybox;
	private Entity player;
	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	
	private final LinkedList<TextParticle> tParticles = new LinkedList<TextParticle>();
	private final LinkedList<ParticleEmitter> visibleEmitters = new LinkedList<ParticleEmitter>();
	
	private final SpriteBatch sB = new SpriteBatch();
	private final BitmapFont fB = new BitmapFont(true);
	
	Texture blank;

	public GameScreen(PirateGame game) {
		super(game);
	}

	@Override
	public void create() {
		
		blank = FileUtils.loadTexture("data/textures/grass.png", true);

		//IslandGenerator ig = new IslandGenerator();
		//Mesh model = ig.getIsland(75, 75, 53);
		
		ObjLoader loader = new ObjLoader();
		Texture shipTex = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        Mesh shipModel = loader.loadObj(Gdx.files.internal("data/models/shipMesh.obj"), true).subMeshes[0].mesh;
		SymbolicMesh mesh = SymbolicMesh.getSymbolicMesh(shipModel);
		
		Entity ship = new Entity();
		ship.readData(pData, PositionalData.class);
		//pData.position.x = 10;
		//pData.lastPos.set(pData.position);
		//pData.scale.set(2.2f, 2.2f, 2.2f);
		pData.calculateComposed();
		ship.writeData(pData, PositionalData.class);
		ship.setAI(new AI_Simple(ship));
		mesh.setPosition(pData.position);
		
		ship.addRenderable(new Model(shipModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1));
		ship.setCollisionShapeInternal(mesh);
		
		Texture sand = FileUtils.loadTexture("data/textures/sand.png", true);
		sand.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		Texture hm = new Texture(Gdx.files.internal("data/textures/heightmap.png"));
		Terrain terrain = new Terrain(sand, -100.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 500.0f, 5000, -100.0f)});
		world = new EntityGraph(terrain, null, true);
		world.addEntity(ship, true);

		Texture skytex = new Texture(Gdx.files.internal("data/textures/sky.png"));
		Texture glowtex = new Texture(Gdx.files.internal("data/textures/glow.png"));
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		Weather weather = new Weather(skytex, glowtex);
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f));
		skybox = new SkyBox(sea, weather);
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		Sprite3D s = new Sprite3D(2, 2, 4, 4);
		s.setGender(true);
		s.addAnimation("move", "move");
		s.addAnimation("attack_1", "attack", "_1");
		//s.addLayer("Human", Color.WHITE, 0, SpriteLayer.BODY);
		//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
		//s.addLayer("sword", Color.WHITE, 0, SpriteLayer.OTHER);
		//s.create();
		player.addRenderable(s);
		//player.addRenderable(new WeaponTrail(Equipment_Slot.RARM, 20, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true), 0.01f));
		CollisionRay ray = new CollisionRay();
		ray.len = 1;
		player.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
		//player.setCollisionShapeExternal(new Box(new Vector3(), 0.1f, 0.1f, 0.1f));

		
		player.readData(pData, PositionalData.class);
		pData.position.set(4, 5, 0);
		player.writeData(pData, PositionalData.class);
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.equip(Equipment_Slot.BODY, new Armour(null, new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(null, new SPRITESHEET("Hair1", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.HEAD), null));
		eData.equip(Equipment_Slot.LEGS, new Armour(null, new SPRITESHEET("BasicClothes", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.TOP), null));
		eData.equip(Equipment_Slot.RARM, new Weapon("attack_1", new SPRITESHEET("sword", Color.WHITE, 0, SpriteLayer.OTHER), new DESCRIPTION(null, null, null, null), 1, new Vector3(0.3f, 0.6f, 0.3f), 0.5f, 50, 50));
		player.writeData(eData, EquipmentData.class);
		
		world.addEntity(player, false);
		
		Random ran = new Random();
		for (int i = 0; i < 15; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			ge.readData(pData, PositionalData.class);
			pData.position.set(ran.nextFloat()*24, 15, ran.nextFloat()*24);
			ge.writeData(pData, PositionalData.class);
			ge.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
			
			ge.readData(eData, EquipmentData.class);
			eData.equip(Equipment_Slot.BODY, new Armour(null, new SPRITESHEET("devil", Color.WHITE, 0, SpriteLayer.BODY), null));
			eData.equip(Equipment_Slot.RARM, new Weapon("attack_1", null, null, 1, new Vector3(0.3f, 0.6f, 0.3f), 0.8f, 3, 5));
			ge.writeData(eData, EquipmentData.class);
			
			world.addEntity(ge, false);
			
			s = new Sprite3D(1, 1, 4, 4);
			s.setGender(true);
			s.addAnimation("move", "move");
			s.addAnimation("attack_1", "attack");
			//s.addLayer("devil", Color.WHITE, 0, SpriteLayer.BODY);
			//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
			//s.create();
			
			ge.addRenderable(s);
			
			ge.readData(pData, PositionalData.class);
			ge.getCollisionShapeInternal().setPosition(pData.position);
			while (GLOBALS.WORLD.collide(ge.getCollisionShapeInternal(), pData.graph) != null)
			{
				ge.readData(pData, PositionalData.class);
				pData.position.set(ran.nextFloat()*14, 15, ran.nextFloat()*14);
				ge.writeData(pData, PositionalData.class);
				ge.getCollisionShapeInternal().setPosition(pData.position);
			}
		}
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
		player.readData(sData, StatusData.class);
		batch.draw(blank, screen_width-80, screen_height-40, ((float)sData.currentHealth/(float)sData.MAX_HEALTH)*50, 10);
	}

	@Override
	public void drawSkybox(float delta)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		//player.readData(pData, PositionalData.class);
		((Terrain) GLOBALS.WORLD.getEntity()).render(cam, cam.position, GLOBALS.LIGHTS);
		GLOBALS.SKYBOX.sea.render(cam, cam.position, GLOBALS.LIGHTS);
	}
	@Override
	public void hide() {
		Gdx.input.setCursorCatched(false);

	}
	
	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void queueRenderables(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		
		GLOBALS.WORLD.queueRenderables(cam, GLOBALS.LIGHTS, delta, modelBatch, decalBatch, trailBatch);
		GLOBALS.WORLD.getText(tParticles, sB, fB);
		Iterator<TextParticle> itr = tParticles.iterator();
		while (itr.hasNext())
		{
			TextParticle tp = itr.next();
			if (tp.lifeTime < 0)
			{
				itr.remove();
				tp.dispose();
			}
			else
			{
				tp.update(delta, cam);
				tp.render(decalBatch);
			}
		}
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
	@Override
	public void show() {
		Gdx.input.setCursorCatched(true);
		GLOBALS.WORLD = world;
		GLOBALS.SKYBOX = skybox;
		GLOBALS.player = player;
	}

	@Override
	public void superDispose() {
		sB.dispose();
		fB.dispose();
		pData.dispose();
		sData.dispose();
		
		
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
		
		GLOBALS.SKYBOX.update(delta);
		
		player.readData(pData, PositionalData.class);
				
		((FollowCam)cam).update(pData);
		
		GLOBALS.WORLD.collectDead(deadList);
		for (EntityGraph eg : deadList) eg.remove();
		deadList.clear();
		
		Iterator<Spell> itr = GLOBALS.SPELLS.iterator();
		while(itr.hasNext())
		{
			Spell s = itr.next();
			boolean alive = s.update(delta);
			if (!alive) 
			{
				itr.remove();
				s.dispose();
			}
		}
		
		//lights.lights.get(0).position.set(pData.position).add(0, 1, 0);
		
		delta /= 300;
		
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

	@Override
	public void drawParticles(float delta) {
		particleNum = 0;
		for (Spell s : GLOBALS.SPELLS)
		{
			s.effect.getVisibleEmitters(visibleEmitters, cam);
		}
		Collections.sort(visibleEmitters, ParticleEmitter.getComparator());
		ParticleEmitter.begin(cam);
		for (ParticleEmitter p : visibleEmitters)
		{
			p.update(delta, cam);
			particleNum += p.getActiveParticles();
			p.render();
		}
		ParticleEmitter.end();
		visibleEmitters.clear();
	}

}
