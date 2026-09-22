package com.aarti.Task_manager.service;

import com.aarti.Task_manager.dto.*;
import com.aarti.Task_manager.entity.Task;
import com.aarti.Task_manager.exception.ResourceNotFoundException;
import com.aarti.Task_manager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service   // Spring creates one shared instance (a "bean")
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {   // constructor injection
        this.repo = repo;
    }

    // Helper: find a task or throw a clean 404. Reused by getById/update/delete.
    private Task findTaskOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    // CREATE
    public TaskResponse create(TaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setCompleted(req.isCompleted());
        task.setPriority(req.getPriority());
        return new TaskResponse(repo.save(task));      // INSERT
    }

    // READ ALL
    public List<TaskResponse> getAll() {
        return repo.findAll().stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    // READ ONE
    public TaskResponse getById(Long id) {
        Task task = findTaskOrThrow(id);
        return new TaskResponse(task);
    }

    // UPDATE
    public TaskResponse update(Long id, TaskRequest req) {
        Task task = findTaskOrThrow(id);
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setCompleted(req.isCompleted());
        task.setPriority(req.getPriority());
        return new TaskResponse(repo.save(task));      // UPDATE (same id)
    }

    // DELETE
    public void delete(Long id) {
        Task task = findTaskOrThrow(id);
        repo.delete(task);
    }
}