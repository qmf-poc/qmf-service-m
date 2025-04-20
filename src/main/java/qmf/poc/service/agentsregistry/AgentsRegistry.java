package qmf.poc.service.agentsregistry;

import java.util.stream.Stream;

public interface AgentsRegistry {
    void enableAgent(String agentId);
    void disableAgent(String agentId);
    Stream<Agent> agents();
}
