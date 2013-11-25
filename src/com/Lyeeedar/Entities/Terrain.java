package com.Lyeeedar.Entities;

import java.util.ArrayList;

import com.Lyeeedar.Collision.CollisionShape;
import com.Lyeeedar.Collision.Triangle;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.ImageUtils;
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
	private final Texture texture;
	
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

	public Terrain(Texture texture, float seaFloor, HeightMap[] heightmaps)
	{
		this.texture = texture;
		this.heightmaps = heightmaps;
		
		this.texBuf = new Texture[heightmaps.length];
		this.posBuf = new float[heightmaps.length*3];
		this.heightBuf = new float[heightmaps.length];
		this.scaleBuf = new float[heightmaps.length];
		
		this.seaFloor = seaFloor;
		
		this.terrain = getTerrain(255);
		
		this.shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/terrain.vertex.glsl"),
				Gdx.files.internal("data/shaders/terrain.fragment.glsl")
				);
		if (!shader.isCompiled()) {
			System.err.println(shader.getLog());
		}
	}
	
	public void render(Camera cam, Vector3 position, LightManager lights)
	{
		shader.begin();
		
		mat41.set(cam.combined);
		
		for (int i = 0; i < heightmaps.length; i++) heightmaps[i].fillBuffers(i, texBuf, posBuf, heightBuf, scaleBuf);
		
		shader.setUniformi("u_hm1", 1);
		shader.setUniformi("u_hm2", 2);
		shader.setUniformi("u_hm3", 3);
		for (int i = 0; i < heightmaps.length; i++)
		{
			if (texBuf[i] != null) texBuf[i].bind(i+1);
		}

		shader.setUniformf("u_seaFloor", seaFloor);
		
		shader.setUniform3fv("u_hm_pos", posBuf, 0, heightmaps.length*3);
		shader.setUniform1fv("u_hm_height", heightBuf, 0, heightmaps.length);
		shader.setUniform1fv("u_hm_scale", scaleBuf, 0, heightmaps.length);
		
		shader.setUniformi("u_posx", ((int)(position.x/(float)scale))*scale);
		shader.setUniformi("u_posz", ((int)(position.z/(float)scale))*scale);
		shader.setUniformMatrix("u_mvp", mat41);
		
		shader.setUniformf("u_viewPos", position);
		
		shader.setUniformf("fog_colour", lights.ambientColour);
		shader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		
		shader.setUniformi("u_texture", 0);
		texture.bind(0);
		
		lights.applyLights(shader);
		
		terrain[0].render(shader, GL20.GL_TRIANGLES);
		terrain[1].render(shader, GL20.GL_TRIANGLES);
		terrain[2].render(shader, GL20.GL_TRIANGLES);
		terrain[3].render(shader, GL20.GL_TRIANGLES);
		
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
//				height = seaFloor+(hm.heights[(int) (tmpVec.x*hm.heights.length)][(int) (tmpVec.z*hm.heights[0].length)]*hm.height);
//				break;
//			}
//		}
//		
//		Pools.free(tmpVec);
//		
//		return height;
//	}
	
	public boolean collide(CollisionShape<?> collide)
	{
		boolean hit = false;
		
		for (HeightMap hm : heightmaps)
		{
			tmpVec.set(collide.getPosition().x, 0, collide.getPosition().z).sub(hm.position).scl(1.0f/hm.scale);
			if (tmpVec.x > 0 && tmpVec.x < 1.0f &&
					tmpVec.z > 0 && tmpVec.z < 1.0f)
			{
				hit = hm.collide(collide, (int) ((tmpVec.x*hm.width)+0.5f), (int) ((tmpVec.z*hm.height)+0.5f));
				break;
			}
		}
		
		return hit;
	}
	
	private static Mesh[] getTerrain(int size)
	{	
		Mesh mesh[] = new Mesh[4];
		float[] vertices = new float[size*size*3];
		int i = 0;
		
		final short[] indices = new short[(size-1)*(size-1)*6];
		
		i = 0;
		for (int ix = 0; ix < size-1; ix++)
		{
			for (int iz = 0; iz < size-1; iz++)
			{
				short start = (short) (ix+(iz*size));

				indices[i++] = (short) (start);
				indices[i++] = (short) (start+1);
				indices[i++] = (short) (start+size);
				
				indices[i++] = (short) (start+1);
				indices[i++] = (short) (start+1+size);
				indices[i++] = (short) (start+size);
			
			}
		}
		
		i = 0;
		for (int ix = 0; ix < size; ix++)
		{
			for (int iz = 0; iz < size; iz++)
			{
				vertices[i++] = (ix*scale);
				vertices[i++] = 0;
				vertices[i++] = (iz*scale);
			}
		}
		mesh[0] = new Mesh(true, size*size, indices.length, 
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		mesh[0].setVertices(vertices);
		mesh[0].setIndices(indices);
		
		i = 0;
		for (int ix = 0; ix < size; ix++)
		{
			for (int iz = 0; iz < size; iz++)
			{
				vertices[i++] = (-size*scale)+(ix*scale)+scale;
				vertices[i++] = 0;
				vertices[i++] = (iz*scale);
			}
		}
		mesh[1] = new Mesh(true, size*size, indices.length, 
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		mesh[1].setVertices(vertices);
		mesh[1].setIndices(indices);
		
		i = 0;
		for (int ix = 0; ix < size; ix++)
		{
			for (int iz = 0; iz < size; iz++)
			{
				vertices[i++] = (ix*scale);
				vertices[i++] = 0;
				vertices[i++] = (-size*scale)+(iz*scale)+scale;
			}
		}
		mesh[2] = new Mesh(true, size*size, indices.length, 
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		mesh[2].setVertices(vertices);
		mesh[2].setIndices(indices);
		
		i = 0;
		for (int ix = 0; ix < size; ix++)
		{
			for (int iz = 0; iz < size; iz++)
			{
				vertices[i++] = (-size*scale)+(ix*scale)+scale;
				vertices[i++] = 0;
				vertices[i++] = (-size*scale)+(iz*scale)+scale;
			}
		}
		mesh[3] = new Mesh(true, size*size, indices.length, 
				new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
		mesh[3].setVertices(vertices);
		mesh[3].setIndices(indices);

		return mesh;
	}
	
	public static class HeightMap
	{
		Texture texture;
		Vector3 position;
		float range;
		float scale;
		int width;
		int height;
		float[][] heights;
		
		Triangle[] triangles = {new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle(), new Triangle()};
		
		@SuppressWarnings("unchecked")
		public HeightMap(Texture texture, Vector3 position, float range, float scale, float seaFloor)
		{
			this.texture = texture;
			this.position = position;
			this.range = range;
			this.scale = scale;
			this.width = texture.getWidth()/Terrain.scale;
			this.height = texture.getHeight()/Terrain.scale;
			
			Pixmap pm = ImageUtils.TextureToPixmap(texture);
			heights = new float[width][height];
			
			Color c = new Color();
			for (int x = 0; x < width; x++)
			{
				for (int z = 0; z < height; z++)
				{
					Color.rgba8888ToColor(c, pm.getPixel(x*Terrain.scale, z*Terrain.scale));
					heights[x][z] = seaFloor+((c.r+c.g+c.b)/3.0f)*range;
				}
			}
		}
		
		public void fillBuffers(int index, Texture[] texBuf,  float[] posBuf, float[] heightBuf, float[] scaleBuf)
		{
			texBuf[index] = texture;
			
			posBuf[(index*3)+0] = position.x;
			posBuf[(index*3)+1] = position.y;
			posBuf[(index*3)+2] = position.z;
			
			heightBuf[index] = range;
			
			scaleBuf[index] = scale;
		}
		
		private static final int[][] locations = {
			{0, 0},
			{0, 1},
			{1, 0},
			{1, 1},
			{0, -1},
			{-1, 0},
			{-1, -1},
			{-1, 1},
			{1, -1},
		};
		
		public boolean collide(CollisionShape<?> shape, int x, int y)
		{
			boolean collide = false;
			
			if (x < 2) x = 2;
			if (y < 2) y = 2;
			if (x > width-3) x = width-3;
			if (y > height-3) y = height-3;

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
						(((float)x+(float)offsets[i][0])/(float)width)*scale, heights[x+offsets[i][0]][y+offsets[i][1]],
						(((float)y+(float)offsets[i][1])/(float)height)*scale,
						
						(((float)x+(float)offsets[i][2])/(float)width)*scale, heights[x+offsets[i][2]][y+offsets[i][3]],
						(((float)y+(float)offsets[i][3])/(float)height)*scale,
						
						(((float)x+(float)offsets[i][4])/(float)width)*scale, heights[x+offsets[i][4]][y+offsets[i][5]],
						(((float)y+(float)offsets[i][5])/(float)height)*scale
						);
			}
		}
	}

}
