package com.Lyeeedar.Screens;

import java.util.HashMap;
import java.util.Random;

import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.StatusData;
import com.Lyeeedar.Entities.Entity.StatusData.STATS;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Pirates.GLOBALS.ELEMENTS;
import com.Lyeeedar.Util.FileUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;

public class CharacterScreen extends AbstractScreen
{
	Table left;
	Table tl;
	Table bl;
	Table right;
	EquipmentData eData;
	StatusData sData;
	
	StatusData emptySData = new StatusData();
	
	public CharacterScreen(PirateGame game)
	{
		super(game);
	}
	
	public void testItems(EquipmentData eData)
	{
		Random ran = new Random();
		for (Equipment_Slot et : Equipment_Slot.values())
		{
			ITEM_TYPE it = Item.fromEquipmentSlot(et);
			
			for (int i = 0; i < 10; i++)
			{
			
				Armour a = new Armour();
				a.description = new DESCRIPTION(et+" "+it+" "+ran.nextInt(50), "AN item of type "+it, "An item that was of type "+et+" and got converted into type "+it+". It is a test item and therefore isnt interesting at all.", it, "data/textures/blank.png");
				a.statusModifier = new StatusData();
				
				for (STATS stat : STATS.values())
				{
					a.statusModifier.stats.put(stat, ran.nextInt(500));
				}
				
				for (ELEMENTS element : ELEMENTS.values())
				{
					a.statusModifier.defense.put(element, ran.nextInt(500));
					a.statusModifier.attack.put(element, ran.nextInt(500));
				}
				
				eData.addItem(a);
			
			}
		}
	}

	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(stage);
		eData = GLOBALS.player.readOnlyRead(EquipmentData.class);
		sData = GLOBALS.player.readOnlyRead(StatusData.class);
		testItems(eData);
		create();
	}

	@Override
	public void hide()
	{
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void create()
	{	
		if (GLOBALS.player == null || eData == null) return;
		
		stage.clear();
		
		left = new Table();
		left.debug();
		tl = new Table();
		tl.debug();
		bl = new Table();
		bl.debug();
		right = new Table();
		right.debug();
		
		Table root = new Table();
		root.debug();
		
		root.setFillParent(true);
		root.setBackground(new SpriteDrawable(new Sprite(FileUtils.loadTexture("data/textures/spellbook.png", true, null, null))));

		root.add(left).expand().uniform().fill().padLeft((int)GLOBALS.sclX(65));
		root.add(right).expand().uniform().fill().padRight((int)GLOBALS.sclX(70));
		
		stage.addActor(root);
		
		fillLeftSlots();
		fillRight(emptySData, emptySData);
	}
	
	public void fillLeftSlots()
	{
		BitmapFont font = FileUtils.getFont("data/skins/arial.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;
		
		left.clear();
		tl.clear();
		bl.clear();
		
		Table main = new Table();
		main.debug();
		Table off = new Table();
		off.debug();
		Table armour = new Table();
		armour.debug();
		
		Image rarm = getEquipSlot(eData, Equipment_Slot.RARM, "data/skins/HEAD.png");
		Image rarmoff1 = getEquipSlot(eData, Equipment_Slot.RARMOFF1, "data/skins/TORSO.png");
		Image rarmoff2 = getEquipSlot(eData, Equipment_Slot.RARMOFF2, "data/skins/FEET.png");
		Image rarmoff3 = getEquipSlot(eData, Equipment_Slot.RARMOFF3, "data/skins/FEET.png");
		
		Image larm = getEquipSlot(eData, Equipment_Slot.LARM, "data/skins/HEAD.png");
		Image larmoff1 = getEquipSlot(eData, Equipment_Slot.LARMOFF1, "data/skins/TORSO.png");
		Image larmoff2 = getEquipSlot(eData, Equipment_Slot.LARMOFF2, "data/skins/FEET.png");
		Image larmoff3 = getEquipSlot(eData, Equipment_Slot.LARMOFF3, "data/skins/FEET.png");
		
		Image head = getEquipSlot(eData, Equipment_Slot.HEAD, "data/skins/HEAD.png");
		Image torso = getEquipSlot(eData, Equipment_Slot.TORSO, "data/skins/TORSO.png");
		Image legs = getEquipSlot(eData, Equipment_Slot.LEGS, "data/skins/FEET.png");
		Image feet = getEquipSlot(eData, Equipment_Slot.FEET, "data/skins/FEET.png");
		
		tl.defaults().padRight((int)GLOBALS.sclX(55));
		
		tl.add(main).left();
		tl.row();
		tl.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true, null, null)));
		tl.row();
		tl.add(off).left();
		tl.row();
		tl.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true, null, null)));
		tl.row();
		tl.add(armour).left();
		tl.row();
		
		main.defaults().pad((int)GLOBALS.sclX(10)).width((int)GLOBALS.sclX(75)).height((int)GLOBALS.sclX(75));
		main.add(rarm);
		main.add(rarmoff1);
		main.add(rarmoff2);
		main.add(rarmoff3);
		
		off.defaults().pad((int)GLOBALS.sclX(10)).width((int)GLOBALS.sclX(75)).height((int)GLOBALS.sclX(75));
		off.add(larm);
		off.add(larmoff1);
		off.add(larmoff2);
		off.add(larmoff3);
		
		armour.defaults().pad((int)GLOBALS.sclX(10)).width((int)GLOBALS.sclX(75)).height((int)GLOBALS.sclX(75));
		armour.add(head);
		armour.add(torso);
		armour.add(legs);
		armour.add(feet);
		
		left.add(tl).expand().fill();
		left.row();
		left.add(bl).padBottom((int)GLOBALS.sclX(75)).height((int)GLOBALS.sclX(50));
	}
	
	public void fillLeftItemsSimple(final Equipment_Slot slot)
	{
		BitmapFont font = FileUtils.getFont("data/skins/arial.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.WHITE;
		
		left.clear();
		
		Table scroll = new Table();
		scroll.debug();
		ScrollPane scrollPane = new ScrollPane(scroll);

		left.add(scrollPane).expand().fill().top().left().expand().padTop((int)GLOBALS.sclX(55)).padBottom((int)GLOBALS.sclX(55));
		
		Array<Item> items = eData.getItems(Item.fromEquipmentSlot(slot));
		
		for (Item i : items)
		{
			final Equipment<?> e = (Equipment<?>) i;
			
			Image image = null;

			if (i == null || i.description == null || i.description.icon == null)
			{
				image = new Image(FileUtils.loadTexture("data/textures/blank.png", true, null, null));
			}
			else
			{
				image = new Image(FileUtils.loadTexture(i.description.icon, true, null, null));
			}
			
			final Table entry = new Table();
			entry.debug();
			entry.add(image).left().width((int)GLOBALS.sclX(75)).height((int)GLOBALS.sclX(75)).padRight((int)GLOBALS.sclX(5));
			entry.add(i.getDescriptionSimple()).left().expandX().fillX();
			entry.setBackground(new SpriteDrawable(new Sprite(FileUtils.loadTexture("data/textures/blank.png", true, null, null))));
			entry.setColor(231.0f/255.0f, 185.0f/255.0f, 145.0f/255.0f, 1.0f);
			
			entry.addListener(new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) 
				{
					if (button == Buttons.RIGHT)
					{
						fillRight(emptySData, emptySData);
						fillLeftSlots();
					}
					else if (button == Buttons.LEFT)
					{
						eData.equip(slot, e);
						eData.doGraphicsUpdates();
						fillRight(emptySData, emptySData);
						fillLeftSlots();
					}
					return false;
				}
				public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) 
				{
					StatusData sd = eData.getEquipment(slot) != null ? eData.getEquipment(slot).statusModifier : emptySData ;
					fillRight(sd, e.statusModifier);
					entry.setColor(211.0f/255.0f, 165.0f/255.0f, 125.0f/255.0f, 1.0f);
				}
				public void exit (InputEvent event, float x, float y, int pointer, Actor fromActor) 
				{
					entry.setColor(231.0f/255.0f, 185.0f/255.0f, 145.0f/255.0f, 1.0f);
				}
			});
			
			scroll.add(entry).fillX().expandX().padBottom(10);
			scroll.row();
		}
	}

	public void fillRight(StatusData remove, StatusData add)
	{
		BitmapFont font = FileUtils.getFont("data/skins/arial.ttf", (int)GLOBALS.sclX(15), false);
		
		LabelStyle dls = new LabelStyle();
		dls.font = font;
		dls.fontColor = Color.BLACK;
		
		LabelStyle pls = new LabelStyle();
		pls.font = font;
		pls.fontColor = Color.GREEN;
		
		LabelStyle nls = new LabelStyle();
		nls.font = font;
		nls.fontColor = Color.RED;
		
		LabelStyle ls = null;
		
		right.clear();
		
		right.defaults().left().expandX().fillX().padLeft((int)GLOBALS.sclX(100)).padRight((int)GLOBALS.sclX(50)).padBottom((int)GLOBALS.sclX(5));
		
		for (STATS statistic : STATS.values())
		{
			Table statt = new Table();
			statt.debug();
			int stat = ( ( sData.stats.get(statistic) - remove.stats.get(statistic) ) + add.stats.get(statistic) ) - sData.stats.get(statistic);
			String stats = "";
			if (stat > 0) 
			{
				stats = stats+"+"+stat;
				ls = pls;
			}
			else if (stat < 0) 
			{
				stats = stats+""+stat;
				ls = nls;
			}
			else
			{
				ls = dls;
			}
			statt.add(new Label(statistic+":", dls));
			statt.add(new Table()).expandX().fillX();
			statt.add(new Label(sData.stats.get(statistic)+"", dls)).width((int)GLOBALS.sclX(50));
			statt.add(new Label(""+stats, ls)).width((int)GLOBALS.sclX(50));
			
			right.add(statt);
			right.row();
		}
		
		right.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true, null, null)));
		right.row();
		
		for (ELEMENTS element : ELEMENTS.values())
		{
			Table statt = new Table();
			statt.debug();			
			int stat = ( ( sData.attack.get(element)  - remove.attack.get(element) ) + add.attack.get(element) ) - sData.attack.get(element);
			String stats = "";
			if (stat > 0) 
			{
				stats = stats+"+"+stat;
				ls = pls;
			}
			else if (stat < 0) 
			{
				stats = stats+""+stat;
				ls = nls;
			}
			else
			{
				ls = dls;
			}

			statt.add(new Label("ATK "+element+":", dls));
			statt.add(new Table()).expandX().fillX();
			statt.add(new Label(sData.attack.get(element)+"", dls)).width((int)GLOBALS.sclX(50));
			statt.add(new Label(""+stats, ls)).width((int)GLOBALS.sclX(50));
			
			right.add(statt);
			right.row();
		}
		
		right.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true, null, null)));
		right.row();
		
		for (ELEMENTS element : ELEMENTS.values())
		{
			Table statt = new Table();
			statt.debug();
			int stat = ( ( sData.defense.get(element)  - remove.defense.get(element) ) + add.defense.get(element) ) - sData.defense.get(element);
			String stats = "";
			if (stat > 0) 
			{
				stats = stats+"+"+stat;
				ls = pls;
			}
			else if (stat < 0) 
			{
				stats = stats+""+stat;
				ls = nls;
			}
			else
			{
				ls = dls;
			}
			statt.add(new Label("DEF "+element+":", dls));
			statt.add(new Table()).expandX().fillX();
			statt.add(new Label(sData.defense.get(element)+"", dls)).width((int)GLOBALS.sclX(50));
			statt.add(new Label(""+stats, ls)).width((int)GLOBALS.sclX(50));
			
			right.add(statt);
			right.row();
		}
	}
	
	private Image getEquipSlot(final EquipmentData eData, final Equipment_Slot slot, final String fallback)
	{
		Image image = null;
		
		final Equipment<?> e = eData.getEquipment(slot);

		if (e == null || e.description == null || e.description.icon == null)
		{
			image = new Image(FileUtils.loadTexture(fallback, true, null, null));
		}
		else
		{
			image = new Image(FileUtils.loadTexture(e.description.icon, true, null, null));
		}
		
		final Image i = image;
		image.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (button == Buttons.RIGHT)
				{
					eData.equip(slot, null);
					eData.doGraphicsUpdates();
					fillLeftSlots();
					fillRight(emptySData, emptySData);
				}
				else if (button == Buttons.LEFT)
				{
					fillLeftItemsSimple(slot);
				}
				
				return false;
			}
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				i.setColor(0.7f, 0.7f, 0.7f, 0.7f);
				if (e != null)
				{
					bl.clear();
					bl.add(e.getDescriptionSimple()).expand().fill();
				}
			}
			public void exit (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				i.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
		});

		return image;
	}
	
	@Override
	public void drawSkybox(float delta)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void queueRenderables(float delta, HashMap<Class, Batch> batches)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch)
	{
		Table.drawDebug(stage);
	}

	float updateCD = 0;
	@Override
	public void update(float delta)
	{
		updateCD += delta;
		
		if (updateCD > 0.5f)
		{
			updateCD = 0;
			create();
		}
	}

	@Override
	public void superDispose()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resized(int width, int height)
	{
		// TODO Auto-generated method stub
		
	}
	
}
