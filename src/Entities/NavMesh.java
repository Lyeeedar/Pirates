package Entities;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class NavMesh {

	public final NavMeshNode[] nodes;
	
	private final Vector3 position = new Vector3();
	private final Matrix4 rotation = new Matrix4();
	private final Matrix4 rotationTra = new Matrix4();
	private final Matrix4 combined = new Matrix4();
	private final Matrix4 inverse = new Matrix4();
	
	private final Matrix4 tmpMat = new Matrix4();
	private final Vector3 tmpVec = new Vector3();
	private final Ray tmpRay = new Ray(new Vector3(), new Vector3());
	
	public final NavMeshPartition partition;
	
	public NavMesh(NavMeshNode[] nodes)
	{
		this.nodes = nodes;
		
		partition = new NavMeshPartition(null, nodes);
	}
	
	public void updateMatrixes()
	{
		combined.setToTranslation(position).mul(rotation);
		inverse.set(rotationTra).mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
	}
	
	public Matrix4 getCombined()
	{
		return combined;
	}
	
	public void setPosition(Vector3 position)
	{
		setPosition(position.x, position.y, position.z);
	}
	public void setPosition(float x, float y, float z)
	{
		position.set(x, y, z);
	}
	
	public void setRotation(Vector3 base, Vector3 direction)
	{
		rotation.setToRotation(base, direction);
		rotationTra.set(rotation).tra();
	}
	
	public NavMeshNode getClosestNode(Vector3 pos)
	{
		return getClosestNode(pos.x, pos.y, pos.z);
	}
	
	public NavMeshNode getClosestNode(float x, float y, float z)
	{
		tmpVec.set(x, y, z).mul(inverse);
		x = tmpVec.x;
		y = tmpVec.y;
		z = tmpVec.z;
		
		NavMeshPartition nmp = partition.getPartition(x, y, z);
		
		if (nmp == null) return null;

		NavMeshNode node = nmp.nodes[0];
		float dist2 = tmpVec.dst2(nmp.nodes[0].x, nmp.nodes[0].y, nmp.nodes[0].z);
		
		for (NavMeshNode n : nmp.nodes)
		{
			if (tmpVec.dst2(n.x, n.y, n.z) < dist2)
			{
				node = n;
				dist2 = tmpVec.dst2(n.x, n.y, n.z);
			}
		}

		return node;
	}
	
	public boolean checkCollision(float x, float y, float z, Ray ray, Vector3 collision)
	{
		tmpVec.set(x, y, z).mul(inverse);
		x = tmpVec.x;
		y = tmpVec.y;
		z = tmpVec.z;
		
		tmpRay.set(ray);
		tmpRay.origin.mul(inverse);
		tmpRay.direction.mul(rotationTra);
		
		NavMeshPartition nmp = partition.getPartition(tmpVec, tmpRay.origin);
		if (nmp == null || nmp.triangles == null || !Intersector.intersectRayTriangles(tmpRay, nmp.triangles, collision)) {
				return false;
		}
		collision.mul(combined);
		
		return true;
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
			
			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);
			
			nodeList.add(n1);
			nodeList.add(n2);
			nodeList.add(n3);
			
			float[] triangle = new float[10];
			triangle[0] = n1.x;
			triangle[1] = n1.y;
			triangle[2] = n1.z;
			triangle[3] = n2.x;
			triangle[4] = n2.y;
			triangle[5] = n2.z;
			triangle[6] = n3.x;
			triangle[7] = n3.y;
			triangle[8] = n3.z;
			
			n1.addTriangle(triangle);
			n2.addTriangle(triangle);
			n3.addTriangle(triangle);
			//nodeList.addAll(subdivideTriangle(n1, n2, n3, maxArea));
		}
		
		final NavMeshNode[] nodes = new NavMeshNode[nodeList.size()];
		nodeList.toArray(nodes);
		
		return new NavMesh(nodes);
	}
	
