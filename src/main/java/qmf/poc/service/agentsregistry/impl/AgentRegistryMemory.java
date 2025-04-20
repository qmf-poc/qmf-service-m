package qmf.poc.service.agentsregistry.impl;

import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class AgentRegistryMemory implements AgentsRegistry {
    private final Map<String, Boolean> agents = new ConcurrentHashMap<>();

    public AgentRegistryMemory(List<String> agents) {
        agents.forEach(agent -> this.agents.put(agent, false));
    }

    @Override
    public void enableAgent(String agentId) {
        agents.put(agentId, true);
    }

    @Override
    public void disableAgent(String agentId) {
        agents.put(agentId, false);
    }

    @Override
    public Stream<Agent> agents() {
        return agents.entrySet().stream().map(e-> new Agent(e.getKey(), e.getValue()));
    }
}
