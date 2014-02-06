package com.Lyeeedar.Screens;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeBox;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.MinimalPositionalData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Terrain;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Entities.AI.AI_RotOnly;
import com.Lyeeedar.Entities.AI.AI_Ship_Control;
import com.Lyeeedar.Entities.AI.AI_Simple;
import com.Lyeeedar.Entities.AI.ActionAttack;
import com.Lyeeedar.Entities.AI.ActionEvaluateDamage;
import com.Lyeeedar.Entities.AI.ActionFindClosestVisible;
import com.Lyeeedar.Entities.AI.ActionGravityAndMovement;
import com.Lyeeedar.Entities.AI.ActionMoveToClosest;
import com.Lyeeedar.Entities.AI.ActionPlayerControl;
import com.Lyeeedar.Entities.AI.ActionRandomWalk;
import com.Lyeeedar.Entities.AI.ActionUpdateAnimations;
import com.Lyeeedar.Entities.AI.ActionWait;
import com.Lyeeedar.Entities.AI.Action_AISwapper;
import com.Lyeeedar.Entities.AI.Action_Dialogue;
import com.Lyeeedar.Entities.AI.BehaviourTree;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.BehaviourTree.Conditional;
import com.Lyeeedar.Entities.AI.ConditionalAnimationLock;
import com.Lyeeedar.Entities.AI.Selector;
import com.Lyeeedar.Entities.AI.Selector.ConcurrentSelector;
import com.Lyeeedar.Entities.AI.Selector.PrioritySelector;
import com.Lyeeedar.Entities.AI.Selector.RandomSelector;
import com.Lyeeedar.Entities.AI.Selector.SequenceSelector;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Weapon;
import com.Lyeeedar.Entities.Items.Weapon.ATTACK_STAGE;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Graphics.Clouds;
import com.Lyeeedar.Graphics.Sea;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Weather;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Graphics.Particles.ParticleEmitter;
import com.Lyeeedar.Graphics.Particles.TextParticle;
import com.Lyeeedar.Graphics.Queueables.AnimatedModel;
import com.Lyeeedar.Graphics.Queueables.MotionTrail;
import com.Lyeeedar.Graphics.Queueables.Sprite3D;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SPRITESHEET;
import com.Lyeeedar.Graphics.Queueables.Sprite3D.SpriteLayer;
import com.Lyeeedar.Graphics.Queueables.TexturedMesh;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.GLOBALS.GENDER;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.ProceduralGeneration.SerkGenerator;
import com.Lyeeedar.Util.Dialogue;
import com.Lyeeedar.Util.Dialogue.DialogueAction;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btHeightfieldTerrainShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;

public class GameScreen extends AbstractScreen {
	
	private SkyBox skybox;
	private Entity player;
	Terrain terrain;
	
	private final LinkedList<TextParticle> tParticles = new LinkedList<TextParticle>();
	private final LinkedList<ParticleEmitter> visibleEmitters = new LinkedList<ParticleEmitter>();
	private final Array<Entity> veggies = new Array<Entity>();
	
	private FollowCam veggieCam;
	
	private final SpriteBatch sB = new SpriteBatch();
	private final BitmapFont fB = new BitmapFont(true);
	
	private Octtree<Entity> veggieTree;

	public GameScreen(PirateGame game) {
		super(game);
	}

