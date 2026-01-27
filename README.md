# Ashcol ServiceHub

## Overview

Ashcol ServiceHub is a comprehensive service management platform with Android and web components.

## Features

### Android App (Ashcol_ServiceHub)
- User registration with automatic location detection
- **Hidden location field auto-filled after splash screen**
- Google Sign-In (requires configuration)
- Facebook Sign-In (requires configuration)
- Email verification
- Password management
- Profile management

### Web App (Ashcol_Web)
- User management
- Ticket system
- Admin dashboard
- API endpoints

## Setup Instructions

### Android App Setup

1. Open the project in Android Studio
2. Configure Google Sign-In (optional):
   - Get SHA-1 fingerprint: `./gradlew signingReport`
   - Create OAuth 2.0 Client ID in Google Cloud Console
   - Add package name: `app.hub`
   - Add SHA-1 fingerprint
   - Update `google-services.json` file
3. Configure Facebook Sign-In (optional):
   - Create Facebook App
   - Add Android platform
   - Add package name: `app.hub`
   - Add key hashes
   - Update `strings.xml` with Facebook App ID
4. Build and run the app

### Web App Setup

1. Install dependencies: `composer install`
2. Configure environment: `cp .env.example .env`
3. Generate app key: `php artisan key:generate`
4. Run migrations: `php artisan migrate`
5. Start server: `php artisan serve`

## Location Feature

### How Location Detection Works

1. **After splash screen:** MainActivity automatically requests location permission from the user
2. **Permission Granted:** The app retrieves your current location and stores it in SharedPreferences
3. **Auto Fill:** A hidden location field is automatically populated with the detected city (with toast notification)
4. **Location Storage:** Detected location is stored in SharedPreferences and used for all subsequent sign-ins
5. **Registration:** New registrations use the detected location from SharedPreferences
6. **Social Login:** Existing users (Google/Facebook) get their location updated from the detected location stored on the device
7. **Database Sync:** All signed-in users have their location updated in the database automatically

### Location Detection Logic

The app uses basic reverse geocoding to determine your city based on GPS coordinates:

- **Metro Manila Area:** Detects Manila City, Mandaluyong, Taguig, San Juan, etc.
- **Fallback:** Defaults to "Manila, Philippines" if precise location cannot be determined

### Location Permissions

The app requires these permissions:

- `ACCESS_FINE_LOCATION` - For precise GPS location
- `ACCESS_COARSE_LOCATION` - For network-based location (fallback)

Users will see a permission dialog on first launch asking for location access. The app will function normally even if location permission is denied.

## Database Migration for Location Field

To add the location field to the users table, run the following command:

```bash
php artisan migrate
```

This will add a `location` column to the `users` table.

### API Endpoints

### Authentication
- `POST /api/v1/register` - User registration (includes auto-detected location)
- `POST /api/v1/login` - User login
- `POST /api/v1/google-signin` - Google Sign-In
- `POST /api/v1/facebook-signin` - Facebook Sign-In

### User Management
- `GET /api/v1/user` - Get user profile (includes auto-detected location)
- `POST /api/v1/change-password` - Change password
- `POST /api/v1/set-initial-password` - Set initial password for Google users

### Tickets
- `POST /api/v1/tickets` - Create ticket
- `GET /api/v1/tickets` - Get user tickets
- `GET /api/v1/tickets/{id}` - Get ticket details
- `PUT /api/v1/tickets/{id}` - Update ticket status
- `POST /api/v1/tickets/{id}/comments` - Add comment to ticket

### Chatbot
- `POST /api/v1/chatbot` - Send message to AI chatbot

## Models

### User
- `id` - User ID
- `username` - Unique username
- `firstName` - First name
- `lastName` - Last name
- `name` - Full name (auto-generated)
- `email` - Email address
- `password` - Hashed password
- `role` - User role (admin, manager, staff, customer)
- `location` - Auto-detected user location (optional)
- `phone` - Phone number (optional)
- `profile_photo` - Profile photo path (optional)
- `email_verified_at` - Email verification timestamp
- `remember_token` - Remember token
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

### Ticket
- `id` - Ticket ID
- `customer_id` - Customer who created the ticket
- `assigned_staff_id` - Staff member assigned to the ticket
- `status_id` - Current status of the ticket
- `title` - Ticket title
- `description` - Ticket description
- `address` - Service address
- `contact` - Contact information
- `service_type` - Type of service requested
- `image` - Image of the issue (optional)
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

### Ticket Status
- `id` - Status ID
- `name` - Status name (e.g., "Open", "In Progress", "Resolved", "Closed")
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

### Ticket Comment
- `id` - Comment ID
- `ticket_id` - Ticket the comment belongs to
- `user_id` - User who made the comment
- `comment` - Comment text
- `created_at` - Creation timestamp
- `updated_at` - Update timestamp

## Roles

- **Admin** - Full access to all features
- **Manager** - Can manage staff and tickets
- **Staff** - Can handle tickets and communicate with customers
- **Customer** - Can create tickets and view their own tickets

## Error Handling

The app includes comprehensive error handling for:
- Network errors
- Authentication errors
- Validation errors
- Server errors
- Google Sign-In errors
- Facebook Sign-In errors

## Logging

The app logs important events and errors for debugging purposes. Check the Android logcat for detailed logs.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License.
