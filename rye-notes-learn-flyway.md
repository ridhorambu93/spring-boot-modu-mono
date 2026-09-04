# Rye Notes — Flyway

## Apa itu Flyway?

Flyway adalah library **database migration** — tugasnya memastikan struktur database (tabel, kolom, index, constraint) selalu sinkron dengan kode aplikasi secara otomatis dan terurut.

Konsepnya seperti Git untuk schema database. Setiap perubahan schema dicatat sebagai file SQL berurutan, dan Flyway yang mengeksekusinya.

---

## Cara Kerja

Flyway menyimpan riwayat migration di tabel khusus bernama `flyway_schema_history` di dalam database.

Setiap kali app start, Flyway:
1. Baca semua file `V*.sql` di `classpath:db/migration`
2. Cek `flyway_schema_history` — sudah sampai versi berapa?
3. Jalankan file yang belum dieksekusi secara berurutan
4. Kalau semua sudah dijalankan → lanjut start app normal

Contoh isi `flyway_schema_history`:

| version | description | script | checksum | success |
|---|---|---|---|---|
| 1 | init schema | V1__init_schema.sql | 123456 | true |
| 2 | create users table | V2__create_users_table.sql | 789012 | true |

---

## Aturan Penting

- **File migration yang sudah dijalankan tidak boleh diedit** — Flyway menyimpan checksum tiap file. Kalau file diubah, checksum tidak cocok → error saat startup
- Kalau mau ubah schema, selalu buat file baru: `V3__...sql`, `V4__...sql`, dst
- `flyway_schema_history` adalah satu-satunya sumber kebenaran Flyway — jangan hapus tabel ini

---

## Kenapa Flyway, bukan `ddl-auto: update`?

| | `ddl-auto: update` | Flyway |
|---|---|---|
| History perubahan | ❌ tidak ada | ✅ lengkap |
| Aman di production | ❌ berbahaya | ✅ terkontrol |
| Bisa rollback | ❌ tidak | ✅ bisa |
| Reproducible di semua env | ❌ tidak konsisten | ✅ konsisten |

Best practice: `ddl-auto: validate` + Flyway. Hibernate hanya validasi schema, Flyway yang kelola perubahannya.

---

## Skenario Error yang Sering Terjadi

**1. DB sudah ada isinya tapi belum pernah pakai Flyway**
```
Schema "public" tidak kosong, tapi flyway_schema_history tidak ada → error
```

**2. `flyway_schema_history` dihapus manual**
```
Flyway kehilangan "memori" → sama seperti skenario 1 → error
```

**3. File migration yang sudah jalan diedit**
```
Checksum file berbeda dengan yang tersimpan di flyway_schema_history → error
```

---

## Apa itu Baseline?

Baseline = titik awal yang kamu tetapkan secara manual ke Flyway.

Kamu bilang ke Flyway: *"kondisi database sekarang anggap sudah selesai, jangan migrate ulang dari V1, mulai catatnya dari sini."*

Flyway akan buat `flyway_schema_history` dengan satu entry bertanda `<< Flyway Baseline >>`, lalu lanjut migrate file berikutnya yang belum ada.

Config di `application.yml`:
```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
```

---

## Penjelasan Detail Config Baseline

### `baseline-on-migrate: true`
Izinkan Flyway membuat `flyway_schema_history` secara otomatis kalau belum ada, lalu langsung lanjut migrate. Tanpa ini, Flyway akan error kalau tabel history tidak ditemukan di DB yang sudah ada isinya.

### `baseline-version: 0`
Ini **bukan nomor versi migration kamu** (bukan V1, V2, dst). Ini adalah nomor yang Flyway tulis sebagai **entry pertama** di `flyway_schema_history` sebagai penanda titik awal sebelum migration beneran dimulai.

Hasil di `flyway_schema_history`:

| version | description | script |
|---|---|---|
| 0 | `<< Flyway Baseline >>` | — (bukan SQL file, hanya penanda) |
| 1 | init schema | V1__init_schema.sql |
| 2 | create users table | V2__create_users_table.sql |
| ... | ... | ... |

**Kenapa harus `0`, bukan `1`?**

Kalau `baseline-version: 1`, Flyway anggap V1 sudah pernah dijalankan dan **skip V1**. Pakai `0` supaya semua file dari V1 ke atas tetap dieksekusi — cocok untuk DB yang benar-benar kosong.

