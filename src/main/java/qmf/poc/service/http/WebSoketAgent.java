package qmf.poc.service.http;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.jsonrpc.transport.JsonRPCAgentsTransport;
import qmf.poc.service.jsonrpc.transport.JsonRPCException;
import qmf.poc.service.jsonrpc.transport.JsonRPCRequestManager;
import qmf.poc.service.jsonrpc.codec.JsonRPCCodec;
import qmf.poc.service.jsonrpc.codec.JsonRPCEncodeError;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_DISABLE;
import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_ENABLE;

public class WebSoketAgent {

    public static void upgraded(Vertx vertx, HttpServerRequest req, JsonRPCAgentsTransport broker) {
        req.toWebSocket()
                .onSuccess(webSocket -> {
                    final String agentId = getAgentId(req);
                    log.debug("Agent {} connected from {}", agentId, req.remoteAddress());
                    final AtomicReference<Buffer> accumulatorRef = new AtomicReference<>(Buffer.buffer());

                    final EventBus eb = vertx.eventBus();
                    // notify agent is alive
                    eb.publish(AGENT_ENABLE, agentId);
                    // make this agent available for sending requests
                    broker.registerAgent(agentId, webSocket::writeTextMessage);
                    // Paranoid check
                    final AtomicBoolean closed = new AtomicBoolean(false);

                    webSocket.frameHandler(frame -> {
                        if (frame.isText() || frame.isContinuation()) {
                            final Buffer accumulator = accumulatorRef.getAndAccumulate(frame.binaryData(), Buffer::appendBuffer);
                            if (frame.isFinal()) {
                                final String messageString = accumulator.toString();
                                accumulatorRef.set(Buffer.buffer());
                                // handle incoming message
                                broker.handle(agentId, messageString);
                            }
                        } else if (frame.isClose()) {
                            webSocket.close();
                            // stop ASAP
                            if (closed.compareAndSet(false, true)) {
                                eb.publish(AGENT_DISABLE, agentId);
                                broker.unregisterAgent(agentId);
                            }
                        }
                    });

                    webSocket.closeHandler(v -> {
                        // stop on non-peer close
                        if (closed.compareAndSet(false, true)) {
                            eb.publish(AGENT_DISABLE, agentId);
                            broker.unregisterAgent(agentId);
                        }
                    });
                })
                .onFailure(err -> log.error("WebSocket upgrade failed", err));
    }

    private static String getAgentId(HttpServerRequest req) {
        return req.getParam("agent") != null && !req.getParam("agent").isEmpty()
                ? req.getParam("agent")
                : "unknown-" + "-" + System.currentTimeMillis();
    }

    private static <T> void eventBusJsonRPCBridge(JsonRPCRequest request, Message<T> message, String agentId, WebSocket webSocket, JsonRPCRequestManager jsonRPCRequestManager) {
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
    }

    private static final Logger log = LoggerFactory.getLogger(WebSoketAgent.class);
}
