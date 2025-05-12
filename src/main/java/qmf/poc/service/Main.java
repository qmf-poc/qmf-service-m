package qmf.poc.service;

import io.vertx.core.Vertx;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agent.AgentClient;
import qmf.poc.service.agentsregistry.impl.AgentsRegistryMemory;
import qmf.poc.service.jsonrpc.AgentClientJsonRPC;
import qmf.poc.service.jsonrpc.transport.JsonRPCAgentsTransport;
import qmf.poc.service.qmf.index.impl.QMFObjectsStorageMemory;
import qmf.poc.service.verticles.HttpServerAgentVerticle;
import qmf.poc.service.verticles.HttpServerAPIVerticle;

public class Main {
    public static void main(String[] cli) {
        try {
            final Args args = new Args(cli);

            if (args.printHelp) {
                args.printHelp();
                return;
            }
            if (args.printVersion) {
                Version.printVersion(log);
                return;
            }

            final QMFObjectsStorageMemory qmfObjectStorage = new QMFObjectsStorageMemory();
            final AgentsRegistryMemory agentsRegistry = new AgentsRegistryMemory(args.agents);
            final JsonRPCAgentsTransport jsonRPCAgentsTransport = new JsonRPCAgentsTransport();
            final AgentClient agentClient = new AgentClientJsonRPC(jsonRPCAgentsTransport);

            final Vertx vertx = Vertx.vertx();

            vertx.deployVerticle(new HttpServerAPIVerticle(agentsRegistry, agentClient, qmfObjectStorage, qmfObjectStorage));
            vertx.deployVerticle(new HttpServerAgentVerticle(jsonRPCAgentsTransport, agentsRegistry));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                vertx.close();
                log.info("Vert.x instance closed");
            }));

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println("Use --help to show all options");
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Main.class);
}