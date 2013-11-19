package com.Lyeeedar.Graphics;

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

public class SkyBox {
	
	private final Texture skyTexture;
	private final Texture seaTexture;
	
	private final ShaderProgram skyShader;
	private final ShaderProgram seaShader;
	
	private final Mesh box;
	private final Mesh sea;
	
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	private final Vector3 seaColour = new Vector3();
	private final float[] amplitudes = new float[8];
	private final float[] wavelengths = new float[8];
	private final float[] speeds = new float[8];
	private final float[] directions = new float[16];
	
	private final Vector3 tmpVec = new Vector3();
	
	private static final float oneThird = 1f/3f;
	private static final float twoThird = 2f/3f;
	
	private float time;
	
	public SkyBox(Texture skyTexture, Texture seaTexture, Vector3 seaColour)
	{
		this.skyTexture = skyTexture;
		this.seaTexture = seaTexture;
		
		this.seaColour.set(seaColour);
				
		amplitudes[0] = 0.3f;
		wavelengths[0] = 1.1f;
		speeds[0] = 1.0f;
		directions[0] = 1.0f; directions[1] = 0.0f;
		
		amplitudes[1] = 1.0f;
		wavelengths[1] = 7.0f;
		speeds[1] = 5.0f;
		directions[2] = 0.0f; directions[3] = 1.0f;
		
		amplitudes[2] = 0.4f;
		wavelengths[2] = 23.0f;
		speeds[2] = 0.1f;
		directions[4] = -1.0f; directions[5] = 1.0f;
		
		box = getSkyBox();
		sea = getSea(500, 250, 500, 250);
		
		skyShader = new ShaderProgram(VERTEX_SHADER_SKY, FRAGMENT_SHADER_SKY);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
		seaShader = new ShaderProgram(
				Gdx.files.internal("data/shaders/sea.vertex.glsl"),
				Gdx.files.internal("data/shaders/sea.fragment.glsl")
				);
		if (!seaShader.isCompiled()) {
			System.err.println(seaShader.getLog());
		}
	}
	
	public void update(float delta)
	{
		time += delta;
		//if (time > Math.PI*2) time = 0;
	}
	
	public void render(Camera cam, Vector3 position)
	{
		seaShader.begin();
		
		mat41.set(cam.combined);
		
		seaShader.setUniformf("delta", time);
		seaShader.setUniformi("numWaves", 3);
		seaShader.setUniform1fv("amplitude", amplitudes, 0, 8);
		seaShader.setUniform1fv("wavelength", wavelengths, 0, 8);
		seaShader.setUniform1fv("speed", speeds, 0, 8);
		seaShader.setUniform2fv("direction", directions, 0, 8);

		seaShader.setUniformf("u_position", position);
		seaShader.setUniformMatrix("u_mvp", mat41);
		
		seaShader.setUniformf("u_colour", seaColour);
		seaShader.setUniformi("u_texture", 0);
		seaTexture.bind(0);
		
		sea.render(seaShader, GL20.GL_TRIANGLES);
		
		seaShader.end();
		
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		
		skyShader.begin();
		
		skyTexture.bind(0);
		mat41.set(cam.combined).mul(mat42.setToTranslation(cam.position));
		skyShader.setUniformMatrix("u_mvp", mat41);
		skyShader.setUniformi("u_texture", 0);
		
		box.render(skyShader, GL20.GL_TRIANGLES);
		
		skyShader.end();
	}
	
