//
//		package com.hydrozoa.pokemon.dialogue;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class LinearDialogueNode extends DialogueNode {
//
//	private String text;
//	private int id;
//	private int pointer; // Next node ID
//
//	public LinearDialogueNode(String text, int id, int pointer) {
//		this.text = text;
//		this.id = id;
//		this.pointer = pointer;
//	}
//
//	// Constructor for DialogueLoader compatibility
//	public LinearDialogueNode(String text, int id) {
//		this(text, id, -1); // Default pointer to -1 (no next node)
//	}
//
//	@Override
//	public String getText() {
//		return text;
//	}
//
//	@Override
//	public int getID() {
//		return id;
//	}
//
//	@Override
//	public List<Integer> getPointers() {
//		return Arrays.asList(pointer);
//	}
//}


package com.hydrozoa.pokemon.dialogue;

public class LinearDialogueNode extends DialogueNode {

	private String text;
	private int id;
	private int pointer; // Next node ID

	public LinearDialogueNode(String text, int id, int pointer) {
		this.text = text;
		this.id = id;
		this.pointer = pointer;
	}

	public LinearDialogueNode(String text, int id) {
		this(text, id, -1); // Default pointer to -1 (no next node)
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public int[] getPointers() {
		return new int[]{pointer};
	}
}