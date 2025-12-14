# 🎫 Ticket System Roadmap & Flow - Android App

## 📋 Overview

This document outlines the complete roadmap and UI/UX flow for implementing the ticketing system in the Android app, supporting both **Customer** and **Admin** roles.

---

## 🗄️ Data Structure

### Ticket Model
```java
Ticket {
    id: Integer
    title: String
    description: String
    customer_id: Integer
    assigned_staff_id: Integer (nullable)
    status_id: Integer
    priority: String (low, medium, high, urgent)
    created_at: DateTime
    updated_at: DateTime
    
    // Relationships
    customer: User
    assignedStaff: User (nullable)
    status: TicketStatus
    comments: List<TicketComment>
}
```

### Ticket Status Model
```java
TicketStatus {
    id: Integer
    name: String (e.g., "Open", "In Progress", "Resolved", "Closed")
    color: String (for UI display)
    is_default: Boolean
}
```

### Ticket Comment Model
```java
TicketComment {
    id: Integer
    ticket_id: Integer
    user_id: Integer
    comment: String
    created_at: DateTime
    updated_at: DateTime
    
    // Relationships
    user: User
}
```

### Priority Values
- `low` - Low priority
- `medium` - Medium priority (default)
- `high` - High priority
- `urgent` - Urgent priority

---

## 🔄 User Flows

### 👤 CUSTOMER FLOW

#### Flow 1: View My Tickets
```
DashboardActivity
    ↓ (Tap "My Tickets" button)
TicketsListActivity (Customer View)
    ├─ Shows only customer's own tickets
    ├─ Filter by status
    ├─ Sort by date/priority
    └─ Tap ticket → TicketDetailActivity
```

#### Flow 2: Create New Ticket
```
Option A: DashboardActivity
    ↓ (Tap "Create Ticket" button)
CreateTicketActivity
    ↓ (Fill form & Submit)
    ↓ (API: POST /api/v1/tickets)
    ↓ (Success)
TicketDetailActivity (new ticket)

Option B: TicketsListActivity
    ↓ (Tap FAB/"+ Create Ticket")
CreateTicketActivity
    ↓ (Fill form & Submit)
    ↓ (API: POST /api/v1/tickets)
    ↓ (Success)
TicketDetailActivity (new ticket)
```

#### Flow 3: View Ticket Details (Customer)
```
TicketsListActivity
    ↓ (Tap ticket item)
TicketDetailActivity (Customer View)
    ├─ Display ticket info (read-only for customers)
    ├─ Show comments list
    ├─ Add comment button
    └─ Back to TicketsListActivity
```

#### Flow 4: Add Comment to Ticket
```
TicketDetailActivity
    ↓ (Tap "Add Comment" button)
AddCommentDialog/Activity
    ↓ (Enter comment & Submit)
    ↓ (API: POST /api/v1/tickets/{id}/comments)
    ↓ (Success)
TicketDetailActivity (refresh comments)
```

---

### 👨‍💼 ADMIN FLOW

#### Flow 1: View All Tickets
```
DashboardActivity (Admin)
    ↓ (Tap "All Tickets" button)
TicketsListActivity (Admin View)
    ├─ Shows ALL tickets in system
    ├─ Filter by status/priority
    ├─ Search by title/description
    ├─ Sort by date/priority/status
    └─ Tap ticket → TicketDetailActivity (Admin View)
```

#### Flow 2: Create Ticket (Admin)
```
TicketsListActivity
    ↓ (Tap FAB/"+ Create Ticket")
CreateTicketActivity (Admin View)
    ├─ Can assign to customer (dropdown)
    ├─ Can assign to staff (dropdown)
    ├─ Can set status
    └─ Can set priority
    ↓ (Submit)
    ↓ (API: POST /api/v1/tickets)
    ↓ (Success)
TicketDetailActivity (new ticket)
```

