# ServiceHub - All-in-One Service Hub App

**The complete mobile solution for Ashcol Airconditioning Corporation's service management system.**

## Overview

ServiceHub is an all-in-one native Android application that serves **customers**, **employees (staff)**, and **administrators** - providing comprehensive mobile access to all aspects of Ashcol's service management ecosystem. The app connects seamlessly to the Ashcol Portal Laravel backend, enabling users to manage tickets, profiles, workloads, and business operations from anywhere.

---

## 📱 Current Features (As of Now)

### ✅ **Authentication & User Management**

#### Login System
- **Email & Password Login** (`MainActivity`)
  - Real-time email validation
  - Secure token-based authentication (Laravel Sanctum)
  - Auto-login check (remembers logged-in users)
  - Role-based navigation after login
  - Comprehensive error handling with user-friendly messages
  - Connection diagnostics for troubleshooting

#### Registration System
- **Multi-Step User Registration** (`RegisterActivity`)
  - **Step 1: Welcome Screen** (`CreateNewAccountFragment`)
    - Welcome message and introduction
    - Continue with email option
    - Social login options (Facebook/Google - UI ready)
  
  - **Step 2: Email Input** (`UserAddEmailFragment`)
    - Email address input with real-time validation
    - Email format validation
    - Stores email for use in OTP verification
  
  - **Step 3: Personal Information** (`activity_register.xml` - Direct Activity Layout)
    - First Name (validates no numbers, minimum length)
    - Last Name (validates no numbers)
    - Username (validates no spaces, minimum 4 characters)
    - Phone Number (validates 10-15 digits)
    - Real-time validation with visual feedback
    - Clean validation helper methods
  
  - **Step 4: Password Creation** (`UserCreatePasswordFragment`)
    - Password input with strength indicator
    - Confirm password validation
    - Password requirements:
      - Minimum 8 characters
      - At least one uppercase letter
      - At least one number
      - At least one symbol
    - Real-time strength feedback (Weak/Good/Strong)
  
  - **Step 5: OTP Verification** (`dialog_verification_code.xml`)
    - Automatic OTP sent to email from Step 2
    - 6-digit code input with auto-focus
    - Real-time code validation
    - Resend code functionality
    - Email masking for privacy
    - Full-screen dialog interface
  
  - **Step 6: Account Created**
    - Success message with user name
    - Automatic navigation to dashboard
    - Token and user data saved
  
  - **Features**:
    - Fragment-based architecture for modularity
    - Data persistence between steps
    - Modern back press handling with `OnBackPressedDispatcher`
    - Comprehensive form validation
    - Clean, maintainable code structure

#### Profile Management
- **View Profile** (`ProfileActivity`)
  - Display user information:
    - Full name
    - Email address
    - Role
  - Fetch user data from API
- **Edit Profile** (`ProfileActivity`)
  - Update name
  - Update email
  - Change password functionality
  - Profile information editing with validation
- **Logout** (`ProfileActivity`)
  - Secure logout with token clearing
  - API logout call
  - Navigation back to login screen

---

### ✅ **Ticket Management System**

#### Customer Ticket Features
- **Create Service Tickets** (`user_createTicket` Fragment, `ServiceSelectActivity`)
  - Service type selection
  - Ticket details form:
    - Title
    - Description
    - Address
    - Contact information
  - **Image Upload Support**:
    - Camera capture
    - Gallery selection
    - Image preview
    - Multipart form data upload
  - Ticket creation with API integration
  - Form validation

- **View My Tickets** (`user_Ticket` Fragment, `MyTicketsFragment`)
  - List of user's tickets
  - Ticket status tracking
  - Ticket history

- **Ticket Details** (`user_Ticket` Fragment)
  - View ticket information
  - Ticket status and updates

#### Employee Ticket Features
- **Assigned Tickets** (`EmployeeAssignedTicketsFragment`)
  - View tickets assigned to the employee
  - Ticket management interface

- **In Progress Tickets** (`InProgressFragment`)
  - View tickets currently in progress
  - Status tracking

