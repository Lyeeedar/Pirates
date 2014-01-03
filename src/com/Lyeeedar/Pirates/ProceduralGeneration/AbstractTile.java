package com.Lyeeedar.Pirates.ProceduralGeneration;

import com.badlogic.gdx.math.Vector3;

public class AbstractTile {
	
	float x, y;
	float height;
	
	float gn, gs, ge, gw;
	
	Landmark landmark;
	boolean path;
	boolean gate;
	
	float ground = 0.0f;
	
	float[] slope = new float[4];
	
	public static class AbstractTileHeuristic implements AStarHeuristic<AbstractTile>
	{
		@Override
		public int getHeuristic(AbstractTile current, AbstractTile previous, AbstractTile dst, int[] currentPos, int[] endPos, int distance)
		{
			return (int) (Vector3.dst(currentPos[0], currentPos[1], 0, endPos[0], endPos[1], 0)*2 + distance + Math.abs(current.height-previous.height));
		}
	}
	
}
