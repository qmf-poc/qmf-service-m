package qmf.poc.service.agentsregistry;

import io.vertx.core.Future;

import java.util.List;

public interface AgentsRegistryMutable {
    Future<List<Agent>> enableAgent(String agentId);

    Future<List<Agent>> disableAgent(String agentId);
}
