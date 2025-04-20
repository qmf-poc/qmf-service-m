package qmf.poc.service.jsonrpc.messages;

public final class JsonRPCResult extends JsonRPCMessage {
    public final long id;
    public final Object result;

    public JsonRPCResult(long id, Object result) {
        this.id = id;
        this.result = result;
    }
}
