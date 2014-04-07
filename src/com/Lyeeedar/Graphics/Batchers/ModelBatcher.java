package com.Lyeeedar.Graphics.Batchers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.PriorityQueue;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Graphics.Queueables.ModelBatchInstance.ModelBatchData;
import com.Lyeeedar.Graphics.Queueables.ModelBatchInstance.ModelBatchData.BatchedInstance;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.UniformBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;

public class ModelBatcher implements Batch
{
	public static int MAX_INSTANCES;
	public static final int BLOCK_SIZE = 16;
		
	private ShaderProgram solidShaderSampling;
	private ShaderProgram transparentShaderSampling;
	private ShaderProgram solidShaderNoSampling;
	private ShaderProgram transparentShaderNoSampling;
	private ShaderProgram simpleShader;
	private static ShaderProgram shader;
	
	private static UniformBufferObject ubo;
	
	private final HashSet<ModelBatchData> lookupTable;
	private final Array<ModelBatchData> queued;
	private final RenderType renderType;
	
	public ModelBatcher(RenderType renderType)
	{
		this.renderType = renderType;
		this.queued = new Array<ModelBatchData>(false, 16);
		this.lookupTable = new HashSet<ModelBatchData>();
		
		IntBuffer ib = BufferUtils.newIntBuffer(16);
		Gdx.gl.glGetIntegerv(GL30.GL_MAX_UNIFORM_BLOCK_SIZE, ib);
		int limitBytes = ib.get(0);
		int supportedMaxFloats = (limitBytes / 4);
		int supportedBlocks = supportedMaxFloats / Math.max(4, BLOCK_SIZE) ;
		MAX_INSTANCES = supportedBlocks ;
		
		if (ubo == null) ubo = new UniformBufferObject(4 * BLOCK_SIZE * MAX_INSTANCES, 1);
		
		if (renderType != RenderType.SIMPLE) loadSolidShader();
		if (renderType != RenderType.SIMPLE) loadTransparentShader();
		if (renderType == RenderType.SIMPLE) loadSimpleShader();
	}
	
	public void add(ModelBatchData data)
	{
		if (!lookupTable.contains(data))
		{
			lookupTable.add(data);
			queued.add(data);
		}
	}
	
	public void renderSolid(LightManager lights, Camera cam)
	{
		for (ModelBatchData mb : queued)
		{
			if (renderType == RenderType.SIMPLE) renderSimpleSolid(mb, mb.solidInstances, cam);
			else renderSolid(mb, lights, mb.solidInstances, cam);
		}
		ModelBatcher.end();
	}
	
	public void renderTransparent(LightManager lights, Camera cam)
	{
		for (ModelBatchData mb : queued)
		{
			if (renderType == RenderType.SIMPLE) renderSimpleTransparent(mb, mb.transparentInstances, cam);
			else renderTransparent(mb, lights, mb.transparentInstances, cam);
		}
		ModelBatcher.end();
		clear();
	}
	
	public void renderSolid(ModelBatchData data, LightManager lights, PriorityQueue<BatchedInstance> instances, Camera cam)
	{
		if (data.useTriplanarSampling) begin(solidShaderSampling, lights, cam, renderType);
		else begin(solidShaderNoSampling, lights, cam, renderType);
		
		flush(data, instances, cam);
	}
	public void renderTransparent(ModelBatchData data, LightManager lights, PriorityQueue<BatchedInstance> instances, Camera cam)
	{
		if (data.useTriplanarSampling) begin(transparentShaderSampling, lights, cam, renderType);
		else begin(transparentShaderNoSampling, lights, cam, renderType);
		
		flush(data, instances, cam);
	}
	public void renderSimpleSolid(ModelBatchData data, PriorityQueue<BatchedInstance> instances, Camera cam)
	{
		begin(simpleShader, null, cam, renderType);
		flush(data, instances, cam);
	}
	public void renderSimpleTransparent(ModelBatchData data, PriorityQueue<BatchedInstance> instances, Camera cam)
	{
		begin(simpleShader, null, cam, renderType);
		flush(data, instances, cam);
	}
	
	private void flush(ModelBatchData data, PriorityQueue<BatchedInstance> instances, Camera cam)
	{
		if (renderType != RenderType.SIMPLE)
		{	
			shader.setUniformi("u_texNum", data.textures.length);
			if (data.useTriplanarSampling) shader.setUniformf("u_triplanarScaling", data.triplanarScaling);
			
			for (int i = 0; i < data.textures.length; i++)
			{
				shader.setUniformi("u_texture"+i, i);
				data.textures[i].bind(i);
			}		
		}
		
		int i = 0;
		FloatBuffer floatbuffer = ubo.getDataBuffer().asFloatBuffer();
		while (!instances.isEmpty())
		{			
			BatchedInstance bi = instances.poll();
			floatbuffer.put(bi.transform.val);
			
			i++;
			if (i == MAX_INSTANCES)
			{
				ubo.bind();
				data.mesh.renderInstanced(shader, data.primitive_type, i);
				i = 0;
				floatbuffer = ubo.getDataBuffer().asFloatBuffer();
			}
			
			data.pool.free(bi);
		}
		
		if (i > 0)
		{
			ubo.bind();
			data.mesh.renderInstanced(shader, data.primitive_type, i);
		}
	}
	
