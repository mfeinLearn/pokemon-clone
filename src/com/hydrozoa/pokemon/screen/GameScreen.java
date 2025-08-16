package com.hydrozoa.pokemon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hydrozoa.pokemon.PokemonGame;
import com.hydrozoa.pokemon.battle.Battle;
import com.hydrozoa.pokemon.battle.event.BattleEvent;
import com.hydrozoa.pokemon.controller.ActorMovementController;
import com.hydrozoa.pokemon.controller.DialogueController;
import com.hydrozoa.pokemon.controller.InteractionController;
import com.hydrozoa.pokemon.controller.OptionBoxController;
import com.hydrozoa.pokemon.controller.BattleScreenController; // Updated import
import com.hydrozoa.pokemon.dialogue.ChoiceDialogueNode;
import com.hydrozoa.pokemon.dialogue.Dialogue;
import com.hydrozoa.pokemon.dialogue.LinearDialogueNode;
import com.hydrozoa.pokemon.model.Camera;
import com.hydrozoa.pokemon.model.DIRECTION;
import com.hydrozoa.pokemon.model.actor.Actor;
import com.hydrozoa.pokemon.model.actor.LimitedWalkingBehavior;
import com.hydrozoa.pokemon.model.actor.PlayerActor;
import com.hydrozoa.pokemon.model.world.World;
import com.hydrozoa.pokemon.model.world.cutscene.ActorWalkEvent;
import com.hydrozoa.pokemon.model.world.cutscene.CutsceneEvent;
import com.hydrozoa.pokemon.model.world.cutscene.CutscenePlayer;
import com.hydrozoa.pokemon.screen.renderer.EventQueueRenderer;
import com.hydrozoa.pokemon.screen.renderer.TileInfoRenderer;
import com.hydrozoa.pokemon.screen.renderer.WorldRenderer;
import com.hydrozoa.pokemon.screen.transition.FadeInTransition;
import com.hydrozoa.pokemon.screen.transition.FadeOutTransition;
import com.hydrozoa.pokemon.ui.DialogueBox;
import com.hydrozoa.pokemon.ui.MoveSelectBox;
import com.hydrozoa.pokemon.ui.OptionBox;
import com.hydrozoa.pokemon.util.Action;
import com.hydrozoa.pokemon.util.AnimationSet;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList; // Added for queue
import java.util.Queue;
import java.util.Random;

public class GameScreen extends AbstractScreen implements CutscenePlayer {

	private InputMultiplexer multiplexer;
	private DialogueController dialogueController;
	private ActorMovementController playerController;
	private InteractionController interactionController;
	private OptionBoxController debugController;

	private HashMap<String, World> worlds = new HashMap<String, World>();
	private World world;
	private PlayerActor player;
	private Camera camera;
	private Dialogue dialogue;

	private Queue<CutsceneEvent> eventQueue = new ArrayDeque<CutsceneEvent>();
	private CutsceneEvent currentEvent;

	private SpriteBatch batch;

	private Viewport gameViewport;

	private WorldRenderer worldRenderer;
	private EventQueueRenderer queueRenderer;
	private TileInfoRenderer tileInfoRenderer;
	private boolean renderTileInfo = false;

	private int uiScale = 2;

	private Stage uiStage;
	private Table dialogRoot;
	private Table menuRoot;
	private DialogueBox dialogueBox;
	private OptionBox optionsBox;
	private OptionBox debugBox;
	private BattleScreenController battleController; // Updated field

