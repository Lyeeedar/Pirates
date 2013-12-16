package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Collision.Triangle;
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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
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
		
	SymbolicMesh sm;
	
	Texture blank;

	public GameScreen(PirateGame game) {
		super(game);
	}

	@Override
	public void create() {
				
		Texture sand = FileUtils.loadTexture("data/textures/grass.png", true);
		sand.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		Texture hm = new Texture(Gdx.files.internal("data/textures/heightmap.png"));
		Terrain terrain = new Terrain(sand, -100.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 1000.0f, 10000, -100.0f)});
		
		terrain.readData(pData, PositionalData.class);
		pData.calculateComposed();
		terrain.writeData(pData, PositionalData.class);
		
		world = new EntityGraph(terrain, null, true);
		
		blank = FileUtils.loadTexture("data/textures/blank.png", true);

		//IslandGenerator ig = new IslandGenerator();
		//Mesh model = ig.getIsland(75, 75, 53);
		
		Texture shipTex = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        Mesh shipModel = FileUtils.loadMesh("data/models/boat.obj");
		
		Entity ship = new Entity();
		ship.readData(pData, PositionalData.class);
		//pData.position.x = 100;
		//pData.lastPos.set(pData.position);
		//pData.scale.set(20f, 20f, 20f);
		pData.calculateComposed();
		ship.writeData(pData, PositionalData.class);
		ship.setAI(new AI_Simple(ship));
		//ship.setAI(new AI_Player_Control(ship, controls));
				
		SymbolicMesh mesh = SymbolicMesh.getSymbolicMesh(shipModel);
		mesh.setPosition(pData.position);
		ship.setCollisionShapeInternal(mesh);
		
		ship.addRenderable(new Model(shipModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1));
		
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
		//player.setAI(new AI_Simple(player));
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
		ray.len = 0.5f;
		player.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
		//player.setCollisionShapeExternal(new Box(new Vector3(), 0.1f, 0.1f, 0.1f));

		
		player.readData(pData, PositionalData.class);
		pData.position.set(4, 12, 0);
		player.writeData(pData, PositionalData.class);
		
		player.readData(sData, StatusData.class);
		sData.factions.add("Player");
		player.writeData(sData, StatusData.class);
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.equip(Equipment_Slot.BODY, new Armour(null, new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(null, new SPRITESHEET("Hair1", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.HEAD), null));
		eData.equip(Equipment_Slot.LEGS, new Armour(null, new SPRITESHEET("BasicClothes", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.TOP), null));
		eData.equip(Equipment_Slot.RARM, new Weapon("attack_1", new SPRITESHEET("sword", Color.WHITE, 0, SpriteLayer.OTHER), new DESCRIPTION(null, null, null, null), 1, new Vector3(0.3f, 0.6f, 0.3f), 0.5f, 50, 50));
		player.writeData(eData, EquipmentData.class);
		
		world.addEntity(player, false);
		
		Random ran = new Random();
		for (int i = 0; i < 0; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			ge.readData(pData, PositionalData.class);
			pData.position.set(4450+ran.nextFloat()*24, 260, 3300+ran.nextFloat()*24);
			ge.writeData(pData, PositionalData.class);
			ge.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
			
			ge.readData(eData, EquipmentData.class);
			eData.equip(Equipment_Slot.BODY, new Armour(null, new SPRITESHEET("devil", Color.WHITE, 0, SpriteLayer.BODY), null));
			eData.equip(Equipment_Slot.RARM, new Weapon("attack_1", null, null, 1, new Vector3(0.3f, 0.6f, 0.3f), 0.8f, 3, 5));
			ge.writeData(eData, EquipmentData.class);
			
			world.addEntity(ge, false);
			
			s = new Sprite3D(3, 3, 4, 4);
			s.setGender(true);
			s.addAnimation("move", "move");
			s.addAnimation("attack_1", "attack");
			//s.addLayer("devil", Color.WHITE, 0, SpriteLayer.BODY);
			//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
			//s.create();
			
			ge.readData(sData, StatusData.class);
			sData.factions.add("Enemy");
			ge.writeData(sData, StatusData.class);
			
			ge.addRenderable(s);
			
			ge.readData(pData, PositionalData.class);
			ge.getCollisionShapeInternal().setPosition(pData.position);
			while (world.collide(ge.getCollisionShapeInternal(), pData.graph) != null)
			{
				ge.readData(pData, PositionalData.class);
				pData.position.set(4450+ran.nextFloat()*24, 260, 3300+ran.nextFloat()*24);
				ge.writeData(pData, PositionalData.class);
				ge.getCollisionShapeInternal().setPosition(pData.position);
			}
		}
		
		Mesh cModel = FileUtils.loadMesh("data/models/Bastion.obj");

		Entity c = new Entity();
		c.readData(pData, PositionalData.class);
		pData.position.x = 4450;
		pData.position.y = 245;
		pData.position.z = 3530;
		pData.Xrotate(180);
		//pData.lastPos.set(pData.position);
		//pData.scale.set(20f, 20f, 20f);
		pData.applyVelocity(0);
		pData.applyVelocity(0);
		c.writeData(pData, PositionalData.class);

		SymbolicMesh cmesh = SymbolicMesh.getSymbolicMesh(cModel);
		sm = cmesh;
		cmesh.setPosition(pData.position);
		c.setCollisionShapeInternal(cmesh);

		c.addRenderable(new Model(cModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1));

		world.addEntity(c, true);
		
		EntityGraph teg = new EntityGraph(null, world, false);
		for (int i = 0; i < 0; i++)
		{
			if (i % 10 == 0)
			{
				teg = new EntityGraph(null, world, false);
			}
			Mesh treeMesh = FileUtils.loadMesh("data/models/tree.obj");
			Entity tree = new Entity();
			
			tree.readData(pData, PositionalData.class);
			pData.position.x = ran.nextInt(1000);
			pData.position.y = 10;
			pData.position.z = ran.nextInt(1000);
			//pData.lastPos.set(pData.position);
			pData.scale.set(20f, 20f, 20f);
			pData.calculateComposed();
			tree.writeData(pData, PositionalData.class);
			
			tree.addRenderable(new Model(treeMesh, GL20.GL_TRIANGLES, sand, new Vector3(0.4f, 1, 0.5f), 1));
			
			teg.addEntity(tree, true);
		}
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
		player.readData(sData, StatusData.class);
		player.readData(pData, PositionalData.class);
		batch.draw(blank, screen_width-80, screen_height-40, ((float)sData.currentHealth/(float)sData.MAX_HEALTH)*50, 10);
		font.draw(spriteBatch, ""+pData.position, 20, screen_height-80);
		font.draw(spriteBatch, ""+pData.rotation, 20, screen_height-120);
	}

	@Override
	public void drawSkybox(float delta)
	{
//		int i = 0;
//		while (i < sm.tris.length)
//		{
//			imr.begin(cam.combined, GL20.GL_TRIANGLES);
//			for (int count = 0; count < 4999/9 && i < sm.tris.length; count++, i++)
//			{
//				Triangle tri = sm.tris[i];
//				imr.vertex(tri.v1.x+sm.getPosition().x, tri.v1.y+sm.getPosition().y, tri.v1.z+sm.getPosition().z);
//				imr.vertex(tri.v2.x+sm.getPosition().x, tri.v2.y+sm.getPosition().y, tri.v2.z+sm.getPosition().z);
//				imr.vertex(tri.v3.x+sm.getPosition().x, tri.v3.y+sm.getPosition().y, tri.v3.z+sm.getPosition().z);
//			}
//			imr.end();
//		}
		
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
		
		//GLOBALS.WORLD.recalculateBounds();
		
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
			if (!alive || s.position.dst2(cam.position) > GLOBALS.FOG_MAX*GLOBALS.FOG_MAX)
			{
				itr.remove();
				s.dispose();
			}
		}
		
		GLOBALS.SPELLS.addAll(GLOBALS.pendingSPELLS);
		GLOBALS.pendingSPELLS.clear();
		
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
			s.effect.update(delta, cam);
			s.effect.getVisibleEmitters(visibleEmitters, cam);
		}
		Collections.sort(visibleEmitters, ParticleEmitter.getComparator());
		ParticleEmitter.begin(cam);
		for (ParticleEmitter p : visibleEmitters)
		{
			particleNum += p.getActiveParticles();
			p.render();
		}
		ParticleEmitter.end();
		visibleEmitters.clear();
	}

	@Override
	public void resized(int width, int height) {
	}

}
