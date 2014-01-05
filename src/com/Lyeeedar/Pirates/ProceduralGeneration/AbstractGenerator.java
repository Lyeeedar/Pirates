package com.Lyeeedar.Pirates.ProceduralGeneration;

import java.util.List;

import com.Lyeeedar.Entities.Entity;
import com.badlogic.gdx.graphics.Color;

public interface AbstractGenerator {

	public Color[][] generate(List<Entity> entities);
	
}
