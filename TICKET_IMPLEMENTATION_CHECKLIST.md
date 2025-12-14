# ✅ Ticket System Implementation Checklist

## 🎯 Quick Overview

**Goal**: Implement a complete ticketing system for Android app supporting Customer and Admin roles.

**Current Status**: Planning phase - Backend exists, Android app needs implementation.

---

## 📋 Implementation Checklist

### Phase 1: Backend API Setup (Laravel) ⚠️ REQUIRED FIRST

- [ ] **Create API Ticket Controller**
  - [ ] `app/Http/Controllers/Api/TicketController.php`
  - [ ] Methods: `index()`, `show()`, `store()`, `update()`, `destroy()`

- [ ] **Create API Ticket Comment Controller**
  - [ ] `app/Http/Controllers/Api/TicketCommentController.php`
  - [ ] Methods: `store()`, `index()` (list comments for a ticket)

- [ ] **Add API Routes**
  - [ ] `GET /api/v1/tickets` - List tickets
  - [ ] `GET /api/v1/tickets/{id}` - Get single ticket
  - [ ] `POST /api/v1/tickets` - Create ticket
  - [ ] `PUT /api/v1/tickets/{id}` - Update ticket
  - [ ] `DELETE /api/v1/tickets/{id}` - Delete ticket (optional)
  - [ ] `GET /api/v1/tickets/{id}/comments` - Get comments
  - [ ] `POST /api/v1/tickets/{id}/comments` - Add comment
  - [ ] `GET /api/v1/ticket-statuses` - Get all statuses
  - [ ] `GET /api/v1/staff` - Get staff list (Admin only)

- [ ] **Test API Endpoints** (Use Postman/Insomnia)
  - [ ] Test all endpoints with proper authentication
  - [ ] Test role-based access (customer vs admin)
  - [ ] Verify response formats

---

### Phase 2: Android Models & API Integration

#### 2.1 Create Model Classes
- [ ] `Ticket.java` - Ticket model
- [ ] `TicketStatus.java` - Status model
- [ ] `TicketComment.java` - Comment model
- [ ] `User.java` (if not exists) - User model for relationships

#### 2.2 Create Response Classes
- [ ] `TicketsResponse.java` - List response wrapper
- [ ] `TicketResponse.java` - Single ticket response wrapper
- [ ] `CommentsResponse.java` - Comments list response
- [ ] `TicketStatusResponse.java` - Statuses list response
- [ ] `StaffListResponse.java` - Staff list response

#### 2.3 Create Request Classes
- [ ] `CreateTicketRequest.java`
- [ ] `UpdateTicketRequest.java`
- [ ] `AddCommentRequest.java`

#### 2.4 Update API Service
- [ ] Add `@GET("api/v1/tickets")` method
- [ ] Add `@GET("api/v1/tickets/{id}")` method
- [ ] Add `@POST("api/v1/tickets")` method
- [ ] Add `@PUT("api/v1/tickets/{id}")` method
- [ ] Add `@DELETE("api/v1/tickets/{id}")` method (optional)
- [ ] Add `@GET("api/v1/tickets/{id}/comments")` method
- [ ] Add `@POST("api/v1/tickets/{id}/comments")` method
- [ ] Add `@GET("api/v1/ticket-statuses")` method
- [ ] Add `@GET("api/v1/staff")` method

---

### Phase 3: Tickets List Screen

#### 3.1 Layout Files
- [ ] `activity_tickets_list.xml` - Main layout
- [ ] `item_ticket.xml` - RecyclerView item layout
- [ ] `item_ticket_status_filter.xml` - Filter chip layout (optional)

#### 3.2 Adapter
- [ ] `TicketsAdapter.java` - RecyclerView adapter
  - [ ] Implement ViewHolder
  - [ ] Bind ticket data to views
  - [ ] Handle click events
  - [ ] Display priority badges (color-coded)
  - [ ] Display status badges (color-coded)

