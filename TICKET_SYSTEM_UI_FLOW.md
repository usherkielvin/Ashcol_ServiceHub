# 🎨 Ticket System - UI Flow Diagrams

## 📱 Screen Flow Diagrams

---

## 👤 CUSTOMER USER FLOW

```
┌─────────────────────┐
│  DashboardActivity  │
│  (Customer View)    │
│                     │
│  [My Tickets: 5]    │◄─── Shows ticket count
│  [Create Ticket]    │
└──────────┬──────────┘
           │
           ├─[Tap "My Tickets"]───┐
           │                       │
           └─[Tap "Create Ticket"]─┐
                                   │
                    ┌──────────────▼──────────────┐
                    │   TicketsListActivity       │
                    │   (Customer View)           │
                    │                             │
                    │  [Filter: All/Open/Closed]  │
                    │  ┌────────────────────────┐ │
                    │  │ Ticket #123            │ │◄─── RecyclerView
                    │  │ Aircon not working     │ │
                    │  │ Status: In Progress    │ │
                    │  └────────────────────────┘ │
                    │  ┌────────────────────────┐ │
                    │  │ Ticket #122            │ │
                    │  │ Refrigerator repair    │ │
                    │  │ Status: Open           │ │
                    │  └────────────────────────┘ │
                    │                             │
                    │  [+ Create Ticket] FAB      │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
           ┌────────▼──────┐      │      ┌───────▼────────┐
           │ TicketDetail  │      │      │ CreateTicket   │
           │ Activity      │      │      │ Activity       │
           │               │      │      │                │
           │ Ticket Info   │      │      │ Title          │
           │ Comments (5)  │      │      │ Description    │
           │               │      │      │ Priority       │
           │ [Add Comment] │      │      │                │
           └───────────────┘      │      │ [Cancel][Submit]│
                                  │      └────────┬───────┘
                                  │               │
                                  │      ┌────────▼───────┐
                                  │      │ Submit Ticket  │
                                  │      │ API Success    │
                                  │      └────────┬───────┘
                                  │               │
                                  └───────────────┼────────┐
                                                  │        │
                                        ┌─────────▼──────┐ │
                                        │ TicketDetail   │ │
                                        │ Activity       │ │
                                        │ (New Ticket)   │ │
                                        └────────────────┘ │
                                                           │
                                        ┌──────────────────▼──────┐
                                        │ AddCommentDialog        │
                                        │                         │
                                        │ [Comment Text Input]    │
                                        │                         │
                                        │ [Cancel] [Post Comment] │
                                        └─────────────────────────┘
```

---

## 👨‍💼 ADMIN USER FLOW

```
┌─────────────────────┐
│  DashboardActivity  │
│  (Admin View)       │
│                     │
│  [All Tickets: 25]  │
│  [Create Ticket]    │
└──────────┬──────────┘
           │
           ├─[Tap "All Tickets"]───┐
           │                       │
           └─[Tap "Create Ticket"]─┐
                                   │
                    ┌──────────────▼──────────────┐
                    │   TicketsListActivity       │
                    │   (Admin View)              │
                    │                             │
                    │  [🔍 Search]                │
                    │  [Filter: Status ▼]        │
                    │  [Filter: Priority ▼]      │
                    │  [Sort: Date ▼]            │
                    │                             │
                    │  ┌────────────────────────┐ │
                    │  │ #123 - Aircon Issue    │ │◄─── All tickets
                    │  │ Customer: John Doe     │ │     visible
                    │  │ Staff: Jane Smith      │ │
                    │  │ Status: In Progress    │ │
                    │  └────────────────────────┘ │
                    │  ┌────────────────────────┐ │
                    │  │ #122 - Refrigerator    │ │
                    │  │ Customer: Alice        │ │
                    │  │ Staff: (Unassigned)    │ │
                    │  │ Status: Open           │ │
                    │  └────────────────────────┘ │
                    │                             │
                    │  [+ Create Ticket] FAB      │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
           ┌────────▼──────┐      │      ┌───────▼────────┐
           │ TicketDetail  │      │      │ CreateTicket   │
           │ Activity      │      │      │ Activity       │
           │ (Admin View)  │      │      │ (Admin Form)   │
           │               │      │      │                │
           │ Ticket Info   │      │      │ Title *        │
           │ [Edit Button] │      │      │ Description *  │
           │               │      │      │ Customer *     │
           │ [Assign Staff]│      │      │ Staff ▼        │
           │ [Change Status]│     │      │ Status ▼       │
           │ [Change Priority]│   │      │ Priority ▼     │
           │               │      │      │                │
           │ Comments (5)  │      │      │ [Cancel][Create]│
           │ [Add Comment] │      │      └────────┬───────┘
           └───────┬───────┘      │               │
                   │              │      ┌────────▼───────┐
           ┌───────▼───────┐      │      │ Submit Ticket  │
           │ EditTicket    │      │      │ API Success    │
           │ Activity      │      │      └────────┬───────┘
           │               │      │               │
           │ Title *       │      │      ┌────────▼───────┐
           │ Description * │      │      │ TicketDetail   │
           │ Status ▼      │      │      │ Activity       │
           │ Priority ▼    │      │      │ (New Ticket)   │
           │ Staff ▼       │      │      └────────────────┘
           │               │      │
           │ [Cancel][Save]│      │
           └───────┬───────┘      │
                   │              │
           ┌───────▼──────────────┘
           │ Save Changes
           │ API Success
           │
           └───► TicketDetail (Refreshed)
```

