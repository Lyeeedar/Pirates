package com.Lyeeedar.Pirates.ProceduralGeneration.LSystems;

import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Collision.BulletWorld;
import com.Lyeeedar.Collision.Octtree.OcttreeEntry;
import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Graphics.Queueables.ModelBatchInstance;
import com.Lyeeedar.Graphics.Queueables.ModelBatchInstance.ModelBatchData;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.CustomFunction;
import de.congrace.exp4j.CustomOperator;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.InvalidCustomFunctionException;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

public class VolumePartitioner
{
	public final VolumePartitioner parent;
	public final HashMap<String, JsonValue> methodTable;

	public final JsonValue rule;
	
	public JsonValue Mesh;

	public final Vector3 min = new Vector3();
	public final Vector3 max = new Vector3();

	public final Array<VolumePartitioner> children = new Array<VolumePartitioner>(false, 16);
	
	public final HashMap<String, String> defines = new HashMap<String, String>();
	public final HashMap<String, Double> variables = new HashMap<String, Double>();
	
	public String coordX = "X";
	public String coordY = "Y";
	public String coordZ = "Z";
	
	public Vector3 snap = new Vector3();

	public VolumePartitioner(Vector3 min, Vector3 max, JsonValue rule, HashMap<String, JsonValue> methodTable, VolumePartitioner parent)
	{
		this.min.set(min);
		this.max.set(max);
				
		this.rule = rule;
		this.methodTable = methodTable;
		this.parent = parent;
		
		if (parent != null)
		{
			for (Map.Entry<String, String> entry : parent.defines.entrySet())
			{
				defines.put(entry.getKey(), entry.getValue());
			}
			for (Map.Entry<String, Double> entry : parent.variables.entrySet())
			{
				variables.put(entry.getKey(), entry.getValue());
			}
			this.coordX = parent.coordX;
			this.coordY = parent.coordY;
			this.coordZ = parent.coordZ;
			this.snap.set(parent.snap);
		}
	}

	public void evaluate()
	{
		processRule(rule);
		for (VolumePartitioner vp : children) vp.evaluate();
	}

	public Matrix4 getLocalAxis(Matrix4 out)
	{
		Vector3 X = Pools.obtain(Vector3.class);
		Vector3 Y = Pools.obtain(Vector3.class);
		Vector3 Z = Pools.obtain(Vector3.class);
		
		Vector3 DX = Pools.obtain(Vector3.class).set(1, 0, 0);
		Vector3 DY = Pools.obtain(Vector3.class).set(0, 1, 0);
		Vector3 DZ = Pools.obtain(Vector3.class).set(0, 0, 1);
		
		getRotation("X", X);
		getRotation("Y", Y);
		getRotation("Z", X);
		
		out.idt().rotate(DX, X).rotate(DY, Y).rotate(DZ, Z);
		
		Pools.free(X);
		Pools.free(Y);
		Pools.free(Z);
		Pools.free(DX);
		Pools.free(DY);
		Pools.free(DZ);
		
		return out;
	}
	