	FloatBuffer fb;
	@Override
	public void create() {
		
		BulletWorld bw = new BulletWorld(new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		GLOBALS.physicsWorld = bw;
		
		veggieTree = new Octtree<Entity>(null, new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		
		Octtree<Entity> rw = new Octtree<Entity>(null, new Vector3(-100000, -100000, -100000), new Vector3(100000, 100000, 100000));
		GLOBALS.renderTree = rw;
		
		//bw.add(cam.renderObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_RENDER);
		//bw.add(cam.aiObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_AI);
		
		veggieCam = new FollowCam(controls, null);
		//rw.add(veggieCam.renderObject, BulletWorld.FILTER_GHOST, BulletWorld.FILTER_RENDER);
				
		// HEIGHT MAP
		Texture sand = FileUtils.loadTexture("data/textures/sand.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture grass = FileUtils.loadTexture("data/textures/grass.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture dirt = FileUtils.loadTexture("data/textures/road.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);	
		Texture rock = FileUtils.loadTexture("data/textures/rock.png", true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);
		
		ArrayList<Entity> ae = new ArrayList<Entity>();
		
		SerkGenerator sg = new SerkGenerator(1000, 10000, 1000, -100, 80085);
		Pixmap hmpm = ImageUtils.arrayToPixmap(sg.generate(ae));
		Texture hm = ImageUtils.PixmapToTexture(hmpm);
		hm.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		terrain = new Terrain(new Texture[]{sand, grass, dirt, rock}, -100.0f, new Terrain.HeightMap[]{new Terrain.HeightMap(hm, new Vector3(0f, 0f, 0f), 1000.0f, 10000, -100.0f)});
		
		terrain.readOnlyRead(PositionalData.class).calculateComposed();
		
		fb = ImageUtils.extractAlpha(hmpm);
		btHeightfieldTerrainShape hf = new btHeightfieldTerrainShape(1000, 1000, fb, 1.f, 0.f, 1.f, 1, false);
		hf.setLocalScaling(new Vector3(10f, 1000f, 10f));
		
		bw.add(hf, new Matrix4().setToTranslation(5000f, 400f, 5000f), terrain, BulletWorld.FILTER_COLLISION, BulletWorld.FILTER_COLLISION);
		
		// END HEIGHTMAP

//		for (Entity e : ae)
//		{
//			bw.add(e);
//		}
		
		// MAKE SHIP
		
		Texture shipTex = new Texture(Gdx.files.internal("data/textures/shipTex.png"));
        Mesh shipModel = FileUtils.loadMesh("data/models/boat.obj");
		
		Entity ship = new Entity(new PositionalData());
		ship.readOnlyRead( PositionalData.class).calculateComposed();
		ship.setAI(new AI_Simple());
		ship.setActivationAction(new Action_AISwapper("THis is a ship woooot", new AI_Ship_Control(controls), new AI_RotOnly(controls)));
		
		ship.addRenderable(new TexturedMesh(shipModel, GL20.GL_TRIANGLES, shipTex, new Vector3(1, 1, 1), 1), new Vector3());
		
		// END SHIP
		
		//  MAKE SKYBOX
		
		Texture seatex = new Texture(Gdx.files.internal("data/textures/water.png"));
		seatex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//seatex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		Weather weather = new Weather(new Vector3(0.4f, 0.6f, 0.6f), new Vector3(-0.3f, -0.3f, 0), new Vector3(0.05f, 0.03f, 0.08f), new Vector3(-0.05f, 0.03f, 0.08f), new Clouds());
		Sea sea = new Sea(seatex, new Vector3(0.0f, 0.3f, 0.5f), terrain);
		skybox = new SkyBox(sea, weather);
		
		// END SKYBOX
		
		// MAKE PLAYER
		
		player = new Entity(new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());

		Selector sselect = new ConcurrentSelector();
		BehaviourTree btree = new BehaviourTree(sselect);
		sselect.addNode(new ActionUpdateAnimations(), 0);
		sselect.addNode(new ActionEvaluateDamage(), 1);
		sselect.addNode(new ActionGravityAndMovement(), 2);
		
		Selector pselect = new PrioritySelector();
		pselect.addNode(new ActionPlayerControl(controls, cam), 0);
		pselect.addNode(new ConditionalAnimationLock(BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED), 1);
		
		sselect.addNode(pselect, 2);
		
		//player.setAI(new AI_Player_Control(controls, cam));
		player.setAI(btree);
		Sprite3D s = new Sprite3D(8, 8, 4, 4);
		s.setGender(GENDER.MALE);
		s.addAnimation("move", "move");
		s.addAnimation("attack_1", "attack", "_1");
		Mesh playerMesh = FileUtils.loadMesh("data/models/human.obj");
		AnimatedModel am = new AnimatedModel(FileUtils.loadModel("data/models/man2.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/skin_d.png", true, null, null), FileUtils.loadTexture("data/textures/skin_s.png", true, null, null)}, new Vector3(0.7f, 0.7f, 0.7f), "walk");
		AnimatedModel hair = new AnimatedModel(FileUtils.loadModel("data/models/hair1.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/hair.png", true, null, null)}, new Vector3(1.0f, 1.0f, 1.0f), null);
		AnimatedModel sword = new AnimatedModel(FileUtils.loadModel("data/models/axe.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/axe_d.png", true, null, null), FileUtils.loadTexture("data/textures/axe_s.png", true, null, null), FileUtils.loadTexture("data/textures/axe_e.png", true, null, null)}, new Vector3(1.0f, 1.0f, 1.0f), "idle");
		System.out.println(sword.model.model.meshes.get(0).getNumVertices());
		MotionTrail swordTrail = new MotionTrail(60, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true, null, null));
		ParticleEffect effect = FileUtils.loadParticleEffect("data/effects/boom.effect");
		player.addRenderable(am, new Vector3());
		//player.addRenderable(effect, new Vector3());
		
		am.attach("DEF-head", hair, new Matrix4().rotate(0, 0, 1, -90).translate(0.1f, 0.5f, 0));
		am.attach("DEF-palm_01_R", sword, new Matrix4().rotate(1, 0, 0, 180).rotate(0, 0, 1, 20));
		//sword.attach("top", effect, new Matrix4().rotate(0, 1, 0, -90).rotate(1, 0, 0, 180).rotate(0, 0, 1, 20).scl(1, 3, 1));
		am.attach(null, swordTrail, new Matrix4());
		
		//sword.attach("top", effect, new Matrix4());
		
		player.readOnlyRead(PositionalData.class).scale.set(2, 2, 2);
		
		player.readOnlyRead(StatusData.class).factions.add("Player");
		
		EquipmentData eData = new EquipmentData();
		player.readData(eData, EquipmentData.class);
		eData.equip(Equipment_Slot.BODY, new Armour(new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(new SPRITESHEET("Hair1", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.HEAD), null));
		eData.equip(Equipment_Slot.LEGS, new Armour(new SPRITESHEET("BasicClothes", new Color(0.4f, 0.5f, 1.0f, 1.0f), 0, SpriteLayer.TOP), null));
		ATTACK_STAGE[] attacks = {
				new ATTACK_STAGE("attack1_1", 2.0f, 20, 10, 1),
				new ATTACK_STAGE("attack1_2", 1.0f, 30, 10, 2),
				new ATTACK_STAGE("attack1_2", 1.2f, 40, 10, 3),
				new ATTACK_STAGE("attack1_2", 1.4f, 50, 10, 4),
				new ATTACK_STAGE("attack1_2", 1.6f, 60, 10, 5),
				new ATTACK_STAGE("attack1_2", 1.8f, 70, 10, 6),
				new ATTACK_STAGE("attack1_2", 2.0f, 80, 10, 7),
				new ATTACK_STAGE("attack1_2", 2.2f, 90, 10, -1)
		};
		eData.equip(Equipment_Slot.RARM, new Weapon(attacks, new SPRITESHEET("sword", Color.WHITE, 0, SpriteLayer.OTHER), new DESCRIPTION(null, null, null, null), 0.5f, 0.3f, sword, swordTrail));
		player.writeData(eData, EquipmentData.class);
		
		OcttreeEntry<Entity> entry = rw.createEntry(player, player.readOnlyRead(PositionalData.class).position, new Vector3(10, 10, 10), Octtree.MASK_AI | Octtree.MASK_RENDER);
		player.readOnlyRead(PositionalData.class).octtreeEntry = entry;
		rw.add(entry);
		bw.add(new btSphereShape(10), new Matrix4().setToTranslation(player.readOnlyRead(PositionalData.class).position), player, (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI), (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_GHOST));
		
		// END PLAYER
		
		// MAKE NPC 1
		
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
		npc.readOnlyRead(PositionalData.class).position.set(-4, 12, 0);
		npc.readOnlyRead(StatusData.class).factions.add("Player");
		npc.readData(eData, EquipmentData.class);
		eData.equip(Equipment_Slot.BODY, new Armour(new SPRITESHEET("Human", Color.WHITE, 0, SpriteLayer.BODY), null));
		eData.equip(Equipment_Slot.HEAD, new Armour(new SPRITESHEET("Hair1", new Color(0.9f, 0.5f, 0.7f, 1.0f), 0, SpriteLayer.HEAD), null));
		npc.writeData(eData, EquipmentData.class);
		
		// END NPC 1
		
		// MAKE ENEMIES
		
		Random ran = new Random();
		for (int i = 0; i < 200; i++)
		{
			Entity ge = new Entity(new PositionalData(), new AnimationData(), new StatusData(), new EquipmentData());
			
			Selector gsselect = new ConcurrentSelector();
			BehaviourTree gbtree = new BehaviourTree(gsselect);
			gsselect.addNode(new ActionUpdateAnimations(), 0);
			gsselect.addNode(new ActionEvaluateDamage(), 1);
			gsselect.addNode(new ActionGravityAndMovement(), 2);
			
			Selector gpselect = new PrioritySelector();
			Selector grselect = new RandomSelector(new Random());
			grselect.addNode(new ActionWait(1), 0);
			grselect.addNode(new ActionRandomWalk(new Random(), 1), 1);
			gpselect.addNode(grselect, 0);
			
			Selector gsselect2 = new SequenceSelector();
			
			Selector gpselect2 = new PrioritySelector();
			
			gpselect2.addNode(new ActionMoveToClosest(true), 0);
			gpselect2.addNode(new ActionAttack(15, false, true), 1);
			gpselect2.addNode(new ConditionalAnimationLock(BehaviourTreeState.FINISHED, BehaviourTreeState.FAILED), 2);
			
			gsselect2.addNode(gpselect2, 0);
			
			gsselect2.addNode(new ActionFindClosestVisible(new OcttreeBox(new Vector3(), new Vector3(50, 50, 50), null), true), 1);
			
			gpselect.addNode(gsselect2, 1);
			
			gsselect.addNode(gpselect, 3);
			
			//AI_Follow ai = new AI_Follow();
			//ai.setFollowTarget(player);
			ge.setAI(gbtree);
			PositionalData pData = ge.readOnlyRead(PositionalData.class);
			pData.position.set(ran.nextFloat()*10000, 1000, ran.nextFloat()*10000);
			pData.scale.set(2, 2, 2);
						
			s = new Sprite3D(3, 3, 4, 4);
			s.setGender(GENDER.MALE);
			s.addAnimation("move", "move");
			s.addAnimation("attack_1", "attack");
			s.updateSpritesheets(ge);
			
			ge.readOnlyRead(StatusData.class).factions.add("Enemy");
			
			AnimatedModel gam = new AnimatedModel(FileUtils.loadModel("data/models/man2.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/skin_d.png", true, null, null), FileUtils.loadTexture("data/textures/skin_s.png", true, null, null)}, new Vector3(0.7f, 0.7f, 0.7f), "walk");
			AnimatedModel ghair = new AnimatedModel(FileUtils.loadModel("data/models/hair1.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/hair.png", true, null, null)}, new Vector3(1.0f, 1.0f, 1.0f), null);
			AnimatedModel gsword = new AnimatedModel(FileUtils.loadModel("data/models/axe.g3db"), new Texture[]{FileUtils.loadTexture("data/textures/axe_d.png", true, null, null), FileUtils.loadTexture("data/textures/axe_s.png", true, null, null), FileUtils.loadTexture("data/textures/axe_e.png", true, null, null)}, new Vector3(1.0f, 1.0f, 1.0f), "idle");
			MotionTrail gswordTrail = new MotionTrail(60, Color.WHITE, FileUtils.loadTexture("data/textures/gradient.png", true, null, null));
			ge.addRenderable(gam, new Vector3());
			
			gam.attach("DEF-head", ghair, new Matrix4().rotate(0, 0, 1, -90).translate(0.1f, 0.5f, 0));
			gam.attach("DEF-palm_01_R", gsword, new Matrix4().rotate(1, 0, 0, 180).rotate(0, 0, 1, 20));
			gam.attach(null, gswordTrail, new Matrix4());
			
			ge.readData(eData, EquipmentData.class);
			eData.equip(Equipment_Slot.BODY, new Armour(new SPRITESHEET("devil", Color.WHITE, 0, SpriteLayer.BODY), null));
			eData.equip(Equipment_Slot.RARM, new Weapon(attacks, new SPRITESHEET("sword", Color.WHITE, 0, SpriteLayer.OTHER), new DESCRIPTION(null, null, null, null), 0.5f, 0.3f, gsword, gswordTrail));
			ge.writeData(eData, EquipmentData.class);
			
			entry = rw.createEntry(ge, pData.position, new Vector3(10, 10, 10), Octtree.MASK_AI | Octtree.MASK_RENDER);
			pData.octtreeEntry = entry;
			rw.add(entry);
			bw.add(new btSphereShape(10), new Matrix4().setToTranslation(pData.position), ge, (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI), (short) (BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_AI | BulletWorld.FILTER_GHOST));
		}
		
