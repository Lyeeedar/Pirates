package com.Lyeeedar.Entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.Lyeeedar.Collision.Box;
import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Collision.Triangle;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Model;
import com.Lyeeedar.Graphics.Renderable;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.ImageUtils;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class Terrain extends Entity {
	
	private final ShaderProgram shader;
	private final Texture[] textures;
	
	public final float seaFloor;
	
	private final Mesh terrain[];
	
	private final HeightMap[] heightmaps;
	
	private final Texture[] texBuf;
	private final float[] posBuf;
	private final float[] heightBuf;
	private final float[] scaleBuf;
	
	private static final int scale = 10;
	
	private final Vector3 tmpVec = new Vector3();
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();

	public Terrain(Texture[] textures, float seaFloor, HeightMap[] heightmaps)
	{
		super(new PositionalData());
		
		this.textures = textures;
		this.heightmaps = heightmaps;
		
		this.texBuf = new Texture[heightmaps.length];
		this.posBuf = new float[heightmaps.length*3];
		this.heightBuf = new float[heightmaps.length];
		this.scaleBuf = new float[heightmaps.length];
		
		this.seaFloor = seaFloor;
		
		this.terrain = Shapes.getArea(255, scale);
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/terrain.vertex.glsl"),
				Gdx.files.internal("data/shaders/terrain.fragment.glsl")
				);
		if (!shader.isCompiled()) {
			System.err.println(shader.getLog());
		}
		
		this.setCollisionShapeInternal(new Box(new Vector3(), 100000, 1000, 100000));
	}
	
	public void render(Camera cam, Vector3 position, LightManager lights)
	{
		this.getCollisionShapeInternal().setPosition(cam.position);
		
		shader.begin();
		
		mat41.set(cam.combined);
		
		for (int i = 0; i < heightmaps.length; i++) heightmaps[i].fillBuffers(i, texBuf, posBuf, heightBuf, scaleBuf);
		
		
		for (int i = 0; i < heightmaps.length; i++)
		{
			if (texBuf[i] != null) 
			{
				shader.setUniformi("u_hm"+(i+1), i);
				texBuf[i].bind(i);
			}
		}

		shader.setUniformf("u_seaFloor", seaFloor);
		
		shader.setUniform3fv("u_hm_pos", posBuf, 0, heightmaps.length*3);
		shader.setUniform1fv("u_hm_height", heightBuf, 0, heightmaps.length);
		shader.setUniform1fv("u_hm_scale", scaleBuf, 0, heightmaps.length);
		
		shader.setUniformi("u_posx", ((int)(position.x/(float)scale))*scale);
		shader.setUniformi("u_posz", ((int)(position.z/(float)scale))*scale);
		shader.setUniformMatrix("u_mvp", mat41);
		
		shader.setUniformf("u_viewPos", position);
		
		shader.setUniformf("fog_col", lights.ambientColour);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		
		for (int i = 0; i < textures.length; i++)
		{
			shader.setUniformi("u_texture"+(i+1), heightmaps.length+i);
			textures[i].bind(heightmaps.length+i);
		}
		
		lights.applyLights(shader);
		
		for (Mesh t : terrain)
			t.render(shader, GL20.GL_TRIANGLES);

		shader.end();
	}
	
