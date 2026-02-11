# Ashcol ServiceHub

## 👥 Development Team – Application Development Project

### 🎨 UI/UX Designer
**Hans Gabrielee Borillo** – UI/UX Designer & Figma Specialist

- User interface design in Figma
- Mobile app mockups & prototypes
- Design system & style guide
- User experience research
- Visual design & branding

### 💻 Android Frontend Developer
**Kenji A. Hizon** – Android XML Layout Developer

- Android XML layout implementation
- Material Design components
- Responsive mobile layouts
- UI component development
- Frontend integration with backend APIs

### 🔧 Backend Developer & Database Architect
**Dizon S. Dizon** – Backend Developer

- Laravel REST API development
- Database design & optimization
- API endpoint implementation
- Server-side business logic
- Data validation & security

### 👨💻 Project Lead & Backend Developer
**Usher Kielvin Ponce** – Project Lead & Backend Developer

- Project coordination & leadership
- Laravel REST API development
- Database architecture
- Firebase integration & real-time sync
- Branch-based routing system
- Server deployment & maintenance

---

## 📚 Academic Project Details

### Application Development Implementation
This project demonstrates practical implementation of modern application development concepts for a real-world service management system.

### Key Technologies & Frameworks

#### Backend (Laravel 11)
- **RESTful API** - Complete API for mobile-web communication
- **Sanctum Authentication** - Token-based authentication system
- **Eloquent ORM** - Database abstraction and relationships
- **Firebase Firestore** - Real-time data synchronization
- **Migration System** - Version-controlled database schema

#### Mobile (Android - Java)
- **Retrofit 2** - Type-safe HTTP client for API communication
- **Gson** - JSON serialization/deserialization
- **Google Sign-In** - OAuth 2.0 authentication
- **Firebase Cloud Messaging** - Push notifications
- **SharedPreferences** - Local data persistence
- **Material Design Components** - Modern UI/UX

#### Database
- **MySQL** - Primary relational database
- **Firebase Firestore** - Real-time NoSQL database for live updates

### Key Features Implemented

#### 1. **Multi-Role Authentication System**
- Customer, Technician, Manager, and Admin roles
- Google OAuth integration
- Email verification system
- Password reset functionality

#### 2. **Service Ticket Management**
- Create service requests with image attachments
- Real-time status updates via Firebase
- Branch-based automatic assignment
- Priority queue management

#### 3. **Branch-Based Routing Algorithm**
- Automatic branch assignment based on region/city
- Region extraction from branch names
- Fallback mechanisms for unassigned users

#### 4. **Real-Time Synchronization**
- Firebase Firestore integration
- Live ticket status updates
- Instant assignment notifications
- Manager dashboard real-time updates

#### 5. **Profile Management**
- Photo upload and storage
- Auto-assignment of region/city from branch
- FCM token registration for push notifications

### Application Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Android Application                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Activities  │  │  Fragments   │  │   Adapters   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         │                  │                  │          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           API Service (Retrofit)                  │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  Laravel REST API                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Controllers  │  │    Models    │  │  Middleware  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│         │                  │                  │          │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Database (MySQL)                     │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│              Firebase Firestore (Real-time)              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Tickets    │  │    Users     │  │  Branches    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### API Endpoints

#### Authentication
- `POST /api/v1/register` - User registration with branch auto-assignment
- `POST /api/v1/login` - User login with token generation
- `POST /api/v1/google-signin` - Google OAuth authentication
- `POST /api/v1/google-register` - Google account registration

#### User Management
- `GET /api/v1/user` - Get authenticated user profile
- `POST /api/v1/user/update` - Update user profile
- `POST /api/v1/user/photo` - Upload profile photo
- `POST /api/v1/change-password` - Change password
- `POST /api/v1/fcm-token` - Register FCM token for notifications

#### Ticket Management
- `POST /api/v1/tickets` - Create service ticket
- `GET /api/v1/tickets` - Get user tickets (role-based filtering)
- `GET /api/v1/tickets/{id}` - Get ticket details
- `PUT /api/v1/tickets/{id}` - Update ticket status
- `POST /api/v1/tickets/{id}/assign` - Assign ticket to technician
- `POST /api/v1/tickets/{id}/comments` - Add comment to ticket

#### Manager & Admin
- `GET /api/v1/manager/tickets` - Get branch tickets (managers)
- `GET /api/v1/manager/employees` - Get branch employees
- `GET /api/v1/admin/users` - Get all users (admin)
- `DELETE /api/v1/admin/users/{id}` - Delete user (admin)

---

## Database Schema

### Users Table
```sql
- id (Primary Key)
- username (Unique)
- firstName
- lastName
- email (Unique)
- password (Hashed)
- role (customer, technician, manager, admin)
- region (Auto-assigned from branch)
- city (Auto-assigned from branch)
- branch (Branch name)
- phone
- gender
- birthdate
- profile_photo
- fcm_token (For push notifications)
- email_verified_at
- created_at, updated_at
```

### Tickets Table
```sql
- id (Primary Key)
- ticket_id (Unique, e.g., TCK-001)
- customer_id (Foreign Key → users)
- assigned_staff_id (Foreign Key → users)
- branch_id (Foreign Key → branches)
- status_id (Foreign Key → ticket_statuses)
- title
- description
- address
- contact
- service_type
- amount
- unit_type
- preferred_date
- scheduled_date
- scheduled_time
- image_path
- attachment_url
- created_at, updated_at
```

