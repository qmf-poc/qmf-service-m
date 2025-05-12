package qmf.poc.service.qmf.index;


import io.vertx.core.Future;
import qmf.poc.service.qmf.index.models.QMFObjectDocument;

import java.util.stream.Stream;

public interface QMFObjectsStorage {
    /**
     * @param search - currently just a substring search
     * @param limit  - limit the number of objects returned, 0 means no limit
     * @return a `Future` that will succeed with a stream of QMFObjectDocument
     * objects matching the search criteria, or fail with a `QMFObjectStorageException`
     */
    Future<Stream<QMFObjectDocument>> queryObjects(String search, int limit);

    /**
     * Retrieves a QMFObjectDocument by its ID.
     *
     * @param id the ID of the object to retrieve, currently "agent-owner-name-type"
     * @return a `Future` that will succeed with the QMFObjectDocument if found,
     * or fail with a `QMFObjectStorageException` if the ID is null, empty,
     * or if the object is not found.
     */
    Future<QMFObjectDocument> getObject(String id);
}
