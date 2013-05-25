package com.lyeeedar.Pirates;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

public class ThreadSafePlane extends Plane {
	private static final long serialVersionUID = 6917699700986507282L;
	
	
	private final Vector3 l = new Vector3();
	private final Vector3 r = new Vector3();

	/** Constructs a new plane based on the normal and distance to the origin.
	 * 
	 * @param normal The plane normal
	 * @param d The distance to the origin */
	public ThreadSafePlane (Vector3 normal, float d) {
		super(normal, d);
	}

	/** Constructs a new plane based on the normal and a point on the plane.
	 * 
	 * @param normal The normal
	 * @param point The point on the plane */
	public ThreadSafePlane (Vector3 normal, Vector3 point) {
		super(normal, point);
	}

	/** Constructs a new plane out of the three given points that are considered to be on the plane. The normal is calculated via a
	 * cross product between (point1-point2)x(point2-point3)
	 * 
	 * @param point1 The first point
	 * @param point2 The second point
	 * @param point3 The third point */
	public ThreadSafePlane (Vector3 point1, Vector3 point2, Vector3 point3) {
		super(point1, point2, point3);
	}

	@Override
	public void set (Vector3 point1, Vector3 point2, Vector3 point3) {
		l.set(point1).sub(point2);
		r.set(point2).sub(point3);
		Vector3 nor = l.crs(r).nor();
		normal.set(nor);
		d = -point1.dot(nor);
	}
}
