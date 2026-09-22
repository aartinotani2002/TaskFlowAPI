package com.aarti.Task_manager.repository;
import com.aarti.Task_manager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // FREE methods you already get: save, findAll, findById, deleteById...

    // "Derived query" - Spring reads the name and writes the SQL for you:
    List<Task> findByCompleted(boolean completed);
}