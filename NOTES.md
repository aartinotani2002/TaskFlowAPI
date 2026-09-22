# Task Manager REST API — Full Build Guide & Explanation

A complete, step-by-step record of how this Spring Boot project was built, with the
code for every file, the meaning of every important line, and **why** each piece exists.
Use it for revision and interview prep.

---

## 0. What this project is

A **REST API** for managing tasks (a to-do backend). A client (Postman, a website, an
app) sends HTTP requests; the API creates/reads/updates/deletes tasks in a database and
replies with JSON.

It is organized into **layers**, each with one job, each talking only to the layer below:

```
Client (Postman)
     │   HTTP + JSON
     ▼
Controller  →  Service  →  Repository  →  Entity  →  Database (H2)
  (HTTP)      (logic)      (DB access)   (table)
```

**Why layers?** Separation of concerns: each part stays simple, testable and replaceable.
Swap H2 for MySQL → only config changes. Change the DB table → the API contract stays stable.

---

## 1. Prerequisites & tools

- **JDK 17+** (this project runs on Java 25)
- **IntelliJ IDEA** (bundles Maven)
- **Postman** (to send test requests)

**Golden rule while developing:** Spring Boot does NOT auto-reload Java changes.
After every `.java` edit → **Stop** the app (red ■) → **Run** it again.

---

## 2. Step 1 — Generate the project (Spring Initializr)

Went to <https://start.spring.io> and chose:

| Setting | Value | Why |
|---------|-------|-----|
| Project | Maven | Maven manages libraries + build |
| Language | Java | our language |
| Group | `com.aarti` | reverse-domain package prefix |
| Artifact | `Task-manager` | project name (→ package `com.aarti.Task_manager`) |
| Java | 17 | language version |

**Dependencies added (and why):**

| Dependency | Why we need it |
|------------|----------------|
| **Spring Web** | gives `@RestController`, the embedded Tomcat server, JSON conversion |
| **Spring Data JPA** | the ORM + repositories, so we write almost no SQL |
| **Validation** | `@NotBlank`, `@Pattern` etc. to validate incoming data |
| **H2 Database** | tiny in-memory DB → zero setup, runs instantly (data resets on restart) |

Clicked **Generate** → unzipped → opened the folder in IntelliJ.
Initializr created `pom.xml`, the `...Application.java` with `main()`, and `application.properties`.

### The four dependencies explained in depth

**Spring Web** — turns a plain Java program into a web app. Bundles three things:
1. *Spring MVC* — the router: gives `@RestController`, `@GetMapping`/`@PostMapping` etc., and
   matches each incoming request to the right method.
2. *Embedded Tomcat* — the web server built INTO the app; listens on port 8080 so you just run
   `main()` (no separate server install/deploy). This is why the log says "Tomcat started".
3. *Jackson (JSON conversion)* — auto-translates JSON ↔ Java objects (uses your getters/setters).
   `@RequestBody` = JSON→object; returning an object = object→JSON. That's why you never wrote
   any toJson/fromJson code.

**Spring Data JPA** — talk to the DB using objects instead of SQL. Three stacked layers:
1. *ORM (Object-Relational Mapping)* — the idea: class↔table, object↔row, field↔column.
2. *JPA + Hibernate* — JPA is the standard (annotations `@Entity`, `@Id`…); Hibernate is the
   engine that implements it and generates the real SQL (it created your `CREATE TABLE tasks`).
3. *Repositories* — extending `JpaRepository<Task, Long>` gives `save/findAll/findById/deleteById`
   for FREE (no code). Plus *derived queries*: `findByCompleted(...)` → Spring writes
   `SELECT * FROM tasks WHERE completed = ?` from the method name. Hence "almost no SQL".

**Validation** — checks incoming data BEFORE your code runs.
- *Jakarta Bean Validation* = the rule annotations (`@NotBlank`, `@Size`, `@Pattern`, `@Min`…).
- *Hibernate Validator* = the engine that enforces them.
- You declare rules on the DTO fields; `@Valid` in the controller triggers the check; if a rule
  fails Spring throws `MethodArgumentNotValidException` → **400 Bad Request** (your method body
  never runs). Validating at the boundary means the Service can assume data is clean.

