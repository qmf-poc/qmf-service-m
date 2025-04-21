package qmf.poc.service.qmf.catalog;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Nullable;

public record QMFObjectCatalog(
        String owner,
        String name,
        String type,
        String subType,
        int objectLevel,
        String restricted,
        String model,
        String created,
        String modified,
        String lastUsed,
        String appldata,
        String remarks
) {
    @Nullable
    public static QMFObjectCatalog fromMap(JsonObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        try {
            return new QMFObjectCatalog(
                    jsonObject.getString("owner"),
                    jsonObject.getString("name"),
                    jsonObject.getString("type"),
                    jsonObject.getString("subType"),
                    jsonObject.getInteger("objectLevel"),
                    jsonObject.getString("restricted"),
                    jsonObject.getString("model"),
                    jsonObject.getString("created"),
                    jsonObject.getString("modified"),
                    jsonObject.getString("lastUsed"),
                    jsonObject.getString("appldata"),
                    jsonObject.getString("remarks")
            );
        }catch (Exception e) {
            return null;
        }
    }
}
