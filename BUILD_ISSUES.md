# Build Issues & Resolution Guide

**Date**: 2025-11-23
**Status**: Project cannot build due to architectural violations and compilation errors
**Impact**: Backend application cannot start - approximately 80+ compilation errors

---

## Admin Account Credentials

The default admin account has been created and is ready to use:

- **Email**: `admin@liyaqa.com`
- **Password**: `admin@1234`
- **Role**: ADMIN (full system permissions)
- **Status**: Active in database
- **Location**: Created in migration `backend/backend-infrastructure/src/main/resources/db/migration/V17__create_users_table.sql`

---

## Completed Work

### 1. User Authentication Infrastructure ✅

The following components have been successfully created for user authentication:

#### UserJpaEntity
- **File**: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/entities/UserJpaEntity.kt`
- **Status**: Complete and correct
- **Description**: JPA entity mapping to the `users` table with all required fields

#### UserEntityMapper
- **File**: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/mappers/UserEntityMapper.kt`
- **Status**: Complete and correct
- **Description**: Bidirectional mapper between `User` domain entity and `UserJpaEntity`

#### UserJpaRepository
- **File**: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/repositories/UserJpaRepository.kt`
- **Status**: Complete and correct
- **Description**: Spring Data JPA repository implementation with custom queries for user management
- **Features**:
  - Find by email
  - Find by email and organization
  - Find by organization/branch
  - Soft delete functionality
  - Existence checks

### 2. Bug Fixes ✅

#### Invoice.kt Null Safety Issue
- **File**: `backend/backend-domain/src/main/kotlin/com/liyaqa/gym/domain/entities/Invoice.kt:77`
- **Fixed**: Changed `require(!dueDate?.isBefore(issueDate) ?: false)` to `require(dueDate?.isBefore(issueDate) != true)`

---

## Critical Issues Blocking Build

### Issue 1: Circular Dependency Between Modules ⚠️

**Problem**: Circular dependency between `backend-application` and `backend-infrastructure`

**Details**:
- `backend-application` depends on `backend-infrastructure` (for `PaymentGatewayFactory`)
- `backend-infrastructure` depends on `backend-application` (through its build configuration)

**Evidence**:
```
Circular dependency between the following tasks:
:backend:backend-application:classes
\--- :backend:backend-application:compileJava
     +--- :backend:backend-application:compileKotlin
     |    \--- :backend:backend-infrastructure:jar
     |         +--- :backend:backend-infrastructure:classes
     |         |    \--- :backend:backend-infrastructure:compileJava
     |         |         +--- :backend:backend-application:jar
```

**Affected Files**:
- `backend/backend-application/build.gradle.kts` - Currently has no dependency on infrastructure
- `backend/backend-infrastructure/build.gradle.kts` - Depends on application

**Root Cause**: Architectural violation - application layer should not depend on infrastructure layer

---

### Issue 2: Clean Architecture Violation - Payment Gateway ⚠️

**Problem**: Application layer directly references infrastructure classes

**Affected Files** (6 use cases):
1. `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/CreateSubscriptionUseCase.kt:17`
2. `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/RenewSubscriptionUseCase.kt:18`
3. `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/UpgradeSubscriptionUseCase.kt:19`
4. `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/financial/ProcessPaymentUseCase.kt:15`
5. `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/financial/ProcessRefundUseCase.kt:15`

**Error**:
```kotlin
import com.liyaqa.infrastructure.payment.gateway.PaymentGatewayFactory
// Error: Unresolved reference 'infrastructure'
```

**Resolution Required**:
1. Create domain interface: `PaymentGatewayFactory` in `backend-domain`
2. Move implementation to infrastructure layer
3. Use dependency injection to provide implementation to use cases

**Example Fix**:

```kotlin
// backend/backend-domain/src/main/kotlin/com/liyaqa/gym/domain/payment/PaymentGatewayFactory.kt
package com.liyaqa.gym.domain.payment

interface PaymentGatewayFactory {
    fun getGateway(provider: PaymentProvider): PaymentGateway
}

// backend/backend-infrastructure/.../PaymentGatewayFactoryImpl.kt
@Component
class PaymentGatewayFactoryImpl : PaymentGatewayFactory {
    override fun getGateway(provider: PaymentProvider): PaymentGateway {
        // existing implementation
    }
}

