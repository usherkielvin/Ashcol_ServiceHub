# Unit Type Field Fix

## Problem

The "Unit Type" field in ticket details always showed "N/A" because:
1. The unit type information was being stored in the `description` field along with other details
2. The app wasn't reading from the separate `unit_type` database column
3. The API response classes weren't properly mapped to receive `unit_type` data

## Solution

Separated unit type from other details by:
1. Adding `unit_type` field to all API response classes
2. Updating ticket detail screens to display unit type separately
3. Keeping "Other Details" for the actual description text

## Changes Made

### API Response Classes

**TicketListResponse.java**:
- Added `@SerializedName("unit_type")` field to `TicketItem` class
- Added `getUnitType()` and `setUnitType()` methods
- Updated `fromCreateResponse()` to include unit type

**CreateTicketResponse.java**:
- Added `@SerializedName("unit_type")` field to `TicketData` class
- Added `getUnitType()` and `setUnitType()` methods

**TicketDetailResponse.java**:
- Already had `unit_type` field (no changes needed)

### Layout Files

**activity_employee_ticket_detail.xml**:
- Added "Unit Type" label and `tvUnitType` TextView
- Changed "Description" label to "Other Details"
- Now shows three separate fields:
  - Service Type
  - Unit Type (from `unit_type` column)
  - Other Details (from `description` column)

### Java Files

**EmployeeTicketDetailActivity.java**:
- Added `tvUnitType` TextView variable
- Initialized `tvUnitType` in `initViews()`
- Updated `displayTicketDetails()` to:
  - Get unit type from `ticket.getUnitType()`
  - Display "N/A" if unit type is null or empty
  - Show description separately in "Other Details"

**ManagerTicketDetailActivity.java**:
- Already correctly using `ticket.getUnitType()` (no changes needed)

## Data Flow

1. **Customer creates ticket**: Selects unit type in service form (e.g., "Split (1), Window (1)")
2. **Backend stores**: Unit type in `unit_type` column, other details in `description` column
3. **API returns**: Both fields separately in JSON response
4. **App displays**:
   - Unit Type: Shows the selected AC unit types
   - Other Details: Shows additional notes/description

## Before vs After

**Before**:
- Unit Type: N/A
- Description: "Unit Type: Split (1), Window (1), ARF (1)\nbello"

**After**:
- Service Type: Repair
- Unit Type: Split (1), Window (1), ARF (1)
- Other Details: bello

## Testing Notes

- Verify unit type displays correctly in all ticket detail screens
- Check that "N/A" shows when unit type is not provided
- Confirm other details (description) displays separately
- Test with various unit type combinations
- Ensure backward compatibility with old tickets that may not have unit_type set
