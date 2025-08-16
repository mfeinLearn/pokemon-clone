package com.hydrozoa.pokemon.controller;

import java.util.Queue;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.hydrozoa.pokemon.battle.Battle;
import com.hydrozoa.pokemon.battle.event.BattleEvent;
import com.hydrozoa.pokemon.battle.event.TextEvent;
import com.hydrozoa.pokemon.battle.moves.MoveSpecification;
import com.hydrozoa.pokemon.ui.DialogueBox;
import com.hydrozoa.pokemon.ui.MoveSelectBox;
import com.hydrozoa.pokemon.ui.OptionBox;

/**
 * @author hydrozoa
 */
public class BattleScreenController extends InputAdapter {

	public enum STATE {
		USE_NEXT_POKEMON,   // Text displayed when Pokemon faints
		SELECT_ACTION,      // Moves, Items, Pokemon, Run
		DEACTIVATED,        // Do nothing, display nothing
		;
	}

	private STATE state = STATE.DEACTIVATED;

	private Queue<BattleEvent> queue;

	private Battle battle;

	private DialogueBox dialogue;
	private OptionBox optionBox;
	private MoveSelectBox moveSelect;

	public BattleScreenController(Battle battle, Queue<BattleEvent> queue, DialogueBox dialogue, MoveSelectBox options, OptionBox optionBox) {
		this.battle = battle;
		this.queue = queue;
		this.dialogue = dialogue;
		this.moveSelect = options;
		this.optionBox = optionBox;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (this.state == STATE.DEACTIVATED || dialogue.isVisible()) {
			System.out.println("BattleScreenController: Input ignored, state: " + state + ", dialogue visible: " + dialogue.isVisible());
			return false;
		}
		if (this.state == STATE.USE_NEXT_POKEMON && optionBox.isVisible()) {
			if (keycode == Keys.UP) {
				optionBox.selectPrevious();
				System.out.println("BattleScreenController: Moved up, selected index: " + optionBox.getSelectedIndex());
			} else if (keycode == Keys.DOWN) {
				optionBox.selectNext();
				System.out.println("BattleScreenController: Moved down, selected index: " + optionBox.getSelectedIndex());
			} else if (keycode == Keys.X) {
				if (optionBox.getSelectedIndex() == 0) { // YES selected
					// TODO: Replace with Pokémon selection UI (e.g., OptionBox listing non-fainted Pokémon)
					for (int i = 0; i < battle.getPlayerTrainer().getTeamSize(); i++) {
						if (!battle.getPlayerTrainer().getPokemon(i).isFainted()) {
							battle.chooseNewPokemon(battle.getPlayerTrainer().getPokemon(i));
							optionBox.setVisible(false);
							this.state = STATE.DEACTIVATED;
							System.out.println("BattleScreenController: Selected new Pokémon: " + battle.getPlayerPokemon().getName());
							break;
						}
					}
				} else if (optionBox.getSelectedIndex() == 1) { // NO selected
					battle.attemptRun();
					optionBox.setVisible(false);
					this.state = STATE.DEACTIVATED;
					System.out.println("BattleScreenController: Attempted to run");
				}
			}
		}
		if (moveSelect.isVisible()) {
			if (keycode == Keys.X) {
				int selection = moveSelect.getSelection();
				if (battle.getPlayerPokemon().getMove(selection) == null) {
					queue.add(new TextEvent("No such move...", 0.5f));
					System.out.println("BattleScreenController: Invalid move selected: " + selection);
				} else {
					battle.progress(moveSelect.getSelection());
					endTurn();
					System.out.println("BattleScreenController: Move selected: " + selection);
				}
			} else if (keycode == Keys.UP) {
				moveSelect.moveUp();
				System.out.println("MoveSelectBox: Moved up, selected index: " + moveSelect.getSelection());
				return true;
			} else if (keycode == Keys.DOWN) {
				moveSelect.moveDown();
				System.out.println("MoveSelectBox: Moved down, selected index: " + moveSelect.getSelection());
				return true;
			} else if (keycode == Keys.LEFT) {
				moveSelect.moveLeft();
				System.out.println("MoveSelectBox: Moved left, selected index: " + moveSelect.getSelection());
				return true;
			} else if (keycode == Keys.RIGHT) {
				moveSelect.moveRight();
				System.out.println("MoveSelectBox: Moved right, selected index: " + moveSelect.getSelection());
				return true;
			}
		}
		return false;
	}

	public STATE getState() {
		return state;
	}

	public void update(float delta) {
		if (isDisplayingNextDialogue() && dialogue.isFinished() && !optionBox.isVisible()) {
			optionBox.clear();
			optionBox.addOption("YES");
			optionBox.addOption("NO");
			optionBox.setVisible(true);
			System.out.println("BattleScreenController: Showing YES/NO prompt, options: " + optionBox.getAmount());
		}
	}

	/**
	 * Displays the UI for a new turn
	 */
	public void restartTurn() {
		this.state = STATE.SELECT_ACTION;
		dialogue.setVisible(false);
		for (int i = 0; i <= 3; i++) {
			String label = "------";
			MoveSpecification spec = battle.getPlayerPokemon().getMoveSpecification(i);
			if (spec != null) {
				label = spec.getName();
			}
			moveSelect.setLabel(i, label.toUpperCase());
		}
		moveSelect.setVisible(true);
		System.out.println("BattleScreenController: Restarted turn, state: SELECT_ACTION");
	}

	/**
	 * Displays UI for selecting a new Pokemon
	 */
	public void displayNextDialogue() {
		this.state = STATE.USE_NEXT_POKEMON;
		dialogue.setVisible(true);
		dialogue.animateText("Send out next pokemon?");
		System.out.println("BattleScreenController: Displaying next Pokémon prompt");
	}

	public boolean isDisplayingNextDialogue() {
		return this.state == STATE.USE_NEXT_POKEMON;
	}

	private void endTurn() {
		moveSelect.setVisible(false);
		this.state = STATE.DEACTIVATED;
		System.out.println("BattleScreenController: Turn ended, state: DEACTIVATED");
	}
}