package qmf.poc.service.qmf.index.models;

import java.util.List;

public record QMFObjectDocument(
        String agentId,
        String owner,
        String name,
        String typ,
        String remarks,
        String body,
        String id
) {
    public QMFObjectDocument(
            String agentId,
            String owner,
            String name,
            String typ,
            String remarks,
            String body
    ) {
        this(agentId, owner, name, typ, remarks, body,
                agentId.trim() + "-" + owner.trim() + "-" + name.trim() + "-" + typ.trim());
    }
}