**H2 Database** — a full SQL database written in Java that runs INSIDE your app.
- `jdbc:h2:mem:taskdb` → `mem` = lives in RAM: zero setup, instant start, but **data resets on
  every restart** (why `GET /api/tasks/3` was empty after restarting).
- `spring.h2.console.enabled=true` → browse it live at `/h2-console`.
- Your Java code doesn't know it's H2 — switching to MySQL changes only `pom.xml` (driver) and
  the datasource URL/credentials in `application.properties`, no code changes. That decoupling
  is the payoff of the ORM layer. Use H2 for learning/tests, MySQL for a real persistent app.

### `pom.xml` — the project recipe
It declares the Spring Boot parent (which manages all library versions), the Java version,
and the four dependencies above. Maven reads it and downloads everything.
**Why:** without it, none of the Spring libraries exist and there is no way to build/run.

### `TaskManagerApplication.java` — the entry point
```java
package com.aarti.Task_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
```
- `@SpringBootApplication` = 3 things at once: enable auto-configuration (Tomcat, JPA, JSON),
  and **component-scan** this package and all sub-packages (so it finds your
  `@Entity`, `@Repository`, `@Service`, `@RestController`, `@RestControllerAdvice`).
- `main()` starts the embedded Tomcat server (port 8080) and wires all beans together.

**Why:** it is the single class you run to start the whole application.

---

## 3. Step 2 — Configure the database (`src/main/resources/application.properties`)

```properties
spring.application.name=Task-manager

server.port=8080

spring.datasource.url=jdbc:h2:mem:taskdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Line-by-line meaning:
- `server.port=8080` — the app listens on <http://localhost:8080>.
- `spring.datasource.url=jdbc:h2:mem:taskdb` — use an H2 database that lives **in memory**
  (`mem`), named `taskdb`. Data is wiped on every restart — perfect for learning.
- `username=sa`, `password=` — default H2 credentials (no password).
- `spring.h2.console.enabled=true` — enables a web page at `/h2-console` to browse the DB.
- `spring.jpa.hibernate.ddl-auto=update` — Hibernate creates/updates DB tables **from your
  entity classes** automatically at startup.
- `spring.jpa.show-sql=true` — prints the SQL Hibernate generates, so you can learn from it.

**Why:** it tells Spring *where* the database is and *how* JPA should behave — all without
touching code. Switching to MySQL later is just editing this file (+ the driver).

> Common mistake we hit: two properties ended up on one line
> (`...show-sql=truespring.application.name=...`) → app refused to start. Fix: one property per line.

---

## 4. Step 3 — The Model: `entity/Task.java`

```java
package com.aarti.Task_manager.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity                    // "this class is a database table"
@Table(name = "tasks")     // table name
public class Task {

    @Id                                                  // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment 1,2,3...
    private Long id;

    @Column(nullable = false)      // NOT NULL column
    private String title;

    private String description;
    private boolean completed;
    private String priority;       // LOW / MEDIUM / HIGH

    @Column(updatable = false)     // set once, never changed by updates
    private LocalDateTime createdAt;

    @PrePersist                    // runs automatically just before the first INSERT
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Task() {}               // JPA requires a no-argument constructor

    // getters & setters for every field (generated with Alt+Insert)
    // ...
}
```

Meaning of each annotation:
- `@Entity` — marks the class as a JPA entity; Hibernate maps it to a table.
- `@Table(name="tasks")` — names that table `tasks`.
- `@Id` — this field is the primary key.
- `@GeneratedValue(strategy = IDENTITY)` — the database auto-generates the id (1, 2, 3…).
- `@Column(nullable=false)` — that column cannot be null.
- `@Column(updatable=false)` — the value is set on insert and never overwritten on updates.
- `@PrePersist` — a lifecycle hook; the method runs right before the row is first saved
  (we use it to stamp `createdAt`).
- **getters/setters** — Spring reads/writes fields through these (e.g. to build JSON).

**Why we use it:** the entity is the bridge between Java and the database. Because of
`@Entity`, Hibernate auto-generated the `CREATE TABLE tasks (...)` (seen in the run log).
You describe the data shape in Java once; the table is built for you — no hand-written SQL.

---

## 5. Step 4 — The Repository: `repository/TaskRepository.java`

```java
package com.aarti.Task_manager.repository;

