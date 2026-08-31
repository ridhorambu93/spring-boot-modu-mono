# Spring Boot Simple Modular Monolith Boilerplate

Spring Boot simple boilerplate with modular monolith architecture.

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

### 2. Jalankan Postgres via Docker (optional | bisa docker bisa yg sudah terinstall pada pc / laptop)

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

### Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/auth/register | Register user baru |
| POST | /api/v1/auth/login | Login & dapat JWT |
| POST | /api/v1/auth/refresh | Refresh access token |
| POST | /api/v1/auth/logout | Logout & revoke refresh token |

### User
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/users/profile | Get current user profile |
| GET | /api/v1/users/{id} | Get user by ID |

### Masterdata - Province ( role : Admin)
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/masterdata/provinces | Get all provinces |
| GET | /api/v1/masterdata/provinces/{id} | Get province by ID |
| POST | /api/v1/masterdata/provinces | Create province |
| PUT | /api/v1/masterdata/provinces/{id} | Update province |
| DELETE | /api/v1/masterdata/provinces/{id} | Soft delete province |

### Masterdata - City (role : Admin)
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/masterdata/cities | Get all cities (optional: ?provinceId=) |
| GET | /api/v1/masterdata/cities/{id} | Get city by ID |
| POST | /api/v1/masterdata/cities | Create city |
| PUT | /api/v1/masterdata/cities/{id} | Update city |
| DELETE | /api/v1/masterdata/cities/{id} | Soft delete city |

## Environment Variables

Lihat `.env.example` untuk daftar lengkap environment variables yang dibutuhkan.

## Running Tests

```bash
./mvnw test
```

## fyi : Integration test menggunakan Testcontainers, pastikan Docker Desktop berjalan.
