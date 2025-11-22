package com.the_pathfinders.db;

import java.time.OffsetDateTime;

public class BloodRequest {
    private long id;
    private String soulId;
    private String bloodGroup;
    private String location;
    private String phone;
    private OffsetDateTime createdAt;

    public BloodRequest() {}

    public BloodRequest(String soulId, String bloodGroup, String location, String phone) {
        this.soulId = soulId;
        this.bloodGroup = bloodGroup;
        this.location = location;
        this.phone = phone;
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

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
