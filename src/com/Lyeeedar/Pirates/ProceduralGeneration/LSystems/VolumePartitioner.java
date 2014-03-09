package com.Lyeeedar.Pirates.ProceduralGeneration.LSystems;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Graphics.Batchers.ModelBatcher;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.Shapes;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.Pools;

public class VolumePartitioner
{
	public final VolumePartitioner parent; 
	
	public final JsonValue rule;
	public final JsonValue rootRule;
	
	public final Vector3 min = new Vector3();
	public final Vector3 max = new Vector3();
	
	public final Array<VolumePartitioner> children = new Array<VolumePartitioner>(false, 16);
	
	public VolumePartitioner(Vector3 min, Vector3 max, JsonValue rule, JsonValue rootRule, VolumePartitioner parent)
	{
		this.min.set(min);
		this.max.set(max);
		this.rule = rule;
		this.rootRule = rootRule;
		this.parent = parent;
	}
	
	public void evaluate()
	{
		processRule(rule);
		for (VolumePartitioner vp : children) vp.evaluate();
	}
	
	public void divideX(float size, int repeats, boolean percentage, JsonValue ruleSub, JsonValue ruleRemainder)
	{
		float step = percentage ? (max.x - min.x) * (size/100.0f) : size ;
		
		Vector3 tmpVec1 = Pools.obtain(Vector3.class);
		Vector3 tmpVec2 = Pools.obtain(Vector3.class);
		
		tmpVec1.set(min);
		tmpVec2.set(max);
		
		tmpVec2.x = tmpVec1.x + step;
		
		int rep = 0;
		while (true)
		{
			if (rep == repeats) break;
			else if (repeats <= 0)
			{
				if (tmpVec2.x > max.x)
				{
					break;
				}
			}
			
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleSub, rootRule, this));
			
			tmpVec1.x = tmpVec2.x;
			tmpVec2.x += step;
			
			rep++;
		}
		
		if (ruleRemainder != null && tmpVec1.x < max.x)
		{
			tmpVec2.x = max.x;
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleRemainder, rootRule, this));
		}
		
		Pools.free(tmpVec1);
		Pools.free(tmpVec2);
	}
	
	
	public void divideY(float size, int repeats, boolean percentage, JsonValue ruleSub, JsonValue ruleRemainder)
	{
		float step = percentage ? (max.y - min.y) * (size/100.0f) : size ;
		
		Vector3 tmpVec1 = Pools.obtain(Vector3.class);
		Vector3 tmpVec2 = Pools.obtain(Vector3.class);
		
		tmpVec1.set(min);
		tmpVec2.set(max);
		
		tmpVec2.y = tmpVec1.y + step;
		
		int rep = 0;
		while (true)
		{
			if (rep == repeats) break;
			else if (repeats <= 0)
			{
				if (tmpVec2.y > max.y)
				{
					break;
				}
			}
			
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleSub, rootRule, this));
			
			tmpVec1.y = tmpVec2.y;
			tmpVec2.y += step;
			
			rep++;
		}
		
		if (ruleRemainder != null && tmpVec1.y < max.y)
		{
			tmpVec2.y = max.y;
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleRemainder, rootRule, this));
		}
		
		Pools.free(tmpVec1);
		Pools.free(tmpVec2);
	}
	
	public void divideZ(float size, int repeats, boolean percentage, JsonValue ruleSub, JsonValue ruleRemainder)
	{		
		float step = percentage ? (max.z - min.z) * (size/100.0f) : size ;
		
		Vector3 tmpVec1 = Pools.obtain(Vector3.class);
		Vector3 tmpVec2 = Pools.obtain(Vector3.class);
		
		tmpVec1.set(min);
		tmpVec2.set(max);
		
		tmpVec2.z = tmpVec1.z + step;
		
		int rep = 0;
		while (true)
		{
			if (rep == repeats) break;
			else if (repeats <= 0)
			{
				if (tmpVec2.z > max.z)
				{
					break;
				}
			}
			
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleSub, rootRule, this));
			
			tmpVec1.z = tmpVec2.z;
			tmpVec2.z += step;
			
			rep++;
		}
		
		if (ruleRemainder != null && tmpVec1.z < max.z)
		{
			tmpVec2.z = max.z;
			children.add(new VolumePartitioner(tmpVec1, tmpVec2, ruleRemainder, rootRule, this));
		}
		
		Pools.free(tmpVec1);
		Pools.free(tmpVec2);
	}
	
	public void processDivide(JsonValue divide)
	{
		String axis = divide.getString("Axis");
		String sizeString = divide.getString("Size");
		boolean percentage = sizeString.endsWith("%");
		float size = percentage ? Float.parseFloat(sizeString.substring(0, sizeString.length()-1)) : Float.parseFloat(sizeString) ;
		int repeats = divide.getInt("Repeats");
		String ruleSubString = divide.getString("Rule");
		JsonValue ruleSub = rootRule.get(ruleSubString);
		String ruleRemainderString = divide.getString("RemainderRule");
		JsonValue ruleRemainder = rootRule.get(ruleRemainderString);
		
		if (axis.equalsIgnoreCase("X")) divideX(size, repeats, percentage, ruleSub, ruleRemainder);
		else if (axis.equalsIgnoreCase("Y")) divideY(size, repeats, percentage, ruleSub, ruleRemainder);
		else if (axis.equalsIgnoreCase("Z")) divideZ(size, repeats, percentage, ruleSub, ruleRemainder);
		else throw new RuntimeException("Invalid Axis: "+axis);
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
				JsonValue nrule = rootRule.get(current.asString());
				processRule(nrule);
			}
			else if (method.equalsIgnoreCase("Divide"))
			{
				processDivide(current);
			}
			else if (method.equalsIgnoreCase("X"))
			{
				String valString = current.asString();
				float val = valString.endsWith("%") ? ( Float.parseFloat(valString.substring(0, valString.length()-1)) / 100.0f ) * (max.x - min.x) : Float.parseFloat(valString) ;
				val /= 2.0f;
				
				min.x = (max.x - min.x) / 2.0f + min.x;
				max.x = min.x;
				
				min.x -= val;
				max.x += val;
			}
			else if (method.equalsIgnoreCase("Y"))
			{
				String valString = current.asString();
				float val = valString.endsWith("%") ? ( Float.parseFloat(valString.substring(0, valString.length()-1)) / 100.0f ) * (max.y - min.y) : Float.parseFloat(valString) ;
				val /= 2.0f;
				
				min.y = (max.y - min.y) / 2.0f + min.y;
				max.y = min.y;
				
				min.y -= val;
				max.y += val;
			}
			else if (method.equalsIgnoreCase("Z"))
			{
				String valString = current.asString();
				float val = valString.endsWith("%") ? ( Float.parseFloat(valString.substring(0, valString.length()-1)) / 100.0f ) * (max.z - min.z) : Float.parseFloat(valString) ;
				val /= 2.0f;
				
				min.z = (max.z - min.z) / 2.0f + min.z;
				max.z = min.z;
				
				min.z -= val;
				max.z += val;
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
			
			mb = new ModelBatcher(mesh, GL20.GL_TRIANGLES, FileUtils.getTextureArray(new String[]{textureName}), false);
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
		center.set(max).sub(min).scl(0.5f).add(min);
		collectMeshes(entity, center);
		Pools.free(center);
	}
	
	public static VolumePartitioner load(String file)
	{
		String contents = Gdx.files.internal(file).readString();
		JsonValue root = new JsonReader().parse(contents);
		JsonValue main = root.get("Main");
		
		return new VolumePartitioner(new Vector3(), new Vector3(), main, root, null);
	}
}
