package com.the_pathfinders.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogHistoryRepository {

    public static class BlogHistoryEntry {
        private int id;
        private String soulId;
        private String blogId;
        private String blogCategory;
        private LocalDateTime viewedAt;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getSoulId() { return soulId; }
        public void setSoulId(String soulId) { this.soulId = soulId; }

        public String getBlogId() { return blogId; }
        public void setBlogId(String blogId) { this.blogId = blogId; }

        public String getBlogCategory() { return blogCategory; }
        public void setBlogCategory(String blogCategory) { this.blogCategory = blogCategory; }

        public LocalDateTime getViewedAt() { return viewedAt; }
        public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
    }

    /**
     * Records that a user viewed a specific blog article
     */
    public void recordBlogView(String soulId, String blogId, String blogCategory) throws Exception {
        String sql = """
            insert into blog_reading_history (soul_id, blog_id, blog_category, viewed_at)
            values (?, ?, ?, ?)
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);
            ps.setString(2, blogId);
            ps.setString(3, blogCategory);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
        }
    }

    /**
     * Gets unique blog IDs viewed by user today
     */
    public Set<String> getViewedTodayBlogIds(String soulId) throws Exception {
        String sql = """
            select distinct blog_id
            from blog_reading_history
            where soul_id = ?
            and date(viewed_at) = current_date
            order by blog_id
        """;

        Set<String> blogIds = new HashSet<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    blogIds.add(rs.getString("blog_id"));
                }
            }
        }

        return blogIds;
    }

    /**
     * Gets reading history with categories (up to specified limit, most recent first)
     */
    public List<String> getReadingHistoryCategories(String soulId, int limit) throws Exception {
        String sql = """
            select blog_category
            from blog_reading_history
            where soul_id = ?
            order by viewed_at desc
            limit ?
        """;

        List<String> categories = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("blog_category"));
                }
            }
        }

        return categories;
    }

    /**
     * Gets the count of articles read today
     */
    public int getArticlesReadToday(String soulId) throws Exception {
        String sql = """
            select count(distinct blog_id) as read_count
            from blog_reading_history
            where soul_id = ?
            and date(viewed_at) = current_date
        """;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("read_count");
                }
            }
        }

        return 0;
    }

    /**
     * Gets all reading history entries for a user (for analytics/insights)
     */
    public List<BlogHistoryEntry> getAllHistory(String soulId, int limit) throws Exception {
        String sql = """
            select id, soul_id, blog_id, blog_category, viewed_at
            from blog_reading_history
            where soul_id = ?
            order by viewed_at desc
            limit ?
        """;

        List<BlogHistoryEntry> entries = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, soulId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BlogHistoryEntry entry = new BlogHistoryEntry();
                    entry.setId(rs.getInt("id"));
                    entry.setSoulId(rs.getString("soul_id"));
                    entry.setBlogId(rs.getString("blog_id"));
                    entry.setBlogCategory(rs.getString("blog_category"));
                    entry.setViewedAt(rs.getTimestamp("viewed_at").toLocalDateTime());
                    entries.add(entry);
                }
            }
        }

        return entries;
    }
}