	public void clear()
	{
		for (ModelBatchData data : queued)
		{
			data.clear();
		}
		
		queued.clear();
		lookupTable.clear();
	}
	
	public static void begin(ShaderProgram shader, LightManager lights, Camera cam, RenderType renderType)
	{
		if (ModelBatcher.shader == shader) return;
		
		if (ModelBatcher.shader != null) ModelBatcher.shader.end();
		
		ModelBatcher.shader = shader;
		shader.begin();
		
		shader.setUniformMatrix("u_pv", cam.combined);
		if (renderType != RenderType.SIMPLE) 
		{
			if (renderType == RenderType.FORWARD) lights.applyLights(shader, 4);
			shader.setUniformf("fog_col", lights.ambientColour);
			shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
			shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
			shader.setUniformf("u_viewPos", cam.position);
			Gdx.gl.glEnable(GL20.GL_BLEND);
		}
	}
	
	public static void end()
	{
		if (shader != null) shader.end();
		shader = null;
	}
	
	public void loadSolidShader()
	{
		if (solidShaderSampling != null && solidShaderNoSampling != null) return;
		
		String vert = "";
		String frag = "";
		
		if (renderType == RenderType.FORWARD)
		{
			vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/forward/modelbatcher.vertex.glsl").readString();
			frag = Gdx.files.internal("data/shaders/forward/modelbatcher.fragment.glsl").readString();
		}
		else if (renderType == RenderType.DEFERRED)
		{
			vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/deferred/modelbatcher.vertex.glsl").readString();
			frag = Gdx.files.internal("data/shaders/deferred/modelbatcher.fragment.glsl").readString();
		}
				
		solidShaderNoSampling = new ShaderProgram(vert, frag);
		if (!solidShaderNoSampling.isCompiled()) System.err.println(solidShaderNoSampling.getLog());
		
		solidShaderSampling = new ShaderProgram("#define USE_TRIPLANAR_SAMPLING\n"+vert, "#define USE_TRIPLANAR_SAMPLING\n"+frag);
		if (!solidShaderSampling.isCompiled()) System.err.println(solidShaderSampling.getLog());
		
		solidShaderSampling.registerUniformBlock("InstanceBlock", 1);
		solidShaderNoSampling.registerUniformBlock("InstanceBlock", 1);
	}
	
	public void loadTransparentShader()
	{
		if (transparentShaderSampling != null && transparentShaderNoSampling != null) return;
		
		String vert = "";
		String frag = "";
		
		if (renderType == RenderType.FORWARD)
		{
			vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/forward/modelbatcher.vertex.glsl").readString();
			frag = "#define HAS_TRANSPARENT\n" + Gdx.files.internal("data/shaders/forward/modelbatcher.fragment.glsl").readString();
		}
		else if (renderType == RenderType.DEFERRED)
		{
			vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/deferred/modelbatcher.vertex.glsl").readString();
			frag = "#define HAS_TRANSPARENT\n" + Gdx.files.internal("data/shaders/deferred/modelbatcher.fragment.glsl").readString();
		}
		
		transparentShaderNoSampling = new ShaderProgram(vert, frag);
		if (!transparentShaderNoSampling.isCompiled()) System.err.println(transparentShaderNoSampling.getLog());
		
		transparentShaderSampling = new ShaderProgram("#define USE_TRIPLANAR_SAMPLING\n"+vert, "#define USE_TRIPLANAR_SAMPLING\n"+frag);
		if (!transparentShaderSampling.isCompiled()) System.err.println(transparentShaderSampling.getLog());
		
		transparentShaderSampling.registerUniformBlock("InstanceBlock", 1);
		transparentShaderNoSampling.registerUniformBlock("InstanceBlock", 1);
	}
	
	public void loadSimpleShader()
	{	
		String vert = "#define MAX_INSTANCES " + MAX_INSTANCES + "\n" + Gdx.files.internal("data/shaders/forward/modelbatcher_simple.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/forward/modelbatcher_simple.fragment.glsl").readString();
		
		simpleShader = new ShaderProgram(vert, frag);
	
		if (!simpleShader.isCompiled()) System.err.println(simpleShader.getLog());
		
		simpleShader.registerUniformBlock("InstanceBlock", 1);
	}
	
	
}