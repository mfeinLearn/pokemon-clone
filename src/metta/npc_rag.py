# metta/npc_rag.py
from typing import Iterable, List, Optional, Set, Dict
from hyperon import MeTTa
import json

def _sym(s: str) -> str:
    """Normalize to a MeTTa-friendly symbol (lowercase, underscores)."""
    return s.strip().lower().replace(" ", "_").replace("-", "_")

class DialogueGenerator:
    """
    Query helper over a MeTTa space to generate dialogues for NPCs.

    Relations queried:
      (dialogue_in_biome <npc> <biome>)
      (dialogue_time <npc> <time_of_day>)
      (dialogue_player_level <npc> <int>)
      (dialogue_theme <npc> <theme>)
      (dialogue_text <npc> <theme> <text>)
      (dialogue_choice <npc> <theme> <label> <next_theme>)
    """

    def __init__(self, metta_instance: MeTTa):
        self.metta = metta_instance

    def _run(self, query_str: str):
        return self.metta.run(query_str)

    def _query_relation_values(self, relation: str, value: str) -> Set[str]:
        """Return NPCs that have (relation npc value)."""
        relation = _sym(relation)
        value = _sym(value)
        q = f'!(match &self ({relation} $npc {value}) $npc)'
        res = self._run(q)
        return {str(r[0]) for r in res if r and len(r) > 0} if res else set()

    def _query_numeric(self, relation: str, npc: str) -> List[int]:
        """Return numeric values for a relation (e.g., dialogue_player_level)."""
        relation = _sym(relation)
        npc = _sym(npc)
        q = f'!(match &self ({relation} {npc} $n) $n)'
        res = self._run(q)
        return [r[0].get_object().value for r in res if r and len(r) > 0] if res else []

    def _query_themes(self, npc: str) -> Set[str]:
        """Return themes for an NPC."""
        npc = _sym(npc)
        q = f'!(match &self (dialogue_theme {npc} $theme) $theme)'
        res = self._run(q)
        return {str(r[0]) for r in res if r and len(r) > 0} if res else set()

    def _query_text(self, npc: str, theme: str) -> List[str]:
        """Return text for an NPC's theme."""
        npc = _sym(npc)
        theme = _sym(theme)
        q = f'!(match &self (dialogue_text {npc} {theme} $text) $text)'
        res = self._run(q)
        return [r[0].get_object().value for r in res if r and len(r) > 0] if res else []

    def _query_choices(self, npc: str, theme: str) -> List[Dict[str, str]]:
        """Return choices for an NPC's theme as [label, next_theme]."""
        npc = _sym(npc)
        theme = _sym(theme)
        q = f'!(match &self (dialogue_choice {npc} {theme} $label $next) [$label $next])'
        res = self._run(q)
        return [{"label": str(r[0]), "next_theme": str(r[1])} for r in res if r and len(r) > 0] if res else []

    def _filter_with_optional_constraint(self, candidates: Set[str], relation: str, desired_value: Optional[str]) -> Set[str]:
        """Filter NPCs by optional relation (e.g., time_of_day)."""
        if not desired_value or not candidates:
            return candidates
        allowed = self._query_relation_values(relation, desired_value)
        return candidates.intersection(allowed)

    def _apply_level_filter(self, candidates: Set[str], player_level: Optional[int]) -> Set[str]:
        """Filter NPCs by player level."""
        if player_level is None or not candidates:
            return candidates
        kept = set()
        for npc in candidates:
            levels = self._query_numeric("dialogue_player_level", npc)
            min_level = levels[0] if levels else 1
            if player_level >= min_level:
                kept.add(npc)
        return kept

    def generate_dialogue(self, npc: str, biome: str, time_of_day: Optional[str] = None, player_level: Optional[int] = None) -> Dict:
        """
        Generate a Dialogue object as JSON for the given NPC and context.
        Returns a JSON-serializable dict compatible with com.hydrozoa.pokemon.dialogue.Dialogue.
        """
        npc = _sym(npc)
        candidates = {npc} if npc in self._query_relation_values("dialogue_in_biome", biome) else set()
        candidates = self._filter_with_optional_constraint(candidates, "dialogue_time", time_of_day)
        candidates = self._apply_level_filter(candidates, player_level)

        if not candidates:
            return {"nodes": []}

        themes = self._query_themes(npc)
        if not themes:
            return {"nodes": []}

        nodes = []
        node_id = 0
        theme_to_id = {}
        for theme in themes:
            texts = self._query_text(npc, theme)
            if not texts:
                continue
            text = texts[0]
            choices = self._query_choices(npc, theme)
            node = {"id": node_id, "text": text}
            if choices:
                node["type"] = "choice"
                node["labels"] = [c["label"] for c in choices]
                node["pointers"] = []
                for c in choices:
                    next_theme = c["next_theme"]
                    if next_theme not in theme_to_id:
                        theme_to_id[next_theme] = node_id + 1
                        nodes.append({"id": node_id + 1, "text": self._query_text(npc, next_theme)[0], "type": "linear", "pointer": -1})
                        node_id += 1
                    node["pointers"].append(theme_to_id[next_theme])
            else:
                node["type"] = "linear"
                node["pointer"] = -1
            nodes.append(node)
            theme_to_id[theme] = node_id
            node_id += 1

        return {"nodes": nodes}

    def suggest_dialogue(self, biome: str, time_of_day: Optional[str] = None, player_level: Optional[int] = None) -> List[Dict]:
        """
        Suggest NPCs with dialogues for the given context.
        Returns a list of {npc, dialogue} where dialogue is a JSON-serializable Dialogue.
        """
        candidates = self._query_relation_values("dialogue_in_biome", biome)
        candidates = self._filter_with_optional_constraint(candidates, "dialogue_time", time_of_day)
        candidates = self._apply_level_filter(candidates, player_level)

        results = []
        for npc in candidates:
            dialogue = self.generate_dialogue(npc, biome, time_of_day, player_level)
            if dialogue["nodes"]:
                results.append({"npc": npc, "dialogue": dialogue})
        return results[:5]