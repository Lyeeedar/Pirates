package Entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

public class NavMesh {

	public final NavMeshNode[] nodes;
	
	public final Vector3 position = new Vector3();
	public final Vector3 rotation = new Vector3();
	
	public final NavMeshPartition partition;
	
	public NavMesh(NavMeshNode[] nodes, Vector3 position, Vector3 rotation)
	{
		this.nodes = nodes;
		
		this.position.set(position);
		this.rotation.set(rotation);
		
		partition = new NavMeshPartition(null, nodes);
	}
	
	public NavMeshNode getClosestNode(Vector3 pos)
	{
		return getClosestNode(pos.x, pos.y, pos.z);
	}
	
	public NavMeshNode getClosestNode(float x, float y, float z)
	{
		NavMeshPartition nmp = partition.getPartition(x, z);
		
		if (nmp == null) return null;
		
		Vector3 tmp = Pools.obtain(Vector3.class).set(x, y, z);
		
		NavMeshNode node = nmp.nodes[0];
		float dist2 = tmp.dst2(nmp.nodes[0].x, nmp.nodes[0].y, nmp.nodes[0].z);
		
		for (NavMeshNode n : nmp.nodes)
		{
			if (tmp.dst2(n.x, n.y, n.z) < dist2)
			{
				node = n;
				dist2 = tmp.dst2(n.x, n.y, n.z);
			}
		}
		
		Pools.free(tmp);
		
		return node;
	}
	
	/**
	 * Only works for triangular polygon meshes (with indices specifiying triangles in groups of 3)
	 * @param mesh
	 * @return
	 */
	public static NavMesh getNavMesh(Mesh mesh, float maxArea)
	{
		final VertexAttributes attributes = mesh.getVertexAttributes();
		final int positionOffset = attributes.getOffset(Usage.Position);
		final int vertCount = mesh.getNumVertices();
		final int vertexSize = attributes.vertexSize / 4;
		final int triangles = mesh.getNumIndices() / 3;
		
		final float[] verts = new float[vertexSize * vertCount];
		mesh.getVertices(verts);	
		final short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);
		
