package com.lyeeedar.Pirates;

import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.util.ImageUtils;

public class Sprite3D {
	
	public enum SpriteLayer
	{
		OTHER, // 6
		HEAD, // 5
		TOP, // 4
		BOTTOM, // 3
		FACE, // 2
		FEET, // 1
		BODY // 0
	}
	
	private static final short NUM_ANIMS = 15;
	private static final short NUM_FRAMES = 8;

	private final HashMap<SpriteLayer, SortedSet<SPRITESHEET>> layers;
	
	private Texture spritesheet;
	private float width;
	private short spriteWidth;
	private float height;
	private short spriteHeight;
	
	private final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
	private final Vector3 position = new Vector3();
	
	private Decal decal;
	private TextureRegion region;
	
	private float animationDelay;
	private float animationCD = 0;
	private boolean animate = false;
	private byte frame = 0;
	private byte animation = 0;
	private byte direction = 0;
	
	private final Vector3 tmp = new Vector3();
	private boolean update = false;	
	
	private boolean lock = false;
	private byte nextAnimation;
	private boolean animateStore;
	private byte switchAtFrame = 0;
	private Informable informable;

	public Sprite3D(float width, float height)
	{
		this.width = width;
		this.height = height;
		
		layers = new HashMap<SpriteLayer, SortedSet<SPRITESHEET>>();
		
		for (SpriteLayer sl : SpriteLayer.values())
		{
			layers.put(sl, new TreeSet<SPRITESHEET>());
		}
	}
	
	public void addLayer(String spritesheet, Color tint, int priority, SpriteLayer layer)
	{
		layers.get(layer).add(new SPRITESHEET(spritesheet, tint, priority));
	}
	public boolean removeLayer(String spritesheet, SpriteLayer layer)
	{
		SortedSet<SPRITESHEET> ss = layers.get(layer);
		SPRITESHEET s = null;
		for (SPRITESHEET sps : ss) if (sps.filename.equals(spritesheet)) { s = sps; break;}
		if (s != null) ss.remove(s);
		else return false;
		return true;
	}
	public void clearLayer(SpriteLayer layer)
	{
		layers.get(layer).clear();
	}
	
	public void create(AssetManager assetManager)
	{
		Pixmap sprite = null;
		SortedSet<SPRITESHEET> layer = layers.get(SpriteLayer.BODY);
		for (SPRITESHEET s : layer)
		{
			if (sprite == null)
			{
				sprite = ImageUtils.copy(assetManager.get(s.filename, Pixmap.class));
				ImageUtils.tint(sprite, s.colour);
			}
			else
			{
				Pixmap pix = assetManager.get(s.filename, Pixmap.class);
				ImageUtils.tint(pix, s.colour);
				sprite = ImageUtils.merge(sprite, pix);
			}
		}
		layer = layers.get(SpriteLayer.FEET);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		layer = layers.get(SpriteLayer.FACE);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		layer = layers.get(SpriteLayer.BOTTOM);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		layer = layers.get(SpriteLayer.TOP);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		layer = layers.get(SpriteLayer.HEAD);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		layer = layers.get(SpriteLayer.OTHER);
		for (SPRITESHEET s : layer)
		{
			Pixmap pix = assetManager.get(s.filename, Pixmap.class);
			ImageUtils.tint(pix, s.colour);
			sprite = ImageUtils.merge(sprite, pix);
		}
		
		if (spritesheet != null) spritesheet.dispose();
		spritesheet = ImageUtils.PixmapToTexture(sprite);
		
		spriteWidth = (short) (spritesheet.getWidth()/NUM_FRAMES);
		spriteHeight = (short) (spritesheet.getHeight()/NUM_ANIMS);
		
		region = new TextureRegion(spritesheet, 0, 0, spriteWidth, spriteHeight);
		decal = Decal.newDecal(width, height, region, true);
	}
	
	public void setPosition(Vector3 position)
	{
		this.position.set(position.x, position.y+(height/2), position.z);
	}
	public void setRotation(Vector3 rotation)
	{
		this.rotation.set(rotation);
	}
	
	public void playAnimation(byte animation, byte nextAnimation, byte switchAtFrame, Informable informable)
	{
		if (lock) return;
		
		this.switchAtFrame = switchAtFrame;
		this.informable = informable;
		this.animation = animation;
		this.update = true;
		this.nextAnimation = nextAnimation;
		this.animateStore = this.animate;
		this.animate = true;
		this.frame = 0;
		
		lock = true;
	}	
	public void setAnimation(byte animation, boolean animate, float animate_speed)
	{
		if (lock) return;
		
		this.animationDelay = animate_speed;
		if (this.animation != animation)
		{
			this.animation = animation;
			update = true;
		}
		if (this.animate != animate)
		{
			this.animate = animate;
			frame = 0;
			update = true;
		}
	}

	public void update(float delta, Camera cam)
	{
		animationCD -= delta;
		
		if (!animate)
		{
			animationCD = 0;
		}
		else if (animationCD < 0)
		{
			animationCD = animationDelay;
			frame++;
			if (lock && frame == switchAtFrame)
			{
				lock = false;
				this.animation = this.nextAnimation;
				this.animate = this.animateStore;
				informable.inform();
				frame = 0;
			}
			if (frame == NUM_FRAMES) {
				frame = 0;
			}
			update = true;
		}
		
		double angle = GLOBALS.angle(cam.direction, rotation, tmp);
		double abs_a = Math.abs(angle);
		
		byte d = 0;
		if (abs_a < 60)
		{
			d = 2;
		}
		else if (abs_a > 120)
		{
			d = 0;
		}
		else if (angle < 0)
		{
			d = 4;
		}
		else if (angle > 0)
		{
			d = 1;
		}
		if (direction != d)
		{
			direction = d;
			update = true;
		}
		
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
		
		if (update)
		{
			update = false;
			if (direction == 4){
				region.setRegion((frame+1)*spriteWidth, ((animation*3)+1)*spriteHeight, -spriteWidth, spriteHeight);
			}
			else {
				region.setRegion(frame*spriteWidth, ((animation*3)+direction)*spriteHeight, spriteWidth, spriteHeight);
			}
			decal.setTextureRegion(region);
		}
	}
	
	public void render(DecalBatch batch)
	{
		batch.add(decal);
	}
	
	private static final class SPRITESHEET implements Comparable<SPRITESHEET>
	{
		public final String filename;
		public final Color colour;
		public final int priority;
		
		public SPRITESHEET(String filename, Color colour, int priority)
		{
			this.filename = filename;
			this.colour = colour;
			this.priority = priority;
		}

		@Override
		public int compareTo(SPRITESHEET a) {
			return a.priority - priority;
		}
	}
}
