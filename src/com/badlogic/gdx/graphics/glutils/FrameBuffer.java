/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link FrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 * 
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 * 
 * <p>
 * A FrameBuffer must be disposed if it is no longer needed
 * </p>
 * 
 * @author mzechner */
public class FrameBuffer implements Disposable {
	/** the frame buffers **/
	private final static Map<Application, Array<FrameBuffer>> buffers = new HashMap<Application, Array<FrameBuffer>>();

	/** the color buffer textures **/
	protected Texture[] colorTextures;
	private final IntBuffer colorDrawBuffers;
	
	/** the depth buffer texture **/
	protected Texture depthTexture;
	protected boolean ownsDepth;

	/** the default framebuffer handle, a.k.a screen. */
	private static int defaultFramebufferHandle;
	/** true if we have polled for the default handle already. */
	private static boolean defaultFramebufferHandleInitialized = false;
	
	/** the framebuffer handle **/
	private int framebufferHandle;

	/** width **/
	protected final int width;

	/** height **/
	protected final int height;

	/** depth **/
	protected final boolean hasDepth;
	protected final boolean hasColour;

	/** format **/
	protected final Pixmap.Format format;

	/** Creates a new FrameBuffer having the given dimensions and potentially a depth buffer attached.
	 * 
	 * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only
	 * RGB565, RGBA4444 and RGB5_A1 are color-renderable
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws GdxRuntimeException in case the FrameBuffer could not be created */
	public FrameBuffer (Pixmap.Format format, int width, int height, boolean hasDepth) {
		this(format, width, height, 1, hasDepth);
	}
	
	public FrameBuffer (Pixmap.Format format, int width, int height, int numColour, boolean hasDepth) {
		this(format, width, height, numColour, hasDepth, null);
	}
	
	public FrameBuffer (Pixmap.Format format, int width, int height, int numColour, boolean hasDepth, Texture depthTexture) {
		this.width = width;
		this.height = height;
		this.format = format;
		this.hasDepth = hasDepth;
		
		if (hasDepth)
		{
			if (depthTexture == null) ownsDepth = true;
			else 
			{
				ownsDepth = false;
				this.depthTexture = depthTexture;
			}
		}
		
		this.colorTextures = new Texture[numColour];
		this.hasColour = numColour > 0;
		build();

		addManagedFrameBuffer(Gdx.app, this);
		
		colorDrawBuffers = BufferUtils.newIntBuffer(numColour);
		for (int i = 0; i < numColour; ++i)
			colorDrawBuffers.put(getColorAttachmentVal(i));
		colorDrawBuffers.position(0);
	}
	
	/**
	 * Override this method in a derived class to set up the backing texture as you like.
	 */
	protected void setupTexture() {
		
		if (hasColour)
		{
			for (int i = 0; i < colorTextures.length; i++)
			{
				colorTextures[i] = new Texture(width, height, format);
				colorTextures[i].setFilter(TextureFilter.Linear, TextureFilter.Linear);
				colorTextures[i].setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			}
		}
		
		if (hasDepth && ownsDepth)
		{
			depthTexture = new Texture(width, height, Format.DEPTH);
			depthTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			depthTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			//Gdx.gl.glTexParameteri(depthTexture.glTarget, GL30.GL_DEPTH_TEXTURE_MODE, GL30.GL_LUMINANCE);
		}
	}
	
	private int getColorAttachmentVal(int i)
	{
		if (i == 0) return GL30.GL_COLOR_ATTACHMENT0;
		else if (i == 1) return GL30.GL_COLOR_ATTACHMENT1;
		else if (i == 2) return GL30.GL_COLOR_ATTACHMENT2;
		else if (i == 3) return GL30.GL_COLOR_ATTACHMENT3;
		else if (i == 4) return GL30.GL_COLOR_ATTACHMENT4;
		else if (i == 5) return GL30.GL_COLOR_ATTACHMENT5;
		else if (i == 6) return GL30.GL_COLOR_ATTACHMENT6;
		else if (i == 7) return GL30.GL_COLOR_ATTACHMENT7;
		else if (i == 8) return GL30.GL_COLOR_ATTACHMENT8;
		else if (i == 9) return GL30.GL_COLOR_ATTACHMENT9;
		else if (i == 10) return GL30.GL_COLOR_ATTACHMENT10;
		else if (i == 11) return GL30.GL_COLOR_ATTACHMENT11;
		else if (i == 12) return GL30.GL_COLOR_ATTACHMENT12;
		else if (i == 13) return GL30.GL_COLOR_ATTACHMENT13;
		else if (i == 14) return GL30.GL_COLOR_ATTACHMENT14;
		else if (i == 15) return GL30.GL_COLOR_ATTACHMENT15;
		else throw new RuntimeException("Too many color attachments to Frame Buffer!");
	}

