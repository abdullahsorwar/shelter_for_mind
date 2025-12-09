package com.the_pathfinders.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserMessageRepository {

    // Send a message to user
    public static long sendMessage(String soulId, String messageType, String subject, String messageContent, Long appointmentId) throws SQLException {
        String sql = "INSERT INTO user_messages (soul_id, message_type, subject, message_content, appointment_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, soulId.toLowerCase());
            ps.setString(2, messageType);
            ps.setString(3, subject);
            ps.setString(4, messageContent);
            if (appointmentId != null) {
                ps.setLong(5, appointmentId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated message ID");
            }
        }
    }

    // Get all messages for a user
    public static List<UserMessage> getMessagesForUser(String soulId) throws SQLException {
        String sql = "SELECT id, soul_id, message_type, subject, message_content, appointment_id, is_read, " +
                     "TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at " +
                     "FROM user_messages WHERE soul_id = ? ORDER BY created_at DESC";

        List<UserMessage> messages = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserMessage msg = new UserMessage();
                    msg.setId(rs.getLong("id"));
                    msg.setSoulId(rs.getString("soul_id"));
                    msg.setMessageType(rs.getString("message_type"));
                    msg.setSubject(rs.getString("subject"));
                    msg.setMessageContent(rs.getString("message_content"));
                    Long aptId = rs.getLong("appointment_id");
                    msg.setAppointmentId(rs.wasNull() ? null : aptId);
                    msg.setRead(rs.getBoolean("is_read"));
                    msg.setCreatedAt(rs.getString("created_at"));
                    messages.add(msg);
                }
            }
        }
        return messages;
    }

    // Get unread messages count
    public static int getUnreadCount(String soulId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_messages WHERE soul_id = ? AND is_read = false";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Mark message as read
    public static boolean markAsRead(long messageId) throws SQLException {
        String sql = "UPDATE user_messages SET is_read = true WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, messageId);
            return ps.executeUpdate() > 0;
        }
    }

    // Mark all messages as read for a user
    public static boolean markAllAsRead(String soulId) throws SQLException {
        String sql = "UPDATE user_messages SET is_read = true WHERE soul_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soulId.toLowerCase());
            return ps.executeUpdate() > 0;
        }
    }

    // Delete a message
    public static boolean deleteMessage(long messageId) throws SQLException {
        String sql = "DELETE FROM user_messages WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, messageId);
            return ps.executeUpdate() > 0;
        }
    }

    // Send appointment confirmation message
    public static void sendAppointmentConfirmation(String soulId, long appointmentId, String doctorName, String appointmentDate) throws SQLException {
        String subject = "✅ Appointment Confirmed";
        String message = String.format(
            "Your appointment with Dr. %s has been confirmed!\n\n" +
            "📅 Date: %s\n\n" +
            "Please arrive 10 minutes early. If you need to cancel or reschedule, please contact us as soon as possible.\n\n" +
            "Thank you for choosing Shelter of Mind!",
            doctorName, appointmentDate
        );

        sendMessage(soulId, "APPOINTMENT_CONFIRMED", subject, message, appointmentId);
    }

    // Send appointment reschedule request message
    public static void sendRescheduleRequest(String soulId, long appointmentId, String doctorName, String currentDate, String availableDates) throws SQLException {
        String subject = "📅 Appointment Reschedule Request";
        String message = String.format(
            "Your appointment with Dr. %s scheduled for %s needs to be rescheduled.\n\n" +
            "Available dates:\n%s\n\n" +
            "Please contact us or book a new appointment with one of the available dates above.\n\n" +
            "We apologize for any inconvenience.",
            doctorName, currentDate, availableDates
        );

        sendMessage(soulId, "APPOINTMENT_RESCHEDULED", subject, message, appointmentId);
    }
}

