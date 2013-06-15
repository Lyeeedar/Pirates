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
		return sphere1.center.dst2(sphere2.center) < (sphere1.radius+sphere2.radius)*(sphere1.radius+sphere2.radius);		
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

	public static boolean collide(Sphere sphere1, Triangle tri1)
	{
		float r2 = sphere1.radius*sphere1.radius;
		
		ThreadSafePlane plane = Pools.obtain(ThreadSafePlane.class);
		plane.set(tri1.v1, tri1.v2, tri1.v3);
		
		// Check plane
		if (plane.distance(sphere1.center) > r2) {
			Pools.free(plane);
			return false;
		}
		
		// Check face
		Vector3 point = Pools.obtain(Vector3.class);
		plane.getClosestPoint(sphere1.center, point);
		
		Pools.free(plane);
		
		if (testPointInTriangle(point, tri1))
		{
			Pools.free(point);
			return true;
		}
		
		Pools.free(point);
		
		// Check 3 vertices
		if (sphere1.center.dst2(tri1.v1) < r2) return true;
		if (sphere1.center.dst2(tri1.v2) < r2) return true;
		if (sphere1.center.dst2(tri1.v3) < r2) return true;
		
		// Check 3 edges
		CollisionRay ray = Pools.obtain(CollisionRay.class);
		
		ray.reset();
		ray.set(tri1.v1, tri1.v2);
		if (collide(sphere1, ray)) {
			Pools.free(ray);
			return true;
		}
		
		ray.reset();
		ray.set(tri1.v1, tri1.v3);
		if (collide(sphere1, ray)) {
			Pools.free(ray);
			return true;
		}
		
		ray.reset();
		ray.set(tri1.v2, tri1.v3);
		if (collide(sphere1, ray)) {
			Pools.free(ray);
			return true;
		}
		
		Pools.free(ray);
		return false;
	}

	public static boolean collide(Sphere sphere1, CollisionRay ray1)
	{
		if (sphere1.center.dst2(ray1.ray.origin) < sphere1.radius*sphere1.radius)
		{
			ray1.intersection.set(ray1.ray.origin);
			ray1.dist = 0;
			
			return true;
		}
		
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
	    
	    float t2 = t*t;
	    
	    if (t2 > ray1.len)
	    {
	    	return false;
	    }	    
	    else if (t2 < ray1.dist)
	    {
	    	ray1.dist = t2;
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
		Vector3 lb = Pools.obtain(Vector3.class).set(box1.center).sub(box1.width, box1.height, box1.depth);
		Vector3 rt = Pools.obtain(Vector3.class).set(box1.center).add(box1.width, box1.height, box1.depth);

		if (
			ray1.ray.origin.x > lb.x && ray1.ray.origin.x < rt.x &&
			ray1.ray.origin.y > lb.y && ray1.ray.origin.y < rt.y &&
			ray1.ray.origin.z > lb.z && ray1.ray.origin.z < rt.z
			)
		{
			ray1.intersection.set(ray1.ray.origin);
			ray1.dist = 0;
			
			Pools.free(lb);
			Pools.free(rt);
			return true;
		}

		Vector3 dirfrac = Pools.obtain(Vector3.class);
		// r.dir is unit direction vector of ray
		dirfrac.x = 1.0f / ray1.ray.direction.x;
		dirfrac.y = 1.0f / ray1.ray.direction.y;
		dirfrac.z = 1.0f / ray1.ray.direction.z;
		
		// lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
		// r.org is origin of ray
		float t1 = (lb.x - ray1.ray.origin.x)*dirfrac.x;
		float t2 = (rt.x - ray1.ray.origin.x)*dirfrac.x;
		float t3 = (lb.y - ray1.ray.origin.y)*dirfrac.y;
		float t4 = (rt.y - ray1.ray.origin.y)*dirfrac.y;
		float t5 = (lb.z - ray1.ray.origin.z)*dirfrac.z;
		float t6 = (rt.z - ray1.ray.origin.z)*dirfrac.z;
		
		Pools.free(lb);
		Pools.free(rt);

		float tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6));
		float tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6));

		float t = 0;
		
		// if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
		if (tmax < 0)
		{
		    t = tmax;
		    return false;
		}

		// if tmin > tmax, ray doesn't intersect AABB
		if (tmin > tmax)
		{
		    t = tmax;
		    return false;
		}
		else
		{
			t = tmin;
		}
		
		float tt = t*t;
		
		if (tt < ray1.len && tt < ray1.dist)
		{
			ray1.dist = tt;
			ray1.intersection.set(ray1.ray.origin).add(dirfrac.set(ray1.ray.direction).scl(t));
			
			Pools.free(dirfrac);
			
			return true;
		}

		Pools.free(dirfrac);
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
		
		if (testPointInTriangle(i, tri1))
		{
			float dist = i.dst2(ray1.ray.origin);
			if (dist < ray1.len && dist < ray1.dist)
			{
				ray1.dist = dist;
				ray1.intersection.set(i);
				Pools.free(i);
				return true;
			}
		}
		Pools.free(i);
		return false;
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
	
	public static float max(float f1, float f2)
	{
		return (f1 < f2) ? f2 : f1 ;
	}
	
	public static float min(float f1, float f2)
	{
		return (f1 > f2) ? f2 : f1 ;
	}
	
	public static boolean testPointInTriangle(Vector3 point, Triangle tri)
	{
		Vector3 v0 = Pools.obtain(Vector3.class);
		Vector3 v1 = Pools.obtain(Vector3.class);
		Vector3 v2 = Pools.obtain(Vector3.class);

		v0.set(tri.v3).sub(tri.v1);
		v1.set(tri.v2).sub(tri.v1);
		v2.set(point).sub(tri.v1);

		float dot00 = v0.dot(v0);
		float dot01 = v0.dot(v1);
		float dot02 = v0.dot(v2);
		float dot11 = v1.dot(v1);
		float dot12 = v1.dot(v2);

		float denom = dot00 * dot11 - dot01 * dot01;
		if (denom == 0) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			return false;
		}

		float u = (dot11 * dot02 - dot01 * dot12) / denom;
		float v = (dot00 * dot12 - dot01 * dot02) / denom;

		if (u >= 0 && v >= 0 && u + v <= 1) {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			return true;
		} else {
			Pools.free(v0);
			Pools.free(v1);
			Pools.free(v2);
			return false;
		}
	}
	
	public static boolean collideShapeList(CollisionShape<?> shape, CollisionShape<?>[] list, short[] indices, boolean fast)
	{
		boolean hit = false;
		
		for (int i = 0; i < indices.length; i++) {
			boolean result = list[i].collide(shape);

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
}








