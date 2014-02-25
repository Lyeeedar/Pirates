package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Items.Item;

public interface DoAction
{
	public BehaviourTreeState doAction(Entity entity, Action parent);
	public DoAction copy();
	public void dispose();
	
	public static class DoSetEntity implements DoAction
	{
		public final String key;
		
		public DoSetEntity(String key)
		{
			this.key = key;
		}

		@Override
		public BehaviourTreeState doAction(Entity entity, Action parent)
		{
			parent.parent.setDataTree(key, entity);
			
			return BehaviourTreeState.FINISHED;
		}

		@Override
		public DoAction copy()
		{
			return new DoSetEntity(key);
		}

		@Override
		public void dispose()
		{
			
		}
	}
	
	public static class DoGiveItem implements DoAction
	{
		Item item;
		
		public DoGiveItem(Item item)
		{
			this.item = item;
		}
		
		@Override
		public BehaviourTreeState doAction(Entity entity, Action parent)
		{
			entity.readOnlyRead(EquipmentData.class).addItem(item);
			
			return BehaviourTreeState.FINISHED;
		}

		@Override
		public DoAction copy()
		{
			return new DoGiveItem(item.copy());
		}

		@Override
		public void dispose()
		{
		}
		
	}
	
	public static class DoNothing implements DoAction
	{

		@Override
		public BehaviourTreeState doAction(Entity entity, Action parent)
		{
			return BehaviourTreeState.FINISHED;
		}

		@Override
		public DoAction copy()
		{
			return new DoNothing();
		}

		@Override
		public void dispose()
		{
			
		}
		
	}
}
