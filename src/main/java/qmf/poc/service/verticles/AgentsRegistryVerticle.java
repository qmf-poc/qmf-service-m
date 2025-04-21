package qmf.poc.service.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;

import java.util.List;

public class AgentsRegistryVerticle extends AbstractVerticle {
    private final AgentsRegistryMutable registryMutable;

    public AgentsRegistryVerticle(AgentsRegistryMutable registryMutable) {
        this.registryMutable = registryMutable;
    }

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        eb.consumer(AGENT_ENABLE, (Message<String> message) -> {
            String agentId = message.body();
            registryMutable.enableAgent(agentId).onSuccess(agents -> {
                message.reply(agents);
                if (log.isTraceEnabled()) traceModified(agents);
                eb.publish(AGENT_LIST_MODIFIED, agents);
            }).onFailure(err -> {
                log.error("Failed to enable agent: {}", agentId, err);
                message.fail(500, "Failed to enable agent: " + err.getMessage());
            });
        });
        log.debug("eventbus register consumer: {}", AGENT_ENABLE);
        eb.consumer(AGENT_DISABLE, (Message<String> message) -> {
            String agentId = message.body();
            registryMutable.disableAgent(agentId).onSuccess(agents -> {
                message.reply(agents);
                if (log.isTraceEnabled()) traceModified(agents);
                eb.publish(AGENT_LIST_MODIFIED, agents);
            }).onFailure(err -> {
                log.error("Failed to disable agent: {}", agentId, err);
                message.fail(500, "Failed to disable agent: " + err.getMessage());
            });
        });
        log.debug("eventbus register consumer: {}", AGENT_DISABLE);
        log.info("Agents registry verticle started");
    }

    private static void traceModified(List<Agent> agents) {
        log.trace("agents list modified: {}", String.join(", ", agents.stream().map(a -> a.id() + '=' + a.active()).toList()));
    }

    public final static String AGENT_ENABLE = "qmf.agents.registry.enable";
    public final static String AGENT_DISABLE = "qmf.agents.registry.disable";
    public final static String AGENT_LIST_MODIFIED = "qmf.agents.registry.modified";

    private static final Logger log = LoggerFactory.getLogger(AgentsRegistryVerticle.class);
}
