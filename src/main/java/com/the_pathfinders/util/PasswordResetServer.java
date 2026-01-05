package com.the_pathfinders.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import com.the_pathfinders.App;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class PasswordResetServer {
    private static HttpServer server;
    private static final int PORT = 8081; // Changed from 8080 to avoid conflict with VerificationServer
    
    public static void start() {
        if (server != null) {
            return; // Already running
        }
        
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/reset-password", new PasswordResetHandler());
            server.setExecutor(null); // Use default executor
            server.start();
            System.out.println("Password reset server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start password reset server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Password reset server stopped");
        }
    }
    
    static class PasswordResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);
            String token = params.get("token");
            
            if (token == null || token.isEmpty()) {
                sendResponse(exchange, 400, "Invalid reset link");
                return;
            }
            
            // Send HTML response
            String response = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Password Reset</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: linear-gradient(135deg, #FFE5E5, #E0F2FE);
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                        }
                        .container {
                            background: white;
                            padding: 40px;
                            border-radius: 20px;
                            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
                            text-align: center;
                            max-width: 400px;
                        }
                        h1 {
                            color: #FF7B7B;
                            margin-bottom: 20px;
                        }
                        p {
                            color: #6B7280;
                            margin-bottom: 30px;
                        }
                        .info {
                            background: #F3F4F6;
                            padding: 15px;
                            border-radius: 10px;
                            margin-top: 20px;
                            color: #374151;
                        }
                    </style>
                    <script>
                        setTimeout(function() {
                            window.close();
                        }, 3000);
                    </script>
                </head>
                <body>
                    <div class="container">
                        <h1>Password Reset</h1>
                        <p>Please return to the application to complete your password reset.</p>
                        <div class="info">
                            The password reset form has been opened in the application.
                            <br><br>
                            This window will close automatically in 3 seconds.
                        </div>
                    </div>
                </body>
                </html>
                """;
            
            sendResponse(exchange, 200, response);
            
            // Open password reset page in JavaFX application
            Platform.runLater(() -> {
                App.showPasswordResetPage(token);
            });
        }
        
        private Map<String, String> parseQuery(String query) {
            Map<String, String> result = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length > 1) {
                        result.put(keyValue[0], keyValue[1]);
                    } else if (keyValue.length == 1) {
                        result.put(keyValue[0], "");
                    }
                }
            }
            return result;
        }
        
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
