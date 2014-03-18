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
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class ForwardRenderer implements Renderer
{
	protected final PostProcessor postprocessor;
	protected final FollowCam cam;
	
	protected final HashMap<Class, Batch> batches;
	
	private long startTime;
	private long time;
	private long frameTime;
	private long averageFrame;
	private long averageUpdate;
	private long averageQueue;
	private long averageModel;
	//private long averageTrail;
	private long averageDecal;
	private long averageOrthogonal;
	private long averageParticles;
	private long averagePost;
	protected int particleNum;
	
	public ForwardRenderer(FollowCam cam)
	{
		this.cam = cam;
		
		batches = new HashMap<Class, Batch>();
		batches.put(TexturedMeshBatch.class, new TexturedMeshBatch(RenderType.FORWARD));
		batches.put(AnimatedModelBatch.class, new AnimatedModelBatch(12, RenderType.FORWARD));
		batches.put(DecalBatcher.class, new DecalBatcher(new DecalBatch(new DiscardCameraGroupStrategy(cam))));
		batches.put(ModelBatcher.class, new ModelBatcher(RenderType.FORWARD));
		batches.put(MotionTrailBatch.class, new MotionTrailBatch());
		batches.put(ParticleEffectBatch.class, new ParticleEffectBatch());
		batches.put(ChunkedTerrainBatch.class, new ChunkedTerrainBatch(RenderType.FORWARD));
		
		postprocessor = new PostProcessor(Format.RGBA8888, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1], cam);
		postprocessor.addEffect(Effect.SSAO);
	}
	
	@Override
	public void render()
	{
		postprocessor.begin();
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		//Gdx.gl.glEnable(GL20.GL_BLEND);
		//Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		time = System.nanoTime();
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(GLOBALS.LIGHTS, cam);
		((ModelBatcher) batches.get(ModelBatcher.class)).renderSolid(GLOBALS.LIGHTS, cam);
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(GLOBALS.LIGHTS, cam);
		((ChunkedTerrainBatch) batches.get(ChunkedTerrainBatch.class)).render(GLOBALS.LIGHTS, cam);
		GLOBALS.physicsWorld.render((PerspectiveCamera) cam);
		GLOBALS.lineRenderer.render(cam);
		averageModel += System.nanoTime()-time;
		averageModel /= 2;
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		GLOBALS.SKYBOX.weather.render(cam, GLOBALS.LIGHTS);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		GLOBALS.SKYBOX.sea.render(cam, cam.position, GLOBALS.LIGHTS);
		
		((ModelBatcher) batches.get(ModelBatcher.class)).renderTransparent(GLOBALS.LIGHTS, cam);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
		Gdx.gl.glDepthMask(false);
		((DecalBatcher) batches.get(DecalBatcher.class)).flush();
		averageDecal += System.nanoTime()-time;
		averageDecal /= 2;
		
		time = System.nanoTime();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(false);
		((MotionTrailBatch) batches.get(MotionTrailBatch.class)).flush(cam);
		((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).render(cam);
		this.particleNum = ((ParticleEffectBatch) batches.get(ParticleEffectBatch.class)).particleNum;
		averageParticles += System.nanoTime()-time;
		averageParticles /= 2;
		
		Gdx.gl20.glBlendEquation(GL20.GL_FUNC_ADD);
		
		time = System.nanoTime();
		postprocessor.end();
		averagePost += System.nanoTime()-time;
		averagePost /= 2;
	}

	@Override
	public HashMap<Class, Batch> getBatches()
	{
		return batches;
	}

	@Override
	public void resize(int width, int height)
	{
		postprocessor.updateBufferSettings(Format.RGBA8888, width, height);
	}

}
