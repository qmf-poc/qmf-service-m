package qmf.poc.service.qmf.index.exceptions;

public class QMFObjectNotFoundException extends Exception{
    public QMFObjectNotFoundException(String message) {
        super(message);
    }

    public QMFObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QMFObjectNotFoundException(Throwable cause) {
        super(cause);
    }
}
