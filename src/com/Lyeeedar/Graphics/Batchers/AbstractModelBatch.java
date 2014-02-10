package com.Lyeeedar.Graphics.Batchers;

import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class AbstractModelBatch implements Batch {
	
	public boolean drawing;
	protected DrawableManager drawableManager = new DrawableManager();
	
	public AbstractModelBatch()
	{
	}

	public void add (Mesh mesh, int primitiveType, Texture[] textures, Vector3 colour, Matrix4 model_matrix, int type, Camera cam) {
		drawableManager.add(mesh, primitiveType, textures, colour, model_matrix, type, cam);
	}
	
	public void flushNoClear(LightManager lights, Camera cam) {
		drawableManager.drawables.sort();
		lights.sort(cam.position);
		render(lights, cam);
		drawing = false;
	}
	
	public void flush(LightManager lights, Camera cam) {
		drawableManager.drawables.sort();
		lights.sort(cam.position);
		render(lights, cam);
		drawing = false;
		drawableManager.clear();
	}
	
	protected abstract void render(LightManager lights, Camera cam);
	public abstract void dispose();
	
	/**
	 * Class used to manage objects to be drawn. All relevant data is stored in Drawable objects that
	 *  can be sorted by distance and texture to minimise context changes.
	 * @author Philip
	 *
	 */
	public static class DrawableManager {
		Pool<Drawable> drawablePool = new Pool<Drawable>() {
			@Override
			protected Drawable newObject () {
				return new Drawable();
			}
		};

		Array<Drawable> drawables = new Array<Drawable>();

		/**
		 * Add a drawable object into the list of drawables
		 * @param mesh
		 * @param primitiveType
		 * @param texture
		 * @param colour
		 * @param model_matrix
		 */
		public void add (Mesh mesh, int primitiveType, Texture[] textures, Vector3 colour, Matrix4 model_matrix, int type, Camera cam) {
			Drawable drawable = drawablePool.obtain();
			drawable.setCommon(mesh, primitiveType, textures, colour, model_matrix, type, cam);
			drawables.add(drawable);
		}

		public void clear () {
			clear(drawables);
		}

		private void clear (Array<Drawable> drawables) {
			while (drawables.size > 0) {
				final Drawable drawable = drawables.pop();
				drawablePool.free(drawable);
			}
		}

		/**
		 * A class used to hold all the information about an object that is relevant to rendering it.
		 * @author Philip
		 *
		 */
		class Drawable implements Comparable<Drawable> {
			Mesh mesh;
			final Matrix4 model_matrix = new Matrix4();
			
			int type;

			float distance;			
			int primitiveType;
			
			final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
			Texture[] textures;
			int textureHash;
						
			final Vector3 tmp = new Vector3();

			public void setCommon (Mesh mesh, int primitiveType, Texture[] textures, Vector3 colour, Matrix4 model_matrix, int type, Camera cam) {
				
				this.type = type;
				this.mesh = mesh;
				this.primitiveType = primitiveType;
				if (colour != null) this.colour.set(colour);
				else this.colour.set(1.0f, 1.0f, 1.0f);
				this.textures = textures;
				textureHash = textures[0].hashCode();
				this.model_matrix.set(model_matrix);
				
				tmp.set(0, 0, 0).mul(model_matrix);
				distance = tmp.dst2(cam.position);

			}

			@Override
			public int compareTo (Drawable other) {
				if (other.distance == distance) return other.textureHash - textureHash;
				else return (int) (other.distance - this.distance)*10;
			}
		}
	}
}
