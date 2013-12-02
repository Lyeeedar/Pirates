package com.Lyeeedar.Util;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.loaders.wavefront.ObjLoader;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

public class FileUtils {
	
	public static ParticleEffect loadParticleEffect(String name)
	{
		Json json = new Json();
		return json.fromJson(ParticleEffect.class, Gdx.files.internal(name));
	}

	public static HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();
	/**
	 * Tries to load the given texture. If set to urgent, will throw a runtime exception if this texture does not exist.
	 * @param textureName
	 * @param urgent
	 * @return
	 */
	public static Texture loadTexture(String textureName, boolean urgent)
	{
		String textureLocation = textureName;
		
		if (loadedTextures.containsKey(textureLocation)) return loadedTextures.get(textureLocation);
		
		if (!Gdx.files.internal(textureLocation).exists()) {
			if (urgent) throw new RuntimeException("Texture "+textureLocation+" does not exist!");
			else return null;
		}
		
		Texture texture = new Texture(Gdx.files.internal(textureLocation), true);
		
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
	
	public static HashMap<String, Pixmap> loadedPixmaps = new HashMap<String, Pixmap>();
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
	
	public static HashMap<String, Mesh> loadedMeshes = new HashMap<String, Mesh>();
	public static Mesh loadMesh(String meshName)
	{
		String meshLocation = meshName;
		
		if (loadedMeshes.containsKey(meshLocation)) return loadedMeshes.get(meshLocation);
		
		if (!Gdx.files.internal(meshLocation).exists()) {
			throw new RuntimeException("Mesh "+meshName+" does not exist!");
		}
		ObjLoader loader = new ObjLoader();
		StillModel model = loader.loadObj(Gdx.files.internal(meshLocation));
		Mesh mesh = model.subMeshes[0].mesh;
		
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
	
	public static HashMap<String, TextureAtlas> loadedAtlases = new HashMap<String, TextureAtlas>();
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
