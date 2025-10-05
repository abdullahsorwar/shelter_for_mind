package com.the_pathfinders.db;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class SoulRepository {

    public static class DuplicateIdException extends Exception {}

    public record Soul(
        String id,
        String key,
        String name,
        java.time.LocalDate dob,
        String mobile,
        String countryCode
    ) {}

    /** Case-insensitive ID existence check (we store lowercased). */
    public boolean idExists(String id) throws SQLException {
        final String sql = "select 1 from soul_id_and_soul_key where soul_id = ?";
        try (var c = DB.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toLowerCase());
            try (var rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void create(Soul s) throws SQLException, DuplicateIdException {
        final String sql = """
            insert into soul_id_and_soul_key
            (soul_id, soul_key_hash, soul_name, dob, mobile, country_code, created_at)
            values (?, ?, ?, ?, ?, ?, now())
        """;
        String hash = BCrypt.hashpw(s.key(), BCrypt.gensalt(12));
        try (var c = DB.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, s.id().toLowerCase());
            ps.setString(2, hash);
            ps.setString(3, s.name());
            if (s.dob() == null) ps.setNull(4, Types.DATE);
            else ps.setDate(4, Date.valueOf(s.dob()));
            ps.setString(5, s.mobile());
            ps.setString(6, s.countryCode());
            ps.executeUpdate();
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) throw new DuplicateIdException();
            throw ex;
        }
    }

    public boolean verify(String id, String rawKey) throws SQLException {
        final String sql = "select soul_key_hash from soul_id_and_soul_key where soul_id = ?";
        try (var c = DB.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, id.toLowerCase());
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hash = rs.getString(1);
                return org.mindrot.jbcrypt.BCrypt.checkpw(rawKey, hash);
            }
        }
    }
}