package qmf.poc.service.http;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agent.AgentClient;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.qmf.storage.QMFObjectStorage;
import qmf.poc.service.qmf.storage.QMFObjectStorageException;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

public class RouteAPI {
    public static Router router(Vertx vertx, AgentsRegistry registry, AgentClient agentClient, QMFObjectStorage storage) {
        // Create a new Router instance
        Router router = Router.router(vertx);

        // Set up logging for all routes
        router.route().handler(LoggerHandler.create());
        // the service is alive
        router.route("/ping").handler(pingHandler);
        // the agent is alive
        final Handler<RoutingContext> pingAgentHandler = pingAgentHandler(agentClient);
        router.route("/ping-agent").handler(pingAgentHandler);
        router.route("/agent-ping").handler(pingAgentHandler);
        // sync the agent
        final Handler<RoutingContext> syncAgentHandler = syncAgentHandler(agentClient, storage);
        router.route("/sync").handler(syncAgentHandler);
        router.route("/catalog").handler(syncAgentHandler);
        router.route("/snapshot").handler(syncAgentHandler);
        // list of agents
        router.route("/agents").handler(agentsHandler(registry));
        // search documents
        final Handler<RoutingContext> queryHandler = queryHandler(storage);
        router.route("/query").handler(queryHandler);
        router.route("/search").handler(queryHandler);
        router.route("/retrieve").handler(queryHandler);
        final Handler<RoutingContext> getHandler = getHandler(storage);
        router.route("/get").handler(getHandler);
        final Handler<RoutingContext> runHandler = runHandler(agentClient);
        router.route("/run").handler(runHandler);

        return router;
    }

    private static final Logger log = LoggerFactory.getLogger(RouteAPI.class);

    private static final Handler<RoutingContext> pingHandler =
            routingContext -> {
                String payload = routingContext.request().getParam("payload");
                log.trace("Ping received with payload: {}", payload);
                routingContext.response().end("Pong=" + payload);
            };

    private static Handler<RoutingContext> pingAgentHandler(AgentClient agentClient) {
        return routingContext -> {
            String agentId = getAgentId(routingContext);
            if (agentId == null) return;
            String payload = routingContext.request().getParam("payload");
            log.trace("Ping agent {} with payload: {}", agentId, payload);

            agentClient.ping(agentId, payload)
                    .thenAccept(reply -> {
                        log.trace("agent {} ping response: {}", agentId, reply);
                        routingContext.response().end(reply == null ? "null" : reply);
                    }).exceptionally(ex -> {
                        final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} ping error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                        return null;
                    });
        };
    }

    private static Handler<RoutingContext> syncAgentHandler(AgentClient agentClient, QMFObjectStorage storage) {
        return routingContext -> {
            String agentId = getAgentId(routingContext);
            if (agentId == null) return;
            log.trace("Sync agent {}", agentId);

            agentClient.getCatalog(agentId)
                    .thenAccept(reply -> {
                        // log.trace("agent {} sync response: {}", agentId, reply);
                        log.trace("agent {} sync returned {} objects", agentId, reply.size());
                        try {
                            storage.load(agentId, reply);
                            routingContext.response().end("synced");
                        } catch (QMFObjectStorageException e) {
                            log.trace("agent {} sync error: {}", agentId, e.getMessage(), e);
                            routingContext.response().setStatusCode(500).end("Error: " + e.getMessage());
                        }
                    }).exceptionally(ex -> {
                        final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} sync error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                        return null;
                    });
        };
    }

    private static Handler<RoutingContext> queryHandler(QMFObjectStorage storage) {
        return routingContext -> {
            String search = routingContext.request().getParam("search");
            String limitP = routingContext.request().getParam("limit");
            Integer limit;
            try {
                limit = limitP == null ? null : Integer.parseInt(limitP);
            } catch (NumberFormatException e) {
                routingContext.response().setStatusCode(400).end("bad limit: " + e.getMessage());
                return;
            }

            log.trace("query catalog search: \"{}\", limit: {}", search, limit);

            try {
                final List<QMFObjectDocument> documents = storage.queryObject(search, limit == null ? -1 : limit)
                        .toList();
                log.trace("query catalog returned {} objects", documents.size());
                routingContext.response()
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonArray(documents).encode());
            } catch (QMFObjectStorageException e) {
                final String message = e.getMessage();
                log.trace("query catalog error: {}", message, e);
                routingContext.response().setStatusCode(400).end("Error: " + message);
            }
        };
    }

    private static Handler<RoutingContext> getHandler(QMFObjectStorage storage) {
        return routingContext -> {
            String id = routingContext.request().getParam("id");
            if (id == null || id.isEmpty()) {
                routingContext.response().setStatusCode(400).end("no id");
                return;
            }

            log.trace("get catalog object id=\"{}\"", id);

            try {
                final Optional<QMFObjectDocument> documentO = storage.getObject(id);
                if (documentO.isEmpty()) {
                    routingContext.response().setStatusCode(404).end("document id=\""+id+"\" not found");
                } else {
                    final QMFObjectDocument document = documentO.get();
                    log.trace("get catalog object id=\"{}\"", id);
                    routingContext.response()
                            .putHeader("Content-Type", "application/json")
                            .end(JsonObject.mapFrom(document).encode());
                }
            } catch (QMFObjectStorageException e) {
                final String message = e.getMessage();
                log.trace("query catalog error: {}", message, e);
                routingContext.response().setStatusCode(400).end("Error: " + message);
            }
        };
    }

    private static Handler<RoutingContext> runHandler(AgentClient agentClient) {
        return routingContext -> {
            String agentId = getAgentId(routingContext);
            if (agentId == null) return;
            String owner = routingContext.request().getParam("owner");
            if (owner == null || owner.isEmpty()) {
                routingContext.response().setStatusCode(400).end("Missing owner");
                return;
            }
            String name = routingContext.request().getParam("name");
            if (name == null || name.isEmpty()) {
                routingContext.response().setStatusCode(400).end("Missing name");
                return;
            }
            String limitP = routingContext.request().getParam("limit");
            int limit;
            try {
                limit = limitP == null ? 99999 : Integer.parseInt(limitP);
            } catch (NumberFormatException e) {
                routingContext.response().setStatusCode(400).end("bad limit: " + e.getMessage());
                return;
            }

            agentClient.run(agentId, owner, name, limit)
                    .thenAccept(reply -> {
                        log.trace("agent {} run response: {}", agentId, reply.body());
                        routingContext.response().end(JsonObject.mapFrom(reply).encode());
                    }).exceptionally(ex -> {
                        final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} run error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                        return null;
                    });
        };
    }

    private static Handler<RoutingContext> agentsHandler(AgentsRegistry registry) {
        return routingContext -> routingContext
                .response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonArray(registry.agents().toList()).encode());
    }

    private static String getAgentId(RoutingContext routingContext) {
        String agentId = routingContext.request().getParam("agent");
        if (agentId == null || agentId.isEmpty()) {
            log.error("Missing agent ID");
            routingContext
                    .response()
                    .setStatusCode(400)
                    .end("Missing agent ID");
        }
        return agentId;
    }

    private static Throwable unwrapCompletionException(Throwable throwable) {
        if (throwable instanceof CompletionException) {
            return throwable.getCause();
        }
        return throwable;
    }
}
