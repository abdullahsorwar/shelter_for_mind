package com.the_pathfinders.util;

import com.the_pathfinders.db.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Utility class for tracking user activity in real-time.
 * Updates last_activity timestamp whenever a user performs an action.
 */
public class ActivityTracker {
    
    /**
     * Update the last_activity timestamp for a soul_id to NOW().
     * This should be called whenever a user performs any action in the app.
     * 
     * @param soulId The soul_id of the active user
     */
    public static void updateActivity(String soulId) {
        if (soulId == null || soulId.trim().isEmpty()) {
            return;
        }
        
        // Run in background to not block UI
        new Thread(() -> {
            try {
                String sql = "UPDATE soul_id_and_soul_key SET last_activity = NOW() WHERE soul_id = ?";
                try (Connection conn = DB.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, soulId.toLowerCase());
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                // Silently fail - activity tracking shouldn't break the app
                System.err.println("Failed to update activity for " + soulId + ": " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Update activity synchronously (blocks until complete).
     * Use this for critical operations like login.
     * 
     * @param soulId The soul_id of the active user
     */
    public static void updateActivitySync(String soulId) {
        if (soulId == null || soulId.trim().isEmpty()) {
            return;
        }
        
        try {
            String sql = "UPDATE soul_id_and_soul_key SET last_activity = NOW() WHERE soul_id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, soulId.toLowerCase());
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Failed to update activity for " + soulId + ": " + e.getMessage());
        }
    }
}