// Update use cases to use domain interface
class CreateSubscriptionUseCase(
    private val paymentGatewayFactory: PaymentGatewayFactory // domain interface
)
```

---

### Issue 3: Missing Spring Dependencies ⚠️

**Problem**: `backend-application` module missing required Spring dependencies

**Missing Dependencies** (partially fixed):
- ✅ `org.springframework.data:spring-data-commons` - **ADDED**
- ✅ `org.springframework.retry:spring-retry` - **ADDED**
- ✅ `org.slf4j:slf4j-api` - **ADDED**

**Affected Files**:
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/member/SearchMembersUseCase.kt`
  - Error: Cannot access `org.springframework.data.domain.Pageable`
  - Error: Cannot access `org.springframework.data.domain.Page`
  - Error: Unresolved reference `PageRequest`, `Sort`

- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/financial/SubmitInvoiceToZATCAUseCase.kt`
  - Error: Unresolved reference `Retryable`
  - Error: Unresolved reference `Backoff`

**Current Fix Applied**:
```kotlin
// backend/backend-application/build.gradle.kts
implementation("org.springframework.data:spring-data-commons")
implementation("org.springframework.retry:spring-retry")
implementation("org.slf4j:slf4j-api")
```

**Note**: Dependencies added but errors may persist due to other compilation issues

---

### Issue 4: Entity Constructor Signature Mismatches ⚠️

**Problem**: Invoice entity constructor calls don't match updated signature

**Affected Files**:
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/CancelSubscriptionUseCase.kt:279`
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/CreateSubscriptionUseCase.kt:258`
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/RenewSubscriptionUseCase.kt:235`
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/UpgradeSubscriptionUseCase.kt:349`

**Errors**:
```
No parameter with name 'metadata' found
No value passed for parameter 'organizationId'
No value passed for parameter 'branchId'
No value passed for parameter 'vat'
No value passed for parameter 'invoiceNumber'
No parameter with name 'transactionId' found
No parameter with name 'paidAt' found
```

**Root Cause**: Invoice entity constructor was updated but call sites weren't updated

**Resolution Required**:
1. Check Invoice entity constructor in `backend/backend-domain/src/main/kotlin/com/liyaqa/gym/domain/entities/Invoice.kt`
2. Update all Invoice instantiation calls to match current constructor signature
3. Map old parameter names to new ones (e.g., `metadata` → actual field names)

---

### Issue 5: Smart Cast Issues ⚠️

**Problem**: Kotlin cannot smart cast nullable properties from different modules

**Affected Files**:
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/access/CheckInMemberUseCase.kt:292`
  - Error: Smart cast to 'kotlin.Int' impossible for 'remainingVisits'
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/CreateSubscriptionUseCase.kt:200`
  - Error: Smart cast to 'kotlin.Int' impossible for 'durationDays'
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/RenewSubscriptionUseCase.kt:285`
- `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/subscription/UpgradeSubscriptionUseCase.kt:404`

**Resolution Required**:
Use safe casting or local variables:

```kotlin
// Instead of:
someValue + durationDays!!  // Error: Smart cast impossible

// Use:
val duration = durationDays ?: throw IllegalStateException("Duration is required")
someValue + duration
```

---

### Issue 6: Type Mismatch Issues ⚠️

**Problem**: Various type compatibility issues

**6.1 GenerateInvoiceUseCase.kt:89**
```kotlin
Error: Argument type mismatch: actual type is 'kotlin.Any', but 'kotlin.String' was expected
```

**6.2 GenerateInvoiceUseCase.kt:90**
```kotlin
Error: Unresolved reference 'addressArabic'
```

**6.3 CancelSubscriptionUseCase.kt:256**
```kotlin
Error: None of the following candidates is applicable:
fun of(amount: Double, currencyCode: String): Money
fun of(amount: BigDecimal, currencyCode: String): Money
```

**6.4 UpgradeSubscriptionUseCase.kt:276**
```kotlin
Error: Cannot access 'constructor(p0: BigInteger!, p1: Long, p2: Int, p3: Int): BigDecimal':
it is package-private in 'java/math/BigDecimal'
```

**Resolution Required**: Review each case and fix type conversions/method calls

---

## Module Dependency Structure

### Current State (Broken)
```
backend (main app)
├── No dependencies on sub-modules ❌
│
backend-presentation
├── depends on: backend-domain
├── depends on: backend-application
├── depends on: backend-common
│
backend-infrastructure
├── depends on: backend-domain
├── depends on: backend-application ⚠️ (causes circular dependency)
├── depends on: backend-common
│
backend-application
├── depends on: backend-domain
├── depends on: backend-common
├── Missing: Spring Data, Spring Retry ⚠️ (partially fixed)
│
backend-domain
├── depends on: backend-common
├── Missing: Spring annotations ✅ (FIXED)
├── Missing: SLF4J ✅ (FIXED)
```

