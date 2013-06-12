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

		TempMeshPartition temp = new TempMeshPartition(null, this.minx, this.maxx, this.miny, this.maxy, this.minz, this.maxz, 0);

		for (short i = 0; i < tris.length; i++)
		{			
			temp.addTriangle(i, tris[i]);
		}
		
		temp.finalize();
		
		partition = new SymbolicMeshPartition(null, temp);
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
		position.set(position);
	}
	
	@Override
	public void setRotation(Vector3 rotation) {
		this.rotation.setToRotation(GLOBALS.DEFAULT_ROTATION, rotation);
		rotationTra.set(this.rotation).tra();
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void free() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Check the given ray to dest against the geometry. Store the resulting collision (if there was one) in the collision Vector3.
	 * @param ray
	 * @param dest
	 * @param collision
	 * @return
	 */
	public boolean checkCollision(CollisionShape<?> shape, boolean fast)
	{	
		shape.transformPosition(inverse);
		shape.transformDirection(rotationTra);

		if (!checkCollisionRecursive(partition, shape, fast)) {
			return false;
		}
		
		shape.transformPosition(combined);
		
		return true;
	}
	
	private boolean checkCollisionRecursive(SymbolicMeshPartition partition, CollisionShape<?> shape, boolean fast)
	{
		boolean collide = intersectShapeTriangles(shape, tris, partition.indices, fast);
		if (fast && collide) return true;
		
		if (partition.tne != null && partition.tne.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.tnw != null && partition.tnw.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.tse != null && partition.tse.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.tsw != null && partition.tsw.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.bne != null && partition.bne.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.bnw != null && partition.bnw.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.bse != null && partition.bse.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		if (fast && collide) return true;
		if (partition.bsw != null && partition.bsw.shape.collide(shape) && checkCollisionRecursive(partition.tne, shape, fast)) collide = true;
		
		return collide;
	}
	
	private boolean intersectShapeTriangles (CollisionShape<?> shape, Triangle[] triangles, short[] indices, boolean fast) {
		boolean hit = false;
		
		for (int i = 0; i < indices.length; i += 3) {
			boolean result = triangles[i].collide(shape);

			if (result == true) {
				hit = true;
				if (fast) break;
			}
		}	

		if (hit == false) {
			return false;
		}
		else {
			return true;
		}
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

		final List<Triangle> tris = new ArrayList<Triangle>(triangles);
		
		for (int i = 0; i < triangles; i++)
		{
			final SymbolicMeshNode n1 = vertexNodes[indices[(i*3)+0]];
			final SymbolicMeshNode n2 = vertexNodes[indices[(i*3)+1]];
			final SymbolicMeshNode n3 = vertexNodes[indices[(i*3)+2]];

			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);

			short[] triangle = new short[3];
			triangle[0] = (short) (indices[(i*3)+0]*3);
			triangle[1] = (short) (indices[(i*3)+1]*3);
			triangle[2] = (short) (indices[(i*3)+2]*3);
			
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

		public List<Triangle> triangleList;
		public List<Short> indices;
		
		public short[] indexArray;
		
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
				tne.addTriangle(index, triangle);
			}
			else if (top == 1 && east == 1 && north == -1)
			{
				tse.addTriangle(index, triangle);
			}
			else if (top == 1 && east == -1 && north == 1)
			{
				tnw.addTriangle(index, triangle);
			}
			else if (top == 1 && east == -1 && north == -1)
			{
				tsw.addTriangle(index, triangle);
			}
			else if (top == -1 && east == 1 && north == 1)
			{
				bne.addTriangle(index, triangle);
			}
			else if (top == -1 && east == 1 && north == -1)
			{
				bse.addTriangle(index, triangle);
			}
			else if (top == -1 && east == -1 && north == 1)
			{
				bnw.addTriangle(index, triangle);
			}
			else if (top == -1 && east == -1 && north == -1)
			{
				bsw.addTriangle(index, triangle);
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

		public final short[] indices;

		public final SymbolicMeshPartition parent;

		public final SymbolicMeshPartition tnw;
		public final SymbolicMeshPartition tne;
		public final SymbolicMeshPartition tsw;
		public final SymbolicMeshPartition tse;

		public final SymbolicMeshPartition bnw;
		public final SymbolicMeshPartition bne;
		public final SymbolicMeshPartition bsw;
		public final SymbolicMeshPartition bse;
		
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
			
			this.indices = tree.indexArray;
			
			this.shape = new Box(new Vector3(tree.midx, tree.midy, tree.midz), maxx-minx, maxy-miny, maxz-minz);
			
			tne = (tree.tne == null) ? null : new SymbolicMeshPartition(this, tree.tne);
			tnw = (tree.tnw == null) ? null : new SymbolicMeshPartition(this, tree.tnw);
			tse = (tree.tse == null) ? null : new SymbolicMeshPartition(this, tree.tse);
			tsw = (tree.tsw == null) ? null : new SymbolicMeshPartition(this, tree.tsw);
			bne = (tree.bne == null) ? null : new SymbolicMeshPartition(this, tree.bne);
			bnw = (tree.bnw == null) ? null : new SymbolicMeshPartition(this, tree.bnw);
			bse = (tree.bse == null) ? null : new SymbolicMeshPartition(this, tree.bse);
			bsw = (tree.bsw == null) ? null : new SymbolicMeshPartition(this, tree.bsw);
		}
	}




}
