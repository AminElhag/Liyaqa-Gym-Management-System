# Security Configuration Documentation

This document describes the security implementation for the Liyaqa Gym Management System.

## Overview

The security layer implements:
- JWT-based authentication
- Stateless session management
- Multi-tenant security (organization and branch isolation)
- Role-based access control (RBAC)
- Rate limiting on authentication endpoints
- CORS configuration for web and mobile apps
- BCrypt password encoding

## Components

### 1. SecurityConfig.kt
Main Spring Security configuration that:
- Disables CSRF (stateless API)
- Configures CORS
- Sets session management to STATELESS
- Defines public and protected endpoints
- Configures authentication provider with BCrypt
- Enables method security (`@PreAuthorize`)

### 2. JwtTokenProvider.kt
Handles JWT token operations:
- `generateToken(userDetails)`: Creates 24-hour access token
- `generateRefreshToken(userDetails)`: Creates 30-day refresh token
- `validateToken(token)`: Validates access token
- `validateRefreshToken(token)`: Validates refresh token
- `getUserIdFromToken(token)`: Extracts user ID
- `getOrganizationIdFromToken(token)`: Extracts organization ID
- `getBranchIdFromToken(token)`: Extracts branch ID (if present)

Token Claims:
```json
{
  "sub": "userId",
  "userId": "uuid",
  "email": "user@example.com",
  "role": "MEMBER|TRAINER|STAFF|ADMIN",
  "organizationId": "uuid",
  "branchId": "uuid",
  "memberId": "uuid",
  "staffId": "uuid",
  "tokenType": "ACCESS|REFRESH",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### 3. JwtAuthenticationFilter.kt
Filter that:
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates the token
- Sets authentication in SecurityContext
- Handles expired tokens (returns 401)
- Skips validation for public endpoints

### 4. GymUserDetails.kt
Custom UserDetails implementation that includes:
- User ID, email, role
- Organization ID and Branch ID (multi-tenant context)
- Member ID or Staff ID (depending on role)
- Authorities (role + permissions)
- Helper methods for permission checks

### 5. User Roles and Permissions

#### MEMBER
- `member:read:own` - Read own profile
- `member:update:own` - Update own profile
- `booking:create:own` - Create own bookings
- `booking:read:own` - View own bookings
- `booking:cancel:own` - Cancel own bookings
- `payment:read:own` - View own payments
- `payment:create:own` - Make payments

#### TRAINER
- `member:read:all` - View all members
- `class:read:all` - View all classes
- `class:create` - Create classes
- `class:update:own` - Update own classes
- `class:cancel:own` - Cancel own classes
- `booking:read:all` - View all bookings
- `attendance:mark` - Mark attendance

#### STAFF
- All TRAINER permissions, plus:
- `member:create` - Create members
- `member:update:all` - Update any member
- `member:suspend` - Suspend members
- `booking:create:all` - Create bookings for anyone
- `booking:cancel:all` - Cancel any booking
- `payment:read:all` - View all payments
- `payment:create` - Process payments
- `access:manage` - Manage facility access

#### ADMIN
- All permissions (`*` wildcard)
- Can access all branches in organization
- Organization-level settings

### 6. Multi-Tenant Security

#### TenantContext.kt
Thread-local storage for current tenant:
```kotlin
TenantContext.setOrganizationId(uuid)
TenantContext.setBranchId(uuid)
TenantContext.getOrganizationId()
TenantContext.getBranchId()
TenantContext.clear()
```

#### TenantFilter.kt
Automatically extracts tenant context from authenticated user and sets it in TenantContext. Always clears context after request.

**Usage in Repositories:**
```kotlin
// Filter queries by current tenant
val organizationId = TenantContext.getOrganizationId()
val members = memberRepository.findAllByOrganizationId(organizationId)
```

### 7. Rate Limiting

#### RateLimitInterceptor.kt
In-memory rate limiting:
- 5 requests per minute per IP/endpoint
- Applied to: `/login`, `/register`, `/forgot-password`, `/reset-password`
- Returns 429 (Too Many Requests) when exceeded

**Production Note:** Use Redis or similar for distributed rate limiting.

### 8. AuthController.kt

#### Endpoints

**POST /api/v1/auth/login**
```json
Request:
{
  "email": "user@example.com",
  "password": "password123",
  "organizationId": "uuid" // optional
}

Response:
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "role": "MEMBER",
    "organizationId": "uuid",
    "branchId": "uuid",
    "memberId": "uuid",
    "staffId": null,
    "isEmailVerified": false
  }
}
```

**POST /api/v1/auth/register**
```json
Request:
{
  "name": "John Doe",
  "nameArabic": "جون دو",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+966501234567",
  "nationalId": "1234567890",
  "gender": "MALE",
  "dateOfBirth": "1990-01-15",
  "branchId": "uuid"
}

