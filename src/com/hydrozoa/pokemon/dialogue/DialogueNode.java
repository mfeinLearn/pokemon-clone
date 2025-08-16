//package com.hydrozoa.pokemon.dialogue;
//
//import java.util.List;
//
//public interface DialogueNode {
//
//	public int getID();
//
//	public List<Integer> getPointers();
//
//	public String getText();
//}

package com.hydrozoa.pokemon.dialogue;

public abstract class DialogueNode {

	public abstract String getText();

	public abstract int getID();

	public abstract int[] getPointers();
}