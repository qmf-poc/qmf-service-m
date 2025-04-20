package qmf.poc.service;

import io.vertx.core.Vertx;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.agentsregistry.impl.AgentRegistryMemory;
import qmf.poc.service.verticles.AgentsRegistryVerticle;
import qmf.poc.service.verticles.HttpServerVerticle;

public class Main {
    public static void main(String[] cli) {
        try {
            final Args args = new Args(cli);

            if (args.printHelp) args.printHelp();
            if (args.printVersion) System.out.println("agent-0.0.1");

            final Vertx vertx = Vertx.vertx();
            final AgentsRegistry agentsRegistry = new AgentRegistryMemory(args.agents);

            vertx.deployVerticle(new AgentsRegistryVerticle(agentsRegistry));
            vertx.deployVerticle(new HttpServerVerticle(agentsRegistry));

            Runtime.getRuntime().addShutdownHook(new Thread(()->{
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