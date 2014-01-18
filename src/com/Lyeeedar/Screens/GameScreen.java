package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionRay;
import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Collision.EntityGraphOcttree;
import com.Lyeeedar.Collision.SymbolicMesh;
import com.Lyeeedar.Collision.Triangle;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.AI.AI_RotOnly;
import com.Lyeeedar.Entities.AI.AI_Ship_Control;
import com.Lyeeedar.Entities.AI.AI_Simple;
import com.Lyeeedar.Entities.AI.Action_AISwapper;
import com.Lyeeedar.Entities.AI.Action_Dialogue;
import com.Lyeeedar.Entities.AI.ActivationAction;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Weapon;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Graphics.AnimatedModel;
import com.Lyeeedar.Graphics.Batch;
import com.Lyeeedar.Graphics.Clouds;
import com.Lyeeedar.Graphics.TexturedMesh;
import com.Lyeeedar.Graphics.ModelBatcher;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Sprite2D;
import com.Lyeeedar.Graphics.Sprite3D;
import com.Lyeeedar.Graphics.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.GLOBALS.GENDER;
import com.Lyeeedar.Pirates.ProceduralGeneration.SerkGenerator;
import com.Lyeeedar.Util.Bag;
import com.Lyeeedar.Util.Dialogue;
import com.Lyeeedar.Util.Dialogue.DialogueAction;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractScreen {
	
	private EntityGraphOcttree world;
	private SkyBox skybox;
	private Entity player;
	Terrain terrain;
	private final PositionalData pData = new PositionalData();
	private final StatusData sData = new StatusData();
	
	private final LinkedList<TextParticle> tParticles = new LinkedList<TextParticle>();
	private final LinkedList<ParticleEmitter> visibleEmitters = new LinkedList<ParticleEmitter>();
	private final Bag<Entity> veggies = new Bag<Entity>();
	EntityGraphOcttree ego;
	
	private Camera veggieCam;
	
	private final SpriteBatch sB = new SpriteBatch();
	private final BitmapFont fB = new BitmapFont(true);
		
	SymbolicMesh sm;
	
	Texture blank;

	public GameScreen(PirateGame game) {
		super(game);
	}

	@Override
	public void create() {
		
		veggieCam = new FollowCam(controls);
				
		Texture sand = FileUtils.loadTexture("data/textures/sand.png", true);
		sand.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		sand.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		Texture grass = FileUtils.loadTexture("data/textures/grass.png", true);
		grass.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		grass.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		Texture dirt = FileUtils.loadTexture("data/textures/road.png", true);
		dirt.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		dirt.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		Texture rock = FileUtils.loadTexture("data/textures/rock.png", true);
		rock.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		rock.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		ArrayList<Entity> ae = new ArrayList<Entity>();
		
		SerkGenerator sg = new SerkGenerator(1000, 10000, 1000, -100, 80085);
		Texture hm = new Texture(Gdx.files.internal("data/textures/heightmap.png"));
		hm = ImageUtils.PixmapToTexture(ImageUtils.arrayToPixmap(sg.generate(ae)));
		hm.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		terrain = new Terrain(new Texture[]{sand, grass, dirt, rock}, -100.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 1000.0f, 10000, -100.0f)});
		
		terrain.readData(pData, PositionalData.class);
		pData.calculateComposed();
		terrain.writeData(pData, PositionalData.class);
		
		world = new EntityGraphOcttree(null, new Vector3(-100000, -100, -100000), new Vector3(100000, 10000, 100000));
		world.add(terrain, true);
		
		for (Entity e : ae)
		{
			world.add(e, false);
		}
		
		blank = FileUtils.loadTexture("data/textures/blank.png", true);

		//IslandGenerator ig = new IslandGenerator();
		//Mesh model = ig.getIsland(75, 75, 53);
		
		Texture shipTex = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        Mesh shipModel = FileUtils.loadMesh("data/models/boat.obj");
		
		Entity ship = new Entity(new PositionalData());
		ship.readData(pData, PositionalData.class);
		//pData.position.x = 100;
		//pData.lastPos.set(pData.position);
		//pData.scale.set(20f, 20f, 20f);
		pData.calculateComposed();
		ship.writeData(pData, PositionalData.class);
		ship.setAI(new AI_Simple());
		ship.setActivationAction(new Action_AISwapper("THis is a ship woooot", new AI_Ship_Control(controls), new AI_RotOnly(controls)));
		//ship.setAI(new AI_Player_Control(ship, controls));
				
		SymbolicMesh mesh = SymbolicMesh.getSymbolicMesh(shipModel);
		mesh.setPosition(pData.position);
		ship.setCollisionShapeInternal(mesh);
		
		ship.addRenderable(new TexturedMesh(shipModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1), new Vector3());
		
		world.add(ship, true);

		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//seatex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		Weather weather = new Weather(new Vector3(0.4f, 0.6f, 0.6f), new Vector3(-0.3f, -0.3f, 0), new Vector3(0.05f, 0.03f, 0.08f), new Vector3(-0.05f, 0.03f, 0.08f), new Clouds());
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f), terrain);
		skybox = new SkyBox(sea, weather);
		
		player = new Entity(new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
		player.setAI(new AI_Player_Control(controls));
		//player.setAI(new AI_Simple(player));
		Sprite3D s = new Sprite3D(8, 8, 4, 4);
		s.setGender(GENDER.MALE);
		s.addAnimation("move", "move");
		s.addAnimation("attack_1", "attack", "_1");
		//s.addLayer("Human", Color.WHITE, 0, SpriteLayer.BODY);
		//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
		//s.addLayer("sword", Color.WHITE, 0, SpriteLayer.OTHER);
		//s.create();
		Mesh playerMesh = FileUtils.loadMesh("data/models/human.obj");
		AnimatedModel am = new AnimatedModel(FileUtils.loadModel("data/models/man.g3db"), FileUtils.loadTexture("data/textures/Untitled4.png", true), new Vector3(0.7f, 0.7f, 0.7f), "idle");
		player.addRenderable(am, new Vector3(0, 0, 0));
		//am.attachModel("DEF-forearm_01_L", (AnimatedModel)am.copy());
		//player.addRenderable(new WeaponTrail(Equipment_Slot.RARM, 20, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true), 0.01f));
		player.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 1f, 0.5f));
		//player.setCollisionShapeExternal(new Box(new Vector3(), 0.1f, 0.1f, 0.1f));

		
		player.readData(pData, PositionalData.class);
		pData.position.set(4, 12, 0);
		pData.position.set(2500, 1000, 2500);
		//pData.Xrotate(30);
		pData.scale.set(2, 2, 2);
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
		
		world.add(player, false);
		
		Entity npc = new Entity(new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
		npc.setAI(new AI_Simple());
		Object[] actionTree = {
			DialogueAction.TEXT3D, "Test dialogue 123 This is Action 0. Text 3D. Advancing.", 1 ,
			new Object[]{ DialogueAction.WAIT, 5, 
			new Object[]{ DialogueAction.TEXT3D, "Waited 5 seconds. New dialogue shown.", 1 ,
			new Object[]{ DialogueAction.WAIT, 5,
			new Object[]{ DialogueAction.TEXT2D, "Now then, what do you want to do? 1: Stuff, 2: other stuff. Choose.", 
			new Object[]{ DialogueAction.INPUT, 
					Keys.NUM_1, 
						new Object[]{ DialogueAction.TEXT3D, "You chose 1.", 1 ,
						new Object[]{ DialogueAction.INPUT, Keys.E, null
						}
						},
					Keys.NUM_2,
						new Object[]{ DialogueAction.TEXT3D, "You chose 2.", 1 ,
						new Object[]{ DialogueAction.INPUT, Keys.E, null
						}
						},
			}
			}
			}
			}
			}
		};
		npc.setActivationAction(new Action_Dialogue("BOOOBIES", new Dialogue(new Entity[]{player, npc}, actionTree)));
		s = new Sprite3D(2, 2, 4, 4);
		s.setGender(GENDER.FEMALE);
		s.addAnimation("move", "move");
		npc.addRenderable(s, new Vector3(0, -2, 0));
		//npc.addRenderable(new Sprite2D(Decal.newDecal(new TextureRegion(ImageUtils.drawText(sB, fB, "I am an NPC with Boobies loool look at them theyre all big and stuff :p")))));
		npc.setCollisionShapeInternal(new Box(new Vector3(), 0.5f, 2.0f, 0.5f));
		npc.readData(pData, PositionalData.class);
		pData.position.set(-4, 12, 0);
		//pData.position.set(4500, 290, 3500);
		npc.writeData(pData, PositionalData.class);
		npc.readData(sData, StatusData.class);
		sData.factions.add("Player");
		npc.writeData(sData, StatusData.class);
		npc.readData(eData, EquipmentData.class);
		eData.equip(Equipment_Slot.BODY, new Armour(null, new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(null, new SPRITESHEET("Hair1", new Color(0.9f, 0.5f, 0.7f, 1.0f), 0, SpriteLayer.HEAD), null));
		npc.writeData(eData, EquipmentData.class);
		//world.add(npc, false);
		
		Random ran = new Random();
		for (int i = 0; i < 0; i++)
		{
			Entity ge = new Entity(new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
			AI_Follow ai = new AI_Follow();
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
			
			world.add(ge, false);
			
			s = new Sprite3D(3, 3, 4, 4);
			s.setGender(GENDER.MALE);
			s.addAnimation("move", "move");
			s.addAnimation("attack_1", "attack");
			//s.addLayer("devil", Color.WHITE, 0, SpriteLayer.BODY);
			//s.addLayer("BasicClothes", Color.WHITE, 0, SpriteLayer.TOP);
			//s.create();
			
			ge.readData(sData, StatusData.class);
			sData.factions.add("Enemy");
			ge.writeData(sData, StatusData.class);
			
			ge.addRenderable(s, new Vector3());
			
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

		Entity c = new Entity(new PositionalData());
		c.readData(pData, PositionalData.class);
		pData.position.x = 4500;
		pData.position.y = 280;
		pData.position.z = 3500;
		//pData.Xrotate(180);
		//pData.lastPos.set(pData.position);
		//pData.scale.set(20f, 20f, 20f);
		pData.applyVelocity(0);
		pData.applyVelocity(0);
		c.writeData(pData, PositionalData.class);

		SymbolicMesh cmesh = SymbolicMesh.getSymbolicMesh(cModel);
		sm = cmesh;
		cmesh.setPosition(pData.position);
		c.setCollisionShapeInternal(cmesh);

		c.addRenderable(new TexturedMesh(cModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1), new Vector3());

		world.add(c, true);
		
		ego = new EntityGraphOcttree(null, new Vector3(0, -1000, 0), new Vector3(10000, 1000, 10000));
		
		Mesh grassMesh = FileUtils.loadMesh("data/models/pinet.obj");
		Texture pinetex = FileUtils.loadTexture("data/textures/pinet.png", true);
		pinetex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, pinetex, new Vector3(1, 1, 1), false), 1, 2500, 50);
		//terrain.vegetate(veggies, new Model(grassMesh, GL20.GL_TRIANGLES, FileUtils.loadTexture("data/textures/pinet.png", true), null, 1), 1, 5000, 50);
		BoundingBox bb = grassMesh.calculateBoundingBox();
		for (Entity v : veggies)
		{
			v.setCollisionShapeInternal(new Box(bb.min, bb.max));
			v.update(0);
			world.add(v, false);
		}
		
		grassMesh = FileUtils.loadMesh("data/models/shr2.obj");
		Texture shrtex = FileUtils.loadTexture("data/textures/shr2.png", true);
		shrtex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, shrtex, new Vector3(1, 1, 1), true), 1, 50000, 50);
		bb = grassMesh.calculateBoundingBox();
		for (Entity v : veggies)
		{
			v.setCollisionShapeInternal(new Box(bb.min, bb.max));
			v.update(0);
			ego.add(v, false);
		}
		
