package qmf.poc.service.verticles;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import qmf.poc.service.agentsregistry.Agent;
import qmf.poc.service.agentsregistry.AgentsRegistryMutable;
import qmf.poc.service.agentsregistry.impl.AgentsRegistryMemory;

import java.util.List;

import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_ENABLE;
import static qmf.poc.service.verticles.AgentsRegistryVerticle.AGENT_LIST_MODIFIED;

@ExtendWith(VertxExtension.class)
class AgentsRegistryVerticleTest {

    private AgentsRegistryMutable mockRegistry;

    @BeforeEach
    void setup() {
        // Mock registry with some test agents
        mockRegistry = new AgentsRegistryMemory(List.of("a", "b"));
    }

    @Test
    void testEnableAgent(Vertx vertx, VertxTestContext testContext) {
        List<Agent> expectedAgents = List.of(
                new Agent("a", false),
                new Agent("b", false),
                new Agent("agent1", true)
        );

        vertx.deployVerticle(new AgentsRegistryVerticle(mockRegistry)).onComplete(testContext.succeeding(id -> {
            EventBus eventBus = vertx.eventBus();

            eventBus.consumer(AGENT_LIST_MODIFIED, event -> {
                Assertions.assertEquals(expectedAgents, event.body());
                testContext.completeNow();
            });

            // Send a message to enable an agent
            eventBus.request(AGENT_ENABLE, "agent1")
                    .onComplete(testContext.succeeding(reply -> {
                        Assertions.assertEquals(expectedAgents, reply.body());
                    }));
        }));
    }
}