package qmf.poc.service.qmf.storage.exceptions;

public class QMFObjectsStorageException extends Exception{
    public QMFObjectsStorageException(String message) {
        super(message);
    }

    public QMFObjectsStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public QMFObjectsStorageException(Throwable cause) {
        super(cause);
    }
}
