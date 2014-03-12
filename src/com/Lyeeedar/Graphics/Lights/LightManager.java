package com.Lyeeedar.Graphics.Lights;

import java.util.HashMap;

import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeFrustum;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.AnimatedModelBatch;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Graphics.Batchers.ChunkedTerrainBatch;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Batchers.TexturedMeshBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.utils.Array;

public class LightManager {
	
	private static final int MAX_LIGHTS = 4;
	private static final int PRIORITY_STEPS = 512;
	
	public final Vector3 ambientColour = new Vector3();	
	public final Light directionalLight = new Light(new Vector3(), new Vector3(), new Vector3());
	public final Array<Light> lights = new Array<Light>();
	
	private boolean needsSort = true;
	private final Vector3 lastSortPosition = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	
	private final float[] positions = new float[3*MAX_LIGHTS];
	private final float[] colours = new float[3*MAX_LIGHTS];
	private final float[] attenuations = new float[MAX_LIGHTS];
	
	private final Vector3 tmpVec = new Vector3();
	
	public FrameBuffer frameBuffer;
	public Camera orthoCam;
	public OcttreeFrustum oFrustum;
	public Array<Entity> shadowEntities;
	public HashMap<Class, Batch> batches;
	public Texture depthTexture;
	public Matrix4 biasMatrix;
	public Matrix4 depthBiasMVP;
	public boolean shadowMap = false;
	public BoundingBox bb;
	
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
	
	public void enableShadowMapping()
	{
		frameBuffer = new FrameBuffer(Format.DEPTH, GLOBALS.RESOLUTION[0]*8, GLOBALS.RESOLUTION[1]*8, false, true);
		orthoCam = new OrthographicCamera();
		orthoCam.viewportWidth = GLOBALS.RESOLUTION[0];
		orthoCam.viewportHeight = GLOBALS.RESOLUTION[1];
		oFrustum = new OcttreeFrustum(orthoCam, -1);
		shadowEntities = new Array<Entity>(false, 16);
		batches = new HashMap<Class, Batch>();
		bb = new BoundingBox();
		
		TexturedMeshBatch renderer = new TexturedMeshBatch(true);
		AnimatedModelBatch modelBatch = new AnimatedModelBatch(12, true);
		ChunkedTerrainBatch terrainBatch = new ChunkedTerrainBatch(true);
		
		batches.put(TexturedMeshBatch.class, renderer);
		batches.put(AnimatedModelBatch.class, modelBatch);
		batches.put(ChunkedTerrainBatch.class, terrainBatch);
		batches.put(ModelBatcher.class, new ModelBatcher(false));
		
		biasMatrix = new Matrix4().scl(0.5f).translate(1, 1, 1);

		depthBiasMVP = new Matrix4();
		
		shadowMap = true;
	}
	
	public void calculateDepthMap(boolean update, Camera cam)
	{
		if (update) 
		{
			bb.min.set(Float.NaN, Float.NaN, Float.NaN);
			bb.max.set(Float.NaN, Float.NaN, Float.NaN);
			
			shadowEntities.clear();
			GLOBALS.renderTree.collectAll(shadowEntities, oFrustum, Octtree.MASK_SHADOW_CASTING, bb);
			bb.set(bb.min, bb.max);
			
			Vector3 dimensions = bb.getDimensions();
			Vector3 center = cam.position;//bb.getCenter();
			
			float radius = dimensions.x;
			radius = Math.max(radius, dimensions.y);
			radius = Math.max(radius, dimensions.z);
			radius = 500;
			
			orthoCam.far = radius*2.0f;
									
			orthoCam.position.set(directionalLight.position).sub(center).nor().scl(radius).add(center);
			orthoCam.direction.set(directionalLight.direction).scl(-1);
			orthoCam.up.set(orthoCam.direction).crs(GLOBALS.DEFAULT_UP).crs(orthoCam.direction);
			orthoCam.near = 0;
			orthoCam.update();
		}
		
		for (Entity e : shadowEntities)
		{
			e.queueRenderables(orthoCam, this, 0, batches, false);
		}
		
		frameBuffer.begin();
		
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LESS);
		Gdx.gl.glDepthMask(true);
		
		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(orthoCam, GL20.GL_TRIANGLES, Color.WHITE);
		((ModelBatcher) batches.get(ModelBatcher.class)).renderSolid(this, orthoCam);
		((ModelBatcher) batches.get(ModelBatcher.class)).clear();
		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(orthoCam, GL20.GL_TRIANGLES, Color.WHITE);
		((ChunkedTerrainBatch) batches.get(ChunkedTerrainBatch.class)).render(this, orthoCam);
		
		frameBuffer.end();
		
		depthTexture = frameBuffer.getDepthBufferTexture();
		
		depthBiasMVP.set(biasMatrix).mul(orthoCam.combined);
	}
	
	public void sort(Vector3 position)
	{
		if (needsSort || !position.epsilonEquals(lastSortPosition, 1))
		{
			for (Light l : lights)
			{
				float distance = position.dst2(l.position);
				l.distance = PRIORITY_STEPS- (int)(PRIORITY_STEPS * (1.0f / (l.attenuation*distance)));
			}
			lights.sort();
			
			directionalLight.position.set(directionalLight.direction).scl(GLOBALS.FOG_MAX).add(position);
			
			positions[0] = directionalLight.position.x;
			positions[1] = directionalLight.position.y;
			positions[2] = directionalLight.position.z;
			
			colours[0] = directionalLight.colour.x;
			colours[1] = directionalLight.colour.y;
			colours[2] = directionalLight.colour.z;
			
			attenuations[0] = 0;
			
			int i = 1;
			int num_lights = (MAX_LIGHTS > lights.size+1) ? lights.size+1 : MAX_LIGHTS;
			for (; i < num_lights; i++)
			{
				Light light = lights.get(i-1);
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
	
	public void applyLights(ShaderProgram shader, int allowedTexCoord)
	{
		shader.setUniformf("u_al_col", ambientColour);
		
		shader.setUniform3fv("u_pl_pos", positions, 0, 3*MAX_LIGHTS);
		shader.setUniform3fv("u_pl_col", colours, 0, 3*MAX_LIGHTS);
		shader.setUniform1fv("u_pl_att", attenuations, 0, MAX_LIGHTS);
		
		if (shadowMap && depthTexture != null)
		{
			shader.setUniformMatrix("u_depthBiasMVP", depthBiasMVP);
			depthTexture.bind(allowedTexCoord);
			shader.setUniformi("u_shadowMapTexture", allowedTexCoord);
			shader.setUniformf("u_poisson_scale", orthoCam.far*15);
		}
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
