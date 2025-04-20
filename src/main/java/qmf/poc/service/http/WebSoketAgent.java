package qmf.poc.service.http;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.jsonrpc.JsonRPCException;
import qmf.poc.service.jsonrpc.JsonRPCRequestManager;
import qmf.poc.service.jsonrpc.codec.JsonRPCCodec;
import qmf.poc.service.jsonrpc.codec.JsonRPCDecodeError;
import qmf.poc.service.jsonrpc.codec.JsonRPCEncodeError;
import qmf.poc.service.jsonrpc.messages.JsonRPCError;
import qmf.poc.service.jsonrpc.messages.JsonRPCMessage;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;
import qmf.poc.service.jsonrpc.messages.JsonRPCResult;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_DISABLE;
import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_ENABLE;
import static qmf.poc.service.verticles.HttpServerVerticle.AGENT_PING;

public class WebSoketAgent {

    public static void upgraded(Vertx vertx, HttpServerRequest req) {
        req.toWebSocket()
                .onSuccess(webSocket -> {
                    final String agentId = getAgentId(req);
                    log.debug("Agent {} connected from {}", agentId, req.remoteAddress());
                    final AtomicReference<Buffer> accumulatorRef = new AtomicReference<>(Buffer.buffer());
                    final JsonRPCRequestManager jsonRPCRequestManager = new JsonRPCRequestManager();

                    final EventBus eb = vertx.eventBus();

                    // notify agent is alive
                    eb.publish(AGENT_ENABLE, agentId);

                    // create a bridge between the event bus and this WebSocket
                    final MessageConsumer<String> consumerPing = eb.consumer(AGENT_PING(agentId), (message) -> {
                        final String payload = message.body();
                        final long id = jsonRPCRequestManager.id();
                        final JsonRPCRequest request = new JsonRPCRequest(id, "ping", Map.of("payload", payload));
                        log.trace("eventbus consumed: {} {}", message.address(), message.body());
                        final String rpcMessage;
                        try {
                            rpcMessage = JsonRPCCodec.encode(request);
                        } catch (JsonRPCEncodeError e) {
                            log.error("Failed to encode JSON-RPC message", e);
                            message.fail(0, e.getMessage());
                            return;
                        }
                        jsonRPCRequestManager.rememberRequest(request)
                                .thenApply(result -> {
                                    log.trace("agent response: {} {}", agentId, result);
                                    return result;
                                })
                                .thenAccept(message::reply)
                                .exceptionally(ex -> {
                                    if (ex instanceof JsonRPCException jsonRPCException) {
                                        message.fail(jsonRPCException.error.code, jsonRPCException.getMessage());
                                    } else {
                                        message.fail(0, ex.getMessage());
                                    }
                                    return null;
                                });
                        webSocket.writeTextMessage(rpcMessage);
                        log.trace("agent sent: {} {}", agentId, rpcMessage);
                    });
                    log.trace("eventbus register consumer {}", consumerPing.address());

                    webSocket.frameHandler(frame -> {
                        if (frame.isText() || frame.isContinuation()) {
                            final Buffer accumulator = accumulatorRef.getAndAccumulate(frame.binaryData(), Buffer::appendBuffer);
                            if (frame.isFinal()) {
                                final String messageString = accumulator.toString();
                                accumulatorRef.set(Buffer.buffer());
                                try {
                                    JsonRPCMessage message = JsonRPCCodec.decode(messageString);
                                    switch (message) {
                                        case JsonRPCResult result -> jsonRPCRequestManager.handleResult(result);
                                        case JsonRPCError error -> jsonRPCRequestManager.handleError(error);
                                        default -> log.warn("Unexpected message type: {}", message);
                                    }
                                } catch (JsonRPCDecodeError e) {
                                    log.warn("Failed to decode JSON-RPC message: \"{}\"", messageString, e);
                                }
                            }
                        } else if (frame.isClose()) {
                            webSocket.close();
                        }
                    });

                    webSocket.closeHandler(v -> {
                        // notify agent has gone
                        eb.publish(AGENT_DISABLE, agentId);

                        jsonRPCRequestManager.close();
                        consumerPing.unregister();
                        log.trace("eventbus unregister consumer {}", consumerPing.address());
                    });
                })
                .onFailure(err -> log.error("WebSocket upgrade failed", err));
    }

    private static String getAgentId(HttpServerRequest req) {
        return req.getParam("agent") != null && !req.getParam("agent").isEmpty()
                ? req.getParam("agent")
                : "unknown-" + "-" + System.currentTimeMillis();
    }

    private static final Logger log = LoggerFactory.getLogger(WebSoketAgent.class);
}
