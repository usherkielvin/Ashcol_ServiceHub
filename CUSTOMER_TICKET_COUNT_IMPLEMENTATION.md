# Customer Ticket Count Implementation

## Overview
Made the "My Tickets" section in the customer home tab functional by displaying real-time ticket counts, similar to the manager dashboard implementation.

## Changes Made

### 1. Layout Updates (`fragment_user__home.xml`)
- Added IDs to the three ticket count TextViews:
  - `tvNewTicketsCount` - Shows count of tickets in progress/assigned/scheduled
  - `tvPendingTicketsCount` - Shows count of pending/new tickets
  - `tvCompletedTicketsCount` - Shows count of completed tickets
- Changed default text from hardcoded values ("03", "05", "15") to "0"

### 2. UserHomeFragment.java Updates
- Added TextView references for ticket counts
- Imported `TicketListResponse` for API data handling
- Added `loadTicketCounts()` method to fetch tickets from API
- Added `updateTicketCounts()` method to count tickets by status and update UI
- Integrated ticket count loading in `onCreateView()` and `onResume()`

## How It Works

1. **Data Fetching**: Uses the existing `getTickets()` API endpoint to fetch all customer tickets
2. **Status Categorization**:
   - **New/In Progress**: Tickets with status containing "in progress", "in_progress", "scheduled", or "assigned"
   - **Pending**: Tickets with status containing "pending" or equal to "new"
   - **Completed**: Tickets with status containing "completed" or "done"
3. **UI Update**: Counts are displayed in real-time on the three colored cards
4. **Auto-Refresh**: Ticket counts refresh automatically when the fragment resumes (user navigates back to home tab)

## Color Scheme
- **Green Card** (#A5D6A7): New/In Progress tickets
- **Orange Card** (#FFE0B2): Pending tickets
- **Purple Card** (#9FA8DA): Completed tickets

## API Integration
- Endpoint: `GET /api/v1/tickets`
- Response: `TicketListResponse` containing list of tickets with status information
- Authentication: Uses TokenManager to get user auth token

## Benefits
- Real-time ticket count visibility for customers
- Consistent with manager dashboard UX
- Automatic updates when returning to home tab
- No manual refresh needed
