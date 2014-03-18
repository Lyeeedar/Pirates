package com.Lyeeedar.Graphics;

import com.Lyeeedar.Graphics.Lights.DirectionalLight;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FollowCam;
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
	Vector3 tmpVec = new Vector3();
	private final Vector3 surface = new Vector3(0.0f, 1.0f, 0.9f);
	private final Vector3 deep = new Vector3(0.0f, 0.0f, 0.2f);
	
	public final DirectionalLight sun;
	
	public Weather(Vector3 colDay, Vector3 shiftDay, Vector3 colNight, Vector3 shiftNight, Clouds clouds)
	{
		this.clouds = clouds;
		this.colDay.set(colDay);
		this.colNight.set(colNight);
		this.shiftDay.set(shiftDay);
		this.shiftNight.set(shiftNight);

		box = Shapes.getBoxMesh(1, 1, 1, false, false);
		
		skyShader = new ShaderProgram(
				Gdx.files.internal("data/shaders/forward/sky.vertex.glsl"),
				Gdx.files.internal("data/shaders/forward/sky.fragment.glsl")
				);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
		
		sun = new DirectionalLight(new Vector3(-0.5f, 0.5f, 0.0f), new Vector3(), false);
	}
	
	boolean increase = false;
	public void update(float delta, FollowCam cam)
	{
		clouds.update(delta);
		
		//delta /= 10;
		delta /= 300000.0f;
		
		if (increase) 
		{
			sun.direction.y += delta;
			
			if (sun.direction.y < 0.0f) 
			{
				sun.direction.x += delta;
			}
			else
			{
				sun.direction.x -= delta;
			}
		}
		else 
		{
			sun.direction.y -= delta;
			
			if (sun.direction.y < 0.0f) 
			{
				sun.direction.x += delta;
			}
			else
			{
				sun.direction.x -= delta;
			}
		}
				
		if (sun.direction.y >= 1.0f) increase = false;
		if (sun.direction.y < -1) increase = true;
				
		if (sun.direction.y >= 0.1f)
		{
			sun.colour.set(1, 1, 0);
			GLOBALS.LIGHTS.ambientColour.set(colDay);
			cs.set(shiftDay);
		}
		else if (sun.direction.y <= -0.5f)
		{
			sun.colour.set(0, 0, 0);
			GLOBALS.LIGHTS.ambientColour.set(colNight);
			cs.set(shiftNight);
		}
		else
		{
			float alpha = 1.0f - (sun.direction.y+0.5f) / 0.6f;
			ImageUtils.lerp(colDay, colNight, alpha, GLOBALS.LIGHTS.ambientColour);
			ImageUtils.lerp(shiftDay, shiftNight, alpha, cs);
			sun.colour.set(1.0f-alpha, 1.0f-alpha, 0);
		}
		
		if (cam.underwater)
		{
			float depth = cam.position.y / -500.0f;
			if (depth > 1) depth = 1;
			depth = depth * depth;
			ImageUtils.lerp(surface, deep, depth, tmpVec);
			GLOBALS.LIGHTS.ambientColour.scl(tmpVec);
			sun.colour.scl(1.0f - depth);
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
	public void render(FollowCam cam, LightManager lights)
	{	
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glDepthMask(false);
		
		skyShader.begin();
		
		mat41.set(cam.combined).mul(mat42.setToTranslation(cam.position));
		skyShader.setUniformMatrix("u_mvp", mat41);
		
		skyShader.setUniformf("sun_dir", sun.direction);
		skyShader.setUniformf("sun_col", sun.colour);
		skyShader.setUniformf("sun_size", 0.008f);
		
		skyShader.setUniformf("sky_col", lights.ambientColour);
		skyShader.setUniformf("col_shift", cs);
		
		box.render(skyShader, GL20.GL_TRIANGLES);
		
		skyShader.end();
		
		if (!cam.underwater) clouds.render(cam, lights);
	}
	
}
