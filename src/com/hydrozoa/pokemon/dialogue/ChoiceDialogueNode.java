//package com.hydrozoa.pokemon.dialogue;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class ChoiceDialogueNode implements DialogueNode {
//
//	private String text;
//	private String[] labels;
//	private int[] nodeIds;
//	private int id;
//
//	public ChoiceDialogueNode(String text, String[] labels, int[] nodeIds, int id) {
//		this.text = text;
//		this.labels = labels;
//		this.nodeIds = nodeIds;
//		this.id = id;
//	}
//
//	@Override
//	public String getText() {
//		return text;
//	}
//
//	public String[] getLabels() {
//		return labels;
//	}
//
//	public int[] getNodeIds() {
//		return nodeIds;
//	}
//
//	@Override
//	public int getID() {
//		return id;
//	}
//
//	@Override
//	public List<Integer> getPointers() {
//		return Arrays.asList(Arrays.stream(nodeIds).boxed().toArray(Integer[]::new));
//	}
//}

package com.hydrozoa.pokemon.dialogue;

public class ChoiceDialogueNode extends DialogueNode {

	private String text;
	private String[] labels;
	private int[] pointers;
	private int id;

	public ChoiceDialogueNode(String text, String[] labels, int[] pointers, int id) {
		this.text = text;
		this.labels = labels;
		this.pointers = pointers;
		this.id = id;
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
		return pointers;
	}

	public String[] getLabels() {
		return labels;
	}
}