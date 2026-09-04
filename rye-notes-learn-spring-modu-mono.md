# Rye Notes — Spring Boot Modular Monolith

## Tech Stack
- Java 21
- Spring Boot 3.4.x
- Maven
- PostgreSQL (lokal Windows)
- Flyway (database migration)
- Spring Security + JWT
- MapStruct + Lombok
- Springdoc OpenAPI (Swagger)
- Testcontainers (integration tests)
- Docker + Docker Compose
- Target deploy: Render.com

---

## Pelajaran & Catatan Penting

### application.yml vs application.properties
- Sama fungsinya, beda format
- `.properties` = flat, cocok untuk config sedikit
- `.yml` = hierarkis, lebih readable untuk config banyak
- Pakai `.yml` untuk project modern

### Profile Spring Boot
- `application.yml` = config utama
- `application-dev.yml` = override untuk lokal dev
- `application-prod.yml` = override untuk production
- Di production, set `SPRING_PROFILES_ACTIVE=prod` via environment variable

### Kenapa Flyway bukan ddl-auto update?
- `ddl-auto: update` praktis tapi berbahaya di production
- Tidak bisa rollback kalau schema salah
- Tidak ada history perubahan
- Flyway memberikan: history lengkap, rollback, semua environment pakai schema sama
- Best practice: `ddl-auto: validate` + Flyway

### Kenapa ddl-auto: validate?
- Hibernate hanya validasi schema, tidak buat/ubah apapun
- Kalau tabel tidak ada → error saat startup (fail fast)
- Memaksa kita selalu buat migration file dulu sebelum tambah entity

### Docker Desktop + Spring Boot (Windows)
- Docker Desktop di Windows versi lama tidak support `docker compose` (V2)
- Pakai `docker-compose` (V1 dengan strip)
- Untuk auth Postgres dari host ke container, pastikan `pg_hba.conf` pakai `md5` bukan `scram-sha-256`
- Lebih simpel: pakai Postgres yang sudah terinstall di Windows untuk dev lokal

### .env file
- Spring Boot tidak otomatis baca `.env`
- `.env` dibaca oleh Docker Compose saat deploy
- `.env.example` = template untuk orang lain yang clone repo
- `.env` masuk `.gitignore` supaya credentials tidak bocor ke repo
- Di production, nilai di-set via environment variable sistem atau secrets manager

### Render.com Free Tier
- RAM hanya 512MB — Spring Boot bisa OOM
- Solusi: limit JVM memory di Dockerfile
  `ENV JAVA_OPTS="-Xmx350m -Xms250m"`
- Sleep setelah 15 menit tidak ada request
- Cold start ~1-2 menit
- Pakai Docker untuk deploy, bukan Java/Maven langsung

### Database Migration / Schema Migration
- Teknik untuk versioning perubahan schema database
- Bagian dari best practice **Database Version Control**
- Konsepnya sama seperti Git untuk code, tapi untuk schema database
- Flyway tracking semua migration di tabel `flyway_schema_history`
- Setiap startup, Flyway cek tabel ini — file baru dijalankan, file lama di-skip
- **File migration yang sudah jalan tidak boleh diedit** — kalau mau ubah schema, buat file baru `V3__...sql`
- Kenapa best practice:
  - Reproducible: siapapun clone repo dapat schema yang sama
  - Auditable: history lengkap perubahan schema
  - Collaborative: semua anggota tim pakai schema yang sinkron
  - Safe deployment: schema berubah terkontrol di production
- Tools populer: **Flyway** (SQL murni, simpel) dan **Liquibase** (lebih powerful, support XML/YAML/JSON)

### Java record vs class untuk DTO
- `record` = fitur Java 16+
- Otomatis generate constructor, getter, equals, hashCode, toString
- Cocok untuk DTO yang immutable (hanya bawa data, tidak ada logic)
- Lebih ringkas dari `class` biasa

---

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
    ├── config/         → JpaConfig
    └── security/       → SecurityConfig
```

---

## Alur Data di Satu Modul

```
Request → Controller → Service → Repository → Database
                ↑           ↑
               DTO        Entity
