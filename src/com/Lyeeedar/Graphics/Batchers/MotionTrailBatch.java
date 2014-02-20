package com.Lyeeedar.Graphics.Batchers;

import java.util.LinkedList;

import com.Lyeeedar.Graphics.Queueables.MotionTrail;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class MotionTrailBatch implements Batch {
	
	private ShaderProgram shader = null;
	private long textureHash = 0L;
	private final LinkedList<MotionTrail> trailList ;
	
	public MotionTrailBatch()
	{
		shader = new ShaderProgram(SHADER_VERTEX, SHADER_FRAGMENT);
		if (!shader.isCompiled())
		{
			Gdx.app.log("Problem loading shader:", shader.getLog());
		}
		trailList = new LinkedList<MotionTrail>();
	}
	
	public void add(MotionTrail trail)
	{
		trailList.add(trail);
	}
	
	public void flush(Camera cam)
	{
		shader.begin();
		shader.setUniformMatrix("u_mv", cam.combined);
		
		for (MotionTrail trail : trailList)
		{
			if (trail.texHash != textureHash) {
				trail.texture.bind();
				textureHash = trail.texHash;
			}
			
			shader.setUniformf("u_colour", trail.colour);
			
			trail.mesh.render(shader, GL20.GL_TRIANGLE_STRIP, 0, trail.num_active);
		}
		
		shader.end();
		textureHash = 0;
		trailList.clear();
	}
	
	public void dispose()
	{
		shader.dispose();
	}
	
	private static final String SHADER_VERTEX = 
			"attribute vec3 a_position;\n"+
			"attribute vec2 a_texCoord0;\n"+
					
			"uniform mat4 u_mv;\n"+
			
			"varying vec2 v_texCoords;\n"+
			
			"void main() {\n"+
			"	v_texCoords = a_texCoord0;\n"+
			"	gl_Position = u_mv * vec4(a_position, 1.0);\n"+
			"}";
	private static final String SHADER_FRAGMENT = 
			"#ifdef GL_ES\n"+
			"	precision mediump float;\n"+
			"#endif\n"+
			
			"uniform sampler2D u_texture;\n"+
			"uniform vec4 u_colour;\n" +
			
			"varying vec2 v_texCoords;\n"+
			
			"void main() {\n"+
			"	gl_FragColor = texture2D(u_texture, v_texCoords) * u_colour;\n" +
			"}";

}