- **Completed Tickets** (`CompletedFragment`)
  - View completed tickets
  - Work history

#### Admin Ticket Features
- **All Tickets View** (`AdminAllTicketsFragment`)
  - View all tickets across the system
  - System-wide ticket oversight

- **Ticket Assignments** (`AssignmentsFragment`)
  - Assign tickets to staff members
  - Workload management

---

### ✅ **Role-Based Dashboards**

#### Customer Dashboard (`DashboardActivity`)
- Welcome screen with user information
- **AI Chatbot Integration**:
  - Interactive chatbot interface
  - Send messages and receive AI responses
  - Chat history display
  - Real-time messaging
- Quick access to:
  - Create new tickets
  - View my tickets
  - Profile management
- Bottom navigation for easy access

#### Employee Dashboard (`employee_DashboardActivity`)
- Employee-specific dashboard
- Bottom navigation with:
  - **Dashboard** (`EmployeeDashboardFragment`) - Overview
  - **Assigned Tickets** (`EmployeeAssignedTicketsFragment`) - My assignments
  - **In Progress** (`InProgressFragment`) - Active work
  - **Completed** (`CompletedFragment`) - Finished work
  - **Settings** (`EmployeeSettingsFragment`) - Employee settings

#### Admin Dashboard (`admin_DashboardActivity`)
- Full administrative control panel
- Bottom navigation with:
  - **All Tickets** (`AdminAllTicketsFragment`) - System-wide ticket view
  - **Branches** (`BranchesFragment`) - Branch management
  - **Assignments** (`AssignmentsFragment`) - Ticket assignment
  - **Reports** (`ReportsFragment`) - Analytics and reporting
  - **Users** (`UsersFragment`) - User management

---

### ✅ **Additional Features**

#### User Interface
- **Material Design 3** components
- **Bottom Navigation** for role-based dashboards
- **Fragments** for modular UI components
- **Responsive layouts** for different screen sizes
- **Loading states** and progress indicators
- **Error handling** with user-friendly dialogs

#### Service Selection
- **Service Type Selection** (`ServiceSelectActivity`)
  - Choose service type
  - Create tickets with service-specific information
  - Image attachment support

#### Employee Management (Admin)
- **Add Employee** (`admin_addEmployee`)
  - Create new employee accounts
  - Employee registration form

#### User Management (Admin)
- **Users View** (`UsersFragment`)
  - View all system users
  - User management interface

#### Notifications
- **Notification Fragment** (`user_Notification`)
  - User notifications display
  - Notification management

---

## 🔧 Technical Features

### API Integration
- **RESTful API** communication with Laravel backend
- **Retrofit 2** for HTTP requests
- **OkHttp** with logging interceptor
- **Gson** for JSON parsing
- **Token-based authentication** (Bearer tokens)
- **30-second timeouts** for reliable connections
- **Error handling** with detailed diagnostics

### Security
- **Secure token storage** using SharedPreferences (`TokenManager`)
- **Email validation** with real-time feedback
- **Password strength validation** with visual indicators
- **OTP verification** for email confirmation
- **HTTPS/HTTP support** (configurable)
- **Network security configuration**
- **Email masking** in OTP dialog for privacy

### Data Management
- **Token persistence** across app sessions
- **User data caching** (email, name, role)
- **Auto-login** functionality
- **Session management**
- **Multi-step form data persistence** during registration
- **Data passing between fragments** via activity container

### Network Configuration
- **Emulator support**: `http://10.0.2.2:8000/`
- **Physical device support**: Configurable IP address
- **Cleartext traffic** allowed for development
- **Network security config** for trusted domains

---

## 📋 API Endpoints Used

### Authentication
- `POST /api/v1/login` - User login
- `POST /api/v1/register` - User registration
- `POST /api/v1/logout` - User logout
- `POST /api/v1/send-verification-code` - Send email verification code
- `POST /api/v1/verify-email` - Verify email with code

### User Management
- `GET /api/v1/user` - Get authenticated user information

### Tickets
- `POST /api/v1/tickets` - Create new ticket (with/without image)

