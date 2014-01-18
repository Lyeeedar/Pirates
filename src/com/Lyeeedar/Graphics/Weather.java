package com.Lyeeedar.Graphics;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.ImageUtils;
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
	
	private final ShaderProgram skyShader;
	
	private final Mesh box;
	
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	Vector3 colDay = new Vector3(); 
	Vector3 shiftDay = new Vector3();
	Vector3 colNight = new Vector3();
	Vector3 shiftNight = new Vector3();
	
	public Weather(Vector3 colDay, Vector3 shiftDay, Vector3 colNight, Vector3 shiftNight, Clouds clouds)
	{
		this.clouds = clouds;
		this.colDay.set(colDay);
		this.colNight.set(colNight);
		this.shiftDay.set(shiftDay);
		this.shiftNight.set(shiftNight);

		box = Shapes.getBoxMesh(1, 1, 1, false, false);
		
		skyShader = new ShaderProgram(
				Gdx.files.internal("data/shaders/sky.vertex.glsl"),
				Gdx.files.internal("data/shaders/sky.fragment.glsl")
				);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
	}
	
	boolean increase = true;
	public void update(float delta)
	{
		clouds.update(delta);
		
		//delta /= 5;
		delta /= 300000.0f;
		
		if (increase) 
		{
			GLOBALS.LIGHTS.directionalLight.direction.y += delta;
			
			if (GLOBALS.LIGHTS.directionalLight.direction.y < 0.0f) 
			{
				GLOBALS.LIGHTS.directionalLight.direction.z += delta;
			}
			else
			{
				GLOBALS.LIGHTS.directionalLight.direction.z -= delta;
			}
		}
		else 
		{
			GLOBALS.LIGHTS.directionalLight.direction.y -= delta;
			
			if (GLOBALS.LIGHTS.directionalLight.direction.y < 0.0f) 
			{
				GLOBALS.LIGHTS.directionalLight.direction.z += delta;
			}
			else
			{
				GLOBALS.LIGHTS.directionalLight.direction.z -= delta;
			}
		}
		
		if (GLOBALS.LIGHTS.directionalLight.direction.y >= 1.0f) increase = false;
		if (GLOBALS.LIGHTS.directionalLight.direction.y < -1) increase = true;
		
		if (GLOBALS.LIGHTS.directionalLight.direction.y >= 0.1f)
		{
			GLOBALS.LIGHTS.directionalLight.colour.set(1, 1, 0);
			GLOBALS.LIGHTS.ambientColour.set(colDay);
			cs.set(shiftDay);
		}
		else if (GLOBALS.LIGHTS.directionalLight.direction.y <= -0.5f)
		{
			GLOBALS.LIGHTS.directionalLight.colour.set(0, 0, 0);
			GLOBALS.LIGHTS.ambientColour.set(colNight);
			cs.set(shiftNight);
		}
		else
		{
			float alpha = 1.0f - (GLOBALS.LIGHTS.directionalLight.direction.y+0.5f) / 0.6f;
			ImageUtils.lerp(colDay, colNight, alpha, GLOBALS.LIGHTS.ambientColour);
			ImageUtils.lerp(shiftDay, shiftNight, alpha, cs);
			GLOBALS.LIGHTS.directionalLight.colour.set(1.0f-alpha, 1.0f-alpha, 0);
		}	
//		
//		lights.ambientColour.x = (lights.directionalLight.direction.y+1)/2;
//		lights.ambientColour.y = (lights.directionalLight.direction.y+1)/2;
//		lights.ambientColour.z = (lights.directionalLight.direction.y+1)/2;
//		
//		strike_time -= delta;
//		if (strike_time < 0.0f)
//		{
//			GLOBALS.LIGHTS.ambientColour.set(0.05f, 0.07f, 0.12f);
//		}
//		else
//		{
//			GLOBALS.LIGHTS.ambientColour.set(0.1f, 0.1f, 0.7f);
//		}
//		
//		if (ran.nextInt(500) == 1)
//		{
//			strike_time = 0.1f;
//		}
	}
	
	Vector3 cs = new Vector3(-2, -2, -2);
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
		skyShader.setUniformf("sun_col", lights.directionalLight.colour);
		skyShader.setUniformf("sun_size", 0.01f);
		
		skyShader.setUniformf("sky_col", lights.ambientColour);
		skyShader.setUniformf("col_shift", cs);
		
		box.render(skyShader, GL20.GL_TRIANGLES);
		
		skyShader.end();
		
		clouds.render(cam, lights);
	}
	
}
