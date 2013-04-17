package Graphics.Renderers;

import Graphics.Lights.LightManager;
import Graphics.Renderers.AbstractRenderer.DrawableManager.Drawable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class CellShadingRenderer extends AbstractRenderer {
	
	private static ShaderProgram shaderBody;
	private static ShaderProgram shaderOutline;
	private int textureHash = 0;

	@Override
	protected void flush(LightManager lights) {
		if (shaderOutline == null) loadOutlineShader();
		if (shaderBody == null) loadBodyShader();
		
		Gdx.gl.glCullFace(GL20.GL_FRONT);
		
		// ----- Draw outlines ----- //
		
		shaderOutline.begin();
		shaderOutline.setUniformMatrix("u_pv", cam.combined);

		for (int i = drawableManager.drawables.size; --i >= 0;) {

			final Drawable drawable = drawableManager.drawables.get(i);

			shaderOutline.setUniformMatrix("u_mm", drawable.model_matrix);
			shaderOutline.setUniformf("u_colour", new Vector3());

			drawable.mesh.render(shaderOutline, drawable.primitiveType);
		}
		shaderOutline.end();
		
		// ----- Fill in main body ----- //
		
		Gdx.gl.glCullFace(GL20.GL_BACK);
		
		shaderBody.begin();
		shaderBody.setUniformMatrix("u_pv", cam.combined);
		lights.applyLights(shaderBody);

		for (int i = drawableManager.drawables.size; --i >= 0;) {

			final Drawable drawable = drawableManager.drawables.get(i);

			shaderBody.setUniformMatrix("u_mm", drawable.model_matrix);
			shaderBody.setUniformMatrix("u_nm", drawable.normal_matrix);

			if (textureHash != drawable.textureHash)
			{
				drawable.texture.bind(0);
				shaderBody.setUniformi("u_texture", 0);
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
		shaderBody = new ShaderProgram(
				Gdx.files.internal("data/shaders/cellshading_body.vertex.glsl"),
				Gdx.files.internal("data/shaders/cellshading_body.fragment.glsl")
				);
		
		if (!shaderBody.isCompiled()) System.err.println(shaderBody.getLog());
	}
	
	private void loadOutlineShader()
	{	
		shaderOutline = new ShaderProgram(
				Gdx.files.internal("data/shaders/cellshading_outline.vertex.glsl"),
				Gdx.files.internal("data/shaders/cellshading_outline.fragment.glsl")
				);
		
		if (!shaderOutline.isCompiled()) System.err.println(shaderOutline.getLog());
	}

	@Override
	public void dispose() {
		shaderBody.dispose();
		shaderBody = null;
		
		shaderOutline.dispose();
		shaderOutline = null;
	}

}
