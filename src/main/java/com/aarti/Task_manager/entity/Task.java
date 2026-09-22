package com.aarti.Task_manager.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity                 // "This class is a database table"
@Table(name = "tasks")
public class Task {

    @Id                                                 // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private boolean completed;
    private String priority;              // LOW / MEDIUM / HIGH

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist                           // runs right before first INSERT
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Task() {}                      // JPA requires an empty constructor

    // + generate getters & setters (IntelliJ: Alt+Insert → Getter and Setter)

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