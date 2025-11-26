# Tenant Access System Documentation

This document describes the multi-tenant access control system implemented in the Liyaqa Gym Management System.

## Overview

The tenant access system provides:

1. **Tenant Context Isolation** - Each request is associated with a specific tenant
2. **Feature-Based Access Control** - Restrict features based on subscription plans
3. **Resource Limits Enforcement** - Enforce limits on branches, members, staff, and storage
4. **Role-Based Authorization** - Different access levels for platform admins and tenant users

## Architecture

### Components

1. **TenantContextHolder** - Thread-local storage for tenant context
2. **TenantContextFilter** - HTTP filter that extracts and sets tenant context
3. **SecurityConfig** - Spring Security configuration with role-based authorization
4. **@RequiresFeature** - Annotation for feature flag checking
5. **FeatureCheckAspect** - AOP aspect that enforces feature requirements

### Request Flow

```
HTTP Request
    ↓
TenantContextFilter (extracts tenant from subdomain/header)
    ↓
JwtAuthenticationFilter (validates JWT token)
    ↓
SecurityConfig (checks role-based authorization)
    ↓
FeatureCheckAspect (checks @RequiresFeature if present)
    ↓
Controller Method
```

## Tenant Identification

Tenants can be identified in two ways:

### 1. Subdomain (Recommended for Web)

```
https://gold-gym.liyaqa.com/api/v1/members
                ↑
            tenant slug
```

### 2. HTTP Header (Recommended for Mobile Apps)

```http
GET /api/v1/members HTTP/1.1
Host: api.liyaqa.com
X-Tenant-Slug: gold-gym
Authorization: Bearer eyJhbGc...
```

## Usage Examples

### 1. Accessing Tenant Context in Services

```kotlin
@Service
class MemberService(
    private val memberRepository: MemberRepository
) {
    fun createMember(request: CreateMemberRequest): Member {
        // Get current tenant ID
        val tenantId = TenantContextHolder.getTenantId()

        // Check if tenant can add more members
        val currentCount = memberRepository.countByTenantId(tenantId)
        if (!TenantContextHolder.canAddMember(currentCount.toInt())) {
            throw TenantLimitExceededException(
                tenantId = tenantId,
                limitType = "members",
                currentValue = currentCount,
                maxValue = TenantContextHolder.require().limits.maxMembers.toLong()
            )
        }

        // Create member
        val member = Member.create(
            tenantId = tenantId,
            name = request.name,
            email = request.email,
            // ... other fields
        )

        return memberRepository.save(member)
    }
}
```

### 2. Feature-Based Access Control

```kotlin
@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    // Basic analytics - available to all plans
    @GetMapping("/basic")
    fun getBasicAnalytics(): ResponseEntity<BasicAnalytics> {
        val analytics = analyticsService.getBasicAnalytics()
        return ResponseEntity.ok(analytics)
    }

    // Advanced analytics - requires ADVANCED_ANALYTICS feature
    @GetMapping("/advanced")
    @RequiresFeature(PlatformFeature.ADVANCED_ANALYTICS)
    fun getAdvancedAnalytics(): ResponseEntity<AdvancedAnalytics> {
        // This will only execute if tenant has ADVANCED_ANALYTICS feature
        val analytics = analyticsService.getAdvancedAnalytics()
        return ResponseEntity.ok(analytics)
    }

    // Multi-branch analytics - requires MULTI_BRANCH feature
    @GetMapping("/branches/comparison")
    @RequiresFeature(PlatformFeature.MULTI_BRANCH)
    fun getBranchComparison(): ResponseEntity<BranchComparison> {
        val comparison = analyticsService.compareBranches()
        return ResponseEntity.ok(comparison)
    }

    // Requires at least one of the features
    @GetMapping("/custom-report")
    @RequiresFeature(
        features = [PlatformFeature.ADVANCED_ANALYTICS, PlatformFeature.API_ACCESS],
        requireAll = false
    )
    fun getCustomReport(): ResponseEntity<CustomReport> {
        val report = analyticsService.getCustomReport()
        return ResponseEntity.ok(report)
    }
}
```

