# Database Layer Documentation

This directory contains the local database implementation using SQLDelight for offline data caching.

## Architecture

The database layer follows the **cache-aside pattern**: check cache first, then network if needed.

### Components

1. **SQLDelight Schema Files** (`sqldelight/com/liyaqa/gym/database/*.sq`)
   - `Member.sq` - Member data with full profile information
   - `ClassSchedule.sq` - Class schedule instances with booking counts
   - `Booking.sq` - Member bookings with status tracking
   - `Subscription.sq` - Member subscriptions with status and validity

2. **Database Factory** (`DatabaseDriverFactory.kt`)
   - `expect` declaration in `commonMain`
   - `actual` implementations:
     - `androidMain` - Uses `AndroidSqliteDriver`
     - `iosMain` - Uses `NativeSqliteDriver`

3. **Database Wrapper** (`LiyaqaDatabaseWrapper.kt`)
   - Initializes SQLDelight database
   - Manages cache TTL and stale data cleanup
   - Provides centralized database access
   - Cache TTL constants:
     - `DEFAULT_CACHE_TTL` - 24 hours
     - `SHORT_CACHE_TTL` - 1 hour (for frequently changing data)
     - `LONG_CACHE_TTL` - 7 days (for rarely changing data)

4. **DAO Interfaces** (`dao/*.kt`)
   - `MemberDao` - CRUD operations for members
   - `ScheduleDao` - CRUD operations for class schedules
   - `BookingDao` - CRUD operations for bookings
   - Each provides:
     - Suspend functions for single operations
     - Flow-based reactive queries
     - Batch save operations
     - Search and filter capabilities

5. **DAO Implementations** (`dao/*DaoImpl.kt`)
   - Map between domain models and database entities
   - Handle timestamp conversions
   - Manage cache timestamps automatically
   - Execute queries on appropriate dispatcher

## Usage

### Dependency Injection

DAOs are registered in `SharedModule.kt`:

```kotlin
// Database wrapper
single { LiyaqaDatabaseWrapper(get()) }
single { get<LiyaqaDatabaseWrapper>().database }

// DAOs
single<MemberDao> { MemberDaoImpl(get()) }
single<ScheduleDao> { ScheduleDaoImpl(get()) }
single<BookingDao> { BookingDaoImpl(get()) }
```

### Using DAOs

```kotlin
class MemberRepository(
    private val memberDao: MemberDao,
    private val memberApi: MemberApiService
) {
    suspend fun getMembers(forceRefresh: Boolean = false): List<Member> {
        // Cache-first approach
        if (!forceRefresh) {
            val cached = memberDao.getAll()
            if (cached.isNotEmpty()) {
                return cached
            }
        }

        // Fetch from network
        val members = memberApi.getMembers()

        // Update cache
        memberDao.saveAll(members)

        return members
    }

    fun observeMembers(): Flow<List<Member>> {
        return memberDao.observeAll()
    }
}
```

### Cache Management

```kotlin
// Clear stale data older than 24 hours
databaseWrapper.clearStaleData()

// Clear stale data with custom TTL
databaseWrapper.clearStaleData(maxAge = 12.hours)

// Clear all cached data
databaseWrapper.clearAllCache()
```

### Database Queries

#### Member Queries
```kotlin
// Get all members
val members = memberDao.getAll()

// Get by ID
val member = memberDao.getById("member-123")

// Search members
val results = memberDao.search("john")

// Filter by status
val activeMembers = memberDao.getByStatus("ACTIVE")

// Observe changes
memberDao.observeAll().collect { members ->
    // UI updates automatically
}
```

#### Schedule Queries
```kotlin
// Get schedules by date range
val schedules = scheduleDao.getByDateRange(
    startDateTime = LocalDateTime.parse("2024-01-01T00:00:00"),
    endDateTime = LocalDateTime.parse("2024-01-31T23:59:59")
)

// Get upcoming schedules
val upcoming = scheduleDao.getUpcoming(limit = 10)

// Get available schedules (not full)
val available = scheduleDao.getAvailable(limit = 20)

// Update booking count
scheduleDao.updateBookedCount(
    id = "schedule-123",
    bookedCount = 15,
    waitlistCount = 3
)
```

#### Booking Queries
```kotlin
// Get member's bookings
val bookings = bookingDao.getByMemberId("member-123")

// Get upcoming bookings
val upcoming = bookingDao.getUpcomingByMember("member-123")

// Check in member
bookingDao.checkIn("booking-123")

// Cancel booking
bookingDao.cancel(
    id = "booking-123",
    cancellationReason = "Member requested"
)
```

## Data Flow

```
┌─────────────┐         ┌──────────────┐         ┌──────────┐
│             │ Request │              │ Query   │          │
│ Repository  │────────>│     DAO      │────────>│ Database │
│             │         │              │         │          │
└─────────────┘         └──────────────┘         └──────────┘
      │                        │                       │
      │                        │                       │
      │ If cache miss          │ Save                  │
      │ or force refresh       │                       │
      │                        │                       │
      v                        v                       v
┌─────────────┐         ┌──────────────┐         ┌──────────┐
│             │ Fetch   │              │ Insert/ │          │
│   Network   │────────>│     DAO      │────────>│ Database │
│  API Layer  │         │              │ Update  │          │
└─────────────┘         └──────────────┘         └──────────┘
```

## Database Schema

### MemberEntity
- Full member profile including contact info
- Emergency contact details
- Status tracking (ACTIVE, INACTIVE, SUSPENDED, etc.)
- Indexed by: branchId, status

### ClassScheduleEntity
- Links to GymClass and Instructor
- Capacity and booking count tracking
- Cancellation support
- Indexed by: classId, startDateTime, instructorId

### BookingEntity
- Links Member to ClassSchedule
- Status tracking (CONFIRMED, WAITLISTED, ATTENDED, etc.)
- Waitlist position for waitlisted bookings
- Check-in and cancellation timestamps
- Indexed by: memberId, scheduleId, status

### SubscriptionEntity
- Member subscription details
- Plan information and validity dates
- Auto-renewal flag
- Pause and cancellation support
- Indexed by: memberId, status, dates

## Best Practices

1. **Always use cache-first approach** - Check cache before network
2. **Update cache after network calls** - Keep cache synchronized
3. **Use Flows for reactive UI** - UI updates automatically when data changes
4. **Clear stale data periodically** - Prevent cache from growing too large
5. **Handle null safety** - Database queries may return null
6. **Use transactions for batch operations** - Better performance
7. **Map at DAO level** - Keep domain models clean from database concerns

## Migrations

SQLDelight handles schema changes through migration files. When updating schemas:

1. Update the `.sq` file with new schema
2. Create migration file in `migrations/` directory
3. Test migration with existing data

## Performance Tips

1. Use batch operations (`saveAll`) instead of individual saves
2. Use appropriate cache TTL based on data volatility
3. Clear old data regularly to keep database size manageable
4. Use indexes for frequently queried fields (already defined in schemas)
5. Use `observeX()` methods for reactive UI updates instead of polling

## Testing

Mock DAOs in tests using interfaces:

```kotlin
class FakeMemberDao : MemberDao {
    private val members = mutableListOf<Member>()

    override suspend fun getAll() = members.toList()
    override suspend fun save(member: Member) {
        members.add(member)
    }
    // ... implement other methods
}
```
