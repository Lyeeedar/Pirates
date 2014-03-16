package com.Lyeeedar.Graphics;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.OctaveGenerator;
import com.Lyeeedar.Pirates.ProceduralGeneration.Noise.SimplexOctaveGenerator;
import com.Lyeeedar.Util.ImageUtils;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Clouds {
	
	Mesh mesh;
	Texture[] cloudTex;
	float time;
	Matrix4 mm = new Matrix4();
	
	ShaderProgram shader;
	
	public Clouds()
	{
		this.cloudTex = new Texture[4];
		for (int i = 0; i < 4; i++)
		{
			cloudTex[i] = ImageUtils.simplexNoiseTexture(64*(2*(i+1)), i, ("CloudsAreAwesome!"+i).hashCode());
		}
		
		this.mesh = Shapes.getCurvedPlaneMesh((int) GLOBALS.FOG_MAX+2000, 100, 0, 1, false);
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/clouds.vertex.glsl"),
				Gdx.files.internal("data/shaders/clouds.fragment.glsl")
				);
		if (!shader.isCompiled()) {
			System.err.println(shader.getLog());
		}
	}
	
	public float cloudCover = 0.5f;
	public float cloudSharpness = 0.9f;
	
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
		shader.setUniformMatrix("u_mm", mm.setToTranslation(cam.position));
		shader.setUniformf("u_pos", cam.position.x/6.0f, cam.position.y, cam.position.z/6.0f);
		shader.setUniformf("u_time", time);
		shader.setUniformf("u_height", 1000.0f);
		
		shader.setUniformf("u_ambient", lights.ambientColour);
		
		shader.setUniformf("CloudCover", cloudCover);
		shader.setUniformf("CloudSharpness", cloudSharpness);
		
		for (int i = 0; i < 4; i++)
		{
			shader.setUniformi("u_texture"+(i+1), i);
			cloudTex[i].bind(i);
		}
		
		mesh.render(shader, GL20.GL_TRIANGLES);
		
		shader.end();
	}

}
