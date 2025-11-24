# TODO Implementation Plan

**Date**: 2025-11-24
**Status**: Build Successful - Runtime Implementation Needed
**Build Status**: ✅ All modules compile successfully

---

## Priority 1: Critical Runtime Issues (Blocks Application Startup)

### 1.1 Missing BranchRepository Implementation
**Priority**: 🔴 CRITICAL
**Status**: ❌ NOT IMPLEMENTED
**Blocks**: Application startup, AuthService
**Estimated Time**: 2-3 hours

**Error**: `No qualifying bean of type 'com.liyaqa.gym.domain.repositories.BranchRepository' available`

**Required Work**:
1. Create `BranchJpaEntity` in infrastructure layer
2. Create `BranchEntityMapper` for domain/JPA conversion
3. Create `BranchJpaRepository` Spring Data interface
4. Create `BranchJpaRepositoryImpl` implementing domain repository
5. Add database migration for branches table (if not exists)
6. Register as Spring bean

**Related Files**:
- Domain: `backend/backend-domain/src/main/kotlin/com/liyaqa/gym/domain/repositories/BranchRepository.kt`
- New: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/entities/BranchJpaEntity.kt`
- New: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/mappers/BranchEntityMapper.kt`
- New: `backend/backend-infrastructure/src/main/kotlin/com/liyaqa/infrastructure/persistence/repositories/BranchJpaRepository.kt`

---

## Priority 2: Integration & External Systems (3-4 hours)

### 2.1 Access Control System Integration
**Files**: `SyncAccessControlSystemUseCase.kt:201, 234, 258`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Integrate with actual access control system
// TODO: Implement actual blacklist sync
// TODO: Implement offline queue
```

**Required Work**:
- Design access control system API interface
- Implement blacklist synchronization
- Add offline queue for failed sync attempts
- Add retry logic and error handling

---

### 2.2 Notification System Integration
**Files**: `NotificationEventListener.kt:266`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Integrate with email/SMS/push notification service
```

**Required Work**:
- Choose notification provider (SendGrid, Twilio, Firebase)
- Implement email notifications
- Implement SMS notifications
- Implement push notifications
- Add notification templates
- Add notification preferences

---

### 2.3 Audit Storage System
**Files**: `AuditEventListener.kt:241`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Integrate with audit storage system
```

**Required Work**:
- Design audit log storage (separate database, Elasticsearch, etc.)
- Implement audit log writer
- Add audit log retention policy
- Add audit log search functionality

---

### 2.4 Analytics & Metrics
**Files**: `AnalyticsEventListener.kt:324, KafkaErrorHandler.kt:103, 108`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Store in time-series database
// TODO: Integrate with metrics system (Prometheus, Micrometer, etc.)
// TODO: Implement error rate monitoring and alerting
```

**Required Work**:
- Set up time-series database (InfluxDB, TimescaleDB)
- Integrate Micrometer for metrics
- Set up Prometheus exporters
- Configure Grafana dashboards
- Implement alerting rules

---

## Priority 3: Dead Letter Queue & Error Handling (1-2 hours)

### 3.1 Kafka DLQ Implementation
**Files**: `KafkaErrorHandler.kt:95`
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: Implement DLQ publishing
```

**Required Work**:
- Configure DLQ topics
- Implement DLQ message publisher
- Add DLQ monitoring
- Add DLQ replay mechanism

---

## Priority 4: Security & Authentication (2-3 hours)

### 4.1 Security Context Integration
**Files**:
- `UpdateMemberProfileUseCase.kt:249`
- `MemberController.kt:332, 399`
- `EquipmentController.kt:386`

**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Get from security context
// TODO: Get current user ID from SecurityContext
```

**Required Work**:
- Implement SecurityContextHolder access
- Create utility methods for current user
- Add user context propagation
- Handle authentication in use cases

---

## Priority 5: Business Logic & Features (4-6 hours)

### 5.1 Class Attendance Tracking
**Files**: `MarkClassAttendanceUseCase.kt:189`
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: In a real implementation:
// - Validate attendance records
// - Handle late arrivals
// - Track no-shows
```

**Required Work**:
- Implement attendance validation rules
- Add late arrival handling
- Implement no-show tracking
- Add attendance reports

---

### 5.2 Member Data Export (GDPR)
**Files**: `DeleteMemberUseCase.kt:115, 237`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Implement actual data export
// TODO: Add business rules
```

**Required Work**:
- Implement GDPR-compliant data export
- Generate member data archive
- Add export format support (JSON, CSV, PDF)
- Implement data deletion verification

---

### 5.3 Subscription Management
**Files**: `AutoRenewSubscriptionJob.kt:286`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Publish RenewalFailedEvent when it's added to domain events
```

**Required Work**:
- Add RenewalFailedEvent to domain events
- Implement event publishing
- Add retry logic for failed renewals
- Notify members of renewal failures

---

### 5.4 Equipment Management
**Files**: `EquipmentController.kt` (multiple locations)
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: Implement via use case (lines 75, 130, 194, 267, 331, 377, 436)
```

**Required Work**:
- Create equipment use cases
- Implement equipment CRUD operations
- Add maintenance tracking
- Implement maintenance schedule
- Add equipment reports

