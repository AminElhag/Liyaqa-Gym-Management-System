# Liyaqa Gym Management System

A comprehensive multi-platform gym management system built with modern technologies.

## Architecture Overview

This project follows a clean, modular architecture with the following structure:

```
liyaqa-gym-management/
├── backend/                    # Spring Boot backend (Clean Architecture)
│   ├── backend-domain/        # Domain entities and business rules
│   ├── backend-application/   # Use cases and application services
│   ├── backend-infrastructure/# Repositories and external services
│   ├── backend-presentation/  # REST controllers and DTOs
│   └── backend-common/        # Shared utilities
├── mobile/                    # Kotlin Multiplatform Mobile
│   ├── shared/               # Shared business logic
│   ├── androidApp/          # Android-specific UI
│   └── iosApp/              # iOS-specific UI
└── web/                      # React TypeScript web application
```

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.5.6
- **Language:** Kotlin 2.1.0
- **JDK:** 21
- **Build Tool:** Gradle Kotlin DSL
- **Database:** PostgreSQL 17
- **Cache:** Redis 7
- **Message Queue:** Apache Kafka
- **Security:** Spring Security + JWT
- **API Documentation:** SpringDoc OpenAPI (Swagger)
- **Testing:** JUnit 5, MockK, Testcontainers

### Mobile
- **Framework:** Kotlin Multiplatform
- **Android UI:** Jetpack Compose
- **iOS UI:** SwiftUI (via Kotlin/Native)
- **Networking:** Ktor Client
- **Serialization:** kotlinx.serialization

### Web
- **Framework:** React 18.3.1
- **Language:** TypeScript 5.7.2
- **Build Tool:** Vite
- **Styling:** Tailwind CSS
- **State Management:** Zustand
- **Data Fetching:** React Query
- **Form Management:** React Hook Form + Zod

## Prerequisites

### Backend Development
- JDK 21 or higher
- Docker & Docker Compose
- Gradle 8.5+ (or use the wrapper)

### Mobile Development
- Android Studio (for Android)
- Xcode 15+ (for iOS, macOS only)
- Kotlin 2.1.0+

### Web Development
- Node.js 20+ and npm/yarn/pnpm

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/AminElhag/Liyaqa-Gym-Management-System.git
cd Liyaqa-Gym-Management-System
```

### 2. Environment Setup

Copy the example environment file and configure it:

```bash
cp .env.example .env
```

Edit `.env` with your configuration values.

### 3. Start Infrastructure Services

Start PostgreSQL, Redis, and Kafka using Docker Compose:

```bash
docker-compose up -d
```

Verify services are running:

```bash
docker-compose ps
```

Access management tools:
- **PgAdmin:** http://localhost:5050 (admin@liyaqa.com / admin123)
- **Kafka UI:** http://localhost:8090

### 4. Backend Setup

Build the entire backend:

```bash
./gradlew :backend:build
```

Run the Spring Boot application:

```bash
./gradlew :backend:bootRun
```

Or run with specific profile:

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew :backend:bootRun
```

The backend API will be available at:
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs

### 5. Web Frontend Setup

Navigate to the web directory:

```bash
cd web
```

**CRITICAL:** Copy the example environment file and configure it:

```bash
cp .env.example .env
```

> ⚠️ **Important:** The `.env` file is required for the web application to work. Without it, you will get **404 errors** when logging in or making API calls. This file is not tracked by git for security reasons.

Install dependencies:

```bash
npm install
# or
yarn install
# or
pnpm install
```

Start development server:

```bash
npm run dev
```

The web application will be available at http://localhost:3000

### 6. Mobile Setup

#### Android

1. Open Android Studio
2. Open the `mobile` directory as a project
3. Sync Gradle
4. Run the `androidApp` configuration

Or via command line:

```bash
./gradlew :mobile:androidApp:installDebug
```

#### iOS (macOS only)

1. Navigate to iOS app directory:
   ```bash
   cd mobile/iosApp
   ```

2. Install CocoaPods dependencies:
   ```bash
   pod install
   ```

3. Open the workspace in Xcode:
   ```bash
   open iosApp.xcworkspace
   ```

4. Build and run from Xcode

## Project Structure Details

### Backend Modules

#### backend-domain
Contains domain entities, value objects, and business rules. No external dependencies.

