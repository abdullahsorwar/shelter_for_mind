package com.the_pathfinders.db;

import java.sql.*;
import java.time.LocalDate;

public class SoulInfoRepository {

    public static class SoulInfo {
        public String soulId;
        public String name;
        public LocalDate dob;
        public String email;
        public String phone;
        public String address;
        public String countryCode;
        public Boolean emailVerified; // Track if email is verified
    }

    public boolean exists(String soulId) throws SQLException {
        String sql = "select 1 from soul_info where soul_id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public SoulInfo getBySoulId(String soulId) throws SQLException {
        String sql = "select soul_id, name, dob, email, phone, address, country_code, email_verified from soul_info where soul_id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                SoulInfo s = new SoulInfo();
                s.soulId = rs.getString("soul_id");
                s.name = rs.getString("name");
                Date d = rs.getDate("dob");
                s.dob = d == null ? null : d.toLocalDate();
                s.email = rs.getString("email");
                s.phone = rs.getString("phone");
                s.address = rs.getString("address");
                s.countryCode = rs.getString("country_code");
                // Try to read email_verified column, default to false if column doesn't exist yet
                try {
                    s.emailVerified = rs.getBoolean("email_verified");
                } catch (SQLException e) {
                    s.emailVerified = false; // Default to false if column doesn't exist
                }
                return s;
            }
        }
    }

    public void insertBasic(String soulId, String name, LocalDate dob, String phone, String countryCode) throws SQLException {
        String sql = "insert into soul_info (soul_id, name, dob, phone, country_code, created_at, updated_at) values (?, ?, ?, ?, ?, now(), now()) on conflict (soul_id) do nothing";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            ps.setString(2, name);
            if (dob == null) ps.setNull(3, Types.DATE); else ps.setDate(3, Date.valueOf(dob));
            ps.setString(4, phone);
            ps.setString(5, countryCode);
            ps.executeUpdate();
        }
    }

    public void upsert(String soulId, String name, LocalDate dob, String email, String phone, String address, String countryCode) throws SQLException {
        // First check if email has changed
        SoulInfo existing = getBySoulId(soulId);
        boolean emailChanged = existing != null && existing.email != null && !existing.email.equals(email);
        
        String sql = "insert into soul_info (soul_id, name, dob, email, phone, address, country_code, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, now(), now()) " +
                     "on conflict (soul_id) do update set name=excluded.name, dob=excluded.dob, email=excluded.email, phone=excluded.phone, address=excluded.address, country_code=excluded.country_code, updated_at=now()";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            ps.setString(2, name);
            if (dob == null) ps.setNull(3, Types.DATE); else ps.setDate(3, Date.valueOf(dob));
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, address);
            ps.setString(7, countryCode);
            ps.executeUpdate();
        }
        
        // If email changed, reset email_verified to false
        if (emailChanged) {
            System.out.println("Email changed from '" + existing.email + "' to '" + email + "', resetting verification status");
            updateEmailVerified(soulId, false);
        }
    }

    // Update email verification status
    public static void updateEmailVerified(String soulId, boolean verified) throws SQLException {
        String sql = "update soul_info set email_verified = ?, updated_at = now() where soul_id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, verified);
            ps.setString(2, soulId.toLowerCase());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated email_verified to " + verified + " for soul_id: " + soulId);
            } else {
                System.err.println("No rows updated for soul_id: " + soulId);
            }
        }
    }

    // Fetch initial data from authentication table
    public SoulRepository.Soul getAuthRecord(String soulId) throws SQLException {
    // Reuse soul_id_and_soul_key to fetch initial basic info for first-time profile setup
        final String sql = "select soul_id, soul_name, dob, mobile, country_code from soul_id_and_soul_key where soul_id = ?";
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String id = rs.getString("soul_id");
                String name = rs.getString("soul_name");
                Date d = rs.getDate("dob");
                java.time.LocalDate l = d == null ? null : d.toLocalDate();
                String mobile = rs.getString("mobile");
                String cc = rs.getString("country_code");
                return new SoulRepository.Soul(id, "", name, l, mobile, cc);
            }
        }
    }
}
