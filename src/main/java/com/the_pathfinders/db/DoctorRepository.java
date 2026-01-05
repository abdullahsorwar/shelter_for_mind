package com.the_pathfinders.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepository {

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String query = "SELECT id, name, degree, phone, consulting_hours, specialization FROM doctors ORDER BY name";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setId(rs.getLong("id"));
                doctor.setName(rs.getString("name"));
                doctor.setDegree(rs.getString("degree"));
                doctor.setPhone(rs.getString("phone"));
                doctor.setConsultingHours(rs.getString("consulting_hours"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public Doctor getDoctorById(long id) throws SQLException {
        String query = "SELECT id, name, degree, phone, consulting_hours, specialization FROM doctors WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = new Doctor();
                    doctor.setId(rs.getLong("id"));
                    doctor.setName(rs.getString("name"));
                    doctor.setDegree(rs.getString("degree"));
                    doctor.setPhone(rs.getString("phone"));
                    doctor.setConsultingHours(rs.getString("consulting_hours"));
                    doctor.setSpecialization(rs.getString("specialization"));
                    return doctor;
                }
            }
        }
        return null;
    }

    public void saveAppointment(Appointment appointment) throws SQLException {
        String query = "INSERT INTO appointments (soul_id, doctor_id, appointment_date, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, appointment.getSoulId());
            ps.setLong(2, appointment.getDoctorId());
            ps.setString(3, appointment.getAppointmentDate());
            ps.setString(4, appointment.getStatus());
            ps.executeUpdate();
        }
    }

    public List<Appointment> getAppointmentsBySoulId(String soulId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT id, soul_id, doctor_id, appointment_date, status, created_at FROM appointments WHERE soul_id = ? ORDER BY created_at DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, soulId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setId(rs.getLong("id"));
                    appointment.setSoulId(rs.getString("soul_id"));
                    appointment.setDoctorId(rs.getLong("doctor_id"));
                    appointment.setAppointmentDate(rs.getString("appointment_date"));
                    appointment.setStatus(rs.getString("status"));
                    appointment.setCreatedAt(rs.getString("created_at"));
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    public void addSampleDoctors() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkQuery);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getInt(1) > 0) {
                return; // Doctors already exist
            }
        }

        // Add sample doctors
        String insertQuery = "INSERT INTO doctors (name, degree, phone, consulting_hours, specialization) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {

            // Doctor 1
            ps.setString(1, "Dr. Sarah Ahmed");
            ps.setString(2, "MBBS, MD (Psychiatry)");
            ps.setString(3, "+880-1712-345678");
            ps.setString(4, "Mon-Fri: 10 AM - 4 PM");
            ps.setString(5, "Clinical Psychology");
            ps.executeUpdate();

            // Doctor 2
            ps.setString(1, "Dr. Imran Khan");
            ps.setString(2, "MBBS, MPhil (Psychiatry)");
            ps.setString(3, "+880-1812-456789");
            ps.setString(4, "Tue-Sat: 2 PM - 8 PM");
            ps.setString(5, "Depression & Anxiety");
            ps.executeUpdate();

            // Doctor 3
            ps.setString(1, "Dr. Fatima Rahman");
            ps.setString(2, "MBBS, FCPS (Psychiatry)");
            ps.setString(3, "+880-1912-567890");
            ps.setString(4, "Sun-Thu: 9 AM - 3 PM");
            ps.setString(5, "Child Psychology");
            ps.executeUpdate();

            // Doctor 4
            ps.setString(1, "Dr. Kamal Hossain");
            ps.setString(2, "MBBS, MD (Neurology)");
            ps.setString(3, "+880-1612-678901");
            ps.setString(4, "Mon-Wed: 3 PM - 7 PM");
            ps.setString(5, "Stress Management");
            ps.executeUpdate();

            // Doctor 5
            ps.setString(1, "Dr. Nadia Chowdhury");
            ps.setString(2, "MBBS, Diploma in Psychology");
            ps.setString(3, "+880-1512-789012");
            ps.setString(4, "Thu-Sat: 11 AM - 5 PM");
            ps.setString(5, "Trauma & PTSD");
            ps.executeUpdate();
        }
    }
}

