from datetime import datetime, timezone
from uuid import uuid4
from typing import Any, Dict
import json
import os
from dotenv import load_dotenv

from uagents import Context, Model, Protocol, Agent
from hyperon import MeTTa

from uagents_core.contrib.protocols.chat import (
    ChatAcknowledgement,
    ChatMessage,
    EndSessionContent,
    StartSessionContent,
    TextContent,
    chat_protocol_spec,
)

from npc_rag import DialogueGenerator
from knowledge import initialize_knowledge_graph
from utils import LLM, process_query

load_dotenv()

agent = Agent(
    name="Dialogue MeTTa Agent",
    port=8005,
    mailbox=True,
    publish_agent_details=True
)

class GameQuery(Model):
    query: str

def create_text_chat(text: str, dialogues: list = None, end_session: bool = False) -> ChatMessage:
    """Create a text chat message with optional dialogues."""
    content = [TextContent(type="text", text=text)]
    if dialogues:
        content.append(TextContent(type="dialogues", text=json.dumps(dialogues)))
    if end_session:
        content.append(EndSessionContent(type="end-session"))
    return ChatMessage(
        timestamp=datetime.now(timezone.utc),
        msg_id=uuid4(),
        content=content,
    )

metta = MeTTa()
initialize_knowledge_graph(metta)
generator = DialogueGenerator(metta)
llm = LLM(api_key=os.getenv("ASI_ONE_API_KEY"))

chat_proto = Protocol(spec=chat_protocol_spec)

@chat_proto.on_message(ChatMessage)
async def handle_message(ctx: Context, sender: str, msg: ChatMessage):
    ctx.storage.set(str(ctx.session), sender)
    await ctx.send(
        sender,
        ChatAcknowledgement(timestamp=datetime.now(timezone.utc), acknowledged_msg_id=msg.msg_id),
    )

    for item in msg.content:
        if isinstance(item, StartSessionContent):
            ctx.logger.info(f"Got a start session message from {sender}")
            continue
        elif isinstance(item, TextContent):
            user_query = item.text.strip()
            ctx.logger.info(f"Got a dialogue query from {sender}: {user_query}")

            try:
                response = process_query(user_query, generator, llm)
                title = response.get("selected_question", "Dialogue Generation")
                body = response.get("humanized_answer", "No response.")
                dialogues = response.get("dialogues", [])
                answer_text = f"**{title}**\n\n{body}"
                await ctx.send(sender, create_text_chat(answer_text, dialogues))
            except Exception as e:
                ctx.logger.error(f"Error processing dialogue query: {e}")
                await ctx.send(
                    sender,
                    create_text_chat(
                        "Error processing your request. Please try again."
                    )
                )
        else:
            ctx.logger.info(f"Got unexpected content from {sender}")

@chat_proto.on_message(ChatAcknowledgement)
async def handle_ack(ctx: Context, sender: str, msg: ChatAcknowledgement):
    ctx.logger.info(f"Got an acknowledgement from {sender} for {msg.acknowledged_msg_id}")

agent.include(chat_proto, publish_manifest=True)

if __name__ == "__main__":
    agent.run()
