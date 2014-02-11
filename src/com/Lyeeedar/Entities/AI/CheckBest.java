package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.badlogic.gdx.utils.Array;

public interface CheckBest
{
	public Entity checkBest(Array<Entity> entities, Action parent);
	
	public CheckBest copy();
	public void dispose();
	
	public static class CheckClosest implements CheckBest
	{
		private final PositionalData pData = new PositionalData();
		private final PositionalData pData2 = new PositionalData();
		
		@Override
		public Entity checkBest(Array<Entity> entities, Action parent)
		{
			Entity entity = (Entity) parent.getData("entity", null);
			
			entity.readData(pData);
			
			Entity closest = null;
			float dst = Float.MAX_VALUE;
			
			for (int i = 0; i < entities.size; i++)
			{
				Entity tmp = entities.get(i);

				tmp.readData(pData2);
				float tdst = pData.position.dst2(pData2.position);
				
				if (tdst < dst)
				{
					dst = tdst;
					closest = tmp;
				}
			}
			
			return closest;
		}

		@Override
		public CheckBest copy()
		{
			return new CheckClosest();
		}

		@Override
		public void dispose()
		{
			pData.dispose();
			pData2.dispose();
		}
		
	}
	
	public static class CheckHPThreshold implements CheckBest
	{
		protected final boolean greaterThan;
		protected final int value;
		
		private final StatusData sData = new StatusData();
		
		
		public CheckHPThreshold(boolean greaterThan, int value)
		{
			this.greaterThan = greaterThan;
			this.value = value;
		}

		@Override
		public Entity checkBest(Array<Entity> entities, Action parent)
		{			
			Entity best = null;
			float hp = -1;
			
			for (int i = 0; i < entities.size; i++)
			{
				Entity tmp = entities.get(i);
				
				tmp.readData(sData);
				
				float thp = ((float) sData.currentHealth / (float) sData.MAX_HEALTH) * 100;
				
				if (greaterThan)
				{
					if (thp < value) continue;
				}
				else
				{
					if (thp > value) continue;
				}
				
				if ((hp == -1) || (!greaterThan && thp < hp) || (greaterThan && thp > hp))
				{
					hp = thp;
					best = tmp;
				}
			}
			
			return best;
		}

		@Override
		public CheckBest copy()
		{
			return new CheckHPThreshold(greaterThan, value);
		}

		@Override
		public void dispose()
		{
			sData.dispose();
		}
	}
}