import com.aarti.Task_manager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // A "derived query": Spring writes the SQL from the method NAME.
    List<Task> findByCompleted(boolean completed);
}
```

Meaning:
- It is an **interface**, not a class — you write no method bodies.
- `extends JpaRepository<Task, Long>` — `Task` = entity type, `Long` = id type. From this you
  inherit ready-made methods: `save()`, `findAll()`, `findById()`, `deleteById()`, `count()`…
- `findByCompleted(boolean)` — a *derived query*: Spring reads the method name and generates
  `SELECT * FROM tasks WHERE completed = ?`.

**Why we use it (and why an interface):** Spring Data JPA generates the actual working
implementation at runtime. You only *declare* what you want. This removes almost all
boilerplate DB code. It must be an interface because you supply no implementation yourself.

---

## 6. Step 5 — The DTOs: `dto/TaskRequest.java` and `dto/TaskResponse.java`

**`TaskRequest`** — the JSON the client is allowed to SEND (validated):
```java
package com.aarti.Task_manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private boolean completed;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Priority must be LOW, MEDIUM or HIGH")
    private String priority;

    // getters & setters
}
```

**`TaskResponse`** — the JSON we SEND BACK:
```java
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

    public TaskResponse(Task t) {        // build a response from a Task entity
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.completed = t.isCompleted();
        this.priority = t.getPriority();
        this.createdAt = t.getCreatedAt();
    }
    // getters
}
```

Meaning:
- `@NotBlank` — the field must not be null/empty.
- `@Pattern(regexp=...)` — the value must match one of `LOW|MEDIUM|HIGH`.
- These rules run when the controller marks the parameter `@Valid`; if they fail → 400.

**Why we use DTOs (the "why not use the entity directly?" answer):**
- **Security** — the client cannot set `id` or `createdAt` (they aren't in `TaskRequest`).
- **Validation** — validate only the fields you accept.
- **Stability** — the API's JSON stays the same even if the DB table changes later.

DTO = *Data Transfer Object*: an object made only for moving data across the API boundary,
kept separate from the database entity.

---

## 7. Step 6 — The Service: `service/TaskService.java`

```java
package com.aarti.Task_manager.service;

import com.aarti.Task_manager.dto.*;
import com.aarti.Task_manager.entity.Task;
import com.aarti.Task_manager.exception.ResourceNotFoundException;
import com.aarti.Task_manager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service   // Spring manages one shared instance (a "bean")
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {   // constructor injection
        this.repo = repo;
    }

    // Helper: fetch a task or throw a clean 404. Reused by getById/update/delete.
    private Task findTaskOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public TaskResponse create(TaskRequest req) {
        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setCompleted(req.isCompleted());
        task.setPriority(req.getPriority());
        return new TaskResponse(repo.save(task));      // INSERT
    }

    public List<TaskResponse> getAll() {
        return repo.findAll().stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public TaskResponse getById(Long id) {
        Task task = findTaskOrThrow(id);
        return new TaskResponse(task);
    }

    public TaskResponse update(Long id, TaskRequest req) {
        Task task = findTaskOrThrow(id);
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setCompleted(req.isCompleted());
        task.setPriority(req.getPriority());
        return new TaskResponse(repo.save(task));      // UPDATE (same id)
    }

    public void delete(Long id) {
        Task task = findTaskOrThrow(id);
        repo.delete(task);
    }
}
```

Meaning:
- `@Service` — registers this as a Spring-managed bean (logic layer).
- **Constructor injection** — declaring `TaskRepository repo` in the constructor makes Spring
  pass in the repository it created. You never write `new TaskRepository()`.
- `repo.save(task)` — does INSERT when id is null, UPDATE when the id already exists.
- `.orElseThrow(...)` — `findById` returns an `Optional`; if empty (not found) it throws.
- `findTaskOrThrow` — one helper reused by getById/update/delete so the "throw 404 if
  missing" rule lives in a single place.
- `.stream().map(TaskResponse::new)` — convert each `Task` entity into a `TaskResponse` DTO.

**Why we use it:** controllers should only handle HTTP; repositories only touch the DB.
**All business logic and DTO↔entity conversion lives here.** This keeps it testable without a
web server and reusable across controllers.

---

## 8. Step 7 — The Controller: `controller/TaskController.java`

```java
package com.aarti.Task_manager.controller;

