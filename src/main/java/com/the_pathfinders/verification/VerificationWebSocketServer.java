package com.the_pathfinders.verification;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationWebSocketServer extends WebSocketServer {
    private static final int PORT = 8081;
    private Map<String, WebSocket> soulConnections = new ConcurrentHashMap<>();

    public VerificationWebSocketServer() {
        super(new InetSocketAddress(PORT));
        System.out.println("WebSocket server created on port " + PORT);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String soulId = handshake.getResourceDescriptor().replace("/", "");
        if (!soulId.isEmpty()) {
            soulConnections.put(soulId, conn);
            System.out.println("WebSocket connection opened for soul_id: " + soulId);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        // Remove connection
        soulConnections.entrySet().removeIf(entry -> entry.getValue() == conn);
        System.out.println("WebSocket connection closed");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("WebSocket message received: " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started on port " + PORT);
    }

    public void notifyVerified(String soulId) {
        WebSocket conn = soulConnections.get(soulId);
        if (conn != null && conn.isOpen()) {
            conn.send("VERIFIED");
            System.out.println("Sent verification notification to soul_id: " + soulId);
        }
    }
}