#### Flow 3: View & Edit Ticket Details (Admin)
```
TicketsListActivity
    ↓ (Tap ticket item)
TicketDetailActivity (Admin View)
    ├─ Display ticket info (editable)
    ├─ Edit button → EditTicketActivity
    ├─ Assign/Reassign staff
    ├─ Change status
    ├─ Change priority
    ├─ Show comments list
    ├─ Add comment button
    └─ Delete ticket button (optional)
```

#### Flow 4: Edit Ticket (Admin)
```
TicketDetailActivity
    ↓ (Tap "Edit" button)
EditTicketActivity
    ├─ Edit title (TextInputEditText)
    ├─ Edit description (TextInputEditText)
    ├─ Change status (Spinner/Dropdown)
    ├─ Change priority (Spinner/Dropdown)
    ├─ Assign/Reassign staff (Spinner/Dropdown)
    └─ Save button
    ↓ (API: PUT /api/v1/tickets/{id})
    ↓ (Success)
TicketDetailActivity (refreshed)
```

#### Flow 5: Filter & Search Tickets (Admin)
```
TicketsListActivity
    ├─ Filter by Status (Chip group or Dropdown)
    ├─ Filter by Priority (Chip group or Dropdown)
    ├─ Search by title/description (SearchView)
    └─ Sort options (Menu)
```

---

## 📱 UI Screens & Components

### 1. TicketsListActivity (Customer & Admin)

#### Customer View:
- **Header**: "My Tickets"
- **FAB**: "+" button (Create Ticket)
- **RecyclerView**: List of tickets
- **Empty State**: "No tickets yet. Create your first ticket!"
- **Pull-to-refresh**: Swipe down to refresh

#### Admin View:
- **Header**: "All Tickets"
- **Filter Bar**: Status chips, Priority chips
- **SearchView**: Search by title/description
- **FAB**: "+" button (Create Ticket)
- **RecyclerView**: List of all tickets
- **Empty State**: "No tickets found"
- **Pull-to-refresh**: Swipe down to refresh
- **Sort Menu**: Sort by date, priority, status

#### Ticket Item Layout (RecyclerView Item):
```
┌─────────────────────────────────────┐
│ [Priority Badge]  #123              │
│                                     │
│ Title: Aircon not working           │
│                                     │
│ Status: In Progress  [Color Badge]  │
│ Priority: High       [Color Badge]  │
│                                     │
│ Customer: John Doe                  │
│ Staff: Jane Smith (if assigned)     │
│                                     │
│ Created: 2 days ago                 │
│ Comments: 5                         │
└─────────────────────────────────────┘
```

---

### 2. CreateTicketActivity

#### Customer View (Simplified):
```
┌─────────────────────────────────────┐
│ Create Service Request              │
├─────────────────────────────────────┤
│                                     │
│ Title *                            │
│ [___________________________]      │
│                                     │
│ Description *                      │
│ [___________________________]      │
│ [___________________________]      │
│ [___________________________]      │
│                                     │
│ Priority                           │
│ (○) Low  (●) Medium  (○) High      │
│                                     │
│ [Cancel]        [Submit]           │
└─────────────────────────────────────┘
```

#### Admin View (Full):
```
┌─────────────────────────────────────┐
│ Create Ticket                       │
├─────────────────────────────────────┤
│                                     │
│ Title *                            │
│ [___________________________]      │
│                                     │
│ Description *                      │
│ [___________________________]      │
│ [___________________________]      │
│                                     │
│ Customer *                         │
│ [Select Customer ▼]                │
│                                     │
│ Assign to Staff                    │
│ [Select Staff ▼]                   │
│                                     │
│ Status                             │
│ [Select Status ▼]                  │
│                                     │
│ Priority                           │
│ [Select Priority ▼]                │
│                                     │
│ [Cancel]        [Create Ticket]    │
└─────────────────────────────────────┘
```

---

### 3. TicketDetailActivity

