package com.Lyeeedar.Screens;

import java.util.HashMap;

import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.Equipment_Slot;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Items.Armour;
import com.Lyeeedar.Entities.Items.Equipment;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Graphics.Batchers.Batch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.TreeStyle;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class InventoryScreen extends AbstractScreen {

	int selected = 0;

	private final PositionalData oldpData = new PositionalData();

	private final PositionalData pData = new PositionalData();
	private final PositionalData camData = new PositionalData();
	private final EquipmentData eData = new EquipmentData();

	Table table = new Table();

	Table left = new Table();
	Table tl = new Table();
	Table bl = new Table();

	Table right = new Table();
	Table tr = new Table();
	Table br = new Table();

	Table rarmour = new Table();
	Table rbuffs = new Table();
	Table rattacks = new Table();

	LabelStyle ls;
	TreeStyle ts;
	TextButtonStyle tbs;
	
	HashMap<Equipment_Slot, Object[]> equipped = new HashMap<Equipment_Slot, Object[]>();

	public InventoryScreen(PirateGame game)
	{
		super(game);
		testItems();
	}


	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
		//GLOBALS.player.readData(oldpData, PositionalData.class);
		//GLOBALS.player.writeData(pData, PositionalData.class);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		//GLOBALS.player.writeData(oldpData, PositionalData.class);

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	public void testItems()
	{
		for (int i = 0; i < 100; i++)
		{
			for (ITEM_TYPE it : ITEM_TYPE.values())
			{
				Item item = new Armour(null, new DESCRIPTION(""+it+" "+i, "Bla bla bla bla bla cheesy cakes nomn omn omn lol wtf is this wauygasbkmnasbfkjasbfkmbasf sdm,bfmsdbfm sdmb fmsdbfmsdbmsdbmfsbm", "fs", it, "data/skins/HAND.png"));
				eData.addItem(item);
			}
		}
		eData.equip(Equipment_Slot.HEAD, new Armour(null, new DESCRIPTION("A HEAD", "THis is a head. It is a big head and it lo", "oks funny and that is like super cool and usj vjrrdu vskrd pir snd stifff lovely banans darn thay stifff well thatysd a gpppf point DOWEHY!", ITEM_TYPE.ARMOUR_HEAD, "data/skins/TORSO.png")));
	}

	@Override
	public void create() {
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(20), false);

		ts = new TreeStyle(new Skin(Gdx.files.internal("data/skins/uiskin.json")).get(TreeStyle.class));
		ls = new LabelStyle();
		ls.fontColor = Color.BLACK;
		ls.font = font;
		tbs = new TextButtonStyle();
		tbs.font = font;

		stage.clear();

		table = new Table();

		left = new Table();
		tl = new Table();
		bl = new Table();

		right = new Table();
		tr = new Table();
		br = new Table();

		rarmour = new Table();
		rbuffs = new Table();
		rattacks = new Table();

		table.setFillParent(true);
		table.setBackground(new SpriteDrawable(new Sprite(FileUtils.loadTexture("data/textures/inventory.png", true, null, null))));

		table.add(left).expand().uniform().fill();
		table.add(right).expand().uniform().fill();

		buildLeft();
		buildRightSlots();


		stage.addActor(table);

		pData.position.set(6, 3, 0);
		camData.position.set(0, 0, 0);
		camData.rotation.set(0, 0, -1);

		((FollowCam)cam).setFollowDist(10);
	}

	public void buildRightDesc(DESCRIPTION desc)
	{
		br.clear();
		br.add(getDesc(desc)).expand().fill().top();
	}

	public void buildRightSlots()
	{
		right.clear();
		tr.clear();
		rarmour.clear();
		rbuffs.clear();
		rattacks.clear();

		Image head = getEquipSlot(eData, Equipment_Slot.HEAD, "data/skins/HEAD.png");
		Image torso = getEquipSlot(eData, Equipment_Slot.TORSO, "data/skins/TORSO.png");
		Image legs = getEquipSlot(eData, Equipment_Slot.LEGS, "data/skins/FEET.png");
		Image feet = getEquipSlot(eData, Equipment_Slot.FEET, "data/skins/FEET.png");

		Image larm = getEquipSlot(eData, Equipment_Slot.LARM, "data/skins/HEAD.png");
		Image rarm = getEquipSlot(eData, Equipment_Slot.RARM, "data/skins/HEAD.png");

//		Image temp = getEquipSlot(eData, Equipment_Slot.TEMPERATURE, "data/skins/HEAD.png");
//		Image light = getEquipSlot(eData, Equipment_Slot.LIGHT, "data/skins/HEAD.png");
//		Image life = getEquipSlot(eData, Equipment_Slot.LIFE, "data/skins/HEAD.png");
//		Image gaia = getEquipSlot(eData, Equipment_Slot.GAIA, "data/skins/HEAD.png");
//		Image force = getEquipSlot(eData, Equipment_Slot.FORCE, "data/skins/HEAD.png");

		right.add(tr).expand().uniform().fill();
		right.row();
		right.add(br).expand().uniform().fill();

		tr.add(rarmour).padLeft(GLOBALS.sclX(134.5f)).padTop(GLOBALS.sclY(85));
		tr.add(new Table()).width(GLOBALS.sclX(160)).expand();
		tr.add(rbuffs).right().padRight(GLOBALS.sclX(117)).padTop(GLOBALS.sclY(125));
		tr.row();
		tr.add(rattacks).colspan(3).padBottom(GLOBALS.sclY(37)).padLeft(GLOBALS.sclX(12));

		rarmour.defaults().width(GLOBALS.sclX(30)).height(GLOBALS.sclY(45)).pad(GLOBALS.sclY(27), GLOBALS.sclX(5), GLOBALS.sclY(5), 0).right();
		rarmour.add(head).padTop(GLOBALS.sclY(40));
		rarmour.row();
		rarmour.add(torso);
		rarmour.row();
		rarmour.add(legs);
		rarmour.row();
		rarmour.add(feet);

//		rbuffs.defaults().width(GLOBALS.sclX(30)).height(GLOBALS.sclY(45)).pad(GLOBALS.sclY(9), GLOBALS.sclX(5), GLOBALS.sclY(5), GLOBALS.sclX(5));
//		rbuffs.add(temp);
//		rbuffs.row();
//		rbuffs.add(light);
//		rbuffs.row();
//		rbuffs.add(life);
//		rbuffs.row();
//		rbuffs.add(gaia);
//		rbuffs.row();
//		rbuffs.add(force);

		rattacks.defaults().width(GLOBALS.sclX(30)).height(GLOBALS.sclY(45)).pad(0, GLOBALS.sclX(11), 0, GLOBALS.sclX(5)).left();
		rattacks.add(larm);
		rattacks.add(rarm);
	}

	public void buildLeft()
	{
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;
		
		left.clear();
		tl.clear();
		bl.clear();
		equipped.clear();

		final Tree tree = new Tree(ts);
		tree.setPadding(0);

		final Label btnArmour = new Label("Armour", ls);
		final Label btnBuffs = new Label("Buffs", ls);
		final Label btnWeapons = new Label("Weapons", ls);
		final Label btnMisc = new Label("Misc", ls);

		btnArmour.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 0;
				buildLeft();
				return false;
			}
		});

		btnWeapons.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 1;
				buildLeft();
				return false;
			}
		});

		btnBuffs.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 2;
				buildLeft();
				return false;
			}
		});

		btnMisc.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 3;
				buildLeft();
				return false;
			}
		});

		if (selected == 0) buildTreeArmour(tree, eData);
