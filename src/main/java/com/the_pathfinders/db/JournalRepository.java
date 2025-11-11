package com.the_pathfinders.db;

import com.the_pathfinders.Journal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JournalRepository {

    /**
     * Get the next journal ID in descending format (starting from 0000000).
     * Fetches the highest ID from the database and increments it.
     */
    public String getNextJournalId() throws SQLException {
        String sql = "SELECT journal_id FROM public_journals ORDER BY journal_id DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                String lastId = rs.getString("journal_id");
                // Parse the numeric part and increment
                int num = Integer.parseInt(lastId);
                int nextNum = num + 1;
                // Format back to 7-digit string with leading zeros
                return String.format("%07d", nextNum);
            } else {
                // No journals yet, start from 0000000
                return "0000000";
            }
        }
    }

    /**
     * Save a journal entry to the database.
     * @param soulId The ID of the user creating the journal
     * @param journalText The journal content text
     * @param fontFamily The font family used when writing
     * @param fontSize The font size used when writing
     * @param isPublic Whether the journal is public or private
     * @return The generated journal ID
     */
    public String saveJournal(String soulId, String journalText, String fontFamily, Integer fontSize, boolean isPublic) throws SQLException {
        String journalId = getNextJournalId();
        
        String sql = "INSERT INTO public_journals (journal_id, soul_id, journal_text, love_count, font_family, font_size, is_public) VALUES (?, ?, ?, 0, ?, ?, ?)";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, journalId);
            ps.setString(2, soulId);
            ps.setString(3, journalText);
            ps.setString(4, fontFamily);
            ps.setInt(5, fontSize);
            ps.setBoolean(6, isPublic);
            ps.executeUpdate();
            
            return journalId;
        }
    }

    /**
     * Load a journal entry by ID.
     */
    public Journal getJournalById(String journalId) throws SQLException {
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, loved_by, font_family, font_size, created_at, is_public FROM public_journals WHERE journal_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, journalId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    journal.setLoveCount(rs.getInt("love_count"));
                    Array lovedArr = rs.getArray("loved_by");
                    if (lovedArr != null) journal.setLovedBy((String[]) lovedArr.getArray());
                    journal.setFontFamily(rs.getString("font_family"));
                    journal.setFontSize(rs.getInt("font_size"));
                    journal.setIsPublic(rs.getBoolean("is_public"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        journal.setCreatedAt(ts.toLocalDateTime());
                        journal.setEntryDate(ts.toLocalDateTime().toLocalDate());
                    }
                    return journal;
                }
            }
        }
        return null;
    }

    /**
     * Update an existing journal's text, style properties, and visibility.
     * @return true if update affected a row
     */
    public boolean updateJournal(String journalId, String journalText, String fontFamily, Integer fontSize, boolean isPublic) throws SQLException {
        String sql = "UPDATE public_journals SET journal_text = ?, font_family = ?, font_size = ?, is_public = ? WHERE journal_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, journalText);
            ps.setString(2, fontFamily);
            ps.setInt(3, fontSize);
            ps.setBoolean(4, isPublic);
            ps.setString(5, journalId);
            int updated = ps.executeUpdate();
            return updated > 0;
        }
    }

    /**
     * Load the latest journal entry for a specific user.
     */
    public Journal getLatestJournalForUser(String soulId) throws SQLException {
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, loved_by, font_family, font_size, created_at, is_public FROM public_journals WHERE soul_id = ? ORDER BY journal_id DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soulId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    journal.setLoveCount(rs.getInt("love_count"));
                    Array lovedArr = rs.getArray("loved_by");
                    if (lovedArr != null) journal.setLovedBy((String[]) lovedArr.getArray());
                    journal.setFontFamily(rs.getString("font_family"));
                    journal.setFontSize(rs.getInt("font_size"));
                    journal.setIsPublic(rs.getBoolean("is_public"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        journal.setCreatedAt(ts.toLocalDateTime());
                        journal.setEntryDate(ts.toLocalDateTime().toLocalDate());
                    }
                    return journal;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all public journals ordered by creation time (newest first).
     */
    public List<Journal> getAllPublicJournals() throws SQLException {
        List<Journal> journals = new ArrayList<>();
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, loved_by, font_family, font_size, created_at, is_public FROM public_journals WHERE is_public = true ORDER BY created_at DESC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Journal journal = new Journal();
                journal.setId(rs.getString("journal_id"));
                journal.setSoulId(rs.getString("soul_id"));
                journal.setText(rs.getString("journal_text"));
                journal.setFontFamily(rs.getString("font_family"));
                journal.setFontSize(rs.getInt("font_size"));
                journal.setLoveCount(rs.getInt("love_count"));
                Array lovedArr = rs.getArray("loved_by");
                if (lovedArr != null) journal.setLovedBy((String[]) lovedArr.getArray());
                journal.setIsPublic(rs.getBoolean("is_public"));
                
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    journal.setCreatedAt(ts.toLocalDateTime());
                    journal.setEntryDate(ts.toLocalDateTime().toLocalDate());
                }
                
                journals.add(journal);
            }
        }
        return journals;
    }
    
    /**
     * Get journals newer than a specific journal ID (for real-time updates).
     * @param sinceJournalId The journal ID to get newer journals after
     * @return List of journals created after the specified ID
     */
    public List<Journal> getNewJournalsSince(String sinceJournalId) throws SQLException {
        List<Journal> journals = new ArrayList<>();
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, loved_by, font_family, font_size, created_at, is_public FROM public_journals WHERE journal_id > ? AND is_public = true ORDER BY created_at DESC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sinceJournalId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    journal.setFontFamily(rs.getString("font_family"));
                    journal.setFontSize(rs.getInt("font_size"));
                    journal.setLoveCount(rs.getInt("love_count"));
                    Array lovedArr = rs.getArray("loved_by");
                    if (lovedArr != null) journal.setLovedBy((String[]) lovedArr.getArray());
                    journal.setIsPublic(rs.getBoolean("is_public"));
                    
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        journal.setCreatedAt(ts.toLocalDateTime());
                        journal.setEntryDate(ts.toLocalDateTime().toLocalDate());
                    }
                    
                    journals.add(journal);
                }
            }
        }
        return journals;
    }

    /**
     * Get all journals for a specific soul/user ordered by newest first.
     */
    public List<Journal> getJournalsBySoulId(String soulId) throws SQLException {
        List<Journal> journals = new ArrayList<>();
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, loved_by, font_family, font_size, created_at, is_public FROM public_journals WHERE lower(soul_id) = lower(?) ORDER BY created_at DESC";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soulId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    journal.setFontFamily(rs.getString("font_family"));
                    journal.setFontSize(rs.getInt("font_size"));
                    journal.setLoveCount(rs.getInt("love_count"));
                    Array lovedArr = rs.getArray("loved_by");
                    if (lovedArr != null) journal.setLovedBy((String[]) lovedArr.getArray());
                    journal.setIsPublic(rs.getBoolean("is_public"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        journal.setCreatedAt(ts.toLocalDateTime());
                        journal.setEntryDate(ts.toLocalDateTime().toLocalDate());
                    }
                    journals.add(journal);
                }
            }
        }
        return journals;
    }
    
    /**
     * Toggle love for a journal. Adds or removes the user from the loved_by array.
     * @param journalId The journal to love/unlove
     * @param soulId The user performing the action
     * @return true if loved, false if unloved
     */
    public boolean toggleLove(String journalId, String soulId) throws SQLException {
        // Check if user already loved this journal
        boolean isLoved = hasUserLoved(journalId, soulId);
        
        if (isLoved) {
            // Remove from loved_by array and decrement count
            String sql = "UPDATE public_journals SET loved_by = array_remove(loved_by, ?), love_count = love_count - 1 WHERE journal_id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, soulId);
                ps.setString(2, journalId);
                ps.executeUpdate();
            }
            return false;
        } else {
            // Add to loved_by array (keeping it sorted) and increment count
            String sql = "UPDATE public_journals SET loved_by = array_append(loved_by, ?), love_count = love_count + 1 WHERE journal_id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, soulId);
                ps.setString(2, journalId);
                ps.executeUpdate();
            }
            
            // Sort the array after adding
            String sortSql = "UPDATE public_journals SET loved_by = (SELECT array_agg(elem ORDER BY elem) FROM unnest(loved_by) elem) WHERE journal_id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sortSql)) {
                ps.setString(1, journalId);
                ps.executeUpdate();
            }
            return true;
        }
    }
    
    /**
     * Check if a user has loved a specific journal.
     */
    public boolean hasUserLoved(String journalId, String soulId) throws SQLException {
        String sql = "SELECT ? = ANY(loved_by) as has_loved FROM public_journals WHERE journal_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, soulId);
            ps.setString(2, journalId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_loved");
                }
            }
        }
        return false;
    }
    
    /**
     * Get love count for a journal.
     */
    public int getLoveCount(String journalId) throws SQLException {
        String sql = "SELECT love_count FROM public_journals WHERE journal_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, journalId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("love_count");
                }
            }
        }
        return 0;
    }
}
