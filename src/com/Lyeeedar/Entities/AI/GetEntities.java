package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Collision.Octtree;
import com.Lyeeedar.Collision.Octtree.OcttreeShape;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.AI.BehaviourTree.Action;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.badlogic.gdx.utils.Array;

public interface GetEntities
{
	public Array<Entity> getEntities(Action parent);
	
	public GetEntities copy();
	public void dispose();
	
	public static class GetAll implements GetEntities
	{
		protected final OcttreeShape shape;
		
		private final PositionalData pData = new PositionalData();
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		
		private final Array<Entity> entities = new Array<Entity>(false, 16);
		
		public GetAll(OcttreeShape shape)
		{
			this.shape = shape;
		}

		@Override
		public Array<Entity> getEntities(Action parent)
		{
			Entity entity = (Entity) parent.getData("entity", null);
			
			entity.readData(pData);
			entity.readData(sData);
			
			shape.setPosition(pData.position);
			shape.setRotation(pData.rotation);
			
			entities.clear();
			GLOBALS.renderTree.collectAll(entities, shape, Octtree.MASK_ENTITY);
			if (entities.size == 0)
			{
				return entities;
			}
			
			for (int i = 0; i < entities.size; i++)
			{
				Entity tmp = entities.get(i);
				if (tmp == entity)
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
				
				tmp.readData(sData2);
				
				if (!sData2.ALIVE)
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
			}

			return entities;
		}

		@Override
		public GetEntities copy()
		{
			return new GetAll(shape);
		}

		@Override
		public void dispose()
		{
			pData.dispose();
			sData.dispose();
			sData2.dispose();
		}
		
	}
	
	public static class GetAllies implements GetEntities
	{
		protected final OcttreeShape shape;
		
		private final PositionalData pData = new PositionalData();
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		
		private final Array<Entity> entities = new Array<Entity>(false, 16);
		
		public GetAllies(OcttreeShape shape)
		{
			this.shape = shape;
		}

		@Override
		public Array<Entity> getEntities(Action parent)
		{
			Entity entity = (Entity) parent.getData("entity", null);
			
			entity.readData(pData);
			entity.readData(sData);
			
			shape.setPosition(pData.position);
			shape.setRotation(pData.rotation);
			
			entities.clear();
			GLOBALS.renderTree.collectAll(entities, shape, Octtree.MASK_ENTITY);
			if (entities.size == 0)
			{
				return entities;
			}
			
			for (int i = 0; i < entities.size; i++)
			{
				Entity tmp = entities.get(i);
				if (tmp == entity)
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
				
				tmp.readData(sData2);
				
				if (!sData2.ALIVE || sData2.factions.size == 0 || !sData.isAlly(sData2))
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
			}

			return entities;
		}

		@Override
		public GetEntities copy()
		{
			return new GetAllies(shape);
		}

		@Override
		public void dispose()
		{
			pData.dispose();
			sData.dispose();
			sData2.dispose();
		}
		
	}
	
	public static class GetEnemies implements GetEntities
	{
		protected final OcttreeShape shape;
		
		private final PositionalData pData = new PositionalData();
		private final StatusData sData = new StatusData();
		private final StatusData sData2 = new StatusData();
		
		private final Array<Entity> entities = new Array<Entity>(false, 16);
		
		public GetEnemies(OcttreeShape shape)
		{
			this.shape = shape;
		}

		@Override
		public Array<Entity> getEntities(Action parent)
		{
			Entity entity = (Entity) parent.getData("entity", null);
			
			entity.readData(pData);
			entity.readData(sData);
			
			shape.setPosition(pData.position);
			shape.setRotation(pData.rotation);
			
			entities.clear();
			GLOBALS.renderTree.collectAll(entities, shape, Octtree.MASK_ENTITY);
			if (entities.size == 0)
			{
				return entities;
			}
			
			for (int i = 0; i < entities.size; i++)
			{
				Entity tmp = entities.get(i);
				if (tmp == entity)
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
				
				tmp.readData(sData2);
				
				if (!sData2.ALIVE || sData2.factions.size == 0 || sData.isAlly(sData2))
				{
					entities.removeIndex(i);
					i--;
					continue;
				}
			}

			return entities;
		}
		
		@Override
		public GetEntities copy()
		{
			return new GetEnemies(shape);
		}

		@Override
		public void dispose()
		{
			pData.dispose();
			sData.dispose();
			sData2.dispose();
		}
		
	}
	
	public static class GetSelf implements GetEntities
	{
		private final Array<Entity> entities = new Array<Entity>(false, 16);
		
		@Override
		public Array<Entity> getEntities(Action parent)
		{
			Entity entity = (Entity) parent.getData("entity", null);
			
			entities.clear();			
			entities.add(entity);
			
			return entities;
		}

		@Override
		public GetEntities copy()
		{
			return new GetSelf();
		}

		@Override
		public void dispose()
		{
			
		}
		
	}
}