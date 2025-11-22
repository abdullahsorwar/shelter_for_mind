package com.the_pathfinders.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KeeperRepository {
    
    /**
     * Status of a keeper signup request:
     * - PENDING: Email verified, waiting for admin approval
     * - APPROVED: Admin approved, keeper can login
     * - REJECTED: Admin rejected the request
     */
    public enum KeeperStatus {
        PENDING, APPROVED, REJECTED
    }
    
    public static class KeeperSignupRequest {
        public String keeperId;
        public String email;
        public String passwordHash;
        public boolean emailVerified;
        public KeeperStatus status;
        public LocalDateTime createdAt;
        public LocalDateTime approvedAt;
        public String approvedBy; // keeper_id of the approving admin
        
        public KeeperSignupRequest() {}
    }
    
    public static class KeeperProfile {
        public String keeperId;
        public String email;
        public String shortName;
        public String phone;
        public String countryCode;
        public String bloodGroup;
        
        public KeeperProfile() {}
    }
    
    /**
     * Create a new keeper signup request
     */
    public static void createSignupRequest(String keeperId, String email, String passwordHash) throws SQLException {
        String sql = """
            INSERT INTO keeper_signups (keeper_id, email, password_hash, email_verified, status, created_at)
            VALUES (?, ?, ?, false, 'PENDING', now())
            ON CONFLICT (keeper_id) DO UPDATE 
            SET email = EXCLUDED.email,
                password_hash = EXCLUDED.password_hash,
                email_verified = false,
                status = 'PENDING',
                created_at = now()
        """;
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            ps.setString(2, email.toLowerCase());
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }
    
    /**
     * Update email verification status for a keeper signup
     */
    public static void updateEmailVerified(String keeperId, boolean verified) throws SQLException {
        String sql = "UPDATE keeper_signups SET email_verified = ? WHERE keeper_id = ?";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, verified);
            ps.setString(2, keeperId.toLowerCase());
            ps.executeUpdate();
        }
    }
    
    /**
     * Check if a keeper ID is already taken
     */
    public static boolean isKeeperIdExists(String keeperId) throws SQLException {
        String sql = """
            SELECT EXISTS(
                SELECT 1 FROM keeper_signups WHERE keeper_id = ?
                UNION
                SELECT 1 FROM keepers WHERE keeper_id = ?
            )
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            ps.setString(2, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
    
    /**
     * Check if an email is already registered
     */
    public static boolean isEmailExists(String email) throws SQLException {
        String sql = """
            SELECT EXISTS(
                SELECT 1 FROM keeper_signups WHERE email = ?
                UNION
                SELECT 1 FROM keepers WHERE email = ?
            )
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            ps.setString(2, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
    
    /**
     * Get signup request details
     */
    public static KeeperSignupRequest getSignupRequest(String keeperId) throws SQLException {
        String sql = "SELECT * FROM keeper_signups WHERE keeper_id = ?";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KeeperSignupRequest request = new KeeperSignupRequest();
                    request.keeperId = rs.getString("keeper_id");
                    request.email = rs.getString("email");
                    request.passwordHash = rs.getString("password_hash");
                    request.emailVerified = rs.getBoolean("email_verified");
                    request.status = KeeperStatus.valueOf(rs.getString("status"));
                    request.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    
                    Timestamp approvedTs = rs.getTimestamp("approved_at");
                    if (approvedTs != null) {
                        request.approvedAt = approvedTs.toLocalDateTime();
                    }
                    request.approvedBy = rs.getString("approved_by");
                    
                    return request;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all pending signup requests (email verified, waiting for admin approval)
     */
    public static List<KeeperSignupRequest> getPendingSignups() throws SQLException {
        List<KeeperSignupRequest> requests = new ArrayList<>();
        String sql = """
            SELECT * FROM keeper_signups 
            WHERE email_verified = true AND status = 'PENDING'
            ORDER BY created_at ASC
        """;
        
        try (Connection c = DB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                KeeperSignupRequest request = new KeeperSignupRequest();
                request.keeperId = rs.getString("keeper_id");
                request.email = rs.getString("email");
                request.passwordHash = rs.getString("password_hash");
                request.emailVerified = rs.getBoolean("email_verified");
                request.status = KeeperStatus.valueOf(rs.getString("status"));
                request.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                
                requests.add(request);
            }
        }
        return requests;
    }
    
    /**
     * Approve a keeper signup request and move to keepers table
     */
    public static void approveSignup(String keeperId, String approvedByKeeperId) throws SQLException {
        Connection c = null;
        try {
            c = DB.getConnection();
            c.setAutoCommit(false);
            
            // Get signup details
            String selectSql = "SELECT email, password_hash FROM keeper_signups WHERE keeper_id = ?";
            String email, passwordHash;
            
            try (PreparedStatement ps = c.prepareStatement(selectSql)) {
                ps.setString(1, keeperId.toLowerCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Signup request not found");
                    }
                    email = rs.getString("email");
                    passwordHash = rs.getString("password_hash");
                }
            }
            
            // Insert into keepers table
            String insertSql = """
                INSERT INTO keepers (keeper_id, email, password_hash, approved_at, approved_by, created_at)
                VALUES (?, ?, ?, now(), ?, now())
            """;
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                ps.setString(1, keeperId.toLowerCase());
                ps.setString(2, email);
                ps.setString(3, passwordHash);
                ps.setString(4, approvedByKeeperId);
                ps.executeUpdate();
            }
            
            // Update signup status
            String updateSql = """
                UPDATE keeper_signups 
                SET status = 'APPROVED', approved_at = now(), approved_by = ?
                WHERE keeper_id = ?
            """;
            try (PreparedStatement ps = c.prepareStatement(updateSql)) {
                ps.setString(1, approvedByKeeperId);
                ps.setString(2, keeperId.toLowerCase());
                ps.executeUpdate();
            }
            
            c.commit();
        } catch (SQLException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (c != null) {
                c.setAutoCommit(true);
                c.close();
            }
        }
    }
    
    /**
     * Authenticate a keeper for login
     */
    public static boolean authenticateKeeper(String keeperId, String password) throws SQLException {
        String sql = "SELECT password_hash FROM keepers WHERE keeper_id = ?";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    return BCrypt.checkpw(password, storedHash);
                }
            }
        }
        return false;
    }
    
    /**
     * Update last login timestamp for a keeper
     */
    public static void updateLastLogin(String keeperId) throws SQLException {
        String sql = "UPDATE keepers SET last_login = NOW() WHERE keeper_id = ?";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            ps.executeUpdate();
            System.out.println("Updated last login for keeper: " + keeperId);
        }
    }
    
    /**
     * Check if keeper exists and is approved
     */
    public static boolean isKeeperApproved(String keeperId) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM keepers WHERE keeper_id = ?)";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
    
    /**
     * Simple BCrypt wrapper for password hashing
     */
    public static class BCrypt {
        public static String hashpw(String password, String salt) {
            // Using Java's built-in MessageDigest for simplicity
            // In production, use a proper BCrypt library
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest((password + salt).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return bytesToHex(hash);
            } catch (Exception e) {
                throw new RuntimeException("Failed to hash password", e);
            }
        }
        
        public static String gensalt() {
            return java.util.UUID.randomUUID().toString();
        }
        
        public static boolean checkpw(String password, String hash) {
            // For now, simple comparison
            // In production, use proper BCrypt verification
            return hash.equals(hashPassword(password));
        }
        
        private static String bytesToHex(byte[] bytes) {
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        }
    }
    
    /**
     * Hash a password using SHA-256 (simplified version)
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    /**
     * Get keeper profile information
     */
    public static KeeperProfile getKeeperProfile(String keeperId) throws SQLException {
        String sql = """
            SELECT k.keeper_id, k.email, k.short_name, k.phone, k.country_code, k.blood_group
            FROM keepers k
            WHERE k.keeper_id = ?
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KeeperProfile profile = new KeeperProfile();
                    profile.keeperId = rs.getString("keeper_id");
                    profile.email = rs.getString("email");
                    profile.shortName = rs.getString("short_name");
                    profile.phone = rs.getString("phone");
                    profile.countryCode = rs.getString("country_code");
                    profile.bloodGroup = rs.getString("blood_group");
                    return profile;
                }
            }
        }
        
        // If no profile found, return basic info with email only
        KeeperProfile profile = new KeeperProfile();
        profile.keeperId = keeperId;
        profile.email = getKeeperEmail(keeperId);
        return profile;
    }
    
    /**
     * Get keeper email
     */
    public static String getKeeperEmail(String keeperId) throws SQLException {
        String sql = "SELECT email FROM keepers WHERE keeper_id = ?";
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, keeperId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return "";
    }
    
    /**
     * Update keeper profile information
     */
    public static void updateKeeperProfile(KeeperProfile profile) throws SQLException {
        String sql = """
            UPDATE keepers
            SET short_name = ?, phone = ?, country_code = ?, blood_group = ?
            WHERE keeper_id = ?
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, profile.shortName);
            ps.setString(2, profile.phone);
            ps.setString(3, profile.countryCode);
            ps.setString(4, profile.bloodGroup);
            ps.setString(5, profile.keeperId.toLowerCase());
            
            int rowsUpdated = ps.executeUpdate();
            System.out.println("Updated keeper profile for: " + profile.keeperId + " (rows: " + rowsUpdated + ")");
        }
    }
    
    /**
     * Soul information with activity status
     */
    public static class SoulInfo {
        public String soulId;
        public String soulName;
        public LocalDateTime lastActivity;
        public boolean isActive; // Active if last_activity is within 15 minutes
        
        public SoulInfo() {}
    }
    
    /**
     * Get all souls with their activity status
     */
    public static List<SoulInfo> getAllSouls() throws SQLException {
        String sql = """
            SELECT 
                soul_id, 
                soul_name, 
                last_activity,
                CASE 
                    WHEN last_activity IS NOT NULL 
                         AND last_activity >= (NOW() - INTERVAL '5 minute')
                    THEN true 
                    ELSE false 
                END as is_active
            FROM soul_id_and_soul_key
            ORDER BY last_activity DESC NULLS LAST, soul_id ASC
        """;
        
        List<SoulInfo> souls = new ArrayList<>();
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                SoulInfo soul = new SoulInfo();
                soul.soulId = rs.getString("soul_id");
                soul.soulName = rs.getString("soul_name");
                
                Timestamp ts = rs.getTimestamp("last_activity");
                soul.lastActivity = ts != null ? ts.toLocalDateTime() : null;
                soul.isActive = rs.getBoolean("is_active");
                
                souls.add(soul);
            }
        }
        
        return souls;
    }
    
    /**
     * Password reset token information
     */
    public static class PasswordResetToken {
        public String token;
        public String keeperId;
        public LocalDateTime expiresAt;
        public boolean used;
        
        public PasswordResetToken() {}
    }
    
    /**
     * Generate and store a password reset token for a keeper
     */
    public static String createPasswordResetToken(String keeperId) throws SQLException {
        // Generate random token
        String token = java.util.UUID.randomUUID().toString();
        
        // Token expires in 1 hour
        String sql = """
            INSERT INTO keeper_password_resets (token, keeper_id, expires_at, used, created_at)
            VALUES (?, ?, NOW() + INTERVAL '1 hour', false, NOW())
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, keeperId.toLowerCase());
            ps.executeUpdate();
        }
        
        return token;
    }
    
    /**
     * Validate a password reset token
     */
    public static PasswordResetToken validateResetToken(String token) throws SQLException {
        String sql = """
            SELECT token, keeper_id, expires_at, used
            FROM keeper_password_resets
            WHERE token = ? AND used = false AND expires_at > NOW()
        """;
        
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.token = rs.getString("token");
                    resetToken.keeperId = rs.getString("keeper_id");
                    resetToken.expiresAt = rs.getTimestamp("expires_at").toLocalDateTime();
                    resetToken.used = rs.getBoolean("used");
                    return resetToken;
                }
            }
        }
        
        return null; // Token invalid or expired
    }
    
    /**
     * Reset keeper password using a valid token
     */
    public static boolean resetPassword(String token, String newPasswordHash) throws SQLException {
        PasswordResetToken resetToken = validateResetToken(token);
        if (resetToken == null) {
            return false;
        }
        
        Connection c = null;
        try {
            c = DB.getConnection();
            c.setAutoCommit(false);
            
            // Update password in keepers table
            String updatePasswordSql = "UPDATE keepers SET password_hash = ? WHERE keeper_id = ?";
            try (PreparedStatement ps = c.prepareStatement(updatePasswordSql)) {
                ps.setString(1, newPasswordHash);
                ps.setString(2, resetToken.keeperId);
                ps.executeUpdate();
            }
            
            // Mark token as used
            String markUsedSql = "UPDATE keeper_password_resets SET used = true WHERE token = ?";
            try (PreparedStatement ps = c.prepareStatement(markUsedSql)) {
                ps.setString(1, token);
                ps.executeUpdate();
            }
            
            c.commit();
            return true;
            
        } catch (SQLException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (c != null) {
                c.setAutoCommit(true);
                c.close();
            }
        }
    }
    
}
