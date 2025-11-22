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

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/verify", new VerificationHandler());
            server.createContext("/verify-keeper", new KeeperVerificationHandler());
            server.setExecutor(null); // Use default executor
            server.start();
            System.out.println("Verification server started on port " + PORT);
        } catch (java.net.BindException e) {
            System.err.println("Port " + PORT + " is already in use. Server may already be running.");
            // Port is in use, likely from a previous instance. This is acceptable.
            // We'll treat this as if the server is already running
            throw new IOException("Port " + PORT + " already in use", e);
        }
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
    
    private class KeeperVerificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            
            String token = params.get("token");
            String keeperId = params.get("keeper_id");

            String responseHtml;
            int statusCode;

            if (token != null && keeperId != null && pendingVerifications.containsKey(token)) {
                String expectedKeeperId = pendingVerifications.get(token);
                
                if (expectedKeeperId.equals(keeperId)) {
                    // Check if already verified recently
                    Long lastVerified = recentlyVerified.get(keeperId);
                    long now = System.currentTimeMillis();
                    
                    if (lastVerified != null && (now - lastVerified) < 5000) {
                        System.out.println("Duplicate keeper verification request ignored for keeper_id: " + keeperId);
                        responseHtml = buildKeeperSuccessPage(keeperId);
                        statusCode = 200;
                    } else {
                        pendingVerifications.remove(token);
                        recentlyVerified.put(keeperId, now);
                        
                        // Update keeper email verification status
                        try {
                            com.the_pathfinders.db.KeeperRepository.updateEmailVerified(keeperId, true);
                            System.out.println("Keeper email verified: " + keeperId);
                            
                            // Notify existing keepers
                            com.the_pathfinders.verification.EmailService.notifyExistingKeepersOfNewSignup(
                                com.the_pathfinders.db.KeeperRepository.getSignupRequest(keeperId).email,
                                keeperId
                            );
                        } catch (Exception e) {
                            System.err.println("Failed to update keeper verification status: " + e.getMessage());
                            e.printStackTrace();
                        }

                        responseHtml = buildKeeperSuccessPage(keeperId);
                        statusCode = 200;
                        
                        recentlyVerified.entrySet().removeIf(entry -> (now - entry.getValue()) > 10000);
                    }
                } else {
                    responseHtml = buildKeeperErrorPage("Invalid verification link");
                    statusCode = 400;
                }
            } else {
                responseHtml = buildKeeperErrorPage("Invalid or expired verification link");
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

        private String buildKeeperSuccessPage(String keeperId) {
            return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Keeper Email Verified</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #FF9A8B 0%, #7CDFD9 100%); " +
                "margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; min-height: 100vh; }" +
                ".container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); " +
                "text-align: center; max-width: 500px; }" +
                "h1 { color: #FF7B7B; margin-bottom: 20px; }" +
                "p { color: #555; font-size: 18px; line-height: 1.6; }" +
                ".checkmark { font-size: 80px; color: #4CAF50; margin-bottom: 20px; }" +
                ".keeper-id { color: #7CDFD9; font-weight: bold; }" +
                ".info-box { background: #f8f9fa; padding: 20px; border-radius: 10px; margin-top: 20px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='checkmark'>✓</div>" +
                "<h1>Email Verified Successfully!</h1>" +
                "<p>Hello, aspiring keeper <span class='keeper-id'>" + keeperId + "</span>!</p>" +
                "<p>Your email has been verified successfully.</p>" +
                "<div class='info-box'>" +
                "<p style='margin: 0;'><strong>Next Steps:</strong></p>" +
                "<p style='margin-top: 10px;'>Existing keepers will review your request and you will receive a follow-up email once your account is approved.</p>" +
                "</div>" +
                "<p style='margin-top: 30px; color: #888; font-size: 14px;'>— The keepers of souls, shelter_of_mind</p>" +
                "</div></body></html>";
        }
        
        private String buildKeeperErrorPage(String errorMessage) {
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
