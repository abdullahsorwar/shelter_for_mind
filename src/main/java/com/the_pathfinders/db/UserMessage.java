package com.the_pathfinders.db;

public class UserMessage {
    private long id;
    private String soulId;
    private String messageType; // APPOINTMENT_CONFIRMED, APPOINTMENT_RESCHEDULED, SYSTEM, MODERATION
    private String subject;
    private String messageContent;
    private Long appointmentId;
    private boolean isRead;
    private String createdAt;

    public UserMessage() {}

    public UserMessage(String soulId, String messageType, String subject, String messageContent) {
        this.soulId = soulId;
        this.messageType = messageType;
        this.subject = subject;
        this.messageContent = messageContent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSoulId() {
        return soulId;
    }

    public void setSoulId(String soulId) {
        this.soulId = soulId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