### Chatbot
- `POST /api/v1/chatbot` - Send message to AI chatbot

---

## 🎯 User Roles & Capabilities

### 👤 **Customers**
- ✅ **Multi-step registration** with guided flow
- ✅ Register new account with email verification
- ✅ **OTP verification** via email
- ✅ Login to account
- ✅ View and edit profile
- ✅ Change password
- ✅ Create service tickets
- ✅ Upload images with tickets
- ✅ View own tickets
- ✅ Use AI chatbot
- ✅ Access customer dashboard

### 👷 **Employees (Staff)**
- ✅ Login to employee account
- ✅ View assigned tickets
- ✅ View in-progress tickets
- ✅ View completed tickets
- ✅ Access employee dashboard
- ✅ Employee settings management

### 👨‍💼 **Administrators**
- ✅ Login to admin account
- ✅ View all tickets system-wide
- ✅ Manage ticket assignments
- ✅ View branches
- ✅ Access reports and analytics
- ✅ Manage users
- ✅ Add new employees
- ✅ Full administrative access

---

## 🚀 Setup & Configuration

### Prerequisites
- Android Studio (latest version)
- Java 11+
- Laravel backend running (see Ashcol Portal README)
- Android device or emulator

### Configuration

1. **Update API Base URL** (`app/src/main/java/app/hub/api/ApiClient.java`):
   ```java
   // For Android Emulator:
   private static final String BASE_URL = "http://10.0.2.2:8000/";
   
   // For Physical Device (use your computer's IP):
   private static final String BASE_URL = "http://192.168.0.103:8000/";
   ```

2. **Network Security** (`app/src/main/res/xml/network_security_config.xml`):
   - Already configured for emulator and common IP addresses
   - Add your IP if using physical device

3. **Start Laravel Server**:
   ```bash
   cd C:\xampp\htdocs\Ashcol_Web
   php artisan serve
   ```

4. **Build and Run**:
   - Open project in Android Studio
   - Sync Gradle files
   - Build project
   - Run on emulator or physical device

---

## 📦 Project Structure

```
app/src/main/java/app/hub/
├── api/                    # API interfaces and models
│   ├── ApiClient.java      # Retrofit client configuration
│   ├── ApiService.java     # API endpoint definitions
│   ├── VerificationRequest.java
│   ├── VerificationResponse.java
│   ├── VerifyEmailRequest.java
│   ├── VerifyEmailResponse.java
│   └── [Other Request/Response models]
├── common/                 # Common activities and fragments
│   ├── RegisterActivity.java    # Multi-step registration container
│   ├── CreateNewAccountFragment.java  # Step 1: Welcome screen
│   ├── UserAddEmailFragment.java      # Step 2: Email input
│   └── UserCreatePasswordFragment.java # Step 4: Password creation
├── util/                   # Utility classes
│   ├── TokenManager.java   # Token storage and management
│   └── EmailValidator.java # Email validation
├── MainActivity.java        # Login screen
├── DashboardActivity.java  # Customer dashboard with chatbot
├── ProfileActivity.java     # Profile viewing and editing
├── ServiceSelectActivity.java # Service selection and ticket creation
├── admin_DashboardActivity.java # Admin dashboard
├── employee_DashboardActivity.java # Employee dashboard
├── admin_addEmployee.java  # Add employee (admin)
└── [Fragments]             # UI fragments for different views
    ├── user_createTicket.java
    ├── user_Ticket.java
    ├── user_Profile.java
    ├── MyTicketsFragment.java
    ├── AdminAllTicketsFragment.java
    ├── EmployeeAssignedTicketsFragment.java
    └── [Other fragments...]
```

### Registration Flow Architecture

The registration system uses a **hybrid approach**:
- **Fragment-based steps**: Steps 1, 2, and 4 use fragments for modularity
- **Direct activity layout**: Step 3 (Tell Us) uses `activity_register.xml` directly for better control
- **Dialog-based OTP**: Step 5 uses a full-screen dialog for OTP verification
- **Data management**: `RegisterActivity` acts as a container, storing data between steps via setters/getters

