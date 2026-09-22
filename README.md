<h1 align="center">📋 Task Manager REST API</h1>

<p align="center">
  A small but complete REST API built with Java &amp; Spring Boot for managing tasks
  (a to-do backend). It supports full CRUD — Create, Read, Update, Delete — over HTTP
  with JSON, and follows the standard layered architecture used in production Spring Boot apps.
</p>

---

## ✨ Features

1. Full CRUD REST API for tasks
2. Layered architecture — Controller → Service → Repository → Entity
3. DTOs for safe, validated input/output (entities never exposed directly)
4. Bean Validation on incoming requests (`@NotBlank`, `@Pattern`)
5. Centralized exception handling with clean, consistent error responses
6. H2 in-memory database for zero-setup runs (easily switched to MySQL)
7. Auto-generated database schema via Hibernate

## 🛠️ Tech Stack

<img width="610" alt="Tech stack" src="https://github.com/user-attachments/assets/91e907da-d02f-4450-af1a-0b9bec77c24a" />

## 🏗️ Architecture

Each layer has a single responsibility and only talks to the layer directly below it:

<img width="581" alt="Architecture" src="https://github.com/user-attachments/assets/6a1c4e82-4fdb-4eec-8efd-eb04c1b8f779" />

1. **Controller** — handles HTTP only (URLs, verbs, status codes)
2. **Service** — business logic + DTO ↔ entity conversion
3. **Repository** — database access (Spring Data JPA)
4. **Entity** — the data model mapped to a table
5. **DTOs** — validated input / safe output at the API boundary
6. **Exception handling** — one place that turns errors into clean JSON

## 📁 Project Structure

<img width="573" alt="Project structure" src="https://github.com/user-attachments/assets/90d2451b-d7f0-4abd-9a62-7a8e21251c27" />

## 🚀 Getting Started

**Prerequisites:** Java 17+ and Maven (or the included `mvnw` wrapper).

```bash
./mvnw spring-boot:run
