# Spring Boot Modular Monolith Boilerplate

Production-ready Spring Boot boilerplate with modular monolith architecture.

## Tech Stack

- Java 21 + Spring Boot 3.4.x
- PostgreSQL + Flyway (migrations)
- Spring Security + JWT
- MapStruct + Lombok
- Springdoc OpenAPI (Swagger)
- Testcontainers (integration tests)
- Docker + Docker Compose

## Project Structure

```
src/main/java/com/boilerplate/
├── shared/
│   ├── audit/          → BaseEntity (id, createdAt, updatedAt)
│   ├── exception/      → AppException, GlobalExceptionHandler
│   └── response/       → ApiResponse<T> wrapper
├── module/
│   └── user/           → controller, service, repository, entity, dto, mapper
└── infrastructure/
    ├── config/         → JPA, OpenAPI configs
    └── security/       → JWT, SecurityConfig, filters
```

## Getting Started

### 1. Setup environment

```bash
cp .env.example .env
# Edit .env sesuai kebutuhan
```

### 2. Jalankan Postgres via Docker

```bash
docker compose up -d
```

### 3. Jalankan aplikasi

```bash
./mvnw spring-boot:run
```

### 4. Akses Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/auth/register | Register user baru |
| POST | /api/v1/auth/login | Login & dapat JWT |
| POST | /api/v1/auth/refresh | Refresh access token |
| GET | /api/v1/users/me | Get current user profile |

## Environment Variables

Lihat `.env.example` untuk daftar lengkap environment variables yang dibutuhkan.

## Running Tests

```bash
./mvnw test
```

> Integration test menggunakan Testcontainers, pastikan Docker Desktop berjalan.
