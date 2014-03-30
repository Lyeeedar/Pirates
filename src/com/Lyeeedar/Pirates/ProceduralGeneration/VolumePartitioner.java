package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.ArrayDeque;
import java.util.Comparator;
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btTriangleMesh;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
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
	public final ArrayMap<String, JsonValue> tempMethodTable = new ArrayMap<String, JsonValue>();
	public final Array<OcclusionArea> occluders;

	public final ArrayDeque<JsonValue> ruleStack = new ArrayDeque<JsonValue>();
	
	public JsonValue Mesh;

	public final Vector3 min = new Vector3();
	public final Vector3 max = new Vector3();

	public final Array<VolumePartitioner> children = new Array<VolumePartitioner>(false, 16);
	
	public final HashMap<String, String> defines = new HashMap<String, String>();
	public final HashMap<String, Double> variables = new HashMap<String, Double>();
	
	public Matrix4 parentTransform = new Matrix4();
	public Vector3 parentPos = new Vector3();
	public Matrix4 localTransform = new Matrix4();
	public Matrix4 composedTransform = new Matrix4();
	public Matrix4 invComposedTransform = new Matrix4();
	
	public Vector3 snap = new Vector3();

	public VolumePartitioner(Vector3 min, Vector3 max, JsonValue rule, HashMap<String, JsonValue> methodTable, VolumePartitioner parent, Array<OcclusionArea> occluders)
	{
		this.min.set(min);
		this.max.set(max);
		
		this.occluders = occluders;
		
		if (parent != null)
		{
			this.parentTransform.set(parent.composedTransform);
			
			parentPos.set(parent.min).add(parent.max).scl(0.5f);
			Vector3 mc = Pools.obtain(Vector3.class).set(min).add(max).scl(0.5f);
			
			Vector3 diff = mc.sub(parentPos);
			
			this.localTransform.setToTranslation(diff);
			
			Pools.free(mc);
			
			for (ObjectMap.Entry<String, JsonValue> entry : parent.tempMethodTable.entries())
			{
				tempMethodTable.put(entry.key, entry.value);
			}
		}
		
		composedTransform.set(parentTransform).mul(localTransform);
		invComposedTransform.set(composedTransform).inv();
				
		if (rule.child != null) this.ruleStack.addFirst(rule.child);
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

			this.snap.set(parent.snap);
		}
	}
	
	public void transform(Matrix4 transform)
	{
		this.localTransform.mul(transform);
		composedTransform.set(parentTransform).mul(localTransform);
		invComposedTransform.set(composedTransform).inv();
	}
	
	public void transformVolume(Matrix4 transform)
	{
		this.localTransform.mul(transform);
		composedTransform.set(parentTransform).mul(localTransform);
		invComposedTransform.set(composedTransform).inv();
				
		Vector3 center = Pools.obtain(Vector3.class);
		
		center.set(min).add(max).scl(0.5f);
		
		min.sub(center);
		max.sub(center);
		
		min.mul(transform);
		max.mul(transform);
		
		if (max.x < min.x) 
		{
			float temp = min.x;
			min.x = max.x;
			max.x = temp;
		}
		
		if (max.y < min.y) 
		{
			float temp = min.y;
			min.y = max.y;
			max.y = temp;
		}
		
		if (max.z < min.z) 
		{
			float temp = min.z;
			min.z = max.z;
			max.z = temp;
		}
		
		min.add(center);
		max.add(center);
		
		Pools.free(center);
	}
	
	public void applyCoords(String coords)
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

	public void setCoords(String X, String Y, String Z)
	{
		Matrix4 rotation = Pools.obtain(Matrix4.class).idt();
		
		float x = X.length() > 1 ? parseEquation(X.substring(0, X.length()-1), 0, variables) : 0;
		float y = Y.length() > 1 ? parseEquation(Y.substring(0, Y.length()-1), 0, variables) : 0;
		float z = Z.length() > 1 ? parseEquation(Z.substring(0, Z.length()-1), 0, variables) : 0;
		
		if (x != 0) rotation.rotate(1, 0, 0, x);
		if (y != 0) rotation.rotate(0, 1, 0, y);
		if (z != 0) rotation.rotate(0, 0, 1, z);
		
		transformVolume(rotation);
		
		Pools.free(rotation);
	}
	
	public void evaluate(float x, float y, float z)
	{
		processResize("X", x);
		processResize("Y", y);
		processResize("Z", z);
		evaluateInternal(this);
	}
	
	private static void evaluateInternal(VolumePartitioner root)
	{
		ArrayDeque<VolumePartitioner> processQueue = new ArrayDeque<VolumePartitioner>();
		ArrayDeque<VolumePartitioner> deferQueue = new ArrayDeque<VolumePartitioner>();
		
		processQueue.add(root);
		
		while (processQueue.size() != 0)
		{
			VolumePartitioner current = processQueue.poll();
			while (current != null)
			{
				if (!current.processRuleStack())
				{
					deferQueue.add(current);
				}
				
				for (VolumePartitioner child : current.children) processQueue.add(child); // Possible performance issue
				
				current = processQueue.poll();
			}
			
			ArrayDeque<VolumePartitioner> temp = processQueue;
			processQueue = deferQueue;
			deferQueue = temp;
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
		
	public void repeat(String eqn, int repeats, float offset, JsonValue ruleOffset, String offsetCoord, JsonValue ruleSub, String ruleCoord, JsonValue ruleRemainder, String remainderCoord, JsonValue repeatRule, String axis)
	{
		float interval = getVal(axis, max) - getVal(axis, min);
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
		setVal(axis, nmax, getVal(axis, nmin)+offset);
		
		if (ruleOffset != null)
		{
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleOffset, methodTable, this, occluders);
			children.add(vp);
			vp.applyCoords(offsetCoord);
		}
		
		setVal(axis, nmin, getVal(axis, nmax));
		
		int rep = 0;
		while (true)
		{
			setVal(axis, nmax, getVal(axis, nmin) + parseEquation(eqn, interval, variables));
			
			if (rep == repeats) break;
			else if (repeats < 0)
			{
				if (getVal(axis, nmax) > getVal(axis, max)) break; 
			}
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleSub, methodTable, this, occluders);
			children.add(vp);
			vp.applyCoords(ruleCoord);
			
			rep++;

			setVal(axis, nmin, getVal(axis, nmax));
			
			if (repeatRule != null) processRuleBlock(repeatRule);
		}

		if (ruleRemainder != null)
		{
			if (getVal(axis, nmin) < getVal(axis, max))
			{
				setVal(axis, nmax, getVal(axis, max));
				
				VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders);
				children.add(vp);
				vp.applyCoords(remainderCoord);
			}
			
		}

		Pools.free(nmin);
		Pools.free(nmax);
	}

	public void processRepeat(JsonValue repeat, String axis)
	{
		axis = axis == null ? repeat.getString("Axis") : axis ;
		String eqn = repeat.getString("Size");
		int repeats = repeat.getInt("Repeats", -1);
		float offset = repeat.has("Offset") ? parseEquation(repeat.getString("Offset"), getVal(axis, max)-getVal(axis, min), variables) : 0 ;
		
		JsonValue ruleSub = repeat.get("Rule").child;
		if (ruleSub == null)
		{
			String ruleString = repeat.getString("Rule");
			ruleSub = methodTable.get(ruleString);
			if (ruleSub == null) ruleSub = tempMethodTable.get(ruleString);
		}
		else
		{
			JsonValue temp = new JsonValue("TempRule");
			temp.name = "TempRule";
			temp.child = ruleSub;
			ruleSub = temp;
		}
		
		String ruleCoord = "xyz";
		if (repeat.has("RuleCoord"))
		{
			ruleCoord = repeat.getString("RuleCoord");
		}
		
		JsonValue ruleOffset = null;
		String offsetCoord = "xyz";
		if (repeat.has("OffsetRule"))
		{
			ruleOffset = repeat.get("OffsetRule").child;
			if (ruleOffset == null)
			{
				String ruleString = repeat.getString("OffsetRule");
				ruleOffset = methodTable.get(ruleString);
				if (ruleOffset == null) ruleOffset = tempMethodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleOffset;
				ruleOffset = temp;
			}
			
			if (repeat.has("OffsetCoord"))
			{
				offsetCoord = repeat.getString("OffsetCoord");
			}
		}		
		
		JsonValue ruleRemainder = null;
		String remainderCoord = "xyz";
		if (repeat.has("RemainderRule"))
		{
			ruleRemainder = repeat.get("RemainderRule").child;
			if (ruleRemainder == null)
			{
				String ruleString = repeat.getString("RemainderRule");
				ruleRemainder = methodTable.get(ruleString);
				if (ruleRemainder == null) ruleRemainder = tempMethodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleRemainder;
				ruleRemainder = temp;
			}
			
			if (repeat.has("RemainderCoord"))
			{
				remainderCoord = repeat.getString("RemainderCoord");
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
				if (ruleRepeat == null) ruleRepeat = tempMethodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = ruleRepeat;
				ruleRepeat = temp;
			}
		}
		
		if (axis != null) repeat(eqn, repeats, offset, ruleOffset, offsetCoord, ruleSub, ruleCoord, ruleRemainder, remainderCoord, ruleRepeat, axis);
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
		String[] coords = divide.has("Coords") ? divide.get("Coords").asStringArray() : null;
		
		for (int i = 0; i < sizes.length; i++)
		{
			String eqn = sizes[i];
			float size = parseEquation(eqn, interval, variables);
			
			setVal(axis, nmax, getVal(axis, nmin) + size);
			
			JsonValue rule = methodTable.get(rules[i]);
			if (rule == null) rule = tempMethodTable.get(rules[i]);
			if (rule == null) rule = tempMethodTable.get(rules[i]);
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this, occluders);
			if (coords != null) vp.applyCoords(coords[i]);
			children.add(vp);
			
			setVal(axis, nmin, getVal(axis, nmax));
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
	}
	
	private static final char[][] csvDelimiters = {
		{'(', ')'}
	};
	public static String[] parseCSV(String csv)
	{
		Array<String> store = new Array<String>(false, 16);
		StringBuilder builder = new StringBuilder();
		int delimiter = -1;
		
		for (int i = 0; i < csv.length(); i++)
		{
			char c = csv.charAt(i);
			if (delimiter == -1)
			{
				if (c == ',')
				{
					store.add(builder.toString());
					builder.delete(0, builder.length());
				}
				else
				{
					builder.append(c);
					
					for (int d = 0; d < csvDelimiters.length; d++)
					{
						if (csvDelimiters[d][0] == c)
						{
							delimiter = d;
							break;
						}
					}
				}
			}
			else
			{
				builder.append(c);
				
				if (csvDelimiters[delimiter][1] == c)
				{
					delimiter = -1;
				}
			}
		}
		
		if (builder.length() > 0)
		{
			store.add(builder.toString());
		}
		
		String[] values = new String[store.size];
		for (int i = 0; i < store.size; i++) values[i] = store.get(i);
		
		for (int i = 0; i < values.length; i++)
		{
			values[i] = values[i].trim();
		}
		return values;
	}
	
	public void processSplit(JsonValue split)
	{
		String[] csvs = split.asStringArray();
		String[][] splits = new String[csvs.length][];
		
		JsonValue ruleRemainder = null;
		
		for (int i = 0; i < csvs.length; i++)
		{
			splits[i] = parseCSV(csvs[i]);
			
			if (splits[i][0].equalsIgnoreCase("Remainder")) 
			{
				ruleRemainder = methodTable.get(splits[i][1]);
				if (ruleRemainder == null) ruleRemainder = tempMethodTable.get(splits[i][1]);
			}
		}
		
		BoundingBox bb = Pools.obtain(BoundingBox.class);
		
		float x = (max.x-min.x) / 2.0f ;
		float y = (max.y-min.y) / 2.0f ;
		float z = (max.z-min.z) / 2.0f ;
		
		Vector3 nmin = Pools.obtain(Vector3.class);
		Vector3 nmax = Pools.obtain(Vector3.class);
		Vector3 center = Pools.obtain(Vector3.class).set(min).add(max).scl(0.5f);
		
		nmin.set(x, y, z).scl(-1);
		nmax.set(x, y, z);
		bb.set(nmin, nmax);
		
		bb.mul(composedTransform);
		
		Array<BoundingBox> usedVolumes = new Array<BoundingBox>(false, 16);
		
		for (OcclusionArea area : occluders)
		{
			JsonValue rule = null;
			for (String[] values : splits)
			{
				if (values[0].equalsIgnoreCase(area.name)) 
				{
					rule = methodTable.get(values[1]);
					if (rule == null) rule = tempMethodTable.get(values[1]);
					break;
				}
			}
			if (rule == null) continue;
			
			BoundingBox intersection = area.intersection(bb);
			
			if (intersection != null)
			{
				intersection.mul(invComposedTransform);
				
				nmin.set(intersection.min).add(center);
				nmax.set(intersection.max).add(center);
				intersection.set(nmin, nmax);
								
				children.add(new VolumePartitioner(intersection.min, intersection.max, rule, methodTable, this, occluders));
				
				usedVolumes.add(intersection);
			}
		}
		
		usedVolumes.sort(new Comparator<BoundingBox>()
				{
					@Override
					public int compare(BoundingBox bb0, BoundingBox bb1)
					{
						if (bb0.min.x < bb1.min.x) return -1;
						if (bb1.min.x < bb0.min.x) return 1;
						if (bb0.min.y < bb1.min.y) return -1;
						if (bb1.min.y < bb0.min.y) return 1;
						if (bb0.min.z < bb1.min.z) return -1;
						if (bb1.min.z < bb0.min.z) return 1;
						
						return 0;
					}
				});
		
		// fill X volumes
		
		nmin.set(min);
		nmax.set(max);
		
		if (usedVolumes.size == 0)
		{
			children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
		}
		
		for (BoundingBox uv : usedVolumes)
		{
			nmax.x = uv.min.x;
			
			if (nmin.x < nmax.x)
			{
				children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
			}
			
			nmin.x = uv.max.x;
		}
		
		nmax.x = max.x;
		
		if (nmin.x < nmax.x)
		{
			children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
		}
		
		// Fill Y volumes
		
		for (BoundingBox uv : usedVolumes)
		{	
			nmin.set(min);
			nmax.set(max);
			
			// Constrain to X bounds
			nmin.x = uv.min.x;
			nmax.x = uv.max.x;
			
			nmax.y = uv.min.y;
			
			// Place below
			if (nmin.y < nmax.y)
			{
				children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
			}
			
			nmin.y = uv.max.y;
			nmax.y = max.y;
			
			// Place above
			if (nmin.y < nmax.y)
			{
				children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
			}
		}
		
		// Fill Z volumes
		
		for (BoundingBox uv : usedVolumes)
		{	
			nmin.set(min);
			nmax.set(max);
			
			// Constrain to XY bounds
			nmin.x = uv.min.x;
			nmax.x = uv.max.x;
			
			nmin.y = uv.min.y;
			nmax.y = uv.max.y;
			
			nmax.z = uv.min.z;
			
			// Place front
			if (nmin.z < nmax.z)
			{
				children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
			}
			
			nmin.z = uv.max.z;
			nmax.z = max.z;
			
			// Place behind
			if (nmin.z < nmax.z)
			{
				children.add(new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders));
			}
		}
		
		Pools.free(bb);
		Pools.free(nmin);
		Pools.free(nmax);
		
		for (BoundingBox uv : usedVolumes) Pools.free(uv);
	}
	
	public void processDivide(JsonValue divide, String axis)
	{
		float interval = getVal(axis, max) - getVal(axis, min);
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
		String[] csvs = divide.asStringArray(); 
		
		for (int i = 0; i < csvs.length; i++)
		{
			String[] values = parseCSV(csvs[i]);
			
			String eqn = values[0];
			String ruleString = values[1];
			String coords = values.length > 2 ? values[2] : null;
			
			float size = parseEquation(eqn, interval, variables);
			
			setVal(axis, nmax, getVal(axis, nmin) + size);
			
			JsonValue rule = methodTable.get(ruleString);
			if (rule == null) rule = tempMethodTable.get(ruleString);
			if (rule == null) throw new RuntimeException("Rule: "+ruleString+" does not exist!");
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this, occluders);
			if (coords != null) vp.applyCoords(coords);
			children.add(vp);
			
			setVal(axis, nmin, getVal(axis, nmax));
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
	}

	public void processSelect(JsonValue select)
	{					
		Vector3 nmin = Pools.obtain(Vector3.class);
		Vector3 nmax = Pools.obtain(Vector3.class);
		
		Vector3 rmin = Pools.obtain(Vector3.class);
		Vector3 rmax = Pools.obtain(Vector3.class);
		
		rmin.set(min);
		rmax.set(max);
		
		String[] csvs = select.asStringArray();
		String remainder = null;
		
		for (int i = 0; i < csvs.length; i++)
		{
			String[] values = parseCSV(csvs[i]);
			
			String side = values[0];
			
			if (side.equalsIgnoreCase("Remainder")) 
			{
				remainder = csvs[i];
				continue;
			}
			
			String eqn = values[1];
			String ruleString = values[2];
			String coords = values.length > 3 ? values[3] : null ; 
			
			String axis = "";
			if (side.equalsIgnoreCase("left") || side.equalsIgnoreCase("right")) axis = "X";
			else if (side.equalsIgnoreCase("top") || side.equalsIgnoreCase("bottom")) axis = "Y";
			else if (side.equalsIgnoreCase("front") || side.equalsIgnoreCase("back")) axis = "Z";
			else throw new RuntimeException("Invalid side: "+side);
			
			float interval = getVal(axis, max) - getVal(axis, min);
			float size = parseEquation(eqn, interval, variables);

			nmin.set(rmin);
			nmax.set(rmax);
			
			if (side.equalsIgnoreCase("left"))
			{
				nmax.x = nmin.x + size;
				rmin.x = nmax.x;
			}
			else if (side.equalsIgnoreCase("right"))
			{
				nmin.x = nmax.x - size;
				rmax.x = nmin.x;
			}
			else if (side.equalsIgnoreCase("bottom"))
			{
				nmax.y = nmin.y + size;
				rmin.y = nmax.y;
			}
			else if (side.equalsIgnoreCase("top"))
			{
				nmin.y = nmax.y - size;
				rmax.y = nmin.y;
			}
			else if (side.equalsIgnoreCase("back"))
			{
				nmax.z = nmin.z + size;
				rmin.z = nmax.z;
			}
			else if (side.equalsIgnoreCase("front"))
			{
				nmin.z = nmax.z - size;
				rmax.z = nmin.z;
			}
			
			JsonValue rule = methodTable.get(ruleString);
			if (rule == null) rule = tempMethodTable.get(ruleString);
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this, occluders);
			if (coords != null) vp.applyCoords(coords);
			children.add(vp);
		}
		
		if (remainder != null)
		{
			String[] values = parseCSV(remainder);
			
			String ruleString = values[1];
			String coords = values.length > 2 ? values[2] : null ; 
			
			JsonValue rule = methodTable.get(ruleString);
			if (rule == null) rule = tempMethodTable.get(ruleString);
			VolumePartitioner vp = new VolumePartitioner(rmin, rmax, rule, methodTable, this, occluders);
			if (coords != null) vp.applyCoords(coords);
			children.add(vp);
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
		
		Pools.free(rmin);
		Pools.free(rmax);
	}
	
	public void processResize(String axis, float val)
	{
		Vector3 lastPos = Pools.obtain(Vector3.class).set(min).add(max).scl(0.5f);
		
		float interval = getVal(axis, max) - getVal(axis, min) ;
		
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
		
		Vector3 newPos = Pools.obtain(Vector3.class).set(min).add(max).scl(0.5f);
		Vector3 diff = newPos.sub(lastPos);
		this.localTransform.translate(diff);
		Pools.free(lastPos);
		Pools.free(newPos);
		composedTransform.set(parentTransform).mul(localTransform);
		invComposedTransform.set(composedTransform).inv();
	}
	
	public void processConditional(JsonValue conditional, boolean canInterrupt)
	{
		String[] values = parseCSV(conditional.asString());
		
		String condition = values[0];
		String ruleSucceed = values[1];
		String ruleFail = values[2];
		String chosenRule = evaluateConditional(condition) ? ruleSucceed : ruleFail ;
		
		JsonValue rule = methodTable.get(chosenRule);
		if (rule == null) rule = tempMethodTable.get(chosenRule);
		
		if (rule.child != null) 
		{
			if (canInterrupt) ruleStack.addFirst(rule.child);
			else processRuleBlock(rule);
		}
	}
	
	public void processMultiConditional(JsonValue conditional, boolean canInterrupt)
	{
		JsonValue current = conditional.child;
		while (current != null)
		{
			System.out.println("\nBlock: "+current.name);
			String[] conditions = parseCSV(current.name);
			boolean pass = true;
			
			if (!current.name.equalsIgnoreCase("else") && conditions.length > 0) 
			{
				for (String condition : conditions)
				{
					System.out.println("Condition: "+condition);
					if (!evaluateConditional(condition))
					{
						pass = false;
						break;
					}
				}
			}
			
			if (pass)
			{
				JsonValue rule = current.child;
				if (rule == null)
				{
					String ruleString = current.asString();
					rule = methodTable.get(ruleString);
					if (rule == null) rule = tempMethodTable.get(ruleString);
				}
				else
				{
					JsonValue temp = new JsonValue("TempRule");
					temp.name = "TempRule";
					temp.child = rule;
					rule = temp;
				}
				
				if (rule.child != null) 
				{
					if (canInterrupt) ruleStack.addFirst(rule.child);
					else processRuleBlock(rule);
				}
				break;
			}
			
			current = current.next;
		}
		
	}
	
	private static final String[] validComparisons = {"<=", ">=", "==", "<", ">"};
	private boolean evaluateConditional(String condition)
	{
		condition = condition.trim();
		
		boolean succeed = false;
		
		if (condition.startsWith("occluded("))
		{
			String name = condition.substring(8, condition.length()-1);
			
			float x = (max.x-min.x) / 2.0f ;
			float y = (max.y-min.y) / 2.0f ;
			float z = (max.z-min.z) / 2.0f ;
			
			BoundingBox bb = Pools.obtain(BoundingBox.class);
			Vector3 tmin = Pools.obtain(Vector3.class);
			Vector3 tmax = Pools.obtain(Vector3.class);
			
			tmin.set(x, y, z).scl(-1);
			tmax.set(x, y, z);
			bb.set(tmin, tmax);
			bb.mul(composedTransform);
						
			succeed = false;
			for (OcclusionArea oa : occluders)
			{
				if (oa.name.equalsIgnoreCase(name) && bb.intersects(oa.box)) 
				{
					succeed = true;
					break;
				}
			}
			
			Pools.free(bb);
			Pools.free(tmin);
			Pools.free(tmax);
		}
		else if (condition.startsWith("defined("))
		{
			String name = condition.substring(8, condition.length()-1);
			if (variables.containsKey(name) || defines.containsKey(name))
			{
				succeed = true;
			}
		}
		else
		{
			boolean found = false;
			
			for (String comparison : validComparisons)
			{
				if (condition.contains(comparison))
				{
					found = true;
					
					String[] parts = condition.split(comparison);
					float left = parseEquation(parts[0], 0, variables);
					float right = parseEquation(parts[1], 0, variables);
					
					System.out.println("Parts: "+parts[0]+" "+parts[1]);
					
					if (comparison.equals("<="))
					{
						succeed = left <= right;
					}
					else if (comparison.equals(">="))
					{
						succeed = left >= right;
					}
					else if (comparison.equals("=="))
					{
						succeed = left == right;
					}
					else if (comparison.equals("<"))
					{
						succeed = left < right;
					}
					else if (comparison.equals(">"))
					{
						succeed = left > right;
					}
					
					System.out.println(left+" "+comparison+" "+right+" = "+succeed+"     ("+condition+")");
					
					break;
				}
			}
			
			if (!found) throw new RuntimeException("Invalid conditional: "+condition);
		}
		
		return succeed;
	}
	
	public void processOcclude(JsonValue occlude)
	{
		String xeqn = occlude.getString("X", "100%");
		String yeqn = occlude.getString("Y", "100%");
		String zeqn = occlude.getString("Z", "100%");
		
		String oxeqn = occlude.getString("OX", "0");
		String oyeqn = occlude.getString("OY", "0");
		String ozeqn = occlude.getString("OZ", "0");
		
		float dx = max.x-min.x;
		float dy = max.y-min.y;
		float dz = max.z-min.z;
		
		float x = parseEquation(xeqn, dx, variables) / 2.0f ;
		float y = parseEquation(yeqn, dy, variables) / 2.0f ;
		float z = parseEquation(zeqn, dz, variables) / 2.0f ;
		
		float ox = parseEquation(oxeqn, dx, variables) / 2.0f ;
		float oy = parseEquation(oyeqn, dy, variables) / 2.0f ;
		float oz = parseEquation(ozeqn, dz, variables) / 2.0f ;
		
		BoundingBox bb = Pools.obtain(BoundingBox.class);
		Vector3 tmin = Pools.obtain(Vector3.class);
		Vector3 tmax = Pools.obtain(Vector3.class);
		
		dx /= 2;
		dy /= 2;
		dz /= 2;
		
		if (snap.x == -1)
		{
			tmin.x = -dx+ox;
			tmax.x = -dx+x*2+ox;
		}
		else if (snap.x == 1)
		{
			tmin.x = dx-x*2+ox;
			tmax.x = dx+ox;
		}
		else
		{
			tmin.x = -x+ox;
			tmax.x = x+ox;
		}
		
		if (snap.y == -1)
		{
			tmin.y = -dy+oy;
			tmax.y = -dy+y*2+oy;
		}
		else if (snap.y == 1)
		{
			tmin.y = dy-y*2+oy;
			tmax.y = dy+oy;
		}
		else
		{
			tmin.y = -y+oy;
			tmax.y = y+oy;
		}
		
		if (snap.z == -1)
		{
			tmin.z = -dz+oz;
			tmax.z = -dz+z*2+oz;
		}
		else if (snap.z == 1)
		{
			tmin.z = dz-z*2+oz;
			tmax.z = dz+oz;
		}
		else
		{
			tmin.z = -z+oz;
			tmax.z = z+oz;
		}
				
		bb.set(tmin, tmax);
		bb.mul(composedTransform);
		
		String name = occlude.getString("Name", "");
				
		occluders.add(new OcclusionArea(bb, name, this));
		
		Pools.free(tmin);
		Pools.free(tmax);
	}
	
	public boolean processRuleStack()
	{
		while (true)
		{
			JsonValue current = ruleStack.pollFirst();
			
			if (current == null)
			{
				break;
			}
			
			if (current.next != null) ruleStack.addFirst(current.next);
			
			if (!processRule(current, true)) 
			{
				return false;
			}
		}
		return true;
	}
	
	public void processRuleBlock(JsonValue rule)
	{
		JsonValue current = rule.child;
		while (current != null)
		{
			processRule(current, false);
			current = current.next;
		}
	}
	
	public boolean processRule(JsonValue current, boolean canInterrupt)
	{	
		String method = current.name;
		
		if (method.equalsIgnoreCase("Rule"))
		{
			JsonValue nrule = methodTable.get(current.asString());
			if (nrule == null) throw new RuntimeException("Invalid rule: "+current.asString());
			ruleStack.addFirst(nrule.child);
		}
		else if (method.equalsIgnoreCase("Child"))
		{
			JsonValue rule = current.child;
			if (rule == null)
			{
				String ruleString = current.asString();
				rule = methodTable.get(ruleString);
				if (rule == null) rule = tempMethodTable.get(ruleString);
			}
			else
			{
				JsonValue temp = new JsonValue("TempRule");
				temp.name = "TempRule";
				temp.child = rule;
				rule = temp;
			}
			
			VolumePartitioner vp = new VolumePartitioner(min, max, rule, methodTable, this, occluders);
			children.add(vp);
		}
		else if (method.equalsIgnoreCase("CoordinateSystem"))
		{
			applyCoords(current.asString());
		}
		else if (method.equalsIgnoreCase("Move"))
		{
			String xstring = current.getString("X", "0");
			String ystring = current.getString("Y", "0");
			String zstring = current.getString("Z", "0");
			
			float x = parseEquation(xstring, max.x-min.x, variables);
			float y = parseEquation(ystring, max.y-min.y, variables);
			float z = parseEquation(zstring, max.z-min.z, variables);
			
			Matrix4 trans = Pools.obtain(Matrix4.class).setToTranslation(x, y, z);
			
			transformVolume(trans);
			
			Pools.free(trans);
		}
		else if (method.equalsIgnoreCase("Rotate"))
		{
			if (current.child != null)
			{
				String xstring = current.getString("X", "0");
				String ystring = current.getString("Y", "1");
				String zstring = current.getString("Z", "0");
				String astring = current.getString("Angle", "0");
				
				float x = parseEquation(xstring, 0, variables);
				float y = parseEquation(ystring, 0, variables);
				float z = parseEquation(zstring, 0, variables);
				float a = parseEquation(astring, 0, variables);				
				
				Matrix4 rotation = Pools.obtain(Matrix4.class).setToRotation(x, y, z, a);
				
				transform(rotation);
				
				Pools.free(rotation);
			}
			else
			{
				applyCoords(current.asString());
			}
			
		}
		else if (method.equalsIgnoreCase("Conditional"))
		{
			processConditional(current, canInterrupt);
		}
		else if (method.equalsIgnoreCase("MultiConditional"))
		{
			processMultiConditional(current, canInterrupt);
		}
		else if (method.equalsIgnoreCase("Split"))
		{
			processSplit(current);
		}
		else if (method.equalsIgnoreCase("Select"))
		{
			processSelect(current);
		}
		else if (method.equalsIgnoreCase("Repeat"))
		{
			processRepeat(current, null);
		}
		else if (method.equalsIgnoreCase("RepeatX"))
		{
			processRepeat(current, "X");
		}
		else if (method.equalsIgnoreCase("RepeatY"))
		{
			processRepeat(current, "Y");
		}
		else if (method.equalsIgnoreCase("RepeatZ"))
		{
			processRepeat(current, "Z");
		}
		else if (method.equalsIgnoreCase("Divide"))
		{
			processDivide(current);
		}
		else if (method.equalsIgnoreCase("DivideX"))
		{
			processDivide(current, "X");
		}
		else if (method.equalsIgnoreCase("DivideY"))
		{
			processDivide(current, "Y");
		}
		else if (method.equalsIgnoreCase("DivideZ"))
		{
			processDivide(current, "Z");
		}
		else if (method.equalsIgnoreCase("Snap"))
		{
			snap.set(current.getFloat("X", snap.x), current.getFloat("Y", snap.y), current.getFloat("Z", snap.z));
		}
		else if (method.equalsIgnoreCase("X") || method.equalsIgnoreCase("Y") || method.equalsIgnoreCase("Z"))
		{
			String axis = method;
			String eqnString = current.asString();
			float interval = getVal(axis, max) - getVal(axis, min) ;
			
			float val = parseEquation(eqnString, interval, variables) ;
			
			processResize(axis, val);
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
		else if (method.equalsIgnoreCase("Occlude"))
		{
			processOcclude(current);
		}
		else if (method.equalsIgnoreCase("Mesh"))
		{
			Mesh = current;
		}
		else if (method.equalsIgnoreCase("Defer"))
		{
			return false;
		}
		else if (method.startsWith("TempRule"))
		{
			tempMethodTable.put(method, current);
		}
		else
		{
			throw new RuntimeException("Unrecognised Rule: "+method);
		}
		return true;
	}

	public ModelBatchData getModelBatchData()
	{
		JsonValue meshValue = Mesh;
		String meshName = meshValue.getString("Name");
		if (defines.containsKey(meshName)) meshName = defines.get(meshName);
		String textureName = meshValue.getString("Texture");
		boolean useTriplanarSampling = meshValue.getBoolean("TriplanarSample", false);
		float triplanarScale = 0;
		if (useTriplanarSampling) triplanarScale = meshValue.getFloat("TriplanarScale");
		boolean seamless = meshValue.getBoolean("IsSeamless", true);
		
		if (defines.containsKey(textureName)) textureName = defines.get(textureName);
		String mbname = "";
		
		if (meshName.equalsIgnoreCase("Sphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta", "8")+"Phi"+meshValue.getString("Phi", "8")+useTriplanarSampling+triplanarScale+seamless;
		}
		else if (meshName.equalsIgnoreCase("HemiSphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta", "8")+"Phi"+meshValue.getString("Phi", "8")+useTriplanarSampling+triplanarScale+seamless;
		}
		else if (meshName.equalsIgnoreCase("Prism"))
		{	
			String eqn = meshValue.getString("loft", "100%");
			mbname = meshName+textureName+"Loft"+eqn+useTriplanarSampling+triplanarScale+seamless;
		}
		else if (meshName.equalsIgnoreCase("Cylinder"))
		{	
			mbname = meshName+textureName+"Phi"+meshValue.getString("Phi", "8")+"HollowScale"+meshValue.getString("HollowScale", "0")+useTriplanarSampling+triplanarScale+seamless;
		} 
		else if (meshName.equalsIgnoreCase("Box"))
		{	
			String eqnx = meshValue.getString("loftX", "100%");
			String eqnz = meshValue.getString("loftZ", "100%");
			int snapx = meshValue.getInt("snapX", 0);
			int snapz = meshValue.getInt("snapZ", 0);
			mbname = meshName+textureName+"SnapX"+snapx+"SnapZ"+snapz+"LoftX"+eqnx+"LoftZ"+eqnz+useTriplanarSampling+triplanarScale+seamless;
		}
		else
		{
			mbname = meshName+textureName+useTriplanarSampling+triplanarScale+seamless;
		}
		
		ModelBatchData data = FileUtils.loadModelBatchData(mbname);
		if (data == null)
		{
			Mesh mesh = null;
			if (meshName.equalsIgnoreCase("Box"))
			{
				String eqnx = meshValue.getString("loftX", "100%");
				String eqnz = meshValue.getString("loftZ", "100%");
				
				float loftx = eqnx.equalsIgnoreCase("100%") ? 1 : parseEquation(eqnx, 1, variables);
				float loftz = eqnz.equalsIgnoreCase("100%") ? 1 : parseEquation(eqnz, 1, variables);
				
				int snapx = meshValue.getInt("snapX", 0);
				int snapz = meshValue.getInt("snapZ", 0);
				
				mesh = Shapes.getBoxMesh(1, loftx, snapx, 1, 1, loftz, snapz, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("Cylinder"))
			{
				int phi = meshValue.getInt("Phi", 8);
				
				String eqns = meshValue.getString("HollowScale", "0");
				float scale = eqns.equalsIgnoreCase("0") ? 0 : parseEquation(eqns, 1, variables); 
				
				mesh = Shapes.getCylinderMesh(phi, scale>0, scale, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("Sphere"))
			{
				int theta = meshValue.getInt("Theta", 8);
				int phi = meshValue.getInt("Phi", 8);
				mesh = Shapes.getSphereMesh(theta, phi, 1, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("HemiSphere"))
			{
				int theta = meshValue.getInt("Theta", 8);
				int phi = meshValue.getInt("Phi", 8);
				mesh = Shapes.getHemiSphereMesh(theta, phi, 1, true, !useTriplanarSampling);
			}
			else if (meshName.equalsIgnoreCase("Prism"))
			{
				String eqn = meshValue.getString("loft", "100%");
				float loft = eqn.equalsIgnoreCase("100%") ? 1 : parseEquation(eqn, 1, variables);
				mesh = Shapes.getPrismMesh(1, loft, 1, 1, true, !useTriplanarSampling);
			}
			else
			{
				mesh = FileUtils.loadMesh(meshName);
			}

			TextureWrap wrap = seamless ? TextureWrap.Repeat : TextureWrap.MirroredRepeat;
			Texture[] textures = FileUtils.getTextureGroup(new String[]{textureName}, wrap);
						
			data = new ModelBatchData(mesh, GL20.GL_TRIANGLES, textures, false, false, useTriplanarSampling, triplanarScale);
			FileUtils.storeModelBatchData(mbname, data);
		}
		return data;
	}

	private void collectMeshesInternal(Entity entity, OcttreeEntry<Entity> entry, btTriangleMesh triangleMesh)
	{
		if (Mesh != null)
		{
			ModelBatchData data = getModelBatchData();
			ModelBatchInstance mb = new ModelBatchInstance(data);
			BoundingBox bb = Pools.obtain(BoundingBox.class);
			mb.getMesh().calculateBoundingBox(bb);
			
			Vector3 meshDim = bb.getDimensions();
			meshDim.set(1.0f / meshDim.x, 1.0f / meshDim.y, 1.0f / meshDim.z);
			Vector3 volumeDim = Pools.obtain(Vector3.class).set(max).sub(min);		
	
			Vector3 scale = volumeDim.scl(meshDim);

			Matrix4 transform = Pools.obtain(Matrix4.class).idt().mul(this.composedTransform).scale(scale.x, scale.y, scale.z);

			entity.addRenderable(mb, transform);

			Pools.free(bb);			
			Pools.free(transform);	
			Pools.free(volumeDim);
			
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
		
		for (VolumePartitioner vp : children)
		{
			vp.collectMeshesInternal(entity, entry, triangleMesh);
		}
	}

	public void collectMeshes(Entity entity, OcttreeEntry<Entity> entry, btTriangleMesh triangleMesh)
	{		
		collectMeshesInternal(entity, entry, triangleMesh);
	}

	public static void loadImportsAndBuildMethodTable(Array<String> importedFiles, JsonValue root, HashMap<String, JsonValue> methodTable, String fileName, HashMap<String, String> renameTable, boolean addMain)
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
					
					String nfileName = file.substring(file.lastIndexOf("/")+1);
					nfileName = nfileName.substring(0, nfileName.lastIndexOf(".")+1);
					
					String contents = Gdx.files.internal(file).readString();
					JsonValue nroot = new JsonReader().parse(contents);
					
					loadImportsAndBuildMethodTable(importedFiles, nroot, methodTable, nfileName, renameTable, false);
				}
			}
		}
		
		JsonValue current = root.child;
		while(current != null)
		{
			if (current.name.equalsIgnoreCase("Main"))
			{
				if (addMain)
				{
					methodTable.put(current.name, current);
				}
			}
			else if (current.name.equalsIgnoreCase("Imports"))
			{
				
			}
			else 
			{
				methodTable.put(fileName+current.name, current);
				
				renameTable.put(current.name, fileName+current.name);
			}
			current = current.next;
		}
		
		correctRenames(root, renameTable);
		renameTable.clear();
	}
	
	private static void correctRenames(JsonValue current, HashMap<String, String> renameTable)
	{
		if (current.isString())
		{
			String cString = current.asString();
			if (cString != null)
			{
				if (renameTable.containsKey(cString))
				{
					current.set(renameTable.get(cString));
				}
			}
		}
		if (current.child != null) correctRenames(current.child, renameTable);
		if (current.next != null) correctRenames(current.next, renameTable);
	}
	
	public static VolumePartitioner load(String file)
	{
		HashMap<String, JsonValue> methodTable = FileUtils.loadGrammar(file);
		JsonValue main = methodTable.get("Main");

		return new VolumePartitioner(new Vector3(), new Vector3(), main, methodTable, null, new Array<OcclusionArea>(false, 16));
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
		
		if (modFunc == null)
		{
			try
			{
				modFunc = new CustomFunction("mod", 2) {
					public double applyFunction(double... value) 
					{
						return value[0] % value[1] ;
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
		expBuilder.withCustomFunction(modFunc);
		expBuilder.withOperation(percentOperator);
		expBuilder.withVariables(variables);
		
		expBuilder.withVariable("X", max.x-min.x);
		expBuilder.withVariable("Y", max.y-min.y);
		expBuilder.withVariable("Z", max.z-min.z);
		
		expBuilder.withVariable("x", (max.x-min.x)/100.0f);
		expBuilder.withVariable("y", (max.y-min.y)/100.0f);
		expBuilder.withVariable("z", (max.z-min.z)/100.0f);
		
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
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("Error parsing equation: "+equation);
		}

		return size;
	}

	private static CustomFunction rndFunc;
	private static CustomFunction modFunc;
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

	private static class OcclusionArea
	{
		BoundingBox box;
		String name;
		VolumePartitioner parent;
		
		public OcclusionArea(BoundingBox bb, String name, VolumePartitioner parent)
		{
			this.box = bb;
			this.name = name;
			this.parent = parent;
		}
		
		public BoundingBox intersection(BoundingBox other)
		{
			if (!other.intersects(box)) return null;
			
			BoundingBox bb = Pools.obtain(BoundingBox.class);
			Vector3 min = Pools.obtain(Vector3.class);
			Vector3 max = Pools.obtain(Vector3.class);
			
			min.x = other.min.x > box.min.x ? other.min.x : box.min.x;
			min.y = other.min.y > box.min.y ? other.min.y : box.min.y;
			min.z = other.min.z > box.min.z ? other.min.z : box.min.z;
			
			max.x = other.max.x < box.max.x ? other.max.x : box.max.x;
			max.y = other.max.y < box.max.y ? other.max.y : box.max.y;
			max.z = other.max.z < box.max.z ? other.max.z : box.max.z;
			
			bb.set(min, max);
			
			Pools.free(min);
			Pools.free(max);
			
			return bb;
		}
	}
}