#### Registration Layout Files
- `fragment_create_new_acc.xml` - Welcome screen (Step 1)
- `fragment_user_add_email.xml` - Email input (Step 2)
- `activity_register.xml` - Personal information form (Step 3)
- `fragment_user_create_pass.xml` - Password creation (Step 4)
- `dialog_verification_code.xml` - OTP verification dialog (Step 5)

---

## 🔐 Default Test Accounts

After running Laravel migrations and seeders:

- **Admin**: `admin@example.com` / `password`
- **Staff**: `staff@example.com` / `password`
- **Customer**: `customer@example.com` / `password`

---

## 🛠️ Technical Stack

- **Language**: Java 11
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Architecture**: MVC (Model-View-Controller) with Fragment-based UI
- **Networking**: Retrofit 2.9.0, OkHttp 4.12.0
- **JSON Parsing**: Gson
- **UI Framework**: Material Design 3 Components
- **Navigation**: Fragment-based with `OnBackPressedDispatcher` for modern back handling
- **Backend**: Laravel 11 with Sanctum authentication

---

## 📝 Package Structure

- **Package Name**: `app.hub`
- **Namespace**: `app.hub`
- **Application ID**: `app.hub`

---

## 🐛 Troubleshooting

### Connection Issues
- **"Connection Error"**: 
  - Check if Laravel server is running (`php artisan serve`)
  - Verify BASE_URL in `ApiClient.java`
  - For emulator: Use `10.0.2.2:8000`
  - For physical device: Use your computer's IP address
  - Ensure device and computer are on same Wi-Fi network

### Timeout Issues
- Timeouts are set to 30 seconds (configurable in `ApiClient.java`)
- Check network connection speed
- Verify server is responding

### Authentication Issues
- Verify user exists in database
- Check token is being saved correctly
- Clear app data and re-login if needed

---

## 📚 Additional Documentation

- `API_CONFIG.md` - API configuration guide
- `MAINACTIVITY_EXPLANATION.md` - Detailed code explanation
- `STATUS_SUMMARY.md` - Development status
- `TROUBLESHOOTING.md` - Common issues and solutions

---

## 🗺️ Future Enhancements

### Planned Features
- [ ] Real-time ticket updates
- [ ] Push notifications
- [ ] Offline mode with local caching
- [ ] Advanced ticket filtering and search
- [ ] Ticket comments system
- [ ] File attachments for tickets
- [ ] Workload calendar view
- [ ] Advanced reporting and analytics
- [ ] Branch management features
- [ ] Employee roster management
- [ ] Social login integration (Facebook/Google)
- [ ] Biometric authentication
- [ ] Remember me functionality

### Technical Improvements
- [ ] Repository pattern implementation
- [ ] ViewModel integration (MVVM architecture)
- [ ] Dependency injection (Hilt/Dagger)
- [ ] Room database for offline support
- [ ] Unit and integration tests
- [ ] Code documentation improvements
- [ ] String resources externalization (i18n support)
- [ ] Accessibility improvements

---

## 🤝 Contributing

- Branch naming: `feature/<name>`
- Small, descriptive commits
- Open PRs for review
- Follow Material Design guidelines
- Maintain code consistency

---

## 📄 License

This project uses Android (Apache 2.0). See [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

---

## 📞 Support

For issues or questions:
1. Check `TROUBLESHOOTING.md`
2. Review API documentation
3. Check Laravel backend logs
4. Verify network configuration

---

## 📝 Recent Updates

### Registration System Improvements (Latest)
- ✅ **Multi-step registration flow** with fragment-based architecture
- ✅ **OTP verification dialog** with automatic email sending
- ✅ **Improved validation system** with helper methods for cleaner code
- ✅ **Modern back press handling** using `OnBackPressedDispatcher`
- ✅ **Email masking** for privacy in OTP dialog
- ✅ **Real-time validation** with visual feedback
- ✅ **Code cleanup** and optimization

---

**Last Updated**: January 2025
**Version**: 1.0
**Status**: Active Development
