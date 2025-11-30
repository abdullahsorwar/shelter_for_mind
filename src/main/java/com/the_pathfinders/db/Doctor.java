package com.the_pathfinders.db;

public class Doctor {
    private long id;
    private String name;
    private String degree;
    private String phone;
    private String consultingHours;
    private String specialization;

    public Doctor() {}

    public Doctor(String name, String degree, String phone, String consultingHours, String specialization) {
        this.name = name;
        this.degree = degree;
        this.phone = phone;
        this.consultingHours = consultingHours;
        this.specialization = specialization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getConsultingHours() {
        return consultingHours;
    }

    public void setConsultingHours(String consultingHours) {
        this.consultingHours = consultingHours;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}

