package com.Lyeeedar.Collision;

import java.util.ArrayList;
import java.util.List;

import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public final class SymbolicMesh extends CollisionShape<SymbolicMesh> {
	
	public final SymbolicMeshNode[] nodes;
	
	public final Triangle[] tris;

	private final Vector3 position = new Vector3();
	private final Vector3 rotationVec = new Vector3();
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
	
	public SymbolicMesh(SymbolicMeshNode[] nodes, Triangle[] tris)
	{	
		this.tris = tris;
		
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

		TempMeshPartition temp = new TempMeshPartition(null, this.minx, this.maxx, this.miny, this.maxy, this.minz, this.maxz, (byte) 0);

		for (short i = 0; i < tris.length; i++)
		{			
			temp.addTriangle(i, tris[i]);
		}
		
		temp.finalize();
		
		partition = new SymbolicMeshPartition(null, temp);
		
		setPosition(new Vector3(0, 0, 0));
		setRotation(GLOBALS.DEFAULT_ROTATION);
		updateMatrixes();
	}

	/**
	 * Recalculate all the transformations for this mesh
	 */
	public void updateMatrixes()
	{
		Matrix4 tmpMat = Pools.obtain(Matrix4.class);
		combined.setToTranslation(position).mul(rotation);
		rotationTra.set(rotation).tra();
		inverse.set(rotationTra).mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
		Pools.free(tmpMat);
	}

	public Matrix4 getCombined()
	{
		return combined;
	}

	@Override
	public void setPosition(Vector3 position)
	{
		if (this.position.x == position.x && this.position.y == position.y && this.position.z == position.z) return;
		
		this.position.set(position);
		updateMatrixes();
	}
	
	@Override
	public void setRotation(Vector3 rotation) {
		
		if (rotationVec.x == rotation.x && rotationVec.y == rotation.y && rotationVec.z == rotation.z) return;
		
		rotationVec.set(rotation);
		this.rotation.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
		updateMatrixes();
	}
	
	@Override
	public boolean collide(Sphere sphere) {
		return checkCollision(sphere, true);
	}

	@Override
	public boolean collide(Cylinder cylinder) {
		return checkCollision(cylinder, true);
	}

	@Override
	public boolean collide(Box rect) {
		return checkCollision(rect, true);
	}

	@Override
	public boolean collide(Triangle tri) {
		return checkCollision(tri, true);
	}

	@Override
	public boolean collide(CollisionRay ray) {
		return checkCollision(ray, false);
	}

	@Override
	public SymbolicMesh set(SymbolicMesh other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SymbolicMesh copy() {
		return this;
	}
	
	@Override
	public void transformPosition(Matrix4 matrix) {
		position.mul(matrix);
		updateMatrixes();
	}

	@Override
	public void transformDirection(Matrix4 matrix) {
		rotation.mul(matrix);
		updateMatrixes();
	}
	
	@Override
	public SymbolicMesh obtain() {
		return this;
	}

	@Override
	public void free() {
		
	}
	
	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected String string() {
		return "SymbolicMesh dickweed";
	}

	public boolean checkCollision(CollisionShape<?> shape, boolean fast)
	{	
		shape.transformPosition(inverse);
		shape.transformDirection(rotationTra);
		
		CollisionShape<?> check = shape.obtain();

		if (!checkCollisionRecursive(partition, shape, check, fast)) {
			check.free();
			return false;
		}
				
		check.free();
		shape.transformPosition(combined);
				
		return true;
	}
	
	private boolean checkCollisionRecursive(SymbolicMeshPartition partition, CollisionShape<?> shape, CollisionShape<?> check, boolean fast)
	{
		//if (!partition.shape.collide(check)) return false;
		
		boolean collide = (partition.indices.length > 0) ? ThreadSafeIntersector.collideShapeList(shape, tris, partition.indices, fast) : false ;
		if (fast && collide) return true;
		
		for (SymbolicMeshPartition p : partition.children)
		{
			check.reset();
			if (p != null && checkCollisionRecursive(p, shape, check, fast)) collide = true;
			if (fast && collide) break;
		}
		
		return collide;
	}

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

		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new SymbolicMeshNode(
					verts[(i*vertexSize)+positionOffset+0],
					verts[(i*vertexSize)+positionOffset+1],
					verts[(i*vertexSize)+positionOffset+2]
					);
		}

		final List<Triangle> tris = new ArrayList<Triangle>(triangles);
		
		for (int i = 0; i < triangles; i++)
		{
			final SymbolicMeshNode n1 = vertexNodes[indices[(i*3)+0]];
			final SymbolicMeshNode n2 = vertexNodes[indices[(i*3)+1]];
			final SymbolicMeshNode n3 = vertexNodes[indices[(i*3)+2]];

			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);
			
			Triangle t = new Triangle();
			t.v1.set(n1.x, n1.y, n1.z);
			t.v2.set(n2.x, n2.y, n2.z);
			t.v3.set(n3.x, n3.y, n3.z);

			tris.add(t);
		}

		Triangle[] triArray = new Triangle[triangles];
		tris.toArray(triArray);
		
		return new SymbolicMesh(vertexNodes, triArray);
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
		
		public byte depth;

		public float minx;
		public float miny;
		public float minz;

		public float midx;
		public float midy;
		public float midz;

		public float maxx;
		public float maxy;
		public float maxz;

		public List<Triangle> triangleList;
		public List<Short> indices;
		
		public short[] indexArray;
		
		public TempMeshPartition parent;
		
		public TempMeshPartition[] children;

		public TempMeshPartition(TempMeshPartition parent, float minx, float maxx, float miny, float maxy, float minz, float maxz, byte depth)
		{
			this.depth = depth;

			this.minx = minx;
			this.miny = miny;
			this.minz = minz;
			this.maxx = maxx;
			this.maxy = maxy;
			this.maxz = maxz;
			
			this.parent = parent;
			
			this.triangleList = new ArrayList<Triangle>();
			this.indices = new ArrayList<Short>();
		}
		
		public void addTriangle(short index, Triangle triangle)
		{
			triangleList.add(triangle);
			indices.add(index);
		}
		
		public boolean cascade (short index, Triangle triangle) {
			
			short top;
			short north;
			short east;
			
			if (triangle.v1.x >= midx && triangle.v2.x >= midx && triangle.v3.x >= midx) east = 1;
			else if (triangle.v1.x < midx && triangle.v2.x < midx && triangle.v3.x < midx) east = -1;
			else east = 0;
			
			if (triangle.v1.y >= midy && triangle.v2.y >= midy && triangle.v3.y >= midy) top = 1;
			else if (triangle.v1.y < midy && triangle.v2.y < midy && triangle.v3.y < midy) top = -1;
			else top = 0;
			
			if (triangle.v1.z >= midz && triangle.v2.z >= midz && triangle.v3.z >= midz) north = 1;
			else if (triangle.v1.z < midz && triangle.v2.z < midz && triangle.v3.z < midz) north = -1;
			else north = 0;
			
			if (top == 1 && east == 1 && north == 1)
			{
				children[SymbolicMeshPartition.TNE].addTriangle(index, triangle);
			}
			else if (top == 1 && east == 1 && north == -1)
			{
				children[SymbolicMeshPartition.TSE].addTriangle(index, triangle);
			}
			else if (top == 1 && east == -1 && north == 1)
			{
				children[SymbolicMeshPartition.TNW].addTriangle(index, triangle);
			}
			else if (top == 1 && east == -1 && north == -1)
			{
				children[SymbolicMeshPartition.TSW].addTriangle(index, triangle);
			}
			else if (top == -1 && east == 1 && north == 1)
			{
				children[SymbolicMeshPartition.BNE].addTriangle(index, triangle);
			}
			else if (top == -1 && east == 1 && north == -1)
			{
				children[SymbolicMeshPartition.BSE].addTriangle(index, triangle);
			}
			else if (top == -1 && east == -1 && north == 1)
			{
				children[SymbolicMeshPartition.BNW].addTriangle(index, triangle);
			}
			else if (top == -1 && east == -1 && north == -1)
			{
				children[SymbolicMeshPartition.BSW].addTriangle(index, triangle);
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
			
			children = new TempMeshPartition[8];
			
			children[SymbolicMeshPartition.TNE] = new TempMeshPartition(this, midx, maxx, midy, maxy, midz, maxz, (byte) (depth+1));
			children[SymbolicMeshPartition.TNW] = new TempMeshPartition(this, minx, midx, midy, maxy, midz, maxz, (byte) (depth+1));
			children[SymbolicMeshPartition.TSE] = new TempMeshPartition(this, midx, maxx, midy, maxy, minz, midz, (byte) (depth+1));
			children[SymbolicMeshPartition.TSW] = new TempMeshPartition(this, minx, midx, midy, maxy, minz, midz, (byte) (depth+1));
			children[SymbolicMeshPartition.BNE] = new TempMeshPartition(this, midx, maxx, miny, midy, midz, maxz, (byte) (depth+1));
			children[SymbolicMeshPartition.BNW] = new TempMeshPartition(this, minx, midx, miny, midy, midz, maxz, (byte) (depth+1));
			children[SymbolicMeshPartition.BSE] = new TempMeshPartition(this, midx, maxx, miny, midy, minz, midz, (byte) (depth+1));
			children[SymbolicMeshPartition.BSW] = new TempMeshPartition(this, minx, midx, miny, midy, minz, midz, (byte) (depth+1));
			
			if (triangleList.size() > BUCKET_SIZE)
			{
				int i = 0;
				count = triangleList.size();
				
				for (i = 0; i < count; i++)
				{
					boolean success = cascade(indices.get(i), triangleList.get(i));
					
					if (success)
					{
						triangleList.remove(i);
						indices.remove(i);
						i--;
						count--;
					}
				}
			}
			
			int bucket_size = indices.size();
			indexArray = new short[bucket_size];
			
			for (int i = 0; i < bucket_size; i++)
			{
				indexArray[i] = indices.get(i);
			}
			
			for (TempMeshPartition p : children) p.finalize();
		}
	}
	
	public final static class SymbolicMeshPartition
	{
		private static final byte TNW = 0;
		private static final byte TNE = 1;
		private static final byte TSW = 2;
		private static final byte TSE = 3;
		private static final byte BNW = 4;
		private static final byte BNE = 5;
		private static final byte BSW = 6;
		private static final byte BSE = 7;
		
		public final byte depth;

		public final float minx;
		public final float miny;
		public final float minz;

		public final float midx;
		public final float midy;
		public final float midz;

		public final float maxx;
		public final float maxy;
		public final float maxz;

		public final short[] indices;

		public final SymbolicMeshPartition parent;

		public final SymbolicMeshPartition[] children;
		
		public final Box shape;

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
			
			this.indices = (tree.indexArray == null) ? new short[0] : tree.indexArray ;
			
			this.shape = new Box(new Vector3(midx, midy, midz), (maxx-minx)/2, (maxy-miny)/2, (maxz-minz)/2);
			
			this.children = new SymbolicMeshPartition[8];
			if (tree.children != null)
			{
				for (int i = 0; i < 8; i++)
				{
					children[i] = (tree.children[i] == null) ? null : new SymbolicMeshPartition(this, tree.children[i]);
				}
			}
		}
	}
}
