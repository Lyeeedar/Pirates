package com.Lyeeedar.Pirates;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Entities.Spells.Spell;
import com.Lyeeedar.Graphics.SkyBox;
import com.Lyeeedar.Graphics.Lights.LightManager;
import com.badlogic.gdx.math.Vector3;

public final class GLOBALS {
	
	public enum ELEMENTS
	{
		TEMPERATURE, // Hot, cold
		LIGHT, // Shadow, lasers, illusions
		LIFE, // Healing, necromancy
		GAIA, // Earth, plants, weather
		FORCE // Gravity, physical buffs
	}

	public static final float STEP = 0.5f;

	public static final int MAX_SPEED_X = 60;
	public static final int MAX_SPEED_Y = 100;
	public static final int MAX_SPEED_Z = 60;

	public static final Vector3 DEFAULT_UP = new Vector3(0, 1, 0);
	public static final Vector3 DEFAULT_ROTATION = new Vector3(0, 0, 1);

	public static final float GRAVITY = -85;

	public static final int[] RESOLUTION = {1920, 1080};
	public static final int[] SCREEN_SIZE = {800, 600};
	
	public static final float FOG_MIN = 2300.0f;
	public static final float FOG_MAX = 2500.0f;

	public static boolean ANDROID = false;
	
	public static float PROGRAM_TIME = 0.0f;
			
	public static SkyBox SKYBOX;
	public static EntityGraph WORLD = new EntityGraph(new Entity(), null, true);
	public static LinkedList<Spell> SPELLS = new LinkedList<Spell>();
	public static LightManager LIGHTS = new LightManager();
	
	public static ExecutorService threadPool = Executors.newFixedThreadPool(1);//Runtime.getRuntime().availableProcessors());
	public static LinkedList<Future<?>> futureList = new LinkedList<Future<?>>();	
	public static void submitTask(Runnable run)
	{
		futureList.add(threadPool.submit(run));
	}
	public static void submitTasks(LinkedList<Runnable> list)
	{
		for (Runnable run : list) futureList.add(threadPool.submit(run));
	}
	public static void waitTillTasksComplete()
	{
		for (Future<?> future : futureList)
		{
			try {
				future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		futureList.clear();
	}

//	public static AssetManager ASSET_MANAGER = new AssetManager();
//	public static void queue_all_assets()
//	{	
//		FileHandle path = Gdx.files.internal("data");
//		if (!path.isDirectory()) path = Gdx.files.internal("./bin/data");
//		if (!path.isDirectory())
//		{
//			CodeSource src = GLOBALS.class.getProtectionDomain().getCodeSource();
//			List<String> list = new ArrayList<String>();
//			if (src != null) {
//				URL jar = src.getLocation();
//				ZipInputStream zip = null;
//				try { zip = new ZipInputStream( jar.openStream()); }
//				catch (IOException e1) { e1.printStackTrace(); }
//				ZipEntry ze = null;
//
//				try {
//					while ((ze=zip.getNextEntry()) != null) {
//						String entryName = ze.getName();
//						if (entryName.startsWith("data")) list.add(entryName);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			loadFolder(list, "sprites", "png", Pixmap.class);
//			loadFolder(list, "textures", "png", Texture.class);
//			loadFolder(list, "music", "ogg", Music.class);
//			loadFolder(list, "sounds", "ogg", Sound.class);
//		}
//		else
//		{
//			loadFolder(path, "sprites", "png", Pixmap.class);
//			loadFolder(path, "textures", "png", Texture.class);
//			loadFolder(path, "music", "ogg", Music.class);
//			loadFolder(path, "sounds", "ogg", Sound.class);
//		}
//
//	}
//	private static <T> void loadFolder(FileHandle path, String folder, String extension, Class<T> type)
//	{
//		List<String> files = new ArrayList<String>();
//		FileHandle[] fileList = path.child(folder).list(extension);
//		for (FileHandle fh : fileList) {
//			String p = fh.path();
//			if (p.startsWith("./bin")) p = p.substring(6);
//			files.add(p);
//		}
//		load(files, type);
//	}
//	private static <T> void loadFolder(List<String> fileList, String folder, String extension, Class<T> type)
//	{
//		List<String> files = new ArrayList<String>();
//		for (String file : fileList) {
//			if (file.startsWith("data/"+folder) && file.endsWith(extension)) files.add(file);
//		}
//		load(files, type);
//	}
//
//	public static <T> void load(List<String> files, Class<T> type)
//	{
//		for (String f : files) ASSET_MANAGER.load(f, type);
//	}

	public static double angle(Vector3 v1, Vector3 v2, Vector3 tmp)
	{
		Vector3 referenceForward = v1;
		Vector3 referenceRight = tmp.set(GLOBALS.DEFAULT_UP).crs(referenceForward);
		Vector3 newDirection = v2;
		float angle = (float) Math.toDegrees(Math.acos(v1.dot(v2) / (v1.len()*v2.len())));
		float sign = (newDirection.dot(referenceRight) > 0.0f) ? 1.0f : -1.0f;
		float finalAngle = sign * angle;
		return finalAngle;
	}
	
	public static int numDigits(int i)
	{
		for (int digits = 1; digits < 100; digits++)
		{
			if (i < Math.pow(10, digits)) return digits;
		}
		return 0;
	}
}
