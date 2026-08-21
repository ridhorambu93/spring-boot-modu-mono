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

---

## Roadmap

- [x] Step 1 — Project setup
- [x] Step 2 — Module User (Entity, Repository, Service, Controller, DTO, Mapper)
- [ ] Step 3 — Spring Security + JWT
- [ ] Step 4 — Swagger / OpenAPI docs
- [ ] Step 5 — Testing (unit + integration)
- [ ] Step 6 — Dockerfile + Docker Compose production
- [ ] Step 7 — GitHub Actions CI/CD
- [ ] Step 8 — Deploy ke Render + koneksi Supabase
