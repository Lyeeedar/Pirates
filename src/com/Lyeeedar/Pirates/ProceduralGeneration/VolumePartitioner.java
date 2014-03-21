package com.Lyeeedar.Pirates.ProceduralGeneration;

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
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
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
	public final Array<BoundingBox> occluders;

	public final JsonValue rule;
	
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
	
	public Vector3 snap = new Vector3();

	public VolumePartitioner(Vector3 min, Vector3 max, JsonValue rule, HashMap<String, JsonValue> methodTable, VolumePartitioner parent, Array<BoundingBox> occluders)
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
		}
		
		composedTransform.set(parentTransform).mul(localTransform);
				
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

			this.snap.set(parent.snap);
		}
	}
	
	public void transformVolume(Matrix4 transform)
	{
		this.localTransform.mul(transform);
		composedTransform.set(parentTransform).mul(localTransform);
				
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
		evaluateInternal();
	}
	
	private void evaluateInternal()
	{
		processRule(rule);
		for (VolumePartitioner vp : children) vp.evaluateInternal();
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
		boolean up = !axis.startsWith("-");
		
		Vector3 nmin = Pools.obtain(Vector3.class).set(min);
		Vector3 nmax = Pools.obtain(Vector3.class).set(max);
		
		if (up) setVal(axis, nmax, getVal(axis, nmin)+offset);
		else setVal(axis, nmin, getVal(axis, nmax)-offset);
		
		if (ruleOffset != null)
		{
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleOffset, methodTable, this, occluders);
			children.add(vp);
			vp.applyCoords(offsetCoord);
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
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleSub, methodTable, this, occluders);
			children.add(vp);
			vp.applyCoords(ruleCoord);
			
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
				}
			}
			else
			{
				if (getVal(axis, nmax) > getVal(axis, min))
				{
					setVal(axis, nmin, getVal(axis, min));
				}
			}
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, ruleRemainder, methodTable, this, occluders);
			children.add(vp);
			vp.applyCoords(remainderCoord);
		}

		Pools.free(nmin);
		Pools.free(nmax);
	}

	public void processRepeat(JsonValue repeat, String axis)
	{
		axis = axis == null ? repeat.getString("Axis") : axis ;
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
			
			VolumePartitioner vp = new VolumePartitioner(nmin, nmax, rule, methodTable, this, occluders);
			if (coords != null) vp.applyCoords(coords[i]);
			children.add(vp);
			
			setVal(axis, nmin, getVal(axis, nmax));
		}
		
		Pools.free(nmin);
		Pools.free(nmax);
	}
	
	public static String[] parseCSV(String csv)
	{
		String[] values = csv.split(",");
		for (int i = 0; i < values.length; i++)
		{
			values[i] = values[i].trim();
		}
		return values;
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
	}
	
	public void processConditional(JsonValue conditional)
	{
		String[] values = parseCSV(conditional.asString());
		
		String condition = values[0];
		String ruleSucceed = values[1];
		String ruleFail = values[2];
		String chosenRule = evaluateConditional(condition) ? ruleSucceed : ruleFail ;
		
		JsonValue rule = methodTable.get(chosenRule);
		
		processRule(rule);
	}
	
	public void processMultiConditional(JsonValue conditional)
	{
		JsonValue current = conditional.child;
		while (current != null)
		{
			System.out.println("\n"+current.name);
			String[] conditions = parseCSV(current.name);
			boolean pass = true;
			
			if (!current.name.equalsIgnoreCase("") && conditions.length > 0) 
			{
				for (String condition : conditions)
				{
					System.out.println(condition);
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
				}
				else
				{
					JsonValue temp = new JsonValue("TempRule");
					temp.name = "TempRule";
					temp.child = rule;
					rule = temp;
				}
				
				processRule(rule);
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
		
		if (condition.equalsIgnoreCase("occluded"))
		{
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
			for (BoundingBox obb : occluders)
			{
				if (bb.intersects(obb)) 
				{
					succeed = true;
					break;
				}
			}
			
			Pools.free(bb);
			Pools.free(tmin);
			Pools.free(tmax);
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
		String xeqn = occlude.getString("X");
		String yeqn = occlude.getString("Y");
		String zeqn = occlude.getString("Z");
		
		float x = parseEquation(xeqn, max.x-min.x, variables) / 2.0f ;
		float y = parseEquation(yeqn, max.y-min.y, variables) / 2.0f ;
		float z = parseEquation(zeqn, max.z-min.z, variables) / 2.0f ;
		
		
		BoundingBox bb = Pools.obtain(BoundingBox.class);
		Vector3 tmin = Pools.obtain(Vector3.class);
		Vector3 tmax = Pools.obtain(Vector3.class);
				
		tmin.set(x, y, z).scl(-1);
		tmax.set(x, y, z);
		bb.set(tmin, tmax);
		bb.mul(composedTransform);
				
		occluders.add(bb);
		
		Pools.free(tmin);
		Pools.free(tmax);
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
				applyCoords(current.asString());
			}
			else if (method.equalsIgnoreCase("Rotate"))
			{
				float axisx = current.getFloat("X");
				float axisy = current.getFloat("Y");
				float axisz = current.getFloat("Z");
				float angle = current.getFloat("Angle");
				Matrix4 rotation = Pools.obtain(Matrix4.class).setToRotation(axisx, axisy, axisz, angle);
				
				transformVolume(rotation);
				
				Pools.free(rotation);
				
			}
			else if (method.equalsIgnoreCase("Conditional"))
			{
				processConditional(current);
			}
			else if (method.equalsIgnoreCase("MultiConditional"))
			{
				processMultiConditional(current);
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
			else if (method.equalsIgnoreCase("Occludes"))
			{
				processOcclude(current);
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
		boolean useTriplanarSampling = meshValue.getBoolean("TriplanarSample", false);
		float triplanarScale = 0;
		if (useTriplanarSampling) triplanarScale = meshValue.getFloat("TriplanarScale");
		boolean seamless = meshValue.getBoolean("IsSeamless", true);
		
		if (defines.containsKey(textureName)) textureName = defines.get(textureName);
		String mbname = "";
		
		if (meshName.equalsIgnoreCase("Sphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta")+"Phi"+meshValue.getString("Phi")+useTriplanarSampling+triplanarScale+seamless;
		}
		else if (meshName.equalsIgnoreCase("HemiSphere"))
		{
			mbname = meshName+textureName+"Theta"+meshValue.getString("Theta")+"Phi"+meshValue.getString("Phi")+useTriplanarSampling+triplanarScale+seamless;
		}
		else if (meshName.equalsIgnoreCase("Prism"))
		{	
			String eqn = meshValue.getString("loft", "100%");
			mbname = meshName+textureName+"Loft"+eqn+useTriplanarSampling+triplanarScale+seamless;
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

		return new VolumePartitioner(new Vector3(), new Vector3(), main, methodTable, null, new Array<BoundingBox>(false, 16));
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
