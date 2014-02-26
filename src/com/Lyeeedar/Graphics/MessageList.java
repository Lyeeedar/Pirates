package com.Lyeeedar.Graphics;

import java.util.ArrayDeque;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class MessageList
{
	public final ArrayDeque<Message> messageList = new ArrayDeque<Message>();
	public final float messageDuration;
	float cd = 0;
	public final int maxMessages;
	
	public MessageList(float messageDuration, int maxMessages)
	{
		this.messageDuration = messageDuration;
		this.maxMessages = maxMessages;
	}
	
	public void update(float delta)
	{
		cd += delta;
		while (cd > messageDuration)
		{
			cd -= messageDuration;
			if (messageList.size() > 0) messageList.removeLast();
		}
	}
	
	public void addMessage(Message message)
	{
		if (messageList.size() == maxMessages)
		{
			messageList.removeLast();
		}
		else if (messageList.size() == 0)
		{
			cd = 0;
		}
		messageList.addFirst(message);
	}
	
	public void render(SpriteBatch batch, BitmapFont font, int x, int y)
	{
		int yp = y;
		for (Message message : messageList)
		{
			for (Array<Text> line : message.lines)
			{
				int xp = x;
				for (Text text : line)
				{
					font.setColor(text.colour);
					font.draw(batch, text.text, xp, yp);
					xp += font.getBounds(text.text).width;
				}
				yp += font.getLineHeight();
			}
		}
	}
	
	public static class Message
	{
		Array<Array<Text>> lines = new Array<Array<Text>>();
		
		public Message()
		{
			
		}
		
		public Message(Text... text)
		{
			Array<Text> line = new Array<Text>();
			for (Text t : text)
			{
				line.add(t);
			}
			
			lines.add(line);
		}
		
		public void addLine(Text... text)
		{
			Array<Text> line = new Array<Text>();
			for (Text t : text) line.add(t);
			lines.add(line);
		}
	}
	
	public static class Text
	{
		String text;
		Color colour;
		
		public Text(String text, Color colour)
		{
			this.text = text;
			this.colour = colour;
		}
	}
}