### 3. Manual Feature Checking

```kotlin
@Service
class BranchService(
    private val branchRepository: BranchRepository
) {
    fun createBranch(request: CreateBranchRequest): Branch {
        val tenantId = TenantContextHolder.getTenantId()

        // Check if tenant has multi-branch feature
        TenantContextHolder.requireFeature(PlatformFeature.MULTI_BRANCH)

        // Check branch limit
        val currentCount = branchRepository.countByTenantId(tenantId)
        if (!TenantContextHolder.canCreateBranch(currentCount.toInt())) {
            throw TenantLimitExceededException(
                tenantId = tenantId,
                limitType = "branches",
                currentValue = currentCount,
                maxValue = TenantContextHolder.require().limits.maxBranches.toLong()
            )
        }

        // Create branch
        val branch = Branch.create(
            tenantId = tenantId,
            name = request.name,
            // ... other fields
        )

        return branchRepository.save(branch)
    }
}
```

### 4. Custom Branding Example

```kotlin
@RestController
@RequestMapping("/api/v1/tenant/branding")
@RequiresFeature(PlatformFeature.CUSTOM_BRANDING)
class BrandingController(
    private val tenantService: TenantService
) {

    @PostMapping("/logo")
    fun uploadLogo(@RequestParam("file") file: MultipartFile): ResponseEntity<LogoResponse> {
        // This entire controller requires CUSTOM_BRANDING feature
        val tenantId = TenantContextHolder.getTenantId()
        val logoUrl = tenantService.uploadLogo(tenantId, file)
        return ResponseEntity.ok(LogoResponse(logoUrl))
    }

    @PutMapping("/colors")
    fun updateColors(@RequestBody colors: BrandColors): ResponseEntity<Tenant> {
        val tenantId = TenantContextHolder.getTenantId()
        val tenant = tenantService.updateBrandColors(tenantId, colors)
        return ResponseEntity.ok(tenant)
    }
}
```

### 5. Storage Limit Checking

```kotlin
@Service
class FileStorageService {

    fun uploadFile(file: MultipartFile): FileMetadata {
        val tenantId = TenantContextHolder.getTenantId()
        val fileSizeMB = file.size / (1024 * 1024)

        // Get current storage usage
        val currentUsage = fileRepository.getTotalStorageUsageMB(tenantId)

        // Check if tenant can use additional storage
        if (!TenantContextHolder.canUseStorage(currentUsage, fileSizeMB)) {
            val limits = TenantContextHolder.require().limits
            throw TenantLimitExceededException(
                tenantId = tenantId,
                limitType = "storage",
                currentValue = currentUsage + fileSizeMB,
                maxValue = limits.maxStorageMB
            )
        }

        // Upload file
        return fileRepository.save(file)
    }
}
```

## Role-Based Authorization

### Platform Admin Routes

```kotlin
// Only accessible by users with PLATFORM_ADMIN role
@RestController
@RequestMapping("/api/v1/platform")
class PlatformAdminController {

    @GetMapping("/tenants")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    fun listAllTenants(): ResponseEntity<List<Tenant>> {
        // Platform admin can see all tenants
        return ResponseEntity.ok(tenantService.findAll())
    }
}
```

### Tenant-Specific Routes

```kotlin
// Accessible by tenant users (TENANT_OWNER, TENANT_ADMIN, etc.)
@RestController
@RequestMapping("/api/v1/members")
class MemberController {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TENANT_OWNER')")
    fun listMembers(): ResponseEntity<List<Member>> {
        // Automatically filtered by tenant context
        val tenantId = TenantContextHolder.getTenantId()
        return ResponseEntity.ok(memberService.findByTenantId(tenantId))
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TENANT_OWNER')")
    fun createMember(@RequestBody request: CreateMemberRequest): ResponseEntity<Member> {
        // Only admins and owners can create members
        val member = memberService.createMember(request)
        return ResponseEntity.ok(member)
    }
}
```

