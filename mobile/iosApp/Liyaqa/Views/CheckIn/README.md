# Check-In Feature Documentation

## Overview
The Check-In feature allows gym members to check in to their upcoming classes using multiple methods:
- **QR Code**: Display a dynamically generated QR code for scanning at the gym entrance
- **Manual Check-In**: One-tap check-in button for immediate confirmation
- **NFC**: Tap-to-check-in using NFC-enabled devices (iOS 13+)

## Architecture

### Files Structure
```
Views/CheckIn/
├── CheckInView.swift           # Main SwiftUI view
├── CheckInViewModel.swift      # Business logic and state management
├── CheckInStatus.swift         # Status model for check-in results
└── README.md                   # This file

Utils/
└── HapticFeedback.swift        # Haptic feedback utility
```

### Key Components

#### 1. CheckInView.swift
- **Purpose**: Main UI for the check-in experience
- **Features**:
  - Displays upcoming class information
  - Shows dynamically generated QR code
  - Manual check-in button
  - NFC check-in option (when available)
  - Real-time status feedback
  - Empty state for no bookings
  - Pull-to-refresh support

#### 2. CheckInViewModel.swift
- **Purpose**: Manages check-in state and business logic
- **Key Responsibilities**:
  - Fetches next eligible booking from `BookingRepository`
  - Generates QR codes with security tokens
  - Refreshes QR codes every 30 seconds
  - Executes check-in via `CheckInUseCase`
  - Manages NFC reading sessions
  - Provides haptic feedback for user actions

#### 3. CheckInStatus.swift
- **Purpose**: Represents check-in result status
- **Properties**:
  - `success: Bool` - Whether check-in succeeded
  - `message: String` - User-facing status message

#### 4. HapticFeedback.swift
- **Purpose**: Provides haptic feedback utilities
- **Methods**:
  - `success()` - Success notification feedback
  - `error()` - Error notification feedback
  - `warning()` - Warning notification feedback
  - Various impact feedback methods

## QR Code Generation

### QR Code Format
The QR code contains the following data:
```
{bookingId}|{timestamp}|{securityToken}
```

**Example:**
```
booking-123|1700000000|abc123xyz456==
```

### Security Features
- **Timestamp**: Prevents replay attacks by including current time
- **Security Token**: Base64-encoded random data (16 bytes)
- **Refresh Interval**: QR codes regenerate every 30 seconds
- **Correction Level**: High ("H") for better reliability

### Implementation Details
```swift
private func createQRData(bookingId: String) -> String {
    let timestamp = Date().timeIntervalSince1970
    let token = generateSecurityToken()
    return "\(bookingId)|\(Int(timestamp))|\(token)"
}
```

## NFC Integration

### Requirements
- iOS 13.0 or later
- Device with NFC capability
- NFC entitlements configured

### Usage Flow
1. User taps "Check In with NFC" button
2. NFC reading session starts
3. User holds device near NFC reader
4. App reads NDEF tag data
5. Validates gym location identifier
6. Executes check-in via `CheckInUseCase`

### Configuration
**Info.plist:**
```xml
<key>NFCReaderUsageDescription</key>
<string>Liyaqa uses NFC to enable quick check-in at the gym entrance.</string>
```

**Entitlements:**
```xml
<key>com.apple.developer.nfc.readersession.formats</key>
<array>
    <string>NDEF</string>
    <string>TAG</string>
</array>
```

## Business Rules

The check-in feature enforces business rules via `CheckInUseCase` and `CanCheckInRule`:

1. **Booking Status**: Only CONFIRMED bookings can be checked in
2. **Time Window**: Check-in allowed 15 minutes before to 5 minutes after class start
3. **Class Status**: Class must not be cancelled
4. **Single Check-In**: Prevents duplicate check-ins

## Integration with Shared Module

### Dependencies
- `CheckInUseCase` - Executes check-in logic
- `BookingRepository` - Fetches member bookings
- `Booking` model - Contains booking data
- `ClassSchedule` model - Contains class information

### Usage Example
```swift
let checkInUseCase = KoinHelper.shared.getCheckInUseCase()
let result = try await checkInUseCase.invoke(bookingId: booking.id)

if let _ = try? result.getOrThrow() {
    // Check-in successful
} else {
    // Handle error
}
```

## User Experience

### Check-In Flow
1. User opens Check-In screen
2. App loads next eligible booking
3. QR code is generated and displayed
4. User can:
   - Show QR code to scanner at gym
   - Tap "Check In Now" for manual check-in
   - Use NFC if device supports it
5. Status feedback is shown
6. Booking status is updated

### Empty State
When no bookings are available:
- Displays friendly empty state message
- Provides "Browse Classes" button
- Suggests booking a class

### Error Handling
- Network errors: Clear error messages
- No bookings: Empty state UI
- Check-in failures: Specific error feedback
- NFC errors: User cancellation gracefully handled

## Testing Checklist

### Functional Tests
- [ ] QR code generates correctly
- [ ] QR code refreshes every 30 seconds
- [ ] Countdown timer displays correctly
- [ ] Manual check-in executes successfully
- [ ] NFC check-in works on compatible devices
- [ ] Haptic feedback triggers appropriately
- [ ] Status messages display correctly
- [ ] Empty state shows when no bookings

### Edge Cases
- [ ] No internet connection
- [ ] Booking outside time window
- [ ] Cancelled class
- [ ] Already checked in
- [ ] NFC unavailable device
- [ ] Camera permissions denied (for future scanning)

### UI/UX Tests
- [ ] Layout adapts to different screen sizes
- [ ] Pull-to-refresh works
- [ ] Loading states display correctly
- [ ] Error states are user-friendly
- [ ] Colors follow theme guidelines
- [ ] Animations are smooth

## Future Enhancements

### Potential Improvements
1. **Location Verification**: Validate user is at gym location using GPS
2. **Offline Support**: Cache bookings for offline QR generation
3. **Apple Wallet**: Add check-in pass to Apple Wallet
4. **Watch App**: Enable check-in from Apple Watch
5. **Bluetooth Beacons**: Auto-detect gym presence for seamless check-in
6. **Analytics**: Track check-in patterns and popular times
7. **Social Features**: Share workout completion with friends
8. **Gamification**: Award badges for check-in streaks

### API Improvements
1. Backend QR validation endpoint
2. Real-time check-in status via WebSocket
3. Push notification on successful check-in
4. Integration with gym door access systems

## Troubleshooting

### Common Issues

**Issue**: QR code not generating
- **Solution**: Check booking repository connection, verify booking exists

**Issue**: NFC not working
- **Solution**: Verify device supports NFC, check entitlements are configured

**Issue**: Check-in fails with time window error
- **Solution**: User arrived too early or too late, show clear message

**Issue**: "No bookings available"
- **Solution**: User hasn't booked any classes, direct to class browsing

## Performance Considerations

- QR code generation is lightweight (~50ms)
- Timer cleanup prevents memory leaks
- NFC session properly invalidated
- Images use efficient CoreImage pipeline
- View model uses `@MainActor` for thread safety

## Accessibility

- All UI elements have proper labels
- Haptic feedback supplements visual feedback
- Error messages are clear and actionable
- High contrast QR code for easy scanning
- Support for Dynamic Type (system font sizes)

## References

- [Apple NFC Documentation](https://developer.apple.com/documentation/corenfc)
- [CoreImage QR Code Generation](https://developer.apple.com/documentation/coreimage)
- [Haptic Feedback Guidelines](https://developer.apple.com/design/human-interface-guidelines/playing-haptics)
