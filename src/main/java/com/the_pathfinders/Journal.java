// java
package com.the_pathfinders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Journal {
    private String id;
    private String soulId;
    private String text;
    private LocalDate entryDate;
    private LocalDateTime createdAt;

    public Journal() {
        this.createdAt = LocalDateTime.now();
    }

    public Journal(String id, String soulId, String text, LocalDate entryDate, LocalDateTime createdAt) {
        this.id = id;
        this.soulId = soulId;
        this.text = text;
        this.entryDate = entryDate;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSoulId() { return soulId; }
    public void setSoulId(String soulId) { this.soulId = soulId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Journal{" +
                "id='" + id + '\'' +
                ", soulId='" + soulId + '\'' +
                ", entryDate=" + entryDate +
                ", createdAt=" + createdAt +
                ", text='" + (text == null ? "" : (text.length() > 60 ? text.substring(0, 60) + "…" : text)) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Journal)) return false;
        Journal journal = (Journal) o;
        return Objects.equals(id, journal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
