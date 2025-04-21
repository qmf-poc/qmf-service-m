package qmf.poc.service.http;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.jsonrpc.codec.JsonRPCCodec;
import qmf.poc.service.jsonrpc.codec.JsonRPCEncodeError;
import qmf.poc.service.jsonrpc.messages.JsonRPCBroadcast;

import java.util.List;
import java.util.function.Consumer;


public class WebSoketAPI {
    public static void upgraded(@NotNull HttpServerRequest req, AgentsRegistry registry, Logger log) {
        req.toWebSocket()
                .onSuccess(webSocket -> {

                    broadcastAgents(registry.agents(), webSocket, log);

                    final Consumer<List<Agent>> listener = agents -> broadcastAgents(agents, webSocket, log);
                    registry.addListener(listener);

                    webSocket.closeHandler(v -> registry.removeListener(listener));
                })
                .onFailure(err -> log.error("WebSocket upgrade failed", err));
    }

    private static void broadcastAgents(List<Agent> agents, @NotNull ServerWebSocket webSocket, Logger log) {
        try {
            final JsonRPCBroadcast broadcast = new JsonRPCBroadcast("agent_modified", agents);
            webSocket.writeTextMessage(JsonRPCCodec.encode(broadcast));
        } catch (JsonRPCEncodeError e) {
            log.error("Failed to send agent_modified broadcast", e);
        }
    }
}
