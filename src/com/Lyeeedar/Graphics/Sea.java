package com.Lyeeedar.Graphics;

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

public class Sea {
	
	private final ShaderProgram seaShader;
	private final Texture seaTexture;
	
	private final Mesh sea[];
	
	private static final int scale = 10;
	
	public final Vector3 seaColour = new Vector3();
	private final float[] amplitudes = new float[8];
	private final float[] wavelengths = new float[8];
	private final float[] speeds = new float[8];
	private final float[] directions = new float[16];
	
	private final int numWaves;
	
	private final Vector3 tmpVec = new Vector3();
	private final Matrix4 mat41 = new Matrix4();
	private final Matrix4 mat42 = new Matrix4();
	
	public Sea(Texture seaTexture, Vector3 seaColour)
	{
		this.seaTexture = seaTexture;
		
		this.seaColour.set(seaColour);
		
		numWaves = 3;
				
		amplitudes[0] = 0.02f;
		wavelengths[0] = 1.1f;
		speeds[0] = 1.0f;
		directions[0] = 1.0f; directions[1] = 0.0f;
		
		amplitudes[1] = 0.3f;
		wavelengths[1] = 73.0f;
		speeds[1] = 15.0f;
		directions[2] = -1.0f; directions[3] = -1.0f;
		
		amplitudes[2] = 0.4f;
		wavelengths[2] = 373.0f;
		speeds[2] = 51.1f;
		directions[4] = 1.0f; directions[5] = 0.0f;
		
		sea = getSea(255);
		
		seaShader = new ShaderProgram(
				Gdx.files.internal("data/shaders/sea.vertex.glsl"),
				Gdx.files.internal("data/shaders/sea.fragment.glsl")
				);
		if (!seaShader.isCompiled()) {
			System.err.println(seaShader.getLog());
		}
	}
	
	public void render(Camera cam, Vector3 position, LightManager lights)
	{
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		seaShader.begin();
		
		mat41.set(cam.combined);
		
		seaShader.setUniformf("delta", GLOBALS.PROGRAM_TIME);
		seaShader.setUniformi("numWaves", numWaves);
		seaShader.setUniform1fv("amplitude", amplitudes, 0, 8);
		seaShader.setUniform1fv("wavelength", wavelengths, 0, 8);
		seaShader.setUniform1fv("speed", speeds, 0, 8);
		seaShader.setUniform2fv("direction", directions, 0, 8);
		
		seaShader.setUniformi("u_posx", ((int)(position.x/(float)scale))*scale);
		seaShader.setUniformi("u_posz", ((int)(position.z/(float)scale))*scale);
		seaShader.setUniformMatrix("u_mvp", mat41);
		
		seaShader.setUniformf("u_viewPos", position);
		
		seaShader.setUniformf("fog_colour", tmpVec.set(seaColour).scl(lights.ambientColour));
		seaShader.setUniformf("fog_min", GLOBALS.FOG_MIN);
		seaShader.setUniformf("fog_max", GLOBALS.FOG_MAX);
		
		seaShader.setUniformf("u_colour", seaColour);
		seaShader.setUniformi("u_texture", 0);
		seaTexture.bind(0);
		
		lights.applyLights(seaShader);
		
		for (Mesh s : sea)
		{
			s.render(seaShader, GL20.GL_TRIANGLES);
		}
		
		seaShader.end();
	}
	
	public float wave(int i, float x, float y) 
	{
	    float frequency = 2.0f*(float)Math.PI/wavelengths[i];
	    float phase = speeds[i] * frequency;
	    float theta = directions[i*2] * x + directions[(i*2)+1] * y;
	    return (float) (amplitudes[i] * Math.sin(theta * frequency + GLOBALS.PROGRAM_TIME * phase));
	}

	public float waveHeight(float x, float y) 
	{
	    float height = 0.0f;
	    for (int i = 0; i < numWaves; ++i)
	        height += wave(i, x, y);
	    return height;
	}
	
	public float waveMax(int i) 
	{
	    return (float) (amplitudes[i]);
	}
	
	public float waveHeightMax()
	{
		float height = 0.0f;
	    for (int i = 0; i < numWaves; ++i)
	        height += waveMax(i);
	    return height;
	}
	
	public void modifyVelocity(Vector3 velocity, float delta, float px, float pz)
	{
		float x = 0;
		float z = 0;
		
		for (int i = 0; i < numWaves; i++)
		{
			x += -directions[(i*2)+0]*speeds[i]*delta*(wave(i, px, pz)/amplitudes[i])*0.01f*amplitudes[i];
			z += -directions[(i*2)+1]*speeds[i]*delta*(wave(i, px, pz)/amplitudes[i])*0.01f*amplitudes[i];
		}
		
		velocity.x += x;
		velocity.z += z;
	}
	
	private static Mesh[] getSea(int size)
	{	
		float offsets[][] = {
				// High res close
				{0, 0, 1},
				{(-size*scale)+scale, 0, 1},
				{0, (-size*scale)+scale, 1},
				{(-size*scale)+scale, (-size*scale)+scale, 1},
				
				// Low res distance
				// Edges
				{(-size*scale)+2*scale, (size*scale)-2*scale, 2},
				{(-size*scale)+2*scale, (-3*size*scale)+6*scale, 2},
				{(size*scale)-2*scale, (-size*scale)+2*scale, 2},
				{(-3*size*scale)+6*scale, (-size*scale)+2*scale, 2},
				// Corners
				{(size*scale)-2*scale, (size*scale)-2*scale, 2},
				{(size*scale)-2*scale, (-3*size*scale)+6*scale, 2},
				{(-3*size*scale)+6*scale, (size*scale)-2*scale, 2},
				{(-3*size*scale)+6*scale, (-3*size*scale)+6*scale, 2}
		};
		
		Mesh mesh[] = new Mesh[offsets.length];
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
		
		for (int index = 0; index < offsets.length; index++)
		{
			i = 0;
			for (int ix = 0; ix < size; ix++)
			{
				for (int iz = 0; iz < size; iz++)
				{
					vertices[i++] = offsets[index][0]+(ix*(offsets[index][2]*scale));
					vertices[i++] = 0;
					vertices[i++] = offsets[index][1]+(iz*(offsets[index][2]*scale));
				}
			}
			mesh[index] = new Mesh(true, size*size, indices.length, 
					new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
			mesh[index].setVertices(vertices);
			mesh[index].setIndices(indices);
		}

		return mesh;
	}

}
