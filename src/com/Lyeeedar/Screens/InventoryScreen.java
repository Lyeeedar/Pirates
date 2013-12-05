package com.Lyeeedar.Screens;

import com.Lyeeedar.Entities.Entity.EquipmentData;
import com.Lyeeedar.Entities.Entity.PositionalData;
import com.Lyeeedar.Entities.Items.Item;
import com.Lyeeedar.Entities.Items.Item.DESCRIPTION;
import com.Lyeeedar.Entities.Items.Item.ITEM_TYPE;
import com.Lyeeedar.Graphics.MotionTrailBatch;
import com.Lyeeedar.Graphics.Renderers.AbstractModelBatch;
import com.Lyeeedar.Pirates.GLOBALS;
import com.Lyeeedar.Pirates.PirateGame;
import com.Lyeeedar.Util.FileUtils;
import com.Lyeeedar.Util.FollowCam;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
				Item item = new Item(new DESCRIPTION(""+it+" "+i, "Bla bla bla bla bla cheesy cakes nomn omn omn lol wtf is this wauygasbkmnasbfkjasbfkmbasf sdm,bfmsdbfm sdmb fmsdbfmsdbmsdbmfsbm", it, FileUtils.loadTexture("data/skins/HAND.png", true)));
				eData.addItem(item);
			}
		}
	}

	@Override
	public void create() {
		
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
		table.setBackground(new SpriteDrawable(new Sprite(FileUtils.loadTexture("data/textures/inventory.png", true))));
		
		table.add(left).expand().uniform().fill();
		table.add(right).expand().uniform().fill();
		
		// BUILD LEFT
		
		final Tree tree = new Tree(skin);
		tree.setPadding(0);
		
		final TextButton btnArmour = new TextButton("Armour", skin);
		final TextButton btnBuffs = new TextButton("Buffs", skin);
		final TextButton btnWeapons = new TextButton("Weapons", skin);
		final TextButton btnMisc = new TextButton("Misc", skin);
		
		btnArmour.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 0;
				create();
				return false;
			}
		});
		
		btnWeapons.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 1;
				create();
				return false;
			}
		});
		
		btnBuffs.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 2;
				create();
				return false;
			}
		});
		
		btnMisc.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				selected = 3;
				create();
				return false;
			}
		});
		
		//skin.getFont("default-font").setScale(sclX(0.8f), sclY(1.0f));
		
		if (selected == 0) buildTreeArmour(tree, eData);
		else if (selected == 1) buildTreeWeapon(tree, eData);
		else if (selected == 2) buildTreeBuff(tree, eData);
		else if (selected == 3) buildTreeMisc(tree, eData);
		
		
		String cn = "";
		if (selected == 0) cn = "Armour";
		else if (selected == 1) cn = "Weapons";
		else if (selected == 2) cn = "Buffs";
		else if (selected == 3) cn = "Misc";
		Label current = new Label(cn, skin);
		
		left.add(tl).padTop(sclY(100)).right().padRight(sclX(90));
		left.row();
		left.add(current).padTop(sclY(45)).left().padLeft(sclX(180));
		left.row();
		left.add(bl).expand().uniform().fill().top().padLeft(sclX(140)).padTop(sclY(30)).padBottom(sclY(133));
		
		tl.defaults().padTop(sclY(11)).width(sclX(50));
		if (selected != 0)
		{
			tl.add(btnArmour);
			tl.row();
		}
		if (selected != 1)
		{
			tl.add(btnWeapons);
			tl.row();
		}
		if (selected != 2)
		{
			tl.add(btnBuffs);
			tl.row();
		}
		if (selected != 3)
		{
			tl.add(btnMisc);
			tl.row();
		}
		
		Table scroll = new Table();
		ScrollPane scrollPane = new ScrollPane(scroll);
		
		bl.add(scrollPane).expand().fill().top().left().expand();
		scroll.add(tree).top().left().expand();
		
		// END LEFT
		
		// BUILD RIGHT
		
		Image head = new Image(FileUtils.loadTexture("data/skins/HEAD.png", true));
		Image torso = new Image(FileUtils.loadTexture("data/skins/TORSO.png", true));
		Image legs = new Image(FileUtils.loadTexture("data/skins/FEET.png", true));
		Image feet = new Image(FileUtils.loadTexture("data/skins/FEET.png", true));
		
		Image larm = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		Image rarm = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		
		Image temp = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		Image light = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		Image life = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		Image gaia = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		Image force = new Image(FileUtils.loadTexture("data/skins/HAND.png", true));
		
		right.add(tr).expand().uniform().fill();
		right.row();
		right.add(br).expand().uniform().fill();
		
		tr.add(rarmour).padLeft(sclX(134.5f)).padTop(sclY(85));
		tr.add(new Table()).width(sclX(160)).expand();
		tr.add(rbuffs).right().padRight(sclX(117)).padTop(sclY(125));
		tr.row();
		tr.add(rattacks).colspan(3).padBottom(sclY(37)).padLeft(sclX(12));
		
		rarmour.defaults().width(sclX(30)).height(sclY(45)).pad(sclY(27), sclX(5), sclY(5), 0).right();
		rarmour.add(head).padTop(sclY(40));
		rarmour.row();
		rarmour.add(torso);
		rarmour.row();
		rarmour.add(legs);
		rarmour.row();
		rarmour.add(feet);
		
		rbuffs.defaults().width(sclX(30)).height(sclY(45)).pad(sclY(9), sclX(5), sclY(5), sclX(5));
		rbuffs.add(temp);
		rbuffs.row();
		rbuffs.add(light);
		rbuffs.row();
		rbuffs.add(life);
		rbuffs.row();
		rbuffs.add(gaia);
		rbuffs.row();
		rbuffs.add(force);
		
		rattacks.defaults().width(sclX(30)).height(sclY(45)).pad(0, sclX(11), 0, sclX(5)).left();
		rattacks.add(larm);
		rattacks.add(rarm);
		
		br.add(getDesc(new DESCRIPTION("Iron Sword", "A sword made of dull grey iron. Spots of rust cover the metallic surface, and the slightly chipped edge gives the blade a malevolent serrated look.", ITEM_TYPE.WEAPON_MUNDANE, FileUtils.loadTexture("data/skins/HAND.png", true))))
			.expand().fill().top();
		// END RIGHT
		
		stage.addActor(table);
		
		pData.position.set(6, 3, 0);
		camData.position.set(0, 0, 0);
		camData.rotation.set(0, 0, -1);
		
		((FollowCam)cam).setFollowDist(10);
	}
	
	private void buildTreeArmour(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Head", ITEM_TYPE.ARMOUR_HEAD, eData));
		tree.add(buildNode("Torso", ITEM_TYPE.ARMOUR_TORSO, eData));
		tree.add(buildNode("Legs", ITEM_TYPE.ARMOUR_LEGS, eData));
		tree.add(buildNode("Feet", ITEM_TYPE.ARMOUR_FEET, eData));
	}
	
	private void buildTreeBuff(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Temperature", ITEM_TYPE.BUFF_TEMPERATURE, eData));
		tree.add(buildNode("Light", ITEM_TYPE.BUFF_LIGHT, eData));
		tree.add(buildNode("Life", ITEM_TYPE.BUFF_LIFE, eData));
		tree.add(buildNode("Gaia", ITEM_TYPE.BUFF_GAIA, eData));
		tree.add(buildNode("Force", ITEM_TYPE.BUFF_FORCE, eData));
	}
	
	private void buildTreeWeapon(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Mundane", ITEM_TYPE.WEAPON_MUNDANE, eData));
		tree.add(buildNode("Temperature", ITEM_TYPE.WEAPON_TEMPERATURE, eData));
		tree.add(buildNode("Light", ITEM_TYPE.WEAPON_LIGHT, eData));
		tree.add(buildNode("Life", ITEM_TYPE.WEAPON_LIFE, eData));
		tree.add(buildNode("Gaia", ITEM_TYPE.WEAPON_GAIA, eData));
		tree.add(buildNode("Force", ITEM_TYPE.WEAPON_FORCE, eData));
	}
	
	private void buildTreeMisc(Tree tree, EquipmentData eData)
	{
		tree.add(buildNode("Misc", ITEM_TYPE.MISC, eData));
	}
	
	private Node buildNode(String title, ITEM_TYPE type, EquipmentData eData)
	{
		Node node = new Node(new Label(title, skin));
		node.setExpanded(true);
		Table stack = new Table();
		for (final Item i : eData.getItems(type))
		{
			final Button b = new Button(skin);
			
			b.add(new Image(i.description.icon)).size(50);
			b.add(new Label(i.description.name, skin));
			
			b.addListener(new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					br.clear();
					br.add(getDesc(i.description));
					return false;
				}
			});
			stack.add(b).expandX().uniform().fill().width(sclX(200));
			stack.row();
			stack.add(new Image(FileUtils.loadTexture("data/textures/swipe.png", true)));
			stack.row();
		}
		Node n = new Node(stack);
		n.setSelectable(false);
		node.add(n);
		return node;
	}
	
	private Table getDesc(DESCRIPTION desc)
	{
		Table table = new Table();
		
		table.add(new Label(desc.name, skin)).top().padRight(sclX(100)).padTop(sclY(55));
		table.row();
		Table main = new Table();
		
		main.add(new Image(desc.icon)).width(sclX(100)).height(sclY(150)).left().padRight(sclX(10)).padLeft(sclX(20));
		Label l = new Label(desc.description, skin);
		l.setWrap(true);
		main.add(l).width(sclX(180));
		
		table.add(main).expand().fill().padBottom(sclY(100));
		
		
		
		return table;
	}
	
	public float sclX(float val)
	{
		float tmp = val/1000.0f;
		return tmp*((float)GLOBALS.RESOLUTION[0]);
	}
	
	public float sclY(float val)
	{
		float tmp = val/1000.0f;
		return tmp*((float)GLOBALS.RESOLUTION[1]);
	}
	
	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		create();
	}

	@Override
	public void drawSkybox(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void queueRenderables(float delta, AbstractModelBatch modelBatch,
			DecalBatch decalBatch, MotionTrailBatch trailBatch) {		
		//GLOBALS.player.queueRenderables(cam, GLOBALS.LIGHTS, delta, modelBatch, decalBatch, trailBatch);
	}

	@Override
	public void drawParticles(float delta) {
		// TODO Auto-generated method stub

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

}