---

## 🎯 Detailed Component Breakdown

### TicketsListActivity - States

#### Loading State:
```
┌────────────────────────────┐
│ My Tickets          [⚙️]   │
├────────────────────────────┤
│                            │
│  ╔════════════════════╗    │
│  ║  Loading...        ║    │◄─── ProgressBar
│  ╚════════════════════╝    │     or Shimmer
│                            │
│  ╔════════════════════╗    │
│  ║  Loading...        ║    │
│  ╚════════════════════╝    │
│                            │
└────────────────────────────┘
```

#### Empty State:
```
┌────────────────────────────┐
│ My Tickets          [⚙️]   │
├────────────────────────────┤
│                            │
│       📋                   │
│                            │
│  No tickets yet            │
│                            │
│  Create your first         │
│  service request           │
│                            │
│  [+ Create Ticket]         │
│                            │
└────────────────────────────┘
```

#### Error State:
```
┌────────────────────────────┐
│ My Tickets          [⚙️]   │
├────────────────────────────┤
│                            │
│       ⚠️                    │
│                            │
│  Failed to load tickets    │
│                            │
│  Please check your         │
│  connection and try again  │
│                            │
│  [Retry]                   │
│                            │
└────────────────────────────┘
```

#### Success State (with data):
```
┌────────────────────────────┐
│ My Tickets          [⚙️]   │
├────────────────────────────┤
│ [All] [Open] [In Progress] │◄─── Filter Chips
│                            │
│ ┌────────────────────────┐ │
│ │ [🔴] #123              │ │◄─── Priority Badge
│ │ Aircon not working     │ │
│ │                        │ │
│ │ Status: [In Progress]  │ │
│ │ Priority: [High]       │ │
│ │                        │ │
│ │ Customer: John Doe     │ │
│ │ 2 days ago             │ │
│ │ 💬 5 comments          │ │
│ └────────────────────────┘ │
│                            │
│ ┌────────────────────────┐ │
│ │ [🔵] #122              │ │
│ │ Refrigerator repair    │ │
│ │ ...                    │ │
│ └────────────────────────┘ │
│                            │
│          [+ FAB]           │◄─── Floating Action Button
└────────────────────────────┘
```

---

### Ticket Item Layout Details

#### Customer View Item:
```
┌─────────────────────────────────────────┐
│ [🔴 High]        #123        2 days ago │
├─────────────────────────────────────────┤
│ Aircon not working                      │
│                                         │
│ The air conditioning unit in the living │
│ room stopped working yesterday...       │
│                                         │
│ Status: [In Progress]  Priority: [High] │
│                                         │
│ Assigned to: Jane Smith                 │
│                                         │
│ 💬 5 comments                           │
└─────────────────────────────────────────┘
```

