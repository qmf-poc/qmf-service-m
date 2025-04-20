package qmf.poc.service.qmf.storage.impl;

import qmf.poc.service.qmf.storage.models.QMFObject;
import qmf.poc.service.qmf.storage.QMFObjectStorage;
import qmf.poc.service.qmf.storage.QMFObjectStorageException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

class QMFObjectStorageMemory implements QMFObjectStorage {
    final Map<String, QMFObject> map = new ConcurrentHashMap<>();

    @Override
    public void addObject(QMFObject qmfObject) throws QMFObjectStorageException {
        map.put(qmfObject.id(), qmfObject);
    }

    @Override
    public Stream<QMFObject> queryObject(String search, int limit) throws QMFObjectStorageException {
        if (search == null || search.isEmpty()) {
            return map.values().stream().limit(limit);
        }
        return map
                .values()
                .stream()
                .filter(object ->
                        object.name().contains(search)
                                || object.owner().contains(search)
                                || object.typ().contains(search)
                                || object.applData().contains(search)
                                || object.remarks().contains(search)
                );
    }

    @Override
    public Optional<QMFObject> getObject(String id) throws QMFObjectStorageException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        QMFObject qmfObject = map.get(id);
        if (qmfObject == null) {
            return Optional.empty();
        }
        return Optional.of(qmfObject);
    }
}
