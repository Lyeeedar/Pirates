package Graphics.Lights;

import com.badlogic.gdx.math.Vector3;

public class Light implements Comparable<Light> {
	
	public final Vector3 position = new Vector3();
	public final Vector3 direction = new Vector3();
	public final Vector3 colour = new Vector3();
	public float attenuation;
	
	public int distance = 0;
	
	public Light(Vector3 position, Vector3 colour, float attenuation)
	{
		this.position.set(position);
		this.colour.set(colour);
		this.attenuation = attenuation;
	}
	
	public Light(Vector3 position, Vector3 direction, Vector3 colour)
	{
		this.position.set(position);
		this.direction.set(direction);
		this.colour.set(colour);
	}

	@Override
	public int compareTo(Light other) {
		return distance - other.distance;
	}

}
