# 🚀 PFK Workspace

**PFK Workspace** is a multi-tenant workspace and project management backend system built with a modern Java stack.
It’s designed to support workspaces, projects, tasks, comments, and activity tracking with a strong focus on clean architecture, scalability, and production-ready patterns.

The project is structured as a **modular monolith** with clear boundaries between domains and is intended to evolve over time with features like background jobs, caching, file storage, and observability.

This repository focuses on building a solid, maintainable backend foundation that can support real-world use cases such as authentication, role-based access control, multi-tenancy, and audit logging.

---

## 🧱 Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL
* **Migrations:** Flyway
* **Cache:** Redis
* **Auth:** JWT (Access & Refresh Tokens)
* **Documentation:** OpenAPI / Swagger
* **Infrastructure:** Docker & Docker Compose
* **Cloud (for files):** AWS S3
* **Testing:** JUnit, Testcontainers (Postgres, Redis)

---

## 🏗️ Architecture

* Modular monolith with clear domain boundaries
* Layered structure (API, Application, Domain, Infrastructure)
* Workspace-based multi-tenancy
* Production-style concerns: validation, error handling, logging, and health checks


---

## ✨ What This Project Covers

* Authentication & user accounts
* Workspaces and membership management
* Projects, tasks, and comments
* Activity & audit logging
* Pagination, filtering, sorting, and search
* Caching, async jobs, and file uploads (planned/ongoing)