	private Mesh getSkyBox()
	{
		int x = 2;
		int y = 2;
		int z = 2;
		float[] cubeVerts = {
				-x, -y, -z, // bottom
				-x, -y, z,
				x, -y, z,
				x, -y, -z,

				-x, y, -z, // top
				-x, y, z,
				x, y, z,
				x, y, -z,

				-x, -y, -z, // back
				-x, y, -z,
				x, y, -z,
				x, -y, -z,

				-x, -y, z, // front
				-x, y, z,
				x, y, z,
				x, -y, z,

				-x, -y, -z, // left
				-x, -y, z,
				-x, y, z,
				-x, y, -z,

				x, -y, -z, // right
				x, -y, z,
				x, y, z,
				x, y, -z};

		float[] cubeTex = {
				
				0.5f, twoThird, // top
				0.5f, 1.0f,
				0.25f, 1.0f,
				0.25f, twoThird,

				0.25f, 0.0f, // bottom
				0.25f, oneThird,
				0.5f, oneThird,
				0.5f, 0.0f,

				1.0f, twoThird, // back
				1.0f, oneThird,
				0.75f, oneThird,
				0.75f, twoThird,

				0.25f, twoThird, // front
				0.25f, oneThird,
				0.5f, oneThird,
				0.5f, twoThird,

				0.0f, twoThird, // left
				0.25f, twoThird,
				0.25f, oneThird,
				0.0f, oneThird,

				0.75f, twoThird, // right
				0.5f, twoThird,
				0.5f, oneThird,
				0.75f, oneThird,

		};

		float[] vertices = new float[24 * 5];
		int pIdx = 0;
		int tIdx = 0;
		for (int i = 0; i < vertices.length;) {
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeVerts[pIdx++];
			vertices[i++] = cubeTex[tIdx++];
			vertices[i++] = cubeTex[tIdx++];
		}

		short[] indices = new short[] {
				0, 2, 1, // bottom
				0, 3, 2,

				4, 5, 6, // top
				4, 6, 7,

				8, 9, 10, // back
				8, 10, 11,

				12, 15, 14, // front
				12, 14, 13,

				16, 17, 18, // left
				16, 18, 19,

				20, 23, 22, // right
				20, 22, 21
		};
		
		Mesh box = new Mesh(true, 24, 36, 
				new VertexAttribute(Usage.Position, 3, "a_position"),
				new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
		
		box.setVertices(vertices);
		box.setIndices(indices);
		
		return box;
	}
	
	private static Mesh getSea(float x, int nx, float z, int nz)
	{
		float dx = x/(float)nx;
		float dz = z/(float)nz;
		
		float[] vertices = new float[nx*nz*3];
		int i = 0;
		
		for (int ix = 0; ix < nx; ix++)
		{
			for (int iz = 0; iz < nz; iz++)
			{
				vertices[i++] = (-x/2f)+(ix*dx);
				vertices[i++] = 0;
				vertices[i++] = (-z/2f)+(iz*dz);
			}
		}
		
		final short[] indices = new short[(nx-1)*(nz-1)*6];
		i = 0;
		for (int ix = 0; ix < nx-1; ix++)
		{
			for (int iz = 0; iz < nz-1; iz++)
			{
				int t1p1 = iz+(ix*nz);
				int t1p2 = iz+1+((ix+1)*nz);
				int t1p3 = iz+((ix+1)*nz);

				indices[i++] = (short) t1p1;
				indices[i++] = (short) t1p2;
				indices[i++] = (short) t1p3;
				
				int t2p1 = iz+(ix*nz);
				int t2p2 = iz+1+(ix*nz);
				int t2p3 = iz+1+((ix+1)*nz);

				indices[i++] = (short) t2p1;
				indices[i++] = (short) t2p2;
				indices[i++] = (short) t2p3;
			}
		}
		
		Mesh mesh = new Mesh(true, nx*nz*3, (nx-1)*(nz-1)*6, 
				new VertexAttribute(Usage.Position, 3, "a_position"));
		
		mesh.setVertices(vertices);
		mesh.setIndices(indices);

		return mesh;
	}
	
	private static final String VERTEX_SHADER_SKY = 
			"attribute vec3 a_position;\n"+
			"attribute vec2 a_texCoord0;\n"+
					
			"uniform mat4 u_mvp;\n"+
			
			"varying vec2 v_texCoords;"+
			
			"void main() {\n"+
			"	v_texCoords = a_texCoord0;\n"+
			"	vec4 position = u_mvp * vec4(a_position, 1.0);\n"+
			"	gl_Position = position.xyww;\n"+
			"}";
	
	private static final String FRAGMENT_SHADER_SKY = 
			"#ifdef GL_ES\n"+
			"	precision mediump float;\n"+
			"#endif\n"+
			
			"uniform sampler2D u_texture;\n"+
			
			"varying vec2 v_texCoords;\n"+
			
			"void main() {\n"+
			"	gl_FragColor.rgb = texture2D(u_texture, v_texCoords).rgb;\n"+
			"	gl_FragColor.a = 1.0;\n"+
			"}";
}
