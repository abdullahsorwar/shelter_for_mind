package com.the_pathfinders;

import java.time.LocalDateTime;

public class SocialWork {
    private String id;
    private String soulId;
    private String type; // "blood_donation", "tree_plantation", etc.
    private String description;
    private LocalDateTime createdAt;
    private Integer impactPoints;

    public SocialWork() {}

    public SocialWork(String id, String soulId, String type, String description, LocalDateTime createdAt, Integer impactPoints) {
        this.id = id;
        this.soulId = soulId;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
        this.impactPoints = impactPoints;
    }

    // Getters and setters...
}
