package com.the_pathfinders.verification;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VerificationServer {
    private HttpServer server;
    private static final int PORT = 8080;
    private Map<String, String> pendingVerifications = new ConcurrentHashMap<>();
    private Map<String, Long> recentlyVerified = new ConcurrentHashMap<>(); // Track recently verified to prevent duplicates
    private VerificationCallback callback;

    public interface VerificationCallback {
        void onVerified(String soulId, String token);
    }

    public VerificationServer(VerificationCallback callback) {
        this.callback = callback;
    }

    public void start() throws IOException {
        if (server != null) {
            System.out.println("Verification server already running");
            return;
        }

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/verify", new VerificationHandler());
        server.setExecutor(null); // Use default executor
        server.start();
        System.out.println("Verification server started on port " + PORT);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Verification server stopped");
        }
    }

    public void registerVerification(String token, String soulId) {
        pendingVerifications.put(token, soulId);
        System.out.println("Registered verification token for soul_id: " + soulId);
    }

    private class VerificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String token = params.get("token");
            String soulId = params.get("soul_id");

            String responseHtml;
            int statusCode;

            if (token != null && soulId != null && pendingVerifications.containsKey(token)) {
                String expectedSoulId = pendingVerifications.get(token);
                
                if (expectedSoulId.equals(soulId)) {
                    // Check if already verified recently (within 5 seconds) to prevent duplicate verifications
                    Long lastVerified = recentlyVerified.get(soulId);
                    long now = System.currentTimeMillis();
                    
                    if (lastVerified != null && (now - lastVerified) < 5000) {
                        // Already verified recently, don't process again
                        System.out.println("Duplicate verification request ignored for soul_id: " + soulId);
                        responseHtml = buildSuccessPage(soulId);
                        statusCode = 200;
                    } else {
                        // Valid verification - first time or after cooldown
                        pendingVerifications.remove(token);
                        recentlyVerified.put(soulId, now);
                        
                        // Notify callback
                        if (callback != null) {
                            callback.onVerified(soulId, token);
                        }

                        responseHtml = buildSuccessPage(soulId);
                        statusCode = 200;
                        
                        // Clean up old entries in recentlyVerified (older than 10 seconds)
                        recentlyVerified.entrySet().removeIf(entry -> (now - entry.getValue()) > 10000);
                    }
                } else {
                    responseHtml = buildErrorPage("Invalid verification link");
                    statusCode = 400;
                }
            } else {
                responseHtml = buildErrorPage("Invalid or expired verification link");
                statusCode = 400;
            }

            byte[] response = responseHtml.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return params;
        }

        private String buildSuccessPage(String soulId) {
            return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Email Verified</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; min-height: 100vh; }" +
                ".container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); " +
                "text-align: center; max-width: 500px; }" +
                "h1 { color: #667eea; margin-bottom: 20px; }" +
                "p { color: #555; font-size: 18px; line-height: 1.6; }" +
                ".checkmark { font-size: 80px; color: #4CAF50; margin-bottom: 20px; }" +
                ".soul-id { color: #764ba2; font-weight: bold; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='checkmark'>✓</div>" +
                "<h1>Email Verified Successfully!</h1>" +
                "<p>Hello, <span class='soul-id'>" + soulId + "</span>!</p>" +
                "<p>Your email has been verified successfully. You can now close this window and return to the application.</p>" +
                "<p style='margin-top: 30px; color: #888; font-size: 14px;'>— The keepers of your soul, shelter_of_mind</p>" +
                "</div></body></html>";
        }

        private String buildErrorPage(String errorMessage) {
            return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Verification Failed</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); " +
                "margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; min-height: 100vh; }" +
                ".container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); " +
                "text-align: center; max-width: 500px; }" +
                "h1 { color: #f5576c; margin-bottom: 20px; }" +
                "p { color: #555; font-size: 18px; line-height: 1.6; }" +
                ".error-icon { font-size: 80px; color: #f5576c; margin-bottom: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='error-icon'>✗</div>" +
                "<h1>Verification Failed</h1>" +
                "<p>" + errorMessage + "</p>" +
                "<p>Please try again or contact support.</p>" +
                "</div></body></html>";
        }
    }
}
