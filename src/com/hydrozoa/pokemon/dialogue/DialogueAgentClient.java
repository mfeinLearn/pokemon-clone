
package com.hydrozoa.pokemon.dialogue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;

public class DialogueAgentClient {
    private static final String AGENT_ENDPOINT = "http://127.0.0.1:8005/dialogue";
    private final Gson gson = new Gson();

    public interface DialogueCallback {
        void onResponse(String dialogueText);
        void onError(String error);
    }

    public void fetchDialogue(String context, DialogueCallback callback) {
        Gdx.app.log("DialogueClient", "Sending request with context: " + context);
        HttpRequest request = new HttpRequestBuilder()
                .newRequest()
                .method("POST")
                .url(AGENT_ENDPOINT)
                .header("Content-Type", "application/json")
                .content(gson.toJson(new DialogueRequest(context)))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                String response = httpResponse.getResultAsString();
                Gdx.app.log("DialogueClient", "Received raw response: " + response);
                try {
                    DialogueJson dialogueJson = gson.fromJson(response, DialogueJson.class);
                    if (dialogueJson.text != null) {
                        callback.onResponse(dialogueJson.text);
                    } else {
                        callback.onError("No text field in response");
                    }
                } catch (Exception e) {
                    callback.onError("Failed to parse response: " + e.getMessage());
                }
            }

            @Override
            public void failed(Throwable t) {
                callback.onError("Request failed: " + t.getMessage());
            }

            @Override
            public void cancelled() {
                callback.onError("Request cancelled");
            }
        });
    }

    private static class DialogueRequest {
        String query;
        DialogueRequest(String query) {
            this.query = query;
        }
    }

    private static class DialogueJson {
        String text;
    }
}


// -- old

//package com.hydrozoa.pokemon.dialogue;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Net.HttpRequest;
//import com.badlogic.gdx.Net.HttpResponse;
//import com.badlogic.gdx.Net.HttpResponseListener;
//import com.badlogic.gdx.net.HttpRequestBuilder;
//import com.google.gson.Gson;
//
//public class DialogueAgentClient {
//    private static final String AGENT_ENDPOINT = "http://127.0.0.1:8005";
//    private final Gson gson = new Gson();
//
//    public interface DialogueCallback {
//        void onResponse(String dialogueText);
//        void onError(String error);
//    }
//
//    public void fetchDialogue(String context, DialogueAgentClient.DialogueCallback callback) {
//        HttpRequest request = new HttpRequestBuilder()
//                .newRequest()
//                .method("POST")
//                .url(AGENT_ENDPOINT)
//                .header("Content-Type", "application/json")
//                .content(gson.toJson(new DialogueRequest(context)))
//                .build();
//
//        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
//            @Override
//            public void handleHttpResponse(HttpResponse httpResponse) {
//                String response = httpResponse.getResultAsString();
//                try {
//                    DialogueJson dialogueJson = gson.fromJson(response, DialogueJson.class);
//                    callback.onResponse(dialogueJson.text);
//                } catch (Exception e) {
//                    callback.onError("Failed to parse response: " + e.getMessage());
//                }
//            }
//
//            @Override
//            public void failed(Throwable t) {
//                callback.onError("Request failed: " + t.getMessage());
//            }
//
//            @Override
//            public void cancelled() {
//                callback.onError("Request cancelled");
//            }
//        });
//    }
//
//    private static class DialogueRequest {
//        String query;
//        DialogueRequest(String query) {
//            this.query = query;
//        }
//    }
//
//    private static class DialogueJson {
//        String text;
//    }
//}



//
//  == really old!!
//package com.hydrozoa.pokemon.dialogue;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Net.HttpRequest;
//import com.badlogic.gdx.Net.HttpResponse;
//import com.badlogic.gdx.Net.HttpResponseListener;
//import com.badlogic.gdx.net.HttpRequestBuilder;
//import com.badlogic.gdx.utils.Json;
//import com.badlogic.gdx.utils.JsonReader;
//import com.badlogic.gdx.utils.JsonValue;
//import com.hydrozoa.pokemon.model.actor.Actor;
//
//public class DialogueAgentClient {
//    private static final String AGENT_URL = "http://localhost:8005/submit";
//
//    public static class DialogueJson {
//        public NodeJson[] nodes;
//
//        public static class NodeJson {
//            public int id;
//            public String text;
//            public String type;
//            public int pointer; // Used for linear nodes
//            public String[] labels; // Used for choice nodes
//            public int[] pointers; // Used for choice nodes
//        }
//    }
//
//    public interface DialogueCallback {
//        void onDialogueReceived(Actor actor, Dialogue dialogue);
//        void onError(Actor actor, String error);
//    }
//
//    public void requestDialogue(Actor actor, String biome, String timeOfDay, Integer playerLevel, DialogueCallback callback) {
//        String query = String.format(
//                "{\"action\":\"dialogue\",\"biome\":\"%s\",\"time_of_day\":%s,\"player_level\":%s}",
//                biome, timeOfDay != null ? "\"" + timeOfDay + "\"" : "null", playerLevel != null ? playerLevel : "null"
//        );
//
//        HttpRequest request = new HttpRequestBuilder()
//                .newRequest()
//                .method("POST")
//                .url(AGENT_URL)
//                .header("Content-Type", "application/json")
//                .content(query)
//                .build();
//
//        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
//            @Override
//            public void handleHttpResponse(HttpResponse httpResponse) {
//                String response = httpResponse.getResultAsString();
//                JsonValue json = new JsonReader().parse(response);
//                JsonValue content = json.get("content");
//                if (content != null) {
//                    for (JsonValue item : content) {
//                        if ("dialogues".equals(item.getString("type", null))) {
//                            JsonValue dialogues = new JsonReader().parse(item.getString("text"));
//                            for (JsonValue dialogue : dialogues) {
//                                if (dialogue.getString("npc", "").equals(actor.getName())) {
//                                    DialogueJson d = new Json().fromJson(DialogueJson.class, dialogue.get("dialogue").toString());
//                                    Dialogue dialogueObj = new Dialogue();
//                                    for (DialogueJson.NodeJson node : d.nodes) {
//                                        if ("linear".equals(node.type)) {
//                                            dialogueObj.addNode(new LinearDialogueNode(node.text, node.id, node.pointer));
//                                        } else if ("choice".equals(node.type)) {
//                                            dialogueObj.addNode(new ChoiceDialogueNode(node.text, node.labels, node.pointers, node.id));
//                                        }
//                                    }
//                                    callback.onDialogueReceived(actor, dialogueObj);
//                                    return;
//                                }
//                            }
//                        }
//                    }
//                }
//                callback.onError(actor, "No dialogue found for NPC: " + actor.getName());
//            }
//
//            @Override
//            public void failed(Throwable t) {
//                callback.onError(actor, "HTTP request failed: " + t.getMessage());
//            }
//
//            @Override
//            public void cancelled() {
//                callback.onError(actor, "HTTP request cancelled");
//            }
//        });
//    }
//}