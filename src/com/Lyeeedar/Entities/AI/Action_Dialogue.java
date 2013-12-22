package com.Lyeeedar.Entities.AI;

import com.Lyeeedar.Entities.Entity;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Util.Dialogue;

public class Action_Dialogue extends ActivationAction {

	Dialogue dia;
	boolean added = false;
	
	public Action_Dialogue(String desc, Dialogue dia) {
		super(desc);
		this.dia = dia;
	}
	
	@Override
	public void activate(Entity activator, Entity Activatee)
	{
		if (!added)
		{
			GLOBALS.DIALOGUES.add(dia);
			dia.setInform(this);
			added = true;
		}
	}

	@Override
	public void inform() {
		added = false;
	}

}
