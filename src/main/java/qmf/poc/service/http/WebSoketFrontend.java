package qmf.poc.service.http;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.jsonrpc.codec.JsonRPCCodec;
import qmf.poc.service.jsonrpc.codec.JsonRPCEncodeError;
import qmf.poc.service.jsonrpc.messages.JsonRPCBroadcast;

import java.util.List;

import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_LIST_MODIFIED;

public class WebSoketFrontend {
    public static void upgraded(Vertx vertx, HttpServerRequest req, Logger log) {
        req.toWebSocket()
                .onSuccess(webSocket -> {

                    final EventBus eb = vertx.eventBus();
                    final MessageConsumer<List<Agent>> consumerAgentsModified =
                            eb.consumer(AGENT_LIST_MODIFIED, (message) -> {
                                final List<Agent> agents = message.body();
                                final JsonRPCBroadcast broadcast = new JsonRPCBroadcast("agent_modified", agents);
                                try {
                                    webSocket.writeTextMessage(JsonRPCCodec.encode(broadcast));
                                } catch (JsonRPCEncodeError e) {
                                    throw new RuntimeException(e);
                                }
                            });

                    webSocket.closeHandler(v -> consumerAgentsModified.unregister());
                })
                .onFailure(err -> log.error("WebSocket upgrade failed", err));
    }
}
