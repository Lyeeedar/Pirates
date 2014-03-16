package com.badlogic.gdx.graphics;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.BufferUtils;

public class TextureArray
{
	private Pixmap[] pixmaps;
	private int glHandle;
	private static final int glTarget = GL30.GL_TEXTURE_2D_ARRAY;
	protected TextureFilter minFilter = TextureFilter.Nearest;
	protected TextureFilter magFilter = TextureFilter.Nearest;
	protected TextureWrap uWrap = TextureWrap.ClampToEdge;
	protected TextureWrap vWrap = TextureWrap.ClampToEdge;
	public int width;
	public int height;
	public int layers;
	
	public TextureArray(Pixmap[] pixmaps)
	{
		this.pixmaps = pixmaps;
		this.width = pixmaps[0].getWidth();
		this.height = pixmaps[0].getHeight();
		this.layers = pixmaps.length;
		
		glHandle = createGLHandle();
		
		setup();
	}
	
	private static final IntBuffer buffer = BufferUtils.newIntBuffer(1);
	
	/** Binds the texture to the given texture unit. Sets the currently active texture unit via
	 * {@link GLCommon#glActiveTexture(int)}.
	 * @param unit the unit (0 to MAX_TEXTURE_UNITS). */
	public void bind (int unit) {
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0 + unit);
		Gdx.gl.glBindTexture(glTarget, glHandle);
	}
	
	/** Sets the {@link TextureWrap} for this texture on the u and v axis. This will bind this texture!
	 * @param u the u wrap
	 * @param v the v wrap */
	public void setWrap (TextureWrap u, TextureWrap v) {
		this.uWrap = u;
		this.vWrap = v;
		bind(0);
		Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_S, u.getGLEnum());
		Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_WRAP_T, v.getGLEnum());
	}
	
	/** Sets the {@link TextureFilter} for this texture for minification and magnification.
	 * This will bind this texture!
	 * @param minFilter the minification filter
	 * @param magFilter the magnification filter */
	public void setFilter (TextureFilter minFilter, TextureFilter magFilter) {
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		bind(0);
		Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_MIN_FILTER, minFilter.getGLEnum());
		Gdx.gl.glTexParameterf(glTarget, GL20.GL_TEXTURE_MAG_FILTER, magFilter.getGLEnum());
	}
	
	protected static int createGLHandle () {
		buffer.position(0);
		buffer.limit(buffer.capacity());
		Gdx.gl.glGenTextures(1, buffer);
		return buffer.get(0);
	}
	
	protected void setup()
	{
		bind(0);

		ByteBuffer bb = ByteBuffer.allocateDirect(4860000);//width*height*pixmaps.length*4);
//		System.out.println(bb.capacity());
//		
//		for (int i = 0; i < layers; i++)
//		{
//			bb.put(pixmaps[i].getPixels());
//			System.out.println(bb.remaining()+"  "+pixmaps[i].getPixels().capacity());
//		}
		
		Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, pixmaps[0].getGLInternalFormat(), width, height, layers, 0, pixmaps[0].getGLFormat(), pixmaps[0].getGLType(), bb);

		for (int i = 0; i < layers; i++)
		{
			Gdx.gl30.glTexSubImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, pixmaps[i].getGLInternalFormat(), pixmaps[i].getGLType(), pixmaps[i].getPixels());
		}
		
		Gdx.gl30.glGenerateMipmap(glTarget);
		
		setWrap(uWrap, vWrap);
		setFilter(minFilter, magFilter);
		Gdx.gl.glBindTexture(glTarget, 0);
	}
}