	public GameScreen(PokemonGame app) {
		super(app);
		gameViewport = new ScreenViewport();
		batch = new SpriteBatch();

		TextureAtlas atlas = app.getAssetManager().get("res/graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);

		AnimationSet playerAnimations = new AnimationSet(
				new Animation(0.4f/2f, atlas.findRegions("brendan_walk_north"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_walk_south"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_walk_east"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_walk_west"), PlayMode.LOOP_PINGPONG),
				atlas.findRegion("brendan_stand_north"),
				atlas.findRegion("brendan_stand_south"),
				atlas.findRegion("brendan_stand_east"),
				atlas.findRegion("brendan_stand_west")
		);
		playerAnimations.addBiking(
				new Animation(0.4f/2f, atlas.findRegions("brendan_bike_north"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_bike_south"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_bike_east"), PlayMode.LOOP_PINGPONG),
				new Animation(0.4f/2f, atlas.findRegions("brendan_bike_west"), PlayMode.LOOP_PINGPONG));
		playerAnimations.addRunning(
				new Animation(0.25f/2f, atlas.findRegions("brendan_run_north"), PlayMode.LOOP_PINGPONG),
				new Animation(0.25f/2f, atlas.findRegions("brendan_run_south"), PlayMode.LOOP_PINGPONG),
				new Animation(0.25f/2f, atlas.findRegions("brendan_run_east"), PlayMode.LOOP_PINGPONG),
				new Animation(0.25f/2f, atlas.findRegions("brendan_run_west"), PlayMode.LOOP_PINGPONG));

		initUI();

		dialogueController = new DialogueController(dialogueBox, optionsBox, this);

		Array<World> loadedWorlds = app.getAssetManager().getAll(World.class, new Array<World>());
		for (World w : loadedWorlds) {
			w.setDialogueController(dialogueController);
			worlds.put(w.getName(), w);
		}
		world = worlds.get("littleroot_town");

		camera = new Camera();
		player = new PlayerActor(world, world.getSafeX(), world.getSafeY(), playerAnimations, this);
		world.addActor(player);

		// Create NPC with same animations as player
		Actor npc = new Actor(world, world.getSafeX(), world.getSafeY() + 1, playerAnimations);
		Dialogue dialogue = new Dialogue();
		LinearDialogueNode node1 = new LinearDialogueNode("Hello, trainer! Ready to talk?", 0, 1);
		ChoiceDialogueNode node2 = new ChoiceDialogueNode("Want to battle?", new String[]{"Yes", "No", "Ok, I'll be back soon!"}, new int[]{2, 3, 4}, 1);
		LinearDialogueNode node3 = new LinearDialogueNode("Great! Let's battle!", 2, -1);
		LinearDialogueNode node4 = new LinearDialogueNode("Maybe next time.", 3, -1);
		LinearDialogueNode node5 = new LinearDialogueNode("Alright, see you soon!", 4, -1);
		dialogue.addNode(node1);
		dialogue.addNode(node2);
		dialogue.addNode(node3);
		dialogue.addNode(node4);
		dialogue.addNode(node5);
		npc.setDialogue(dialogue);
		npc.refaceWithoutAnimation(DIRECTION.SOUTH);
		world.addActor(npc, new LimitedWalkingBehavior(npc, 2, 2, 2, 2, 1f, 3f, new Random(), dialogueController));

		multiplexer = new InputMultiplexer();
		playerController = new ActorMovementController(player); // Fixed: single parameter
		interactionController = new InteractionController(player, dialogueController);
		debugController = new OptionBoxController(debugBox);
		debugController.addAction(new Action() {
			@Override
			public void action() {
				renderTileInfo = !renderTileInfo;
			}
		}, "Toggle show coords");

		multiplexer.addProcessor(0, debugController);
		multiplexer.addProcessor(1, dialogueController);
		multiplexer.addProcessor(2, playerController);
		multiplexer.addProcessor(3, interactionController);

		worldRenderer = new WorldRenderer(getApp().getAssetManager(), world);
		queueRenderer = new EventQueueRenderer(app.getSkin(), eventQueue);
		tileInfoRenderer = new TileInfoRenderer(world, camera);
	}

	private void initUI() {
		uiStage = new Stage(new ScreenViewport());
		uiStage.getViewport().update(Gdx.graphics.getWidth()/uiScale, Gdx.graphics.getHeight()/uiScale, true);

		dialogRoot = new Table();
		dialogRoot.setFillParent(true);
		uiStage.addActor(dialogRoot);

		dialogueBox = new DialogueBox(getApp().getSkin());
		dialogueBox.setVisible(false);

		optionsBox = new OptionBox(getApp().getSkin());
		optionsBox.setVisible(false);

		Table dialogTable = new Table();
		dialogTable.add(optionsBox)
				.expand()
				.align(Align.right)
				.space(8f)
				.row();
		dialogTable.add(dialogueBox)
				.expand()
				.align(Align.bottom)
				.space(8f)
				.row();

		dialogRoot.add(dialogTable).expand().align(Align.bottom);

		menuRoot = new Table();
		menuRoot.setFillParent(true);
		uiStage.addActor(menuRoot);

		debugBox = new OptionBox(getApp().getSkin());
		debugBox.setVisible(false);

		Table menuTable = new Table();
		menuTable.add(debugBox).expand().align(Align.top | Align.left);

		menuRoot.add(menuTable).expand().fill();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void update(float delta) {
		while (currentEvent == null || currentEvent.isFinished()) {
			if (eventQueue.peek() == null) {
				currentEvent = null;
				break;
			} else {
				currentEvent = eventQueue.poll();
				currentEvent.begin(this);
			}
		}

		if (currentEvent != null) {
			currentEvent.update(delta);
		}

		if (currentEvent == null) {
			playerController.update(delta);
		}

		dialogueController.update(delta);

		if (!dialogueBox.isVisible()) {
			camera.update(player.getWorldX()+0.5f, player.getWorldY()+0.5f);
			world.update(delta);
		}
		uiStage.act(delta);
	}

	@Override
	public void render(float delta) {
		gameViewport.apply();
		batch.begin();
		worldRenderer.render(batch, camera);
		queueRenderer.render(batch, currentEvent);
		if (renderTileInfo) {
			tileInfoRenderer.render(batch, Gdx.input.getX(), Gdx.input.getY());
		}
		batch.end();

		uiStage.draw();
	}

	@Override
	public void resize(int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		uiStage.getViewport().update(width/uiScale, height/uiScale, true);
		gameViewport.update(width, height);
	}

	@Override
	public void resume() {
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
		if (currentEvent != null) {
			currentEvent.screenShow();
		}
	}

	public void changeWorld(World newWorld, int x, int y, DIRECTION face) {
		player.changeWorld(newWorld, x, y);
		this.world = newWorld;
		player.refaceWithoutAnimation(face);
		this.worldRenderer.setWorld(newWorld);
		this.camera.update(player.getWorldX()+0.5f, player.getWorldY()+0.5f);
	}

	@Override
	public void changeLocation(World newWorld, int x, int y, DIRECTION facing, Color color) {
		getApp().startTransition(
				this,
				this,
				new FadeOutTransition(0.8f, color, getApp().getTweenManager(), getApp().getAssetManager()),
				new FadeInTransition(0.8f, color, getApp().getTweenManager(), getApp().getAssetManager()),
				new Action() {
					@Override
					public void action() {
						changeWorld(newWorld, x, y, facing);
					}
				});
	}

	@Override
	public World getWorld(String worldName) {
		return worlds.get(worldName);
	}

	@Override
	public void queueEvent(CutsceneEvent event) {
		eventQueue.add(event);
	}
}