		final NavMeshNode[] vertexNodes = new NavMeshNode[vertCount];
		
		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new NavMeshNode(
					verts[(i*vertexSize)+positionOffset+0],
					verts[(i*vertexSize)+positionOffset+1],
					verts[(i*vertexSize)+positionOffset+2]
					);
		}
		
		final List<NavMeshNode> nodeList = new ArrayList<NavMeshNode>();
		
		for (int i = 0; i < triangles; i++)
		{
			final NavMeshNode n1 = vertexNodes[indices[(i*3)+0]];
			final NavMeshNode n2 = vertexNodes[indices[(i*3)+1]];
			final NavMeshNode n3 = vertexNodes[indices[(i*3)+2]];
			
			if (getAngle(n1, n2) > 45 ||
					getAngle(n1, n3) > 45 ||
					getAngle(n2, n3) > 45)
			{
				continue;
			}
			
			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);
			
			nodeList.add(n1);
			nodeList.add(n2);
			nodeList.add(n3);
			nodeList.addAll(subdivideTriangle(n1, n2, n3, maxArea));
		}
		
		final NavMeshNode[] nodes = new NavMeshNode[nodeList.size()];
		nodeList.toArray(nodes);
		
		return new NavMesh(nodes, new Vector3(), new Vector3());
	}
	
	private static float getAngle(NavMeshNode n1, NavMeshNode n2)
	{
		double O = Math.abs(n1.y-n2.y);
		double H = Math.sqrt(Math.pow(n1.x-n2.x, 2) + Math.pow(n1.z-n2.z, 2));
		
		return (float) Math.sin(O/H);
	}
	
	private static List<NavMeshNode> subdivideTriangle(NavMeshNode n1, NavMeshNode n2, NavMeshNode n3, float targetArea)
	{
		Vector3 AB = Pools.obtain(Vector3.class).set(n1.x, n1.y, n1.z).sub(n2.x, n2.y, n2.z);
		Vector3 AC = Pools.obtain(Vector3.class).set(n1.x, n1.y, n1.z).sub(n3.x, n3.y, n3.z);
		
		float area = 0.5f*AB.crs(AC).dst(0, 0, 0);
		
		List<NavMeshNode> nodes = new ArrayList<NavMeshNode>();
		if (area > targetArea)
		{
			NavMeshNode n4 = new NavMeshNode(
					(n1.x+n2.x+n3.x)/3,
					(n1.y+n2.y+n3.y)/3,
					(n1.z+n2.z+n3.z)/3
					);
			
			n4.addNodes(n1, n2, n3);
			n1.addNodes(n4);
			n2.addNodes(n4);
			n3.addNodes(n4);
			
			nodes.add(n4);
			nodes.addAll(subdivideTriangle(n1, n3, n4, targetArea));
			nodes.addAll(subdivideTriangle(n1, n2, n4, targetArea));
			nodes.addAll(subdivideTriangle(n2, n3, n4, targetArea));
		}
		
		Pools.free(AB);
		Pools.free(AC);
		
		return nodes;
	}
	
	public static class NavMeshNode
	{
		public final float x;
		public final float y;
		public final float z;
		
		public final List<NavMeshNode> connectedNodes;
		
		public NavMeshNode(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			
			connectedNodes = new ArrayList<NavMeshNode>();
		}
		
		public void addNodes(NavMeshNode... nodes)
		{
			for (NavMeshNode n : nodes)
			{
				connectedNodes.add(n);
			}
		}
	}
	
	public static class NavMeshPartition
	{
		public final float minx;
		public final float minz;
		
		public final float maxx;
		public final float maxz;
		
		public final NavMeshNode[] nodes;
		
		public final NavMeshPartition parent;
		
		public final NavMeshPartition top_left;
		public final NavMeshPartition top_right;
		public final NavMeshPartition bot_left;
		public final NavMeshPartition bot_right;
		
		public NavMeshPartition(NavMeshPartition parent, NavMeshNode[] nodes)
		{
			this.nodes = nodes;
			
			float minx = nodes[0].x;
			float minz = nodes[0].z;
			
			float maxx = nodes[0].x;
			float maxz = nodes[0].z;
			
			for (NavMeshNode node : nodes)
			{
				if (node.x < minx) minx = node.x;
				if (node.z < minz) minz = node.z;
				
				if (node.x > maxx) maxx = node.x;
				if (node.z > maxz) maxz = node.z;
			}
			
			this.minx = minx;
			this.minz = minz;
			this.maxx = maxx;
			this.maxz = maxz;
			
			this.parent = parent;
			
			if (nodes.length < 4)
			{
				top_left = null;
				top_right = null;
				bot_left = null;
				bot_right = null;
			}
			else
			{
				final float midx = minx+(maxx-minx);
				final float midz = minz+(maxz-minz);
				
				NavMeshNode[] n = null;
				
				n = getPartitionedNodes(nodes, minx, midz, midx, maxz);
				top_left = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, midz, maxx, maxz);
				top_right = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, minx, minz, midx, midz);
				bot_left = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, minz, maxx, midz);
				bot_right = (n.length == 0) ? null : new NavMeshPartition(this, n);
			}
		}
		
		private NavMeshNode[] getPartitionedNodes(NavMeshNode[] nodes, float minx, float minz, float maxx, float maxz)
		{
			List<NavMeshNode> nodeList = new ArrayList<NavMeshNode>();
			
			for (NavMeshNode node : nodes)
			{
				if (node.x > minx && node.x < maxx && 
						node.z < minz && node.z > maxz)
				{
					nodeList.add(node);
				}
			}
			
			NavMeshNode[] finalNodes = new NavMeshNode[nodeList.size()];
			nodeList.toArray(finalNodes);
			
			return finalNodes;
		}
		
		public NavMeshPartition getPartition(float x, float z)
		{
			if (x < minx || x > maxx || z < minz || z > maxz) return null;
			else if (top_left != null && x > top_left.minx && x < top_left.maxx &&
					z > top_left.minz && z < top_left.maxz)
			{
				return top_left.getPartition(x, z);
			}
			else if (top_right != null && x > top_right.minx && x < top_right.maxx &&
					z > top_right.minz && z < top_right.maxz)
			{
				return top_right.getPartition(x, z);
			}
			else if (bot_left != null && x > bot_left.minx && x < bot_left.maxx &&
					z > bot_left.minz && z < bot_left.maxz)
			{
				return bot_left.getPartition(x, z);
			}
			else if (bot_right != null && x > bot_right.minx && x < bot_right.maxx &&
					z > bot_right.minz && z < bot_right.maxz)
			{
				return bot_right.getPartition(x, z);
			}
			else return this;
		}
	}
}
