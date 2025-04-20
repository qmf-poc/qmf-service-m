package qmf.poc.service.http;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.LoggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.AgentsRegistry;

import java.util.Map;

import static qmf.poc.service.verticles.HttpServerVerticle.AGENT_PING;

public class RouteHandler {
    public static Router router(Vertx vertx, AgentsRegistry registry) {
        // Create a new Router instance
        Router router = Router.router(vertx);

        // Set up logging for all routes
        router.route().handler(LoggerHandler.create());

        router.route("/ping").handler(pingHandler);

        // Define a route for agent ping
        final Handler<RoutingContext> pingAgentHandler = pingAgentHandler(vertx);
        router.route("/ping-agent").handler(pingAgentHandler);
        router.route("/agent-ping").handler(pingAgentHandler);

        // Define a route for agent registry
        router.route("/agents").handler(agentsHandler(registry));

        return router;
    }

    private static final Logger log = LoggerFactory.getLogger(RouteHandler.class);

    private static final Handler<RoutingContext> pingHandler =
            routingContext -> {
                String payload = routingContext.request().getParam("payload");
                log.trace("Ping received with payload: {}", payload);
                routingContext.response().end("Pong=" + payload);
            };

    private static Handler<RoutingContext> pingAgentHandler(Vertx vertx) {
        return routingContext -> {
            String agentId = routingContext.request().getParam("agent");
            String payload = routingContext.request().getParam("payload");
            log.trace("Ping agent {} with payload: {}", agentId, payload);

            vertx.eventBus().request(AGENT_PING(agentId), payload).onComplete(message -> {
                if (message.succeeded()) {
                    final Object body = message.result().body();
                    log.trace("agent {} ping response: {}", agentId, body);
                    routingContext.response().end(body == null ? "<null>" : body.toString());
                } else {
                    log.trace("agent {} ping error: {}", agentId, message.cause().getMessage());
                    routingContext.response().setStatusCode(500).end("Error: " + message.cause().getMessage());
                }
            });
        };
    }

    private static Handler<RoutingContext> agentsHandler(AgentsRegistry registry) {
        return routingContext -> routingContext
                .response()
                .putHeader("Content-Type", "application/json")
                .end(JsonObject.mapFrom(Map.of("agents", registry.agents().toList())).encode());
    }
}