	private void build () {
		//if (!Gdx.graphics.isGL30Available()) throw new GdxRuntimeException("GL3 is required.");

		GL30 gl = Gdx.graphics.getGL30();

		// iOS uses a different framebuffer handle! (not necessarily 0)
		if (!defaultFramebufferHandleInitialized) {
			defaultFramebufferHandleInitialized = true;
		   if (Gdx.app.getType() == ApplicationType.iOS) {
		     IntBuffer intbuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();
		     gl.glGetIntegerv(GL30.GL_FRAMEBUFFER_BINDING, intbuf);
		     defaultFramebufferHandle = intbuf.get(0);
		   }
		   else {
		     defaultFramebufferHandle = 0;
		   }
		}
		
		setupTexture();

		IntBuffer handle = BufferUtils.newIntBuffer(1);
		gl.glGenFramebuffers(1, handle);
		framebufferHandle = handle.get(0);
		
		gl.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferHandle);
		if (hasColour) {
			for (int i = 0; i < colorTextures.length; i++)
			{
				gl.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, getColorAttachmentVal(i), GL30.GL_TEXTURE_2D,
						colorTextures[i].getTextureObjectHandle(), 0);
			}
		}
		if (hasDepth) {
			gl.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, 
					depthTexture.getTextureObjectHandle(), 0);
		}
		int result = gl.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

		gl.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		gl.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL30.GL_FRAMEBUFFER, defaultFramebufferHandle);

		if (result != GL30.GL_FRAMEBUFFER_COMPLETE) {
			if (hasColour) 
			{
				for (int i = 0; i < colorTextures.length; i++) colorTextures[i].dispose();
			}
			if (hasDepth && ownsDepth) {
				depthTexture.dispose();
			}

			handle.clear();
			handle.put(framebufferHandle);
			handle.flip();
			gl.glDeleteFramebuffers(1, handle);

			if (result == GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
			if (result == GL30.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS)
				throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
			if (result == GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT)
				throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
			if (result == GL30.GL_FRAMEBUFFER_UNSUPPORTED)
				throw new IllegalStateException("frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	/** Releases all resources associated with the FrameBuffer. */
	public void dispose () {
		GL30 gl = Gdx.graphics.getGL30();

		IntBuffer handle = BufferUtils.newIntBuffer(1);

		if (hasColour) for (int i = 0; i < colorTextures.length; i++) colorTextures[i].dispose();
		if (hasDepth && ownsDepth) {
			depthTexture.dispose();
		}

		handle.clear();
		handle.put(framebufferHandle);
		handle.flip();
		gl.glDeleteFramebuffers(1, handle);

		if (buffers.get(Gdx.app) != null) buffers.get(Gdx.app).removeValue(this, true);
	}

	/** Makes the frame buffer current so everything gets drawn to it. */
	public void begin () {
		Gdx.graphics.getGL30().glViewport(0, 0, width, height);
		Gdx.graphics.getGL30().glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferHandle);
		Gdx.gl30.glDrawBuffers(colorDrawBuffers.capacity(), colorDrawBuffers);
	}

	/** Unbinds the framebuffer, all drawing will be performed to the normal framebuffer from here on. */
	public void end () {
		Gdx.graphics.getGL30().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.graphics.getGL30().glBindFramebuffer(GL30.GL_FRAMEBUFFER, defaultFramebufferHandle);
	}
	
	/** @return the color buffer texture */
	public Texture getColorBufferTexture () 
	{
		return getColorBufferTexture(0);
	}

	/** @return the color buffer texture */
	public Texture getColorBufferTexture (int index) 
	{
		return colorTextures[index];
	}
	
	public Texture getDepthBufferTexture()
	{
		return depthTexture;
	}

	/** @return the height of the framebuffer in pixels */
	public int getHeight () {
		return height;
	}

	/** @return the width of the framebuffer in pixels */
	public int getWidth () {
		return width;
	}

	private static void addManagedFrameBuffer (Application app, FrameBuffer frameBuffer) {
		Array<FrameBuffer> managedResources = buffers.get(app);
		if (managedResources == null) managedResources = new Array<FrameBuffer>();
		managedResources.add(frameBuffer);
		buffers.put(app, managedResources);
	}

	/** Invalidates all frame buffers. This can be used when the OpenGL context is lost to rebuild all managed frame buffers. This
	 * assumes that the texture attached to this buffer has already been rebuild! Use with care. */
	public static void invalidateAllFrameBuffers (Application app) {
		if (Gdx.graphics.getGL30() == null) return;

		Array<FrameBuffer> bufferArray = buffers.get(app);
		if (bufferArray == null) return;
		for (int i = 0; i < bufferArray.size; i++) {
			bufferArray.get(i).build();
		}
	}

	public static void clearAllFrameBuffers (Application app) {
		buffers.remove(app);
	}

	public static StringBuilder getManagedStatus (final StringBuilder builder) {
		builder.append("Managed buffers/app: { ");
		for (Application app : buffers.keySet()) {
			builder.append(buffers.get(app).size);
			builder.append(" ");
		}
		builder.append("}");
		return builder;
	}
	
	public static String getManagedStatus () {
		return getManagedStatus(new StringBuilder()).toString();
	}
}
