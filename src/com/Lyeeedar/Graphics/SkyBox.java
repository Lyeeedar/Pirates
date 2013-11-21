package com.Lyeeedar.Graphics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
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
	
	private final ShaderProgram skyShader;
	
	private final Mesh box;
	
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	private final Vector3 tmpVec = new Vector3();
	
	private static final float oneThird = 1f/3f;
	private static final float twoThird = 2f/3f;
	
	public SkyBox(Texture skyTexture)
	{
		this.skyTexture = skyTexture;

		box = getSkyBox();
		
		skyShader = new ShaderProgram(VERTEX_SHADER_SKY, FRAGMENT_SHADER_SKY);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
	}
	
	public void update(float delta)
	{
		
	}
	
	public void render(Camera cam, LightManager lights)
	{	
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		
		skyShader.begin();
		
		//skyTexture.bind(0);
		mat41.set(cam.combined).mul(mat42.setToTranslation(cam.position));
		skyShader.setUniformMatrix("u_mvp", mat41);
		//skyShader.setUniformMatrix("u_v", mat42);
		skyShader.setUniformf("u_colour", lights.ambientColour);
		//skyShader.setUniformi("u_texture", 0);
		
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
	
	private static final String VERTEX_SHADER_SKY = 
			"attribute vec3 a_position;\n"+
			"attribute vec2 a_texCoord0;\n"+
					
			"uniform mat4 u_mvp;\n"+
			"uniform mat4 u_v;\n"+
			
			"varying float v_height;\n"+
			
			"void main() {\n"+
			"	vec4 position = u_mvp * vec4(a_position, 1.0);\n"+
			"	gl_Position = position.xyww;\n"+
			"	v_height = (u_v * vec4(a_position, 1.0)).y;\n"+
			"}";
	
	private static final String FRAGMENT_SHADER_SKY = 
			"#ifdef GL_ES\n"+
			"	precision mediump float;\n"+
			"#endif\n"+
			
			"uniform vec3 u_colour;\n"+
						
			"varying float v_height;\n"+
			
			"void main() {\n"+
			"	gl_FragColor.a = 1.0;\n"+
			"	gl_FragColor.rgb = u_colour;\n"+
			"}";
}
