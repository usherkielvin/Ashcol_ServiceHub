# Registration Phone Number Update

## Changes Made

### 1. Philippine Phone Number Format

**Layout Changes** (`activity_register.xml`):
- Split phone input into two fields:
  - Fixed "+63" prefix (disabled, non-editable, 80dp width)
  - Number input field (10 digits max, flexible width)
- Added phone icon to the country code field
- Updated hint to "9XX XXX XXXX" to guide users
- Set `maxLength="10"` on the phone input field
- Updated validation message to "Invalid phone number (must be 10 digits)"

**Java Changes** (`RegisterActivity.java`):
- Updated `validatePhone()` to accept exactly 10 digits (not 10-15)
- Updated `validateTellUsForm()` to check for exactly 10 digits
- Modified `savePersonalInfoAndContinue()` to concatenate "+63" with the entered number
- Phone number is now stored as "+639XXXXXXXXX" format

### 2. NCR Cities Expansion

**Before**: Only 2 cities (Taguig City, Valenzuela City)

**After**: All 17 cities/municipality in Metro Manila:
1. Caloocan City
2. Las Piñas City
3. Makati City
4. Malabon City
5. Mandaluyong City
6. Manila City
7. Marikina City
8. Muntinlupa City
9. Navotas City
10. Parañaque City
11. Pasay City
12. Pasig City
13. Pateros
14. Quezon City
15. San Juan City
16. Taguig City
17. Valenzuela City

### 3. Other Regions

**Calabarzon** (unchanged):
- Rodriguez
- General Trias
- Dasmariñas
- Santa Rosa
- Santa Cruz
- Batangas City

**Central Luzon** (unchanged):
- San Fernando
- Malolos City

## Phone Number Format

**User Input**: 
- Sees fixed "+63" prefix
- Enters 10 digits (e.g., 9171234567)

**Stored Format**: 
- +639171234567

**Validation**:
- Must be exactly 10 digits
- Only numeric input allowed
- Real-time validation with error messages

## UI Layout

```
┌─────────────────────────────────────────┐
│ Phone number                            │
├──────────┬──────────────────────────────┤
│  📞 +63  │  9XX XXX XXXX               │
│ (fixed)  │  (user input, 10 digits)    │
└──────────┴──────────────────────────────┘
```

## Testing Notes

- Verify "+63" prefix is visible and non-editable
- Test that only 10 digits can be entered
- Confirm validation shows error for less than or more than 10 digits
- Check that phone number is saved as "+639XXXXXXXXX"
- Verify NCR dropdown shows all 17 cities
- Test region/city selection for all regions
- Ensure auto-detection still works for supported cities
