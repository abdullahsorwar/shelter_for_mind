package com.the_pathfinders.db;

import java.time.OffsetDateTime;

public class BloodDonor {
    private long id;
    private String soulId;
    private String bloodGroup;
    private String contactNumber;
    private String lastDonationInfo;
    private String area;
    private OffsetDateTime createdAt;

    public BloodDonor() {}

    public BloodDonor(String soulId, String bloodGroup, String contactNumber, String lastDonationInfo, String area) {
        this.soulId = soulId;
        this.bloodGroup = bloodGroup;
        this.contactNumber = contactNumber;
        this.lastDonationInfo = lastDonationInfo;
        this.area = area;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getLastDonationInfo() {
        return lastDonationInfo;
    }

    public void setLastDonationInfo(String lastDonationInfo) {
        this.lastDonationInfo = lastDonationInfo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
