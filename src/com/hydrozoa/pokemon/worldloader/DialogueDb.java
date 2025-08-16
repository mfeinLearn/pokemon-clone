package com.hydrozoa.pokemon.worldloader;

import java.util.HashMap;

import com.hydrozoa.pokemon.dialogue.Dialogue;

/**
 * @author Hydrozoa
 */
public class DialogueDb {

	private HashMap<String, Dialogue> knownDialogue = new HashMap<String, Dialogue>();

	protected void addTerrain(String name, Dialogue dialogue) {
		knownDialogue.put(name, dialogue);
	}

	public Dialogue getDialogue(String name) {
		if (!knownDialogue.containsKey(name)) {
			throw new NullPointerException("Could not find Dialogue of name "+name);
		}
		return knownDialogue.get(name);
	}

}

//
//package com.hydrozoa.pokemon.worldloader;
//
//import com.badlogic.gdx.utils.ObjectMap;
//import com.hydrozoa.pokemon.dialogue.Dialogue;
//
//public class DialogueDb {
//
//	private ObjectMap<String, Dialogue> dialogues = new ObjectMap<>();
//
//	public void addTerrain(String name, Dialogue dialogue) {
//		dialogues.put(name, dialogue);
//	}
//
//	public Dialogue getDialogue(String name) {
//		return dialogues.get(name);
//	}
//}