package com.aarti.Task_manager.dto;

import com.aarti.Task_manager.entity.Task;
import java.time.LocalDateTime;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private String priority;
    private LocalDateTime createdAt;

    public TaskResponse(Task t) {         // build from an entity
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.completed = t.isCompleted();
        this.priority = t.getPriority();
        this.createdAt = t.getCreatedAt();
    }
    // + getters (Alt+Insert → Getter)

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}