package com.Lyeeedar.Util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

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
		
		bp.drawPixmap(bp, 0, 0, 0, 0, width, height);
		
		Color c = new Color();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				Color.rgba8888ToColor(c, ap.getPixel(x, y));
				
				if (c.a != 0) bp.drawPixel(x, y, ap.getPixel(x, y));
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

}
