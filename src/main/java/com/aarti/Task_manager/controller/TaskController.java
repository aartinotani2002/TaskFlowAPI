package com.aarti.Task_manager.controller;

import com.aarti.Task_manager.dto.*;
import com.aarti.Task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                     // every return value → JSON automatically
@RequestMapping("/api/tasks")      // common URL prefix
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @PostMapping                                            // POST /api/tasks
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest req) {
        return new ResponseEntity<>(service.create(req), HttpStatus.CREATED);   // 201
    }

    @GetMapping                                             // GET /api/tasks
    public List<TaskResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")                                    // GET /api/tasks/5
    public TaskResponse getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")                                    // PUT /api/tasks/5
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")                                 // DELETE /api/tasks/5
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();          // 204
    }
}