```kotlin
// Example: Member entity
package com.liyaqa.gym.domain

data class Member(
    val id: UUID,
    val name: String,
    val email: Email,
    val membershipType: MembershipType
)
```

#### backend-application
Use cases and application services that orchestrate domain logic.

```kotlin
// Example: Register member use case
package com.liyaqa.gym.application

class RegisterMemberUseCase(
    private val memberRepository: MemberRepository
) {
    fun execute(request: RegisterMemberRequest): Member {
        // Business logic
    }
}
```

#### backend-infrastructure
Implements repositories, external service integrations, and data access.

```kotlin
// Example: JPA repository implementation
package com.liyaqa.gym.infrastructure

@Repository
class JpaMemberRepository : MemberRepository {
    // Implementation
}
```

#### backend-presentation
REST controllers, request/response DTOs, and API documentation.

```kotlin
// Example: REST controller
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val registerMemberUseCase: RegisterMemberUseCase
) {
    @PostMapping
    fun register(@RequestBody request: RegisterMemberDto): ResponseEntity<MemberDto>
}
```

#### backend-common
Shared utilities, constants, and helper functions used across modules.

### Clean Architecture Principles

The backend follows Clean Architecture with clear dependency rules:

1. **Domain** - No dependencies on outer layers
2. **Application** - Depends only on domain
3. **Infrastructure** - Implements application interfaces
4. **Presentation** - Depends on application layer

Dependency flow: Presentation → Application → Domain ← Infrastructure

## Development Guidelines

### Code Style

- **Kotlin:** Follow official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **TypeScript/React:** Follow Airbnb style guide
- Use meaningful variable and function names
- Write self-documenting code with comments for complex logic

### Testing

Backend tests:
```bash
./gradlew test
```

Web tests:
```bash
cd web && npm test
```

### Database Migrations

Database migrations should be managed using Flyway or Liquibase (to be added).

For development, you can use:
```yaml
spring.jpa.hibernate.ddl-auto: update
```

For production, always use:
```yaml
spring.jpa.hibernate.ddl-auto: validate
```

## API Documentation

Once the backend is running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Configuration Profiles

### Development (dev)
- Auto-updates database schema
- Detailed logging
- Hot reload enabled

### Production (prod)
- Validates schema only
- Optimized logging
- Enhanced security

### Test
- In-memory H2 database
- Embedded Kafka
- Fast test execution

## Monitoring and Health Checks

Spring Boot Actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## Troubleshooting

### Backend won't start
1. Check if ports 8080 is available
2. Verify database is running: `docker-compose ps`
3. Check logs: `docker-compose logs postgres`

### Database connection issues
1. Verify environment variables in `.env`
2. Check PostgreSQL is healthy: `docker-compose exec postgres pg_isready`
3. Reset database: `docker-compose down -v && docker-compose up -d`

### Kafka issues
1. Ensure Zookeeper is running before Kafka
2. Check Kafka UI: http://localhost:8090
3. View logs: `docker-compose logs kafka`

### Mobile build issues
1. Clean Gradle cache: `./gradlew clean`
2. Invalidate Android Studio caches: File → Invalidate Caches
3. Update Gradle wrapper: `./gradlew wrapper --gradle-version=8.5`

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add some feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a pull request

## License

This project is proprietary software. All rights reserved.

## Support

For issues and questions:
- Create an issue on GitHub
- Contact: support@liyaqa.com

## Roadmap

- [ ] User authentication and authorization
- [ ] Member management (CRUD)
- [ ] Class scheduling and booking
- [ ] Payment processing
- [ ] Attendance tracking
- [ ] Trainer management
- [ ] Equipment inventory
- [ ] Reporting and analytics
- [ ] Mobile app release
- [ ] Multi-language support
- [ ] Dark mode

## Architecture Decisions

### Why Clean Architecture?
- Clear separation of concerns
- Testability at every layer
- Independence from frameworks
- Flexibility to change implementations

### Why Kotlin Multiplatform?
- Share business logic between platforms
- Type-safe code sharing
- Native performance on both iOS and Android
- Single source of truth for domain logic

### Why Spring Boot 3.5+?
- Native image support with GraalVM
- Enhanced observability
- Better performance
- Virtual threads support (Project Loom)

### Why Microservices-ready?
- Kafka for event-driven architecture
- Redis for distributed caching
- Stateless design for horizontal scaling
- API-first approach

---

**Built with ❤️ by the Liyaqa Team**
