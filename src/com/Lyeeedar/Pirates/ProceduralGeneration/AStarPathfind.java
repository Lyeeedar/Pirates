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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class AStarPathfind <E>
{
	Random ran = new Random();
	E[][] grid;
	int[] startPos;
	int[] endPos;
	int[] currentPos;
	Node[][] nodes;

	LinkedList<Node> openList = new LinkedList<Node>();
	HashSet<Node> closedList = new HashSet<Node>();
	
	AStarHeuristic<E> heuristicFunc;

	public AStarPathfind(E[][] grid, int startx, int starty, int endx, int endy, AStarHeuristic<E> heuristicFunc)
	{
		this.grid = grid;
		this.startPos = new int[]{startx, starty};
		this.endPos = new int[]{endPos[0], endPos[1]};
		this.currentPos = new int[]{startx, starty};

		this.heuristicFunc = heuristicFunc;

		nodes = new Node[grid.length][grid[0].length];

		nodes[startx][starty] = new Node(startx, starty, 0, heuristicFunc.getHeuristic(grid[startx][starty], grid[endPos[0]][endPos[1]], startPos, endPos, 0));
		openList.add(nodes[startx][starty]);

		while(currentPos[0] != endPos[0] || currentPos[1] != endPos[1])
		{
			path();
		}

		fillArray();
	}

	public int[][] getPath()
	{
		int length = nodes[endPos[0]][endPos[1]].distance+1;
		int[][] path = new int[length][2];

		path[length-1][0] = endPos[0];
		path[length-1][1] = endPos[1];

		int cx = endPos[0];
		int cy = endPos[1];

		for (int i = length-1; i > 0; i--)
		{
			if (cx-1 > 0 && nodes[cx-1][cy] != null && nodes[cx-1][cy].distance <= i)
			{
				cx--;
			}
			else if (cx+1 < grid.length && nodes[cx+1][cy] != null && nodes[cx+1][cy].distance <= i)
			{
				cx++;
			}
			else if (cy-1 > 0 && nodes[cx][cy-1] != null && nodes[cx][cy-1].distance <= i)
			{
				cy--;
			}
			else if (cy+1 < grid[0].length && nodes[cx][cy+1] != null && nodes[cx][cy+1].distance <= i)
			{
				cy++;
			}

			path[i-1][0] = cx;
			path[i-1][1] = cy;
		}

		return path;
	}

	public void fillArray()
	{
		for (Node n : closedList)
		{
			nodes[n.x][n.y] = n;
		}
	}
	
	int[][] offsets = {
		{1, 0},
		{-1, 0},
		{0, 1},
		{0, -1}
	};

	private void path()
	{
		Node current = findBestNode();
		currentPos[0] = current.x;
		currentPos[1] = current.y;

		for (int[] offset : offsets)
		{
			if (
					currentPos[0]-2 <= 0 ||
					currentPos[0]+1 >= grid.length ||
					currentPos[1]-2 <= 0 ||
					currentPos[1]+1 >= grid[0].length
					)
			{
			}
			else
			{
				int tempx = currentPos[0]+offset[0];
				int tempy = currentPos[1]+offset[1];
		
				Node tempn = new Node(tempx, tempy, current.distance+1, heuristicFunc.getHeuristic(grid[tempx][tempy], grid[endPos[0]][endPos[1]], currentPos, endPos, current.distance+1));
				addNodeToOpenList(tempn);
			}
		}
	}

	public boolean isNodeInClosedList(Node n)
	{
		return closedList.contains(n);
	}

	public void addNodeToOpenList(Node n)
	{
		if (isNodeInClosedList(n)) return;

		if (openList.size() == 0)
		{
			openList.add(n);
			return;
		}
		Node less = openList.get(0);

		for (int i = 0; i < openList.size(); i++)
		{
			if (n.cost < less.cost)
			{
				openList.add(i, n);
				return;
			}
		}
		openList.add(n);
	}

	public void removeNodeFromOpenList(Node n)
	{
		openList.remove(n);
	}

	public Node findBestNode()
	{
		if (openList.size() == 0)
		{
			System.err.println("No nodes in list!");
			for (Node n : closedList)
			{	
				System.err.printf(" %d %d ", n.x, n.y);
			}
			System.err.printf(" \n ");
		}
		Node best = openList.get(0);
		openList.remove(0);
		closedList.add(best);
		return best;
	}
}

class Node
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
}
