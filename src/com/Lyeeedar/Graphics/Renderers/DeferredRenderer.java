package com.Lyeeedar.Graphics.Renderers;

import java.util.HashMap;

import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ChunkedTerrainBatch;
import com.Lyeeedar.Graphics.Batchers.DecalBatcher;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Batchers.MotionTrailBatch;
import com.Lyeeedar.Graphics.Batchers.ParticleEffectBatch;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor;
import com.Lyeeedar.Graphics.PostProcessing.PostProcessor.Effect;
import com.Lyeeedar.Graphics.Queueables.Queueable.RenderType;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.DiscardCameraGroupStrategy;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.Lyeeedar.Util.ImageUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class DeferredRenderer implements Renderer
{
	protected final SpriteBatch sbatch;
	
	protected final FollowCam cam;	
	protected final HashMap<Class, Batch> batches;
	
	protected final FrameBuffer gbuffer;
	protected final FrameBuffer lbuffer;
	
	protected ShaderProgram ssaoShader;
	
	protected final Texture blank;
	protected Matrix4 tmpMat = new Matrix4();

	public DeferredRenderer(FollowCam cam)
	{
		this.cam = cam;
		
		batches = new HashMap<Class, Batch>();
		batches.put(TexturedMeshBatch.class, new TexturedMeshBatch(RenderType.DEFERRED));
		batches.put(AnimatedModelBatch.class, new AnimatedModelBatch(12, RenderType.DEFERRED));
		batches.put(DecalBatcher.class, new DecalBatcher(new DecalBatch(new DiscardCameraGroupStrategy(cam))));
		batches.put(ModelBatcher.class, new ModelBatcher(RenderType.DEFERRED));
		batches.put(MotionTrailBatch.class, new MotionTrailBatch());
		batches.put(ParticleEffectBatch.class, new ParticleEffectBatch());
		batches.put(ChunkedTerrainBatch.class, new ChunkedTerrainBatch(RenderType.DEFERRED));
		
		gbuffer = new FrameBuffer(Format.RGBA8888, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], 4, true);
		lbuffer = new FrameBuffer(Format.RGBA8888, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], 1, true);
		
		sbatch = new SpriteBatch();
		
		setupSSAO();
		
		blank = FileUtils.loadTexture("data/textures/blank.png", true, null, null);
	}
	
	@Override
	public void render()
	{
		// Draw G Buffers
		gbuffer.begin();
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(GLOBALS.LIGHTS, cam);
		((ModelBatcher) batches.get(ModelBatcher.class)).renderSolid(GLOBALS.LIGHTS, cam);
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(GLOBALS.LIGHTS, cam);
		((ChunkedTerrainBatch) batches.get(ChunkedTerrainBatch.class)).render(GLOBALS.LIGHTS, cam);
		
		gbuffer.end();
		
		Texture depth = gbuffer.getDepthBufferTexture();
		Texture albedo = gbuffer.getColorBufferTexture(0);
		Texture normals = gbuffer.getColorBufferTexture(1);
		Texture specular = gbuffer.getColorBufferTexture(2);
		Texture emissive = gbuffer.getColorBufferTexture(3);
		
		lbuffer.begin();
		
		Gdx.gl.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDepthMask(false);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		// Do SSAO + ambient 
		sbatch.setShader(ssaoShader);
		sbatch.setColor(GLOBALS.LIGHTS.ambientColour.x, GLOBALS.LIGHTS.ambientColour.y, GLOBALS.LIGHTS.ambientColour.z, 1);
		sbatch.disableBlending();
		sbatch.begin();
		
		depth.bind(1);
		ssaoShader.setUniformi("u_depth", 1);	
		normals.bind(2);
		ssaoShader.setUniformi("u_normal", 2);
		
		ssaoShader.setUniformMatrix("u_invProj", cam.invProjectionView);		
		ssaoShader.setUniformf("distanceThreshold", 5);
		ssaoShader.setUniformf("filterRadius", 10.0f/cam.viewportWidth, 10.0f/cam.viewportHeight);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		sbatch.draw(blank, 0, 0, lbuffer.getWidth(), lbuffer.getHeight(), 0, 0, blank.getWidth(), blank.getHeight(), false, true);
		sbatch.end();
		
		// Do Lightting
		
		lbuffer.end();
		
		Texture lighting = lbuffer.getColorBufferTexture();
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		sbatch.setColor(1, 1, 1, 1);
		sbatch.setShader(null);
		sbatch.disableBlending();
		sbatch.begin();
		
		sbatch.draw(albedo, 0, 0, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0, 0, albedo.getWidth(), albedo.getHeight(), false, true);
		sbatch.draw(depth, Gdx.graphics.getWidth()/2, 0, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0, 0, depth.getWidth(), depth.getHeight(), false, true);
		sbatch.draw(normals, 0, Gdx.graphics.getHeight()/2, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0, 0, normals.getWidth(), normals.getHeight(), false, true);
		sbatch.draw(lighting, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0, 0, lighting.getWidth(), lighting.getHeight(), false, true);
		
		sbatch.end();
		
//		// Compose  image
//		sbatch.setColor(1, 1, 1, 1);
//		sbatch.enableBlending();
//		sbatch.setShader(null);
//		
//		sbatch.begin();
//		
//		sbatch.setBlendFunction(GL30.GL_ONE, GL30.GL_ONE);
//		sbatch.draw(albedo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, albedo.getWidth(), albedo.getHeight(), false, true);
//		sbatch.setBlendFunction(GL30.GL_ZERO, GL30.GL_SRC_COLOR);
//		sbatch.draw(lighting, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, lighting.getWidth(), lighting.getHeight(), false, true);
//		sbatch.setBlendFunction(GL30.GL_ONE, GL30.GL_ONE);
//		sbatch.draw(emissive, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, emissive.getWidth(), emissive.getHeight(), false, true);
//		sbatch.end();
//		
//		// Draw transparent
//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_LESS);
//		Gdx.gl.glDepthMask(true);
//		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
//		
//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_LESS);
//		Gdx.gl.glDepthMask(true);
//		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//		Gdx.gl.glCullFace(GL20.GL_BACK);
//		GLOBALS.SKYBOX.sea.render(cam, cam.position, GLOBALS.LIGHTS);
//		
//		((ModelBatcher) batches.get(ModelBatcher.class)).renderTransparent(GLOBALS.LIGHTS, cam);
//		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//		
//		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//		Gdx.gl.glDepthFunc(GL20.GL_LESS);
//		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
//		Gdx.gl.glDepthMask(false);
//		((DecalBatcher) batches.get(DecalBatcher.class)).flush();
//		
//		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_LESS);
//		Gdx.gl.glDepthMask(false);
//		((MotionTrailBatch) batches.get(MotionTrailBatch.class)).flush(cam);
//		((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).render(cam);
	}
	
	private void setupSSAO()
	{		
		String vert = Gdx.files.internal("data/shaders/deferred/ssao.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/shaders/deferred/ssao.fragment.glsl").readString();
		ssaoShader = new ShaderProgram(vert, frag);
		if (!ssaoShader.isCompiled()) System.err.println(ssaoShader.getLog());
	}

	@Override
	public HashMap<Class, Batch> getBatches()
	{
		return batches;
	}

	@Override
	public void resize(int width, int height)
	{
		// TODO Auto-generated method stub
		
	}

}
