# Spring Boot Simple Modular Monolith Boilerplate

Simple Boilerplate Spring Boot dengan arsitektur modular monolith

## Tech Stack

- Java 21 + Spring Boot 3.4.x
- PostgreSQL + Flyway
- Spring Security + JWT
- MapStruct + Lombok
- Springdoc OpenAPI (Swagger)
- Docker + Docker Compose (opsional)

## Struktur Project

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

## Cara Menjalankan

### 1. Setup environment

```bash
cp .env.example .env
# Sesuaikan isi .env
```

### 2. Jalankan Postgres (opsional, skip kalau sudah punya Postgres yg keinstall di lokal pribadi ya gaess!)

```bash
docker compose up -d
```

### 3. Jalankan aplikasi

```bash
./mvnw spring-boot:run
```

### 4. Swagger UI

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

### Masterdata - Province
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/masterdata/provinces | Get all provinces |
| GET | /api/v1/masterdata/provinces/{id} | Get province by ID |
| POST | /api/v1/masterdata/provinces | Create province |
| PUT | /api/v1/masterdata/provinces/{id} | Update province |
| DELETE | /api/v1/masterdata/provinces/{id} | Soft delete province |

### Masterdata - City
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/masterdata/cities | Get all cities (opsional: ?provinceId=) |
| GET | /api/v1/masterdata/cities/{id} | Get city by ID |
| POST | /api/v1/masterdata/cities | Create city |
| PUT | /api/v1/masterdata/cities/{id} | Update city |
| DELETE | /api/v1/masterdata/cities/{id} | Soft delete city |

## CORS & Note (jika ingin coba di local pribadi dan integrasi ke FE)

Konfigurasi CORS ada di `SecurityConfig.java`. Allowed origins dibaca dari env var `CORS_ALLOWED_ORIGINS`, default sudah di-set ke `http://localhost:5173` (Vite default port).

Kalau FE jalan di port lain, set di `.env`:

```env
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Multiple origins dipisah koma:

```env
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

## FYI : Kalau FE pakai axios dengan `baseURL` langsung ke `http://localhost:8080`, CORS check aktif dari browser — pastikan origin FE sudah terdaftar. Kalau FE pakai Vite proxy (`/api` → `localhost:8080`), CORS tidak diperlukan.

## Environment Variables

Lihat `.env.example` untuk daftar lengkap.

## Testing (OPSIONAL tapi bagus di lakuin wkwkwk!)

```bash
./mvnw test
```

Integration test pakai Testcontainers — Docker harus jalan kalau mau jalanin test ini. Untuk development biasa tidak perlu.
