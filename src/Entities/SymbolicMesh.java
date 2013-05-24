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

public final class SymbolicMesh {
	
	public final SymbolicMeshNode[] nodes;
	
	public final float[] vertices;
	public final int vertexSize;

	private final Vector3 position = new Vector3();
	private final Matrix4 rotation = new Matrix4();
	private final Matrix4 rotationTra = new Matrix4();
	private final Matrix4 combined = new Matrix4();
	private final Matrix4 inverse = new Matrix4();

	private final SymbolicMeshPartition partition;
	
	private final float minx;
	private final float miny;
	private final float minz;
	
	private final float maxx;
	private final float maxy;
	private final float maxz;
	
	public SymbolicMesh(SymbolicMeshNode[] nodes, short[][] triangles, float[] vertices, int vertexSize)
	{	
		this.vertices = vertices;
		this.vertexSize = vertexSize;
		
		List<SymbolicMeshNode> nodeList = new ArrayList<SymbolicMeshNode>(nodes.length);
		boolean[] duplicates = new boolean[nodes.length];
		for (int i = 0; i < nodes.length; i++)
		{
			if (duplicates[i]) continue;
			SymbolicMeshNode n1 = nodes[i];
			for (int j = 0; j < nodes.length; j++)
			{
				if (duplicates[j]) continue;
				if (j == i) continue;
				SymbolicMeshNode n2 = nodes[j];

				if (n1.x == n2.x && n1.y == n2.y && n1.z == n2.z)
				{
					duplicates[j] = true;
				}
			}

			nodeList.add(n1);
		}

		SymbolicMeshNode[] newNodes = new SymbolicMeshNode[nodeList.size()];
		nodeList.toArray(newNodes);

		this.nodes = newNodes;

		float minx = newNodes[0].x;
		float miny = newNodes[0].y;
		float minz = newNodes[0].z;

		float max = 0;

		for (SymbolicMeshNode node : newNodes)
		{
			if (node.x < minx) minx = node.x;
			if (node.y < miny) miny = node.y;
			if (node.z < minz) minz = node.z;

			if (node.x-minx > max) max = node.x-minx;
			if (node.y-miny > max) max = node.y-miny;
			if (node.z-minz > max) max = node.z-minz;

			node.calculate();
		}

		max++;
		
		this.minx = minx;
		this.miny = miny;
		this.minz = minz;
		this.maxx = minx+max;
		this.maxy = miny+max;
		this.maxz = minz+max;

		TempMeshPartition temp = new TempMeshPartition(null, this.minx, this.maxx, this.miny, this.maxy, this.minz, this.maxz, 0);

		for (short[] tri : triangles)
		{			
			float[] x = new float[3];
			float[] y = new float[3];
			float[] z = new float[3];
			
			for (int i = 0; i < 3; i++)
			{
				x[i] = vertices[(tri[i]*vertexSize)+0];
				y[i] = vertices[(tri[i]*vertexSize)+1];
				z[i] = vertices[(tri[i]*vertexSize)+2];
			}
			
			temp.addTriangle(tri, x, y, z);
		}
		
		temp.finalize();
		
		partition = new SymbolicMeshPartition(null, temp);
	}

	/**
	 * Recalculate all the transformations for this mesh
	 */
	public void updateMatrixes()
	{
		Matrix4 tmpMat = new Matrix4();
		combined.setToTranslation(position).mul(rotation);
		inverse.set(rotationTra).mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
	}

	/**
	 * Get the combined transformation matrix for this mesh
	 * @return
	 */
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

	/**
	 * Check the given ray to dest against the geometry. Store the resulting collision (if there was one) in the collision Vector3.
	 * @param ray
	 * @param dest
	 * @param collision
	 * @return
	 */
	public boolean checkCollision(Ray ray, Vector3 dest, Vector3 collision, float[] min_dist, Vector3[] tmp)
	{
		dest.mul(inverse);

		ray.origin.mul(inverse);
		ray.direction.mul(rotationTra);

		if (!checkCollisionIterative(partition, ray, dest, collision, min_dist, tmp)) {
			return false;
		}

		collision.mul(combined);
		
		return true;
	}
	
