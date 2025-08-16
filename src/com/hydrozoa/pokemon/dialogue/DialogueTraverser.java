//package com.hydrozoa.pokemon.dialogue;
//
//import java.util.List;
//
//
//public class DialogueTraverser {
//
//	private Dialogue dialogue;
//	private DialogueNode current;
//
//	public DialogueTraverser(Dialogue dialogue) {
//		this.dialogue = dialogue;
//		this.current = dialogue.getNode(0);
//	}
//
//	public DialogueNode getNode() {
//		return current;
//	}
//
//	public DialogueNode getNextNode(int choiceIndex) {
//		if (current == null) {
//			return null;
//		}
//
//		List<Integer> pointers = current.getPointers();
//		if (pointers == null || pointers.isEmpty()) {
//			return null;
//		}
//
//		int nextID;
//		if (current instanceof ChoiceDialogueNode) {
//			ChoiceDialogueNode choiceNode = (ChoiceDialogueNode) current;
//			nextID = choiceNode.getPointers()[choiceIndex];
//		} else {
//			nextID = pointers.get(0);
//		}
//
//		if (nextID < 0) {
//			return null;
//		}
//
//		current = dialogue.getNode(nextID);
//		System.out.println("Retrieving node ID: " + nextID + ", Node: " + (current != null ? current.getText() : "null"));
//		return current;
//	}
//}

package com.hydrozoa.pokemon.dialogue;

public class DialogueTraverser {

	private Dialogue dialogue;
	private DialogueNode current;

	public DialogueTraverser(Dialogue dialogue) {
		this.dialogue = dialogue;
		this.current = dialogue.getNode(0);
	}

	public DialogueNode getNode() {
		return current;
	}

	public DialogueNode getNextNode(int choiceIndex) {
		if (current == null) {
			return null;
		}

		int[] pointers = current.getPointers();
		if (pointers == null || pointers.length == 0) {
			return null;
		}

		int nextID;
		if (current instanceof ChoiceDialogueNode) {
			nextID = pointers[choiceIndex];
		} else {
			nextID = pointers[0];
		}

		if (nextID < 0) {
			return null;
		}

		current = dialogue.getNode(nextID);
		System.out.println("Retrieving node ID: " + nextID + ", Node: " + (current != null ? current.getText() : "null"));
		return current;
	}
}