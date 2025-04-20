package qmf.poc.service.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;

import java.util.List;

public class AgentsRegistryVerticle extends AbstractVerticle {
    private final AgentsRegistry registry;

    public AgentsRegistryVerticle(AgentsRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void start() {
        EventBus eb = vertx.eventBus();
        eb.consumer(AGENT_ENABLE, (Message<String> message) -> {
            String agentId = message.body();
            registry.enableAgent(agentId);
            final List<Agent> agents = registry.agents().toList();
            message.reply(agents);
            if (log.isTraceEnabled())
                log.trace("agents list modified: {}", String.join(", ", agents.stream().map(Agent::id).toList()));
            eb.publish(AGENT_LIST_MODIFIED, agents);
        });
        log.debug("eventbus register consumer: {}", AGENT_ENABLE);
        eb.consumer(AGENT_DISABLE, (Message<String> message) -> {
            String agentId = message.body();
            registry.disableAgent(agentId);
            final List<Agent> agents = registry.agents().toList();
            message.reply(agents);
            if (log.isTraceEnabled())
                log.trace("agents list modified: {}", String.join(", ", agents.stream().map(Agent::id).toList()));
            eb.publish(AGENT_LIST_MODIFIED, agents);
        });
        log.debug("eventbus register consumer: {}", AGENT_DISABLE);
        log.info("Agents registry verticle started");
    }

    public final static String AGENT_ENABLE = "qmf.agent.registry.enable";
    public final static String AGENT_DISABLE = "qmf.agent.registry.disable";
    public final static String AGENT_LIST_MODIFIED = "qmf.agent.registry.modified";

    private final Logger log = LoggerFactory.getLogger(AgentsRegistryVerticle.class);
}
