package qmf.poc.service.agentsregistry;

import io.vertx.core.Future;

import java.util.List;

public interface AgentsRegistryMutable {
    @SuppressWarnings("UnusedReturnValue")
    Future<List<Agent>> enableAgent(String agentId, String db);

    @SuppressWarnings("UnusedReturnValue")
    Future<List<Agent>> disableAgent(String agentId);
}
