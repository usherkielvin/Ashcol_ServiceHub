# Adding Location Column to Users Table in Railway

## Overview

This guide explains how to add the `location` column to the `users` table in your Railway database. The location field is used for:

- **User registration:** Users can enter their location manually
- **Automatic detection:** The Android app can auto-detect user location and auto-fill the field
- **Service matching:** Future features can use location for service recommendations

## Steps

### 1. Connect to Your Railway Database

1. Go to your Railway project dashboard
2. Navigate to your database service
3. Click on "Connect" or "Connect to Database"
4. Choose your preferred method (e.g., Railway CLI, psql, etc.)

### 2. Run the Migration

If you have access to the Laravel application, you can run the migration directly:

```bash
php artisan migrate
```

This will automatically add the `location` column to the `users` table.

### 3. Manual SQL (Alternative Method)

If you prefer to run the SQL manually, execute the following command:

```sql
ALTER TABLE users ADD COLUMN location VARCHAR(255) NULL;
```

### 4. Verify the Column Was Added

Run the following query to verify the column was added:

```sql
DESCRIBE users;
```

You should see the `location` column in the output.

### 5. Update Your Application

Make sure your application code is updated to handle the new `location` field. The changes should already be included in the codebase.

## Notes

- The `location` column is nullable, so existing users will not be affected
- The column has a maximum length of 255 characters
- The field is optional during registration but can be required if you modify the validation rules

## Troubleshooting

If you encounter any issues:

1. Check that the database connection is working
2. Verify that you have the necessary permissions to modify the table
3. Ensure that the migration has not already been run (check the `migrations` table)
4. If using the manual SQL method, make sure the syntax is correct for your database engine

## Support

If you need further assistance, please check the project documentation or reach out to the development team.