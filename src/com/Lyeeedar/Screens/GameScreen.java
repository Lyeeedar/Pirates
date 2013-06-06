package com.Lyeeedar.Screens;

import java.util.ArrayList;
import java.util.Random;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.SymbolicMesh;
import com.Lyeeedar.Entities.AI.AI_Follow;
import com.Lyeeedar.Entities.AI.AI_Player_Control;
import com.Lyeeedar.Graphics.MotionTrail;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Sprite3D;
import com.Lyeeedar.Graphics.Lights.Light;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Renderers.AbstractRenderer;
import com.Lyeeedar.Graphics.Sprite3D.SpriteLayer;
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
	
	public GameScreen(LightManager lights) {
		super(lights);
		this.lights = lights;
	}

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
	Entity player;
	Entity.EntityData data = new Entity.EntityData();
	
	int numEntities = 5;
	ArrayList<Entity> entities = new ArrayList<Entity>();
	ArrayList<Sprite3D> sprites = new ArrayList<Sprite3D>();
	
	Sprite3D decal;
	
	MotionTrail trail;
	
	@Override
	public void show() {
		Gdx.input.setCursorCatched(true);

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
	public void resume() {
		// TODO Auto-generated method stub

	}

	Light l;
	@Override
	public void create() {
		
		long time = System.nanoTime();
		
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/models/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/textures/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/textures/blank.png"));
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		
		IslandGenerator ig = new IslandGenerator();
		model = ig.getIsland(40, 40, 40);
		//texture = new Texture(Gdx.files.internal("data/shipTex.png"));
		//model = loader.loadObj(Gdx.files.internal("data/shipMesh.obj"), true).subMeshes[0].mesh;
		GLOBALS.TEST_NAV_MESH = SymbolicMesh.getSymbolicMesh(model, 1f);

		Texture tex = new Texture(Gdx.files.internal("data/textures/test.png"));
		skyBox = new SkyBox(tex, new Vector3(0.0f, 0.79f, 1), new Vector3(1, 1, 1));
		
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			
			ge.readData(data);
			data.positionalData.position.set(50, 55, 60);
			ge.writeData(data);
			entities.add(ge);
			
			Sprite3D s = new Sprite3D(1, 2);
			s.setGender(false);
			s.addAnimation("move");
			s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
			s.create(GLOBALS.ASSET_MANAGER);
			
			sprites.add(s);
		}
		
		Sprite3D s = new Sprite3D(1, 2);
		s.setGender(false);
		s.addAnimation("move");
		s.addAnimation("attack_1");
		s.addLayer("human_body", Color.WHITE, 0, SpriteLayer.BODY);
		s.create(GLOBALS.ASSET_MANAGER);
		
		decal = s;
		
		trail = new MotionTrail(100, Color.WHITE, GLOBALS.ASSET_MANAGER.get("data/textures/gradient.png", Texture.class));
		
		System.out.println("Load time: "+(System.nanoTime()-time)/1000000);
	}

	@Override
	public void drawModels(float delta, AbstractRenderer renderer) {
		
		renderer.add(model, GL20.GL_TRIANGLES, texture, colour.set(1f, 1f, 1f), GLOBALS.TEST_NAV_MESH.getCombined(), 0);
	}
	
	@Override
	public void drawSkybox(float delta)
	{
		skyBox.render(cam);
	}

	@Override
	public void drawDecals(float delta, DecalBatch batch) {
		decal.render(batch);
		
		for (Sprite3D sprite : sprites)
		{
			sprite.render(batch);
		}
	}
	
	@Override
	public void drawTrails(float delta, MotionTrailBatch batch)
	{
		batch.render(trail);
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {
	
	}

	long time = System.currentTimeMillis();
	@Override
	public void update(float delta) {
		
		GLOBALS.submitTask(player.getRunnable(delta));//player.update(delta);
		for (Entity ge : entities) GLOBALS.submitTask(ge.getRunnable(delta));//ge.update(delta);
		GLOBALS.waitTillTasksComplete();
		
		skyBox.update(delta);
		
		GLOBALS.TEST_NAV_MESH.setPosition(0, -1, 0);
		GLOBALS.TEST_NAV_MESH.setRotation(GLOBALS.DEFAULT_ROTATION, GLOBALS.DEFAULT_ROTATION);
		GLOBALS.TEST_NAV_MESH.updateMatrixes();
		
		for (int i = 0; i < numEntities; i++)
		{
			entities.get(i).readData(data);
			
			Sprite3D s = sprites.get(i);
			
			s.setPosition(data.positionalData.position);
			s.setRotation(data.positionalData.rotation);
			if (data.animationData.updateAnimations){
				s.playAnimationLoop(data.animationData.anim, data.animationData.animation, data.animationData.useDirection);
				s.setAnimation(data.animationData.animate, data.animationData.animate_speed);
			}
			if (data.animationData.animationLock)
			{
				s.playAnimationSingle(data.animationData.playAnim, data.animationData.playAnimation, data.animationData.nextAnim, data.animationData.nextAnimation, data.animationData.startFrame, data.animationData.endFrame, data.animationData.informable);
			}
			
			s.update(delta, cam);
		}
		
		player.readData(data);
		
		cam.update(data);

		decal.setPosition(data.positionalData.position);
		decal.setRotation(data.positionalData.rotation);
		if (data.animationData.updateAnimations){
			decal.playAnimationLoop(data.animationData.anim, data.animationData.animation, data.animationData.useDirection);
			decal.setAnimation(data.animationData.animate, data.animationData.animate_speed);
		}
		if (data.animationData.animationLock)
		{
			decal.playAnimationSingle(data.animationData.playAnim, data.animationData.playAnimation, data.animationData.nextAnim, data.animationData.nextAnimation, data.animationData.startFrame, data.animationData.endFrame, data.animationData.informable);
		}
		decal.update(delta, cam);
		
		if (System.currentTimeMillis()-time > 10)
		{
			trail.update(data.positionalData.position, tmp.set(data.positionalData.position).add(0, 2, 0));
			time = System.currentTimeMillis();
		}
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub
		
	}

}
