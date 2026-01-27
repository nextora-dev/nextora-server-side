# 🎓 Nextora - University Campus Management System

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

A comprehensive backend system for managing all aspects of university campus life.

## ✨ Features

- 🔐 **Multi-role Authentication** - Students, Staff, Admins with JWT
- 🎓 **Club Management** - Create clubs, manage memberships
- 🗳️ **Voting System** - Secure elections with anonymous voting
- 📖 **Kuppi Sessions** - Peer tutoring management
- 📅 **Event Management** - Campus events and registrations
- 🏠 **Boarding Houses** - Student accommodation listings
- 🔍 **Lost & Found** - Report and claim lost items
- 💼 **Internships** - Opportunity management
- 💳 **Payments** - Campus service payments

## 🚀 Quick Start

```bash
# Clone the repository
git clone <repository-url>
cd server-side-spingboot

# Setup (creates .env, checks prerequisites)
./scripts/setup.sh

# Start the application
./scripts/start.sh
```

**Access:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## 📋 Prerequisites

- Java 17+
- PostgreSQL 14+
- Redis (optional)
- Maven (or use included wrapper)

## 🛠️ Scripts

| Script | Description |
|--------|-------------|
| `./scripts/setup.sh` | Initial setup |
| `./scripts/start.sh` | Start application |
| `./scripts/stop.sh` | Stop application |
| `./scripts/build.sh` | Build JAR |
| `./scripts/test.sh` | Run tests |
| `./scripts/docker-start.sh` | Start with Docker |
| `./scripts/docker-stop.sh` | Stop Docker |
| `./scripts/db-reset.sh` | Reset database |

## 📖 Documentation

- [Infrastructure Guide](docs/INFRASTRUCTURE.md)
- [Use Cases & API Examples](docs/PROJECT_USE_CASES.md)

## 🏗️ Architecture

```
src/main/java/lk/iit/nextora/
├── common/         # Shared utilities
├── config/         # Configuration
├── infrastructure/ # Cross-cutting concerns
└── module/         # Business modules
    ├── auth/       # Authentication
    ├── club/       # Club management
    ├── voting/     # Elections
    ├── kuppi/      # Tutoring
    ├── event/      # Events
    └── ...
```

## 🐳 Docker

```bash
# Start with Docker
./scripts/docker-start.sh

# Stop
./scripts/docker-stop.sh
```

## 🧪 Testing

```bash
./scripts/test.sh
```

## 📝 License

[MIT License](LICENSE)

## 👥 Team

NextOra Team
