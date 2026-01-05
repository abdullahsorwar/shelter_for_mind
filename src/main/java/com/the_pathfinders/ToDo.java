package com.the_pathfinders;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ToDo {
    private final BooleanProperty done = new SimpleBooleanProperty(false);
    private final StringProperty task = new SimpleStringProperty("");

    public ToDo() {}

    public ToDo(String task) {
        this.task.set(task);
    }

    public ToDo(boolean done, String task) {
        this.done.set(done);
        this.task.set(task);
    }

    public BooleanProperty doneProperty() { return done; }
    public boolean isDone() { return done.get(); }
    public void setDone(boolean value) { done.set(value); }

    public StringProperty taskProperty() { return task; }
    public String getTask() { return task.get(); }
    public void setTask(String value) { task.set(value); }
}