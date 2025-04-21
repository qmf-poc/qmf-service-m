package qmf.poc.service.agentsregistry;

import java.util.List;
import java.util.function.Consumer;

public interface AgentsRegistry {
    List<Agent> agents();
    void addListener(Consumer<List<Agent>> listener);
    void removeListener(Consumer<List<Agent>> listener);
}