package qmf.poc.service.agentsregistry;

import java.io.Serializable;

public record Agent(String id, boolean active, String db) implements Serializable {
}