## Available Roles

### Platform Roles
- `PLATFORM_ADMIN` - System administrators who manage the entire platform

### Tenant Roles
- `TENANT_OWNER` - Gym owner (full access to tenant)
- `TENANT_ADMIN` - Gym administrator (high-level access)
- `TENANT_MANAGER` - Operations manager (medium-level access)
- `ADMIN` - Branch admin
- `STAFF` - Gym staff member
- `TRAINER` - Personal trainer
- `MEMBER` - Gym member (limited access)

## Platform Features

Features available based on subscription plan:

| Feature | Starter | Professional | Enterprise |
|---------|---------|--------------|------------|
| BASIC_MEMBERSHIP | ✅ | ✅ | ✅ |
| CLASS_BOOKING | ✅ | ✅ | ✅ |
| ACCESS_CONTROL | ✅ | ✅ | ✅ |
| FINANCIAL_REPORTS | ✅ | ✅ | ✅ |
| TRAINER_MANAGEMENT | ❌ | ✅ | ✅ |
| ADVANCED_ANALYTICS | ❌ | ✅ | ✅ |
| MULTI_BRANCH | ❌ | ✅ | ✅ |
| ZATCA_INTEGRATION | ❌ | ✅ | ✅ |
| CUSTOM_BRANDING | ❌ | ❌ | ✅ |
| API_ACCESS | ❌ | ❌ | ✅ |
| WHATSAPP_INTEGRATION | ❌ | ❌ | ✅ |

## Subscription Limits

| Limit | Starter | Professional | Enterprise |
|-------|---------|--------------|------------|
| Max Branches | 1 | 3 | 999 |
| Max Members | 500 | 2,000 | Unlimited |
| Max Staff | 10 | 30 | 999 |
| Max Storage | 5 GB | 50 GB | 1 TB |

## Error Handling

The system provides detailed error responses for tenant-related issues:

### Feature Not Available (403)

```json
{
  "type": "https://liyaqa.com/errors/feature-not-available",
  "title": "Feature Not Available",
  "status": 403,
  "detail": "This feature is not available on your current subscription plan",
  "feature": "ADVANCED_ANALYTICS",
  "tenantId": "123e4567-e89b-12d3-a456-426614174000",
  "upgradeMessage": "Please upgrade your subscription plan to access this feature"
}
```

### Limit Exceeded (429)

```json
{
  "type": "https://liyaqa.com/errors/limit-exceeded",
  "title": "Limit Exceeded",
  "status": 429,
  "detail": "You have reached the limit for members on your current plan",
  "limitType": "members",
  "currentValue": 500,
  "maxValue": 500,
  "tenantId": "123e4567-e89b-12d3-a456-426614174000",
  "upgradeMessage": "Please upgrade your subscription plan to increase your limits"
}
```

## Best Practices

### 1. Always Use TenantContextHolder in Services

```kotlin
// ✅ Good - Uses tenant context
@Service
class MemberService {
    fun findMembers(): List<Member> {
        val tenantId = TenantContextHolder.getTenantId()
        return memberRepository.findByTenantId(tenantId)
    }
}

// ❌ Bad - Doesn't filter by tenant
@Service
class MemberService {
    fun findMembers(): List<Member> {
        return memberRepository.findAll() // Returns all members from all tenants!
    }
}
```

### 2. Check Limits Before Creating Resources

```kotlin
// ✅ Good - Checks limits first
fun createBranch(request: CreateBranchRequest): Branch {
    val currentCount = branchRepository.countByTenantId(tenantId)
    if (!TenantContextHolder.canCreateBranch(currentCount.toInt())) {
        throw TenantLimitExceededException(...)
    }
    return branchRepository.save(branch)
}

// ❌ Bad - Creates without checking limits
fun createBranch(request: CreateBranchRequest): Branch {
    return branchRepository.save(branch) // Might exceed limits!
}
```