//		EntityGraph teg = new EntityGraph(null, world, false);
//		for (int i = 0; i < 1000; i++)
//		{
//			if (i % 10 == 0)
//			{
//				teg = new EntityGraph(null, world, false);
//			}
//			Mesh treeMesh = FileUtils.loadMesh("data/models/crappygrass.obj");
//			Entity tree = new Entity();
//			
//			tree.readData(pData, PositionalData.class);
//			pData.position.x = ran.nextInt(1000);
//			pData.position.y = 10;
//			pData.position.z = ran.nextInt(1000);
//			//pData.lastPos.set(pData.position);
//			pData.scale.set(10f, 10f, 10f);
//			pData.calculateComposed();
//			tree.writeData(pData, PositionalData.class);
//			
//			tree.addRenderable(new Model(treeMesh, GL20.GL_TRIANGLES, sand, new Vector3(0.4f, 1, 0.5f), 1));
//			
//			teg.addEntity(tree, true);
//		}
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
		player.readData(sData, StatusData.class);
		player.readData(pData, PositionalData.class);
		batch.draw(blank, screen_width-80, screen_height-40, ((float)sData.currentHealth/(float)sData.MAX_HEALTH)*50, 10);
		font.draw(spriteBatch, ""+pData.position, 20, screen_height-80);
		font.draw(spriteBatch, ""+pData.rotation, 20, screen_height-120);
		
		player.readData(pData, PositionalData.class);
		box.center.set(pData.rotation).scl(2).add(pData.position);
		Entity e = activate(box, pData.graph, elist, pData.position, pData);
		if (e != null)
		{
			font.draw(spriteBatch, ""+e.getActivationAction().getDesc(), 220, 220);
		}
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.queue2D(batch);
		}
	}
	
	private final ArrayList<EntityGraph> elist = new ArrayList<EntityGraph>();
	private Box box = new Box(new Vector3(), 0.5f, 0.5f, 0.5f);
	protected Entity activate(CollisionShape<?> shape, EntityGraph graph, List<EntityGraph> list, Vector3 pos, PositionalData pData)
	{
		boolean found = GLOBALS.WORLD.collide(shape, graph, list);
		if (!found) return null;
		
		float min = Float.MAX_VALUE;
		Entity chosen = null;
		
		for (EntityGraph eg : list)
		{
			if (eg.entity != null && eg.entity.hasActivationAction())
			{
				eg.entity.readData(pData, PositionalData.class);
				float dist = pos.dst2(pData.position);
				if (dist < min) 
				{
					min = dist;
					chosen = eg.entity;
				}
			}
		}
		list.clear();
		
		return chosen;
	}

	ImmediateModeRenderer imr = new ImmediateModeRenderer20(false, false, 0);
	@Override
	public void drawSkybox(float delta)
	{
//		player.readData(pData, PositionalData.class);
//		Triangle[] tris = terrain.getTris();
//		if (tris != null) {
//			imr.begin(cam.combined, GL20.GL_TRIANGLES);
//			for (int i = 0; i < tris.length; i++)
//			{
//				Triangle tri = tris[i];
//				//imr.vertex(pData.position.x, pData.position.y, pData.position.z);
//				imr.vertex(tri.v1.x, tri.v1.y, tri.v1.z);
//				imr.vertex(tri.v2.x, tri.v2.y, tri.v2.z);
//				imr.vertex(tri.v3.x, tri.v3.y, tri.v3.z);
//			}
//			
//			imr.end();
//		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		terrain.render(cam, cam.position, GLOBALS.LIGHTS);
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
	public void queueRenderables(float delta, HashMap<Class, Batch> batches) {
		
		GLOBALS.WORLD.queueRenderables(cam, GLOBALS.LIGHTS, delta, batches);
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
		
		ego.queueRenderables(veggieCam, GLOBALS.LIGHTS, delta, batches);
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.queue3D(decalBatch);
		}
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}
	@Override
	public void show() {
		Gdx.input.setCursorCatched(true);
		Gdx.input.setInputProcessor(controls.ip);
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
						
		((FollowCam)cam).update(player);
		veggieCam.position.set(cam.position);
		veggieCam.direction.set(cam.direction);
		veggieCam.update();
		
		GLOBALS.WORLD.collectDead(deadList);
		for (EntityGraph eg : deadList) eg.remove();
		deadList.clear();
		
		Iterator<Spell> itr = GLOBALS.SPELLS.iterator();
		while(itr.hasNext())
		{
			Spell s = itr.next();
			boolean alive = s.update(delta, cam);
			if (!alive || s.position.dst2(cam.position) > GLOBALS.FOG_MAX*GLOBALS.FOG_MAX)
			{
				itr.remove();
				s.dispose();
			}
		}
		
		GLOBALS.SPELLS.addAll(GLOBALS.pendingSPELLS);
		GLOBALS.pendingSPELLS.clear();
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.update(delta, cam);
		}
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
		veggieCam.viewportWidth = width;
		veggieCam.viewportHeight = height;
		veggieCam.near = 2f;
		veggieCam.far = 1502f;
	}

}
