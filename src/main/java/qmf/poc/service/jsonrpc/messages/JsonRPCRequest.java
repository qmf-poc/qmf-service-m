package qmf.poc.service.jsonrpc.messages;

public final class JsonRPCRequest extends JsonRPCMessage {
    public final long id;
    public final String method;
    public final Object params;

    public JsonRPCRequest(long id, String method, Object params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    @Override
    public String toString() {
        return "JsonRPCRequest{" +
                "id=" + id +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }
}
