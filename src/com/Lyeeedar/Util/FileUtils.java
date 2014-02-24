package com.Lyeeedar.Util;

import java.util.HashMap;
import java.util.Map.Entry;

import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.UBJsonReader;

public class FileUtils {
	
	private static final HashMap<String, float[][]> cachedVertexArrays = new HashMap<String, float[][]>();
	public static float[][] getVertexArray(String name)
	{
		return cachedVertexArrays.get(name);
	}
	public static void storeVertexArray(String name, float[][] varray)
	{
		cachedVertexArrays.put(name, varray);
	}
	
	private static final HashMap<String, Sound> loadedSounds = new HashMap<String, Sound>();
	
	public static Sound loadSound(String location)
	{
		if (loadedSounds.containsKey(location))
		{
			return loadedSounds.get(location);
		}
		
		Sound s = Gdx.audio.newSound(Gdx.files.internal(location));
		
		loadedSounds.put(location, s);
		
		return s;
	}
	
	private static final HashMap<String, HashMap<Integer, BitmapFont[]>> loadedFonts = new HashMap<String, HashMap<Integer, BitmapFont[]>>();
	
	public static BitmapFont getFont(String location, int size, boolean flip)
	{
		if (!Gdx.files.internal(location).exists()) {
			throw new RuntimeException("Font "+location+" does not exist!");
		}
		
		BitmapFont font = null;
		if (loadedFonts.containsKey(location))
		{
			HashMap<Integer, BitmapFont[]> hash = loadedFonts.get(location);
			if (hash.containsKey(size))
			{
				BitmapFont[] block = hash.get(size);
				if (flip) font = block[1];
				else font = block[0];
				
				if (font == null)
				{
					FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(location));
					font = generator.generateFont(size, FreeTypeFontGenerator.DEFAULT_CHARS, flip);
					generator.dispose();
					
					if (flip) block[1] = font;
					else block[0] = font;
				}
			}
			else
			{
				FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(location));
				font = generator.generateFont(size, FreeTypeFontGenerator.DEFAULT_CHARS, flip);
				generator.dispose();
				BitmapFont[] block = new BitmapFont[2];
				if (flip) block[1] = font;
				else block[0] = font;
				hash.put(size, block);
			}
		}
		else
		{
			HashMap<Integer, BitmapFont[]> hash = new HashMap<Integer, BitmapFont[]>();
			loadedFonts.put(location, hash);
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(location));
			font = generator.generateFont(size, FreeTypeFontGenerator.DEFAULT_CHARS, flip);
			generator.dispose();
			BitmapFont[] block = new BitmapFont[2];
			if (flip) block[1] = font;
			else block[0] = font;
			hash.put(size, block);
		}
		return font;
	}
	
	private static HashMap<String, ParticleEffect> loadedEffects = new HashMap<String, ParticleEffect>();
	public static ParticleEffect loadParticleEffect(String name)
	{
		if (loadedEffects.containsKey(name))
		{
			return loadedEffects.get(name).copy();
		}
		
		if (!Gdx.files.internal(name).exists()) {
			throw new RuntimeException("Effect "+name+" does not exist!");
		}
		
		Json json = new Json();
		ParticleEffect effect = json.fromJson(ParticleEffect.class, Gdx.files.internal(name));
		effect.name = name;
		
		loadedEffects.put(name, effect);
		
		return effect;
	}
	
	private static HashMap<String, Array<ParticleEffect>> pooledEffects = new HashMap<String, Array<ParticleEffect>>();
	public static ParticleEffect obtainParticleEffect(String name)
	{
		Array<ParticleEffect> pool = null;
		if (pooledEffects.containsKey(name))
		{
			pool = pooledEffects.get(name);
			if (pool.size > 0)
			{
				return pool.removeIndex(0);
			}
		}
		
		ParticleEffect effect = loadParticleEffect(name);
		
		if (pool == null)
		{
			pool = new Array<ParticleEffect>(false, 16);
			pooledEffects.put(name, pool);
		}
		
		pool.add(effect);
		
		return effect;
	}
	public static void freeParticleEffect(ParticleEffect effect)
	{
		pooledEffects.get(effect.name).add(effect);
	}

	private static final HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();
	/**
	 * Tries to load the given texture. If set to urgent, will throw a runtime exception if this texture does not exist.
	 * @param textureName
	 * @param urgent
	 * @return
	 */
	public static Texture loadTexture(String textureName, boolean urgent, TextureFilter filter, TextureWrap wrap)
	{
		String textureLocation = textureName;
		
		if (loadedTextures.containsKey(textureLocation)) return loadedTextures.get(textureLocation);
		
		if (!Gdx.files.internal(textureLocation).exists()) {
			if (urgent) throw new RuntimeException("Texture "+textureLocation+" does not exist!");
			else return null;
		}
		
		Texture texture = new Texture(Gdx.files.internal(textureLocation), true);
		if (filter != null) texture.setFilter(filter, filter);
		if (wrap != null) texture.setWrap(wrap, wrap);
		
		loadedTextures.put(textureLocation, texture);
		
		return texture;
	}
	
	public static void unloadTextures()
	{
		for (Entry<String, Texture> entry : loadedTextures.entrySet())
		{
			entry.getValue().dispose();
		}
		loadedTextures.clear();
	}
	
	private static final HashMap<String, Pixmap> loadedPixmaps = new HashMap<String, Pixmap>();
	/**
	 * Tries to load the given pixmap. If set to urgent, will throw a runtime exception if this pixmap does not exist.
	 * @param name
	 * @param urgent
	 * @return
	 */
	public static Pixmap loadPixmap(String name, boolean urgent)
	{
		String location = name;
		
		if (loadedPixmaps.containsKey(location)) return loadedPixmaps.get(location);
		
		if (!Gdx.files.internal(location).exists()) {
			if (urgent) throw new RuntimeException("Pixmap "+location+" does not exist!");
			else return null;
		}
		
		Pixmap pixmap = new Pixmap(Gdx.files.internal(location));
		
		loadedPixmaps.put(location, pixmap);
		
		return pixmap;
	}
	
	public static void unloadPixmaps()
	{
		for (Entry<String, Pixmap> entry : loadedPixmaps.entrySet())
		{
			entry.getValue().dispose();
		}
		loadedPixmaps.clear();
	}
	
	private static final HashMap<String, Mesh> loadedMeshes = new HashMap<String, Mesh>();
	public static Mesh loadMesh(String meshName)
	{
		String meshLocation = meshName;
		
		if (loadedMeshes.containsKey(meshLocation)) return loadedMeshes.get(meshLocation);
		
		if (!Gdx.files.internal(meshLocation).exists()) {
			throw new RuntimeException("Mesh "+meshName+" does not exist!");
		}
		ObjLoader loader = new ObjLoader();
		Model model = loader.loadModel(Gdx.files.internal(meshLocation));
		Mesh mesh = model.meshes.get(0);
		
		loadedMeshes.put(meshLocation, mesh);
		
		return mesh;
	}
	
	public static void unloadMeshes()
	{
		for (Entry<String, Mesh> entry : loadedMeshes.entrySet())
		{
			entry.getValue().dispose();
		}
		loadedMeshes.clear();
	}
	
	private static final HashMap<String, Model> loadedModels = new HashMap<String, Model>();
	public static Model loadModel(String modelName)
	{
		String location = modelName;
		
		if (loadedModels.containsKey(location)) return loadedModels.get(location);
		
		if (!Gdx.files.internal(location).exists()) {
			throw new RuntimeException("Mesh "+modelName+" does not exist!");
		}
		G3dModelLoader loader = new G3dModelLoader(new UBJsonReader());
		Model model = loader.loadModel(Gdx.files.internal(location));
		
		loadedModels.put(location, model);
		
		return model;
	}
	
	public static void unloadModels()
	{
		for (Entry<String, Model> entry : loadedModels.entrySet())
		{
			entry.getValue().dispose();
		}
		loadedModels.clear();
	}
	
	private static final HashMap<String, TextureAtlas> loadedAtlases = new HashMap<String, TextureAtlas>();
	public static TextureAtlas loadAtlas(String atlasName)
	{
		String atlasLocation = atlasName;
		
		if (loadedMeshes.containsKey(atlasLocation)) return loadedAtlases.get(atlasLocation);
		
		if (!Gdx.files.internal(atlasLocation).exists()) {
			throw new RuntimeException("Atlas "+atlasName+" does not exist!");
		}
		
		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasLocation));
		
		loadedAtlases.put(atlasLocation, atlas);
		
		return atlas;
	}
	
	public static void unloadAtlases()
	{
		for (Entry<String, TextureAtlas> entry : loadedAtlases.entrySet())
		{
			entry.getValue().dispose();
		}
		loadedAtlases.clear();
	}
}
