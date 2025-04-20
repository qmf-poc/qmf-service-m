package qmf.poc.service.jsonrpc.messages;

sealed public class JsonRPCMessage permits JsonRPCRequest, JsonRPCResult, JsonRPCError, JsonRPCBroadcast {}