	private boolean intersectRayTriangles (final Ray ray, final short[] indices, final Vector3 intersection, final float[] min_dist, final Vector3[] tmp) {
		boolean hit = false;
		for (int i = 0; i < indices.length; i += 3) {
			int i1 = indices[i] * vertexSize;
			int i2 = indices[i + 1] * vertexSize;
			int i3 = indices[i + 2] * vertexSize;

			boolean result = Intersector.intersectRayTriangle(ray, 
					tmp[0].set(vertices[i1], vertices[i1 + 1], vertices[i1 + 2]),
					tmp[1].set(vertices[i2], vertices[i2 + 1], vertices[i2 + 2]),
					tmp[2].set(vertices[i3], vertices[i3 + 1], vertices[i3 + 2]), 
					tmp[3]);

			if (result == true) {
				float dist = tmp[0].set(ray.origin).sub(tmp[3]).len2();
				if (dist < min_dist[0]) {
					min_dist[0] = dist;
					intersection.set(tmp[3]);
					hit = true;
				}
			}
		}

		if (hit == false) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private boolean checkCollisionIterative(SymbolicMeshPartition partition, Ray ray, Vector3 end, Vector3 collision, float[] min_dist, Vector3[] tmp)
	{
		if ((ray.origin.x < minx && end.x < minx) || (ray.origin.x > maxx && end.x > maxx)) return false;
		else
		{
			ray.origin.x = (ray.origin.x < minx) ? minx : ray.origin.x;
			ray.origin.x = (ray.origin.x > maxx) ? maxx : ray.origin.x;
			
			end.x = (end.x < minx) ? minx : end.x;
			end.x = (end.x > maxx) ? maxx : end.x;
		}
		
		if ((ray.origin.y < miny && end.y < miny) || (ray.origin.y > maxy && end.y > maxy)) return false;
		else
		{
			ray.origin.y = (ray.origin.y < miny) ? miny : ray.origin.y;
			ray.origin.y = (ray.origin.y > maxy) ? maxy : ray.origin.y;
			
			end.y = (end.y < miny) ? miny : end.y;
			end.y = (end.y > maxy) ? maxy : end.y;
		}
		
		if ((ray.origin.z < minz && end.z < minz) || (ray.origin.z > maxz && end.z > maxz)) return false;
		else
		{
			ray.origin.z = (ray.origin.z < minz) ? minz : ray.origin.z;
			ray.origin.z = (ray.origin.z > maxz) ? maxz : ray.origin.z;
			
			end.z = (end.z < minz) ? minz : end.z;
			end.z = (end.z > maxz) ? maxz : end.z;
		}
		
		SymbolicMeshPartition current = partition;
		SymbolicMeshPartition next = partition;
		
		boolean collide = false;
		
		while(next != null)
		{
			current = next;		
			next = current.getPartition(ray.origin, end);
			
			if (next == null) break;
			
			collide = (intersectRayTriangles(ray, current.triangles, collision, min_dist, tmp)) ? true : collide;
		}
		
		collide = (checkCollisionRecursive(current, ray, end, collision, min_dist, tmp)) ? true : collide;
		
		return collide;
	}
	
	private boolean checkCollisionRecursive(SymbolicMeshPartition partition, Ray ray, Vector3 end, Vector3 collision, float[] min_dist, Vector3[] tmp)
	{
		boolean collide = intersectRayTriangles(ray, partition.triangles, collision, min_dist, tmp);
		
		short top = partition.getTop(ray.origin.y, end.y);
		short north = partition.getNorth(ray.origin.z, end.z);
		short east = partition.getEast(ray.origin.x, end.x);
		
		if (partition.tne != null && top >= 0 && north >= 0 && east >= 0 && checkCollisionRecursive(partition.tne, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.tnw != null && top >= 0 && north >= 0 && east <= 0 && checkCollisionRecursive(partition.tnw, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.tse != null && top >= 0 && north <= 0 && east >= 0 && checkCollisionRecursive(partition.tse, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.tsw != null && top >= 0 && north <= 0 && east <= 0 && checkCollisionRecursive(partition.tsw, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.bne != null && top <= 0 && north >= 0 && east >= 0 && checkCollisionRecursive(partition.bne, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.bnw != null && top <= 0 && north >= 0 && east <= 0 && checkCollisionRecursive(partition.bnw, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.bse != null && top <= 0 && north <= 0 && east >= 0 && checkCollisionRecursive(partition.bse, ray, end, collision, min_dist, tmp)) collide = true;
		if (partition.bsw != null && top <= 0 && north <= 0 && east <= 0 && checkCollisionRecursive(partition.bsw, ray, end, collision, min_dist, tmp)) collide = true;
		
		return collide;
	}

	/**
	 * Only works for triangular polygon meshes (with indices specifiying triangles in groups of 3)
	 * @param mesh
	 * @return
	 */
	public static SymbolicMesh getSymbolicMesh(Mesh mesh, float maxArea)
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

		final SymbolicMeshNode[] vertexNodes = new SymbolicMeshNode[vertCount];
		final float[] vertexes = new float[vertCount * 3];

		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new SymbolicMeshNode(
					verts[(i*vertexSize)+positionOffset+0],
					verts[(i*vertexSize)+positionOffset+1],
					verts[(i*vertexSize)+positionOffset+2]
					);
			
			vertexes[(i*3)+0] = verts[(i*vertexSize)+positionOffset+0];
			vertexes[(i*3)+1] = verts[(i*vertexSize)+positionOffset+1];
			vertexes[(i*3)+2] = verts[(i*vertexSize)+positionOffset+2];
		}

		final List<short[]> tris = new ArrayList<short[]>();
		
		for (int i = 0; i < triangles; i++)
		{
			final SymbolicMeshNode n1 = vertexNodes[indices[(i*3)+0]];
			final SymbolicMeshNode n2 = vertexNodes[indices[(i*3)+1]];
			final SymbolicMeshNode n3 = vertexNodes[indices[(i*3)+2]];

			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);

			short[] triangle = new short[3];
			triangle[0] = indices[(i*3)+0];
			triangle[1] = indices[(i*3)+1];
			triangle[2] = indices[(i*3)+2];

			tris.add(triangle);
		}

		final short[][] triArray = new short[tris.size()][3];
		tris.toArray(triArray);
		
		return new SymbolicMesh(vertexNodes, triArray, vertexes, 3);
	}

	public final static class SymbolicMeshNode
	{
		public final float x;
		public final float y;
		public final float z;

		public final List<SymbolicMeshNode> connectedNodes;		
		public float[] distances;
		public float[] angles;

		public SymbolicMeshNode(float x, float y, float z)
		{
			this.x = x;
			this.y = y;
			this.z = z;

			connectedNodes = new ArrayList<SymbolicMeshNode>();
		}

		public void calculate()
		{
			int size = connectedNodes.size();
			distances = new float[size];
			angles = new float[size];

			Vector3 tmp1 = new Vector3();
			Vector3 tmp2 = new Vector3();

			for (int i = 0; i < size; i++)
			{
				SymbolicMeshNode node = connectedNodes.get(i);

				tmp1.set(x, y, z);
				tmp2.set(node.x, node.y, node.z);

				distances[i] = tmp1.dst(tmp2);
				angles[i] = getAngle(tmp1, tmp2);
			}
		}

		private float getAngle(Vector3 n1, Vector3 n2)
		{
			double O = Math.abs(n1.y-n2.y);
			double H = Math.sqrt(Math.pow(n1.x-n2.x, 2) + Math.pow(n1.z-n2.z, 2));

			return (float) Math.sin(O/H);
		}

		public void addNodes(SymbolicMeshNode... nodes)
		{
			for (SymbolicMeshNode n : nodes)
			{
				connectedNodes.add(n);
			}
		}
	}

	public class TempMeshPartition
	{
		private static final int BUCKET_SIZE = 8;
		
		public final int depth;

		public final float minx;
		public final float miny;
		public final float minz;

		public float midx;
		public float midy;
		public float midz;

		public final float maxx;
		public final float maxy;
		public final float maxz;

		public short[] triangles = new short[0];
		public List<short[]> triangleList;
		public List<float[]> xs;
		public List<float[]> ys;
		public List<float[]> zs;
		
		public final TempMeshPartition parent;

		public TempMeshPartition tnw;
		public TempMeshPartition tne;
		public TempMeshPartition tsw;
		public TempMeshPartition tse;

		public TempMeshPartition bnw;
		public TempMeshPartition bne;
		public TempMeshPartition bsw;
		public TempMeshPartition bse;

		public TempMeshPartition(TempMeshPartition parent, float minx, float maxx, float miny, float maxy, float minz, float maxz, int depth)
		{
			this.depth = depth;

			this.minx = minx;
			this.miny = miny;
			this.minz = minz;
			this.maxx = maxx;
			this.maxy = maxy;
			this.maxz = maxz;
			
			this.parent = parent;
			
			this.triangleList = new ArrayList<short[]>();
			xs = new ArrayList<float[]>();
			ys = new ArrayList<float[]>();
			zs = new ArrayList<float[]>();
		}
		
		public void addTriangle(short[] triangle, float[] x, float[] y, float[] z)
		{
			triangleList.add(triangle);
			xs.add(x);
			ys.add(y);
			zs.add(z);
		}
		
		public boolean cascade (short[] triangle, float[] x, float[] y, float[] z) {
			
			short top;
			short north;
			short east;
			
			if (x[0] >= midx && x[1] >= midx && x[2] >= midx) east = 1;
			else if (x[0] < midx && x[1] < midx && x[2] < midx) east = -1;
			else east = 0;
			
			if (y[0] >= midy && y[1] >= midy && y[2] >= midy) top = 1;
			else if (y[0] < midy && y[1] < midy && y[2] < midy) top = -1;
			else top = 0;
			
			if (z[0] >= midz && z[1] >= midz && z[2] >= midz) north = 1;
			else if (z[0] < midz && z[1] < midz && z[2] < midz) north = -1;
			else north = 0;
			
			if (top == 1 && east == 1 && north == 1)
			{
				tne.addTriangle(triangle, x, y, z);
			}
			else if (top == 1 && east == 1 && north == -1)
			{
				tse.addTriangle(triangle, x, y, z);
			}
			else if (top == 1 && east == -1 && north == 1)
			{
				tnw.addTriangle(triangle, x, y, z);
			}
			else if (top == 1 && east == -1 && north == -1)
			{
				tsw.addTriangle(triangle, x, y, z);
			}
			else if (top == -1 && east == 1 && north == 1)
			{
				bne.addTriangle(triangle, x, y, z);
			}
			else if (top == -1 && east == 1 && north == -1)
			{
				bse.addTriangle(triangle, x, y, z);
			}
			else if (top == -1 && east == -1 && north == 1)
			{
				bnw.addTriangle(triangle, x, y, z);
			}
			else if (top == -1 && east == -1 && north == -1)
			{
				bsw.addTriangle(triangle, x, y, z);
			}
			else
			{
				return false;
			}
			return true;
		}
		
		public void finalize()
		{
			if (triangleList.size() == 0) return;
			
			int count = 0;
			
			midx = minx+((maxx-minx)/2f);
			midy = miny+((maxy-miny)/2f);
			midz = minz+((maxz-minz)/2f);
			
			tne = new TempMeshPartition(this, midx, maxx, midy, maxy, midz, maxz, depth+1);
			tnw = new TempMeshPartition(this, minx, midx, midy, maxy, midz, maxz, depth+1);
			tse = new TempMeshPartition(this, midx, maxx, midy, maxy, minz, midz, depth+1);
			tsw = new TempMeshPartition(this, minx, midx, midy, maxy, minz, midz, depth+1);
			bne = new TempMeshPartition(this, midx, maxx, miny, midy, midz, maxz, depth+1);
			bnw = new TempMeshPartition(this, minx, midx, miny, midy, midz, maxz, depth+1);
			bse = new TempMeshPartition(this, midx, maxx, miny, midy, minz, midz, depth+1);
			bsw = new TempMeshPartition(this, minx, midx, miny, midy, minz, midz, depth+1);
			
			if (triangleList.size() > BUCKET_SIZE)
			{
				int i = 0;
				count = triangleList.size();
				
				for (i = 0; i < count; i++)
				{
					boolean success = cascade(triangleList.get(i), xs.get(i), ys.get(i), zs.get(i));
					
					if (success)
					{
						triangleList.remove(i);
						xs.remove(i);
						ys.remove(i);
						zs.remove(i);
						i--;
						count--;
					}
				}
			}
			
			int bucket_size = triangleList.size();
			triangles = new short[bucket_size*3];
			
			for (int i = 0; i < bucket_size; i++)
			{
				System.arraycopy(triangleList.get(i), 0, triangles, i*3, 3);
			}
			
			if (tne != null) tne.finalize();
			if (tnw != null) tnw.finalize();
			if (tse != null) tse.finalize();
			if (tsw != null) tsw.finalize();
			if (bne != null) bne.finalize();
			if (bnw != null) bnw.finalize();
			if (bse != null) bse.finalize();
			if (bsw != null) bsw.finalize();
		}
	}
	
	public final static class SymbolicMeshPartition
	{
		public final int depth;

		public final float minx;
		public final float miny;
		public final float minz;

		public final float midx;
		public final float midy;
		public final float midz;

		public final float maxx;
		public final float maxy;
		public final float maxz;

		public final short[] triangles;

		public final SymbolicMeshPartition parent;

		public final SymbolicMeshPartition tnw;
		public final SymbolicMeshPartition tne;
		public final SymbolicMeshPartition tsw;
		public final SymbolicMeshPartition tse;

		public final SymbolicMeshPartition bnw;
		public final SymbolicMeshPartition bne;
		public final SymbolicMeshPartition bsw;
		public final SymbolicMeshPartition bse;

		public SymbolicMeshPartition(SymbolicMeshPartition parent, TempMeshPartition tree)
		{
			this.depth = tree.depth;

			this.minx = tree.minx;
			this.miny = tree.miny;
			this.minz = tree.minz;
			
			this.midx = tree.midx;
			this.midy = tree.midy;
			this.midz = tree.midz;
			
			this.maxx = tree.maxx;
			this.maxy = tree.maxy;
			this.maxz = tree.maxz;

			this.parent = parent;
			
			this.triangles = tree.triangles;
			
			tne = (tree.tne == null) ? null : new SymbolicMeshPartition(this, tree.tne);
			tnw = (tree.tnw == null) ? null : new SymbolicMeshPartition(this, tree.tnw);
			tse = (tree.tse == null) ? null : new SymbolicMeshPartition(this, tree.tse);
			tsw = (tree.tsw == null) ? null : new SymbolicMeshPartition(this, tree.tsw);
			bne = (tree.bne == null) ? null : new SymbolicMeshPartition(this, tree.bne);
			bnw = (tree.bnw == null) ? null : new SymbolicMeshPartition(this, tree.bnw);
			bse = (tree.bse == null) ? null : new SymbolicMeshPartition(this, tree.bse);
			bsw = (tree.bsw == null) ? null : new SymbolicMeshPartition(this, tree.bsw);
		}
		
		public short getEast(float x1, float x2)
		{
			short east;
			if (x1 >= midx && x2 >= midx) east = 1;
			else if (x1 < midx && x2 < midx) east = -1;
			else east = 0;
			return east;
		}
		
		public short getNorth(float z1, float z2)
		{
			short north;
			if (z1 >= midz && z2 >= midz) north = 1;
			else if (z1 < midz && z2 < midz) north = -1;
			else north = 0;
			return north;
		}
		
		public short getTop(float y1, float y2)
		{
			short top;
			if (y1 >= midy && y2 >= midy) top = 1;
			else if (y1 < midy && y2 < midy) top = -1;
			else top = 0;
			return top;
		}
		
		public SymbolicMeshPartition getPartition(Vector3 start, Vector3 end)
		{
			short top = getTop(start.y, end.y);
			short north = getNorth(start.z, end.z);
			short east = getEast(start.x, end.x);
			
			if (top == 0 || east == 0 || north == 0)
			{
				return null;
			}
			else if (top == 1 && north == 1 && east == 1)
			{
				return tne;
			}
			else if (top == 1 && north == -1 && east == 1)
			{
				return tse;
			}
			else if (top == 1 && north == 1 && east == -1)
			{
				return tnw;
			}
			else if (top == 1 && north == -1 && east == -1)
			{
				return tsw;
			}
			else if (top == -1 && north == 1 && east == 1)
			{
				return bne;
			}
			else if (top == -1 && north == -1 && east == 1)
			{
				return bse;
			}
			else if (top == -1 && north == 1 && east == -1)
			{
				return bnw;
			}
			else if (top == -1 && north == -1 && east == -1)
			{
				return bsw;
			}
			else
			{
				return null;
			}
		}
		
		@Override
		public String toString()
		{
			String s = "";
			s += "Partition: "+minx+","+miny+","+minz+" to "+maxx+","+maxy+","+maxz + "\n";
			s += "Num Tris: "+triangles.length/3 + "\n";
			s += "Depth: "+depth + "\n";
			if (tne != null) s += "tne: "+tne.minx+","+tne.miny+","+tne.minz+" to "+tne.maxx+","+tne.maxy+","+tne.maxz + "\n";
			if (tnw != null) s += "tnw: "+tnw.minx+","+tnw.miny+","+tnw.minz+" to "+tnw.maxx+","+tnw.maxy+","+tnw.maxz + "\n";
			if (tse != null) s += "tse: "+tse.minx+","+tse.miny+","+tse.minz+" to "+tse.maxx+","+tse.maxy+","+tse.maxz + "\n";
			if (tsw != null) s += "tsw: "+tsw.minx+","+tsw.miny+","+tsw.minz+" to "+tsw.maxx+","+tsw.maxy+","+tsw.maxz + "\n";
			if (bne != null) s += "bne: "+bne.minx+","+bne.miny+","+bne.minz+" to "+bne.maxx+","+bne.maxy+","+bne.maxz + "\n";
			if (bnw != null) s += "bnw: "+bnw.minx+","+bnw.miny+","+bnw.minz+" to "+bnw.maxx+","+bnw.maxy+","+bnw.maxz + "\n";
			if (bse != null) s += "bse: "+bse.minx+","+bse.miny+","+bse.minz+" to "+bse.maxx+","+bse.maxy+","+bse.maxz + "\n";
			if (bsw != null) s += "bsw: "+bsw.minx+","+bsw.miny+","+bsw.minz+" to "+bsw.maxx+","+bsw.maxy+","+bsw.maxz + "\n";

			return s;
		}
		
		public String toStringRecurse()
		{
			String s = "";
			s += "Partition: "+minx+","+miny+","+minz+" to "+maxx+","+maxy+","+maxz + "\n";
			s += "Num Tris: "+triangles.length/3 + "\n";
			s += "Depth: "+depth + "\n";
			if (tne != null) s += "tne:\n"+tne.toStringRecurse();
			if (tnw != null) s += "tnw:\n"+tnw.toStringRecurse();
			if (tse != null) s += "tse:\n"+tse.toStringRecurse();
			if (tsw != null) s += "tsw:\n"+tsw.toStringRecurse();
			if (bne != null) s += "bne:\n"+bne.toStringRecurse();
			if (bnw != null) s += "bnw:\n"+bnw.toStringRecurse();
			if (bse != null) s += "bse:\n"+bse.toStringRecurse();
			if (bsw != null) s += "bsw:\n"+bsw.toStringRecurse();

			return s;
		}
	}
}
