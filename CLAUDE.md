# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Task Manager backend — a Fudan University 3-week assignment implementing role-based task management with Leader/Worker RBAC, a task lifecycle state machine, and dual storage strategies (in-memory or MySQL).

## Build & Run

```bash
# Build
./mvnw compile

# Run (default: in-memory storage, port 8080)
./mvnw spring-boot:run

# Package
./mvnw package

# Test
./mvnw test

# Switch to MySQL storage — edit src/main/resources/application.yml:
#   storage.type: db
# Requires MySQL at localhost:3306/task_db (schema in schema.sql)
```

## Architecture

Layered: **Controller → Service → Repository**

- `controller/` — REST endpoints split by role (`LeaderController`, `WorkerController`). Controllers only forward requests and wrap responses; never modify task state directly.
- `service/impl/TaskServiceImpl` — All business logic lives here: state machine transitions, permission checks, optimistic lock retry logic. The `TaskService` interface is currently a stub.
- `repository/TaskRepository` — Interface with two implementations selected via `storage.type` config property:
  - `MemoryTaskRepositoryImpl` — ConcurrentHashMap + synchronized methods
  - `DbTaskRepositoryImpl` — JdbcTemplate with SQL-level optimistic locking (`WHERE version=?`)
- `common/` — `Result<T>` unified JSON wrapper (`{code, message, data}`), `ResultCode` error enums, `UserContext` (ThreadLocal), `AuthInterceptor`
- `exception/GlobalExceptionHandler` — Catches all exceptions, returns JSON; never exposes stack traces

## State Machine

```
(null) → PENDING              [Leader creates]
PENDING → IN_PROGRESS         [Worker claims]
PENDING/IN_PROGRESS → PAUSED  [Leader pauses]
PAUSED/ERROR_PAUSED → PENDING [Leader resumes]
IN_PROGRESS → COMPLETED       [Worker finishes]
IN_PROGRESS → ERROR_PAUSED    [Worker reports error]
```

Invalid transitions throw `BusinessException` with `STATUS_INVALID` (4003).

## Key Conventions

- **Identity**: No login. Requests carry `X-User-Id` and `X-User-Role` headers, extracted by `AuthInterceptor` into `UserContext` (ThreadLocal).
- **Optimistic locking**: `Task.version` field. Repository `update()` returns rows affected; `TaskServiceImpl.updateWithLocking()` throws `STATUS_CONFLICT` (4004) if 0 rows.
- **All responses are HTTP 200**: Business success/failure is in the `code` field of the JSON body. Error codes defined in `ResultCode`.
- **When adding repository methods**: Define in `TaskRepository` interface first, then implement in both `MemoryTaskRepositoryImpl` and `DbTaskRepositoryImpl`.
- **State transitions must only happen in `TaskServiceImpl`**, never in controllers.
- **Entity classes use Lombok `@Data`**.

## Incomplete Stubs

Several files are empty and need implementation: `WebConfig` (register interceptor), `BusinessException` (constructor), `CommonController` (`GET /api/tasks/{taskId}`), `Role`, `User`, `UserRepository`, `TaskService` interface. See `READMI_AI.md` and `TaskManager.md` for full requirements.
