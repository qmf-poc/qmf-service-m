package qmf.poc.service.jsonrpc.transport;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.jsonrpc.messages.JsonRPCError;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;
import qmf.poc.service.jsonrpc.messages.JsonRPCResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static qmf.poc.service.log.LogHelper.ellipse;

public class JsonRPCImpl {
    private final Map<Long, Pending> pendings = new ConcurrentHashMap<>();

    public Future<Object> rememberRequest(JsonRPCRequest request) {
        Promise<Object> promise = Promise.promise();
        pendings.put(request.id, new Pending(promise, request));
        log.trace("rememberRequest: id={} method={} params={}", request.id, request.method, request.params);
        return promise.future();
    }

    public void acceptResult(JsonRPCResult result) {
        log.trace("handleResult: id={} result={}", result.id, ellipse(result.result));
        Pending pending = pendings.remove(result.id);

        if (pending == null) {
            log.warn("handle result without pending request: id={} result={}", result.id, result.result);
            return;
        }

        pending.promise.complete(result.result);
    }

    public void acceptError(JsonRPCError error) {
        Pending pending = pendings.remove(error.id);

        if (pending == null) {
            log.warn("Error without pending request: {}", error);
            return;
        }

        pending.promise.fail(new JsonRPCException(pending.request, error.error));
    }

    public void close() {
        pendings.forEach((i, c) -> {
            if (c != null) {
                c.promise.fail("Cancelled");
            }
        });
    }

    static private final Logger log = LoggerFactory.getLogger(JsonRPCImpl.class);

    private record Pending(Promise<Object> promise, JsonRPCRequest request) {
    }
}
