package com.Lyeeedar.Graphics.Batchers;

import com.Lyeeedar.Graphics.Batchers.AbstractModelBatch.DrawableManager.Drawable;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class CellShadingModelBatch extends AbstractModelBatch {
	
	private static ShaderProgram shaderBody;
	private int textureHash = 0;

	@Override
	protected void render(LightManager lights, Camera cam) {
		if (shaderBody == null) loadBodyShader();
		
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		shaderBody.begin();
		shaderBody.setUniformMatrix("u_pv", cam.combined);
		shaderBody.setUniformf("fog_col", lights.ambientColour);
		shaderBody.setUniformf("fog_min", GLOBALS.FOG_MIN);
		shaderBody.setUniformf("fog_max", GLOBALS.FOG_MAX);
		shaderBody.setUniformf("u_viewPos", cam.position);
		lights.applyLights(shaderBody);
		
		for (int i = drawableManager.drawables.size; --i >= 0;) {

			final Drawable drawable = drawableManager.drawables.get(i);
			
			shaderBody.setUniformMatrix("u_mm", drawable.model_matrix);

			if (textureHash != drawable.textureHash)
			{
				for (int it = 0; it < drawable.textures.length; it++)
				{
					drawable.textures[it].bind(it);
					shaderBody.setUniformi("u_texture"+it, it);
				}

				textureHash = drawable.textureHash;
			}
			
			shaderBody.setUniformf("u_colour", drawable.colour);

			drawable.mesh.render(shaderBody, drawable.primitiveType);
		}
		
		shaderBody.end();
		textureHash = 0;
	}
	
	private void loadBodyShader()
	{
		if (GLOBALS.ANDROID)
		{
			shaderBody = new ShaderProgram(
					Gdx.files.internal("data/shaders/cellshading_body_vert.vertex.glsl"),
					Gdx.files.internal("data/shaders/cellshading_body_vert.fragment.glsl")
					);
		}
		else
		{
			shaderBody = new ShaderProgram(
					Gdx.files.internal("data/shaders/cellshading_body.vertex.glsl"),
					Gdx.files.internal("data/shaders/cellshading_body.fragment.glsl")
					);
		}
		
		if (!shaderBody.isCompiled()) System.err.println(shaderBody.getLog());
	}

	@Override
	public void dispose() {
		shaderBody.dispose();
		shaderBody = null;
	}

}
