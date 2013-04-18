package Screens;

import Entities.GameEntity;
import Entities.NavMesh;
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
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
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
	GameEntity player;
	
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

	@Override
	public void create() {
		
		controls = new Controls(GLOBALS.ANDROID);
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/blank.png"));
		plane = genPlane(1000, 0, 1000);

		player = new GameEntity();
		player.setAi(new AI_Player_Control(player, controls, cam));
		
		IslandGenerator ig = new IslandGenerator();
		
		model = ig.getIsland(20);
		GLOBALS.TEST_NAV_MESH = NavMesh.getNavMesh(model, 50f);
		
		renderer = new CellShadingRenderer();
		renderer.cam = cam;
		
		lights = new LightManager();
		lights.ambientColour.set(0.8f, 0.9f, 0.7f);
		lights.directionalLight.colour.set(0.2f, 0.3f, 0.2f);
		lights.directionalLight.direction.set(0, 0.5f, -0.5f);

		Texture tex = new Texture(Gdx.files.internal("data/test.png"));
		skyBox = new SkyBox(tex);
	}

	@Override
	public void drawModels(float delta) {
		
		renderer.begin();
		
		renderer.add(model, GL20.GL_TRIANGLES, texture1, colour.set(0.5f, 1, 0.5f), GLOBALS.TEST_NAV_MESH.getCombined());
		
		tmpMat.setToTranslation(player.getPosition()).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, player.getRotation()));
		renderer.add(character, GL20.GL_TRIANGLES, texture1, colour.set(1, 0.7f, 0.6f), tmpMat);

		tmpMat2.setToTranslation(player.getPosition().x, 0, player.getPosition().z);
		renderer.add(plane, GL20.GL_TRIANGLE_STRIP, texture1, colour.set(0.2f, 0.8f, 1), tmpMat2);
		
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

	@Override
	public void update(float delta) {
		
		player.update(delta);
		
		GLOBALS.TEST_NAV_MESH.setPosition(0, -2, 0);
		GLOBALS.TEST_NAV_MESH.setRotation(GLOBALS.DEFAULT_ROTATION, GLOBALS.DEFAULT_ROTATION);
		GLOBALS.TEST_NAV_MESH.updateMatrixes();
		

	}
	
	public static Mesh genPlane (float x, float y, float z) {

		Mesh mesh = new Mesh(true, 4, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Normal, 3, "a_normal"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		
		float[] vertices = {
				-x, y, -z, 0, 1, 0, 0, 0,
				-x, y, z, 0, 1, 0, 0, 1,
				x, y, -z, 0, 1, 0, 1, 0,
				x, y, z, 0, 1, 0, 1, 1
				};

		mesh.setVertices(vertices);

		return mesh;
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub
		
	}

}
