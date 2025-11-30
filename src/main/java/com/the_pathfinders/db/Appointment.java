package com.the_pathfinders.db;

public class Appointment {
    private long id;
    private String soulId;
    private long doctorId;
    private String appointmentDate;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private String createdAt;

    public Appointment() {}

    public Appointment(String soulId, long doctorId, String appointmentDate) {
        this.soulId = soulId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.status = "PENDING";
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

    public long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(long doctorId) {
        this.doctorId = doctorId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

