package com.hydrozoa.pokemon.dialogue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hydrozoa
 */
public class Dialogue {

	private Map<Integer, DialogueNode> nodes = new HashMap<Integer, DialogueNode>();

	public DialogueNode getNode(int id) {
		DialogueNode node = nodes.get(id);
		System.out.println("Retrieving node ID: " + id + ", Node: " + (node != null ? node.getText() : "null"));
		return node;
	}

	public void addNode(DialogueNode node) {
		nodes.put(node.getID(), node);
		System.out.println("Added node ID: " + node.getID() + ", Text: " + node.getText());
	}

	public int getStart() {
		return 0;
	}

	/**
	 * @return Number of nodes in this dialogue
	 */
	public int size() {
		return nodes.size();
	}
}