---

### 5.5 Member Query Features
**Files**: `MemberController.kt:425, 439, 445, 459, 465, 479`
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: Implement when subscription query use cases are available
// TODO: Implement when booking query use cases are available
// TODO: Implement when attendance query use cases are available
```

**Required Work**:
- Create subscription query use cases
- Create booking query use cases
- Create attendance query use cases
- Implement pagination and filtering
- Add sorting capabilities

---

### 5.6 Analytics Dashboard
**Files**: `AnalyticsController.kt` (lines 66, 136, 196, 252, 312, 367, 428)
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: Implement via use case
```

**Required Work**:
- Create analytics use cases
- Implement revenue analytics
- Implement member analytics
- Implement class analytics
- Create dashboard aggregations
- Add date range filtering

---

## Priority 6: Payment & Webhooks (2-3 hours)

### 6.1 Payment Result Integration
**Files**: `SubscriptionController.kt:204, 373`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Get actual payment ID from result
```

**Required Work**:
- Extract payment ID from gateway response
- Store payment reference in subscription
- Add payment status tracking
- Implement payment reconciliation

---

### 6.2 Webhook Security
**Files**: `PaymentController.kt:314`
**Status**: ❌ TODO
**Priority**: 🔴 HIGH

```kotlin
// TODO: Verify webhook signature
```

**Required Work**:
- Implement webhook signature verification
- Add replay attack prevention
- Implement webhook retry handling
- Add webhook logging and monitoring

---

### 6.3 Repository Methods
**Files**:
- `MembershipPlanController.kt:76`
- `SubscriptionController.kt:470`

**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Implement findAll in repository
```

**Required Work**:
- Add findAll methods to repositories
- Implement pagination support
- Add filtering capabilities
- Optimize queries for large datasets

---

## Priority 7: Notifications & Communications (2-3 hours)

### 7.1 Class Schedule Notifications
**Files**: `ClassScheduleController.kt:401, 518`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: Implement notification service to notify all booked members
// TODO: Send notifications to all affected members
```

**Required Work**:
- Implement member notification service
- Add email notifications for schedule changes
- Add SMS notifications (optional)
- Add push notifications (optional)
- Implement notification preferences

---

### 7.2 Member Suspension Notifications
**Files**: `SuspendMemberUseCase.kt:88`
**Status**: ❌ TODO
**Priority**: 🟢 LOW

```kotlin
// TODO: Send notification to member if requested
```

**Required Work**:
- Add suspension notification template
- Implement notification sending
- Add notification preferences check
- Track notification delivery

---

## Priority 8: PDF & File Generation (1-2 hours)

### 8.1 Invoice PDF Generation
**Files**: `InvoiceController.kt:183`
**Status**: ❌ TODO
**Priority**: 🟡 MEDIUM

```kotlin
// TODO: In production, read actual PDF file from storage
```

**Required Work**:
- Choose PDF library (iText, Apache PDFBox)
- Create invoice PDF template
- Implement PDF generation
- Store PDFs in file storage (S3, local, etc.)
- Add PDF retrieval endpoint

---

## Summary by Priority

### 🔴 CRITICAL (Must Fix for Production)
- Missing BranchRepository Implementation (2-3h)
- Webhook signature verification (1h)

**Total**: 3-4 hours

### 🟡 MEDIUM (Important but not blocking)
- Access Control System Integration (3-4h)
- Notification System Integration (2-3h)
- Audit Storage System (2h)
- Analytics & Metrics (2h)
- Security Context Integration (2h)
- Member Data Export (GDPR) (2h)
- Subscription Renewal Events (1h)
- Payment ID Integration (1h)
- Repository findAll methods (1h)
- Class Schedule Notifications (2h)
- Invoice PDF Generation (1-2h)

**Total**: 18-21 hours

### 🟢 LOW (Nice to have, can defer)
- Kafka DLQ Implementation (2h)
- Class Attendance Tracking (3h)
- Equipment Management (4h)
- Member Query Features (3h)
- Analytics Dashboard (4h)
- Member Suspension Notifications (1h)

**Total**: 17 hours

---

## Overall Effort Estimate

- **Critical**: 3-4 hours
- **Medium Priority**: 18-21 hours
- **Low Priority**: 17 hours
- **Total**: **38-42 hours** (~5-6 working days)

---

## Recommended Implementation Order

1. **Day 1** (8h): BranchRepository + Webhook Security + Notification System basics
2. **Day 2** (8h): Access Control Integration + Audit Storage + Security Context
3. **Day 3** (8h): Analytics & Metrics + Payment Integration + Repository methods
4. **Day 4** (8h): Class Notifications + PDF Generation + GDPR Export
5. **Day 5** (8h): Equipment Management + Member Query Features
6. **Day 6** (8h): Analytics Dashboard + Attendance Tracking + DLQ + Final Testing

---

## Notes

- All compilation errors have been resolved ✅
- Backend builds successfully ✅
- Flyway migrations work correctly ✅
- JPA configuration is correct ✅
- Main blocker is missing repository implementations
- Most TODOs are for integrations with external systems
- Consider prioritizing based on business requirements

**Last Updated**: 2025-11-24 19:45
