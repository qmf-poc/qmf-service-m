package qmf.poc.service.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agent.AgentClient;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.http.RouteAPI;
import qmf.poc.service.http.WebSoketAPI;
import qmf.poc.service.qmf.storage.QMFObjectStorage;

public class HttpServerAPIVerticle extends AbstractVerticle {
    private final AgentsRegistry registry;
    private final AgentClient agentClient;
    private final QMFObjectStorage storage;

    public HttpServerAPIVerticle(AgentsRegistry registry, AgentClient agentClient, QMFObjectStorage storage) {
        this.registry = registry;
        this.agentClient = agentClient;
        this.storage = storage;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        final Router router = RouteAPI.router(vertx, registry, agentClient, storage );
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if ("/rpc".equals(req.path())) {
                        WebSoketAPI.upgraded(vertx, req, log);
                    } else {
                        router.handle(req);
                    }
                })
                .listen(PORT)
                .onSuccess(s -> {
                    log.info("API HTTP server started on port " + PORT);
                    startPromise.complete();
                })
                .onFailure(err -> {
                    log.error("Failed to start API HTTP server", err);
                    startPromise.fail(err);
                });
    }

    @Override
    public void stop() {
        log.info("API HTTP server stopped");
    }

    private static final Logger log = LoggerFactory.getLogger(HttpServerAPIVerticle.class);
    private static final int PORT = 8081;
}
