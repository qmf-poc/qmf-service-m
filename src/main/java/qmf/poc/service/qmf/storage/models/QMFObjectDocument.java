package qmf.poc.service.qmf.storage.models;

public record QMFObjectDocument(
        String agentId,
        String owner,
        String name,
        String typ,
        String remarks,
        String applData,
        String id
) {
    public QMFObjectDocument(String agentId, String owner, String name, String typ, String remarks, String applData) {
        this(agentId, owner, name, typ, remarks, applData,
                agentId.trim() + "-" + owner.trim() + "-" + name.trim() + "-" + typ.trim());
    }
}