		// END ENEMIES
		
		// MAKE TREES
		
		Mesh grassMesh = FileUtils.loadMesh("data/models/pinet.obj");
		Texture pinetex = FileUtils.loadTexture("data/textures/pinet.png", true, TextureFilter.MipMapLinearLinear, null);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, new Texture[]{pinetex}, new Vector3(1, 1, 1), false), 1, 2500, 50);
		btBoxShape tBox = new btBoxShape(new Vector3(10, 50, 10));
		for (Entity v : veggies)
		{
			v.update(0);
			entry = rw.createEntry(v, v.readOnlyRead(MinimalPositionalData.class).position, new Vector3(10, 50, 10), Octtree.MASK_RENDER);
			rw.add(entry);
			bw.add(tBox, new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position), v, (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER), (short)(BulletWorld.FILTER_COLLISION | BulletWorld.FILTER_RENDER | BulletWorld.FILTER_GHOST));
		}
		veggies.clear();
		
		// END TREES
		
		// MAKE GRASS
		
		grassMesh = FileUtils.loadMesh("data/models/shr2.obj");
		Texture shrd = FileUtils.loadTexture("data/textures/shr2_d.png", true, TextureFilter.MipMapLinearLinear, null);
		Texture shrs = FileUtils.loadTexture("data/textures/shr2_s.png", true, TextureFilter.MipMapLinearLinear, null);
		Texture shre = FileUtils.loadTexture("data/textures/shr2_e.png", true, TextureFilter.MipMapLinearLinear, null);
		terrain.vegetate(veggies, new ModelBatcher(grassMesh, GL20.GL_TRIANGLES, new Texture[]{shrd, shrs, shre}, new Vector3(1, 1, 1), true), 1, 50000, 50);
		btBoxShape box = new btBoxShape(new Vector3(1, 1, 1));
		for (Entity v : veggies)
		{
			v.update(0);
			//rw.add(new btBoxShape(new Vector3(1, 1, 1)), new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position), v, BulletWorld.FILTER_RENDER, BulletWorld.FILTER_GHOST);
			btCollisionObject o = new btCollisionObject();
			o.setCollisionShape(box);
			o.setWorldTransform(new Matrix4().setToTranslation(v.readOnlyRead(MinimalPositionalData.class).position));
			OcttreeEntry<Entity> oe = veggieTree.createEntry(v, v.readOnlyRead(MinimalPositionalData.class).position, new Vector3(1, 1, 1), Octtree.MASK_RENDER);
			veggieTree.add(oe);
		}
		veggies.clear();
		
		// END GRASS
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
//		player.readData(sData, StatusData.class);
//		player.readData(pData, PositionalData.class);
//		batch.draw(blank, screen_width-80, screen_height-40, ((float)sData.currentHealth/(float)sData.MAX_HEALTH)*50, 10);
//		font.draw(spriteBatch, ""+pData.position, 20, screen_height-80);
//		font.draw(spriteBatch, ""+pData.rotation, 20, screen_height-120);
//		
//		player.readData(pData, PositionalData.class);
//		box.center.set(pData.rotation).scl(2).add(pData.position);
//		Entity e = activate(box, pData.graph, elist, pData.position, pData);
//		if (e != null)
//		{
//			font.draw(spriteBatch, ""+e.getActivationAction().getDesc(), 220, 220);
//		}
		
		for (Dialogue d : GLOBALS.DIALOGUES)
		{
			d.queue2D(batch);
		}
	}
	