### 3. Use @RequiresFeature for Entire Controllers

```kotlin
// ✅ Good - Apply to entire controller if all endpoints need the feature
@RestController
@RequiresFeature(PlatformFeature.ADVANCED_ANALYTICS)
@RequestMapping("/api/v1/analytics/advanced")
class AdvancedAnalyticsController {
    // All endpoints automatically require ADVANCED_ANALYTICS
}

// ✅ Also Good - Apply to individual endpoints if they have different requirements
@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController {
    @GetMapping("/basic")
    fun getBasic() { ... } // No feature required

    @GetMapping("/advanced")
    @RequiresFeature(PlatformFeature.ADVANCED_ANALYTICS)
    fun getAdvanced() { ... } // Requires feature
}
```

### 4. Clean Up Context in Background Tasks

```kotlin
// ✅ Good - Uses executeWithContext helper
fun processBackgroundJob(tenantId: UUID) {
    val tenant = tenantRepository.findById(tenantId)
    val context = TenantContext.fromTenant(tenant)

    TenantContextHolder.executeWithContext(context) {
        // Your background task code here
        memberService.processMemberships()
    }
    // Context automatically cleaned up
}

// ❌ Bad - Might leak context
fun processBackgroundJob(tenantId: UUID) {
    val tenant = tenantRepository.findById(tenantId)
    val context = TenantContext.fromTenant(tenant)
    TenantContextHolder.set(context)
    memberService.processMemberships()
    // Forgot to clear context!
}
```

## Testing

### Unit Tests

```kotlin
@Test
fun `should enforce feature restriction`() {
    // Setup tenant context without ADVANCED_ANALYTICS feature
    val tenant = createTestTenant(features = setOf(PlatformFeature.BASIC_MEMBERSHIP))
    val context = TenantContext.fromTenant(tenant)

    TenantContextHolder.executeWithContext(context) {
        assertThrows<FeatureNotEnabledException> {
            analyticsController.getAdvancedAnalytics()
        }
    }
}

@Test
fun `should enforce member limit`() {
    val tenant = createTestTenant(maxMembers = 10)
    val context = TenantContext.fromTenant(tenant)

    // Create 10 members
    repeat(10) { memberRepository.save(createTestMember()) }

    TenantContextHolder.executeWithContext(context) {
        assertThrows<TenantLimitExceededException> {
            memberService.createMember(CreateMemberRequest(...))
        }
    }
}
```

### Integration Tests

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Test
    fun `should filter members by tenant`() {
        mockMvc.perform(
            get("/api/v1/members")
                .header("X-Tenant-Slug", "gold-gym")
                .header("Authorization", "Bearer $token")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[*].tenantId").value(everyItem(equalTo(goldGymTenantId))))
    }
}
```

## Security Considerations

1. **Tenant Isolation** - All data queries MUST filter by tenant ID
2. **Feature Validation** - Always use @RequiresFeature for premium features
3. **Limit Enforcement** - Check limits before creating new resources
4. **Context Cleanup** - Always clear context in finally blocks
5. **Background Tasks** - Explicitly set context for async/scheduled tasks

## Troubleshooting

### "Tenant context not set" Exception

**Cause**: TenantContextFilter didn't run or was skipped for this path.

**Solution**:
1. Check if the path is in the excluded paths list
2. Verify TenantContextFilter is registered
3. Ensure the filter order is correct

### Feature Check Not Working

**Cause**: AOP aspect not being applied.

**Solution**:
1. Ensure Spring AOP is enabled (`@EnableAspectJAutoProxy`)
2. Check that the class is a Spring-managed bean
3. Verify method is public (AOP doesn't work on private methods)

### Tenant Data Leaking Between Requests

**Cause**: TenantContextHolder.clear() not being called.

**Solution**:
1. Verify TenantContextFilter's finally block is executing
2. For background tasks, use `executeWithContext` helper
3. Check for any custom thread pools that might bypass filters