| baseline-version | Efek |
|---|---|
| `0` | Semua V1, V2, V3... dijalankan |
| `1` | V1 di-skip, mulai dari V2 |
| `5` | V1-V5 di-skip, mulai dari V6 |

**Perlu diganti ke depannya?**

Tidak perlu. `baseline-version: 0` adalah config permanen. Mau nanti ada V10, V20, V100 pun config ini tetap `0`. Yang berubah hanya file migration kamu, config Flyway tidak perlu disentuh lagi.

**Apakah migration bergantung dari code?**

Ya, sepenuhnya. Flyway baca semua file `V*.sql` yang ada di `src/main/resources/db/migration/`. Selama file-file itu ada dan konsisten, Flyway akan selalu bisa rebuild schema dari nol di DB manapun.

---

## Case Nyata — Pindah DB ke `db-app-banking`

**Situasi:**
- Nama database diganti ke `db-app-banking`
- `flyway_schema_history` di-copy dari DB lama, tapi tabel-tabel fisiknya tidak ikut
- Flyway baca history dan bilang "sudah V9, skip semua" — padahal DB kosong
- Hibernate validasi dan complain: `Schema-validation: missing table [cities]`

**Pelajaran: jangan copy `flyway_schema_history` dari DB lama**

`flyway_schema_history` hanyalah "catatan", bukan data. Kalau tabel fisiknya tidak ikut terbawa, Flyway skip semua migration dan Hibernate akan error karena tabel tidak ada.

**Error pertama** — saat DB kosong tanpa history:
```
Found non-empty schema(s) "public" but no schema history table.
```

**Error kedua** — saat history di-copy tapi tabel fisik tidak ada:
```
Schema-validation: missing table [cities]
```

**Solusi yang benar untuk pindah ke DB baru kosong:**

Tambah config ini di `application.yml`:
```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
```

Lalu jalankan app ke DB yang benar-benar kosong. Flyway akan:
1. Deteksi `flyway_schema_history` belum ada
2. Buat tabel history dengan baseline version 0
3. Jalankan semua V1-V9 secara berurutan
4. Semua tabel terbentuk dari file migration yang ada di code

Log yang diharapkan:
```
flyway_schema_history does not exist yet
Creating Schema History table with baseline...
Successfully baselined schema with version: 0
Migrating schema to version 1 - init schema
Migrating schema to version 2 - create users table
...
Successfully applied 9 migrations to schema "public"
```

---

## Kesimpulan

> Selama `flyway_schema_history` ada dan konsisten dengan file migration, Flyway akan selalu happy. Kalau pindah DB baru yang kosong, Flyway buat sendiri. Kalau DB sudah ada isinya, harus pakai baseline atau recreate.

---

## Case Nyata — Checksum Mismatch & Cara Repair

**Situasi:**
- File migration V6/V7 pernah diedit setelah sudah dijalankan ke DB
- Ada duplikat file V6 yang nyangkut di `target/` dari build lama
- Setelah `mvn clean`, checksum yang tersimpan di DB tidak cocok dengan file lokal

**Error yang muncul:**
```
Migration checksum mismatch for migration version 6
-> Applied to database : -512061570
-> Resolved locally    : 2114852429
Either revert the changes to the migration, or run repair to update the schema history.
```

**Penyebab:**
Flyway menyimpan checksum tiap file di `flyway_schema_history`. Kalau file diubah setelah dijalankan — bahkan spasi atau newline pun dihitung — checksum tidak cocok dan Flyway error saat startup.

**Solusi — Flyway Repair via Maven Plugin**

Tambah Flyway Maven Plugin di `pom.xml`:
```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/db-app-banking</url>
        <user>postgres</user>
        <password>postgres</password>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.4</version>
        </dependency>
    </dependencies>
</plugin>
```

Lalu jalankan:
```bash
./mvnw flyway:repair
```

Perintah ini akan update checksum di `flyway_schema_history` agar sinkron dengan file migration lokal.

**Kenapa pakai Maven Plugin, bukan edit DB langsung?**

