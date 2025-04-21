package qmf.poc.service.jsonrpc;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agent.AgentClient;
import qmf.poc.service.agent.RunResult;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;
import qmf.poc.service.jsonrpc.transport.JsonRPCAgentsTransport;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AgentClientJsonRPC implements AgentClient {
    private final JsonRPCAgentsTransport transport;

    public AgentClientJsonRPC(JsonRPCAgentsTransport transport) {
        this.transport = transport;
    }

    @Override
    public Future<String> ping(String agentId, Object payload) {
        log.debug("ping agent {} with payload {}", agentId, payload);
        final long id = id();
        final JsonRPCRequest request = new JsonRPCRequest(id, "ping", Map.of("payload", payload == null ? "" : payload));

        return transport
                .sendRequest(agentId, request)
                .map(result -> (result instanceof String) ? (String) result : result.toString());
    }

    @Override
    public Future<RunResult> run(String agentId, String owner, String name, int limit) {
        log.debug("run agent {} with owner {} name {} limit {}", agentId, owner, name, limit);
        final long id = id();
        final JsonRPCRequest request = new JsonRPCRequest(id, "run", Map.of(
                "owner", owner,
                "name", name,
                "limit", limit
        ));

        return transport
                .sendRequest(agentId, request)
                .map(result ->
                        new RunResult(
                                (result instanceof JsonObject jsonObject) ? jsonObject.getString("body") : "",
                                owner,
                                name));
    }

    @Override
    public Future<List<QMFObjectCatalog>> getCatalog(String agentId) {
        log.debug("sync agent {}", agentId);
        final long id = id();
        final JsonRPCRequest request = new JsonRPCRequest(id, "snapshot", Map.of());

        return transport
                .sendRequest(agentId, request)
                .map(result -> {
                    if (result instanceof JsonObject jsonObject) {
                        Object catalogValue = jsonObject.getValue("catalog");
                        if (catalogValue instanceof JsonObject catalogObj) {
                            Object qmfObjects = catalogObj.getValue("qmfObjects");
                            if (qmfObjects instanceof JsonArray qmfArray) {
                                log.debug("qmfObjects has {} items", qmfArray.size());
                                final List<QMFObjectCatalog> catalog = qmfArray.stream()
                                        .map(obj -> {
                                            if (obj instanceof JsonObject m) {
                                                return QMFObjectCatalog.fromMap(m);
                                            } else {
                                                return null;
                                            }
                                        })
                                        .filter(Objects::nonNull)
                                        .toList();
                                log.debug("catalog derived from qmfObjects has {} items", catalog.size());
                                return catalog;
                            } else {
                                log.error("qmfObjects is not a JsonArray: {}", qmfObjects.getClass());
                                // TODO: throw new Exception("snapshot result is not a map: ");
                                return List.of();
                            }
                        } else {
                            log.error("catalog is not a JsonObject: {}", catalogValue.getClass());
                            // TODO: throw new Exception("snapshot result is not a map: ");
                            return List.of();
                        }
                    } else {
                        log.error("snapshot result is not a JsonObject: {}", result.getClass());
                        // TODO: throw new Exception("snapshot result is not a map: ");
                        return List.of();
                    }
                });
    }

    private static long lastId = 0;

    private static long id() {
        long id = System.currentTimeMillis();
        while (id <= lastId) {
            id++;
        }
        lastId = id;
        return id;
    }

    private static final Logger log = LoggerFactory.getLogger(AgentClientJsonRPC.class);
}
