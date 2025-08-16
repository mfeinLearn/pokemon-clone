package com.hydrozoa.pokemon.model.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.math.GridPoint2;
import com.hydrozoa.pokemon.model.DIRECTION;
import com.hydrozoa.pokemon.model.TileMap;
import com.hydrozoa.pokemon.model.actor.*;
import com.hydrozoa.pokemon.controller.DialogueController; // Import DialogueController


/**
 * Contains data about the game world, such as references to Actors, and WorldObjects.
 * Query the world from here.
 * 
 * @author hydrozoa
 */
public class World implements ActorObserver {
	
	/** Unique name used to refer to this world */
	private String name;
	private int safeX;		// fly destination
	private int safeY;
	
	private TileMap map;
	private List<Actor> actors;
	private HashMap<Actor, ActorBehavior> brains;
	private List<WorldObject> objects;
	private DialogueController dialogueController; // Add reference to DialogueController

	// Original constructor without DialogueController
	public World(String name, int width, int height, int safeX, int safeY) {
		this(name, width, height, safeX, safeY, null);
	}

	/**
	 * @param name		Name of the world for internal model
	 * @param width		Size of world in tiles
	 * @param height
	 * @param safeX		Coord player can stand on, fly to
	 * @param safeY
	 */
	public World(String name, int width, int height, int safeX, int safeY, DialogueController dialogueController) {
		this.name = name;
		this.map = new TileMap(width, height);
		this.safeX = safeX;
		this.safeY = safeY;
		this.dialogueController = dialogueController;
		actors = new ArrayList<Actor>();
		brains = new HashMap<Actor, ActorBehavior>();
		objects = new ArrayList<WorldObject>();
	}
	
	public void addActor(Actor a) {
		map.getTile(a.getX(), a.getY()).setActor(a);
		actors.add(a);
	}
	
	public void addActor(Actor a, ActorBehavior b) {
		addActor(a);
		brains.put(a, b);
	}
	
	public void addObject(WorldObject o) {
		for (GridPoint2 p : o.getTiles()) {
			//System.out.println("\t Adding tile: "+p.x+", "+p.y);
			map.getTile(o.getX()+p.x, o.getY()+p.y).setObject(o);
		}
		objects.add(o);
	}
	
	public void removeActor(Actor actor) {
		map.getTile(actor.getX(), actor.getY()).setActor(null);
		actors.remove(actor);
		if (brains.containsKey(actor)) {
			brains.remove(actor);
		}
	}

	// Method to set DialogueController if not passed in constructor
	public void setDialogueController(DialogueController dialogueController) {
		this.dialogueController = dialogueController;
	}

	public void update(float delta) {
		for (Actor a : actors) {
			if (brains.containsKey(a)) {
				brains.get(a).update(delta);
			}
			a.update(delta);
		}
		for (WorldObject o : objects) {
			o.update(delta);
		}
		// Check for facing actors after updates
		checkFacingActors();
	}

	private void checkFacingActors() {
		// Only proceed if no dialogue is active
		if (dialogueController.isDialogueActive()) {
			return;
		}

		// Find the player (assuming PlayerActor is a subclass of Actor)
		Actor player = null;
		for (Actor a : actors) {
			if (a instanceof PlayerActor) {
				player = a;
				break;
			}
		}

		if (player == null || player.getMovementState() != Actor.MOVEMENT_STATE.STILL) {
			return;
		}

		// Check each actor
		for (Actor npc : actors) {
			if (npc == player || npc.getMovementState() != Actor.MOVEMENT_STATE.STILL || npc.getDialogue() == null) {
				continue;
			}

			// Check if NPC is on an adjacent tile
			int dx = npc.getX() - player.getX();
			int dy = npc.getY() - player.getY();

			// Adjacent tiles are one unit away in one direction
			if ((Math.abs(dx) == 1 && dy == 0) || (Math.abs(dy) == 1 && dx == 0)) {
				// Check if they are facing each other
				DIRECTION playerFacing = player.getFacing();
				DIRECTION npcFacing = npc.getFacing();

				// Determine required NPC facing direction to face the player
				DIRECTION requiredNpcFacing = null;
				if (dx == 1 && dy == 0) { // NPC is to the right of player
					requiredNpcFacing = DIRECTION.WEST;
				} else if (dx == -1 && dy == 0) { // NPC is to the left
					requiredNpcFacing = DIRECTION.EAST;
				} else if (dy == 1 && dx == 0) { // NPC is above
					requiredNpcFacing = DIRECTION.SOUTH;
				} else if (dy == -1 && dx == 0) { // NPC is below
					requiredNpcFacing = DIRECTION.NORTH;
				}

				// Check if NPC is facing the player and player is facing the NPC
				if (npcFacing == requiredNpcFacing && playerFacing == DIRECTION.getOpposite(npcFacing)) {
					// Make NPC face the player (already should be, but ensures correctness)
					npc.refaceWithoutAnimation(requiredNpcFacing);
					// Start the dialogue
					dialogueController.startDialogue(npc.getDialogue());
					break; // Only start one dialogue at a time
				}
			}
		}
	}

	public TileMap getMap() {
		return map;
	}
	
	public List<Actor> getActors() {
		return actors;
	}
	
	public List<WorldObject> getWorldObjects() {
		return objects;
	}

	public String getName() {
		return name;
	}
	
	public int getSafeX() {
		return safeX;
	}
	
	public int getSafeY() {
		return safeY;
	}

	@Override
	public void actorMoved(Actor a, DIRECTION direction, int x, int y) {
		
	}

	@Override
	public void attemptedMove(Actor a, DIRECTION direction) {
		
	}

	@Override
	public void actorBeforeMoved(Actor a, DIRECTION direction) {
		
	}
}
