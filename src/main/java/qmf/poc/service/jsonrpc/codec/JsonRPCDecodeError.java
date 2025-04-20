package qmf.poc.service.jsonrpc.codec;

public class JsonRPCDecodeError extends Exception {
    public JsonRPCDecodeError(String message) {
        super(message);
    }
    public JsonRPCDecodeError(String message, Throwable cause) {
        super(message, cause);
    }
}
