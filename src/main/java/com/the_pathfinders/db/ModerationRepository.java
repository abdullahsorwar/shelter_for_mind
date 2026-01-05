package com.the_pathfinders.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ModerationRepository {
    
    /**
     * Represents a moderation message
     */
    public static class ModerationMessage {
        public int messageId;
        public int journalId;
        public String soulId;
        public String keeperId;
        public String messageContent;
        public boolean isRead;
        public LocalDateTime createdAt;
        public String journalTitle;  // For display purposes
        
        public ModerationMessage() {}
    }
    
    /**
     * Send a moderation message to a user about their journal post
     */
    public static int sendModerationMessage(String journalId, String soulId, String keeperId, String messageContent) throws SQLException {
        String sql = """
            INSERT INTO moderation_messages (journal_id, soul_id, keeper_id, message_content, is_read, created_at)
            VALUES (?, ?, ?, ?, false, NOW())
            RETURNING message_id
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Convert char(7) journal_id to int for foreign key
            ps.setInt(1, Integer.parseInt(journalId));
            ps.setString(2, soulId);
            ps.setString(3, keeperId);
            ps.setString(4, messageContent);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("message_id");
                }
                throw new SQLException("Failed to create moderation message");
            }
        }
    }
    
    /**
     * Get all messages for a specific user (soul)
     */
    public static List<ModerationMessage> getMessagesForSoul(String soulId) throws SQLException {
        String sql = """
            SELECT 
                mm.message_id,
                mm.journal_id,
                mm.soul_id,
                mm.keeper_id,
                mm.message_content,
                mm.is_read,
                mm.created_at
            FROM moderation_messages mm
            WHERE mm.soul_id = ?
            ORDER BY mm.created_at DESC
        """;
        
        List<ModerationMessage> messages = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, soulId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModerationMessage msg = new ModerationMessage();
                    msg.messageId = rs.getInt("message_id");
                    msg.journalId = rs.getInt("journal_id");
                    msg.soulId = rs.getString("soul_id");
                    msg.keeperId = rs.getString("keeper_id");
                    msg.messageContent = rs.getString("message_content");
                    msg.isRead = rs.getBoolean("is_read");
                    msg.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    // Set journalTitle to show journal ID since public_journals has no title
                    msg.journalTitle = "Journal #" + msg.journalId;
                    messages.add(msg);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Get count of unread messages for a user
     */
    public static int getUnreadMessageCount(String soulId) throws SQLException {
        String sql = """
            SELECT COUNT(*) as unread_count
            FROM moderation_messages
            WHERE soul_id = ? AND is_read = false
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, soulId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unread_count");
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Mark a message as read
     */
    public static void markMessageAsRead(int messageId) throws SQLException {
        String sql = """
            UPDATE moderation_messages
            SET is_read = true
            WHERE message_id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, messageId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Mark all messages as read for a user
     */
    public static void markAllMessagesAsRead(String soulId) throws SQLException {
        String sql = """
            UPDATE moderation_messages
            SET is_read = true
            WHERE soul_id = ? AND is_read = false
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, soulId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Get all public journals for moderation review
     */
    public static List<Journal> getPublicJournalsForModeration() throws SQLException {
        String sql = """
            SELECT 
                pj.journal_id,
                pj.soul_id,
                pj.journal_text,
                pj.love_count,
                pj.created_at,
                (SELECT COUNT(*) FROM moderation_messages mm WHERE mm.journal_id = pj.journal_id::int) as moderation_count
            FROM public_journals pj
            ORDER BY pj.created_at DESC
        """;
        
        List<Journal> journals = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Journal journal = new Journal();
                journal.journalId = rs.getString("journal_id");
                journal.soulId = rs.getString("soul_id");
                journal.content = rs.getString("journal_text");
                journal.loveCount = rs.getInt("love_count");
                journal.moderationCount = rs.getInt("moderation_count");
                journal.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                journals.add(journal);
            }
        }
        
        return journals;
    }
    
    /**
     * Get moderation history for a specific journal
     */
    public static List<ModerationMessage> getModerationHistoryForJournal(String journalId) throws SQLException {
        String sql = """
            SELECT 
                mm.message_id,
                mm.journal_id,
                mm.soul_id,
                mm.keeper_id,
                mm.message_content,
                mm.is_read,
                mm.created_at,
                k.short_name
            FROM moderation_messages mm
            JOIN keepers k ON mm.keeper_id = k.keeper_id
            WHERE mm.journal_id = ?
            ORDER BY mm.created_at DESC
        """;
        
        List<ModerationMessage> messages = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Convert char(7) journal_id to int
            ps.setInt(1, Integer.parseInt(journalId));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ModerationMessage msg = new ModerationMessage();
                    msg.messageId = rs.getInt("message_id");
                    msg.journalId = rs.getInt("journal_id");
                    msg.soulId = rs.getString("soul_id");
                    msg.keeperId = rs.getString("keeper_id");
                    msg.messageContent = rs.getString("message_content");
                    msg.isRead = rs.getBoolean("is_read");
                    msg.createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    messages.add(msg);
                }
            }
        }
        
        return messages;
    }
    
    /**
     * Inner class to represent a Journal for moderation purposes
     */
    public static class Journal {
        public String journalId;  // char(7) in public_journals
        public String soulId;
        public String content;
        public int loveCount;
        public LocalDateTime createdAt;
        public int moderationCount;  // Count of moderation messages sent
        
        public Journal() {}
    }
}
