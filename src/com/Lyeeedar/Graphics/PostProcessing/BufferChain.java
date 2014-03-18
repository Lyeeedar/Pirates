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
package com.Lyeeedar.Graphics.PostProcessing;

import com.Lyeeedar.Graphics.PostProcessing.Effects.PostProcessingEffect;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class BufferChain {
	
	public static final int NUM_BUFFERS = 2;
	
	final FrameBuffer[] buffers = new FrameBuffer[NUM_BUFFERS];

	int currentBuffer;
	
	Texture texture;
	Texture depthTexture;

	public BufferChain(Format format, int width, int height) {
		updateBuffers(format, width, height);
	}

	public void updateBuffers(Format format, int width, int height) {

		for (int i = 0; i < NUM_BUFFERS; i++)
		{
			if (buffers[i] != null) buffers[i].dispose();
			buffers[i] = new FrameBuffer(format, width, height, false);
		}
		
		currentBuffer = 0;
	}
	
	public void begin(Texture texture, Texture depthTexture)
	{
		this.texture = texture;
		this.depthTexture = depthTexture;
	}
	
	public void applyEffect(PostProcessingEffect effect)
	{
		nextBuffer();
		effect.render(texture, buffers[currentBuffer], depthTexture);
		texture = buffers[currentBuffer].getColorBufferTexture(0);
	}
	
	public Texture getFinalImage()
	{
		return texture;
	}
	
	private void nextBuffer()
	{
		currentBuffer++;
		
		if (currentBuffer == NUM_BUFFERS) currentBuffer = 0;
	}
	
	public void dispose()
	{
		for (int i = 0; i < buffers.length; i++)
		{
			buffers[i].dispose();
		}
	}
}
