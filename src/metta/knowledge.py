# metta/knowledge.py
from hyperon import MeTTa, E, S, ValueAtom

def _S(x: str) -> S:
    return S(x.lower().replace(" ", "_").replace("-", "_"))

def initialize_knowledge_graph(metta: MeTTa):
    """
    Initialize the MeTTa knowledge graph with NPC dialogue knowledge.

    Relations used (all lowercase, underscore-separated):
      (dialogue_in_biome <npc> <biome>)          ; e.g., forest, town
      (dialogue_time <npc> <time_of_day>)        ; day, night
      (dialogue_player_level <npc> <int>)        ; minimum player level
      (dialogue_theme <npc> <theme>)             ; e.g., greeting, quest, advice
      (dialogue_text <npc> <theme> <text>)       ; actual dialogue text
      (dialogue_choice <npc> <theme> <label> <next_theme>) ; choice options
    """

    def add(rel: str, a: str, b):
        if isinstance(b, int):
            metta.space().add_atom(E(_S(rel), _S(a), ValueAtom(b)))
        else:
            metta.space().add_atom(E(_S(rel), _S(a), _S(b)))

    def add_text(rel: str, npc: str, theme: str, text: str):
        metta.space().add_atom(E(_S(rel), _S(npc), _S(theme), ValueAtom(text)))

    def add_choice(rel: str, npc: str, theme: str, label: str, next_theme: str):
        metta.space().add_atom(E(_S(rel), _S(npc), _S(theme), ValueAtom(label), _S(next_theme)))

    # --- NPC: Rival ---
    add("dialogue_in_biome", "rival", "town")
    add("dialogue_time", "rival", "day")
    add("dialogue_player_level", "rival", 1)
    add("dialogue_theme", "rival", "greeting")
    add_text("dialogue_text", "rival", "greeting", "Hey, trainer! Ready to test your skills?")
    add("dialogue_theme", "rival", "challenge")
    add_text("dialogue_text", "rival", "challenge", "Want to battle or train some more?")
    add_choice("dialogue_choice", "rival", "challenge", "Battle", "battle_response")
    add_choice("dialogue_choice", "rival", "challenge", "Train", "train_response")
    add_choice("dialogue_choice", "rival", "challenge", "Later", "later_response")
    add("dialogue_theme", "rival", "battle_response")
    add_text("dialogue_text", "rival", "battle_response", "Let's do this!")
    add("dialogue_theme", "rival", "train_response")
    add_text("dialogue_text", "rival", "train_response", "Smart move. Come back stronger!")
    add("dialogue_theme", "rival", "later_response")
    add_text("dialogue_text", "rival", "later_response", "Alright, catch you later!")

    # --- NPC: Guide ---
    add("dialogue_in_biome", "guide", "forest")
    add("dialogue_time", "guide", "day")
    add("dialogue_player_level", "guide", 3)
    add("dialogue_theme", "guide", "welcome")
    add_text("dialogue_text", "guide", "welcome", "Welcome to the forest! Need tips?")
    add("dialogue_theme", "guide", "advice")
    add_text("dialogue_text", "guide", "advice", "What do you want to know about?")
    add_choice("dialogue_choice", "guide", "advice", "Pokémon", "pokemon_advice")
    add_choice("dialogue_choice", "guide", "advice", "Items", "items_advice")
    add("dialogue_theme", "guide", "pokemon_advice")
    add_text("dialogue_text", "guide", "pokemon_advice", "Grass-types are strong here!")
    add("dialogue_theme", "guide", "items_advice")
    add_text("dialogue_text", "guide", "items_advice", "Potions are key for long journeys.")