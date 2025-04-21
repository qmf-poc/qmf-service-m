package qmf.poc.service.jsonrpc.messages;

public final class JsonRPCError extends JsonRPCMessage {
    public final long id;
    public final Error error;

    public JsonRPCError(long id, Error error) {
        this.id = id;
        this.error = error;
    }

    public record Error(int code, String message, Object data) {
    }
}
