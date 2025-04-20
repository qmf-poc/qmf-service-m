package qmf.poc.service.qmf.storage;


import qmf.poc.service.qmf.catalog.QMFObjectCatalog;
import qmf.poc.service.qmf.storage.models.QMFObjectDocument;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// TODO: make async
public interface QMFObjectStorage {
    void addObject(QMFObjectDocument qmfObjectDocument) throws QMFObjectStorageException;

    Stream<QMFObjectDocument> queryObject(String search, int limit) throws QMFObjectStorageException;

    Optional<QMFObjectDocument> getObject(String id) throws QMFObjectStorageException;

    void load(String agentId, List<QMFObjectCatalog> objects) throws QMFObjectStorageException;
}
