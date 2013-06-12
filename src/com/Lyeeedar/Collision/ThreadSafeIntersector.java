package com.Lyeeedar.Collision;

import com.Lyeeedar.Util.Pools;
import com.Lyeeedar.Util.ThreadSafePlane;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class ThreadSafeIntersector {

	// --------------------- SPHERE --------------------- //

	public static boolean collide(Sphere sphere1, Sphere sphere2)
	{
		return sphere1.center.dst2(sphere2.center) < Math.pow(sphere1.radius+sphere2.radius, 2);		
	}

	/**
	 * NEEDS LOTS OF OPTIMISING
	 * @param sphere1
	 * @param cylinder1
	 * @return
	 */
	public static boolean collide(Sphere sphere1, Cylinder cylinder1)
	{	
		Vector3 p2 = Pools.obtain(Vector3.class).set(cylinder1.rotation).scl(cylinder1.height);
		Vector3 p1 = Pools.obtain(Vector3.class).set(cylinder1.center).sub(p2);
		p2.add(cylinder1.center);
		
		Vector3 pp = Pools.obtain(Vector3.class);
		projectPointOntoLine(p1, p2, sphere1.center, pp);
		
		if (pp.dst2(sphere1.center) > (cylinder1.radius+sphere1.radius)*(cylinder1.radius+sphere1.radius)) {
			Pools.free(p1);
			Pools.free(p2);
			Pools.free(pp);
			return false;
		}
		
		float drh = pp.dst(cylinder1.center);
		float n = p1.dst(p2);
		if (n > cylinder1.radius) n = cylinder1.radius;
		
		float a = (float) Math.sqrt(Math.pow(sphere1.radius, 2) - Math.pow(sphere1.radius - (drh-n), 2));
		if (drh > cylinder1.height+a) {
			Pools.free(p1);
			Pools.free(p2);
			Pools.free(pp);
			return false;
		}

		Pools.free(p1);
		Pools.free(p2);
		Pools.free(pp);
		return true;
	}

	public static boolean collide(Sphere sphere1, Box box1)
	{
		float dist_squared = sphere1.radius * sphere1.radius;

		if (sphere1.center.x < box1.center.x-box1.width) dist_squared -= Math.pow(sphere1.center.x - (box1.center.x-box1.width), 2);
		else if (sphere1.center.x > box1.center.x+box1.width) dist_squared -= Math.pow(sphere1.center.x - (box1.center.x+box1.width), 2);

		if (sphere1.center.y < box1.center.y-box1.height) dist_squared -= Math.pow(sphere1.center.y - (box1.center.y-box1.height), 2);
		else if (sphere1.center.y > box1.center.y+box1.height) dist_squared -= Math.pow(sphere1.center.y - (box1.center.y+box1.height), 2);

		if (sphere1.center.z < box1.center.z-box1.height) dist_squared -= Math.pow(sphere1.center.z - (box1.center.z-box1.height), 2);
		else if (sphere1.center.z > box1.center.z+box1.height) dist_squared -= Math.pow(sphere1.center.z - (box1.center.z+box1.height), 2);

		return dist_squared > 0;
	}

	/**
	 * Source: http://realtimecollisiondetection.net/blog/?p=103
	 * @param sphere1
	 * @param tri1
	 * @return
	 */
	public static boolean collide(Sphere sphere1, Triangle tri1)
	{
		Vector3 tmp = Pools.obtain(Vector3.class);

		Vector3 A = Pools.obtain(Vector3.class).set(tri1.v1).sub(sphere1.center);
		Vector3 B = Pools.obtain(Vector3.class).set(tri1.v2).sub(sphere1.center);
		Vector3 C = Pools.obtain(Vector3.class).set(tri1.v3).sub(sphere1.center);

		float rr = sphere1.radius * sphere1.radius;
		Vector3 V = Pools.obtain(Vector3.class).set(B).sub(A).crs(tmp.set(C).sub(A));
		float d = A.dot(V);
		float e = V.dot(V);
		boolean sep1 = d * d > rr * e;

		float aa = A.dot(A);
		float ab = A.dot(B);
		float ac = A.dot(C);
		float bb = B.dot(B);
		float bc = B.dot(C);
		float cc = C.dot(C);

		boolean sep2 = (aa > rr) & (ab > aa) & (ac > aa);
		boolean sep3 = (bb > rr) & (ab > bb) & (bc > bb);
		boolean sep4 = (cc > rr) & (ac > cc) & (bc > cc);

		Vector3 AB = Pools.obtain(Vector3.class).set(B).sub(A);
		Vector3 BC = Pools.obtain(Vector3.class).set(C).sub(B);
		Vector3 CA = Pools.obtain(Vector3.class).set(A).sub(C);

		float d1 = ab - aa;
		float d2 = bc - bb;
		float d3 = ac - cc;

		float e1 = AB.dot(AB);
		float e2 = BC.dot(BC);
		float e3 = CA.dot(CA);

		Vector3 Q1 = Pools.obtain(Vector3.class).set(A).scl(e1).sub(tmp.set(AB).scl(d1));
		Vector3 Q2 = Pools.obtain(Vector3.class).set(B).scl(e2).sub(tmp.set(BC).scl(d2));
		Vector3 Q3 = Pools.obtain(Vector3.class).set(C).scl(e3).sub(tmp.set(CA).scl(d3));

		Vector3 QC = Pools.obtain(Vector3.class).set(C).scl(e1).sub(Q1);
		Vector3 QA = Pools.obtain(Vector3.class).set(A).scl(e2).sub(Q2);
		Vector3 QB = Pools.obtain(Vector3.class).set(B).scl(e3).sub(Q3);

		boolean sep5 = Q1.dot(Q1) > rr * e1 * e1 && Q1.dot(QC) > 0;
		boolean sep6 = Q2.dot(Q2) > rr * e2 * e2 && Q2.dot(QA) > 0;
		boolean sep7 = Q3.dot(Q3) > rr * e3 * e3 && Q3.dot(QB) > 0;

		Pools.free(tmp);

		Pools.free(A);
		Pools.free(B);
		Pools.free(C);
		Pools.free(V);

		Pools.free(AB);
		Pools.free(BC);
		Pools.free(CA);

		Pools.free(Q1);
		Pools.free(Q2);
		Pools.free(Q3);

		Pools.free(QA);
		Pools.free(QB);
		Pools.free(QC);

		return sep1 || sep2 || sep3 || sep4 || sep5 || sep6 || sep7;
	}

	public static boolean collide(Sphere sphere1, CollisionRay ray1)
	{
		Vector3 o = Pools.obtain(Vector3.class).set(ray1.ray.origin).sub(sphere1.center);
		//Compute A, B and C coefficients
	    float a = ray1.ray.direction.dot(ray1.ray.direction);
	    float b = 2 * ray1.ray.direction.dot(o);
	    float c = o.dot(o) - (sphere1.radius * sphere1.radius);

	    //Find discriminant
	    float disc = b * b - 4 * a * c;
	    
	    // if discriminant is negative there are no real roots, so return 
	    // false as ray misses sphere
	    if (disc < 0)
	        return false;

	    // compute q as described above
	    float distSqrt = (float) Math.sqrt(disc);
	    float q;
	    if (b < 0)
	        q = (-b - distSqrt)/2.0f;
	    else
	        q = (-b + distSqrt)/2.0f;

	    // compute t0 and t1
	    float t0 = q / a;
	    float t1 = c / q;

	    // make sure t0 is smaller than t1
	    if (t0 > t1)
	    {
	        // if t0 is bigger than t1 swap them around
	        float temp = t0;
	        t0 = t1;
	        t1 = temp;
	    }

	    // if t1 is less than zero, the object is in the ray's negative direction
	    // and consequently the ray misses the sphere
	    if (t1 < 0)
	        return false;

	    float t;
	    // if t0 is less than zero, the intersection point is at t1
	    if (t0 < 0)
	    {
	        t = t1;
	    }
	    // else the intersection point is at t0
	    else
	    {
	        t = t0;
	    }
	    
	    if (t < ray1.dist)
	    {
	    	ray1.dist = t;
		    ray1.intersection.set(ray1.ray.direction).scl(t).add(ray1.ray.origin);
	    }
	    
	    return true;
	}
	
	// --------------------- CYLINDER --------------------- //

	public static boolean collide(Cylinder cylinder1, Sphere sphere1)
	{
		return collide(sphere1, cylinder1);
	}

	public static boolean collide(Cylinder cylinder1, Cylinder cylinder2)
	{
		Box box1 = Pools.obtain(Box.class).set(cylinder1.center, cylinder1.radius, cylinder1.height, cylinder1.radius);
		Box box2 = Pools.obtain(Box.class).set(cylinder2.center, cylinder2.radius, cylinder2.height, cylinder2.radius);

		if (!collide(box1, box2)) {
			Pools.free(box1);
			Pools.free(box2);
			return false;
		}
		Pools.free(box1);
		Pools.free(box2);

		return cylinder1.center.dst2(cylinder2.center) < Math.pow(cylinder1.radius+cylinder2.radius, 2);
	}

	public static boolean collide(Cylinder cylinder1, Box box1)
	{
		Box box2 = Pools.obtain(Box.class).set(cylinder1.center, cylinder1.radius, cylinder1.height, cylinder1.radius);

		if (!collide(box1, box2)) {
			Pools.free(box2);
			return false;
		}
		Pools.free(box2);

		float dist_squared = cylinder1.radius * cylinder1.radius;

		if (cylinder1.center.x < box1.center.x-box1.width) dist_squared -= Math.pow(cylinder1.center.x - (box1.center.x-box1.width), 2);
		else if (cylinder1.center.x > box1.center.x+box1.width) dist_squared -= Math.pow(cylinder1.center.x - (box1.center.x+box1.width), 2);

		if (cylinder1.center.z < box1.center.z-box1.height) dist_squared -= Math.pow(cylinder1.center.z - (box1.center.z-box1.height), 2);
		else if (cylinder1.center.z > box1.center.z+box1.height) dist_squared -= Math.pow(cylinder1.center.z - (box1.center.z+box1.height), 2);

		return dist_squared > 0;
	}

	public static boolean collide(Cylinder cylinder1, Triangle tri1)
	{
		return false;
	}

	public static boolean collide(Cylinder cylinder1, CollisionRay ray1)
	{
		return false;
	}

	// --------------------- BOX --------------------- //

	public static boolean collide(Box box1, Sphere sphere1)
	{
		return collide(sphere1, box1);
	}

	public static boolean collide(Box box1, Cylinder cylinder1)
	{
		return collide(cylinder1, box1);
	}

	public static boolean collide(Box box1, Box box2)
	{
		return (
				(box1.center.x-box1.width) < (box2.center.x+box2.width) && // 1.minx < 2.maxx
				(box1.center.x+box1.width) > (box2.center.x-box2.width) && // 1.maxx > 2.minx

				(box1.center.y-box1.height) < (box2.center.y+box2.height) && // 1.miny < 2.maxy
				(box1.center.y+box1.height) > (box2.center.y-box2.height) && // 1.maxy > 2.miny

				(box1.center.z-box1.depth) < (box2.center.z+box2.depth) && // 1.minz < 2.maxz
				(box1.center.z+box1.depth) > (box2.center.z-box2.depth)    // 1.maxz > 2.minz
				);
	}

	/**
	 * Source: http://fileadmin.cs.lth.se/cs/Personal/Tomas_Akenine-Moller/code/tribox2.txt
	 * @param box1
	 * @param tri1
	 * @return
	 */
	public static boolean collide(Box box1, Triangle tri1)
	{
		/*    use separating axis theorem to test overlap between triangle and box */
		/*    need to test for overlap in these directions: */
		/*    1) the {x,y,z}-directions (actually, since we use the AABB of the triangle */
		/*       we do not even need to test these) */
		/*    2) normal of the triangle */
		/*    3) crossproduct(edge from tri, {x,y,z}-directin) */
		/*       this gives 3x3=9 more tests */

		float min,max,d,p0,p1,p2,rad,fex,fey,fez;

		/* This is the fastest branch on Sun */
		/* move everything so that the boxcenter is in (0,0,0) */
		Vector3 v0 = Pools.obtain(Vector3.class).set(tri1.v1).sub(box1.center);
		Vector3 v1 = Pools.obtain(Vector3.class).set(tri1.v2).sub(box1.center);
		Vector3 v2 = Pools.obtain(Vector3.class).set(tri1.v3).sub(box1.center);

		/* compute triangle edges */
		Vector3 e0 = Pools.obtain(Vector3.class).set(v1).sub(v0);		/* tri edge 0 */
		Vector3 e1 = Pools.obtain(Vector3.class).set(v2).sub(v1);		/* tri edge 1 */
		Vector3 e2 = Pools.obtain(Vector3.class).set(v0).sub(v2);		/* tri edge 2 */

		/* Bullet 3:  */
		/*  test the 9 tests first (this was faster) */

		fex = Math.abs(e0.x);
		fey = Math.abs(e0.y);
		fez = Math.abs(e0.z);

		// AXISTEST_X01
		p0 = e0.z*v0.y - e0.y*v0.z;
		p2 = e0.z*v2.y - e0.y*v2.z;
		if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;}
		rad = fez * box1.height + fey * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEST_Y02
		p0 = -e0.y*v0.x + e0.x*v0.z;
		p2 = -e0.z*v2.x + e0.x*v2.z;
		if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;}
		rad = fez * box1.width + fex * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEXT_Z12
		p1 = e0.y*v1.x - e0.x*v1.y;
		p2 = e0.y*v2.x - e0.x*v2.y;
		if(p2<p1) {min=p2; max=p1;} else {min=p1; max=p2;}
		rad = fey * box1.width + fex * box1.height;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		fex = Math.abs(e1.x);
		fey = Math.abs(e1.y);
		fez = Math.abs(e1.z);

		//AXISTEXT_X01
		p0 = e1.z*v0.y - e1.y*v0.z;
		p2 = e1.z*v2.y - e1.y*v2.z;
		if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;}
		rad = fez *box1.height + fey * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEST_Y02
		p0 = -e1.z*v0.x + e1.x*v0.z;
		p2 = -e1.z*v2.x + e1.x*v2.z;
		if(p0<p2) {min=p0; max=p2;} else {min=p2; max=p0;}
		rad = fez * box1.width + fex * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEST_Z0
		p0 = e1.y*v0.x - e1.x*v0.y;
		p1 = e1.y*v1.x - e1.x*v1.y;
		if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;}
		rad = fey * box1.width + fex * box1.height;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		fex = Math.abs(e2.x);
		fey = Math.abs(e2.y);
		fez = Math.abs(e2.z);

		//AXISTEST_X2
		p0 = e2.z*v0.y - e2.y*v0.z;
		p1 = e2.z*v1.y - e2.y*v1.z;
		if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;}
		rad = fez * box1.height + fey * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEST_Y1
		p0 = -e2.z*v0.x + e2.x*v0.z;
		p1 = -e2.z*v1.x + e2.x*v1.z;
		if(p0<p1) {min=p0; max=p1;} else {min=p1; max=p0;}
		rad = fez * box1.width + fex * box1.depth;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		//AXISTEST_Z12
		p1 = e2.y*v1.x - e2.x*v1.y;
		p2 = e2.y*v2.x - e2.x*v2.y;
		if(p2<p1) {min=p2; max=p1;} else {min=p1; max=p2;}
		rad = fey * box1.width + fex * box1.height;
		if(min>rad || max<-rad) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		/* Bullet 1: */
		/*  first test overlap in the {x,y,z}-directions */
		/*  find min, max of the triangle each direction, and test for overlap in */
		/*  that direction -- this is equivalent to testing a minimal AABB around */
		/*  the triangle against the AABB */

		/* test in X-direction */
		min = v0.x;
		if (v1.x < min) min = v1.x;
		if (v2.x < min) min = v2.x;
		max = v0.x;
		if (v1.x > max) max = v1.x;
		if (v2.x > max) max = v2.x;
		if(min>box1.width || max<-box1.width) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		/* test in Y-direction */
		min = v0.y;
		if (v1.y < min) min = v1.y;
		if (v2.y < min) min = v2.y;
		max = v0.y;
		if (v1.y > max) max = v1.y;
		if (v2.y > max) max = v2.y;
		if(min>box1.height || max<-box1.height) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		/* test in Z-direction */
		min = v0.z;
		if (v1.z < min) min = v1.z;
		if (v2.z < min) min = v2.z;
		max = v0.z;
		if (v1.z > max) max = v1.z;
		if (v2.z > max) max = v2.z;
		if(min>box1.depth || max<-box1.depth) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}

		/* Bullet 2: */
		/*  test if the box intersects the plane of the triangle */
		/*  compute plane equation of triangle: normal*x+d=0 */
		Vector3 normal = e0.crs(e1);
		d=-normal.dot(v0);  /* plane eq: normal.x+d=0 */

		Vector3 vmin = v0;
		Vector3 vmax = v1;
		if(normal.x>0.0f)
		{
			vmin.x=-box1.width;
			vmax.x=box1.width;
		}
		else
		{
			vmin.x=box1.width;
			vmax.x=-box1.width;
		}

		if(normal.y>0.0f)
		{
			vmin.y=-box1.height;
			vmax.y=box1.height;
		}
		else
		{
			vmin.y=box1.height;
			vmax.y=-box1.height;
		}
		if(normal.z>0.0f)
		{
			vmin.z=-box1.depth;
			vmax.z=box1.depth;
		}
		else
		{
			vmin.z=box1.depth;
			vmax.z=-box1.depth;
		}
		if(normal.dot(vmin)+d>0.0f) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return false;
		}
		if(normal.dot(vmax)+d>=0.0f) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			Pools.free(e0);
			Pools.free(e1);
			Pools.free(e2);
			return true;
		}

		Pools.free(v0);
		Pools.free(v1);
		Pools.free(v2);
		Pools.free(e0);
		Pools.free(e1);
		Pools.free(e2);
		return false;
	}

	public static boolean collide(Box box1, CollisionRay ray1)
	{
		return false;
	}

	// --------------------- TRIANGLE --------------------- //

	public static boolean collide(Triangle tri1, Sphere sphere1)
	{
		return collide(sphere1, tri1);
	}

	public static boolean collide(Triangle tri1, Cylinder cylinder1)
	{
		return collide(cylinder1, tri1);
	}

	public static boolean collide(Triangle tri1, Box box1)
	{
		return collide(box1, tri1);
	}

	public static boolean collide(Triangle tri1, Triangle tri2)
	{
		return false;
	}

	public static boolean collide(Triangle tri1, CollisionRay ray1)
	{
		ThreadSafePlane p = Pools.obtain(ThreadSafePlane.class);
		Vector3 i = Pools.obtain(Vector3.class);

		p.set(tri1.v1, tri1.v2, tri1.v3);
		if (!intersectRayPlane(ray1.ray, p, i)) {
			Pools.free(p);
			Pools.free(i);
			return false;
		}
		Pools.free(p);

		Vector3 v0 = Pools.obtain(Vector3.class);
		Vector3 v1 = Pools.obtain(Vector3.class);
		Vector3 v2 = Pools.obtain(Vector3.class);

		v0.set(tri1.v3).sub(tri1.v1);
		v1.set(tri1.v2).sub(tri1.v1);
		v2.set(i).sub(tri1.v1);

		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);

		Pools.free(v0);
		Pools.free(v1);
		Pools.free(v2);

		float denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) {
			Pools.free(i);
			return false;
		}

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if (u >= 0 && v >= 0 && u + v <= 1) {
			float d = i.dst(ray1.intersection);
			if (d < ray1.dist)
			{
				ray1.dist = d;
				ray1.intersection.set(i);
			}
			Pools.free(i);
			return true;
		} else {
			Pools.free(i);
			return false;
		}
	}

	// --------------------- COLLISIONRAY --------------------- //

	public static boolean collide(CollisionRay ray1, Sphere sphere1)
	{
		return collide(sphere1, ray1);
	}

	public static boolean collide(CollisionRay ray1, Cylinder cylinder1)
	{
		return collide(cylinder1, ray1);
	}

	public static boolean collide(CollisionRay ray1, Box box1)
	{
		return collide(box1, ray1);
	}

	public static boolean collide(CollisionRay ray1, Triangle tri1)
	{
		return collide(tri1, ray1);
	}

	public static boolean collide(CollisionRay ray1, CollisionRay ray2)
	{
		return false;
	}

	// --------------------- OTHER --------------------- //

	public static Vector3 projectPointOntoLine(Vector3 l1, Vector3 l2, Vector3 point, Vector3 lpoint)
	{
		Vector3 e = Pools.obtain(Vector3.class).set(l2).sub(l1);
		float U = e.dot(lpoint.set(point).sub(l1)) / e.len2();
		lpoint.set(l1).add(e.scl(U));
		Pools.free(e);
		return lpoint;
	}

	public static boolean intersectRayPlane (Ray ray, ThreadSafePlane plane, Vector3 intersection) {
		float denom = ray.direction.dot(plane.getNormal());
		if (denom != 0) {
			float t = -(ray.origin.dot(plane.getNormal()) + plane.getD()) / denom;
			if (t < 0) return false;

			if (intersection != null) {
				Vector3 tmp = Pools.obtain(Vector3.class);
				intersection.set(ray.origin).add(tmp.set(ray.direction).scl(t));
				Pools.free(tmp);
			}
			return true;
		} else if (plane.testPoint(ray.origin) == Plane.PlaneSide.OnPlane) {
			if (intersection != null) intersection.set(ray.origin);
			return true;
		} else
			return false;
	}
}








