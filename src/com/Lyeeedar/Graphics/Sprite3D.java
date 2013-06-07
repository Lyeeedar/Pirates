package com.Lyeeedar.Graphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.AnimationData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.ImageUtils;
import com.Lyeeedar.Util.Informable;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

public class Sprite3D implements Renderable {
	
	private static final String FILE_PREFIX = "data/sprites/";
	private static final String FILE_SEPERATOR = ".";
	private static final String FILE_SUFFIX = ".png";
	
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
	
	private static final short NUM_ANIMS = 8;
	private static final short NUM_FRAMES = 8;

	private final HashMap<SpriteLayer, SortedSet<SPRITESHEET>> layers;
	
	private final List<String> animations;
	
	private String gender;
	
	private final HashMap<String, Texture> spritesheet;
	private float width;
	private final short spriteWidth = 1024/NUM_FRAMES;
	private float height;
	private final short spriteHeight = 1024/NUM_ANIMS;
	
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
	private boolean useDirection = true;
	private String currentAnimation = "";
	
	private final Vector3 tmp = new Vector3();
	private boolean update = false;	
	
	private boolean lock = false;
	private byte nextAnimation;
	private String nextAnim;
	private boolean animateStore;
	private boolean directionStore;
	private byte endFrame;
	private Informable informable;
	
	private final PositionalData pData = new PositionalData();
	private final AnimationData aData = new AnimationData();

	public Sprite3D(float width, float height)
	{
		this.width = width;
		this.height = height;
		
		layers = new HashMap<SpriteLayer, SortedSet<SPRITESHEET>>();
		
		for (SpriteLayer sl : SpriteLayer.values())
		{
			layers.put(sl, new TreeSet<SPRITESHEET>());
		}
		
		animations = new ArrayList<String>();
		spritesheet = new HashMap<String, Texture>();
	}
	
	@Override
	public void queue(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {
		decalBatch.add(decal);
		
	}

	@Override
	public void set(Entity source) {
		
		source.readData(pData, PositionalData.class);
		source.readData(aData, AnimationData.class);
		
		setPosition(pData.position);
		setRotation(pData.rotation);
		if (aData.updateAnimations){
			playAnimationLoop(aData.anim, aData.animation, aData.useDirection);
			setAnimation(aData.animate, aData.animate_speed);
		}
		if (aData.animationLock)
		{
			playAnimationSingle(aData.playAnim, aData.playAnimation, aData.nextAnim, aData.nextAnimation, aData.startFrame, aData.endFrame, aData.informable);
		}
	}
	
	public void addAnimation(String animation)
	{
		this.animations.add(animation);
	}
	public boolean removeAnimation(String animation)
	{
		for (int i = 0; i < animations.size(); i++)
		{
			if (animations.get(i).equals(animation))
			{
				animations.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void setGender(boolean male)
	{
		if (male) gender = "male";
		else gender = "female";
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
		for (Map.Entry<String, Texture> entry : spritesheet.entrySet())
		{
			entry.getValue().dispose();
		}
		spritesheet.clear();
		
		for (String anim : animations)
		{
			Pixmap sprite = null;
			SortedSet<SPRITESHEET> layer = layers.get(SpriteLayer.BODY);
			for (SPRITESHEET s : layer)
			{
				if (sprite == null)
				{
					sprite = ImageUtils.copy(assetManager.get(FILE_PREFIX+s.filename+FILE_SEPERATOR+anim+FILE_SEPERATOR+gender+FILE_SUFFIX, Pixmap.class));
					ImageUtils.tint(sprite, s.colour);
				}
				else
				{
					merge(s, anim, sprite, assetManager);
				}
			}
			layer = layers.get(SpriteLayer.FEET);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			layer = layers.get(SpriteLayer.FACE);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			layer = layers.get(SpriteLayer.BOTTOM);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			layer = layers.get(SpriteLayer.TOP);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			layer = layers.get(SpriteLayer.HEAD);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			layer = layers.get(SpriteLayer.OTHER);
			for (SPRITESHEET s : layer)
			{
				merge(s, anim, sprite, assetManager);
			}
			
			spritesheet.put(anim, ImageUtils.PixmapToTexture(sprite));
		}
		
		region = new TextureRegion(spritesheet.get(animations.get(0)), 0, 0, spriteWidth, spriteHeight);
		decal = Decal.newDecal(width, height, region, true);
	}
	
	private final <T> Pixmap merge(SPRITESHEET ss, String anim, Pixmap sprite, AssetManager assetManager)
	{
		String filename = FILE_PREFIX+ss.filename+FILE_SEPERATOR+anim+FILE_SEPERATOR+gender+FILE_SUFFIX;
		if (assetManager.isLoaded(filename, Pixmap.class))
		{
			Pixmap pix = assetManager.get(filename, Pixmap.class);
			ImageUtils.tint(pix, ss.colour);
			return ImageUtils.merge(sprite, pix);
		}
		else
		{
			return sprite;
		}
	}
	
	public void setPosition(Vector3 position)
	{
		this.position.set(position.x, position.y+(height/2), position.z);
	}
	public void setRotation(Vector3 rotation)
	{
		this.rotation.set(rotation);
	}
	
	public void playAnimationLoop(String animationName, byte animation, boolean useDirection)
	{
		if (lock) return;
		
		this.animate = true;
		if (!currentAnimation.equals(animationName))
		{
			Texture texture = spritesheet.get(animationName);
			region.setTexture(texture);
			update = true;
		}
		if (this.animation != animation)
		{
			this.animation = animation;
			update = true;
		}
		this.useDirection = useDirection;
	}
	public void playAnimationSingle(String anim, byte animation, String nextAnim, byte nextAnimation, byte startFrame, byte endFrame, Informable informable)
	{
		if (lock) return;
		
		this.directionStore = this.useDirection;
		this.animateStore = this.animate;
		this.nextAnim = nextAnim;
		this.nextAnimation = nextAnimation;
		this.endFrame = endFrame;
		this.informable = informable;
		
		playAnimationLoop(anim, animation, false);
		this.frame = startFrame;
		
		lock = true;
	}
	private void finishAnimationSingle()
	{
		lock = false;
		informable.inform();
		frame = 0;
		playAnimationLoop(nextAnim, nextAnimation, directionStore);
		this.animate = animateStore;
		this.useDirection = directionStore;
	}
	
	public void setAnimation(boolean animate, float animate_speed)
	{
		if (lock) return;
		
		this.animationDelay = animate_speed;
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
			if (lock && frame == endFrame)
			{
				finishAnimationSingle();
			}
			else if (frame == NUM_FRAMES) {
				frame = 0;
			}
			update = true;
		}
		
		if (useDirection)
		{
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
		}
		else
		{
			direction = 0;
		}
		
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		decal.setPosition(position.x, position.y, position.z);
		
		if (update)
		{
			update = false;
			if (direction == 4){
				region.setRegion((frame+1)*spriteWidth, (animation+1)*spriteHeight, -spriteWidth, spriteHeight);
			}
			else {
				region.setRegion(frame*spriteWidth, (animation+direction)*spriteHeight, spriteWidth, spriteHeight);
			}
			decal.setTextureRegion(region);
		}
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