#### 3.3 Activity
- [ ] `TicketsListActivity.java`
  - [ ] Initialize RecyclerView
  - [ ] Fetch tickets from API
  - [ ] Implement pull-to-refresh
  - [ ] Handle loading states
  - [ ] Handle empty states
  - [ ] Handle error states
  - [ ] Navigate to detail screen on item click
  - [ ] FAB button to create ticket

#### 3.4 Customer-Specific Features
- [ ] Show only customer's own tickets
- [ ] Basic filter by status (optional)

#### 3.5 Admin-Specific Features
- [ ] Show all tickets
- [ ] Filter by status (chip group or dropdown)
- [ ] Filter by priority (chip group or dropdown)
- [ ] Search by title/description (SearchView)
- [ ] Sort by date/priority/status

---

### Phase 4: Create Ticket Screen

#### 4.1 Layout File
- [ ] `activity_create_ticket.xml`
  - [ ] Title input field
  - [ ] Description input field (multi-line)
  - [ ] Priority selection (Customer: radio buttons, Admin: dropdown)
  - [ ] Customer dropdown (Admin only)
  - [ ] Staff dropdown (Admin only)
  - [ ] Status dropdown (Admin only)

#### 4.2 Activity
- [ ] `CreateTicketActivity.java`
  - [ ] Form validation
  - [ ] Submit to API
  - [ ] Loading indicator
  - [ ] Success navigation to detail screen
  - [ ] Error handling
  - [ ] Role-based form fields

---

### Phase 5: Ticket Detail Screen

#### 5.1 Layout File
- [ ] `activity_ticket_detail.xml`
  - [ ] Ticket information section
  - [ ] Comments RecyclerView
  - [ ] Add comment button
  - [ ] Edit button (Admin only)
  - [ ] Quick actions (Admin: assign, change status)

#### 5.2 Comments Adapter
- [ ] `CommentsAdapter.java`
  - [ ] Display comment text
  - [ ] Display user name
  - [ ] Display timestamp

#### 5.3 Activity
- [ ] `TicketDetailActivity.java`
  - [ ] Fetch ticket details from API
  - [ ] Display ticket information
  - [ ] Load and display comments
  - [ ] Handle "Add Comment" button
  - [ ] Handle "Edit" button (Admin)
  - [ ] Role-based UI elements
  - [ ] Refresh functionality

---

### Phase 6: Edit Ticket Screen (Admin Only)

#### 6.1 Layout File
- [ ] `activity_edit_ticket.xml`
  - [ ] All editable fields (title, description)
  - [ ] Status dropdown
  - [ ] Priority dropdown
  - [ ] Staff assignment dropdown

#### 6.2 Activity
- [ ] `EditTicketActivity.java`
  - [ ] Pre-fill form with existing ticket data
  - [ ] Form validation
  - [ ] Update ticket via API
  - [ ] Navigate back to detail screen on success

---

### Phase 7: Comments System

#### 7.1 Add Comment Dialog
- [ ] `dialog_add_comment.xml` - Dialog layout
- [ ] `AddCommentDialog.java` - Dialog class
  - [ ] Show dialog
  - [ ] Submit comment via API
  - [ ] Refresh comments list on success

#### 7.2 Comment Item Layout
- [ ] `item_comment.xml` - Comment item layout

---

### Phase 8: Dashboard Integration

- [ ] Add "My Tickets" button to DashboardActivity (Customer)
- [ ] Add "All Tickets" button to DashboardActivity (Admin)
- [ ] Add ticket count display
- [ ] Add "Create Ticket" quick action
- [ ] Navigate to TicketsListActivity

---

### Phase 9: UI/UX Polish

#### Visual Enhancements
- [ ] Priority badge colors (Low: Gray, Medium: Blue, High: Orange, Urgent: Red)
- [ ] Status badge colors (from API)
- [ ] Loading shimmer effect
- [ ] Empty state illustrations
- [ ] Error state with retry button
- [ ] Smooth animations
- [ ] Material Design 3 components