//		else if (selected == 1) buildTreeWeapon(tree, eData);
//		else if (selected == 2) buildTreeBuff(tree, eData);
		else if (selected == 3) buildTreeMisc(tree, eData);


		String cn = "";
		if (selected == 0) cn = "Armour";
		else if (selected == 1) cn = "Weapons";
		else if (selected == 2) cn = "Buffs";
		else if (selected == 3) cn = "Misc";
		Label current = new Label(cn, ls);

		left.add(tl).padTop(GLOBALS.sclY(70)).right().padRight(GLOBALS.sclX(90));
		left.row();
		left.add(current).padTop(GLOBALS.sclY(45)).left().padLeft(GLOBALS.sclX(180));
		left.row();
		left.add(bl).expand().fill().top().padLeft(GLOBALS.sclX(140)).padTop(GLOBALS.sclY(30)).padBottom(GLOBALS.sclY(133));

		tl.defaults().width(GLOBALS.sclX(50));
		if (selected == 0)
		{
			btnArmour.setVisible(false);
		}
		if (selected == 1)
		{
			btnWeapons.setVisible(false);
		}
		if (selected == 2)
		{
			btnBuffs.setVisible(false);
		}
		if (selected == 3)
		{
			btnMisc.setVisible(false);
		}
		
		tl.add(btnArmour);
		tl.row();
		tl.add(btnWeapons);
		tl.row();
		tl.add(btnBuffs);
		tl.row();
		tl.add(btnMisc);
		tl.row();

		Table scroll = new Table();
		ScrollPane scrollPane = new ScrollPane(scroll);

		bl.add(scrollPane).expand().fill().top().left().expand();
		scroll.add(tree).top().left().expand();		
	}

	private void buildTreeArmour(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Head", ITEM_TYPE.ARMOUR_HEAD, eData, Equipment_Slot.HEAD));
		tree.add(buildNode("Torso", ITEM_TYPE.ARMOUR_TORSO, eData, Equipment_Slot.TORSO));
		tree.add(buildNode("Legs", ITEM_TYPE.ARMOUR_LEGS, eData, Equipment_Slot.LEGS));
		tree.add(buildNode("Feet", ITEM_TYPE.ARMOUR_FEET, eData, Equipment_Slot.FEET));
	}

