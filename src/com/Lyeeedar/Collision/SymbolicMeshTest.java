package com.Lyeeedar.Collision;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.Lyeeedar.Collision.SymbolicMesh.SymbolicMeshPartition;
import com.Lyeeedar.Collision.SymbolicMesh.TempMeshPartition;

public class SymbolicMeshTest {
	Random ran = new Random();

	public void subdivide(TempMeshPartition parent)
	{
		TempMeshPartition[] children = new TempMeshPartition[8];
		
		ArrayList<float[]> x = new ArrayList<float[]>();
			x.add(new float[]{parent.minx, parent.midx});
			x.add(new float[]{parent.midx, parent.maxx});
		
		ArrayList<float[]> y = new ArrayList<float[]>();
			y.add(new float[]{parent.miny, parent.midy});
			y.add(new float[]{parent.midy, parent.maxy});
		
		ArrayList<float[]> z = new ArrayList<float[]>();
			z.add(new float[]{parent.minz, parent.midz});
			z.add(new float[]{parent.midz, parent.maxz});
		
		ArrayList<ArrayList<float[]>> all = parent.powerset(x, y, z);
		
		for (int i = 0; i < all.size(); i++)
		{
			ArrayList<float[]> division = all.get(i);
			children[i] = new TempMeshPartition(parent, division.get(0)[0], division.get(0)[1], division.get(1)[0], division.get(1)[1], division.get(2)[0], division.get(2)[1], (byte) (parent.depth+1), parent.tris);
		}
		
		parent.children = children;
		
		if (parent.depth < 5 && ran.nextInt(5) != 2)
		{
			for (TempMeshPartition p : children)
			{
				subdivide(p);
			}
		}
	}
	
	@Test
	public void testPartitionSearch() {
		TempMeshPartition root = new TempMeshPartition(null, 0f, 100f, 0f, 100f, 0f, 100f, (byte) (0), null);
		subdivide(root);
		
		ArrayList<TempMeshPartition> leafs = new ArrayList<TempMeshPartition>();
		root.gatherAll(leafs);
		
		CollisionRay ray = new CollisionRay();
		for (TempMeshPartition p : leafs)
		{
			ray.len = p.maxy-p.miny-0.2f;
			ray.ray.direction.set(0, -1, 0);
			ray.ray.origin.set(p.midx, p.maxy-0.1f, p.midz);
			ray.reset();
			
			TempMeshPartition intersect = p.getPartition(ray);
			
			assertTrue(intersect == p);
		}
		
		SymbolicMeshPartition sroot = new SymbolicMeshPartition(null, root);
		ArrayList<SymbolicMeshPartition> leafss = new ArrayList<SymbolicMeshPartition>();
		sroot.gatherAll(leafss);
		
		for (SymbolicMeshPartition p : leafss)
		{
			ray.len = p.maxy-p.miny-0.2f;
			ray.ray.direction.set(0, -1, 0);
			ray.ray.origin.set(p.midx, p.maxy-0.1f, p.midz);
			ray.reset();
			
			SymbolicMeshPartition intersect = p.getPartition(ray);
			
			assertTrue(intersect == p);
		}
	}

}
