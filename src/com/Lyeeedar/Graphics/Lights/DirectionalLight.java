package com.Lyeeedar.Graphics.Lights;

import com.Lyeeedar.Collision.Octtree;
import com.badlogic.gdx.math.Vector3;

public class DirectionalLight extends Light
{
	public Vector3 direction = new Vector3();
	public DirectionalLight(Vector3 direction, Vector3 colour)
	{
		super(colour);
		
		this.direction.set(direction);
	}

	@Override
	public void createEntry(Octtree<Light> octtree)
	{
		this.entry = octtree.createEntry(this, new Vector3(), new Vector3().set(octtree.max).sub(octtree.min), Octtree.MASK_DIRECTION_LIGHT);
	}

	@Override
	public void render()
	{
		// TODO Auto-generated method stub
		
	}
	
//	public FrameBuffer frameBuffer;
//	public Camera orthoCam;
//	public OcttreeFrustum oFrustum;
//	public Array<Entity> shadowEntities;
//	public HashMap<Class, Batch> batches;
//	public Texture depthTexture;
//	public Matrix4 biasMatrix;
//	public Matrix4 depthBiasMVP;
//	public boolean shadowMap = false;
//	public BoundingBox bb;
//	public void enableShadowMapping()
//	{
//		frameBuffer = new FrameBuffer(Format.DEPTH, GLOBALS.RESOLUTION[0]*8, GLOBALS.RESOLUTION[1]*8, 0, true);
//		orthoCam = new OrthographicCamera();
//		orthoCam.viewportWidth = GLOBALS.RESOLUTION[0];
//		orthoCam.viewportHeight = GLOBALS.RESOLUTION[1];
//		oFrustum = new OcttreeFrustum(orthoCam, -1);
//		shadowEntities = new Array<Entity>(false, 16);
//		batches = new HashMap<Class, Batch>();
//		bb = new BoundingBox();
//		
//		batches.put(TexturedMeshBatch.class, new TexturedMeshBatch(RenderType.SIMPLE));
//		batches.put(AnimatedModelBatch.class, new AnimatedModelBatch(12, RenderType.SIMPLE));
//		batches.put(ChunkedTerrainBatch.class, new ChunkedTerrainBatch(RenderType.SIMPLE));
//		batches.put(ModelBatcher.class, new ModelBatcher(RenderType.SIMPLE));
//		
//		biasMatrix = new Matrix4().scl(0.5f).translate(1, 1, 1);
//
//		depthBiasMVP = new Matrix4();
//		
//		shadowMap = true;
//	}
//	
//	public void calculateDepthMap(boolean update, Camera cam)
//	{
//		if (update) 
//		{
//			bb.min.set(Float.NaN, Float.NaN, Float.NaN);
//			bb.max.set(Float.NaN, Float.NaN, Float.NaN);
//			
//			shadowEntities.clear();
//			GLOBALS.renderTree.collectAll(shadowEntities, oFrustum, Octtree.MASK_SHADOW_CASTING, bb);
//			bb.set(bb.min, bb.max);
//			
//			Vector3 dimensions = bb.getDimensions();
//			Vector3 center = cam.position;//bb.getCenter();
//			
//			float radius = dimensions.x;
//			radius = Math.max(radius, dimensions.y);
//			radius = Math.max(radius, dimensions.z);
//			radius = 500;
//			
//			orthoCam.far = radius*2.0f;
//									
//			orthoCam.position.set(directionalLight.position).sub(center).nor().scl(radius).add(center);
//			orthoCam.direction.set(directionalLight.direction).scl(-1);
//			orthoCam.up.set(orthoCam.direction).crs(GLOBALS.DEFAULT_UP).crs(orthoCam.direction);
//			orthoCam.near = 0;
//			orthoCam.update();
//		}
//		
//		for (Entity e : shadowEntities)
//		{
//			e.queueRenderables(orthoCam, this, 0, batches, false);
//		}
//		
//		frameBuffer.begin();
//		
//		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
//		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
//		Gdx.gl.glCullFace(GL20.GL_FRONT);
//		
//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_LESS);
//		Gdx.gl.glDepthMask(true);
//		
//		((TexturedMeshBatch) batches.get(TexturedMeshBatch.class)).render(orthoCam, GL20.GL_TRIANGLES, Color.WHITE);
//		((ModelBatcher) batches.get(ModelBatcher.class)).renderSolid(this, orthoCam);
//		((ModelBatcher) batches.get(ModelBatcher.class)).clear();
//		((AnimatedModelBatch) batches.get(AnimatedModelBatch.class)).render(orthoCam, GL20.GL_TRIANGLES, Color.WHITE);
//		((ChunkedTerrainBatch) batches.get(ChunkedTerrainBatch.class)).render(this, orthoCam);
//		
//		frameBuffer.end();
//		
//		depthTexture = frameBuffer.getDepthBufferTexture();
//		
//		depthBiasMVP.set(biasMatrix).mul(orthoCam.combined);
//	}

}