//	private void buildTreeBuff(Tree tree, EquipmentData eData)
//	{
//		tree.add(buildNode("Temperature", ITEM_TYPE.BUFF_TEMPERATURE, eData, Equipment_Slot.TEMPERATURE));
//		tree.add(buildNode("Light", ITEM_TYPE.BUFF_LIGHT, eData, Equipment_Slot.LIGHT));
//		tree.add(buildNode("Life", ITEM_TYPE.BUFF_LIFE, eData, Equipment_Slot.LIFE));
//		tree.add(buildNode("Gaia", ITEM_TYPE.BUFF_GAIA, eData, Equipment_Slot.GAIA));
//		tree.add(buildNode("Force", ITEM_TYPE.BUFF_FORCE, eData, Equipment_Slot.FORCE));
//	}
//
//	private void buildTreeWeapon(Tree tree, EquipmentData eData)
//	{
//		tree.add(buildNode("Mundane", ITEM_TYPE.WEAPON_MUNDANE, eData, Equipment_Slot.LARM));
//		tree.add(buildNode("Temperature", ITEM_TYPE.WEAPON_TEMPERATURE, eData, Equipment_Slot.LARM));
//		tree.add(buildNode("Light", ITEM_TYPE.WEAPON_LIGHT, eData, Equipment_Slot.LARM));
//		tree.add(buildNode("Life", ITEM_TYPE.WEAPON_LIFE, eData, Equipment_Slot.LARM));
//		tree.add(buildNode("Gaia", ITEM_TYPE.WEAPON_GAIA, eData, Equipment_Slot.LARM));
//		tree.add(buildNode("Force", ITEM_TYPE.WEAPON_FORCE, eData, Equipment_Slot.LARM));
//	}

	private void buildTreeMisc(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Misc", ITEM_TYPE.MISC, eData, null));
	}

	private Node buildNode(String title, ITEM_TYPE type, final EquipmentData eData, final Equipment_Slot slot)
	{
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;

		Node node = new Node(new Label(title, ls));
		Table stack = new Table();
		for (final Item i : eData.getItems(type))
		{
			final Table b = new Table();
			b.defaults().left().expandX();
			
			fillItemTable(b, i, slot);
			
			stack.add(b).expand().uniform().fill().width(GLOBALS.sclX(200));
			stack.row();
			stack.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true, null, null)));
			stack.row();
		}
		Node n = new Node(stack);
		n.setSelectable(false);
		node.add(n);
		node.setSelectable(false);
		return node;
	}
	
	private void fillItemTable(final Table b, final Item i, final Equipment_Slot slot)
	{
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(15), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;
		
		b.clearChildren();
		
		//b.add(new Image(i.description.icon)).size(50);
		if (slot != null && ((Equipment<?>)i).equipped != null){
			b.add(new Label("E ",ls));
			equipped.put(((Equipment<?>)i).equipped, new Object[]{b, i});
		}
		else
		{
			b.add(new Label("  ",ls));
		}
		b.add(new Label(i.description.name,ls));
		if (i.num > 1) b.add(new Label("("+i.num+")", ls));
		b.setBackground(new SpriteDrawable(new Sprite(FileUtils.loadTexture("data/textures/blank.png", true, null, null))));
		b.setColor(231.0f/255.0f, 185.0f/255.0f, 145.0f/255.0f, 1.0f);

		b.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (button == Buttons.RIGHT && slot != null)
				{
					Equipment<?> e = (Equipment<?>)i;
					eData.equip(slot, e);
					buildRightSlots();
					
					Object[] old = equipped.get(slot);
					equipped.put(slot, new Object[]{b, i});
					fillItemTable(b, i, slot);
					if (old != null)
					{
						fillItemTable((Table)old[0], (Item)old[1], slot);
					}
				}
				return false;
			}
			public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				buildRightDesc(i.description);
				b.setColor(211.0f/255.0f, 165.0f/255.0f, 125.0f/255.0f, 1.0f);
			}
			public void exit (InputEvent event, float x, float y, int pointer, Actor fromActor) {
				b.setColor(231.0f/255.0f, 185.0f/255.0f, 145.0f/255.0f, 1.0f);
				br.clear();
			}
		});
	}

	private Image getEquipSlot(final EquipmentData eData, final Equipment_Slot slot, String fallback)
	{
		Image image = null;
		
		final Equipment<?> e = eData.getEquipment(slot);

		if (e == null)
		{
			image = new Image(FileUtils.loadTexture(fallback, true, null, null));
		}
		else
		{
			image = new Image(FileUtils.loadTexture(e.description.icon, true, null, null));
			final Image i = image;
			image.addListener(new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					if (button == Buttons.RIGHT)
					{
						br.clear();
						eData.equip(e.equipped, null);
						eData.doGraphicsUpdates();
						buildRightSlots();
						
						Object[] old = equipped.get(slot);
						equipped.put(slot, null);
						if (old != null)
						{
							fillItemTable((Table)old[0], (Item)old[1], slot);
						}
					}
					return false;
				}
				public void enter (InputEvent event, float x, float y, int pointer, Actor fromActor) {
					buildRightDesc(e.description);
					i.setColor(0.7f, 0.7f, 0.7f, 0.7f);
				}
				public void exit (InputEvent event, float x, float y, int pointer, Actor fromActor) {
					i.setColor(1.0f, 1.0f, 1.0f, 1.0f);
					br.clear();
				}
			});
		}

		return image;
	}

	private Table getDesc(DESCRIPTION desc)
	{
		BitmapFont font = FileUtils.getFont("data/skins/parchment.ttf", (int)GLOBALS.sclX(10), false);
		LabelStyle ls = new LabelStyle();
		ls.font = font;
		ls.fontColor = Color.BLACK;

		Table table = new Table();

		table.add(new Label(desc.name, ls)).top().padRight(GLOBALS.sclX(100)).padTop(GLOBALS.sclY(55));
		table.row();
		Table main = new Table();

		main.add(new Image(FileUtils.loadTexture(desc.icon, true, null, null))).width(GLOBALS.sclX(100)).height(GLOBALS.sclY(150)).left().padRight(GLOBALS.sclX(10)).padLeft(GLOBALS.sclX(20));
		Label l = new Label(desc.longDesc, ls);
		l.setWrap(true);
		main.add(l).width(GLOBALS.sclX(180));

		table.add(main).expand().fill().padBottom(GLOBALS.sclY(100));



		return table;
	}

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		create();
	}

	@Override
	public void queueRenderables(float delta, HashMap<Class, Batch> batches) {		
		//GLOBALS.player.queueRenderables(cam, GLOBALS.LIGHTS, delta, modelBatch, decalBatch, trailBatch);
	}

	@Override
	public void drawOrthogonals(float delta, SpriteBatch batch) {		
		//batch.draw(tex, 0, 0, GLOBALS.SCREEN_SIZE[0], GLOBALS.SCREEN_SIZE[1]);
		Table.drawDebug(stage);

	}

	float cooldown = 0;
	@Override
	public void update(float delta) {
		((FollowCam)cam).updateBasic(camData);
		cooldown += delta;

		if (cooldown > 0.5f)
		{
			//create();
			cooldown = 0.0f;
		}
	}

	@Override
	public void superDispose() {
		// TODO Auto-generated method stub

	}


	@Override
	public void resized(int width, int height) {
		// TODO Auto-generated method stub
		
	}

}
