package com.the_pathfinders.verification;

import com.the_pathfinders.db.SoulInfoRepository;

import java.io.IOException;
import java.sql.SQLException;

public class VerificationManager {
    private static VerificationManager instance;
    private VerificationServer httpServer;
    private VerificationWebSocketServer wsServer;
    private boolean isRunning = false;

    private VerificationManager() {}

    public static synchronized VerificationManager getInstance() {
        if (instance == null) {
            instance = new VerificationManager();
        }
        return instance;
    }

    public void start() throws IOException {
        if (isRunning) {
            System.out.println("Verification manager already running");
            return;
        }

        // Start HTTP server
        httpServer = new VerificationServer((soulId, token) -> {
            System.out.println("Verification callback triggered for soul_id: " + soulId);
            
            // Update database
            try {
                SoulInfoRepository.updateEmailVerified(soulId, true);
                System.out.println("Database updated: email verified for soul_id: " + soulId);
            } catch (SQLException e) {
                System.err.println("Failed to update database: " + e.getMessage());
                e.printStackTrace();
            }

            // Notify WebSocket clients
            if (wsServer != null) {
                wsServer.notifyVerified(soulId);
            }
        });
        httpServer.start();

        // Start WebSocket server
        wsServer = new VerificationWebSocketServer();
        wsServer.start();

        isRunning = true;
        System.out.println("Verification manager started successfully");
    }

    public void stop() {
        if (!isRunning) {
            return;
        }

        if (httpServer != null) {
            httpServer.stop();
        }
        if (wsServer != null) {
            try {
                wsServer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping WebSocket server: " + e.getMessage());
            }
        }

        isRunning = false;
        System.out.println("Verification manager stopped");
    }

    public void sendVerificationEmail(String soulId, String email) throws Exception {
        if (!isRunning) {
            start();
        }

        // Generate token
        String token = EmailService.generateVerificationToken(soulId);
        
        // Register with HTTP server
        httpServer.registerVerification(token, soulId);

        // Send email
        EmailService.sendVerificationEmail(email, soulId, token);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
