package qmf.poc.service.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.http.RouteHandler;
import qmf.poc.service.http.WebSoketAgent;
import qmf.poc.service.http.WebSoketFrontend;

public class HttpServerVerticle extends AbstractVerticle {
    private final AgentsRegistry registry;

    public HttpServerVerticle(AgentsRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        final Router router = RouteHandler.router(vertx, registry);
        vertx.createHttpServer()
                .requestHandler(req -> {
                    if ("/rpc".equals(req.path())) {
                        WebSoketFrontend.upgraded(vertx, req, log);
                    } else if ("/agent".equals(req.path())) {
                        WebSoketAgent.upgraded(vertx, req);
                    } else {
                        router.handle(req);
                    }
                })
                .listen(8081)
                .onSuccess(s -> {
                    log.info("HTTP server started on port 8081");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    log.error("Failed to start HTTP server", err);
                    startPromise.fail(err);
                });
    }

    @Override
    public void stop() {
        log.info("HTTP server stopped");
    }

    public static String AGENT_PING(String agent) {
        return "qmf.agent.command.ping." + agent;
    }

    private static final Logger log = LoggerFactory.getLogger(HttpServerVerticle.class);
}