| | Maven Plugin (`flyway:repair`) | Edit DB langsung |
|---|---|---|
| Cara resmi Flyway | ✅ | ❌ workaround |
| Audit trail tercatat | ✅ | ❌ bypass mekanisme Flyway |
| Reproducible | ✅ | ❌ rawan human error |
| Aman | ✅ | ⚠️ hanya untuk darurat |

> Aturan tetap sama: **jangan edit file migration yang sudah dijalankan ke DB**. Repair hanya untuk menyelamatkan kondisi dev yang sudah terlanjur.

---

## Flyway — Pedang Bermata Dua

Flyway sangat membantu, tapi bisa jadi bencana kalau salah pakai. Konsepnya sama seperti `php artisan migrate` di Laravel, bedanya Flyway lebih strict karena ada checksum validation.

> Satu file migration yang salah bisa block seluruh tim dari menjalankan aplikasi.

---

## Perbandingan Flyway vs Laravel Artisan Migrate

| | Flyway | Laravel Artisan |
|---|---|---|
| Rollback | ❌ tidak ada (buat migration baru) | ✅ ada `migrate:rollback` |
| Checksum validation | ✅ strict | ❌ tidak ada |
| File format | Pure SQL | PHP class |
| Repair tool | ✅ `flyway:repair` | ❌ tidak ada, langsung edit DB |

Justru karena Flyway tidak punya rollback, kamu harus **lebih hati-hati saat nulis migration** dibanding Laravel.

---

## Aturan Tim — Wajib Diikuti

### 🔴 Aturan #1 — Jangan pernah edit file migration yang sudah di-commit
Begitu file di-commit ke repo, anggap file itu **read-only selamanya**. Sudah jalan di DB manapun (dev sekalipun) → buat file baru.

```
❌ Edit V6__create_accounts_table.sql
✅ Buat V9__alter_accounts_add_column.sql
```

### 🔴 Aturan #2 — Test di local dulu sebelum push
Jangan push file migration yang belum dicoba jalan di local. Begitu di-commit dan tim lain pull → semua kena error yang sama.

### 🟡 Aturan #3 — Naming yang deskriptif
Nama file harus langsung menjelaskan isinya tanpa perlu dibuka.

```
❌ V9__update.sql
❌ V9__fix.sql
✅ V9__add_status_column_to_users.sql
✅ V10__rename_name_to_full_name_in_users.sql
```

### 🟡 Aturan #4 — Satu migration, satu concern
Jangan gabungkan banyak perubahan dalam satu file. Kalau ada yang salah, lebih mudah di-trace.

```
❌ V9__add_status_and_rename_column_and_add_index.sql
✅ V9__add_status_to_users.sql
✅ V10__rename_name_to_full_name_in_users.sql
✅ V11__add_index_accounts_user_id.sql
```

### 🟡 Aturan #5 — Jangan taruh logic bisnis di migration
Migration hanya untuk perubahan schema, bukan manipulasi data bisnis.

```sql
-- ❌ logic bisnis, tidak boleh
UPDATE users SET balance = balance * 1.1 WHERE type = 'PREMIUM';

-- ✅ pure schema change, boleh
ALTER TABLE users ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE';

-- ✅ data seeding yang berkaitan dengan schema baru, boleh
UPDATE users SET status = 'ACTIVE' WHERE role = 'ADMIN';
```

### 🟢 Aturan #6 — Backup DB sebelum migration di production
Tidak peduli sekecil apapun perubahannya — selalu backup sebelum migrate di production. Flyway tidak punya rollback, satu-satunya jalan balik adalah restore dari backup.

```
Sebelum deploy production:
1. Backup DB
2. Jalankan migration di staging dulu
3. Kalau staging aman → baru production
```

---

## Checklist Sebelum Buat Migration Baru

```
[ ] Nama file deskriptif dan sesuai konvensi Vx__nama_yang_jelas.sql
[ ] Hanya satu concern per file
[ ] Tidak ada logic bisnis, hanya schema change
[ ] Sudah dicoba jalan di local tanpa error
[ ] Tidak mengubah file migration yang sudah ada
[ ] Kalau production → DB sudah di-backup
```

---

## Kesimpulan

> Flyway bukan tools yang sulit, tapi butuh disiplin. Satu aturan yang paling penting: **file migration yang sudah di-commit tidak boleh diubah**. Sisanya mengikuti dengan sendirinya.
