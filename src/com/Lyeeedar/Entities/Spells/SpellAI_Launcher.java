package com.Lyeeedar.Entities.Spells;

import java.util.ArrayList;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.EntityGraph;
import com.Lyeeedar.Graphics.Particles.ParticleEffect;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

public class SpellAI_Launcher extends SpellAI {

	public Vector3 rotation = new Vector3();

	private PositionalData pData = new PositionalData();
	private StatusData sData1 = new StatusData();
	private StatusData sData2 = new StatusData();
	
	public PerspectiveCamera cam;
	public ArrayList<EntityGraph> list = new ArrayList<EntityGraph>();
	public Vector3 tmp = new Vector3();
	
	public float cd = 0;
	public float speed;
	public float duration;
	
	public SpellAI_Launcher(Vector3 rotation, float speed, float duration)
	{
		this.rotation.set(rotation);
		this.speed = speed;
		this.duration = duration;
		
		cam = new PerspectiveCamera(120, GLOBALS.RESOLUTION[0], GLOBALS.RESOLUTION[1]);
	}
	
	@Override
	public boolean update(float delta, Spell spell) {
		
		duration -= delta;
		
		if (duration < 0) return false;
		
		cd -= delta;
		
		if (cd < 0)
		{
			cd = speed;
			
			cam.position.set(spell.position);
			cam.direction.set(rotation);
			cam.update();
			
			GLOBALS.WORLD.getVisible(cam, list);
			if (list.size() > 0)
			{
				spell.caster.readData(sData1, StatusData.class);
				
				Entity found = null;
				float minDist = Float.MAX_VALUE;
				for (EntityGraph eg : list)
				{
					eg.entity.readData(sData2, StatusData.class);
					
					if (sData2.factions.size() > 0)
					{
						boolean fail = false;
						for (String faction : sData2.factions)
						{
							if (fail) break;
							for (String faction2 : sData1.factions)
							{
								if (fail) break;
								if (faction.equals(faction2)) fail = true;
							}
						}
						if (fail) continue;
					}
					else
					{
						continue;
					}
					
					eg.entity.readData(pData, PositionalData.class);
					float dist = spell.position.dst2(pData.position);
					if (dist < minDist)
					{
						found = eg.entity;
						minDist = dist;
					}
				}
				
				if (found != null)
				{
					found.readData(pData, PositionalData.class);
					
//					SpellAI aimove = new SpellAI_HomingBolt(pData.position.sub(spell.position), 0.5f, 20, 1);
//					SpellAI aidam = new SpellAI_Explosion(1, 0.5f, 2, 0.3f, 6);
//					ParticleEffect effect = FileUtils.loadParticleEffect("data/effects/boom.effect");
//					Spell s = new Spell(spell.position, aimove, aidam, effect, spell.caster);
//					GLOBALS.pendingSPELLS.add(s);
				}
				
				list.clear();
			}
		
		}
		
		return true;
		
	}

	@Override
	public void dispose() {
		pData.dispose();
		sData2.dispose();
	}

}
