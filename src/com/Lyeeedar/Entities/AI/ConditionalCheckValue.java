package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.AI.BehaviourTree.BehaviourTreeState;
import com.Lyeeedar.Entities.AI.BehaviourTree.Conditional;

public class ConditionalCheckValue extends Conditional
{

	public final String key;
	public final Object value;

	public final BehaviourTreeState succeed;
	public final BehaviourTreeState fail;
	
	public ConditionalCheckValue(String key, Object value, BehaviourTreeState succeed, BehaviourTreeState fail)
	{
		this.key = key;
		this.value = value;
		this.succeed = succeed;
		this.fail = fail;
	}

	@Override
	public BehaviourTreeState evaluate()
	{
		Object storedValue = getData(key, null);
		
		if (storedValue == null) state = fail;
		else state = storedValue.equals(value) ? succeed : fail ;
		
		return state;
	}

	@Override
	public void cancel()
	{
		
	}

	@Override
	public Conditional copy()
	{
		return new ConditionalCheckValue(key, value, succeed, fail);
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
		
	}

}
