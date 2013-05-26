package Screens;

import java.util.ArrayList;
import java.util.Random;

import Entities.Entity;
import Entities.SymbolicMesh;
import Entities.AI.AI_Follow;
import Entities.AI.AI_Player_Control;
import Graphics.SkyBox;
import Graphics.Sprite3D;
import Graphics.Lights.Light;
import Graphics.Lights.LightManager;
import Graphics.Renderers.CellShadingRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.FollowCam;
import com.lyeeedar.Pirates.GLOBALS;
import com.lyeeedar.Pirates.ProceduralGeneration.IslandGenerator;

public class GameScreen extends AbstractScreen {
	
	SkyBox skyBox;
	Controls controls;
	Mesh model;
	Mesh character;
	Mesh plane;
	Texture texture;
	Texture texture1;
	Matrix4 tmpMat = new Matrix4();
	Matrix4 tmpMat1 = new Matrix4();
	Matrix4 tmpMat2 = new Matrix4();
	Vector3 colour = new Vector3();
	Entity player;
	Entity.EntityData data = new Entity.EntityData();
	
	int numEntities = 5;
	ArrayList<Entity> entities = new ArrayList<Entity>();
	ArrayList<Sprite3D> sprites = new ArrayList<Sprite3D>();
	
	CellShadingRenderer renderer;
	LightManager lights;
	
	Sprite3D decal;
	
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
		controls = new Controls(GLOBALS.ANDROID);
		cam = new FollowCam(controls);
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/blank.png"));
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls));
		
		IslandGenerator ig = new IslandGenerator();
		model = ig.getIsland(40, 40, 40);
		//texture = new Texture(Gdx.files.internal("data/shipTex.png"));
		//model = loader.loadObj(Gdx.files.internal("data/shipMesh.obj"), true).subMeshes[0].mesh;
		GLOBALS.TEST_NAV_MESH = SymbolicMesh.getSymbolicMesh(model, 1f);
		
		renderer = new CellShadingRenderer();
		renderer.cam = cam;
		
		lights = new LightManager();
		lights.ambientColour.set(0.8f, 0.9f, 0.7f);
		lights.directionalLight.colour.set(0.2f, 0.3f, 0.2f);
		lights.directionalLight.direction.set(0, 0.5f, -0.5f);
		
		l = new Light(new Vector3(), new Vector3(0.4f, 0.4f, 0.4f), 0.5f);
		
		lights.add(l);

		Texture tex = new Texture(Gdx.files.internal("data/test.png"));
		skyBox = new SkyBox(tex, new Vector3(0.0f, 0.79f, 1), new Vector3(1, 1, 1));
		
		Texture female = new Texture(Gdx.files.internal("data/female.png"));
		female.setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
		female.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			
			ge.readData(data);
			data.position.set(50, 55, 60);
			ge.writeData(data);
			entities.add(ge);
			
			sprites.add(new Sprite3D(female, 4, 1, 2, 0.1f));
		}
		
		decal = new Sprite3D(female, 4, 1, 2, 0.1f);
		
		System.out.println("Load time: "+(System.nanoTime()-time)/1000000);
	}

	@Override
	public void drawModels(float delta) {
		
		renderer.begin();
		
		renderer.add(model, GL20.GL_TRIANGLES, texture, colour.set(1f, 1f, 1f), GLOBALS.TEST_NAV_MESH.getCombined(), 0);
		
		//renderer.add(shipMesh, GL20.GL_TRIANGLES, shipTex, colour.set(1, 1, 1), tmpMat.setToTranslation(-10, 5, -10), 0);
		
		//player.readData(data);
		//tmpMat.setToTranslation(data.position).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, data.rotation));
		//renderer.add(character, GL20.GL_TRIANGLES, texture1, colour.set(1, 0.7f, 0.6f), tmpMat, 1);
		
		//decal.
		
//		for (Entity ge : entities)
//		{
//			ge.readData(data);
//			tmpMat.setToTranslation(data.position).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, data.rotation));
//			renderer.add(character, GL20.GL_TRIANGLES, texture1, colour.set(1, 0.7f, 0.6f), tmpMat, 1);
//		}

		renderer.end(lights);
		skyBox.render(cam);

		decalBatch.flush();
	}

	@Override
	public void drawTransparent(float delta) {
		decal.render(decalBatch);
		
		for (Sprite3D sprite : sprites)
		{
			sprite.render(decalBatch);
		}
	}

	@Override
	public void drawOrthogonals(float delta) {
		// TODO Auto-generated method stub

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
			
			s.setPosition(data.position);
			s.setRotation(data.rotation);
			
			s.update(delta, cam);
		}
		
		player.readData(data);
		
		cam.update(data);

		decal.setPosition(data.position);
		decal.setRotation(data.rotation);
		decal.update(delta, cam);
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub
		
	}

}