Response:
{
  "message": "Registration successful. Please verify your email.",
  "userId": "uuid",
  "memberId": "uuid",
  "requiresEmailVerification": true
}
```

**POST /api/v1/auth/refresh**
```json
Request:
{
  "refreshToken": "eyJ..."
}

Response:
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**POST /api/v1/auth/logout**
```json
Response:
{
  "message": "Logged out successfully",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**POST /api/v1/auth/forgot-password**
```json
Request:
{
  "email": "user@example.com",
  "organizationId": "uuid" // optional
}

Response:
{
  "message": "If the email exists, a password reset link has been sent",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**POST /api/v1/auth/reset-password** *(Not yet implemented)*
```json
Request:
{
  "token": "reset-token",
  "newPassword": "newpassword123"
}
```

## Configuration

### Required Properties

```properties
# JWT Secret (MUST be at least 256 bits for HS512)
app.jwt.secret=YourVerySecretJWTKeyThatShouldBeAtLeast256BitsLongForHS512Algorithm

# Token Expiration
app.jwt.access-token-expiration-ms=86400000  # 24 hours
app.jwt.refresh-token-expiration-ms=2592000000  # 30 days

# CORS
app.cors.allowed-origins=http://localhost:3000,https://yourdomain.com
app.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
```

### Environment Variables (Production)

```bash
export JWT_SECRET="your-production-secret-key"
export CORS_ORIGINS="https://app.yourdomain.com,https://mobile.yourdomain.com"
```

## Usage Examples

### Using @PreAuthorize

```kotlin
@RestController
@RequestMapping("/api/v1/members")
class MemberController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('member:read:all') or (hasAuthority('member:read:own') and #id == principal.memberId)")
    fun getMember(@PathVariable id: UUID): MemberDTO {
        // ...
    }

    @PostMapping
    @PreAuthorize("hasAuthority('member:create')")
    fun createMember(@RequestBody request: CreateMemberRequest): MemberDTO {
        // ...
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteMember(@PathVariable id: UUID) {
        // ...
    }
}
```

### Getting Current User

```kotlin
@GetMapping("/me")
fun getCurrentUser(@AuthenticationPrincipal userDetails: GymUserDetails): UserInfo {
    return UserInfo(
        id = userDetails.getUserId(),
        email = userDetails.username,
        role = userDetails.getRole().name,
        organizationId = userDetails.getOrganizationId(),
        branchId = userDetails.getBranchId()
    )
}
```

### Tenant-Aware Queries

```kotlin
@Service
class MemberService(private val memberRepository: MemberRepository) {

    fun getAllMembers(): List<Member> {
        val organizationId = TenantContext.getOrganizationId()
            ?: throw IllegalStateException("Organization context not set")

        return memberRepository.findAllByOrganizationId(organizationId)
    }

    fun getMembersByBranch(): List<Member> {
        val branchId = TenantContext.getBranchId()
            ?: throw IllegalStateException("Branch context not set")

        return memberRepository.findAllByBranchId(branchId)
    }
}
```

## Security Best Practices

1. **JWT Secret**
   - Use at least 256 bits (32 characters) for HS512
   - Rotate secrets periodically
   - Never commit secrets to version control
   - Use environment variables in production

2. **CORS**
   - Specify exact origins (avoid wildcards in production)
   - Only allow necessary methods
   - Set `allowCredentials=true` only if needed

3. **Rate Limiting**
   - Use Redis for distributed systems
   - Adjust limits based on traffic patterns
   - Consider different limits per endpoint

4. **Password Security**
   - BCrypt strength: 12 (configurable)
   - Enforce password complexity in client
   - Implement password reset with time-limited tokens

5. **Token Security**
   - Store tokens securely (HttpOnly cookies or secure storage)
   - Implement token blacklisting for logout
   - Short-lived access tokens (24 hours)
   - Longer refresh tokens (30 days)

6. **Multi-Tenant Security**
   - Always filter by organizationId
   - Validate branch access for branch-specific operations
   - Prevent cross-tenant data leakage

## TODO / Future Improvements

- [ ] Implement email verification
- [ ] Implement password reset with tokens
- [ ] Add token blacklist (for logout)
- [ ] Implement refresh token rotation
- [ ] Add Redis-based rate limiting
- [ ] Add audit logging for security events
- [ ] Implement 2FA/MFA support
- [ ] Add IP whitelist/blacklist
- [ ] Implement session management
- [ ] Add security headers (HSTS, CSP, etc.)
- [ ] Implement password complexity validation
- [ ] Add account lockout after failed attempts
- [ ] Implement OAuth2/OIDC providers (Google, Apple, etc.)

## Testing

### Manual Testing with curl

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "phone": "+966501234567",
    "gender": "MALE",
    "branchId": "your-branch-uuid"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

# Use access token
curl -X GET http://localhost:8080/api/v1/members/me \
  -H "Authorization: Bearer <access-token>"

# Refresh token
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<refresh-token>"
  }'
```

## Support

For issues or questions, please refer to the main project documentation or create an issue on GitHub.
