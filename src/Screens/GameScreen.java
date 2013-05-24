package Screens;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import Entities.Entity;
import Entities.SymbolicMesh;
import Entities.AI.AI_Follow;
import Entities.AI.AI_Player_Control;
import Graphics.SkyBox;
import Graphics.Lights.Light;
import Graphics.Lights.LightManager;
import Graphics.Renderers.CellShadingRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Pirates.Controls;
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
	
	int numEntities = 10;
	ArrayList<Entity> entities = new ArrayList<Entity>();
	
	CellShadingRenderer renderer;
	LightManager lights;
	
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
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/blank.png"));
		
		player = new Entity();
		player.setAI(new AI_Player_Control(player, controls, cam));
		
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
		Random ran = new Random();
		for (int i = 0; i < numEntities; i++)
		{
			Entity ge = new Entity();
			AI_Follow ai = new AI_Follow(ge);
			ai.setFollowTarget(player);
			ge.setAI(ai);
			
			ge.readData(data);
			data.position.set(ran.nextInt(30), 55, ran.nextInt(30));
			ge.writeData(data);
			entities.add(ge);
		}
		
		System.out.println("Load time: "+(System.nanoTime()-time)/1000000);
	}

	@Override
	public void drawModels(float delta) {
		
		renderer.begin();
		
		renderer.add(model, GL20.GL_TRIANGLES, texture, colour.set(1f, 1f, 1f), GLOBALS.TEST_NAV_MESH.getCombined(), 0);
		
		//renderer.add(shipMesh, GL20.GL_TRIANGLES, shipTex, colour.set(1, 1, 1), tmpMat.setToTranslation(-10, 5, -10), 0);
		
		player.readData(data);
		tmpMat.setToTranslation(data.position).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, data.rotation));
		renderer.add(character, GL20.GL_TRIANGLES, texture1, colour.set(1, 0.7f, 0.6f), tmpMat, 1);
		
		for (Entity ge : entities)
		{
			ge.readData(data);
			tmpMat.setToTranslation(data.position).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, data.rotation));
			renderer.add(character, GL20.GL_TRIANGLES, texture1, colour.set(1, 0.7f, 0.6f), tmpMat, 1);
		}

		renderer.end(lights);
		
		skyBox.render(cam);

	}

	@Override
	public void drawTransparent(float delta) {
		// TODO Auto-generated method stub

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
		
		GLOBALS.TEST_NAV_MESH.setPosition(0, -2, 0);
		GLOBALS.TEST_NAV_MESH.setRotation(GLOBALS.DEFAULT_ROTATION, GLOBALS.DEFAULT_ROTATION);
		GLOBALS.TEST_NAV_MESH.updateMatrixes();
		
//		if (System.currentTimeMillis()-time > 5000)
//		{
//			try {
//				Thread.sleep(1500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			time = System.currentTimeMillis();
//		}

	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub
		
	}

}
