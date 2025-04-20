package qmf.poc.service.jsonrpc.messages;

public final class JsonRPCError extends JsonRPCMessage {
    public final long id;
    public final Error error;

    public JsonRPCError(long id, Error error) {
        this.id = id;
        this.error = error;
    }

    public static class Error {
        public final int code;
        public final String message;
        public final Object data;

        public Error(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
}
