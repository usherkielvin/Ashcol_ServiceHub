# Customer Auto-Refresh Implementation

## Overview
Implemented real-time auto-refresh functionality for customer ticket views so customers see updates immediately when managers or technicians interact with their tickets.

## What Was Implemented

### 1. Android App - Customer Side

#### New Firebase Listener (`CustomerFirebaseListener.java`)
- Created dedicated Firebase listener for customer tickets
- Listens to Firestore for real-time ticket updates
- Triggers immediate refresh when managers/techs update tickets
- Located: `app/src/main/java/app/hub/user/CustomerFirebaseListener.java`

#### Updated Customer Ticket Fragment (`UserTicketsFragment.java`)
- Added 5-second auto-polling mechanism (background refresh)
- Integrated CustomerFirebaseListener for instant Firebase updates
- Maintains existing Firestore manager for payment updates
- Auto-refresh starts when fragment loads
- All listeners properly cleaned up on fragment destroy

**Key Features:**
- 5-second polling interval (less aggressive than employee 3-second)
- Firebase real-time updates for instant notifications
- Silent background refresh (no UI disruption)
- Proper lifecycle management

### 2. Web Portal - Customer Side

#### Ticket Detail View (`tickets/show.blade.php`)
- Added JavaScript auto-refresh every 5 seconds
- Updates ticket status, assigned staff, and comment count in real-time
- Shows subtle notification when updates occur
- Only enabled for customer users (not managers/techs)

#### Ticket List View (`tickets/index.blade.php`)
- Added JavaScript auto-refresh every 10 seconds
- Refreshes entire ticket list to show status changes
- Shows notification when list updates
- Only enabled for customer users

#### Backend Support (`TicketController.php`)
- Updated `show()` method to support JSON responses for AJAX
- Returns ticket data in JSON format when requested via AJAX
- Maintains backward compatibility with regular page views

## How It Works

### Android Flow:
1. Customer opens ticket list (UserTicketsFragment)
2. Auto-refresh starts:
   - Firebase listener connects to Firestore
   - 5-second polling timer starts
3. When manager/tech updates ticket:
   - Firebase instantly notifies customer app
   - Polling also catches updates every 5 seconds
4. Customer sees updates immediately without manual refresh

### Web Flow:
1. Customer views ticket detail or list page
2. JavaScript auto-refresh starts (5s for detail, 10s for list)
3. When manager/tech updates ticket:
   - AJAX request fetches latest data
   - Page updates dynamically without full reload
   - Subtle notification shows update occurred
4. Customer sees changes in real-time

## Benefits

1. **Instant Updates**: Customers see status changes immediately
2. **No Manual Refresh**: Eliminates need to pull-to-refresh or reload page
3. **Better UX**: Customers stay informed about their ticket progress
4. **Dual Mechanism**: Firebase + polling ensures updates aren't missed
5. **Efficient**: Silent background updates don't disrupt user experience

## Technical Details

### Android Polling Intervals:
- Employee/Tech: 3 seconds (fast for active work)
- Customer: 5 seconds (balanced for monitoring)

### Web Polling Intervals:
- Ticket Detail: 5 seconds (active viewing)
- Ticket List: 10 seconds (overview monitoring)

### Firebase Integration:
- Customers listen to: `tickets` collection where `customerId` matches
- Employees listen to: `tickets` collection where `assignedTo` matches
- Real-time sync via Firestore ensures instant updates

## Files Modified

### Android:
1. `app/src/main/java/app/hub/user/CustomerFirebaseListener.java` (NEW)
2. `app/src/main/java/app/hub/user/UserTicketsFragment.java` (UPDATED)

### Web:
1. `resources/views/tickets/show.blade.php` (UPDATED)
2. `resources/views/tickets/index.blade.php` (UPDATED)
3. `app/Http/Controllers/TicketController.php` (UPDATED)

## Testing Recommendations

1. **Android**: Open customer ticket list, have manager update ticket status, verify instant update
2. **Web**: Open customer ticket detail, have tech assign themselves, verify status updates
3. **Performance**: Monitor battery/network usage with auto-refresh enabled
4. **Edge Cases**: Test with poor network, airplane mode, background/foreground transitions

## Future Enhancements

- Add WebSocket support for even faster web updates
- Implement push notifications for critical status changes
- Add user preference to enable/disable auto-refresh
- Show "live" indicator when auto-refresh is active
