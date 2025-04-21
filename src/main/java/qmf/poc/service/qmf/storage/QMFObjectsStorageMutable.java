package qmf.poc.service.qmf.storage;


import io.vertx.core.Future;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;

import java.util.List;
import java.util.stream.Stream;

public interface QMFObjectsStorageMutable {
    /**
     * Store the QMFObjectCatalogs in the storage.
     *
     * @param agentId The ID of the agent.
     * @param objects The list of QMFObjectCatalogs to store.
     * @return A Future that will be completed when the operation is done or failed with QMFObjectStorageException.
     */
    Future<Void> load(String agentId, List<QMFObjectCatalog> objects);
}