import com.aarti.Task_manager.dto.*;
import com.aarti.Task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                  // every returned object → JSON automatically
@RequestMapping("/api/tasks")   // common URL prefix for all methods
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {   // constructor injection
        this.service = service;
    }

    @PostMapping                                             // POST /api/tasks
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest req) {
        return new ResponseEntity<>(service.create(req), HttpStatus.CREATED);   // 201
    }

    @GetMapping                                              // GET /api/tasks
    public List<TaskResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")                                     // GET /api/tasks/5
    public TaskResponse getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")                                     // PUT /api/tasks/5
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")                                  // DELETE /api/tasks/5
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();           // 204
    }
}
```

Meaning of each annotation:
- `@RestController` — combines `@Controller` + `@ResponseBody`; return values become JSON.
- `@RequestMapping("/api/tasks")` — every method's URL starts with this.
- `@PostMapping` / `@GetMapping` / `@PutMapping` / `@DeleteMapping` — map an HTTP verb + path
  to a method.
- `@RequestBody` — convert the incoming JSON body into a `TaskRequest` object.
- `@Valid` — run the validation rules on that object before the method body executes.
- `@PathVariable` — pull `{id}` out of the URL (`/api/tasks/5` → `id = 5`).
- `ResponseEntity` — lets you set the exact HTTP status (201 Created, 204 No Content, …).

**Why we use it:** it is the only layer that knows about HTTP. It receives the request,
delegates the real work to the Service, and returns the HTTP response with the right status.

### REST endpoints summary
| Method | URL | Purpose | Success |
|--------|-----|---------|---------|
| POST | /api/tasks | create | 201 |
| GET | /api/tasks | list all | 200 |
| GET | /api/tasks/{id} | get one | 200 (404 if missing) |
| PUT | /api/tasks/{id} | update | 200 (404 if missing) |
| DELETE | /api/tasks/{id} | delete | 204 (404 if missing) |

---

## 9. Step 8 — Clean error handling: `exception/`

**`ResourceNotFoundException.java`** — our own error type:
```java
package com.aarti.Task_manager.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```
- `extends RuntimeException` — makes it a throwable exception.
- Thrown by the Service (`findTaskOrThrow`) when a task id does not exist.

**`GlobalExceptionHandler.java`** — turns exceptions into clean JSON:
```java
package com.aarti.Task_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice   // watches EVERY controller for exceptions
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);   // 404
    }
}
```
- `@RestControllerAdvice` — makes this class handle exceptions across all controllers.
- `@ExceptionHandler(ResourceNotFoundException.class)` — when THAT exception is thrown
  anywhere, run this method instead of crashing.
- It returns a tidy JSON body with HTTP **404**.

**Why we use it:** without it, a missing task returns an ugly **500** stack trace (because a
`RuntimeException` defaults to 500). With it, all error handling lives in ONE place,
controllers stay clean (no scattered try/catch), and clients get predictable error JSON.

> Gotcha we hit: after adding the handler you must **restart** the app, or it isn't loaded and
> you still get the default 500.

---

## 10. Step 9 — Run & test

1. Run `TaskManagerApplication`. Look for `Started TaskManagerApplication` and
   `Found 1 JPA repository interfaces` in the log.
2. Browse the DB at <http://localhost:8080/h2-console>
   (JDBC URL `jdbc:h2:mem:taskdb`, user `sa`, no password).
3. In Postman:

**Create** — POST `http://localhost:8080/api/tasks`, Body → raw → JSON:
```json
{ "title": "Finish report", "description": "Q3 numbers", "completed": false, "priority": "HIGH" }
```
→ 201 Created, returns the task with `id: 1`.

