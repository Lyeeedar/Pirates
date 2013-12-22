package com.Lyeeedar.Util;

import java.util.ArrayList;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class Dialogue {
	
	public enum DialogueAction {
		TEXT3D,
		TEXT2D,
		INPUT,
		WAIT
	};
	
	Entity[] entities;
	Informable inform;
	Object[] actionTree;
	Object[] currentAction;
	
	Decal decal;
	String text;
	Entity current;
	
	SpriteBatch sB = new SpriteBatch();
	BitmapFont font = new BitmapFont(true);
	
	PositionalData pData = new PositionalData();
	
	float waitTime = 0;
	
	public Dialogue(Entity[] entities, Object[] actionTree)
	{
		this.entities = entities;
		this.actionTree = actionTree;
		this.currentAction = actionTree;
		
		font = FileUtils.getFont("data/skins/parchment.ttf", (int) GLOBALS.sclX(20), true);
		font.setColor(Color.BLACK);
	}
	
	public void setInform(Informable inform)
	{
		this.inform = inform;
	}
	
	public void queue3D(DecalBatch batch) 
	{
		if (decal != null)
		{
			batch.add(decal);
		}
	}
	
	public void queue2D(SpriteBatch batch) 
	{
		if (decal == null)
		{
			font.draw(batch, text, 40, 40);
		}
	}
	
	public void update(float delta, Camera cam)
	{
		if (currentAction == null)
		{
			if (inform != null) 
			{
				inform.inform();
				inform = null;
			}
			GLOBALS.DIALOGUES.remove(this);
			currentAction = actionTree;
			return;
		}
		
		switch ((DialogueAction)currentAction[0]) {
		case INPUT:
			INPUT();
			break;
		case TEXT2D:
			TEXT2D();
			break;
		case TEXT3D:
			TEXT3D();
			break;
		case WAIT:
			WAIT();
			break;
		default:
			break;
		};
		
		if (waitTime != Float.MAX_VALUE) waitTime -= delta;
		
		if (decal != null)
		{
			current.readData(pData, PositionalData.class);
			decal.setPosition(pData.position.x, pData.position.y+2, pData.position.z);
			decal.setRotation(cam.direction, GLOBALS.DEFAULT_UP);
		}
	}
		
	public void dispose()
	{
			
	}
	
	public void TEXT3D()
	{
		String text = (String) currentAction[1];
		NinePatch np = new NinePatch(FileUtils.loadTexture("data/skins/texbox.png", true), 11, 49, 14, 48);
		decal = ImageUtils.getTextDecal(0, 0, sB, font, np, text);
		current = entities[(Integer) currentAction[2]];
		currentAction = (Object[]) currentAction[3];
	}

	public void TEXT2D()
	{
		text = (String) currentAction[1];
		decal = null;
		currentAction = (Object[]) currentAction[2];
	}
	
	public void INPUT()
	{
		for (int i = 1; i < currentAction.length; i+=2)
			if (Gdx.input.isKeyPressed(((Number)currentAction[i]).intValue())) 
			{
				currentAction = (Object[]) currentAction[i+1];
				break;
			}
	}
	
	public void WAIT()
	{
		if (waitTime < 0) 
		{
			currentAction = (Object[]) currentAction[2];
			waitTime = Float.MAX_VALUE;
		}
		else if (waitTime == Float.MAX_VALUE)
		{
			waitTime = ((Number) currentAction[1]).floatValue();
		}
	}
}
