# metta/utils.py
import json
import re
from typing import Any, Dict, List, Optional
from npc_rag import DialogueGenerator, _sym

class LLM:
    """Lightweight stub for intent detection."""
    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key

    def classify_intent(self, text: str) -> str:
        t = text.lower()
        if "add" in t or "learn" in t or "rule" in t:
            return "add"
        return "dialogue"

def _parse_json_query(q: str) -> Optional[Dict[str, Any]]:
    try:
        return json.loads(q)
    except Exception:
        return None

def _parse_natural_dialogue(q: str) -> Optional[Dict[str, Any]]:
    """
    Parse natural language queries like:
      "dialogue for rival in town at day level 1"
      "dialogue in forest level 3"
    """
    t = q.strip().lower()
    if "dialogue" not in t:
        return None

    npc = None
    biome = None
    time_of_day = None
    level = None

    m = re.search(r"for\s+([a-z_]+)", t)
    if m:
        npc = m.group(1)

    m = re.search(r"in\s+([a-z_]+)", t)
    if m:
        biome = m.group(1)

    m = re.search(r"(?:at|during)\s+(day|night)", t)
    if m:
        time_of_day = m.group(1)

    m = re.search(r"level\s+(\d+)", t)
    if m:
        level = int(m.group(1))

    if not biome:
        return None

    return {
        "action": "dialogue",
        "npc": npc,
        "biome": biome,
        "time_of_day": time_of_day,
        "player_level": level,
    }

def _humanize_dialogue(req: Dict[str, Any], suggestions: List[Dict]) -> str:
    if not suggestions:
        biome = req.get("biome")
        return f"No dialogues found for biome '{biome}'.\n\nTip: add rules with JSON like:\n```json\n{{ \"action\": \"add\", \"relation\": \"dialogue_in_biome\", \"npc\": \"rival\", \"value\": \"town\" }}\n```"

    lines = []
    lines.append("Context:")
    ctx_bits = []
    for k in ["npc", "biome", "time_of_day", "player_level"]:
        v = req.get(k)
        if v is not None:
            ctx_bits.append(f"{k}={v}")
    lines.append("- " + ", ".join(ctx_bits) if ctx_bits else "- (none)")
    lines.append("")
    lines.append("Suggested Dialogues:")
    for item in suggestions:
        npc = item["npc"]
        dialogue = item["dialogue"]
        nodes = dialogue["nodes"]
        lines.append(f"- NPC: {npc}")
        for node in nodes:
            if node["type"] == "linear":
                lines.append(f"  - Linear (ID {node['id']}): {node['text']}")
            else:
                lines.append(f"  - Choice (ID {node['id']}): {node['text']}")
                for label, pointer in zip(node["labels"], node["pointers"]):
                    lines.append(f"    - Option: {label} -> Node {pointer}")
    return "\n".join(lines)

def process_query(query: str, generator: DialogueGenerator, llm: LLM):
    """
    Process queries for dialogue generation.
    Supports JSON and natural text like "dialogue for rival in town at day".
    """
    data = _parse_json_query(query) or _parse_natural_dialogue(query)

    if data is None:
        intent = llm.classify_intent(query)
        if intent == "add":
            return {
                "selected_question": "Add Rule",
                "humanized_answer": (
                    "To add knowledge, send JSON like:\n"
                    "```json\n{ \"action\": \"add\", \"relation\": \"dialogue_in_biome\", \"npc\": \"rival\", \"value\": \"town\" }\n```"
                ),
            }
        return {
            "selected_question": "Dialogue Help",
            "humanized_answer": (
                "Send a dialogue request. Examples:\n\n"
                "- Natural: `dialogue for rival in town at day level 1`\n"
                "- JSON:\n```json\n{\n"
                "  \"action\": \"dialogue\",\n"
                "  \"npc\": \"rival\",\n"
                "  \"biome\": \"town\",\n"
                "  \"time_of_day\": \"day\",\n"
                "  \"player_level\": 1\n"
                "}\n```"
            ),
        }

    action = data.get("action", "dialogue").lower()

    if action == "add":
        rel = data.get("relation")
        npc = data.get("npc")
        value = data.get("value")
        if rel is None or npc is None or value is None:
            return {
                "selected_question": "Add Rule",
                "humanized_answer": "Missing fields. Required: relation, npc, value.",
            }
        if isinstance(value, str) and value.isdigit():
            value = int(value)
        result = generator.add_knowledge(rel, npc, value)
        return {
            "selected_question": "Add Rule",
            "humanized_answer": result,
        }

    biome = data.get("biome")
    if not biome:
        return {
            "selected_question": "Dialogue",
            "humanized_answer": "A biome is required. Example: `dialogue in town`",
        }

    npc = data.get("npc")
    time_of_day = data.get("time_of_day")
    player_level = data.get("player_level")
    if isinstance(player_level, str) and player_level.isdigit():
        player_level = int(player_level)

    if npc:
        dialogue = generator.generate_dialogue(npc, biome, time_of_day, player_level)
        suggestions = [{"npc": npc, "dialogue": dialogue}] if dialogue["nodes"] else []
    else:
        suggestions = generator.suggest_dialogue(biome, time_of_day, player_level)

    human = _humanize_dialogue(
        {
            "npc": npc,
            "biome": biome,
            "time_of_day": time_of_day,
            "player_level": player_level,
        },
        suggestions
    )

    return {
        "selected_question": "Dialogue Generation",
        "humanized_answer": human,
        "dialogues": suggestions
    }