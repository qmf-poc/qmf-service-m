package qmf.poc.service.qmf.storage.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;
import qmf.poc.service.qmf.storage.QMFObjectStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class QMFObjectStorageMemory implements QMFObjectStorage {
    final Map<String, QMFObjectDocument> map = new ConcurrentHashMap<>();

    @Override
    public void addObject(QMFObjectDocument qmfObjectDocument) {
        map.put(qmfObjectDocument.id(), qmfObjectDocument);
    }

    @Override
    public Stream<QMFObjectDocument> queryObject(String search, int limit) {
        final int lim = limit < 0 ? Integer.MAX_VALUE : limit;

        if (search == null || search.isEmpty()) {
            return map.values().stream().limit(lim);
        }
        return map
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
    }

    @Override
    public Optional<QMFObjectDocument> getObject(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        QMFObjectDocument qmfObjectDocument = map.get(id);
        if (qmfObjectDocument == null) {
            return Optional.empty();
        }
        return Optional.of(qmfObjectDocument);
    }

    @Override
    public void load(String agentId, List<QMFObjectCatalog> objects) {
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
    }

    private static final Logger log = LoggerFactory.getLogger(QMFObjectStorageMemory.class);
}
