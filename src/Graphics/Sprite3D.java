package Graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Pirates.GLOBALS;

public class Sprite3D {
	
	private static final short NUM_ANIMS = 4;
	
	private final Texture spritesheet;
	private final int numFrames;
	private final float width;
	private final int spriteWidth;
	private final float height;
	private final int spriteHeight;
	
	private final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
	private final Vector3 tmp = new Vector3();
	
	private final Decal decal;
	private final TextureRegion region;
	
	private final float animationDelay;
	private float animationCD = 0;
	private int frame = 0;
	private int animation = 0;

	public Sprite3D(Texture spritesheet, int numFrames, float width, float height, float animationDelay)
	{
		this.spritesheet = spritesheet;
		this.numFrames = numFrames;
		this.width = width;
		this.spriteWidth = spritesheet.getWidth()/numFrames;
		this.height = height;
		this.spriteHeight = spritesheet.getHeight()/NUM_ANIMS;
		this.animationDelay = animationDelay;
		
		this.region = new TextureRegion(spritesheet, 0, 0, spriteWidth, spriteHeight);
		this.decal = Decal.newDecal(width, height, region, true);
	}
	
	public void setPosition(Vector3 position)
	{
		decal.setPosition(position.x, position.y+(height/2), position.z);
	}
	
	public void setRotation(Vector3 rotation)
	{
		this.rotation.set(rotation);
	}

	public void update(float delta, Camera cam)
	{
		decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		
		animationCD -= delta;
		
		if (animationCD < 0)
		{
			animationCD = animationDelay;
			frame++;
			if (frame >= numFrames) frame = 0;
		}
		
		double angle = GLOBALS.angle(cam.direction, rotation, tmp);
		double abs_a = Math.abs(angle);

		if (abs_a < 60)
		{
			animation = 3;
		}
		else if (abs_a > 120)
		{
			animation = 0;
		}
		else if (angle < 0)
		{
			animation = 1;
		}
		else if (angle > 0)
		{
			animation = 2;
		}
		else
		{
			System.err.println("YOU FORGOT SOMETHING IN SPRITE3D CLASS!!!!!!   "+angle+"   "+abs_a);
		}
		
		region.setRegion(frame*spriteWidth, animation*spriteHeight, spriteWidth, spriteHeight);
		decal.setTextureRegion(region);
	}
	
	public void render(DecalBatch batch)
	{
		batch.add(decal);
	}
}
