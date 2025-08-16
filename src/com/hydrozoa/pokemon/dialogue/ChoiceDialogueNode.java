package com.hydrozoa.pokemon.dialogue;

import java.util.ArrayList;
import java.util.List;

// might need to extends !!
public class ChoiceDialogueNode implements DialogueNode {

	private String text;
	private String[] labels;
	private int[] nodeIds;

	public ChoiceDialogueNode(String text, String[] labels, int[] nodeIds) {
		this.text = text;
		this.labels = labels;
		this.nodeIds = nodeIds;
	}

	public String getText() {
		return text;
	}

	public String[] getLabels() {
		return labels;
	}

	public int[] getNodeIds() {
		return nodeIds;
	}

	@Override
	public int getID() {
		return 0;
	}

	@Override
	public List<Integer> getPointers() {
		return List.of();
	}
}

//public class ChoiceDialogueNode implements DialogueNode {
//
//	private String text;
//	private int id;
//
//	private List<Integer> pointers = new ArrayList<Integer>();
//	private List<String> labels = new ArrayList<String>();
//
//	public ChoiceDialogueNode(String text, int id) {
//		this.text = text;
//		this.id = id;
//	}
//
//	public void addChoice(String text, int targetId) {
//		pointers.add(targetId);
//		labels.add(text);
//	}
//
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
//		return pointers;
//	}
//
//	public List<String> getLabels() {
//		return labels;
//	}
//
//}