```

- Entity = representasi tabel di DB
- DTO = data yang masuk/keluar via API (jangan expose entity langsung)
- Repository = interface untuk query DB
- Service = business logic
- Controller = handle HTTP request/response
- Mapper = konversi Entity ↔ DTO

---

### Refresh Token
- Solusi untuk access token yang expired tanpa harus login ulang
- Dua token: access token (expiry pendek, 15 menit) + refresh token (expiry panjang, 7 hari)
- Access token → dipakai untuk akses endpoint
- Refresh token → hanya dipakai untuk minta access token baru
- Alur:
  1. Login → dapat access token + refresh token
  2. Akses endpoint pakai access token
  3. Access token expired → kirim refresh token ke POST /auth/refresh
  4. Server validasi refresh token → kasih access token baru
  5. Refresh token expired → user harus login ulang
- Simpan refresh token di database supaya bisa di-revoke (logout, suspicious activity)
- Kalau pakai Redis nanti bisa lebih cepat, tapi butuh infrastruktur tambahan

### Rate Limiting
- Pembatas jumlah request dalam rentang waktu tertentu
- Penting di auth endpoint untuk mencegah brute force attack
- Tanpa rate limiter, attacker bisa coba password jutaan kali secara otomatis
- Dengan rate limiter: request ke-6 dalam 1 menit → 429 Too Many Requests
- Library: Bucket4j (simpel, pure rate limiting) atau Resilience4j (lebih lengkap)
- Cara kerja: setiap IP dapat "bucket" berisi N token, tiap request ambil 1 token, kalau habis → 429
- Kelemahan implementasi in-memory: reset saat app restart, tidak efektif di multi-instance
- Solusi production: simpan bucket di Redis supaya persistent dan shared antar instance

### JWT (JSON Web Token)
- Terdiri dari 3 bagian: `header.payload.signature`
- `header` = algoritma enkripsi
- `payload` = data user (id, email, role, expiry)
- `signature` = tanda tangan untuk validasi token tidak dimanipulasi
- Stateless — server tidak perlu simpan token, cukup validasi signature
- Access token expiry pendek (15 menit) untuk keamanan
- JWT secret harus random string minimal 256-bit di production

### Spring Security Filter Chain
- Request masuk → melewati serangkaian filter sebelum sampai ke Controller
- Urutan filter penting — rate limit harus sebelum JWT validation
- `addFilterBefore(filter, beforeFilter)` = jalankan `filter` sebelum `beforeFilter`
- `OncePerRequestFilter` = filter yang hanya dieksekusi sekali per request
- Filter yang kita buat:
  - `RateLimitFilter` → cek rate limit per IP
  - `JwtAuthFilter` → validasi JWT token

### GitHub Actions CI/CD
- Workflow disimpan di `.github/workflows/*.yml` di root repo
- `on: push: branches: [main]` — pipeline trigger saat push ke branch main
- `permissions: packages: write` — izin untuk push image ke GitHub Container Registry (ghcr.io)
- `actions/checkout@v4` — clone repo ke runner
- `actions/setup-java@v4` dengan `cache: maven` — setup JDK + cache dependency Maven supaya build lebih cepat
- `docker/login-action` — login ke ghcr.io pakai `GHCR_TOKEN` secret
- `docker/build-push-action` — build Dockerfile dan push image ke ghcr.io
- Image tersimpan di `ghcr.io/<username>/<repo-name>:latest`
- `RENDER_DEPLOY_HOOK_URL` — URL webhook dari Render, dipanggil dengan curl untuk trigger redeploy otomatis
- Secrets disimpan di GitHub repo → Settings → Secrets and variables → Actions
- `.github/workflows/` harus ada di root repo — GitHub tidak akan baca kalau ada di subfolder

### Dockerfile Multi-Stage Build
- Dockerfile bisa punya beberapa stage, masing-masing pakai base image berbeda
- **Stage 1 (builder)**: pakai `maven:3.9-eclipse-temurin-21-alpine` — compile + package JAR
- **Stage 2 (runner)**: pakai `eclipse-temurin:21-jre-alpine` — hanya JRE, copy JAR dari stage 1
- Hasilnya: image production kecil, tidak ada Maven, tidak ada source code
- `COPY --from=builder` = ambil file dari stage sebelumnya
- `RUN mvn dependency:go-offline` dulu sebelum copy src — supaya layer cache Maven tidak invalid setiap kode berubah
- `ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]` — pakai shell supaya env var `$JAVA_OPTS` bisa dibaca

### Docker Layer Caching
- Docker build bekerja layer per layer, setiap instruksi = 1 layer
- Layer di-cache — kalau tidak ada perubahan, Docker pakai cache (lebih cepat)
- Urutan instruksi penting: taruh yang jarang berubah di atas, yang sering berubah di bawah
- `COPY pom.xml` → `RUN mvn dependency:go-offline` → `COPY src` → `RUN mvn package`
- Kalau hanya src yang berubah, layer dependency tidak perlu di-rebuild

### docker-compose dev vs prod
- `docker-compose.dev.yml` — untuk lokal: ada Postgres container + app, pakai profile dev
- `docker-compose.prod.yml` — untuk production: hanya app, DB dari env var eksternal (Supabase)
- Jalankan dev: `docker compose -f docker-compose.dev.yml up`
- `depends_on: condition: service_healthy` — app tidak start sebelum Postgres benar-benar siap
- Di prod, tidak ada Postgres container — DB dihandle Supabase, cukup set `DB_URL` env var

### OOP — Parameter vs Field/Property
- **Field/Property** = variabel milik class, dideklarasikan di dalam class
- **Parameter** = nilai yang dikirim saat memanggil method, ada di dalam `()`
- **Argument** = nilai aktual yang dikirim saat method dipanggil
- **Method chaining** = memanggil method berantai pada object yang sama

---

## Roadmap

- [x] Step 1 — Project setup
- [x] Step 2 — Module User (Entity, Repository, Service, Controller, DTO, Mapper)
- [x] Step 3 — Spring Security + JWT + Refresh Token + Rate Limiting
- [x] Step 4 — Swagger / OpenAPI docs
- [x] Step 5 — Testing (unit + integration)
- [x] Step 6 — Dockerfile + Docker Compose production
- [x] Step 7 — GitHub Actions CI/CD
- [ ] Step 8 — Deploy ke Render + koneksi Supabase

---

## Refactor Modul User — Service Interface + Impl Pattern

### Apa yang diubah

1. **BaseEntity** — tambah field `deletedAt` untuk soft delete support di semua modul
2. **DTO dipindah** dari `module/user/dto/` ke `module/user/domain/dto/` — mengikuti pola `domain/` yang mengelompokkan dto bersama
3. **Service dipecah** jadi interface + impl:
   - `UserService` (interface) + `UserServiceImpl` (impl)
   - `AuthService` (interface) + `AuthServiceImpl` (impl)
   - `RefreshTokenService` (interface) + `RefreshTokenServiceImpl` (impl)
4. **`api/` folder** ditambahkan — berisi `UserApi.java` sebagai public interface modul user
5. **Fix typo** `/pofile` → `/profile` di `UserController`

### Kenapa Service harus Interface + Impl?

- Controller inject interface, bukan concrete class
- Kalau implementasi berubah (misal tambah caching, ganti logic), controller tidak perlu diubah
- Memudahkan unit test — bisa mock interface tanpa load Spring context
- Konsisten dengan pola yang dipakai di project referensi (SAT)

### Struktur Akhir Modul User

```
module/user/
├── api/
│   └── UserApi.java              ← public interface untuk modul lain
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── domain/
│   └── dto/
│       ├── AuthResponse.java
│       ├── LoginRequest.java
│       ├── RefreshTokenRequest.java
│       ├── RegisterRequest.java
│       └── UserResponse.java
├── entity/
│   ├── RefreshToken.java
│   └── User.java
├── mapper/
│   └── UserMapper.java
├── repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
└── service/
    ├── AuthService.java           ← interface
    ├── AuthServiceImpl.java       ← implementasi
    ├── RefreshTokenService.java   ← interface
    ├── RefreshTokenServiceImpl.java
    ├── UserService.java           ← interface
    └── UserServiceImpl.java       ← implements UserService + UserApi
```

### Aturan Dependency

- Controller → inject **interface** (UserService, AuthService)
- Modul lain → inject **UserApi** (bukan UserService)
- Tidak ada modul yang boleh import langsung ke `*ServiceImpl`

---

## Jackson Naming Strategy — camelCase vs snake_case

### Masalah Klasik

Java pakai **camelCase** untuk nama field, tapi JSON response bisa camelCase atau snake_case tergantung konvensi tim/API.

```java
// Java field
private String fullName;
private Instant createdAt;
```

```json
// Default Jackson output (camelCase)
{ "fullName": "John Doe", "createdAt": "..." }

// Kalau pakai SNAKE_CASE
{ "full_name": "John Doe", "created_at": "..." }
```

### Config Global — Cukup Satu Baris

Jackson otomatis konversi **semua field** tanpa perlu annotasi satu per satu.

**application.yml**
```yaml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
```

**application.properties**
```properties
spring.jackson.property-naming-strategy=SNAKE_CASE
```

### Pilihan Naming Strategy

| Strategy | Java field | JSON output |
|---|---|---|
| `LOWER_CAMEL_CASE` | `fullName` | `fullName` (default) |
| `SNAKE_CASE` | `fullName` | `full_name` |
| `UPPER_CAMEL_CASE` | `fullName` | `FullName` |
| `KEBAB_CASE` | `fullName` | `full-name` |
| `LOWER_CASE` | `fullName` | `fullname` |

### Rekomendasi

- **Spring + React/TypeScript** → `LOWER_CAMEL_CASE` (default) karena JavaScript native-nya camelCase
- **Spring + Python/Ruby client** → `SNAKE_CASE`
- **Public API** → ikuti konvensi yang sudah ada, dokumentasikan di Swagger

> Yang paling penting: **pilih satu, konsisten di seluruh project.** Jangan campur camelCase dan snake_case di response yang sama.

### Darimana Aturan Ini?

Ini bawaan **Jackson** (`com.fasterxml.jackson.databind.PropertyNamingStrategies`), bukan Spring Boot. Spring Boot hanya expose config-nya lewat `application.yml` via `spring.jackson.*` sehingga tidak perlu setup Jackson bean manual di Java code.

Referensi:
- Jackson docs → `PropertyNamingStrategies`
- Spring Boot docs → Application Properties, section `spring.jackson.*`
