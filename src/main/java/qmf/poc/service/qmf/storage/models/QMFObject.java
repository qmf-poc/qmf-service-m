package qmf.poc.service.qmf.storage.models;

public record QMFObject(
    QMFRepository repository,
    String owner,
    String name,
    String typ,
    String remarks,
    String applData,
    String id
) {
    public QMFObject(QMFRepository repository, String owner, String name, String typ, String remarks, String applData) {
        this(repository, owner, name, typ, remarks, applData,
            repository.id() + ":" + owner.trim() + "-" + name.trim() + "-" + typ.trim());
    }
}