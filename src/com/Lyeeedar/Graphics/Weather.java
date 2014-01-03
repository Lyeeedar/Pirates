package com.Lyeeedar.Graphics;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Weather {
	
	public Clouds clouds;
	
	private final Texture skyTexture;
	private final Texture glowTexture;
	
	private final ShaderProgram skyShader;
	
	private final Mesh box;
	
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	public Weather(Texture skyTexture, Texture glowTexture, Clouds clouds)
	{
		this.skyTexture = skyTexture;
		this.glowTexture = glowTexture;
		this.clouds = clouds;

		box = Shapes.getBoxMesh(1, 1, 1, false, false);
		
		skyShader = new ShaderProgram(
				Gdx.files.internal("data/shaders/sky.vertex.glsl"),
				Gdx.files.internal("data/shaders/sky.fragment.glsl")
				);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
	}
	
	public void update(float delta)
	{
		clouds.update(delta);
	}
	
	public void render(Camera cam, LightManager lights)
	{	
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glDepthMask(false);
		
		skyShader.begin();
		
		mat41.set(cam.combined).mul(mat42.setToTranslation(cam.position));
		skyShader.setUniformMatrix("u_mvp", mat41);
		
		skyShader.setUniformf("sun_dir", lights.directionalLight.direction);
		
		glowTexture.bind(1);
		skyTexture.bind(0);
		
		skyShader.setUniformi("color", 0);
		skyShader.setUniformi("glow", 1);
		
		box.render(skyShader, GL20.GL_TRIANGLES);
		
		skyShader.end();
		
		clouds.render(cam, lights);
	}
	
}