#### Admin View Item:
```
┌─────────────────────────────────────────┐
│ [🔴 Urgent]     #123        2 days ago  │
├─────────────────────────────────────────┤
│ Aircon not working                      │
│                                         │
│ Customer: John Doe                      │
│ Staff: Jane Smith                       │
│                                         │
│ Status: [In Progress]  Priority: [High] │
│                                         │
│ 💬 5 comments                           │
└─────────────────────────────────────────┘
```

---

### TicketDetailActivity - Comments Section

```
┌─────────────────────────────────────────┐
│ Ticket Details                          │
├─────────────────────────────────────────┤
│ [Ticket Info Section]                   │
│                                         │
├─────────────────────────────────────────┤
│ Comments (5)                            │
├─────────────────────────────────────────┤
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Jane Smith                          │ │
│ │ 2 hours ago                         │ │
│ ├─────────────────────────────────────┤ │
│ │ Thanks for reporting. I'll send a   │ │
│ │ technician tomorrow morning.        │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ John Doe                            │ │
│ │ 1 hour ago                          │ │
│ ├─────────────────────────────────────┤ │
│ │ Thank you! That would be great.     │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ [Add Comment Button]                    │
└─────────────────────────────────────────┘
```

---

## 🎨 Material Design Components Usage

### Priority Badges (Chips):
```
Low:     [Gray Chip]
Medium:  [Blue Chip]
High:    [Orange Chip]
Urgent:  [Red Chip]
```

### Status Badges (Chips):
```
Open:         [Gray Chip]
In Progress:  [Blue Chip]
Resolved:     [Green Chip]
Closed:       [Dark Gray Chip]
```

### Action Buttons:
- **FAB**: Floating Action Button for "Create Ticket"
- **Primary Button**: Solid color (Save, Submit, Create)
- **Secondary Button**: Outlined (Cancel, Edit)

### Input Fields:
- **TextInputLayout** with TextInputEditText
- **Dropdown Menus** (Spinner) for status, priority, staff
- **Radio Groups** for priority selection (Customer)

---

## 🔄 Navigation Patterns

### Navigation Stack:
```
DashboardActivity
    ↓
TicketsListActivity
    ↓
TicketDetailActivity
    ├─ EditTicketActivity (Admin only)
    └─ AddCommentDialog
```

### Back Navigation:
- All activities support back button
- EditTicketActivity → Returns to TicketDetailActivity
- AddCommentDialog → Closes, stays on TicketDetailActivity

---

## 📐 Layout Specifications

### Ticket Item Height:
- **Min Height**: 120dp
- **Padding**: 16dp
- **Margin**: 8dp between items

### Spacing:
- **Section Spacing**: 24dp
- **Card Padding**: 16dp
- **Text Spacing**: 8dp

### Typography:
- **Title**: 16sp, Bold
- **Body**: 14sp, Regular
- **Caption**: 12sp, Regular (for timestamps)
- **Label**: 14sp, Medium (for labels)

---

## 🎯 User Experience Flow

### Creating a Ticket (Customer):
1. User taps "Create Ticket" button
2. Form appears with 3 fields: Title, Description, Priority
3. User fills form
4. Validation on submit:
   - Title: Required, min 5 characters
   - Description: Required, min 10 characters
5. Show loading indicator on submit
6. On success: Navigate to new ticket detail
7. On error: Show error message, stay on form

### Viewing Tickets (Customer):
1. User sees list of their tickets
2. Tap on a ticket → Detail screen
3. Can see all information (read-only)
4. Can add comments
5. Can see status updates
6. Pull down to refresh list

### Admin Managing Tickets:
1. Admin sees all tickets in system
2. Can filter by status, priority
3. Can search by title/description
4. Tap ticket → Detail screen
5. Can edit ticket (tap Edit button)
6. Can assign/reassign staff
7. Can change status/priority
8. Can add comments
9. All changes saved via API

---

## 🚀 Quick Actions (Admin)

### Quick Actions in TicketDetailActivity:
- **Swipe Actions** (Optional):
  - Swipe left: Change status
  - Swipe right: Assign staff

### Context Menu (Long press):
- Edit Ticket
- Delete Ticket (if implemented)
- Assign to Staff
- Change Priority

---

**This document provides visual guidance for implementing the ticket system UI.**

