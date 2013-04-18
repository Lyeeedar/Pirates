package Entities;

import Entities.NavMesh.NavMeshNode;
import Entities.AI.AI_Package;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;
import com.lyeeedar.Pirates.GLOBALS;

public class GameEntity {
	
	private final Vector3 UP = new Vector3(GLOBALS.DEFAULT_UP);	
	private final Vector3 rotation = new Vector3(GLOBALS.DEFAULT_ROTATION);
	private final Vector3 position = new Vector3(0, 0, 0);
	private final Vector3 velocity = new Vector3(0, 0, 0);
	
	private AI_Package ai;
	
	public GameEntity()
	{
	}
	
	public void update(float delta)
	{
		if (ai != null) ai.update(delta);
	}

	public void Yrotate (float angle) {	
		Vector3 dir = Pools.obtain(Vector3.class);
		dir.set(rotation).nor();
		if(dir.y>-0.7 && angle<0 || dir.y<+0.7 && angle>0)
		{
			Vector3 localAxisX = Pools.obtain(Vector3.class).set(rotation);
			localAxisX.crs(UP).nor();
			rotate(localAxisX.x, localAxisX.y, localAxisX.z, angle);
			Pools.free(localAxisX);
		}
		Pools.free(dir);
	}

	public void Xrotate (float angle) {
		rotate(0, 1, 0, angle);
	}

	public void rotate (float x, float y, float z, float angle) {
		Vector3 axis = Pools.obtain(Vector3.class).set(x, y, z);
		Matrix4 tmpMat = Pools.obtain(Matrix4.class).idt();
		tmpMat.setToRotation(axis, angle);
		Pools.free(axis);
		rotation.mul(tmpMat).nor();
		UP.mul(tmpMat).nor();
		Pools.free(tmpMat);
	}
	
	public void translate(float x, float y, float z)
	{
		positionAbsolutely(position.x+x, position.y+y, position.z+z);
	}
	
	public void positionAbsolutely(float x, float y, float z)
	{
		position.set(x, y, z);
	}

	public void positionYAbsolutely(float y)
	{
		positionAbsolutely(position.x, y, position.z);
	}
	
	public void left_right(float mag)
	{
		velocity.x += (float)Math.sin(rotation.z) * mag;
		velocity.z += -(float)Math.sin(rotation.x) * mag;
	}

	public void forward_backward(float mag)
	{
		velocity.x += (float)Math.sin(rotation.x) * mag;
		velocity.z += (float)Math.sin(rotation.z) * mag;
	}
	
	private final Ray ray = new Ray(new Vector3(), new Vector3());
	private final Vector3 collision = new Vector3();
	public void applyVelocity(float delta)
	{
		Vector3 v = Pools.obtain(Vector3.class).set(velocity.x, (velocity.y + GLOBALS.GRAVITY*delta), velocity.z);
		v.scl(delta);
		
		Vector3 nPos = Pools.obtain(Vector3.class).set(position).add(v);

		ray.origin.set(position).add(0, 0.5f, 0);
		ray.direction.set(nPos).sub(position);
		
		ray.direction.set(nPos.x-position.x, 0, 0);
		if (v.x != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(nPos.x, position.y+0.5f, position.z, ray, collision) && collision.dst2(ray.origin) < 1)
		{
			velocity.x = 0;
			v.x = 0;
		}
		
		ray.direction.set(0, 0, nPos.z-position.z);
		if (v.z != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(position.x, position.y+0.5f, nPos.z, ray, collision) && collision.dst2(ray.origin) < 1)
		{
			velocity.z = 0;
			v.z = 0;
		}
		
		ray.direction.set(0, nPos.y-position.y, 0);
		if (v.y != 0 && GLOBALS.TEST_NAV_MESH.checkCollision(position.x, nPos.y, position.z, ray, collision) && collision.dst2(ray.origin) < 1)
		{
			velocity.y = 0;
			v.y = 0;
			positionYAbsolutely(collision.y+0.1f);
		}
		else if (nPos.y < -1)
		{
			velocity.y = 0;
			v.y = 0;
			positionYAbsolutely(-1);
		}
		
		translate(v.x, v.y, v.z);
		
		velocity.x = 0;
		velocity.z = 0;
		
		Pools.free(v);
		Pools.free(nPos);
	}

	public AI_Package getAi() {
		return ai;
	}

	public void setAi(AI_Package ai) {
		this.ai = ai;
	}

	public Vector3 getUP() {
		return UP;
	}

	public Vector3 getRotation() {
		return rotation;
	}

	public Vector3 getPosition() {
		return position;
	}

	public Vector3 getVelocity() {
		return velocity;
	}
}
