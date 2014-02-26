package com.Lyeeedar.Entities.Items;

import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Pools;

public class Item {
		
	public enum ITEM_TYPE
	{
		ARMOUR_HEAD,
		ARMOUR_TORSO,
		ARMOUR_LEGS,
		ARMOUR_FEET,
		
		WEAPON_MAIN,
		WEAPON_OFF,
		
		MISC
	}
	
	public static ITEM_TYPE fromEquipmentSlot(Equipment_Slot slot)
	{
		if (slot == Equipment_Slot.HEAD)
		{
			return ITEM_TYPE.ARMOUR_HEAD;
		}
		else if (slot == Equipment_Slot.TORSO)
		{
			return ITEM_TYPE.ARMOUR_TORSO;
		}
		else if (slot == Equipment_Slot.LEGS)
		{
			return ITEM_TYPE.ARMOUR_LEGS;
		}
		else if (slot == Equipment_Slot.FEET)
		{
			return ITEM_TYPE.ARMOUR_FEET;
		}
		
		else if (slot == Equipment_Slot.LARM || slot == Equipment_Slot.LARMOFF1 || slot == Equipment_Slot.LARMOFF2 || slot == Equipment_Slot.LARMOFF3)
		{
			return ITEM_TYPE.WEAPON_OFF;
		}
		else if (slot == Equipment_Slot.RARM || slot == Equipment_Slot.RARMOFF1 || slot == Equipment_Slot.RARMOFF2 || slot == Equipment_Slot.RARMOFF3)
		{
			return ITEM_TYPE.WEAPON_MAIN;
		}
		
		return ITEM_TYPE.MISC;
	}

	public DESCRIPTION description;
	public int num = 1; 
	public float dropRate = 0;
	public boolean stackable = true;
	
	public Item ()
	{
		
	}
	
	public Item(DESCRIPTION desc)
	{
		this.description = desc;
	}
	
	public Item set(Item item)
	{
		this.description = item.description;
		this.num = item.num;
		
		return this;
	}
	
	public Item copy()
	{
		return Pools.obtain(Item.class).set(this);
	}
	
	public Table getDescriptionSimple()
	{
		BitmapFont fonttitle = FileUtils.getFont("data/skins/arial.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle lstitle = new LabelStyle();
		lstitle.font = fonttitle;
		lstitle.fontColor = Color.WHITE;
		
		BitmapFont fontdesc = FileUtils.getFont("data/skins/arial.ttf", (int)GLOBALS.sclX(10), false);
		LabelStyle lsdesc = new LabelStyle();
		lsdesc.font = fontdesc;
		lsdesc.fontColor = Color.WHITE;
		
		Table table = new Table();
		table.debug();
		
		table.add(new Label(description.name, lstitle)).left().colspan(2);
		table.row();
		table.add(new Label(description.shortDesc, lsdesc)).left();
		table.add(new Table()).expandX().fillX();
		if (stackable) table.add(new Label("Stack: "+num, lsdesc)).right();
		
		return table;
	}
	
	public Table getDescriptionDetailed()
	{
		Table table = new Table();
		table.debug();
		return table;
	}
	
	public static final class DESCRIPTION
	{
		public String icon;
		public String name;
		public String shortDesc;
		public String longDesc;
		public ITEM_TYPE item_type;
		
		public DESCRIPTION(String name, String shortDesc, String longDesc, ITEM_TYPE item_type, String icon)
		{
			this.icon = icon;
			this.name = name;
			this.shortDesc = shortDesc;
			this.longDesc = longDesc;
			this.item_type = item_type;
		}
	}
}
