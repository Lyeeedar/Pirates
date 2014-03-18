package com.Lyeeedar.Graphics.Lights;

import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeShape;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LightManager 
{
	private static final int MAX_LIGHTS = 4;
	
	Octtree<Light> lightGraph = new Octtree<Light>(null, new Vector3(-10000, -10000, -10000), new Vector3(10000, 10000, 10000));
	public final Vector3 ambientColour = new Vector3();
	
	private final float[] positions = new float[3*MAX_LIGHTS];
	private final float[] colours = new float[3*MAX_LIGHTS];
	private final float[] attenuations = new float[MAX_LIGHTS];
	public DirectionalLight dl;
	
	Array<Light> lightArray = new Array<Light>(false, 16);
	
	private final Vector3 tmpVec = new Vector3();
	
	public void addLight(Light light)
	{
		light.createEntry(lightGraph);
		lightGraph.add(light.entry);
	}
	
	public void collectAll(Array<Light> output, OcttreeShape shape, int bitmask)
	{
		lightGraph.collectAll(output, shape, bitmask);
	}
	
	public void sort(OcttreeShape shape, Camera cam, RenderType renderType)
	{
		if (renderType == RenderType.FORWARD)
		{
			lightGraph.collectAll(lightArray, shape, Octtree.MASK_DIRECTION_LIGHT);
			
			DirectionalLight directionalLight = (DirectionalLight) lightArray.get(0);
			dl = directionalLight;
			dl.computeShadowMap(cam);
			
			tmpVec.set(directionalLight.direction).scl(GLOBALS.FOG_MAX).add(cam.position);
			
			positions[0] = tmpVec.x;
			positions[1] = tmpVec.y;
			positions[2] = tmpVec.z;
			
			colours[0] = directionalLight.colour.x;
			colours[1] = directionalLight.colour.y;
			colours[2] = directionalLight.colour.z;
			
			attenuations[0] = 0;
			
			lightArray.clear();
			
			lightGraph.collectAll(lightArray, shape, Octtree.MASK_POINT_LIGHT);
			
			int i = 1;
			int num_lights = (MAX_LIGHTS > lightArray.size+1) ? lightArray.size+1 : MAX_LIGHTS;
			for (; i < num_lights; i++)
			{
				PointLight light = (PointLight) lightArray.get(i-1);
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
		}
		else if (renderType == RenderType.DEFERRED)
		{
			lightGraph.collectAll(lightArray, shape, Octtree.MASK_SHADOW_CASTING);
			for (Light l : lightArray)
			{
				l.computeShadowMap(cam);
			}
			lightArray.clear();
		}
	}
	
	public void applyLights(ShaderProgram shader, int allowedTexCoord)
	{
		shader.setUniformf("u_al_col", ambientColour);
		
		shader.setUniform3fv("u_pl_pos", positions, 0, 3*MAX_LIGHTS);
		shader.setUniform3fv("u_pl_col", colours, 0, 3*MAX_LIGHTS);
		shader.setUniform1fv("u_pl_att", attenuations, 0, MAX_LIGHTS);
		
		if (dl != null && dl.shadowMap != null)
		{
			shader.setUniformMatrix("u_depthBiasMVP", dl.depthBiasMVP);
			dl.shadowMap.bind(allowedTexCoord);
			shader.setUniformi("u_shadowMapTexture", allowedTexCoord);
			shader.setUniformf("u_poisson_scale", dl.orthoCam.far*15);
		}
	}
	
}
