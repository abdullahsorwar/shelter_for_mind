package com.the_pathfinders.db;

import java.time.OffsetDateTime;

public class ToDoItem {
    private long id;
    private String soulId;
    private String task;
    private boolean done;
    private OffsetDateTime createdAt;

    public ToDoItem() {}

    public ToDoItem(long id, String soulId, String task, boolean done, OffsetDateTime createdAt) {
        this.id = id;
        this.soulId = soulId;
        this.task = task;
        this.done = done;
        this.createdAt = createdAt;
    }

    public ToDoItem(String soulId, String task, boolean done) {
        this(0, soulId, task, done, null);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSoulId() { return soulId; }
    public void setSoulId(String soulId) { this.soulId = soulId; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}