package qmf.poc.service.qmf.storage;


import qmf.poc.service.qmf.storage.models.QMFObject;

import java.util.Optional;
import java.util.stream.Stream;

public interface QMFObjectStorage {
    void addObject(QMFObject qmfObject) throws QMFObjectStorageException;

    Stream<QMFObject> queryObject(String search, int limit) throws QMFObjectStorageException;

    Optional<QMFObject> getObject(String id) throws QMFObjectStorageException;
}
