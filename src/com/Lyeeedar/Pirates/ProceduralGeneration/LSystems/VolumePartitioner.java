package com.Lyeeedar.Pirates.ProceduralGeneration.LSystems;

import java.util.HashMap;
import java.util.Map;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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

	public final Vector3 min = new Vector3();
	public final Vector3 max = new Vector3();

	public final Array<VolumePartitioner> children = new Array<VolumePartitioner>(false, 16);
	
	public final HashMap<String, Double> variables = new HashMap<String, Double>();
	
	public String coordX = "X";
	public String coordY = "Y";
	public String coordZ = "Z";

	public VolumePartitioner(Vector3 min, Vector3 max, JsonValue rule, HashMap<String, JsonValue> methodTable, VolumePartitioner parent)
	{
		this.min.set(min);
		this.max.set(max);
				
		this.rule = rule;
		this.methodTable = methodTable;
		this.parent = parent;
		
		if (parent != null)
		{
			for (Map.Entry<String, Double> entry : parent.variables.entrySet())
			{
				variables.put(entry.getKey(), entry.getValue());
			}
			this.coordX = parent.coordX;
			this.coordY = parent.coordY;
			this.coordZ = parent.coordZ;
		}
	}

	public void evaluate()
	{
		processRule(rule);
		for (VolumePartitioner vp : children) vp.evaluate();
	}

	public String getCoord(String coord)
	{
		if (coord.equalsIgnoreCase("X")) return coordX;
		if (coord.equalsIgnoreCase("Y")) return coordY;
		if (coord.equalsIgnoreCase("Z")) return coordZ;
		return null;
	}
	
	public void setCoords(String coords)
	{
		int pos = 0;
		String coord = "";
		for (int i = 0; i < coords.length(); i++)
		{
			String c = ""+coords.charAt(i);
			coord += c;
			if (c.equalsIgnoreCase("X") || c.equalsIgnoreCase("Y") || c.equalsIgnoreCase("Z"))
			{
				if (pos == 0)
				{
					System.out.println("Setting X to "+coord);
					coordX = coord;
				}
				else if (pos == 1)
				{
					System.out.println("Setting Y to "+coord);
					coordY = coord;
				}
				else if (pos == 2)
				{
					System.out.println("Setting Z to "+coord);
					coordZ = coord;
				}
				
				pos++;
				coord = "";
			}
		}
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
		
	public void repeat(String eqn, int repeats, JsonValue ruleSub, JsonValue ruleRemainder, String axis)
	{
		float interval = getVal(axis, max) - getVal(axis, min);
		boolean up = !axis.startsWith("-");
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
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
		int repeats = repeat.getInt("Repeats");
		
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
		
		JsonValue ruleRemainder = repeat.get("RemainderRule").child;
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
		
		axis = getCoord(axis);
		if (axis != null) repeat(eqn, repeats, ruleSub, ruleRemainder, axis);
		else throw new RuntimeException("Invalid Axis: "+axis);
	}
	
	public void processDivide(JsonValue divide)
	{
		String axis = divide.getString("Axis");
		
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
		JsonValue rule = select.get("Rule").child;
		if (rule == null)
		{
			String ruleString = select.getString("Rule");
			rule = methodTable.get(ruleString);
		}
		else
		{
			JsonValue temp = new JsonValue("TempRule");
			temp.name = "TempRule";
			temp.child = rule;
			rule = temp;
		}
			
		Vector3 nmin = Pools.obtain(Vector3.class);
		Vector3 nmax = Pools.obtain(Vector3.class);
		
		String[] coords = select.get("Coords").asStringArray();
		String[] sides = select.get("Sides").asStringArray();
		
		for (int i = 0; i < sides.length; i++)
		{
			String side = sides[i];
			String axis = "";
			if (side.equalsIgnoreCase("left") || side.equalsIgnoreCase("right")) axis = coordX;
			else if (side.equalsIgnoreCase("top") || side.equalsIgnoreCase("bottom")) axis = coordY;
			else if (side.equalsIgnoreCase("front") || side.equalsIgnoreCase("back")) axis = coordZ;
			else throw new RuntimeException("Invalid side: "+side);
			
			float interval = getVal(axis, max) - getVal(axis, min);
			String eqn = select.getString("Size");
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
				nmax.z = nmin.z + size;
			}
			else if (side.equalsIgnoreCase("back"))
			{
				nmin.z = nmax.z - size;
			}
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this);
			vp.setCoords(coords[i]);
			children.add(vp);
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
	}
	
	public void processRule(JsonValue rule)
	{
		System.out.println(rule.name);
		JsonValue current = rule.child;
	
		while (current != null)
		{
			String method = current.name;
			
			System.out.println(method);

			if (method.equalsIgnoreCase("Rule"))
			{
				JsonValue nrule = methodTable.get(current.asString());
				processRule(nrule);
			}
			else if (method.equalsIgnoreCase("CoordinateSystem"))
			{
				coordX = current.getString("X");
				coordY = current.getString("Y");
				coordZ = current.getString("Z");
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
			else if (method.equalsIgnoreCase("X") || method.equalsIgnoreCase("Y") || method.equalsIgnoreCase("Z"))
			{
				String axis = getCoord(method);
				String eqnString = current.asString();
				float interval = getVal(axis, max) - getVal(axis, min) ;
				
				float val = parseEquation(eqnString, interval, variables) ;
				val /= 2.0f;
				
				setVal(axis, min, interval / 2.0f + getVal(axis, min));
				setVal(axis, max, getVal(axis, min));

				modVal(axis, min, -val);
				modVal(axis, max, val);
			}
			else if (method.equalsIgnoreCase("Define"))
			{
				JsonValue definition = current.child;
				while (definition != null)
				{
					String name = definition.name;
					String eqn = definition.asString();
					double val = parseEquation(eqn, 0, variables);
					
					variables.put(name, val);
					
					definition = definition.next;
				}
			}
			else if (method.equalsIgnoreCase("Mesh"))
			{

			}
			else
			{
				throw new RuntimeException("Unrecognised Rule: "+method);
			}

			current = current.next;
		}
	}

	public ModelBatcher getModelBatcher()
	{
		String meshName = rule.get("Mesh").getString("Name");
		String textureName = rule.get("Mesh").getString("Texture");
		String mbname = meshName+textureName;
		ModelBatcher mb = FileUtils.loadModelBatcher(mbname);
		if (mb == null)
		{
			Mesh mesh = null;
			if (meshName.equalsIgnoreCase("Box"))
			{
				mesh = Shapes.getBoxMesh(1, 1, 1, true, true);
			}
			else
			{
				mesh = FileUtils.loadMesh(meshName);
			}

			mb = new ModelBatcher(mesh, GL20.GL_TRIANGLES, FileUtils.getTextureArray(new String[]{textureName}), false, false);
			FileUtils.storeModelBatcher(mbname, mb);
		}
		return mb;
	}

	private void collectMeshes(Entity entity, Vector3 center)
	{
		if (children.size == 0 && rule.get("Mesh") != null)
		{
			ModelBatcher mb = getModelBatcher();
			BoundingBox bb = Pools.obtain(BoundingBox.class);
			mb.getMesh().calculateBoundingBox(bb);

			Vector3 meshDim = bb.getDimensions();
			meshDim.set(1.0f / meshDim.x, 1.0f / meshDim.y, 1.0f / meshDim.z);
			Vector3 volumeDim = Pools.obtain(Vector3.class).set(max).sub(min);
						
			Vector3 translation = Pools.obtain(Vector3.class).set(volumeDim).scl(0.5f).add(min).sub(center);
			Vector3 scale = volumeDim.scl(meshDim);

			Matrix4 transform = Pools.obtain(Matrix4.class).setToTranslationAndScaling(translation, scale);

			entity.addRenderable(mb, transform);

			Pools.free(bb);
			Pools.free(volumeDim);
			Pools.free(transform);
			Pools.free(translation);
		}
		else for (VolumePartitioner vp : children)
		{
			vp.collectMeshes(entity, center);
		}
	}

	public void collectMeshes(Entity entity)
	{
		Vector3 center = Pools.obtain(Vector3.class);
		center.set(min).add(max).scl(0.5f);
		collectMeshes(entity, center);
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
					System.out.println("Appending: "+contents);
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

	private float parseEquation(String equation, float interval, HashMap<String, Double> variables)
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

		try
		{
			ExpressionBuilder expBuilder = new ExpressionBuilder(equation);
			expBuilder.withCustomFunction(rndFunc);
			expBuilder.withOperation(percentOperator);
			expBuilder.withVariables(variables);
			
			Calculable eqn = expBuilder.build();
			size = (float) eqn.calculate();
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
