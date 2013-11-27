package com.Lyeeedar.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public final class ImageUtils {

	public static Pixmap TextureToPixmap(Texture texture)
	{
		texture.getTextureData().prepare();
		return texture.getTextureData().consumePixmap();
	}
	
	public static Texture PixmapToTexture(Pixmap pixmap)
	{
		return new Texture(pixmap);
	}
	
	public static Texture merge(Texture below, Texture above)
	{
		Pixmap bp = TextureToPixmap(below);
		Pixmap ap = TextureToPixmap(above);
		
		return PixmapToTexture(merge(bp, ap));
	}
	
	public static Pixmap merge(Pixmap bp, Pixmap ap)
	{
		final int width = bp.getWidth();
		final int height = bp.getHeight();
		
		//bp.drawPixmap(bp, 0, 0, 0, 0, width, height);
		
		Color cb = new Color();
		Color ca = new Color();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Color.rgba8888ToColor(ca, ap.getPixel(x, y));
				
				if (ca.a < 1.0f)
				{
					Color.rgba8888ToColor(cb, bp.getPixel(x, y));
					ca.mul(ca.a, ca.a, ca.a, 1.0f);
					cb.mul(cb.a, cb.a, cb.a, 1.0f);
					ca.add(cb);
				}
				
				bp.drawPixel(x, y, Color.rgba8888(ca));
			}
		}
		
		return bp;
	}
	
	public static void tint(Pixmap pixels, Color tint)
	{
		final int width = pixels.getWidth();
		final int height = pixels.getHeight();
		
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Color c = new Color();
				Color.rgba8888ToColor(c, pixels.getPixel(x, y));
				
				c.mul(tint);
				
				pixels.drawPixel(x, y, Color.rgba8888(c));
			}
		}
	}
	
	public static Pixmap copy(Pixmap pixmap)
	{
		Pixmap np = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
		np.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
		return np;
	}
	
	public static Decal getTextDecal(float width, float height, SpriteBatch sB, BitmapFont font, String... text)
	{
		Texture tex = drawText(sB, font, text);
		
		TextureRegion region = new TextureRegion(tex, tex.getWidth(), 0, -tex.getWidth(), tex.getHeight());
		
		Decal decal = Decal.newDecal(width, height, region, true);
		
		return decal;
	}
	
	public static Texture drawText(SpriteBatch sB, BitmapFont font, String... text)
	{
		float height = font.getLineHeight() * (text.length+1);
		
		float width = 0;
		
		float temp;
		for (int i = 0; i < text.length; i++)
		{
			temp = font.getBounds(text[i]).width;
			
			if (temp > width) width = temp;
		}
		
		FrameBuffer fB = new FrameBuffer(Format.RGBA4444, (int) width, (int) height, false);
		fB.begin();
		
		Gdx.graphics.getGL20().glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
				
		sB.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		sB.begin();
		
		for (int line = 0; line < text.length; line++)
		{
			font.draw(sB, text[line], 0, (line+1)*font.getLineHeight());
		}
		sB.end();
	
		fB.end();
		return fB.getColorBufferTexture();
	}

}
