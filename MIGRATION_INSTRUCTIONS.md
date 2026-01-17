# Database Migration Instructions

## Add Phone Column to Users Table

A migration has been created to add the `phone` column to the `users` table. You need to run this migration.

### Steps:

1. **Open Terminal/Command Prompt** in your Laravel project directory:
   ```bash
   cd C:\xampp\htdocs\Ashcol_Web
   ```

2. **Run the migration**:
   ```bash
   php artisan migrate
   ```

3. **Verify the migration**:
   - Check that the `phone` column was added to the `users` table
   - You can verify in your database or run: `php artisan migrate:status`

### If Migration Fails:

If you get an error that the column already exists:
- The column might already be in your database
- You can skip this migration or modify it to check if column exists first

### Manual SQL Alternative:

If you prefer to run SQL directly:
```sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20) NULL AFTER email;
```

---

**After running the migration, the Google Sign-In flow should work properly!**
