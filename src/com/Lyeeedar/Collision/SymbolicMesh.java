package com.Lyeeedar.Collision;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Octtree;
import com.Lyeeedar.Util.Octtree.OcttreeEntry;
import com.Lyeeedar.Util.Pools;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public final class SymbolicMesh extends CollisionShape<SymbolicMesh> {

	public final SymbolicMeshNode[] nodes;

	public final Triangle[] tris;

	private final Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
	private final Vector3 position = new Vector3();
	private final Vector3 rotationVec = new Vector3();
	private final Matrix4 rotation = new Matrix4();
	private final Matrix4 rotationTra = new Matrix4();
	private final Matrix4 combined = new Matrix4();
	private final Matrix4 inverse = new Matrix4();

	private final Octtree<Short> octtree;

	private final float minx;
	private final float miny;
	private final float minz;

	private final float maxx;
	private final float maxy;
	private final float maxz;

	public final Box box = new Box();

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
		
		octtree = new Octtree<Short>(null, new Vector3(minx, miny, minz), new Vector3(maxx, maxy, maxz));
		//octtree.divide(2);

		for (short i = 0; i < tris.length; i++)
		{			
			octtree.add(i, tris[i]);
			tris[i].calculateBoundingBox();
		}

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
		combined.setToTranslation(position).scale(scale.x, scale.y, scale.z).mul(rotation);
		rotationTra.set(rotation).tra();
		inverse.set(rotationTra).scale(1.0f/scale.x, 1.0f/scale.y, 1.0f/scale.z).translate(-position.x, -position.y, -position.z);
		Pools.free(tmpMat);
	}

	public Matrix4 getCombined()
	{
		return combined;
	}

	@Override
	public Vector3 getPosition()
	{
		return position;
	}

	@Override
	public void calculateBoundingBox() {
		box.width = (maxx-minx)/2.0f;
		box.height = (maxy-miny)/2.0f;
		box.depth = (maxz-minz)/2.0f;

		box.center.set(minx+box.width, miny+box.height, minz+box.depth).mul(combined);
	}

	@Override
	public boolean checkBoundingBox(Box box) {
		return this.box.collide(box);
	}
	
	@Override
	public BoundingBox getBoundingBox(BoundingBox bb) {
		return box.getBoundingBox(bb);
	}

	@Override
	public Box getBoundingBox() {
		return box;
	}

	@Override
	public void setScaling(Vector3 scale) {
		if (this.scale.x == scale.x && this.scale.y == scale.y && this.scale.z == scale.z) return;

		this.scale.set(scale);

		updateMatrixes();
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
	public boolean collide(SymbolicMesh mesh) {
		return false;
	}

	@Override
	public SymbolicMesh set(SymbolicMesh other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setGeneric(CollisionShape<?> other) {
		set((SymbolicMesh)other);
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
	public void transformScaling(float scale) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public SymbolicMesh obtain() {
		return this;
	}

	@Override
	public void free() {
		//throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		//throw new UnsupportedOperationException();
	}

	@Override
	protected String string() {
		return "SymbolicMesh";
	}

	public boolean checkCollision(CollisionShape<?> shape, boolean fast)
	{	
		shape.transformPosition(inverse);
		shape.transformDirection(rotationTra);
		shape.transformScaling(1.0f/scale.x);
		//shape.calculateBoundingBox();

		CollisionShape<?> check = shape.obtain();

		if (!checkCollisionIterative(shape, check, fast)) {
			check.free();
			shape.transformPosition(combined);
			shape.transformDirection(rotation);
			shape.transformScaling(scale.x);

			return false;
		}

		check.free();
		shape.transformPosition(combined);
		shape.transformDirection(rotation);
		shape.transformScaling(scale.x);
		//shape.calculateBoundingBox();

		return true;
	}

	private boolean checkCollisionIterative(CollisionShape<?> shape, CollisionShape<?> check, boolean fast)
	{
		Stack<Octtree<Short>> openList = Pools.obtain(Stack.class);
		openList.clear();
		openList.push(octtree);
		boolean collide = false;

		while (!openList.empty())
		{
			Octtree<Short> partition = openList.pop();

			check.reset();
			if (!partition.box.collide(check))
			{
				continue;
			}

			boolean tcollide = false;
			for (OcttreeEntry<Short, CollisionShape<?>> s : partition.elements) if (shape.collide(tris[s.e])) tcollide = true;
			if (tcollide) collide = true;

			if (fast && collide) break;

			if (partition.children != null) for (Octtree<Short> p : partition.children)
			{
				check.reset();
				if (p.numChildElements != 0 && p.elements.size > 0 && p.box.collide(check)) 
					openList.push(p);
			}
		}

		Pools.free(openList);

		return collide;
	}

	public static SymbolicMesh getSymbolicMesh(Mesh mesh)
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
		
		if (triangles == 0) return SymbolicMesh.getSymbolicMesh(verts);

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

			if (!t.isLine()) tris.add(t);
		}

		Triangle[] triArray = new Triangle[tris.size()];
		tris.toArray(triArray);

		return new SymbolicMesh(vertexNodes, triArray);
	}

	public static SymbolicMesh getSymbolicMesh(float[] vertices, int[] indices)
	{
		final int vertCount = vertices.length / 3;
		final int vertexSize = 3;
		final int triangles = indices.length / 3;

		final SymbolicMeshNode[] vertexNodes = new SymbolicMeshNode[vertCount];

		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new SymbolicMeshNode(
					vertices[(i*vertexSize)+0],
					vertices[(i*vertexSize)+1],
					vertices[(i*vertexSize)+2]
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
			t.calculateBoundingBox();

			if (!t.isLine()) tris.add(t);
		}

		Triangle[] triArray = new Triangle[tris.size()];
		tris.toArray(triArray);

		return new SymbolicMesh(vertexNodes, triArray);
	}
	
	public static SymbolicMesh getSymbolicMesh(float[] vertices)
	{
		final int vertCount = vertices.length / 3;
		final int vertexSize = 3;
		final int triangles = vertCount / 3;

		final SymbolicMeshNode[] vertexNodes = new SymbolicMeshNode[vertCount];

		for (int i = 0; i < vertCount; i++)
		{
			vertexNodes[i] = new SymbolicMeshNode(
					vertices[(i*vertexSize)+0],
					vertices[(i*vertexSize)+1],
					vertices[(i*vertexSize)+2]
					);
		}

		final List<Triangle> tris = new ArrayList<Triangle>(triangles);

		for (int i = 0; i < triangles; i++)
		{
			final SymbolicMeshNode n1 = vertexNodes[(i*3)+0];
			final SymbolicMeshNode n2 = vertexNodes[(i*3)+1];
			final SymbolicMeshNode n3 = vertexNodes[(i*3)+2];

			n1.addNodes(n2, n3);
			n2.addNodes(n1, n3);
			n3.addNodes(n1, n2);

			Triangle t = new Triangle();
			t.v1.set(n1.x, n1.y, n1.z);
			t.v2.set(n2.x, n2.y, n2.z);
			t.v3.set(n3.x, n3.y, n3.z);
			t.calculateBoundingBox();

			if (!t.isLine()) tris.add(t);
		}

		Triangle[] triArray = new Triangle[tris.size()];
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


}
