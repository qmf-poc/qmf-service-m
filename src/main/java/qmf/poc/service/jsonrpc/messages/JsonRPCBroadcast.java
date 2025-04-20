package qmf.poc.service.jsonrpc.messages;

public final class JsonRPCBroadcast extends JsonRPCMessage {
    public final String method;
    public final Object params;

    public JsonRPCBroadcast(String method, Object params) {
        this.method = method;
        this.params = params;
    }
}
