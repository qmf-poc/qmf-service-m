package qmf.poc.service.jsonrpc;

import qmf.poc.service.jsonrpc.messages.JsonRPCError;
import qmf.poc.service.jsonrpc.messages.JsonRPCRequest;

public class JsonRPCException extends Exception {
    public final JsonRPCError.Error error;
    public final JsonRPCRequest request;

    public JsonRPCException(JsonRPCRequest request, JsonRPCError.Error error) {
        super("JsonRPC Error: (" + error.code + ") " +
                error.message + (error.data != null ? " - " +
                error.data : "") + ". Request: " + request.toString());
        this.error = error;
        this.request = request;
    }
}
