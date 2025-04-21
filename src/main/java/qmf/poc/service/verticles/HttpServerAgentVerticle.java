package qmf.poc.service.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;
import qmf.poc.service.http.WebSoketAgent;
import qmf.poc.service.jsonrpc.transport.JsonRPCAgentsTransport;

public class HttpServerAgentVerticle extends AbstractVerticle {
    private final JsonRPCAgentsTransport broker;
    private final AgentsRegistryMutable agentsRegistry;

    public HttpServerAgentVerticle(JsonRPCAgentsTransport broker, AgentsRegistryMutable agentsRegistry) {
        this.broker = broker;
        this.agentsRegistry = agentsRegistry;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if ("/agent".equals(req.path())) {
                        WebSoketAgent.upgraded(req, broker, agentsRegistry);
                    }
                })
                .listen(PORT)
                .onSuccess(s -> {
                    log.info("Agent websocket server started on port " + PORT);
                    startPromise.complete();
                })
                .onFailure(err -> {
                    log.error("Failed to start agent websocket server", err);
                    startPromise.fail(err);
                });
    }

    @Override
    public void stop() {
        log.info("Agent websocket server stopped");
    }

    private static final Logger log = LoggerFactory.getLogger(HttpServerAgentVerticle.class);
    private static final int PORT = 8082;
}