#### Customer View (Read-only):
```
┌─────────────────────────────────────┐
│ #123 - Aircon not working    [Back] │
├─────────────────────────────────────┤
│                                     │
│ Status: In Progress  [Badge]       │
│ Priority: High       [Badge]       │
│                                     │
│ Description:                        │
│ The air conditioning unit in the    │
│ living room stopped working...      │
│                                     │
│ Created: Jan 15, 2025 10:30 AM     │
│ Updated: Jan 16, 2025 02:15 PM     │
│                                     │
│ Assigned to: Jane Smith            │
│                                     │
│ ──────────────────────────────────  │
│                                     │
│ Comments (5)                        │
│ ──────────────────────────────────  │
│                                     │
│ [RecyclerView: Comments List]      │
│                                     │
│ [Add Comment Button]               │
└─────────────────────────────────────┘
```

#### Admin View (Editable):
```
┌─────────────────────────────────────┐
│ #123 - Aircon not working    [Back] │
├─────────────────────────────────────┤
│                                     │
│ [Edit Button]                       │
│                                     │
│ Status: In Progress  [Badge]       │
│ Priority: High       [Badge]       │
│                                     │
│ Description:                        │
│ The air conditioning unit in the    │
│ living room stopped working...      │
│                                     │
│ Customer: John Doe                  │
│ Assigned to: [Jane Smith ▼]        │
│                                     │
│ Created: Jan 15, 2025 10:30 AM     │
│ Updated: Jan 16, 2025 02:15 PM     │
│                                     │
│ ──────────────────────────────────  │
│                                     │
│ Comments (5)                        │
│ ──────────────────────────────────  │
│                                     │
│ [RecyclerView: Comments List]      │
│                                     │
│ [Add Comment Button]               │
└─────────────────────────────────────┘
```

---

### 4. EditTicketActivity (Admin Only)

```
┌─────────────────────────────────────┐
│ Edit Ticket #123             [Back] │
├─────────────────────────────────────┤
│                                     │
│ Title *                            │
│ [Aircon not working_______]        │
│                                     │
│ Description *                      │
│ [The air conditioning unit...]     │
│ [___________________________]      │
│                                     │
│ Status *                           │
│ [In Progress ▼]                    │
│                                     │
│ Priority *                         │
│ [High ▼]                           │
│                                     │
│ Assign to Staff                    │
│ [Jane Smith ▼]                     │
│                                     │
│ [Cancel]        [Save Changes]     │
└─────────────────────────────────────┘
```

---

### 5. AddCommentDialog/Activity

```
┌─────────────────────────────────────┐
│ Add Comment                         │
├─────────────────────────────────────┤
│                                     │
│ [___________________________]      │
│ [___________________________]      │
│ [___________________________]      │
│                                     │
│ [Cancel]        [Post Comment]     │
└─────────────────────────────────────┘
```

#### Comment Item Layout (in RecyclerView):
```
┌─────────────────────────────────────┐
│ John Doe                            │
│ 2 hours ago                         │
├─────────────────────────────────────┤
│ Thanks for reporting. I'll send     │
│ a technician tomorrow.              │
└─────────────────────────────────────┘
```

---

## 🌐 API Endpoints Required

### 1. List Tickets
```
GET /api/v1/tickets
Query Parameters:
  - status_id (optional): Filter by status
  - priority (optional): Filter by priority
  - search (optional): Search in title/description
  - page (optional): Pagination

Response:
{
  "success": true,
  "data": {
    "tickets": [
      {
        "id": 1,
        "title": "Aircon not working",
        "description": "...",
        "priority": "high",
        "customer": {
          "id": 1,
          "name": "John Doe",
          "email": "john@example.com"
        },
        "assigned_staff": {
          "id": 2,
          "name": "Jane Smith",
          "email": "jane@example.com"
        },
        "status": {
          "id": 2,
          "name": "In Progress",
          "color": "blue"
        },
        "comments_count": 5,
        "created_at": "2025-01-15T10:30:00Z",
        "updated_at": "2025-01-16T14:15:00Z"
      }
    ],
    "meta": {
      "current_page": 1,
      "total": 25,
      "per_page": 15
    }
  }
}
```

