package qmf.poc.service.agent;

import io.vertx.core.Future;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;

import java.util.List;

public interface AgentClient {
    Future<String> ping(String agentId, Object payload);

    Future<List<QMFObjectCatalog>> getCatalog(String agentId);
    Future<RunResult> run(String agentId, String owner, String name, int limit);
}
