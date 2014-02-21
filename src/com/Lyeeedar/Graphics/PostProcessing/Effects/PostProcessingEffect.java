/*******************************************************************************
 * Copyright (c) 2012 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Graphics.PostProcessing.Effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * This class models an effect to be applied in Post Processing. <br>
 * It should be used with a PostProcessor class.
 * @author Philip
 *
 */
public abstract class PostProcessingEffect {
	
	SpriteBatch batch = new SpriteBatch();
	
	ShaderProgram shader;

	public PostProcessingEffect()
	{
		create();
	}
	
	abstract public void render(Texture texture, FrameBuffer buffer, Texture depthTexture);
	
	public void dispose()
	{
		if (shader != null) shader.dispose();
		shader = null;
		if (batch != null) batch.dispose();
		batch = null;
	}
	
	public abstract void create();
	
}
