package Graphics;

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
	private final ShaderProgram seaShader;
	
	private final Mesh box;
	private final Mesh plane;
	
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	private final Vector3 seaColour = new Vector3();
	private final Vector3 foamColour = new Vector3();
	
	private final Vector3 tmpVec = new Vector3();
	
	private static final float oneThird = 1f/3f;
	private static final float twoThird = 2f/3f;
	
	private float seaHeight;
	private float foamHeight;
	private float time;
	
	public SkyBox(Texture texture, Vector3 seaColour, Vector3 foamColour)
	{
		this.skyTexture = texture;
		this.seaColour.set(seaColour);
		this.foamColour.set(foamColour);
		
		box = getSkyBox();
		plane = genPlane(1000, 0, 1000);
		
		skyShader = new ShaderProgram(VERTEX_SHADER_SKY, FRAGMENT_SHADER_SKY);
		if (!skyShader.isCompiled()) {
			System.err.println(skyShader.getLog());
		}
		seaShader = new ShaderProgram(VERTEX_SHADER_SEA, FRAGMENT_SHADER_SEA);
		if (!seaShader.isCompiled()) {
			System.err.println(seaShader.getLog());
		}
	}
	
	public void update(float delta)
	{
		time += delta;
		if (time > Math.PI*2) time = 0;
		
		seaHeight = (float) Math.sin(time+1)/20;
		foamHeight = (float) Math.sin(time)/20;
	}
	
	public void render(Camera cam)
	{
		Gdx.gl.glDepthMask(false);
		
		seaShader.begin();
		
		tmpVec.set(cam.position.x, foamHeight+0.05f, cam.position.z);
		mat41.set(cam.combined).mul(mat42.setToTranslation(tmpVec));
		seaShader.setUniformMatrix("u_mvp", mat41);
		seaShader.setUniformf("u_colour", foamColour);
		
		plane.render(seaShader, GL20.GL_TRIANGLE_STRIP);
		
		Gdx.gl.glDepthMask(true);
		
		tmpVec.set(cam.position.x, seaHeight, cam.position.z);
		mat41.set(cam.combined).mul(mat42.setToTranslation(tmpVec));
		seaShader.setUniformMatrix("u_mvp", mat41);
		seaShader.setUniformf("u_colour", seaColour);
		
		plane.render(seaShader, GL20.GL_TRIANGLE_STRIP);
		
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
	
	private static Mesh genPlane (float x, float y, float z) {

		Mesh mesh = new Mesh(true, 4, 0, 
				new VertexAttribute(Usage.Position, 3, "a_position"));
		
		float[] vertices = {
				-x, y, -z,
				-x, y, z,
				x, y, -z,
				x, y, z
				};

		mesh.setVertices(vertices);

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
	
	private static final String VERTEX_SHADER_SEA = 
			"attribute vec3 a_position;\n"+
					
			"uniform mat4 u_mvp;\n"+
			
			"void main() {\n"+
			"	vec4 position = u_mvp * vec4(a_position, 1.0);\n"+
			"	gl_Position = position.xyzw;\n"+
			"}";
	
	private static final String FRAGMENT_SHADER_SEA = 
			"#ifdef GL_ES\n"+
			"	precision mediump float;\n"+
			"#endif\n"+
			
			"uniform vec3 u_colour;\n"+
			
			"void main() {\n"+
			"	gl_FragColor.rgb = u_colour;\n"+
			"	gl_FragColor.a = 1.0;\n"+
			"}";
}
