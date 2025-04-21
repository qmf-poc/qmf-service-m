package qmf.poc.service.agentsregistry.impl;

import io.vertx.core.Future;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentsRegistryMemory implements AgentsRegistry, AgentsRegistryMutable {
    private final Map<String, Boolean> agents = new ConcurrentHashMap<>();

    public AgentsRegistryMemory(List<String> agents) {
        agents.forEach(agent -> this.agents.put(agent, false));
    }

    @Override
    public Future<List<Agent>> enableAgent(String agentId) {
        agents.put(agentId, true);
        return Future.succeededFuture(agents());
    }

    @Override
    public Future<List<Agent>> disableAgent(String agentId) {
        agents.put(agentId, false);
        return Future.succeededFuture(agents());
    }

    @Override
    public List<Agent> agents() {
        return agents.entrySet().stream().map(e-> new Agent(e.getKey(), e.getValue())).toList();
    }
}
