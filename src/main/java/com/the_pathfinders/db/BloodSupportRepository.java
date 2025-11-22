package com.the_pathfinders.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class BloodSupportRepository {

    public BloodRequest saveRequest(BloodRequest request) throws SQLException {
        String sql = """
            insert into blood_requests (soul_id, blood_group, location, phone)
            values (?, ?, ?, ?)
            returning id, created_at
        """;
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nullIfBlank(request.getSoulId()));
            ps.setString(2, request.getBloodGroup());
            ps.setString(3, request.getLocation());
            ps.setString(4, request.getPhone());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    request.setId(rs.getLong("id"));
                    request.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                }
            }
            return request;
        }
    }

    public BloodDonor saveDonor(BloodDonor donor) throws SQLException {
        String sql = """
            insert into blood_donors (soul_id, blood_group, contact_number, last_donation_info, area)
            values (?, ?, ?, ?, ?)
            returning id, created_at
        """;
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nullIfBlank(donor.getSoulId()));
            ps.setString(2, donor.getBloodGroup());
            ps.setString(3, donor.getContactNumber());
            ps.setString(4, donor.getLastDonationInfo());
            ps.setString(5, donor.getArea());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    donor.setId(rs.getLong("id"));
                    donor.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                }
            }
            return donor;
        }
    }

    public List<BloodDonor> findDonorsByBloodGroup(String bloodGroup) throws SQLException {
        List<BloodDonor> donors = new ArrayList<>();
        String sql = """
            select id, soul_id, blood_group, contact_number, last_donation_info, area, created_at
            from blood_donors
            where lower(blood_group) = lower(?)
            order by created_at desc
        """;
        try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bloodGroup);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BloodDonor donor = new BloodDonor();
                    donor.setId(rs.getLong("id"));
                    donor.setSoulId(rs.getString("soul_id"));
                    donor.setBloodGroup(rs.getString("blood_group"));
                    donor.setContactNumber(rs.getString("contact_number"));
                    donor.setLastDonationInfo(rs.getString("last_donation_info"));
                    donor.setArea(rs.getString("area"));
                    donor.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    donors.add(donor);
                }
            }
        }
        return donors;
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
