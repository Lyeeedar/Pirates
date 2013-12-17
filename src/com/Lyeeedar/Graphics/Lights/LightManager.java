package com.Lyeeedar.Graphics.Lights;

import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LightManager {
	
	private static final int MAX_LIGHTS = 4;
	private static final int PRIORITY_STEPS = 512;
	
	public final Vector3 ambientColour = new Vector3();	
	public final Light directionalLight = new Light(new Vector3(), new Vector3(), new Vector3());	
	public final Array<Light> lights = new Array<Light>();
	
	private boolean needsSort = true;
	private final Vector3 lastSortPosition = new Vector3();
	
	private final float[] positions = new float[3*MAX_LIGHTS];
	private final float[] colours = new float[3*MAX_LIGHTS];
	private final float[] attenuations = new float[MAX_LIGHTS];
	
	public LightManager()
	{
		
	}
	
	public void remove(Light light)
	{
		light.manager = null;
		lights.removeValue(light, true);
		needsSort = true;
	}
	
	public void add(Light light)
	{
		light.manager = this;
		lights.add(light);
		needsSort = true;
	}
	
	public void sort(Vector3 position)
	{
		if (needsSort || !position.equals(lastSortPosition))
		{
			for (Light l : lights)
			{
				float distance = position.dst2(l.position);
				l.distance = (int)(PRIORITY_STEPS * (1.0f / (l.attenuation*distance)));
			}
			lights.sort();
			
			int i = 0;
			int num_lights = (MAX_LIGHTS > lights.size) ? lights.size : MAX_LIGHTS;
			for (; i < num_lights; i++)
			{
				Light light = lights.get(i);
				positions[(i*3)+0] = light.position.x;
				positions[(i*3)+1] = light.position.y;
				positions[(i*3)+2] = light.position.z;
				
				colours[(i*3)+0] = light.colour.x;
				colours[(i*3)+1] = light.colour.y;
				colours[(i*3)+2] = light.colour.z;
				
				attenuations[i] = light.attenuation;
			}
			
			for (; i < MAX_LIGHTS; i++)
			{
				positions[(i*3)+0] = 0;
				positions[(i*3)+1] = 0;
				positions[(i*3)+2] = 0;
				
				colours[(i*3)+0] = 0;
				colours[(i*3)+1] = 0;
				colours[(i*3)+2] = 0;
				
				attenuations[i] = 0;
			}
			
			lastSortPosition.set(position);
		}
	}
	
	public void applyLights(ShaderProgram shader)
	{
		shader.setUniformf("u_al_col", ambientColour);
		
		shader.setUniformf("u_dl_dir", directionalLight.direction);
		shader.setUniformf("u_dl_col", directionalLight.colour);
		
		shader.setUniform3fv("u_pl_pos", positions, 0, 3*MAX_LIGHTS);
		shader.setUniform3fv("u_pl_col", colours, 0, 3*MAX_LIGHTS);
		shader.setUniform1fv("u_pl_att", attenuations, 0, MAX_LIGHTS);
	}
	
	public Vector3 getLight(Vector3 position, Vector3 light)
	{
		Vector3 tmpVec = Pools.obtain(Vector3.class);
		light.set(ambientColour);
		for (Light l : lights)
		{
			float dist = l.position.dst(position);
			float brightness = 1.0f / (l.attenuation*dist);
			tmpVec.set(l.colour).scl(brightness);
			light.add(tmpVec);
		}
		Pools.free(tmpVec);
		if (light.x > 1.0f) light.x = 1.0f;
		if (light.y > 1.0f) light.y = 1.0f;
		if (light.z > 1.0f) light.z = 1.0f;
		return light;
	}
}
