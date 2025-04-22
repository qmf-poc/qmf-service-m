package qmf.poc.service.http;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agent.AgentClient;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.qmf.storage.QMFObjectsStorage;
import qmf.poc.service.qmf.storage.QMFObjectsStorageMutable;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class RouteAPI {
    @org.jetbrains.annotations.NotNull
    public static Router router(
            Vertx vertx,
            AgentsRegistry registry,
            AgentClient agentClient,
            QMFObjectsStorage storage,
            QMFObjectsStorageMutable storageMutable) {
        // Create a new Router instance
        Router router = Router.router(vertx);

        // Set up logging for all routes
        router.route().handler(LoggerHandler.create());
        router.route().handler(
                CorsHandler
                        .create()
                        .addOrigin("*")
                        .allowedMethods(Set.of(
                                HttpMethod.GET,
                                HttpMethod.OPTIONS,
                                HttpMethod.POST)));
        // the service is alive
        router.route("/ping").handler(pingHandler);
        // the agent is alive
        final Handler<RoutingContext> pingAgentHandler = pingAgentHandler(agentClient, registry);
        router.route("/ping-agent").handler(pingAgentHandler);
        router.route("/agent-ping").handler(pingAgentHandler);
        // sync the agent
        final Handler<RoutingContext> syncAgentHandler = syncAgentHandler(agentClient, storageMutable, registry);
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
        final Handler<RoutingContext> runHandler = runHandler(agentClient, registry);
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

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> pingAgentHandler(AgentClient agentClient, AgentsRegistry agentsRegistry) {
        return routingContext -> {
            String agentId = getAgentId(routingContext, agentsRegistry);
            if (agentId == null) return;
            String payload = routingContext.request().getParam("payload");
            log.trace("Ping agent {} with payload: {}", agentId, payload);

            agentClient.ping(agentId, payload)
                    .onSuccess(reply -> {
                        log.trace("agent {} ping response: {}", agentId, reply);
                        routingContext.response().end(reply == null ? "null" : reply);
                    }).onFailure(throwable -> {
                        // final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} ping error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                    });
        };
    }

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> syncAgentHandler(AgentClient agentClient, QMFObjectsStorageMutable storageMutable, AgentsRegistry agentsRegistry) {
        return routingContext -> {
            String agentId = getAgentId(routingContext, agentsRegistry);
            if (agentId == null) return;
            log.trace("Sync agent {}", agentId);

            //noinspection CodeBlock2Expr
            agentClient.getCatalog(agentId)
                    .compose(reply -> {
                        log.trace("agent {} sync returned {} objects", agentId, reply.size());
                        return storageMutable.load(agentId, reply);
                    })
                    .onSuccess(reply -> {
                        routingContext.response().end("synced");
                    }).onFailure(throwable -> {
                        // final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} sync error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                    });
        };
    }

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> queryHandler(QMFObjectsStorage storage) {
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
            storage.queryObjects(search, limit == null ? -1 : limit)
                    .onSuccess(documentsStream -> {
                        final List<QMFObjectDocument> documents = documentsStream.toList();
                        log.trace("query catalog returned {} objects", documents.size());
                        routingContext.response()
                                .putHeader("Content-Type", "application/json")
                                .end(new JsonArray(documents).encode());
                    }).onFailure(throwable -> {
                        // final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("query catalog error: {}", message, throwable);
                        routingContext.response().setStatusCode(400).end("Error: " + message);
                    });
        };
    }

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> getHandler(QMFObjectsStorage storage) {
        return routingContext -> {
            String id = routingContext.request().getParam("id");
            if (id == null || id.isEmpty()) {
                routingContext.response().setStatusCode(400).end("no id");
                return;
            }

            log.trace("get catalog object id=\"{}\"", id);

            storage.getObject(id)
                    .onSuccess(document -> {
                        log.trace("get catalog object id=\"{}\"", id);
                        routingContext.response()
                                .putHeader("Content-Type", "application/json")
                                .end(JsonObject.mapFrom(document).encode());
                    }).onFailure(throwable -> {
                        // final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("query catalog error: {}", message, throwable);
                        routingContext.response().setStatusCode(400).end("Error: " + message);
                    });

        };
    }

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> runHandler(AgentClient agentClient, AgentsRegistry agentsRegistry) {
        return routingContext -> {
            String agentId = getAgentId(routingContext, agentsRegistry);
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
                    .onSuccess(reply -> {
                        log.trace("agent {} run response: {}", agentId, reply);
                        routingContext.response()
                                .putHeader("Content-Type", "application/json")
                                .end(JsonObject.mapFrom(reply).encode());
                    })
                    .onFailure(throwable -> {
                        // final Throwable throwable = unwrapCompletionException(ex);
                        final String message = throwable.getMessage();
                        log.trace("agent {} run error: {}", agentId, throwable.getMessage(), throwable);
                        routingContext.response().setStatusCode(500).end("Error: " + message);
                    });
        };
    }

    @NotNull
    @Contract(pure = true)
    private static Handler<RoutingContext> agentsHandler(AgentsRegistry registry) {
        return routingContext -> routingContext
                .response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonArray(registry.agents()).encode());
    }

    @Nullable
    private static String getAgentId(RoutingContext routingContext, AgentsRegistry registry) {
        final String agentId = routingContext.request().getParam("agent");
        if (agentId == null || agentId.isEmpty()) {
            try {
                return registry.agents().getFirst().id();
            } catch (NoSuchElementException e) {
                log.error("Missing agent ID and not one agent connected");
                routingContext
                        .response()
                        .setStatusCode(400)
                        .end("Missing agent ID");
                return null;
            }
        }
        return agentId;
    }
}
