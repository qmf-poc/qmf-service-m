package qmf.poc.service.qmf.storage.impl;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.storage.QMFObjectsStorageMutable;
import qmf.poc.service.qmf.storage.exceptions.QMFObjectNotFoundException;
import qmf.poc.service.qmf.storage.exceptions.QMFObjectsStorageException;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;
import qmf.poc.service.qmf.storage.QMFObjectsStorage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class QMFObjectsStorageMemory implements QMFObjectsStorage, QMFObjectsStorageMutable {
    final Map<String, QMFObjectDocument> map = new ConcurrentHashMap<>();

    @Override
    public Future<Stream<QMFObjectDocument>> queryObjects(String search, int limit) {
        final Stream<QMFObjectDocument> documentStream =
                (search == null || search.isEmpty())
                        ? map.values().stream()
                        : map
                        .values()
                        .stream()
                        .filter(object ->
                                object.agentId().contains(search)
                                        || object.name().contains(search)
                                        || object.owner().contains(search)
                                        || object.typ().contains(search)
                                        || object.applData().contains(search)
                                        || object.remarks().contains(search)
                        );

        return Future.succeededFuture(
                (limit > 0)
                        ? documentStream.limit(limit)
                        : documentStream
        );
    }

    @Override
    public Future<QMFObjectDocument> getObject(String id) {
        if (id == null || id.isEmpty()) {
            return Future.failedFuture(new QMFObjectsStorageException("ID cannot be null or empty"));
        }
        QMFObjectDocument qmfObjectDocument = map.get(id);
        if (qmfObjectDocument == null) {
            return Future.failedFuture(new QMFObjectNotFoundException("Object not found for ID: " + id));
        }
        return Future.succeededFuture(qmfObjectDocument);
    }

    @Override
    public Future<Void> load(String agentId, @NotNull List<QMFObjectCatalog> objects) {
        log.debug("load start: agentId={}, objects={}", agentId, objects.size());
        for (QMFObjectCatalog object : objects) {
            QMFObjectDocument qmfObjectDocument = new QMFObjectDocument(
                    agentId,
                    object.owner(),
                    object.name(),
                    object.type(),
                    object.remarks(),
                    object.appldata()
            );
            map.put(qmfObjectDocument.id(), qmfObjectDocument);
        }
        log.trace("loaded: agentId={}, objects={}", agentId, objects.size());
        return Future.succeededFuture();
    }

    private void addObject(QMFObjectDocument qmfObjectDocument) {
        map.put(qmfObjectDocument.id(), qmfObjectDocument);
    }

    private static final Logger log = LoggerFactory.getLogger(QMFObjectsStorageMemory.class);
}
