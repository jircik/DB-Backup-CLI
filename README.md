# DB Backup CLI

A production-grade database backup tool built with **Java 21**, **Spring Boot 3.3**, **Spring Shell**, and **Spring Batch**. Runs as an interactive command-line application that connects to your databases, dumps them using native tools, compresses the output, and stores backups to local disk (or cloud storage in a future release).

---

## Features

- **Interactive shell** — type commands directly, get immediate feedback
- **Named connection profiles** — define databases once in `application.yml`, reference them by name at runtime
- **Full PostgreSQL support** — connection testing and full dumps via `pg_dump`
- **Automatic compression** — every dump is gzipped before storage
- **Local storage** — backups written to `~/.dbbackup/backups/<database>/`
- **Spring Batch pipeline** — every backup runs as a 5-step job: validate → dump → compress → store → notify
- **Structured JSON logging** — activate the `json` profile for log ingestion into ELK, Datadog, etc.
- **Job history** — all job executions tracked in an embedded H2 database at `~/.dbbackup/batchdb`

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Maven wrapper | included (`mvnw`) |
| `pg_dump` | 17+ (must match your PostgreSQL server version) |
| PostgreSQL server | 13+ |
| Docker | optional (for running a local PostgreSQL instance) |

### Installing `pg_dump` on Windows

1. Download the PostgreSQL installer from https://www.postgresql.org/download/windows/
2. Select only **Command Line Tools** during installation
3. Add `C:\Program Files\PostgreSQL\<version>\bin` to your `PATH`
4. Verify: `pg_dump --version`

---

## Getting Started

### 1. Clone and build

```bash
git clone https://github.com/your-username/db-backup-cli.git
cd db-backup-cli
./mvnw package -DskipTests
```

### 2. Configure a connection profile

Edit `src/main/resources/application.yml` and add your database under `dbbackup.profiles`:

```yaml
dbbackup:
  profiles:
    my-db:
      db-type: POSTGRES
      host: localhost
      port: 5432
      database: mydb
      username: myuser
      password: mypassword   # use --password flag at runtime instead for security
  storage:
    local:
      path: ${user.home}/.dbbackup/backups
```

> **Security note:** avoid committing passwords to version control. Leave `password` blank in the config and pass it via the `--password` flag at runtime.

### 3. Run

```bash
java -jar target/db-backup-cli-0.1.0-SNAPSHOT.jar
```

You will see the Spring Shell prompt:

```
shell:>
```

---

## Commands

### `test-connection` — verify database connectivity

```
shell:> test-connection --profile my-db
Connection successful → localhost:5432/mydb [PostgreSQL]
```

**Options:**

| Flag | Description |
|---|---|
| `--profile` | Named profile from `application.yml` |
| `--db-type` | Override: `POSTGRES` \| `MYSQL` \| `MONGODB` |
| `--host` | Override: database host |
| `--port` | Override: database port |
| `--database` | Override: database name |
| `--username` | Override: username |
| `--password` | Override: password |

---

### `backup` — run a full database backup

```
shell:> backup --profile my-db
Backup completed successfully.
Output: mydb/mydb-20260422-134709.sql.gz
```

**Options:**

| Flag | Default | Description |
|---|---|---|
| `--profile` | — | Named profile from `application.yml` |
| `--db-type` | — | Override: database type |
| `--host` | — | Override: database host |
| `--port` | — | Override: database port |
| `--database` | — | Override: database name |
| `--username` | — | Override: username |
| `--password` | — | Override: password |
| `--backup-type` | `FULL` | `FULL` \| `INCREMENTAL` \| `DIFFERENTIAL` |
| `--storage` | `local` | `local` \| `s3` |

You can use `--profile` to load base config and override individual fields:

```
shell:> backup --profile my-db --backup-type FULL --storage local
```

Or specify everything inline without a profile:

```
shell:> backup --db-type POSTGRES --host localhost --port 5432 \
               --database mydb --username myuser --password secret
```

---

## Configuration Reference

Full `application.yml` structure:

```yaml
spring:
  batch:
    job:
      enabled: false          # jobs are triggered manually via shell, not on startup
    jdbc:
      initialize-schema: always
  datasource:
    url: jdbc:h2:file:${user.home}/.dbbackup/batchdb   # job history database
    driver-class-name: org.h2.Driver
    username: sa
    password:
  shell:
    interactive:
      enabled: true

dbbackup:
  profiles:
    # define as many named profiles as you need
    local-pg:
      db-type: POSTGRES       # POSTGRES | MYSQL | MONGODB
      host: localhost
      port: 5432
      database: mydb
      username: dev
      password:               # leave blank, pass via --password at runtime
  storage:
    local:
      path: ${user.home}/.dbbackup/backups
    s3:                       # Phase 2 — not yet active
      bucket: my-backup-bucket
      region: us-east-1
  notifications:
    slack:
      webhook-url: ""         # Phase 2 — leave blank to disable
```

---

## Output Structure

Backups are stored under the configured `storage.local.path`:

