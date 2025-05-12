package qmf.poc.service.qmf.index;


import io.vertx.core.Future;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;

import java.util.List;

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
