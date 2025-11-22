package com.the_pathfinders.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MoodTrackerRepository {

    public static class MoodEntry {
        private int id;
        private String soulId;
        private int moodScore;
        private int stressScore;
        private int anxietyScore;
        private int energyScore;
        private int sleepScore;
        private int socialScore;
        private String answers;
        private LocalDateTime createdAt;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getSoulId() { return soulId; }
        public void setSoulId(String soulId) { this.soulId = soulId; }

        public int getMoodScore() { return moodScore; }
        public void setMoodScore(int moodScore) { this.moodScore = moodScore; }

        public int getStressScore() { return stressScore; }
        public void setStressScore(int stressScore) { this.stressScore = stressScore; }

        public int getAnxietyScore() { return anxietyScore; }
        public void setAnxietyScore(int anxietyScore) { this.anxietyScore = anxietyScore; }

        public int getEnergyScore() { return energyScore; }
        public void setEnergyScore(int energyScore) { this.energyScore = energyScore; }

        public int getSleepScore() { return sleepScore; }
        public void setSleepScore(int sleepScore) { this.sleepScore = sleepScore; }

        public int getSocialScore() { return socialScore; }
        public void setSocialScore(int socialScore) { this.socialScore = socialScore; }

        public String getAnswers() { return answers; }
        public void setAnswers(String answers) { this.answers = answers; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public void saveMoodEntry(MoodEntry entry) throws Exception {
        String sql = """
            insert into mood_tracker (soul_id, mood_score, stress_score, anxiety_score, 
                                      energy_score, sleep_score, social_score, answers, created_at)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entry.getSoulId());
            ps.setInt(2, entry.getMoodScore());
            ps.setInt(3, entry.getStressScore());
            ps.setInt(4, entry.getAnxietyScore());
            ps.setInt(5, entry.getEnergyScore());
            ps.setInt(6, entry.getSleepScore());
            ps.setInt(7, entry.getSocialScore());
            ps.setString(8, entry.getAnswers());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
        }
    }

    public List<MoodEntry> getMoodHistory(String soulId, int limit) throws Exception {
        String sql = """
            select id, soul_id, mood_score, stress_score, anxiety_score, 
                   energy_score, sleep_score, social_score, answers, created_at
            from mood_tracker
            where soul_id = ?
            order by created_at desc
            limit ?
        """;

        List<MoodEntry> entries = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MoodEntry entry = new MoodEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setSoulId(rs.getString("soul_id"));
                    entry.setMoodScore(rs.getInt("mood_score"));
                    entry.setStressScore(rs.getInt("stress_score"));
                    entry.setAnxietyScore(rs.getInt("anxiety_score"));
                    entry.setEnergyScore(rs.getInt("energy_score"));
                    entry.setSleepScore(rs.getInt("sleep_score"));
                    entry.setSocialScore(rs.getInt("social_score"));
                    entry.setAnswers(rs.getString("answers"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        entry.setCreatedAt(ts.toLocalDateTime());
                    }

                    entries.add(entry);
                }
            }
        }

        return entries;
    }

    public MoodEntry getLatestMoodEntry(String soulId) throws Exception {
        String sql = """
            select id, soul_id, mood_score, stress_score, anxiety_score, 
                   energy_score, sleep_score, social_score, answers, created_at
            from mood_tracker
            where soul_id = ?
            order by created_at desc
            limit 1
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MoodEntry entry = new MoodEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setSoulId(rs.getString("soul_id"));
                    entry.setMoodScore(rs.getInt("mood_score"));
                    entry.setStressScore(rs.getInt("stress_score"));
                    entry.setAnxietyScore(rs.getInt("anxiety_score"));
                    entry.setEnergyScore(rs.getInt("energy_score"));
                    entry.setSleepScore(rs.getInt("sleep_score"));
                    entry.setSocialScore(rs.getInt("social_score"));
                    entry.setAnswers(rs.getString("answers"));

                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        entry.setCreatedAt(ts.toLocalDateTime());
                    }

                    return entry;
                }
            }
        }

        return null;
    }
}

