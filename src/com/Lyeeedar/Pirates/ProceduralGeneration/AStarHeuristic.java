package com.Lyeeedar.Pirates.ProceduralGeneration;

public interface AStarHeuristic<E> {
	public abstract int getHeuristic(E current, E dst, int[] currentPos, int[] endPos, int distance);
}
