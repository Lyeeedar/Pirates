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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class AStarPathfind
{
	Random ran = new Random();
	AbstractTile[][] grid;
	int startx;
	int starty;
	int endx;
	int endy;
	int currentx;
	int currenty;
	Node[][] nodes;

	LinkedList<Node> openList = new LinkedList<Node>();
	HashSet<Node> closedList = new HashSet<Node>();

	public AStarPathfind(AbstractTile[][] grid, int startx, int starty, int endx, int endy)
	{
		this.grid = grid;
		this.startx = startx;
		this.starty = starty;
		this.currentx = startx;
		this.currenty = starty;
		this.endx = endx;
		this.endy = endy;

		nodes = new Node[grid.length][grid.length];

		nodes[startx][starty] = new Node(startx, starty, 0, 0, 0, grid[startx][starty].height);
		openList.add(nodes[startx][starty]);

		while(currentx != endx || currenty != endy)
		{
			path();
		}

		fillArray();
	}

	public int[][] getPath()
	{
		int length = nodes[endx][endy].distance+1;
		int[][] path = new int[length][2];

		path[length-1][0] = endx;
		path[length-1][1] = endy;

		int cx = endx;
		int cy = endy;

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
			else if (cy+1 < grid.length && nodes[cx][cy+1] != null && nodes[cx][cy+1].distance <= i)
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
		currentx = current.x;
		currenty = current.y;

		for (int[] offset : offsets)
		{
			if (
					currentx-2 <= 0 ||
					currentx+1 >= grid.length ||
					currenty-2 <= 0 ||
					currenty+1 >= grid.length
					)
			{
			}
			else
			{
				int tempx = currentx+offset[0];
				int tempy = currenty+offset[1];
	
				int heuristic = (int) (Math.pow(Math.abs(tempx-endx), 2)+Math.pow(Math.abs(tempy-endy), 2));
				
				if (grid[tempx][tempy].height < 10) heuristic *= 100;
				
				Node tempn = new Node(tempx, tempy, heuristic, current.distance+1, (int) Math.pow(grid[tempx][tempy].height-current.height, 3), grid[tempx][tempy].height);
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

	class Node
	{
		int x;
		int y;
		int cost;
		int heuristic;
		int distance;
		int influence;
		float height;

		public Node(int x, int y, int heuristic, int distance, int influence, float height)
		{
			this.height = height;
			this.influence = influence;
			this.x = x;
			this.y = y;
			this.heuristic = heuristic;
			this.distance = distance;
			this.cost = heuristic + distance + influence;
		}
	}

}