### 2. Get Single Ticket
```
GET /api/v1/tickets/{id}

Response:
{
  "success": true,
  "data": {
    "id": 1,
    "title": "Aircon not working",
    "description": "...",
    "priority": "high",
    "customer": {...},
    "assigned_staff": {...},
    "status": {...},
    "comments": [
      {
        "id": 1,
        "comment": "Thanks for reporting...",
        "user": {
          "id": 2,
          "name": "Jane Smith",
          "email": "jane@example.com"
        },
        "created_at": "2025-01-16T12:00:00Z"
      }
    ],
    "created_at": "2025-01-15T10:30:00Z",
    "updated_at": "2025-01-16T14:15:00Z"
  }
}
```

### 3. Create Ticket
```
POST /api/v1/tickets
Body (Customer):
{
  "title": "Aircon not working",
  "description": "The air conditioning unit...",
  "priority": "high"
}

Body (Admin):
{
  "title": "Aircon not working",
  "description": "The air conditioning unit...",
  "customer_id": 1,
  "assigned_staff_id": 2,
  "status_id": 2,
  "priority": "high"
}

Response:
{
  "success": true,
  "message": "Ticket created successfully",
  "data": {
    "ticket": {...}  // Full ticket object
  }
}
```

### 4. Update Ticket
```
PUT /api/v1/tickets/{id}
Body:
{
  "title": "Updated title",
  "description": "Updated description",
  "status_id": 3,
  "priority": "medium",
  "assigned_staff_id": 2
}

Response:
{
  "success": true,
  "message": "Ticket updated successfully",
  "data": {
    "ticket": {...}  // Updated ticket object
  }
}
```

### 5. Add Comment
```
POST /api/v1/tickets/{id}/comments
Body:
{
  "comment": "Thanks for reporting. I'll send a technician."
}

Response:
{
  "success": true,
  "message": "Comment added successfully",
  "data": {
    "comment": {
      "id": 1,
      "comment": "Thanks for reporting...",
      "user": {...},
      "created_at": "2025-01-16T12:00:00Z"
    }
  }
}
```

### 6. Get Ticket Statuses (for dropdowns)
```
GET /api/v1/ticket-statuses

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Open",
      "color": "gray",
      "is_default": true
    },
    {
      "id": 2,
      "name": "In Progress",
      "color": "blue",
      "is_default": false
    }
  ]
}
```

### 7. Get Staff List (Admin - for assignment)
```
GET /api/v1/staff

Response:
{
  "success": true,
  "data": [
    {
      "id": 2,
      "name": "Jane Smith",
      "email": "jane@example.com",
      "role": "staff"
    }
  ]
}
```

---

## 📦 Android Components to Create

### Models (API Response Classes)
1. `Ticket.java` - Ticket model
2. `TicketStatus.java` - Status model
3. `TicketComment.java` - Comment model
4. `TicketsResponse.java` - List response
5. `TicketResponse.java` - Single ticket response
6. `CreateTicketRequest.java` - Create request
7. `UpdateTicketRequest.java` - Update request
8. `AddCommentRequest.java` - Add comment request
9. `TicketStatusResponse.java` - Statuses list
10. `StaffListResponse.java` - Staff list

### Activities & Fragments
1. `TicketsListActivity.java` - List screen
2. `CreateTicketActivity.java` - Create form
3. `TicketDetailActivity.java` - Detail view
4. `EditTicketActivity.java` - Edit form (Admin)
5. `AddCommentDialog.java` - Comment dialog

### Adapters
1. `TicketsAdapter.java` - RecyclerView adapter for tickets list
2. `CommentsAdapter.java` - RecyclerView adapter for comments

### Layout Files
1. `activity_tickets_list.xml`
2. `item_ticket.xml` - Ticket item layout
3. `activity_create_ticket.xml`
4. `activity_ticket_detail.xml`
5. `activity_edit_ticket.xml`
6. `item_comment.xml` - Comment item layout
7. `dialog_add_comment.xml`

