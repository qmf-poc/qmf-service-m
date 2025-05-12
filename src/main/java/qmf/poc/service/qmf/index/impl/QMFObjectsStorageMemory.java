package qmf.poc.service.qmf.index.impl;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.index.QMFObjectsStorageMutable;
import qmf.poc.service.qmf.index.exceptions.QMFObjectNotFoundException;
import qmf.poc.service.qmf.index.exceptions.QMFObjectsStorageException;
import qmf.poc.service.qmf.index.indexer.AppldataIndexer;
import qmf.poc.service.qmf.index.models.QMFObjectDocument;
import qmf.poc.service.qmf.index.QMFObjectsStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class QMFObjectsStorageMemory implements QMFObjectsStorage, QMFObjectsStorageMutable {
    final Map<String, QMFObjectDocument> objById = new ConcurrentHashMap<>();
    final Map<String, List<QMFObjectDocument>> objByKey = new ConcurrentHashMap<>();

    @Override
    public Future<Stream<QMFObjectDocument>> queryObjects(String search, int limit) {
        final Stream<QMFObjectDocument> documentStream =
                search == null || search.length() < 3
                        ? objById.values().stream()
                        : objByKey.keySet().stream()
                        .filter(key -> key.contains(search))
                        .flatMap(key -> objByKey.get(key).stream());

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
        QMFObjectDocument qmfObjectDocument = objById.get(id);
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
            objById.put(qmfObjectDocument.id(), qmfObjectDocument);
            List<String> index = AppldataIndexer.index(object.type(), object.appldata());
            index.add(object.owner());
            index.add(object.name());
            for (String key : index) {
                objByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(qmfObjectDocument);
            }
        }
        log.trace("loaded: agentId={}, objects={}", agentId, objects.size());
        return Future.succeededFuture();
    }

    private static final Logger log = LoggerFactory.getLogger(QMFObjectsStorageMemory.class);
}
