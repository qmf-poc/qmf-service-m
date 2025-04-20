package qmf.poc.service.jsonrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.jsonrpc.messages.JsonRPCError;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;
import qmf.poc.service.jsonrpc.messages.JsonRPCResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class JsonRPCRequestManager {
    private final Map<Long, Pending> pendings = new ConcurrentHashMap<>();

    public CompletionStage<Object> rememberRequest(JsonRPCRequest request) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendings.put(request.id, new Pending(future, request));
        log.trace("rememberRequest: id={} method={} params={}", request.id, request.method, request.params);
        return future;
    }

    public void handleResult(JsonRPCResult result) {
        log.trace("handleResult: id={} result={}", result.id, result.result);
        Pending pending = pendings.remove(result.id);

        if (pending == null) {
            log.warn("handle result without pending request: id={} result={}", result.id, result.result);
            return;
        }

        CompletableFuture<Object> future = pending.completable;
        future.complete(result.result);
    }

    public void handleError(JsonRPCError error) {
        Pending pending = pendings.remove(error.id);

        if (pending == null) {
            log.warn("Error without pending request: {}", error);
            return;
        }

        CompletableFuture<Object> completable = pending.completable;
        completable.completeExceptionally(new JsonRPCException(pending.request, error.error));
    }

    public void close() {
        pendings.forEach((i, c) -> {
            if (c != null) {
                c.completable.cancel(true);
            }
        });
    }

    public long id() {
        long id = System.currentTimeMillis();
        while (pendings.containsKey(id)) {
            id++;
        }
        return id;
    }

    static private final Logger log = LoggerFactory.getLogger(JsonRPCRequestManager.class);

    private record Pending(CompletableFuture<Object> completable, JsonRPCRequest request) {
    }
}
