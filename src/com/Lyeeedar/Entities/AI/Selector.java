package com.Lyeeedar.Entities.AI;

import java.util.Random;

import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeNode;
import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.badlogic.gdx.utils.Array;

public abstract class Selector extends BehaviourTreeNode
{
	public Selector()
	{
		super();
	}

	protected final Array<BehaviourTreeNode> nodes = new Array<BehaviourTreeNode>();
	public void addNode(BehaviourTreeNode node, int priority)
	{
		node.priority = priority;
		node.parent = this;
		nodes.add(node);
		nodes.sort();
	}
	
	@Override
	public void setData(String key, Object value)
	{
		super.setData(key, value);
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).setData(key, value);
		}
	}
	
	@Override
	public void cancel()
	{
		for (int i = 0; i < nodes.size; i++)
		{
			nodes.get(i).cancel();
		}
	}
	
	public static class PrioritySelector extends Selector
	{
		public PrioritySelector()
		{
			super();
		}

		@Override
		public BehaviourTreeState evaluate()
		{
			BehaviourTreeState state = BehaviourTreeState.FAILED;
			
			int i = 0;
			for (; i < nodes.size; i++)
			{
				BehaviourTreeState temp = nodes.get(i).evaluate();
				if (temp != BehaviourTreeState.FAILED)
				{
					state = temp;
					break;
				}
			}
			i++;
			for (; i < nodes.size; i++)
			{
				nodes.get(i).cancel();
			}
			
			this.state = state;
			return state;
		}
	}
	
	public static class SequenceSelector extends Selector
	{

		public SequenceSelector()
		{
			super();
		}

		private int i = 0;
		
		@Override
		public BehaviourTreeState evaluate()
		{
			BehaviourTreeState state = BehaviourTreeState.FINISHED;
			
			for (; i < nodes.size; i++)
			{
				BehaviourTreeState temp = nodes.get(i).evaluate();
				if (temp != BehaviourTreeState.FINISHED)
				{
					state = temp;
					break;
				}
			}
			
			if (state != BehaviourTreeState.RUNNING)
			{
				i = 0;
				for (; i < nodes.size; i++)
				{
					nodes.get(i).cancel();
				}
				i = 0;
			}
			
			this.state = state;
			return state;
		}
		
	}
	
	public static class RandomSelector extends Selector
	{
		public RandomSelector(Random ran)
		{
			super();
			this.ran = ran;
		}

		private final Random ran;
		private int i = -1;
		
		@Override
		public BehaviourTreeState evaluate()
		{
			if (i == -1) i = ran.nextInt(nodes.size);
			BehaviourTreeState state = nodes.get(i).evaluate();
			
			if (state != BehaviourTreeState.RUNNING)
			{
				i = -1;
			}
			
			this.state = state;
			return state;
		}
		
	}
}