#### User Experience
- [ ] Pull-to-refresh on list screen
- [ ] Swipe gestures (optional)
- [ ] Context menus (optional)
- [ ] Confirmation dialogs for important actions
- [ ] Toast messages for success/error
- [ ] Proper error messages

---

### Phase 10: Testing

#### Functional Testing
- [ ] Test customer flow: Create → View → Comment
- [ ] Test admin flow: Create → Edit → Assign → Filter
- [ ] Test role-based permissions
- [ ] Test API error handling
- [ ] Test network error handling
- [ ] Test empty states
- [ ] Test loading states

#### Edge Cases
- [ ] No tickets scenario
- [ ] Network failure scenario
- [ ] Invalid data submission
- [ ] Large ticket lists (pagination)
- [ ] Large comment lists

---

## 📁 File Structure After Implementation

```
app/src/main/java/hans/ph/
├── api/
│   ├── ApiService.java (updated)
│   ├── Ticket.java
│   ├── TicketStatus.java
│   ├── TicketComment.java
│   ├── TicketsResponse.java
│   ├── TicketResponse.java
│   ├── CreateTicketRequest.java
│   ├── UpdateTicketRequest.java
│   └── AddCommentRequest.java
│
├── TicketsListActivity.java
├── CreateTicketActivity.java
├── TicketDetailActivity.java
├── EditTicketActivity.java (Admin only)
├── AddCommentDialog.java
│
├── adapter/
│   ├── TicketsAdapter.java
│   └── CommentsAdapter.java
│
└── util/
    └── TokenManager.java (existing)
```

```
app/src/main/res/layout/
├── activity_tickets_list.xml
├── item_ticket.xml
├── activity_create_ticket.xml
├── activity_ticket_detail.xml
├── activity_edit_ticket.xml
├── item_comment.xml
└── dialog_add_comment.xml
```

---

## 🚀 Recommended Implementation Order

1. **Backend API** (1-2 days) - CRITICAL: Must be done first
2. **Android Models & API Service** (1 day)
3. **Tickets List Screen** (2 days) - Most visible feature
4. **Create Ticket Screen** (1-2 days)
5. **Ticket Detail Screen** (2 days)
6. **Comments System** (1 day)
7. **Edit Ticket Screen** (1 day)
8. **UI Polish** (1-2 days)
9. **Testing** (1-2 days)

**Total Estimated Time**: 11-15 days

---

## 🔑 Key Implementation Notes

### Priority Values:
- Use constants: `"low"`, `"medium"`, `"high"`, `"urgent"`

### Status:
- Fetched from API: `GET /api/v1/ticket-statuses`
- Use `TicketStatus.color` for badge colors

### Role Detection:
- Check user role from `TokenManager` or `UserResponse`
- Show/hide UI elements based on role

### API Authentication:
- All ticket endpoints require `Authorization: Bearer {token}` header
- Use `TokenManager.getToken()` to get token

### Error Handling:
- Network errors: Show retry button
- API errors: Show user-friendly messages
- Validation errors: Show inline error messages

---

## 📝 Quick Reference

### API Endpoints Summary:
```
GET    /api/v1/tickets              - List tickets
GET    /api/v1/tickets/{id}         - Get ticket
POST   /api/v1/tickets              - Create ticket
PUT    /api/v1/tickets/{id}         - Update ticket
DELETE /api/v1/tickets/{id}         - Delete ticket (optional)
GET    /api/v1/tickets/{id}/comments - Get comments
POST   /api/v1/tickets/{id}/comments - Add comment
GET    /api/v1/ticket-statuses      - Get statuses
GET    /api/v1/staff                - Get staff (Admin)
```

### Role-Based Access:
- **Customer**: Can create, view own tickets, add comments
- **Admin**: Full access - create, view all, edit, assign, filter

---

**Use this checklist to track progress during implementation!**

