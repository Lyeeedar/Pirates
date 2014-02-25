package com.Lyeeedar.Entities.Items;

import com.badlogic.gdx.utils.Pools;

public class Item {
		
	public enum ITEM_TYPE
	{
		ARMOUR_HEAD,
		ARMOUR_TORSO,
		ARMOUR_LEGS,
		ARMOUR_FEET,
		
		WEAPON_MUNDANE,
		WEAPON_TEMPERATURE,
		WEAPON_LIGHT,
		WEAPON_LIFE,
		WEAPON_GAIA,
		WEAPON_FORCE,
		
		BUFF_TEMPERATURE,
		BUFF_LIGHT,
		BUFF_LIFE,
		BUFF_GAIA,
		BUFF_FORCE,
		
		MISC
	}

	public DESCRIPTION description;
	public int num = 1; 
	public float dropRate = 0;
	
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
	
	public static final class DESCRIPTION
	{
		public String icon;
		public String name;
		public String description;
		public ITEM_TYPE item_type;
		
		public DESCRIPTION(String name, String description, ITEM_TYPE item_type, String icon)
		{
			this.icon = icon;
			this.name = name;
			this.description = description;
			this.item_type = item_type;
		}
	}
}
