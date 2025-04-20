package qmf.poc.service.jsonrpc.codec;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import qmf.poc.service.jsonrpc.messages.*;

public class JsonRPCCodec {
    public static final String JSON_RPC_VERSION = "2.0";

    public static String encodeBroadcast(String method, Object params) {
        JsonObject json = new JsonObject()
                .put("jsonrpc", JSON_RPC_VERSION)
                .put("method", method)
                .put("params", params);
        return json.encode();
    }

    public static String encodeRequest(long id, String method, Object params) {
        JsonObject json = new JsonObject()
                .put("jsonrpc", JSON_RPC_VERSION)
                .put("method", method)
                .put("params", params)
                .put("id", id);
        return json.encode();
    }

    public static String encodeResponse(long id, Object result) {
        JsonObject json = new JsonObject()
                .put("jsonrpc", JSON_RPC_VERSION)
                .put("result", result)
                .put("id", id);
        return json.encode();
    }

    public static String encodeError(long id, int code, String message, Object data) {
        JsonObject json = new JsonObject()
                .put("jsonrpc", JSON_RPC_VERSION)
                .put("error", new JsonObject()
                        .put("code", code)
                        .put("message", message)
                        .put("data", data))
                .put("id", id);
        return json.encode();
    }

    public static String encode(JsonRPCMessage message) throws JsonRPCEncodeError {
        return switch (message) {
            case JsonRPCBroadcast broadcast -> encodeBroadcast(broadcast.method, broadcast.params);
            case JsonRPCRequest request -> encodeRequest(request.id, request.method, request.params);
            case JsonRPCResult response -> encodeResponse(response.id, response.result);
            case JsonRPCError error -> encodeError(error.id, error.error.code, error.error.message, error.error.data);
            default -> throw new JsonRPCEncodeError("Unexpected value (should never happen): " + message);
        };
    }

    public static JsonRPCMessage decode(String jsonString) throws JsonRPCDecodeError {
        JsonObject json ;
        try {
            json = new JsonObject(jsonString);
        }catch (DecodeException e) {
            throw new JsonRPCDecodeError("Can't parse message: " + jsonString, e);
        }
        if (json.getString("jsonrpc") == null) {
            throw new JsonRPCDecodeError("Invalid JSON-RPC message: " + jsonString);
        }
        if (json.getString("method") != null) {
            Long id = json.getLong("id");
            return (id == null)
                    ? new JsonRPCBroadcast(json.getString("method"), json.getValue("params"))
                    : new JsonRPCRequest(id, json.getString("method"), json.getValue("params"));
        }
        Long id = json.getLong("id");
        if (id == null) {
            throw new JsonRPCDecodeError("Invalid JSON-RPC message: " + jsonString);
        }
        if (json.containsKey("error")) {
            final JsonObject error = json.getJsonObject("error");
            JsonRPCError.Error decodedError = new JsonRPCError.Error(
                    error.getInteger("code"),
                    error.getString("message"),
                    error.getValue("data")
            );
            return new JsonRPCError(id, decodedError);
        }
        if (json.containsKey("result")) {
            return new JsonRPCResult(id, json.getValue("result"));
        }
        throw new JsonRPCDecodeError("Invalid JSON-RPC message: " + jsonString);
    }
}
