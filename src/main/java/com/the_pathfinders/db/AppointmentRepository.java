package com.the_pathfinders.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {

    // Create appointment
    public long createAppointment(String soulId, long doctorId, String appointmentDate) throws SQLException {
        String sql = "INSERT INTO appointments (soul_id, doctor_id, appointment_date, status, created_at) " +
                     "VALUES (?, ?, ?, 'PENDING', NOW())";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, soulId.toLowerCase());
            ps.setLong(2, doctorId);
            ps.setString(3, appointmentDate);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated appointment ID");
            }
        }
    }

    // Get pending appointments (for keeper dashboard)
    public List<AppointmentDetails> getPendingAppointments() throws SQLException {
        String sql = "SELECT a.id, a.soul_id, a.doctor_id, a.appointment_date, a.status, a.created_at, " +
                     "d.name as doctor_name, d.degree, d.phone as doctor_phone, d.consulting_hours, " +
                     "si.name as soul_name, si.phone as soul_phone " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.id " +
                     "LEFT JOIN soul_info si ON a.soul_id = si.soul_id " +
                     "WHERE a.status = 'PENDING' " +
                     "ORDER BY a.created_at DESC";

        List<AppointmentDetails> appointments = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AppointmentDetails details = new AppointmentDetails();
                details.id = rs.getLong("id");
                details.soulId = rs.getString("soul_id");
                details.doctorId = rs.getLong("doctor_id");
                details.appointmentDate = rs.getString("appointment_date");
                details.status = rs.getString("status");
                details.createdAt = rs.getString("created_at");
                details.doctorName = rs.getString("doctor_name");
                details.doctorDegree = rs.getString("degree");
                details.doctorPhone = rs.getString("doctor_phone");
                details.consultingHours = rs.getString("consulting_hours");
                details.soulName = rs.getString("soul_name");
                details.soulPhone = rs.getString("soul_phone");
                appointments.add(details);
            }
        }
        return appointments;
    }

    // Get all appointments
    public List<AppointmentDetails> getAllAppointments() throws SQLException {
        String sql = "SELECT a.id, a.soul_id, a.doctor_id, a.appointment_date, a.status, a.created_at, " +
                     "d.name as doctor_name, d.degree, d.phone as doctor_phone, d.consulting_hours, " +
                     "si.name as soul_name, si.phone as soul_phone " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.id " +
                     "LEFT JOIN soul_info si ON a.soul_id = si.soul_id " +
                     "ORDER BY a.created_at DESC";

        List<AppointmentDetails> appointments = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AppointmentDetails details = new AppointmentDetails();
                details.id = rs.getLong("id");
                details.soulId = rs.getString("soul_id");
                details.doctorId = rs.getLong("doctor_id");
                details.appointmentDate = rs.getString("appointment_date");
                details.status = rs.getString("status");
                details.createdAt = rs.getString("created_at");
                details.doctorName = rs.getString("doctor_name");
                details.doctorDegree = rs.getString("degree");
                details.doctorPhone = rs.getString("doctor_phone");
                details.consultingHours = rs.getString("consulting_hours");
                details.soulName = rs.getString("soul_name");
                details.soulPhone = rs.getString("soul_phone");
                appointments.add(details);
            }
        }
        return appointments;
    }

    // Update appointment status
    public boolean updateAppointmentStatus(long appointmentId, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    // Update appointment date (for reschedule)
    public boolean updateAppointmentDate(long appointmentId, String newDate) throws SQLException {
        String sql = "UPDATE appointments SET appointment_date = ?, status = 'RESCHEDULED' WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newDate);
            ps.setLong(2, appointmentId);
            return ps.executeUpdate() > 0;
        }
    }

    // Get appointment by ID
    public AppointmentDetails getAppointmentById(long id) throws SQLException {
        String sql = "SELECT a.id, a.soul_id, a.doctor_id, a.appointment_date, a.status, a.created_at, " +
                     "d.name as doctor_name, d.degree, d.phone as doctor_phone, d.consulting_hours, " +
                     "si.name as soul_name, si.phone as soul_phone " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.id " +
                     "LEFT JOIN soul_info si ON a.soul_id = si.soul_id " +
                     "WHERE a.id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AppointmentDetails details = new AppointmentDetails();
                    details.id = rs.getLong("id");
                    details.soulId = rs.getString("soul_id");
                    details.doctorId = rs.getLong("doctor_id");
                    details.appointmentDate = rs.getString("appointment_date");
                    details.status = rs.getString("status");
                    details.createdAt = rs.getString("created_at");
                    details.doctorName = rs.getString("doctor_name");
                    details.doctorDegree = rs.getString("degree");
                    details.doctorPhone = rs.getString("doctor_phone");
                    details.consultingHours = rs.getString("consulting_hours");
                    details.soulName = rs.getString("soul_name");
                    details.soulPhone = rs.getString("soul_phone");
                    return details;
                }
            }
        }
        return null;
    }

    // Inner class for appointment details with doctor and soul info
    public static class AppointmentDetails {
        public long id;
        public String soulId;
        public long doctorId;
        public String appointmentDate;
        public String status;
        public String createdAt;
        public String doctorName;
        public String doctorDegree;
        public String doctorPhone;
        public String consultingHours;
        public String soulName;
        public String soulPhone;
    }
}