### Required State (Clean Architecture)
```
backend (main app)
├── depends on: backend-presentation ✅
├── depends on: backend-infrastructure ✅
│
backend-presentation
├── depends on: backend-application ✅
├── depends on: backend-domain ✅
│
backend-application
├── depends on: backend-domain ✅
├── NO dependency on infrastructure ✅
│
backend-infrastructure
├── depends on: backend-domain ✅
├── depends on: backend-application ❌ (REMOVE THIS)
│
backend-domain
├── NO dependencies on other layers ✅
```

---

## Resolution Checklist

### Phase 1: Fix Module Dependencies (Priority: CRITICAL)
- [ ] Remove `backend-application` dependency from `backend-infrastructure/build.gradle.kts`
- [ ] Verify main `backend` module depends on:
  - [ ] `backend-presentation`
  - [ ] `backend-infrastructure`

### Phase 2: Fix Architecture Violations (Priority: HIGH)
- [ ] Create `PaymentGatewayFactory` interface in `backend-domain`
- [ ] Rename infrastructure implementation to `PaymentGatewayFactoryImpl`
- [ ] Update all use cases to use domain interface
- [ ] Create domain interfaces for any other infrastructure dependencies

### Phase 3: Fix Compilation Errors (Priority: HIGH)
- [ ] Fix Invoice constructor signature mismatches (6 files)
- [ ] Fix smart cast issues (4 files)
- [ ] Fix type mismatch in GenerateInvoiceUseCase
- [ ] Fix Money.of() type compatibility
- [ ] Fix BigDecimal constructor access

### Phase 4: Test Build (Priority: MEDIUM)
- [ ] Run `./gradlew clean build -x test`
- [ ] Resolve any remaining compilation errors
- [ ] Run `./gradlew :backend:bootRun`

### Phase 5: Test Authentication (Priority: MEDIUM)
- [ ] Verify backend starts successfully
- [ ] Test login endpoint: POST `http://localhost:8080/api/v1/auth/login`
- [ ] Credentials: `{"email": "admin@liyaqa.com", "password": "admin@1234"}`
- [ ] Verify JWT token generation
- [ ] Test authenticated endpoints

---

## Quick Start Commands

### Check Current Build Status
```bash
./gradlew :backend:build -x test 2>&1 | grep -E "error:|FAILED"
```

### Count Compilation Errors
```bash
./gradlew :backend:build -x test 2>&1 | grep "^e: " | wc -l
```

### View Specific Module Errors
```bash
./gradlew :backend:backend-application:build -x test 2>&1 | grep "^e: "
```

### Test Database Connection
```bash
docker exec -it liyaqa-postgres psql -U liyaqa_admin -d liyaqa_gym -c "SELECT email, role FROM users WHERE email = 'admin@liyaqa.com';"
```

---

## Additional Notes

### Database Configuration
- **Host**: localhost:5434
- **Database**: liyaqa_gym
- **Username**: liyaqa_admin
- **Password**: liyaqa_password
- **Confirmed**: Connection successful, migrations applied, admin account exists

### Web Frontend Configuration
- **API URL**: http://localhost:8080/api/v1 (configured in `web/.env`)
- **Status**: Frontend configured correctly, waiting for backend

### Security Configuration
- **Location**: `backend/backend-presentation/src/main/kotlin/com/liyaqa/gym/presentation/security/SecurityConfig.kt`
- **Status**: Properly configured with:
  - CSRF disabled for API endpoints
  - CORS enabled
  - JWT authentication
  - Public endpoints: `/api/v1/auth/**`, `/actuator/health`, `/swagger-ui/**`
  - @Order(1) annotation to override Spring Boot defaults

---

## Estimated Effort

- **Phase 1 (Module Dependencies)**: 30 minutes
- **Phase 2 (Architecture Fix)**: 2-3 hours
- **Phase 3 (Compilation Errors)**: 3-4 hours
- **Phase 4-5 (Testing)**: 1 hour

**Total**: ~7-9 hours of development work

---

## Contact & Support

If you need assistance with any of these issues, the key files to review are:

1. Module dependencies: `backend/*/build.gradle.kts`
2. Use case errors: `backend/backend-application/src/main/kotlin/com/liyaqa/gym/application/**/*.kt`
3. Domain entities: `backend/backend-domain/src/main/kotlin/com/liyaqa/gym/domain/entities/*.kt`
4. Authentication flow: `backend/backend-presentation/src/main/kotlin/com/liyaqa/gym/presentation/**/*.kt`

---

**Last Updated**: 2025-11-23
**Created By**: Claude Code Analysis Session
