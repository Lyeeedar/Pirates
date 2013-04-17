package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.lyeeedar.Pirates.Controls;
import com.lyeeedar.Pirates.GLOBALS;
import com.lyeeedar.Pirates.ProceduralGeneration.IslandGenerator;

import Entities.GameEntity;
import Entities.NavMesh;
import Entities.AI.AI_Player_Control;
import Graphics.SkyBox;
import Graphics.Lights.Light;
import Graphics.Lights.LightManager;
import Graphics.Renderers.CellShadingRenderer;

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
	GameEntity player;
	
	CellShadingRenderer renderer;
	LightManager lights;
	
	Light light;
	
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
		Texture tex = new Texture(Gdx.files.internal("data/test.png"));
		skyBox = new SkyBox(tex);
		controls = new Controls(GLOBALS.ANDROID);
		
		ObjLoader loader = new ObjLoader();
		character = loader.loadObj(Gdx.files.internal("data/character.obj")).subMeshes[0].mesh;
		
		texture = new Texture(Gdx.files.internal("data/grass.png"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		//texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear);
		
		texture1 = new Texture(Gdx.files.internal("data/blank.png"));
		plane = genPlane(100, 0, 100);

		player = new GameEntity();
		player.setAi(new AI_Player_Control(player, controls, cam));
		
		IslandGenerator ig = new IslandGenerator();
		
		model = ig.getIsland(20);
		GLOBALS.TEST_NAV_MESH = NavMesh.getNavMesh(model, 3f);
		
		renderer = new CellShadingRenderer();
		renderer.cam = cam;
		
		lights = new LightManager();
		lights.ambientColour.set(0.8f, 0.9f, 0.7f);
		lights.directionalLight.colour.set(0.4f, 0.7f, 0.5f);
		lights.directionalLight.direction.set(0, -1, 0);
		
		light = new Light(new Vector3(), new Vector3(1, 1, 1), 0.05f);
		
		lights.add(light);	
	}

	@Override
	public void drawModels(float delta) {

		renderer.begin();
		renderer.add(model, GL20.GL_TRIANGLES, texture1, new Vector3(0.5f, 1, 0.5f), new Matrix4());
		
		tmpMat.setToTranslation(player.getPosition()).mul(tmpMat1.setToRotation(GLOBALS.DEFAULT_ROTATION, player.getRotation()));
		renderer.add(character, GL20.GL_TRIANGLES, texture1, new Vector3(1, 0.7f, 0.6f), tmpMat);
		
		tmpMat2.setToTranslation(player.getPosition().x, 0, player.getPosition().z);
		renderer.add(plane, GL20.GL_TRIANGLES, texture1, new Vector3(0.2f, 0.8f, 1), tmpMat2);
		
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
		
		light.position.set(player.getPosition()).add(0, 2, 0);
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub

	}
	
	public static float[] genCubeVertices(float x, float y, float z)
	{
		x /= 2.0f;
		y /= 2.0f;
		z /= 2.0f;

		float[] cubeVerts = {
				-x, -y, -z, // bottom
				-x, -y, z,
				x, -y, z,
				x, -y, -z,

				-x, y, -z, // top
				-x, y, z,
				x, y, z,
				x, y, -z,

				-x, -y, -z, // back
				-x, y, -z,
				x, y, -z,
				x, -y, -z,

				-x, -y, z, // front
				-x, y, z,
				x, y, z,
				x, -y, z,

				-x, -y, -z, // left
				-x, -y, z,
				-x, y, z,
				-x, y, -z,

				x, -y, -z, // right
				x, -y, z,
				x, y, z,
				x, y, -z};

		float[] cubeNormals = {
				0.0f, -1.0f, 0.0f, // bottom
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f,

				0.0f, 1.0f, 0.0f, // top
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f,

				0.0f, 0.0f, -1.0f, // back
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f,
				0.0f, 0.0f,	-1.0f,

				0.0f, 0.0f, 1.0f, //front
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f,

				-1.0f, 0.0f, 0.0f, // left
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f,

				1.0f, 0.0f, 0.0f, // right
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f};

		float[] cubeTex = {

				0.0f, 0.0f, // bottom
				0.0f, z,
				x, z,
				x, 0.0f,

				x, 0.0f, // top
				x, z,
				0.0f, z,
				0.0f, 0.0f,

				x, y, // back
				x, 0.0f,
				0.0f, 0.0f,
				0.0f, y,

				x, y, // front
				x, 0.0f,
				0.0f, 0.0f,
				0.0f, y,

				z, y, // left
				0.0f, y,
				0.0f, 0.0f,
				z, 0.0f,

				z, y, // right
				0.0f, y,
				0.0f, 0.0f,
				z, 0.0f

		};

		float[] vertices = new float[24 * 8];
		int pIdx = 0;
		int nIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeNormals[nIdx++];
			vertices[i++] = cubeTex[tIdx++];
			vertices[i++] = cubeTex[tIdx++];
		}

		return vertices;
	}

	public static short[] genCubeIndices()
	{
		return new short[] {
				0, 2, 1, // bottom
				0, 3, 2,

				4, 5, 6, // top
				4, 6, 7,

				8, 9, 10, // back
				8, 10, 11,

				12, 15, 14, // front
				12, 14, 13,

				16, 17, 18, // left
				16, 18, 19,

				20, 23, 22, // right
				20, 22, 21
		};
	}

	public static Mesh genCuboid (float x, float y, float z) {

		Mesh mesh = new Mesh(true, 24, 36, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.Normal, 3, "a_normal"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));

		float[] vertices = genCubeVertices(x, y, z);

		short[] indices = genCubeIndices();

		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
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

}
