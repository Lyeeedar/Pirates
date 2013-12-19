package com.Lyeeedar.Graphics;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Clouds {
	
	Mesh mesh;
	Texture cloudTex;
	float time;
	Matrix4 mm = new Matrix4().setToLookAt(new Vector3(0, -1, 0), new Vector3(0, 0, 1));
	
	ShaderProgram shader;
	
	public Clouds(Texture cloudTex)
	{
		this.cloudTex = cloudTex;
		cloudTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		this.mesh = Shapes.getHemiSphereMesh(100, 100, 100, false);//Shapes.getPlaneMesh(1000, 1000, false);
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/clouds.vertex.glsl"),
				Gdx.files.internal("data/shaders/clouds.fragment.glsl")
				);
		if (!shader.isCompiled()) {
			System.err.println(shader.getLog());
		}
	}
	
	public void update(float delta)
	{
		time += delta;
	}
	
	public void render(Camera cam, LightManager lights)
	{
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		shader.begin();
		
		shader.setUniformMatrix("u_mvp", cam.combined);
		shader.setUniformMatrix("u_mm", mm);
		shader.setUniformf("u_pos", cam.position.x, cam.position.y-50.0f, cam.position.z);
		shader.setUniformf("u_time", time/500.0f);
		
		shader.setUniformi("u_texture", 0);
		cloudTex.bind(0);
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
	}

}