### Branches Table
```sql
- id (Primary Key)
- name (e.g., ASHCOL Valenzuela)
- location (Region, City format)
- address
- latitude, longitude
- is_active
- created_at, updated_at
```

### Ticket Statuses Table
```sql
- id (Primary Key)
- name (Pending, Assigned, In Progress, Completed, Cancelled)
- color (Hex color code)
- is_default
- created_at, updated_at
```

---

## Setup Instructions

### Android App Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Ashcol_ServiceHub
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory

3. **Configure Google Sign-In** (Optional)
   ```bash
   # Get SHA-1 fingerprint
   ./gradlew signingReport
   ```
   - Create OAuth 2.0 Client ID in Google Cloud Console
   - Add package name: `app.hub`
   - Add SHA-1 fingerprint
   - Download and replace `google-services.json` in `app/` directory

4. **Update API Base URL**
   - Edit `ApiClient.java`
   - Update `BASE_URL` to your Laravel backend URL

5. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

### Backend Setup (Laravel)

1. **Install Dependencies**
   ```bash
   cd Ashcol_Web
   composer install
   npm install
   ```

2. **Environment Configuration**
   ```bash
   cp .env.example .env
   php artisan key:generate
   ```

3. **Configure Database** (`.env`)
   ```env
   DB_CONNECTION=mysql
   DB_HOST=127.0.0.1
   DB_PORT=3306
   DB_DATABASE=ashcol_db
   DB_USERNAME=root
   DB_PASSWORD=
   ```

4. **Run Migrations**
   ```bash
   php artisan migrate --seed
   ```

5. **Configure Firebase** (Optional for real-time features)
   - Download service account JSON from Firebase Console
   - Place in `storage/app/firebase/`
   - Update `FIREBASE_CREDENTIALS` in `.env`

6. **Start Development Server**
   ```bash
   php artisan serve
   npm run dev
   ```

---

## Key Algorithms & Logic

### 1. Branch Assignment Algorithm
```java
// Automatic branch assignment based on region/city
String guessedBranch = Branch.guessFromRegionCity(region, city);

// Extract region/city from branch name (reverse mapping)
Map<String, String> regionCity = Branch.extractRegionCityFromBranch(branchName);
```

**Supported Regions:**
- NCR (National Capital Region)
- Central Luzon (Bulacan, Pampanga, etc.)
- CALABARZON (Cavite, Laguna, Batangas, Rizal, Quezon)

### 2. Ticket Queue Management
- FIFO (First In, First Out) for pending tickets
- Priority-based assignment for managers
- Real-time status synchronization via Firebase

### 3. Authentication Flow
```
User Login → Token Generation → Store in SharedPreferences
         ↓
    Validate Token → API Request with Bearer Token
         ↓
    Role-Based Access Control → Dashboard Routing
```

### 4. Real-Time Sync Algorithm
```
MySQL Update → Trigger Firestore Sync → Push to Mobile
     ↓                                        ↓
Persistent Storage              Real-time UI Update
```

---

## Project Scope

**Course:** Application Development Final Project  
**Institution:** NU MOA (National University - Mall of Asia)  
**Objective:** Develop a complete mobile-web service management system  
**Focus:** RESTful API, Real-time synchronization, Multi-role authentication

---

## Features Showcase

### Customer Features
- ✅ Register with Google or Email
- ✅ Create service requests with photos
- ✅ Track ticket status in real-time
- ✅ View service history
- ✅ Receive push notifications

### Technician Features
- ✅ View assigned tickets
- ✅ Update ticket status
- ✅ Add comments and updates
- ✅ Filter by status (Pending, In Progress, Completed)

### Manager Features
- ✅ View all branch tickets
- ✅ Assign tickets to technicians
- ✅ Monitor employee workload
- ✅ Real-time dashboard updates
- ✅ Generate reports

### Admin Features
- ✅ User management (CRUD)
- ✅ Branch management
- ✅ System-wide analytics
- ✅ Delete users and tickets

---

## Testing

### Unit Tests
```bash
# Laravel backend tests
php artisan test

# Android instrumented tests
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] User registration (Email & Google)
- [ ] Login with different roles
- [ ] Create service ticket with image
- [ ] Assign ticket to technician
- [ ] Update ticket status
- [ ] Real-time dashboard updates
- [ ] Push notifications
- [ ] Profile photo upload

---

## Error Handling

The application includes comprehensive error handling:
- Network errors with retry mechanisms
- Authentication errors with token refresh
- Validation errors with user-friendly messages
- Server errors with fallback responses
- Google Sign-In errors with detailed logging

---

## Security Features

- ✅ Password hashing (bcrypt)
- ✅ Token-based authentication (Sanctum)
- ✅ CSRF protection
- ✅ SQL injection prevention (Eloquent ORM)
- ✅ XSS protection
- ✅ Role-based access control
- ✅ File upload validation

---

## Future Enhancements

- [ ] Offline mode with local database sync
- [ ] In-app chat between customer and technician
- [ ] Payment integration
- [ ] Service rating system
- [ ] Advanced analytics dashboard
- [ ] Multi-language support

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## Acknowledgments

- **NU MOA Faculty** - For guidance and support
- **Ashcol Airconditioning Corporation** - For the real-world use case
- **Google Firebase** - For real-time database services
- **Laravel Community** - For excellent documentation

---

## Contact

**Project Lead:** Usher Kielvin Ponce  
**Email:** [Your Email]  
**GitHub:** [Your GitHub Profile]

---

**Built with ❤️ by the NU MOA APPDEV Team**
