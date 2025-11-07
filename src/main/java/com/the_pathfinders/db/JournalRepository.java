package com.the_pathfinders.db;

import com.the_pathfinders.Journal;

import java.sql.*;

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
     * @return The generated journal ID
     */
    public String saveJournal(String soulId, String journalText) throws SQLException {
        String journalId = getNextJournalId();
        
        String sql = "INSERT INTO public_journals (journal_id, soul_id, journal_text, love_count) VALUES (?, ?, ?, 0)";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, journalId);
            ps.setString(2, soulId);
            ps.setString(3, journalText);
            ps.executeUpdate();
            
            return journalId;
        }
    }

    /**
     * Load a journal entry by ID.
     */
    public Journal getJournalById(String journalId) throws SQLException {
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, created_at FROM public_journals WHERE journal_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, journalId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    
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
     * Load the latest journal entry for a specific user.
     */
    public Journal getLatestJournalForUser(String soulId) throws SQLException {
        String sql = "SELECT journal_id, soul_id, journal_text, love_count, created_at FROM public_journals WHERE soul_id = ? ORDER BY journal_id DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, soulId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Journal journal = new Journal();
                    journal.setId(rs.getString("journal_id"));
                    journal.setSoulId(rs.getString("soul_id"));
                    journal.setText(rs.getString("journal_text"));
                    
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
}