### API Service Methods
Update `ApiService.java`:
- `getTickets(statusId, priority, search)` - List tickets
- `getTicket(id)` - Get single ticket
- `createTicket(CreateTicketRequest)` - Create ticket
- `updateTicket(id, UpdateTicketRequest)` - Update ticket
- `addComment(id, AddCommentRequest)` - Add comment
- `getTicketStatuses()` - Get statuses
- `getStaff()` - Get staff list (Admin)

---

## 🗺️ Implementation Roadmap

### Phase 1: Backend API Endpoints (Laravel) ⚠️
**Status:** Need to create API endpoints

**Tasks:**
1. Create `Api/TicketController.php`
2. Create `Api/TicketCommentController.php`
3. Add routes to `routes/api.php`
4. Add API resources/transformers
5. Test endpoints with Postman/Insomnia

**Estimated Time:** 1-2 days

---

### Phase 2: Android Models & API Service (2-3 days)

#### Step 2.1: Create Models (Day 1)
- [ ] Create `Ticket.java` model
- [ ] Create `TicketStatus.java` model
- [ ] Create `TicketComment.java` model
- [ ] Create `TicketsResponse.java`
- [ ] Create `TicketResponse.java`
- [ ] Create request classes

#### Step 2.2: Update API Service (Day 1-2)
- [ ] Add ticket endpoints to `ApiService.java`
- [ ] Add comment endpoints
- [ ] Add status endpoints
- [ ] Add staff list endpoint

#### Step 2.3: Test API Integration (Day 2-3)
- [ ] Test list tickets API
- [ ] Test create ticket API
- [ ] Test get ticket API
- [ ] Test update ticket API
- [ ] Test add comment API

---

### Phase 3: Tickets List Screen (2-3 days)

#### Step 3.1: Layout & UI (Day 1)
- [ ] Create `activity_tickets_list.xml`
- [ ] Create `item_ticket.xml`
- [ ] Design filter/search UI
- [ ] Add FAB button

#### Step 3.2: TicketsListActivity (Day 1-2)
- [ ] Implement RecyclerView
- [ ] Create `TicketsAdapter.java`
- [ ] Implement pull-to-refresh
- [ ] Implement filter functionality
- [ ] Implement search functionality
- [ ] Add empty state handling

#### Step 3.3: Role-based View (Day 2-3)
- [ ] Customer view (own tickets only)
- [ ] Admin view (all tickets + filters)
- [ ] Handle different data based on role
- [ ] Navigation to detail screen

---

### Phase 4: Create Ticket Screen (2 days)

#### Step 4.1: Layout (Day 1)
- [ ] Create `activity_create_ticket.xml`
- [ ] Customer form (title, description, priority)
- [ ] Admin form (all fields + dropdowns)

#### Step 4.2: CreateTicketActivity (Day 1-2)
- [ ] Form validation
- [ ] API integration (create ticket)
- [ ] Loading states
- [ ] Error handling
- [ ] Success navigation to detail screen

---

### Phase 5: Ticket Detail Screen (2-3 days)

#### Step 5.1: Layout (Day 1)
- [ ] Create `activity_ticket_detail.xml`
- [ ] Display ticket information
- [ ] Comments RecyclerView
- [ ] Add comment button

#### Step 5.2: TicketDetailActivity (Day 1-2)
- [ ] Fetch ticket details
- [ ] Display ticket info
- [ ] Load comments
- [ ] Create `CommentsAdapter.java`
- [ ] Handle customer vs admin view

#### Step 5.3: Admin Features (Day 2-3)
- [ ] Edit button
- [ ] Quick assign staff
- [ ] Quick change status
- [ ] Quick change priority

---

### Phase 6: Edit Ticket Screen (Admin) (1-2 days)

#### Step 6.1: Layout
- [ ] Create `activity_edit_ticket.xml`
- [ ] All editable fields
- [ ] Dropdowns for status/priority/staff

