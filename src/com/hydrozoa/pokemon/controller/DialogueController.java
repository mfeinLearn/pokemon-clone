//
//		package com.hydrozoa.pokemon.controller;
//
//import com.badlogic.gdx.Input.Keys;
//import com.badlogic.gdx.InputAdapter;
//import com.hydrozoa.pokemon.ui.DialogueBox;
//import com.hydrozoa.pokemon.ui.OptionBox;
//import com.hydrozoa.pokemon.dialogue.Dialogue;
//import com.hydrozoa.pokemon.dialogue.DialogueNode;
//import com.hydrozoa.pokemon.dialogue.LinearDialogueNode;
//import com.hydrozoa.pokemon.dialogue.ChoiceDialogueNode;
//import com.hydrozoa.pokemon.dialogue.DialogueTraverser;
//import com.hydrozoa.pokemon.screen.GameScreen;
//
//public class DialogueController extends InputAdapter {
//
//	private DialogueBox dialogueBox;
//	private OptionBox optionBox;
//	private DialogueTraverser traverser;
//	private GameScreen gameScreen;
//
//	public DialogueController(DialogueBox dialogueBox, OptionBox optionBox, GameScreen gameScreen) {
//		this.dialogueBox = dialogueBox;
//		this.optionBox = optionBox;
//		this.gameScreen = gameScreen;
//	}
//
//	public boolean isDialogueActive() {
//		return dialogueBox.isVisible();
//	}
//
//	public void startDialogue(Dialogue dialogue) {
//		System.out.println("Starting dialogue");
//		traverser = new DialogueTraverser(dialogue);
//		dialogueBox.setVisible(true);
//
//		DialogueNode nextNode = traverser.getNode();
//		if (nextNode == null) {
//			System.out.println("No starting node, closing dialogue");
//			dialogueBox.setVisible(false);
//			optionBox.setVisible(false);
//			return;
//		}
//		System.out.println("Displaying node ID: " + nextNode.getID() + ", Text: " + nextNode.getText());
//		dialogueBox.animateText(nextNode.getText());
//		if (nextNode instanceof ChoiceDialogueNode) {
//			ChoiceDialogueNode node = (ChoiceDialogueNode)nextNode;
//			optionBox.clear();
//			for (String s : node.getLabels()) {
//				optionBox.addOption(s);
//			}
//			optionBox.setVisible(true);
//			System.out.println("OptionBox options: " + optionBox.getAmount());
//		} else {
//			optionBox.setVisible(false);
//		}
//	}
//
//	public void update(float delta) {
//		if (isDialogueActive()) {
//			dialogueBox.act(delta); // Assuming Scene2D actor
//		}
//	}
//
//	@Override
//	public boolean keyUp(int keycode) {
//		if (isDialogueActive()) {
//			System.out.println("Key pressed: " + keycode + ", DialogueBox finished: " + dialogueBox.isFinished());
//			if (keycode == Keys.UP && traverser.getNode() instanceof ChoiceDialogueNode) {
//				optionBox.selectPrevious();
//				System.out.println("Moved up, selected index: " + optionBox.getSelectedIndex());
//				return true;
//			}
//			if (keycode == Keys.DOWN && traverser.getNode() instanceof ChoiceDialogueNode) {
//				optionBox.selectNext();
//				System.out.println("Moved down, selected index: " + optionBox.getSelectedIndex());
//				return true;
//			}
//			if (keycode == Keys.X && dialogueBox.isFinished()) {
//				DialogueNode currentNode = traverser.getNode();
//				int choiceIndex = -1;
//				if (currentNode instanceof ChoiceDialogueNode) {
//					choiceIndex = optionBox.getSelectedIndex();
//					System.out.println("ChoiceDialogueNode, selected index: " + choiceIndex);
//				}
//				DialogueNode nextNode = traverser.getNextNode(choiceIndex);
//				if (nextNode == null) {
//					System.out.println("No next node, closing dialogue");
//					dialogueBox.setVisible(false);
//					optionBox.setVisible(false);
//					traverser = null;
//					return true;
//				}
//				System.out.println("Displaying node ID: " + nextNode.getID() + ", Text: " + nextNode.getText());
//				dialogueBox.animateText(nextNode.getText());
//				if (nextNode instanceof ChoiceDialogueNode) {
//					ChoiceDialogueNode node = (ChoiceDialogueNode)nextNode;
//					optionBox.clear();
//					for (String s : node.getLabels()) {
//						optionBox.addOption(s);
//					}
//					optionBox.setVisible(true);
//					System.out.println("OptionBox options: " + optionBox.getAmount());
//				} else {
//					optionBox.setVisible(false);
//				}
//				return true;
//			}
//		}
//		return false;
//	}
//}


