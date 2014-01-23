package com.Lyeeedar.Util;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;

public final class ImageUtils {
	
	public static ArrayList<BufferedImage> splitImage(BufferedImage image, int numx, int numy)
	{
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		
		int xstep = image.getWidth()/numx;
		int ystep = image.getHeight()/numy;
		
		for (int y = 0; y < numy; y++)
		{
			for (int x = 0; x < numx; x++)
			{
				BufferedImage ni = image.getSubimage(x*xstep, y*ystep, xstep, ystep);
				images.add(ni);
			}
		}
		
		return images;
	}

	public static FloatBuffer extractAlpha(Pixmap pm)
	{
		FloatBuffer fb = BufferUtils.newFloatBuffer(pm.getWidth()*pm.getHeight());
		
		Color color = new Color();
		for (int x = 0; x < pm.getWidth(); x++)
		{
			for (int y = 0; y < pm.getHeight(); y++)
			{
				int cval = pm.getPixel(x, y);
				Color.rgba8888ToColor(color, cval);
				System.out.println(color.a);
				fb.put(color.a);
			}
		}
		
		return fb;
	}
	
	public static Pixmap TextureToPixmap(Texture texture)
	{
		try {
			texture.getTextureData().prepare();
		}
		catch (Exception e)
		{
			
		}
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
	
	public static Decal getTextDecal(float width, float height, SpriteBatch sB, BitmapFont font, NinePatch np, String... text)
	{
		Texture tex = drawText(sB, font, np, text);
		
		TextureRegion region = new TextureRegion(tex, tex.getWidth(), 0, -tex.getWidth(), tex.getHeight());
		
		if (width == 0)
		{
			width = 0.25f * text[0].length();
		}
		if (height == 0)
		{
			height = 0.8f;
		}
		
		Decal decal = Decal.newDecal(width, height, region, true);
		
		return decal;
	}
	
	public static Texture drawText(SpriteBatch sB, BitmapFont font, NinePatch np, String... text)
	{
		float height = font.getLineHeight() * (text.length+1);
		float width = 0;
		float xoffset = 0;
		float yoffset = 0;
		
		float temp;
		for (int i = 0; i < text.length; i++)
		{
			temp = font.getBounds(text[i]).width;
			
			if (temp > width) width = temp;
		}
		
		if (np != null)
		{
			width += np.getLeftWidth()+np.getRightWidth();
			xoffset = np.getLeftWidth();
			
			height += np.getBottomHeight()+np.getTopHeight();
			yoffset = np.getTopHeight();
		}
		
		FrameBuffer fB = new FrameBuffer(Format.RGBA4444, (int) width, (int) height, false);
		fB.begin();
		
		Gdx.graphics.getGL20().glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		Gdx.graphics.getGL20().glClear(GL20.GL_COLOR_BUFFER_BIT);
				
		sB.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		sB.begin();
		
		if (np != null)
		{
			np.draw(sB, 0, 0, width, height);
		}
		
		for (int line = 0; line < text.length; line++)
		{
			font.draw(sB, text[line], xoffset, yoffset+(line+1)*font.getLineHeight());
		}
		sB.end();
	
		fB.end();
		return fB.getColorBufferTexture();
	}
	
	public static BufferedImage[] deconstructAtlas(TextureAtlas atlas)
	{
		Texture tex = atlas.getTextures().iterator().next();
		tex.getTextureData().prepare();
		Pixmap pixels = tex.getTextureData().consumePixmap();
		
		Array<AtlasRegion> regions = atlas.getRegions();
		regions.sort(new Comparator<AtlasRegion>(){
			@Override
			public int compare(AtlasRegion a1, AtlasRegion a2) {
				int val1 = Integer.parseInt(a1.name.replace("sprite", ""));
				int val2 = Integer.parseInt(a2.name.replace("sprite", ""));
				return val1 - val2;
			}});
		
		BufferedImage[] images = new BufferedImage[regions.size];
		
		for (int i = 0; i < regions.size; i++)
		{
			AtlasRegion region = regions.get(i);
			images[i] = new BufferedImage(region.getRegionWidth(), region.getRegionHeight(), BufferedImage.TYPE_INT_ARGB);
			
			for (int x = region.getRegionX(); x < region.getRegionX()+region.getRegionWidth(); x++)
			{
				for (int y = region.getRegionY(); y < region.getRegionY()+region.getRegionHeight(); y++)
				{
					Color c = new Color();
					Color.rgba8888ToColor(c, pixels.getPixel(x, y));
					
					java.awt.Color cc = new java.awt.Color(c.r, c.g, c.b, c.a);
					
					images[i].setRGB(x-region.getRegionX(), y-region.getRegionY(), cc.getRGB());
				}
			}
		}
		
		return images;
	}
	
	public static BufferedImage pixmapToImage(Pixmap pm)
	{
		BufferedImage image = new BufferedImage(pm.getWidth(), pm.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < pm.getWidth(); x++)
		{
			for (int y = 0; y < pm.getHeight(); y++)
			{
				Color c = new Color();
				Color.rgba8888ToColor(c, pm.getPixel(x, y));
				
				java.awt.Color cc = new java.awt.Color(c.r, c.g, c.b, c.a);
				
				image.setRGB(x, y, cc.getRGB());
			}
		}
		
		return image;
	}
	
	public static BufferedImage arrayToImage(Color[][] array)
	{
		BufferedImage image = new BufferedImage(array.length, array[0].length, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < array.length; x++)
		{
			for (int y = 0; y < array[0].length; y++)
			{
				Color c = array[x][y];
				
				java.awt.Color cc = new java.awt.Color(c.r, c.g, c.b, c.a);
				
				image.setRGB(x, y, cc.getRGB());
			}
		}
		
		return image;
	}
	
	public static Pixmap arrayToPixmap(Color[][] array)
	{
		Pixmap image = new Pixmap(array.length, array[0].length, Format.RGBA8888);
		for (int x = 0; x < array.length; x++)
		{
			for (int y = 0; y < array[0].length; y++)
			{
				Color c = array[x][y];
				
				image.drawPixel(x, y, Color.rgba8888(c));
			}
		}
		
		return image;
	}
	
	public static void sampleColour(Pixmap pixmap, Color colour, float x, float y)
	{
		int bottom = (int) (y);
		int top = bottom + 1;
		int left = (int) (x);
		int right = left + 1;
		
		float ax = x-left;
		float ay = y-bottom;
		
		Color tl = new Color();
		Color tr = new Color();
		Color bl = new Color();
		Color br = new Color();
		
		Color.rgba8888ToColor(tl, pixmap.getPixel(left, top));
		Color.rgba8888ToColor(tr, pixmap.getPixel(right, top));
		
		Color.rgba8888ToColor(bl, pixmap.getPixel(left, bottom));
		Color.rgba8888ToColor(br, pixmap.getPixel(right, bottom));
		
		tl.lerp(tr, ax);
		bl.lerp(br, ax);
		bl.lerp(tl, ay);
		
		colour.set(bl);
	}
	
	public static final float lerp(float s, float e, float a)
	{
		return s + (e-s)*a;
	}
	
	public static final Vector3 lerp(Vector3 s, Vector3 e, float a, Vector3 dst)
	{
		dst.set(e).sub(s).scl(a).add(s);		
		return dst;
	}
	
	public static final float bilerp(float s00, float s01, float s10, float s11, float xfrac, float yfrac) {
		float s0 = (s01 - s00)*xfrac + s00;
		float s1 = (s11 - s10)*xfrac + s10;
		return (s1 - s0)*yfrac + s0;
	}
	
	public static float bilinearInterpolation(float[][] array, float x, float y)
	{
		x = MathUtils.clamp(x, 0, array.length-2);
		y = MathUtils.clamp(y, 0, array[0].length-2);
		
		int bottom = (int) (y);
		int top = bottom + 1;
		int left = (int) (x);
		int right = left + 1;

		float s00 = array[left][bottom];
		float s01 = array[right][bottom];
		float s10 = array[left][top];
		float s11 = array[right][top];
		
		return bilerp(s00, s01, s10, s11, x-(float)left, y-(float)bottom);
	}

}
