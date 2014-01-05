/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.PriorityQueue;

public class AStarPathfind <E>
{
	private static final int[][] offsets = {
		{1, 0},
		{-1, 0},
		{0, 1},
		{0, -1}
	};
	
	private final E[][] grid;
	private final int[] endPos;
	private final int[] currentPos;
	private final Node[][] nodes;

	private final PriorityQueue<Node> openList = new PriorityQueue<Node>();
	
	private final AStarHeuristic<E> heuristicFunc;

	public AStarPathfind(E[][] grid, int startx, int starty, int endx, int endy, AStarHeuristic<E> heuristicFunc)
	{
		this.grid = grid;
		this.endPos = new int[]{endx, endy};
		this.currentPos = new int[]{startx, starty};

		this.heuristicFunc = heuristicFunc;

		nodes = new Node[grid.length][grid[0].length];

		addNodeToOpenList(new Node(startx, starty, 0, 0));

		while(nodes[endPos[0]][endPos[1]] == null)
		{
			path();
		}
	}

	public int[][] getPath()
	{
		int length = nodes[endPos[0]][endPos[1]].distance;
		int[][] path = new int[length+1][2];

		path[length][0] = endPos[0];
		path[length][1] = endPos[1];

		int cx = endPos[0];
		int cy = endPos[1];

		for (int i = length-1; i >= 0; i--)
		{
			if (cx-1 >= 0 && nodes[cx-1][cy] != null && nodes[cx-1][cy].distance == i)
			{
				cx--;
			}
			else if (cx+1 < grid.length && nodes[cx+1][cy] != null && nodes[cx+1][cy].distance == i)
			{
				cx++;
			}
			else if (cy-1 >= 0 && nodes[cx][cy-1] != null && nodes[cx][cy-1].distance == i)
			{
				cy--;
			}
			else if (cy+1 < grid[0].length && nodes[cx][cy+1] != null && nodes[cx][cy+1].distance == i)
			{
				cy++;
			}
			else
			{
				System.err.println("Failed to find a linking node when getting path! "+cx+", "+cy);
				break;
			}

			path[i][0] = cx;
			path[i][1] = cy;
		}

		return path;
	}

	private void path()
	{
		Node current = findBestNode();
		currentPos[0] = current.x;
		currentPos[1] = current.y;

		for (int[] offset : offsets)
		{
			int tempx = currentPos[0]+offset[0];
			int tempy = currentPos[1]+offset[1];
			if (
					tempx < 0 ||
					tempx == grid.length ||
					tempy < 0 ||
					tempy == grid[0].length
					)
			{
			}
			else
			{
				Node tempn = new Node(tempx, tempy, current.distance+1, heuristicFunc.getHeuristic(grid[tempx][tempy], grid[currentPos[0]][currentPos[1]], grid[endPos[0]][endPos[1]], currentPos, endPos, current.distance+1));
				addNodeToOpenList(tempn);
			}
		}
	}

	public void addNodeToOpenList(Node n)
	{
		if (nodes[n.x][n.y] != null && nodes[n.x][n.y].distance <= n.distance) return;
		openList.add(n);
		nodes[n.x][n.y] = n;
	}

	public Node findBestNode()
	{
		if (openList.size() == 0)
		{
			System.err.println("No nodes in list!");
			System.err.printf(" \n ");
		}
		Node n = null;
		while(n == null)
		{
			n = openList.poll();
			//if (nodes[n.x][n.y] != null && nodes[n.x][n.y].cost <= n.cost) n = null;
		}
		
		return n;
	}
	
	public void printGrid()
	{
		for (int x = 0; x < nodes.length; x++)
		{
			for (int z = 0; z < nodes[0].length; z++)
			{
				int i = (nodes[x][z] != null) ? nodes[x][z].distance : -1;
				System.out.print(i+"\t");
			}
			System.out.print("\n");
		}
		System.out.println("\n");
	}

	public static void main(String[] args)
	{
		int size = 20;
		AbstractTile[][] grid = new AbstractTile[size][size];
		
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				grid[x][y] = new AbstractTile();
				grid[x][y].x = x;
				grid[x][y].y = y;
			}
		}
		
		AStarPathfind<AbstractTile> pathFind = new AStarPathfind<AbstractTile>(grid, 1, 1, 18, 16, new AbstractTile.AbstractTileHeuristic());
		int[][] path = pathFind.getPath();
		
		int[][] pg = new int[size][size];
		for (int[] p : path)
		{
			pg[p[0]][p[1]] = 1;
		}
		
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				
				System.out.print(pg[x][y]+"\t");
			}
			System.out.print("\n");
		}
	}
}

class Node implements Comparable<Node>
{
	int x;
	int y;
	int cost;
	int distance;

	public Node(int x, int y, int distance, int cost)
	{
		this.x = x;
		this.y = y;
		this.distance = distance;
		this.cost = cost;
	}

	@Override
	public int compareTo(Node n) {
		return cost-n.cost;
	}
}