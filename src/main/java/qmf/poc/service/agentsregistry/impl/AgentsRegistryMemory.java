package qmf.poc.service.agentsregistry.impl;

import io.vertx.core.Future;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistry;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AgentsRegistryMemory implements AgentsRegistry, AgentsRegistryMutable {
    private final Map<String, Boolean> agents = new ConcurrentHashMap<>();
    private final List<Consumer<List<Agent>>> listeners = new LinkedList<>();
    private final Object listenersLock = new Object();

    public AgentsRegistryMemory(List<String> agents) {
        agents.forEach(agent -> this.agents.put(agent, false));
    }

    @Override
    public Future<List<Agent>> enableAgent(String agentId) {
        agents.put(agentId, true);
        notifyListeners();
        return Future.succeededFuture(agents());
    }

    @Override
    public Future<List<Agent>> disableAgent(String agentId) {
        agents.put(agentId, false);
        notifyListeners();
        return Future.succeededFuture(agents());
    }

    @Override
    public List<Agent> agents() {
        return agents.entrySet().stream().map(e -> new Agent(e.getKey(), e.getValue())).toList();
    }

    @Override
    public void addListener(Consumer<List<Agent>> listener) {
        synchronized (listenersLock) {
            if (listeners.contains(listener)) {
                return;
            }
            listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(Consumer<List<Agent>> listener) {
        synchronized (listenersLock) {
            if (listeners.isEmpty()) {
                return;
            }
            listeners.remove(listener);
        }
    }

    private void notifyListeners() {
        final Consumer<List<Agent>>[] snapshot;
        synchronized (listenersLock) {
            //noinspection unchecked
            snapshot = listeners.toArray(new Consumer[0]);
        }
        List<Agent> agentsList = agents();
        for (Consumer<List<Agent>> listener : snapshot) {
            listener.accept(agentsList);
        }
    }
}
