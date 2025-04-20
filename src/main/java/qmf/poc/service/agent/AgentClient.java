package qmf.poc.service.agent;

import qmf.poc.service.qmf.catalog.QMFObjectCatalog;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface AgentClient {
    CompletionStage<String> ping(String agentId, Object payload);

    CompletionStage<List<QMFObjectCatalog>> getCatalog(String agentId);
    CompletionStage<RunResult> run(String agentId, String owner, String name, int limit);
}
