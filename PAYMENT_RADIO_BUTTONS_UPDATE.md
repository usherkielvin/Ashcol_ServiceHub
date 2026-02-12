# Payment Radio Buttons Update

## Changes Made

### 1. Fixed Radio Button Behavior

**Problem**: Radio buttons inside MaterialCardViews within a RadioGroup weren't working properly - clicking the card or radio button didn't properly select/deselect options.

**Solution**: 
- Made radio buttons non-clickable (`android:clickable="false"` and `android:focusable="false"`)
- Card click handlers now control the radio button selection
- This ensures mutual exclusivity works correctly within the RadioGroup

**Files Modified**:
- `app/src/main/res/layout/fragment_user_payment.xml`
- `app/src/main/res/layout/fragment_user__profile_payment.xml`
- `app/src/main/res/layout/activity_payment_selection.xml`

### 2. Replaced GCash with Google Pay

**Problem**: Payment option showed "GCash" but needed to be changed to "Google Pay" (GPay).

**Solution**: Replaced all references to GCash with Google Pay throughout the codebase.

**Changes Made**:

#### Layout Files:
- `fragment_user_payment.xml`:
  - Changed `cardGcash` → `cardGpay`
  - Changed `ivGcash` → `ivGpay`
  - Changed `rbGcash` → `rbGpay`
  - Changed text "GCash" → "Google Pay"

- `fragment_user__profile_payment.xml`:
  - Changed `cardGcash` → `cardGpay`
  - Changed `rbGcash` → `rbGpay`
  - Changed text "GCash" → "Google Pay"
  - Changed subtitle "GCash bank" → "Google Payment"

- `activity_payment_selection.xml`:
  - Changed `rbGCash` → `rbGPay`
  - Changed text "GCash" → "Google Pay"

#### Java Files:
- `PaymentSelectionActivity.java`:
  - Changed variable `rbGCash` → `rbGPay`
  - Changed `R.id.rbGCash` → `R.id.rbGPay`
  - Changed `PaymentMethod.GCASH` → `PaymentMethod.GPAY`

- `UserPaymentFragment.java`:
  - Changed variable `rbGcash` → `rbGpay`
  - Changed variable `cardGcash` → `cardGpay`
  - Changed variable `gcashChecked` → `gpayChecked`
  - Changed `R.id.rbGcash` → `R.id.rbGpay`
  - Changed `R.id.cardGcash` → `R.id.cardGpay`
  - Changed method parameter names
  - Changed display text "GCash" → "Google Pay"
  - Updated comments

- `PaymentMethod.java` (enum):
  - Changed `GCASH("gcash", "GCash")` → `GPAY("gpay", "Google Pay")`

- `EmployeeWorkConfirmPaymentFragment.java`:
  - Updated payment method check from `"gcash"` → `"gpay"` and `"google pay"`

## Payment Methods Available

After the update, the payment methods are:
1. **Cash** - Default selection, payment collected by technician
2. **Google Pay** - Online payment via Google Pay
3. **Credit Card** - Online payment via credit card
4. **Bank Transfer** - Bank transfer payment (some screens show as "Coming Soon")

## Radio Button Behavior

- Only one payment method can be selected at a time
- Clicking anywhere on the payment card selects that method
- Radio buttons are visual indicators only (not directly clickable)
- Card click handlers manage the selection state
- Cash is pre-selected by default

## Testing Notes

- Verify radio buttons work correctly when clicking cards
- Confirm only one option can be selected at a time
- Check that "Google Pay" displays correctly in all payment screens
- Test payment flow with Google Pay selection
- Ensure backend API accepts "gpay" as payment method value
