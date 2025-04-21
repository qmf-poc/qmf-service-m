package qmf.poc.service.http;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;
import qmf.poc.service.jsonrpc.transport.JsonRPCAgentsTransport;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class WebSoketAgent {

    public static void upgraded(@NotNull HttpServerRequest req, JsonRPCAgentsTransport jsonRpcTransport, AgentsRegistryMutable agentsRegistry) {
        req.toWebSocket()
                .onSuccess(webSocket -> {
                    final String agentId = getAgentId(req);
                    final String db = req.getParam("db");
                    log.debug("Agent {} connected from {}", agentId, req.remoteAddress());
                    final AtomicReference<Buffer> accumulatorRef = new AtomicReference<>(Buffer.buffer());

                    // notify agent is alive
                    agentsRegistry.enableAgent(agentId, db == null ? agentId: db);
                    // make this agent available for sending requests
                    jsonRpcTransport.registerAgent(agentId, webSocket::writeTextMessage);
                    // Paranoid check
                    final AtomicBoolean closed = new AtomicBoolean(false);

                    webSocket.frameHandler(frame -> {
                        if (frame.isText() || frame.isContinuation()) {
                            final Buffer accumulator = accumulatorRef.getAndAccumulate(frame.binaryData(), Buffer::appendBuffer);
                            if (frame.isFinal()) {
                                final String messageString = accumulator.toString();
                                accumulatorRef.set(Buffer.buffer());
                                // handle incoming message
                                jsonRpcTransport.handle(agentId, messageString);
                            }
                        } else if (frame.isClose()) {
                            webSocket.close();
                            // stop ASAP
                            if (closed.compareAndSet(false, true)) {
                                agentsRegistry.disableAgent(agentId);
                                jsonRpcTransport.unregisterAgent(agentId);
                            }
                        }
                    });

                    webSocket.closeHandler(v -> {
                        // stop on non-peer close
                        if (closed.compareAndSet(false, true)) {
                            agentsRegistry.disableAgent(agentId);
                            jsonRpcTransport.unregisterAgent(agentId);
                        }
                    });
                })
                .onFailure(err -> log.error("WebSocket upgrade failed", err));
    }

    private static String getAgentId(@NotNull HttpServerRequest req) {
        return req.getParam("agent") != null && !req.getParam("agent").isEmpty()
                ? req.getParam("agent")
                : "unknown-" + "-" + System.currentTimeMillis();
    }
    private static final Logger log = LoggerFactory.getLogger(WebSoketAgent.class);
}
