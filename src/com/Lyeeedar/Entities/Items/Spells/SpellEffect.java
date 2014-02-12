package com.Lyeeedar.Entities.Items.Spells;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.StatusData;

public abstract class SpellEffect
{
	public SpellPayload payload;
	
	public SpellEffect(SpellPayload payload)
	{
		this.payload = payload;
	}
	
	public abstract SpellEffect copy();
	public abstract boolean update(float delta, Entity target);	
	
	public static interface SpellPayload
	{
		public void apply(Entity target);
		public void remove(Entity target);
	}
	
	public static class SpellPayloadHP implements SpellPayload
	{
		public final int amount;
		private final StatusData sData = new StatusData();
		
		public SpellPayloadHP(int amount)
		{
			this.amount = amount;
		}
		
		@Override
		public void apply(Entity target)
		{
			target.readData(sData);
			sData.damage = amount;
			target.writeData(sData);
		}
		@Override
		public void remove(Entity target)
		{
			target.readData(sData);
			sData.damage = -amount;
			target.writeData(sData);
		}
	}
	
	public static class InstantSpellEffect extends SpellEffect
	{
		public InstantSpellEffect(SpellPayload payload)
		{
			super(payload);
		}

		@Override
		public boolean update(float delta, Entity target)
		{
			payload.apply(target);
			return false;
		}

		@Override
		public SpellEffect copy()
		{
			return new InstantSpellEffect(payload);
		}
		
	}	
	public static class RepeatingSpellEffect extends SpellEffect
	{
		private static final float TICK = 1.0f;
		
		public float tickCD = 0;
		public float duration;
		
		public RepeatingSpellEffect(SpellPayload payload, float duration)
		{
			super(payload);
			this.duration = duration;
		}

		
		@Override
		public boolean update(float delta, Entity target)
		{
			tickCD -= delta;
			duration -= delta;
			
			if (tickCD < 0)
			{
				payload.apply(target);
				tickCD = TICK;
			}
			
			return duration > 0;
		}


		@Override
		public SpellEffect copy()
		{
			return new RepeatingSpellEffect(payload, duration);
		}
		
	}	
	public static class DurationSpellEffect extends SpellEffect
	{
		public float duration;
		public boolean applied = false;
		
		public DurationSpellEffect(SpellPayload payload, float duration)
		{
			super(payload);
			this.duration = duration;
		}

		
		@Override
		public boolean update(float delta, Entity target)
		{
			if (!applied)
			{
				payload.apply(target);
			}
			
			duration -= delta;
			
			if (duration <= 0)
			{
				payload.remove(target);
			}
			
			return duration > 0;
		}
		
		@Override
		public SpellEffect copy()
		{
			return new DurationSpellEffect(payload, duration);
		}	
	}
}