**List** — GET `http://localhost:8080/api/tasks` → 200, array of tasks.
**Get one** — GET `http://localhost:8080/api/tasks/1` → 200.
**Update** — PUT `http://localhost:8080/api/tasks/1` with a JSON body → 200.
**Delete** — DELETE `http://localhost:8080/api/tasks/1` → 204.
**404 test** — GET `http://localhost:8080/api/tasks/999` → clean 404 with a `message`.
**Validation test** — POST with `{ "priority": "WRONG" }` → 400 with field errors.

---

## 11. How one request touches every layer

`POST /api/tasks` with `{ "title": "Finish report", "priority": "HIGH" }`:

1. **Controller** — `@PostMapping` catches it; `@RequestBody` turns JSON → `TaskRequest`;
   `@Valid` checks the rules.
2. **Service** — copies the data into a `Task` entity; calls `repo.save(task)`.
3. **Repository** — turns `save()` into `INSERT INTO tasks ...`.
4. **Entity + DB** — the row is saved; the DB assigns `id = 1`.
5. Back up: Service wraps the saved `Task` in a `TaskResponse`; Controller returns it as JSON
   with **201 Created**.

If the id doesn't exist: Service throws `ResourceNotFoundException` →
`GlobalExceptionHandler` catches it → client gets a clean **404**.

---

## 12. File-by-file one-liners

| File | Job | Why it exists |
|------|-----|---------------|
| `pom.xml` | libraries + build | brings in Spring/JPA/H2; without it nothing compiles |
| `application.properties` | DB + app config | points to the database, sets JPA behavior |
| `TaskManagerApplication.java` | `main()`; starts app | single entry point; scans & wires beans |
| `entity/Task.java` | data model = DB table | maps Java ↔ database; auto-creates the table |
| `repository/TaskRepository.java` | DB access | free CRUD via Spring Data JPA |
| `dto/TaskRequest.java` | validated input JSON | safe boundary; validates client data |
| `dto/TaskResponse.java` | output JSON | controls exactly what is exposed |
| `service/TaskService.java` | business logic | rules + DTO↔entity conversion |
| `controller/TaskController.java` | REST endpoints | the HTTP layer |
| `exception/ResourceNotFoundException.java` | custom error | signals "task not found" |
| `exception/GlobalExceptionHandler.java` | central error handling | turns errors into clean JSON |

---

## 13. Key annotations glossary

| Annotation | Layer | Meaning |
|------------|-------|---------|
| `@SpringBootApplication` | main | start + auto-config + component scan |
| `@Entity`, `@Table` | model | class ↔ DB table |
| `@Id`, `@GeneratedValue` | model | primary key + auto-increment |
| `@Column` | model | column rules (nullable, updatable) |
| `@PrePersist` | model | run code before first insert |
| `@Repository` (implied) | data | data-access bean |
| `@Service` | logic | business-logic bean |
| `@RestController` | web | controller whose returns become JSON |
| `@RequestMapping` | web | base URL path |
| `@GetMapping/@PostMapping/@PutMapping/@DeleteMapping` | web | map HTTP verb → method |
| `@RequestBody` | web | JSON body → object |
| `@PathVariable` | web | URL segment → parameter |
| `@Valid` | web | trigger validation |
| `@NotBlank`, `@Pattern` | dto | validation rules |
| `@RestControllerAdvice` | error | global exception handling |
| `@ExceptionHandler` | error | handle a specific exception |

---

## 14. What to build next (practice)

- **Spring Security + JWT** so each user only sees their own tasks.
- **Pagination & sorting** with `Pageable`.
- **Switch to MySQL** (add the driver + update `application.properties`).
- **Unit tests** for the Service and `@WebMvcTest` for the Controller.