//	protected Entity activate(CollisionShape<?> shape, EntityGraph graph, List<EntityGraph> list, Vector3 pos, PositionalData pData)
//	{
//		boolean found = GLOBALS.WORLD.collide(shape, graph, list);
//		if (!found) return null;
//		
//		float min = Float.MAX_VALUE;
//		Entity chosen = null;
//		
//		for (EntityGraph eg : list)
//		{
//			if (eg.entity != null && eg.entity.hasActivationAction())
//			{
//				eg.entity.readData(pData, PositionalData.class);
//				float dist = pos.dst2(pData.position);
//				if (dist < min) 
//				{
//					min = dist;
//					chosen = eg.entity;
//				}
//			}
//		}
//		list.clear();
//		
//		return chosen;
//	}

	ImmediateModeRenderer imr = new ImmediateModeRenderer20(false, false, 0);
	@Override
	public void drawSkybox(float delta)
	{
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
	
	float updateCooldown = 0;
	boolean update = false;
	Array<Entity> renderEntities = new Array<Entity>();
	Array<Entity> aiEntities = new Array<Entity>();
	@Override
	public void queueRenderables(float delta, HashMap<Class, Batch> batches) {
				
		if (update) 
		{
			renderEntities.clear();
			GLOBALS.renderTree.collectAll(renderEntities, cam.renderFrustum, true, Octtree.MASK_RENDER);
		}
		for (Entity e : renderEntities)
		{
			e.queueRenderables(cam, GLOBALS.LIGHTS, delta, batches);
		}
		
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
		
		if (update) 
		{
			veggies.clear();
			veggieTree.collectAll(veggies, veggieCam.renderFrustum, true, Octtree.MASK_RENDER);
		}
		for (Entity e : veggies)
		{
			e.queueRenderables(veggieCam, GLOBALS.LIGHTS, delta, batches);
		}
		
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
		GLOBALS.SKYBOX = skybox;
		GLOBALS.player = player;
	}

	@Override
	public void superDispose() {
		sB.dispose();
		fB.dispose();
	}

	LinkedList<Runnable> list = new LinkedList<Runnable>();
	boolean increase = true;
	
	float strike_time = 0.0f;
	Random ran = new Random();
	PositionalData pData = new PositionalData();
	@Override
	public void update(float delta) {
		
		if (update) update = false;
		updateCooldown -= delta;
		if (updateCooldown < 0)
		{
			updateCooldown = 0.1f;
			update = true;
		}
					
		if (update) 
		{
			GLOBALS.physicsWorld.update(delta);
			aiEntities.clear();
			GLOBALS.renderTree.collectAll(aiEntities, cam.aiShape, true, Octtree.MASK_AI);
		}
		for (Entity e : aiEntities)
		{
			if (e.getAI() == null) continue;
			StatusData sData = e.readOnlyRead(StatusData.class);
			if (sData.DAMAGED > 0)
			{
				float mag = 1.0f - ((float)sData.DAMAGED) / ((float)sData.MAX_HEALTH/2);
				if (mag > 1.0f) mag = 1.0f;
				
				e.readData(pData, Entity.PositionalData.class);
				Decal decal = ImageUtils.getTextDecal(1.0f*GLOBALS.numDigits(sData.DAMAGED), 3.2f, sB, fB, null, ""+sData.DAMAGED);
				tParticles.add(new TextParticle(decal, 3.0f, pData.position.add(0, 2, 0), new Vector3(0, 3.6f, 0), new Vector3(1.0f, mag, 0.0f)));
				sData.DAMAGED = 0;
			}
			
			if (!sData.ALIVE)
			{
				GLOBALS.physicsWorld.remove(e.readOnlyRead(PositionalData.class).physicsBody);
				e.dispose();
			}
			else list.add(e.getRunnable(delta));
		}
		
		GLOBALS.submitTasks(list);
		list.clear();
		GLOBALS.waitTillTasksComplete();
		
		
		GLOBALS.SKYBOX.update(delta);
						
		((FollowCam)cam).update(player);
		veggieCam.position.set(cam.position);
		veggieCam.direction.set(cam.direction);
		veggieCam.update();
		
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
	public void resized(int width, int height) {
		veggieCam.viewportWidth = width;
		veggieCam.viewportHeight = height;
		veggieCam.near = 2f;
		veggieCam.far = 1502f;
		veggieCam.update();
	}

}