	public Vector3 getRotation(String axis, Vector3 out)
	{
		String coord = null;
		if (axis.equalsIgnoreCase("X")) coord = coordX;
		else if (axis.equalsIgnoreCase("Y")) coord = coordY;
		else if (axis.equalsIgnoreCase("Z")) coord = coordZ;
		else throw new RuntimeException("Invalid axis: "+axis);
		
		out.set(0, 0, 0);
		float val = coord.startsWith("-") ? -1 : 1 ;
		if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("X")) out.x = val;
		else if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("Y")) out.y = val;
		else if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("Z")) out.z = val;
		else throw new RuntimeException("Invalid coord: "+coord);
		
		return out;
	}
	
	public String getCoord(String coord)
	{
		if (coord.equalsIgnoreCase("X")) return coordX;
		if (coord.equalsIgnoreCase("Y")) return coordY;
		if (coord.equalsIgnoreCase("Z")) return coordZ;
		return null;
	}
	
	public void setCoords(String X, String Y, String Z)
	{
		String oldX = coordX;
		String oldY = coordY;
		String oldZ = coordZ;
		
		for (int i = 0; i < 3; i++)
		{
			String coord = "";
			if (i == 0) coord = X;
			else if (i == 1) coord = Y;
			else if (i == 2) coord = Z;
			
			String oldCoord = "";
			if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("X")) oldCoord = oldX;
			else if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("Y")) oldCoord = oldY;
			else if (coord.substring(coord.length()-1, coord.length()).equalsIgnoreCase("Z")) oldCoord = oldZ;
			
			String sign = coord.startsWith("-") == oldCoord.startsWith("-") ? "" : "-";
			String dir = oldCoord.substring(oldCoord.length()-1, oldCoord.length());
			String newCoord = sign+dir;
			
			if (i == 0) coordX = newCoord;
			else if (i == 1) coordY = newCoord;
			else if (i == 2) coordZ = newCoord;
		}
	}
		
	public void setCoords(String coords)
	{
		int pos = 0;

		String nX = "";
		String nY = "";
		String nZ = "";
		String coord = "";
		
		for (int i = 0; i < coords.length(); i++)
		{
			String c = ""+coords.charAt(i);
			coord += c;
			if (c.equalsIgnoreCase("X") || c.equalsIgnoreCase("Y") || c.equalsIgnoreCase("Z"))
			{
								
				if (pos == 0)
				{
					nX = coord;
				}
				else if (pos == 1)
				{
					nY = coord;
				}
				else if (pos == 2)
				{
					nZ = coord;
				}
				
				pos++;
				coord = "";
			}
		}
		setCoords(nX, nY, nZ);
	}
		
	public float getVal(String axis, Vector3 vals)
	{	
		if (axis.length() > 2 || axis.length() == 0) throw new RuntimeException("Invalid axis: "+axis);
		if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("X"))
		{
			return vals.x;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Y"))
		{
			return vals.y;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Z"))
		{
			return vals.z;
		}
		else throw new RuntimeException("Invalid axis: "+axis);
	}
	
	public void modVal(String axis, Vector3 vals, float val)
	{		
		if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("X"))
		{
			vals.x += val;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Y"))
		{
			vals.y += val;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Z"))
		{
			vals.z += val;
		}
		else throw new RuntimeException("Invalid axis: "+axis);
	}
	
	public void setVal(String axis, Vector3 vals, float val)
	{		
		if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("X"))
		{
			vals.x = val;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Y"))
		{
			vals.y = val;
		}
		else if (axis.substring(axis.length()-1, axis.length()).equalsIgnoreCase("Z"))
		{
			vals.z = val;
		}
		else throw new RuntimeException("Invalid axis: "+axis);
	}
		
	public void repeat(String eqn, int repeats, float offset, JsonValue ruleOffset, JsonValue ruleSub, JsonValue ruleRemainder, JsonValue repeatRule, String axis)
	{
		float interval = getVal(axis, max) - getVal(axis, min);
		boolean up = !axis.startsWith("-");
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
		if (up) setVal(axis, nmax, getVal(axis, nmin)+offset);
		else setVal(axis, nmin, getVal(axis, nmax)-offset);
		
		if (ruleOffset != null)
		{
			children.add(new VolumePartitioner(nmin, nmax, ruleOffset, methodTable, this));
		}
		
		if (up) setVal(axis, nmin, getVal(axis, nmax));
		else setVal(axis, nmax, getVal(axis, nmin));
		
		int rep = 0;
		while (true)
		{
			if (up) setVal(axis, nmax, getVal(axis, nmin) + parseEquation(eqn, interval, variables));
			else setVal(axis, nmin, getVal(axis, nmax) - parseEquation(eqn, interval, variables));
			
			if (rep == repeats) break;
			else if (repeats < 0)
			{
				if (up) 
				{ 
					if (getVal(axis, nmax) > getVal(axis, max)) break; 
				}
				else 
				{
					if (getVal(axis, nmin) < getVal(axis, min)) break;
				}
			}
			
			children.add(new VolumePartitioner(nmin, nmax, ruleSub, methodTable, this));
			rep++;

			if (up) setVal(axis, nmin, getVal(axis, nmax));
			else setVal(axis, nmax, getVal(axis, nmin));
			
			if (repeatRule != null) processRule(repeatRule);
		}

		if (ruleRemainder != null)
		{
			if (up) 
			{
				if (getVal(axis, nmin) < getVal(axis, max))
				{
					setVal(axis, nmax, getVal(axis, max));
					children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this));
				}
			}
			else
			{
				if (getVal(axis, nmax) > getVal(axis, min))
				{
					setVal(axis, nmin, getVal(axis, min));
					children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this));
				}
			}
			
		}

		Pools.free(nmin);
		Pools.free(nmax);
	}

	public void processRepeat(JsonValue repeat)
	{
		String axis = repeat.getString("Axis");
		String eqn = repeat.getString("Size");
		int repeats = repeat.has("Repeats") ? repeat.getInt("Repeats") : -1 ;
		float offset = repeat.has("Offset") ? parseEquation(repeat.getString("Offset"), getVal(axis, max)-getVal(axis, min), variables) : 0 ;
		
		JsonValue ruleSub = repeat.get("Rule").child;
		if (ruleSub == null)
		{
			String ruleString = repeat.getString("Rule");
			ruleSub = methodTable.get(ruleString);
		}
		else
		{
			JsonValue temp = new JsonValue("TempRule");
			temp.name = "TempRule";
			temp.child = ruleSub;
			ruleSub = temp;
		}
		
		JsonValue ruleOffset = null;
		if (repeat.has("OffsetRule"))
		{
			ruleOffset = repeat.get("OffsetRule").child;
			if (ruleOffset == null)
			{
				String ruleString = repeat.getString("OffsetRule");
				ruleOffset = methodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleOffset;
				ruleOffset = temp;
			}
		}		
		
		JsonValue ruleRemainder = null;
		if (repeat.has("RemainderRule"))
		{
			ruleRemainder = repeat.get("RemainderRule").child;
			if (ruleRemainder == null)
			{
				String ruleString = repeat.getString("RemainderRule");
				ruleRemainder = methodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleRemainder;
				ruleRemainder = temp;
			}
		}
		
		JsonValue ruleRepeat = null;
		if (repeat.has("RepeatRule"))
		{
			ruleRepeat = repeat.get("RepeatRule").child;
			if (ruleRepeat == null)
			{
				String ruleString = repeat.getString("RepeatRule");
				ruleRepeat = methodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleRepeat;
				ruleRepeat = temp;
			}
		}
		
		axis = getCoord(axis);
		if (axis != null) repeat(eqn, repeats, offset, ruleOffset, ruleSub, ruleRemainder, ruleRepeat, axis);
		else throw new RuntimeException("Invalid Axis: "+axis);
	}
	
	public void processDivide(JsonValue divide)
	{
		String axis = getCoord(divide.getString("Axis"));
		
		float interval = getVal(axis, max) - getVal(axis, min);
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
		String[] sizes = divide.get("Sizes").asStringArray();
		String[] rules = divide.get("Rules").asStringArray();
		
		for (int i = 0; i < sizes.length; i++)
		{
			String eqn = sizes[i];
			float size = parseEquation(eqn, interval, variables);
			
			setVal(axis, nmax, getVal(axis, nmin) + size);
			
			JsonValue rule = methodTable.get(rules[i]);
			children.add(new VolumePartitioner(nmin, nmax, rule, methodTable, this));
			
			setVal(axis, nmin, getVal(axis, nmax));
		}
	}

	public void processSelect(JsonValue select)
	{					
		Vector3 nmin = Pools.obtain(Vector3.class);
		Vector3 nmax = Pools.obtain(Vector3.class);
		
		String[] coords = select.get("Coords").asStringArray();
		String[] sides = select.get("Sides").asStringArray();
		String[] sizes = select.get("Sizes").asStringArray();
		String[] rules = select.get("Rules").asStringArray();
		
		for (int i = 0; i < sides.length; i++)
		{
			String side = sides[i];
			String axis = "";
			if (side.equalsIgnoreCase("left") || side.equalsIgnoreCase("right")) axis = coordX;
			else if (side.equalsIgnoreCase("top") || side.equalsIgnoreCase("bottom")) axis = coordY;
			else if (side.equalsIgnoreCase("front") || side.equalsIgnoreCase("back")) axis = coordZ;
			else throw new RuntimeException("Invalid side: "+side);
			
			float interval = getVal(axis, max) - getVal(axis, min);
			String eqn = sizes[i];
			float size = parseEquation(eqn, interval, variables);

			nmin.set(min);
			nmax.set(max);
			
			if (side.equalsIgnoreCase("left"))
			{
				nmax.x = nmin.x + size;
			}
			else if (side.equalsIgnoreCase("right"))
			{
				nmin.x = nmax.x - size;
			}
			else if (side.equalsIgnoreCase("bottom"))
			{
				nmax.y = nmin.y + size;
			}
			else if (side.equalsIgnoreCase("top"))
			{
				nmin.y = nmax.y - size;
			}
			else if (side.equalsIgnoreCase("front"))
			{
				nmin.z = nmax.z - size;
			}
			else if (side.equalsIgnoreCase("back"))
			{
				nmax.z = nmin.z + size;
			}
			
			JsonValue rule = methodTable.get(rules[i]);
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this);
			vp.setCoords(coords[i]);
			children.add(vp);
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
	}
	
	public void processRule(JsonValue rule)
	{
		JsonValue current = rule.child;
	
		while (current != null)
		{
			String method = current.name;
			
			if (method.equalsIgnoreCase("Rule"))
			{
				JsonValue nrule = methodTable.get(current.asString());
				if (nrule == null) throw new RuntimeException("Invalid rule: "+current.asString());
				processRule(nrule);
			}
			else if (method.equalsIgnoreCase("CoordinateSystem"))
			{
				setCoords(current.asString());
			}
			else if (method.equalsIgnoreCase("Select"))
			{
				processSelect(current);
			}
			else if (method.equalsIgnoreCase("Repeat"))
			{
				processRepeat(current);
			}
			else if (method.equalsIgnoreCase("Divide"))
			{
				processDivide(current);
			}
			else if (method.equalsIgnoreCase("Snap"))
			{
				snap.set(current.getFloat("X"), current.getFloat("Y"), current.getFloat("Z"));
			}
			else if (method.equalsIgnoreCase("X") || method.equalsIgnoreCase("Y") || method.equalsIgnoreCase("Z"))
			{
				String axis = getCoord(method);
				String eqnString = current.asString();
				float interval = getVal(axis, max) - getVal(axis, min) ;
				
				float val = parseEquation(eqnString, interval, variables) ;
				val /= 2.0f;
				
				int snapVal = MathUtils.round(getVal(axis, snap));
				if (snapVal == 1)
				{
					setVal(axis, min, getVal(axis, max)-(val*2));
				}
				else if (snapVal == 0)
				{
					setVal(axis, min, interval / 2.0f + getVal(axis, min));
					setVal(axis, max, getVal(axis, min));

					modVal(axis, min, -val);
					modVal(axis, max, val);
				}
				else if (snapVal == -1)
				{
					setVal(axis, max, getVal(axis, min)+(val*2));
				}
				else throw new RuntimeException("Invalid snap val: "+snap+" for axis: "+axis);
				
			}
			else if (method.equalsIgnoreCase("Define"))
			{
				JsonValue definition = current.child;
				while (definition != null)
				{
					String name = definition.name;
					String value = definition.asString();
					defines.put(name, value);
					
					try
					{
						double val = parseEquationWithException(value, 0, variables);
						variables.put(name, val);
					}
					catch (Exception e) {}
					
					definition = definition.next;
				}
			}
			else if (method.equalsIgnoreCase("Mesh"))
			{
				Mesh = current;
			}
			else
			{
				throw new RuntimeException("Unrecognised Rule: "+method);
			}

			current = current.next;
		}
	}

	public ModelBatchData getModelBatchData()
	{
		JsonValue meshValue = Mesh;
		String meshName = meshValue.getString("Name");
		if (defines.containsKey(meshName)) meshName = defines.get(meshName);
		String textureName = meshValue.getString("Texture");
		boolean useTriplanarSampling = meshValue.getBoolean("TriplanarSample");
		float triplanarScale = 0;
		if (useTriplanarSampling) triplanarScale = meshValue.getFloat("TriplanarScale");
		
		if (defines.containsKey(textureName)) textureName = defines.get(textureName);
		String mbname = "";
		
		if (meshName.equalsIgnoreCase("Sphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta")+"Phi"+meshValue.getString("Phi")+useTriplanarSampling+triplanarScale;
		}
		else if (meshName.equalsIgnoreCase("HemiSphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta")+"Phi"+meshValue.getString("Phi")+useTriplanarSampling+triplanarScale;
		}
		else if (meshName.equalsIgnoreCase("Prism"))
		{
			String eqn = meshValue.getString("loft");
			mbname = meshName+textureName+"Loft"+eqn+useTriplanarSampling+triplanarScale;
		}
		else
		{
			mbname = meshName+textureName+useTriplanarSampling+triplanarScale;
		}
		
		ModelBatchData data = FileUtils.loadModelBatchData(mbname);
		if (data == null)
		{
			Mesh mesh = null;
			if (meshName.equalsIgnoreCase("Box"))
			{
				mesh = Shapes.getBoxMesh(1, 1, 1, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("Sphere"))
			{
				int theta = meshValue.getInt("Theta");
				int phi = meshValue.getInt("Phi");
				mesh = Shapes.getSphereMesh(theta, phi, 1, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("HemiSphere"))
			{
				int theta = meshValue.getInt("Theta");
				int phi = meshValue.getInt("Phi");
				mesh = Shapes.getHemiSphereMesh(theta, phi, 1, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("Prism"))
			{
				String eqn = meshValue.getString("loft");
				float loft = parseEquation(eqn, 1, variables);
				mesh = Shapes.getPrismMesh(1, loft, 1, 1, true, !useTriplanarSampling);
			}
			else
			{
				mesh = FileUtils.loadMesh(meshName);
			}

			data = new ModelBatchData(mesh, GL20.GL_TRIANGLES, FileUtils.getTextureArray(new String[]{textureName}), false, false, useTriplanarSampling, triplanarScale);
			FileUtils.storeModelBatchData(mbname, data);
		}
		return data;
	}

	private void collectMeshes(Entity entity, Vector3 center, OcttreeEntry<Entity> entry, btTriangleMesh triangleMesh)
	{
		if (Mesh != null)
		{
			ModelBatchData data = getModelBatchData();
			ModelBatchInstance mb = new ModelBatchInstance(data);
			BoundingBox bb = Pools.obtain(BoundingBox.class);
			mb.getMesh().calculateBoundingBox(bb);
			
			Matrix4 rotation = getLocalAxis(Pools.obtain(Matrix4.class));
			bb.mul(rotation);
			
			Vector3 meshDim = bb.getDimensions();
			meshDim.set(1.0f / meshDim.x, 1.0f / meshDim.y, 1.0f / meshDim.z);
			Vector3 volumeDim = Pools.obtain(Vector3.class).set(max).sub(min);
						
			Vector3 translation = Pools.obtain(Vector3.class).set(volumeDim).scl(0.5f).add(min).sub(center);
			Vector3 scale = volumeDim.scl(meshDim);

			Matrix4 transform = Pools.obtain(Matrix4.class).setToTranslationAndScaling(translation, scale).mul(rotation);

			entity.addRenderable(mb, transform);

			Pools.free(bb);
			Pools.free(volumeDim);
			Pools.free(transform);
			Pools.free(translation);
			Pools.free(rotation);
			
			if (entry != null)
			{
				if (entry.box.pos.x-entry.box.extents.x > min.x) entry.box.extents.x = entry.box.pos.x-min.x;
				if (entry.box.pos.y-entry.box.extents.y > min.y) entry.box.extents.y = entry.box.pos.y-min.y;
				if (entry.box.pos.z-entry.box.extents.z > min.z) entry.box.extents.z = entry.box.pos.z-min.z;
				
				if (entry.box.pos.x+entry.box.extents.x < max.x) entry.box.extents.x = max.x-entry.box.pos.x;
				if (entry.box.pos.y+entry.box.extents.y < max.y) entry.box.extents.y = max.y-entry.box.pos.y;
				if (entry.box.pos.z+entry.box.extents.z < max.z) entry.box.extents.z = max.z-entry.box.pos.z;	
			}
			if (triangleMesh != null)
			{
				BulletWorld.addTriangles(mb.getMesh(), transform, triangleMesh);
			}
		}
		else for (VolumePartitioner vp : children)
		{
			vp.collectMeshes(entity, center, entry, triangleMesh);
		}
	}

	public void collectMeshes(Entity entity, OcttreeEntry<Entity> entry, btTriangleMesh triangleMesh)
	{
		Vector3 center = Pools.obtain(Vector3.class);
		center.set(min).add(max).scl(0.5f);
		collectMeshes(entity, center, entry, triangleMesh);
		Pools.free(center);
	}

	private static void loadImportsAndBuildMethodTable(Array<String> importedFiles, JsonValue root, HashMap<String, JsonValue> methodTable)
	{
		JsonValue imports = root.get("Imports");
		if (imports != null)
		{
			String[] files = imports.asStringArray();
			for (String file : files)
			{
				if (!importedFiles.contains(file, false))
				{
					importedFiles.add(file);
					
					String contents = Gdx.files.internal(file).readString();
					JsonValue nroot = new JsonReader().parse(contents);
					
					loadImportsAndBuildMethodTable(importedFiles, nroot, methodTable);
				}
			}
		}
		
		JsonValue current = root.child;
		while(current != null)
		{
			if (current.name.equalsIgnoreCase("Main") || current.name.equalsIgnoreCase("Imports"))
			{
				
			}
			else methodTable.put(current.name, current);
			current = current.next;
		}
	}
	
	public static VolumePartitioner load(String file)
	{
		String contents = Gdx.files.internal(file).readString();
		JsonValue root = new JsonReader().parse(contents);
		JsonValue main = root.get("Main");
		HashMap<String, JsonValue> methodTable = new HashMap<String, JsonValue>();
		
		loadImportsAndBuildMethodTable(new Array<String>(false, 16), root, methodTable);

		return new VolumePartitioner(new Vector3(), new Vector3(), main, methodTable, null);
	}

	private float parseEquationWithException(String equation, float interval, HashMap<String, Double> variables) throws UnknownFunctionException, UnparsableExpressionException
	{
		equation = equation.replace("%", "#");
		
		float size = 0;
		
		if (percentOperator == null)
		{
			percentOperator = new PercentOperator();
		}
		percentOperator.interval = interval;

		if (rndFunc == null)
		{
			try
			{
				rndFunc = new CustomFunction("rnd") {
					public double applyFunction(double... value) 
					{
						double val = 0;
						for (double v : value) val += MathUtils.random()*v;
						return val;
					}

				};
			}
			catch (InvalidCustomFunctionException e)
			{
				e.printStackTrace();
			}
		}

		ExpressionBuilder expBuilder = new ExpressionBuilder(equation);
		expBuilder.withCustomFunction(rndFunc);
		expBuilder.withOperation(percentOperator);
		expBuilder.withVariables(variables);
		expBuilder.withVariable("X", getVal(coordX, max)-getVal(coordX, min));
		expBuilder.withVariable("Y", getVal(coordY, max)-getVal(coordY, min));
		expBuilder.withVariable("Z", getVal(coordZ, max)-getVal(coordZ, min));
		
		Calculable eqn = expBuilder.build();
		size = (float) eqn.calculate();

		return size;
	}
	
	private float parseEquation(String equation, float interval, HashMap<String, Double> variables)
	{
		float size = 0;

		try
		{
			size = parseEquationWithException(equation, interval, variables);
		}
		catch (UnknownFunctionException e)
		{
			e.printStackTrace();
		}
		catch (UnparsableExpressionException e)
		{
			e.printStackTrace();
		}

		return size;
	}

	private static CustomFunction rndFunc;
	private static PercentOperator percentOperator;
	
	private static class PercentOperator extends CustomOperator
	{
		float interval;
		protected PercentOperator()
		{
			super("#", true, 1, 1);
		}

		@Override
		protected double applyOperation(double[] arg0)
		{
			return ( arg0[0] / 100.0 ) * interval;
		}
		
	}
}