//	public float getHeight(float x, float z)
//	{
//		float height = seaFloor;
//	
//		Vector3 tmpVec = Pools.obtain(Vector3.class);
//		
//		for (HeightMap hm : heightmaps)
//		{
//			tmpVec.set(x, 0, z).sub(hm.position).scl(1.0f/hm.scale);
//			if (tmpVec.x > 0 && tmpVec.x < 1.0f &&
//					tmpVec.z > 0 && tmpVec.z < 1.0f)
//			{
//				height = seaFloor+(hm.heights[(int) (tmpVec.x*hm.heights.length)][(int) (tmpVec.z*hm.heights[0].length)]);
//				break;
//			}
//		}
//		
//		Pools.free(tmpVec);
//		
//		return height;
//	}
	
	public void vegetate(List<Entity> entities, Renderable renderable, int splat, int num, int maxTries)
	{
		HeightMap hm = heightmaps[0];
		
		MinimalPositionalData pData = Pools.obtain(MinimalPositionalData.class);
		Random ran = new Random();
		for (int i = 0; i < num; i++)
		{
			Entity v = new Entity(new MinimalPositionalData());
			
			v.readData(pData, MinimalPositionalData.class);
			
			for (int rep = 0; rep < maxTries; rep++)
			{
				pData.position.x = hm.position.x+ran.nextInt(hm.scale);
				pData.position.z = hm.position.z+ran.nextInt(hm.scale);
				tmpVec.set(pData.position.x, 0, pData.position.z).sub(hm.position).scl(1.0f/hm.scale);
				
				int x = (int) (tmpVec.x*hm.size);
				int z = (int) (tmpVec.z*hm.size);
				
				if (hm.getSplat(x, z) == splat)
				{
					pData.position.y = hm.heights[x][z];
					break;
				}
			}
			
			v.writeData(pData, MinimalPositionalData.class);
			
			v.addRenderable(renderable.copy());
			
			entities.add(v);
		}
		Pools.free(pData);
	}
	
	public boolean collide(CollisionShape<?> collide)
	{
		boolean hit = false;
		
		for (HeightMap hm : heightmaps)
		{
			tmpVec.set(collide.getPosition().x, 0, collide.getPosition().z).sub(hm.position).scl(1.0f/hm.scale);
			if (tmpVec.x > 0 && tmpVec.x < 1.0f &&
					tmpVec.z > 0 && tmpVec.z < 1.0f)
			{
				hit = hm.collide(collide, (int) ((tmpVec.x*hm.size)), (int) ((tmpVec.z*hm.size)));
				break;
			}
		}
		
		return hit;
	}

	public static class HeightMap
	{
		Texture texture;
		Vector3 position;
		float range;
		int scale;
		int size;
		float[][] heights;
		byte[][] splats;
		
		Triangle[] triangles = {new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle()};
		
		@SuppressWarnings("unchecked")
		public HeightMap(Texture texture, Vector3 position, float range, int scale, float seaFloor)
		{
			this.texture = texture;
			this.position = position;
			this.range = range;
			this.scale = scale;
			this.size = scale / Terrain.scale;
			
			Pixmap pm = ImageUtils.TextureToPixmap(texture);
			heights = new float[size][size];
			splats = new byte[size][size];
			
			Color c = new Color();
			for (int x = 0; x < size; x++)
			{
				for (int z = 0; z < size; z++)
				{
					ImageUtils.sampleColour(pm, c, ((x*Terrain.scale)/(float)scale)*(float)pm.getWidth(), ((z*Terrain.scale)/(float)scale)*(float)pm.getHeight());
					//Color.rgba8888ToColor(c, pm.getPixel((int)(((x*Terrain.scale)/(float)scale)*(float)pm.getWidth()), (int)(((z*Terrain.scale)/(float)scale)*(float)pm.getHeight())));
					heights[x][z] = seaFloor+c.a*range;
					
					splats[x][z] = 0;
					
					if (c.r != 0.0f) splats[x][z] = 1;
					if (c.g != 0.0f) splats[x][z] = 2;
					if (c.b != 0.0f) splats[x][z] = 3;
				}
			}
		}
		
		public byte getSplat(int x, int z)
		{
			return splats[x][z];
		}
		
		public void fillBuffers(int index, Texture[] texBuf,  float[] posBuf, float[] heightBuf, float[] scaleBuf)
		{
			texBuf[index] = texture;
			
			posBuf[(index*3)+0] = position.x;
			posBuf[(index*3)+1] = position.y;
			posBuf[(index*3)+2] = position.z;
			
			heightBuf[index] = range;
			
			scaleBuf[index] = (float)scale;
		}
		
		private static final int[][] locations = {
			{0, 0},
//			{0, 1},
//			{1, 0},
//			{1, 1},
//			{0, -1},
//			{-1, 0},
//			{-1, -1},
//			{-1, 1},
//			{1, -1},
		};
		
		public boolean collide(CollisionShape<?> shape, int x, int y)
		{
			boolean collide = false;
			
			if (x < 2) x = 2;
			if (y < 2) y = 2;
			if (x > size-3) x = size-3;
			if (y > size-3) y = size-3;

			for (int[] loc : locations)
			{
				fillTriangles(x+loc[0], y+loc[1]);
				for (Triangle t : triangles)
				{
					if (t.collide(shape)) collide = true;
				}
			}
			
			return collide;
		}
		
		private static final int[][] offsets = {
			{0, 0, 		1, 0, 		1, 1},
			{0, 0, 		0, 1, 		1, 1},
			{0, 0, 		-1, 0, 		-1, -1},
			{0, 0, 		0, -1, 		-1, -1},
			{0, 0, 		-1, 0, 		-1, 1},
			{0, 0, 		0, -1, 		1, -1},
			{0, 0, 		1, 0, 		1, -1},
			{0, 0, 		0, 1, 		-1, 1},
			};
		
		private void fillTriangles(int x, int y)
		{
			for (int i = 0; i < offsets.length; i++)
			{
				triangles[i].set(
						(((float)x+(float)offsets[i][0])/(float)size)*scale, heights[x+offsets[i][0]][y+offsets[i][1]],
						(((float)y+(float)offsets[i][1])/(float)size)*scale,
						
						(((float)x+(float)offsets[i][2])/(float)size)*scale, heights[x+offsets[i][2]][y+offsets[i][3]],
						(((float)y+(float)offsets[i][3])/(float)size)*scale,
						
						(((float)x+(float)offsets[i][4])/(float)size)*scale, heights[x+offsets[i][4]][y+offsets[i][5]],
						(((float)y+(float)offsets[i][5])/(float)size)*scale
						);
			}
		}
	}

}
