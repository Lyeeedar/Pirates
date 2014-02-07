package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;

public interface DoAction
{
	public BehaviourTreeState doAction(Entity entity, Action parent);
	
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
	}
	
	public static class DoNothing implements DoAction
	{

		@Override
		public BehaviourTreeState doAction(Entity entity, Action parent)
		{
			return BehaviourTreeState.FINISHED;
		}
		
	}
}
