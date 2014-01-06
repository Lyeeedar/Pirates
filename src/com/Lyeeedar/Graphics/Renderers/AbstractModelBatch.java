package com.Lyeeedar.Graphics.Renderers;

import com.Lyeeedar.Graphics.Batch;
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
	
	public Camera cam;
	public boolean drawing;
	protected DrawableManager drawableManager = new DrawableManager();
	
	public AbstractModelBatch()
	{
	}

	public void add (Mesh mesh, int primitiveType, Texture texture, Vector3 colour, Matrix4 model_matrix, int type) {
		//if (cam != null) if (!cam.frustum.sphereInFrustum(attributes.getSortCenter(), attributes.getBoundingSphereRadius()*2)) return;
		drawableManager.add(mesh, primitiveType, texture, colour, model_matrix, type);
	}
	
	public void flush(LightManager lights) {
		drawableManager.drawables.sort();
		lights.sort(cam.position);
		render(lights);
		drawing = false;
		drawableManager.clear();
	}
	
	protected abstract void render(LightManager lights);
	public abstract void dispose();
	
	/**
	 * Class used to manage objects to be drawn. All relevant data is stored in Drawable objects that
	 *  can be sorted by distance and texture to minimise context changes.
	 * @author Philip
	 *
	 */
	class DrawableManager {
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
		public void add (Mesh mesh, int primitiveType, Texture texture, Vector3 colour, Matrix4 model_matrix, int type) {
			Drawable drawable = drawablePool.obtain();
			drawable.setCommon(mesh, primitiveType, texture, colour, model_matrix, type);
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
			private static final int PRIORITY_DISCRETE_STEPS = 256;
			Mesh mesh;
			final Matrix4 model_matrix = new Matrix4();
			
			int type;

			int distance;			
			int primitiveType;
			
			final Vector3 colour = new Vector3(1.0f, 1.0f, 1.0f);
			Texture texture;
			int textureHash;
						
			final Vector3 tmp = new Vector3();

			public void setCommon (Mesh mesh, int primitiveType, Texture texture, Vector3 colour, Matrix4 model_matrix, int type) {
				
				this.type = type;
				this.mesh = mesh;
				this.primitiveType = primitiveType;
				if (colour != null) this.colour.set(colour);
				else this.colour.set(1.0f, 1.0f, 1.0f);
				this.texture = texture;
				textureHash = texture.hashCode();
				this.model_matrix.set(model_matrix);
				
				tmp.set(0, 0, 0).mul(model_matrix);
				distance = (int)(PRIORITY_DISCRETE_STEPS * tmp.dst2(cam.position));

			}

			@Override
			public int compareTo (Drawable other) {
				if (other.distance == distance) return other.textureHash - textureHash;
				else return other.distance - this.distance;
			}
		}
	}
}