package com.hydrozoa.pokemon.controller;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.hydrozoa.pokemon.ui.DialogueBox;
import com.hydrozoa.pokemon.ui.OptionBox;
import com.hydrozoa.pokemon.dialogue.Dialogue;
import com.hydrozoa.pokemon.dialogue.DialogueNode;
import com.hydrozoa.pokemon.dialogue.LinearDialogueNode;
import com.hydrozoa.pokemon.dialogue.ChoiceDialogueNode;
import com.hydrozoa.pokemon.dialogue.DialogueTraverser;
import com.hydrozoa.pokemon.screen.GameScreen;

public class DialogueController extends InputAdapter {

	private DialogueBox dialogueBox;
	private OptionBox optionBox;
	private DialogueTraverser traverser;
	private GameScreen gameScreen;

	public DialogueController(DialogueBox dialogueBox, OptionBox optionBox, GameScreen gameScreen) {
		this.dialogueBox = dialogueBox;
		this.optionBox = optionBox;
		this.gameScreen = gameScreen;
	}

	public boolean isDialogueActive() {
		return dialogueBox.isVisible();
	}

	public void startDialogue(Dialogue dialogue) {
		System.out.println("Starting dialogue");
		traverser = new DialogueTraverser(dialogue);
		dialogueBox.setVisible(true);

		DialogueNode nextNode = traverser.getNode();
		if (nextNode == null) {
			System.out.println("No starting node, closing dialogue");
			dialogueBox.setVisible(false);
			optionBox.setVisible(false);
			return;
		}
		System.out.println("Displaying node ID: " + nextNode.getID() + ", Text: " + nextNode.getText());
		dialogueBox.animateText(nextNode.getText());
		if (nextNode instanceof ChoiceDialogueNode) {
			ChoiceDialogueNode node = (ChoiceDialogueNode)nextNode;
			optionBox.clear();
			for (String s : node.getLabels()) {
				optionBox.addOption(s);
			}
			optionBox.setVisible(true);
			System.out.println("OptionBox options: " + optionBox.getAmount());
		} else {
			optionBox.setVisible(false);
		}
	}

	public void update(float delta) {
		if (isDialogueActive()) {
			dialogueBox.act(delta); // Assuming Scene2D actor
		}
	}

	@Override
	public boolean keyUp(int keycode) {
		if (isDialogueActive()) {
			System.out.println("Key pressed: " + keycode + ", DialogueBox finished: " + dialogueBox.isFinished());
			if (keycode == Keys.UP && traverser.getNode() instanceof ChoiceDialogueNode) {
				optionBox.selectPrevious();
				System.out.println("Moved up, selected index: " + optionBox.getSelectedIndex());
				return true;
			}
			if (keycode == Keys.DOWN && traverser.getNode() instanceof ChoiceDialogueNode) {
				optionBox.selectNext();
				System.out.println("Moved down, selected index: " + optionBox.getSelectedIndex());
				return true;
			}
			if (keycode == Keys.X && dialogueBox.isFinished()) {
				DialogueNode currentNode = traverser.getNode();
				int choiceIndex = -1;
				if (currentNode instanceof ChoiceDialogueNode) {
					choiceIndex = optionBox.getSelectedIndex();
					System.out.println("ChoiceDialogueNode, selected index: " + choiceIndex);
				}
				DialogueNode nextNode = traverser.getNextNode(choiceIndex);
				if (nextNode == null) {
					System.out.println("No next node, closing dialogue");
					dialogueBox.setVisible(false);
					optionBox.setVisible(false);
					traverser = null;
					return true;
				}
				System.out.println("Displaying node ID: " + nextNode.getID() + ", Text: " + nextNode.getText());
				dialogueBox.animateText(nextNode.getText());
				if (nextNode instanceof ChoiceDialogueNode) {
					ChoiceDialogueNode node = (ChoiceDialogueNode)nextNode;
					optionBox.clear();
					for (String s : node.getLabels()) {
						optionBox.addOption(s);
					}
					optionBox.setVisible(true);
					System.out.println("OptionBox options: " + optionBox.getAmount());
				} else {
					optionBox.setVisible(false);
				}
				return true;
			}
		}
		return false;
	}
}