package qmf.poc.service.jsonrpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.jsonrpc.codec.JsonRPCCodec;
import qmf.poc.service.jsonrpc.codec.JsonRPCDecodeError;
import qmf.poc.service.jsonrpc.codec.JsonRPCEncodeError;
import qmf.poc.service.jsonrpc.messages.JsonRPCError;
import qmf.poc.service.jsonrpc.messages.JsonRPCMessage;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;
import qmf.poc.service.jsonrpc.messages.JsonRPCResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static qmf.poc.service.log.LogHelper.ellipse;

public class JsonRPCAgentsTransport {
    static class AgentNotFoundException extends Exception {
        final String agentId;

        public AgentNotFoundException(String agentId) {
            super("Agent not found: " + agentId);
            this.agentId = agentId;
        }
    }

    private final Map<String, AgentContext> transportByAgents = new ConcurrentHashMap<>();

    public void registerAgent(String agentId, Consumer<String> sender) {
        log.debug("register agent {} with sender {}", agentId, sender);
        final AgentContext context = new AgentContext(sender);
        transportByAgents.put(agentId, context);
    }

    public void unregisterAgent(String agentId) {
        log.debug("unregister agent {}", agentId);
        final AgentContext context = transportByAgents.remove(agentId);
        if (context != null) {
            context.jsonRPCRequestManager.close();
        }
    }

    public void handle(String agentId, String messageString) {
        log.debug("handle message {}", ellipse(messageString));
        final AgentContext context = transportByAgents.get(agentId);
        if (context == null) {
            log.error("agent {} not connected (handle)", agentId);
            return;
        }
        try {
            JsonRPCMessage message = JsonRPCCodec.decode(messageString);
            switch (message) {
                case JsonRPCResult result -> context.jsonRPCRequestManager.handleResult(result);
                case JsonRPCError error -> context.jsonRPCRequestManager.handleError(error);
                default -> log.warn("Unexpected message type: {}", message);
            }
        } catch (JsonRPCDecodeError e) {
            log.warn("Failed to decode JSON-RPC message: \"{}\"", messageString, e);
        }
    }

    public CompletionStage<Object> sendRequest(String agentId, JsonRPCRequest request) {
        final AgentContext context = transportByAgents.get(agentId);
        if (context == null) {
            log.error("agent {} not connected (sendRequest)", agentId);
            return CompletableFuture.failedFuture(new AgentNotFoundException(agentId));
        }

        final String rpcMessage;
        try {
            rpcMessage = JsonRPCCodec.encode(request);
        } catch (JsonRPCEncodeError e) {
            log.error("failed to encode request {} {}", request, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }

        final CompletionStage<Object> future = context.jsonRPCRequestManager.rememberRequest(request)
                .thenApply(result -> {
                    if (log.isTraceEnabled()) {
                        final String resultString = (result instanceof String) ? (String) result : result.toString();
                        log.trace("response: {} {}",
                                agentId,
                                ellipse(resultString));
                    }
                    return result;
                });
        log.trace("send encoded request: {} {}", agentId, ellipse(rpcMessage));
        context.sender.accept(rpcMessage);
        return future;
    }

    private static class AgentContext {
        final Consumer<String> sender;
        final JsonRPCRequestManager jsonRPCRequestManager = new JsonRPCRequestManager();

        public AgentContext(Consumer<String> sender) {
            this.sender = sender;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JsonRPCAgentsTransport.class);
}