//	private static float getAngle(NavMeshNode n1, NavMeshNode n2)
//	{
//		double O = Math.abs(n1.y-n2.y);
//		double H = Math.sqrt(Math.pow(n1.x-n2.x, 2) + Math.pow(n1.z-n2.z, 2));
//		
//		return (float) Math.sin(O/H);
//	}
//	
//	private static List<NavMeshNode> subdivideTriangle(NavMeshNode n1, NavMeshNode n2, NavMeshNode n3, float targetArea)
//	{
//		Vector3 AB = Pools.obtain(Vector3.class).set(n1.x, n1.y, n1.z).sub(n2.x, n2.y, n2.z);
//		Vector3 AC = Pools.obtain(Vector3.class).set(n1.x, n1.y, n1.z).sub(n3.x, n3.y, n3.z);
//		
//		float area = 0.5f*AB.crs(AC).dst(0, 0, 0);
//		
//		List<NavMeshNode> nodes = new ArrayList<NavMeshNode>();
//		if (area > targetArea)
//		{
//			NavMeshNode n4 = new NavMeshNode(
//					(n1.x+n2.x+n3.x)/3,
//					(n1.y+n2.y+n3.y)/3,
//					(n1.z+n2.z+n3.z)/3
//					);
//			
//			n4.addNodes(n1, n2, n3);
//			n1.addNodes(n4);
//			n2.addNodes(n4);
//			n3.addNodes(n4);
//			
//			nodes.add(n4);
//			nodes.addAll(subdivideTriangle(n1, n3, n4, targetArea));
//			nodes.addAll(subdivideTriangle(n1, n2, n4, targetArea));
//			nodes.addAll(subdivideTriangle(n2, n3, n4, targetArea));
//		}
//		
//		Pools.free(AB);
//		Pools.free(AC);
//		
//		return nodes;
//	}
	
	public static class NavMeshNode
	{
		public final float x;
		public final float y;
		public final float z;
		
		public final List<NavMeshNode> connectedNodes;
		public final List<float[]> triangles;
		
		public NavMeshNode(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			
			connectedNodes = new ArrayList<NavMeshNode>();
			triangles = new ArrayList<float[]>();
		}
		
		public void addNodes(NavMeshNode... nodes)
		{
			for (NavMeshNode n : nodes)
			{
				connectedNodes.add(n);
			}
		}
		
		public void addTriangle(float[] triangle)
		{
			triangles.add(triangle);
		}
	}
	
	public static class NavMeshPartition
	{
		public final float minx;
		public final float miny;
		public final float minz;
		
		public final float maxx;
		public final float maxy;
		public final float maxz;
		
		public final NavMeshNode[] nodes;
		public final boolean[] childNodes;
		public final float[] triangles;
		
		public final NavMeshPartition parent;
		
		public final NavMeshPartition tnw;
		public final NavMeshPartition tne;
		public final NavMeshPartition tsw;
		public final NavMeshPartition tse;
		
		public final NavMeshPartition bnw;
		public final NavMeshPartition bne;
		public final NavMeshPartition bsw;
		public final NavMeshPartition bse;
		
		public NavMeshPartition(NavMeshPartition parent, NavMeshNode[] nodes)
		{
			this.nodes = nodes;
			this.childNodes = new boolean[nodes.length];
			
			float minx = nodes[0].x;
			float miny = nodes[0].y;
			float minz = nodes[0].z;
			
			float maxx = nodes[0].x;
			float maxy = nodes[0].y;
			float maxz = nodes[0].z;
			
			for (NavMeshNode node : nodes)
			{
				if (node.x < minx) minx = node.x;
				if (node.y < miny) miny = node.y;
				if (node.z < minz) minz = node.z;
				
				if (node.x > maxx) maxx = node.x;
				if (node.y > maxy) maxy = node.y;
				if (node.z > maxz) maxz = node.z;
			}
			
			this.minx = minx;
			this.miny = miny;
			this.minz = minz;
			this.maxx = maxx;
			this.maxy = maxy;
			this.maxz = maxz;
			
			this.parent = parent;
			
			if (nodes.length == 1)
			{
				tnw = tne = tsw = tse = bnw = bne = bsw = bse = null;
			}
			else
			{
				final float midx = minx+(maxx-minx)/2;
				final float midy = miny+(maxy-miny)/2;
				final float midz = minz+(maxz-minz)/2;
				
				NavMeshNode[] n = null;
				
				n = getPartitionedNodes(nodes, minx, midx, midy, maxy, midz, maxz);
				tnw = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, maxx, midy, maxy, midz, maxz);
				tne = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, minx, midx, midy, maxy, minz, midz);
				tsw = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, maxx, midy, maxy, minz, midz);
				tse = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, minx, midx, miny, midy, midz, maxz);
				bnw = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, maxx, miny, midy, midz, maxz);
				bne = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, minx, midx, miny, midy, minz, midz);
				bsw = (n.length == 0) ? null : new NavMeshPartition(this, n);
				
				n = getPartitionedNodes(nodes, midx, maxx, miny, midy, minz, midz);
				bse = (n.length == 0) ? null : new NavMeshPartition(this, n);
			}
			
			this.triangles = getTriangles(nodes);
		}
		
		private NavMeshNode[] getPartitionedNodes(NavMeshNode[] nodes, float minx, float maxxy, float miny, float maxy, float minz, float maxz)
		{
			List<NavMeshNode> nodeList = new ArrayList<NavMeshNode>();
			
			for (int i = 0; i < nodes.length; i++)
			{
				if (childNodes[i]) continue;
				NavMeshNode node = nodes[i];
				if (node.x > minx && node.x < maxx && 
						node.y > miny && node.y < maxy && 
						node.z > minz && node.z < maxz)
				{
					nodeList.add(node);
					childNodes[i] = true;
				}
			}
			
			NavMeshNode[] finalNodes = new NavMeshNode[nodeList.size()];
			nodeList.toArray(finalNodes);
			
			return finalNodes;
		}
		
		private float[] getTriangles(NavMeshNode[] nodes)
		{
			List<float[]> triangleList = new ArrayList<float[]>();
			
			for (int i = 0; i < nodes.length; i++)
			{
				//if (childNodes[i]) continue;
				NavMeshNode node = nodes[i];
				for (float[] triangle : node.triangles)
				{
					if (triangle[9] == 0)
					{
						triangleList.add(triangle);
						triangle[9] = 1;
					}
				}
			}
			
			if (triangleList.size() == 0) return null;
			
			float[] triangles = new float[triangleList.size()*9];
			for (int i = 0; i < triangleList.size(); i++)
			{
				System.arraycopy(triangleList.get(i), 0, triangles, i*9, 9);
				triangleList.get(i)[9] = 0;
			}
			
			return triangles;
		}
		
		public NavMeshPartition getPartition(float x, float y, float z)
		{
			NavMeshPartition nmp = null;
			if (x < minx || x > maxx || y < miny || y > maxy || z < minz || z > maxz) return null;
			else if (tnw != null && (nmp=tnw.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (tne != null && (nmp=tne.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (tsw != null && (nmp=tsw.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (tse != null && (nmp=tse.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (bnw != null && (nmp=bnw.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (bne != null && (nmp=bne.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (bsw != null && (nmp=bsw.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else if (bse != null && (nmp=bse.getPartition(x, y, z)) != null)
			{
				return nmp;
			}
			else return this;
		}
		
		public NavMeshPartition getPartition(Vector3 p1, Vector3 p2)
		{
			NavMeshPartition nmp = null;
			if (p1.x < minx || p1.x > maxx || p1.y < miny || p1.y > maxy || p1.z < minz || p1.z > maxz ||
					p2.x < minx || p2.x > maxx || p2.y < miny || p2.y > maxy || p2.z < minz || p2.z > maxz) return null;
			else if (tnw != null && (nmp=tnw.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (tne != null && (nmp=tne.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (tsw != null && (nmp=tsw.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (tse != null && (nmp=tse.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (bnw != null && (nmp=bnw.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (bne != null && (nmp=bne.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (bsw != null && (nmp=bsw.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else if (bse != null && (nmp=bse.getPartition(p1, p2)) != null)
			{
				return nmp;
			}
			else return this;
		}
	}
}