```
~/.dbbackup/
├── backups/
│   └── mydb/
│       ├── mydb-20260422-134709.sql.gz
│       └── mydb-20260423-090512.sql.gz
└── batchdb.mv.db              ← Spring Batch job history
```

Filenames follow the pattern: `<database>-<yyyyMMdd>-<HHmmss>.sql.gz`

---

## Structured Logging

Activate JSON logging (for log aggregation platforms) by running with the `json` Spring profile:

```bash
java -jar db-backup-cli-0.1.0-SNAPSHOT.jar --spring.profiles.active=json
```

Each log line becomes a JSON object with `jobId`, `batchStatus`, `exitCode`, `durationMs`, and other structured fields — ready for ingestion into ELK, Datadog, Splunk, etc.

Default (human-readable) format:
```
13:47:09.248 INFO  c.d.batch.BackupJobListener - Backup job starting
13:47:09.506 INFO  c.d.batch.BackupJobListener - Backup job finished
```

---

## Architecture

The application is built around a **Spring Batch pipeline** that runs for every backup, whether triggered manually or (in a future release) on a schedule. All executions share the same code path.

```
CLI command
    └── BackupCommand
            └── syncJobLauncher.run(backupJob, params)
                    ├── Step 1: validateConnectionStep  → test JDBC connectivity
                    ├── Step 2: dumpStep                → run pg_dump, write .sql to temp dir
                    ├── Step 3: compressStep            → gzip the .sql file
                    ├── Step 4: storeStep               → copy .sql.gz to storage backend
                    └── Step 5: notifyStep              → send Slack notification (if configured)
```

**Key components:**

| Component | Role |
|---|---|
| `BackupStrategy` | Interface — one implementation per database type |
| `BackupStrategyRegistry` | Resolves the correct strategy by `DbType` |
| `StorageProvider` | Interface — one implementation per storage backend |
| `StorageProviderRegistry` | Resolves the correct provider by name |
| `ProfileResolver` | Merges named profile + CLI overrides into a `ProfileProperties` |
| `BackupJobListener` | Logs job start/finish, cleans up temp directories |
| `AppConfig` | Configures a synchronous `JobLauncher` (blocks until job completes) |

---

## Running Tests

Unit tests (no Docker required):

```bash
./mvnw test
```

Integration tests (requires Docker — Testcontainers pulls `postgres:16-alpine` automatically):

```bash
./mvnw verify
```

Integration tests that run `pg_dump` are automatically skipped when `pg_dump` is not on the PATH, so CI environments without PostgreSQL client tools won't fail.

---

## Roadmap

### Phase 2 — Storage & Notifications

- [ ] **Amazon S3 storage** — `S3StorageProvider` using AWS SDK v2; activate with `--storage s3`
- [ ] **Slack notifications** — post backup success/failure to a Slack channel via incoming webhook; configure `dbbackup.notifications.slack.webhook-url`

### Phase 3 — Scheduling

- [ ] **Scheduled backups** — define cron expressions per profile in `application.yml`; jobs run automatically via Spring Batch scheduler
- [ ] **Retention policy** — automatically delete backups older than N days

### Phase 4 — Additional Databases

- [ ] **MySQL / MariaDB** — `mysqldump`-based strategy
- [ ] **MongoDB** — `mongodump`-based strategy

### Phase 5 — Restore & Management

- [ ] **Restore command** — `restore --profile my-db --file mydb-20260422-134709.sql.gz`
- [ ] **List backups command** — `list-backups --profile my-db`
- [ ] **Incremental backups** — WAL-based incremental strategy for PostgreSQL

---

## Project Structure

```
src/
├── main/java/com/databackupcli/
│   ├── batch/
│   │   ├── BackupJobConfig.java        ← Step and Job bean definitions
│   │   ├── BackupJobListener.java      ← Lifecycle logging + temp dir cleanup
│   │   └── tasklets/                   ← One tasklet per pipeline step
│   ├── config/
│   │   ├── AppConfig.java              ← Synchronous JobLauncher
│   │   ├── BackupProperties.java       ← @ConfigurationProperties root
│   │   └── ProfileProperties.java      ← Per-profile connection config
│   ├── model/
│   │   ├── DbType.java
│   │   ├── BackupType.java
│   │   ├── BackupException.java
│   │   └── StorageException.java
│   ├── notification/
│   │   └── SlackNotifier.java          ← Phase 2 stub
│   ├── shell/
│   │   ├── BackupCommand.java
│   │   ├── ConnectionTestCommand.java
│   │   └── ProfileResolver.java
│   ├── storage/
│   │   ├── StorageProvider.java
│   │   ├── StorageProviderRegistry.java
│   │   └── LocalStorageProvider.java
│   ├── strategy/
│   │   ├── BackupStrategy.java
│   │   ├── BackupStrategyRegistry.java
│   │   └── postgres/
│   │       ├── PgDumpCommand.java
│   │       └── PostgresBackupStrategy.java
│   └── util/
│       └── CompressionUtil.java
└── main/resources/
    ├── application.yml
    └── logback-spring.xml
```

---

## License

MIT