#### Step 6.2: EditTicketActivity
- [ ] Pre-fill form with existing data
- [ ] Form validation
- [ ] API integration (update ticket)
- [ ] Success navigation back to detail

---

### Phase 7: Comments System (1-2 days)

#### Step 7.1: Add Comment Dialog
- [ ] Create `dialog_add_comment.xml`
- [ ] Implement `AddCommentDialog.java`
- [ ] API integration
- [ ] Refresh comments after adding

#### Step 7.2: Comments Display
- [ ] Create `item_comment.xml`
- [ ] Complete `CommentsAdapter.java`
- [ ] Display user info, timestamp
- [ ] Handle long comments

---

### Phase 8: UI/UX Polish (2-3 days)

#### Enhancements:
- [ ] Priority badges (color-coded)
- [ ] Status badges (color-coded)
- [ ] Loading indicators (Shimmer effect)
- [ ] Empty states (illustrations)
- [ ] Error states with retry
- [ ] Pull-to-refresh animations
- [ ] Smooth transitions
- [ ] Material Design 3 components
- [ ] Dark mode support

---

### Phase 9: Testing & Bug Fixes (2-3 days)

#### Testing:
- [ ] Test customer flow (create, view, comment)
- [ ] Test admin flow (create, edit, assign, filter)
- [ ] Test edge cases (empty lists, errors, network issues)
- [ ] Test role-based permissions
- [ ] Performance testing (large lists)
- [ ] Bug fixes

---

## 📊 Priority Matrix

### Must Have (MVP):
1. ✅ List tickets (Customer: own tickets, Admin: all tickets)
2. ✅ Create ticket
3. ✅ View ticket details
4. ✅ Add comments
5. ✅ Basic filtering (status, priority)

### Should Have:
1. Edit ticket (Admin)
2. Assign staff (Admin)
3. Search functionality
4. Sort functionality
5. Pull-to-refresh

### Nice to Have:
1. Delete ticket
2. Delete comments
3. Real-time updates (WebSocket)
4. Ticket attachments
5. Advanced filters

---

## 🎨 UI Design Guidelines

### Color Scheme for Priorities:
- **Low**: Gray (#9E9E9E)
- **Medium**: Blue (#2196F3)
- **High**: Orange (#FF9800)
- **Urgent**: Red (#F44336)

### Status Badge Colors:
- Use colors from `TicketStatus.color` field
- Default: Gray for "Open", Blue for "In Progress", Green for "Resolved"

### Material Design Components:
- Use Material 3 components
- Cards for ticket items
- Chip groups for filters
- FAB for create actions
- Bottom sheets for filters
- Snackbars for success/error messages

---

## 🔐 Role-Based Permissions

### Customer Permissions:
- ✅ Create tickets (only for themselves)
- ✅ View own tickets
- ✅ View ticket details
- ✅ Add comments to own tickets
- ❌ Edit tickets
- ❌ Assign tickets
- ❌ View all tickets
- ❌ Delete tickets

### Admin Permissions:
- ✅ Create tickets (for any customer)
- ✅ View all tickets
- ✅ View ticket details
- ✅ Edit tickets
- ✅ Assign/reassign staff
- ✅ Change status
- ✅ Change priority
- ✅ Add comments
- ✅ Delete tickets (optional)

---

## 📝 Next Steps

1. **Start with Backend API** - Create all ticket API endpoints in Laravel
2. **Create Models** - Build Android models matching API responses
3. **Build List Screen** - Start with tickets list (most important)
4. **Build Create Screen** - Enable ticket creation
5. **Build Detail Screen** - Show ticket details and comments
6. **Add Admin Features** - Edit, assign, filter

---

## 📚 Additional Notes

- Use Retrofit for API calls
- Use Gson for JSON parsing
- Implement proper error handling
- Add loading states for all API calls
- Cache tickets locally (SharedPreferences or Room) if needed
- Implement pagination for large ticket lists
- Use RecyclerView with DiffUtil for efficient list updates

---

**Last Updated:** January 2025
**Status:** Planning Phase

