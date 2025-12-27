# MainActivity.java - Complete Syntax Explanation

## 📦 **SECTION 1: Package Declaration (Line 1)**

```java
package app.hub;
```

**Explanation:**
- `package` = Java keyword to declare the package/namespace
- `app.hub` = The package name (folder structure: `hans/ph/`)
- **Purpose:** Groups related classes together and prevents naming conflicts
- **Why:** All classes in this folder belong to the `app.hub` package

---

## 📥 **SECTION 2: Import Statements (Lines 3-25)**

### 2.1 Project Imports (Lines 3-9)
```java
import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.LoginRequest;
import app.hub.api.LoginResponse;
import app.hub.util.EmailValidator;
import app.hub.util.TokenManager;
```

**Explanation:**
- `import` = Java keyword to include external classes
- `app.hub.R` = Auto-generated class containing all resource IDs (layouts, strings, etc.)
- `app.hub.api.*` = API-related classes for network calls
- `app.hub.util.*` = Utility classes (email validation, token storage)

**Purpose:** Allows using these classes without full package names

---

### 2.2 Android Framework Imports (Lines 10-14)
```java
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
```

**Explanation:**
- `android.content.Intent` = Used to navigate between activities (screens)
- `android.os.Bundle` = Container for passing data between activities
- `android.text.Editable` = Interface for editable text content
- `android.text.TextWatcher` = Listener for text field changes
- `android.widget.Toast` = Small popup messages

---

### 2.3 AndroidX Library Imports (Lines 16-17)
```java
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
```

**Explanation:**
- `AppCompatActivity` = Base class for all activities (screens)
- `AlertDialog` = Popup dialog for showing messages/errors
- **AndroidX:** Modern Android support library (replaces old support library)

---

### 2.4 Material Design Imports (Lines 19-21)
```java
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
```

**Explanation:**
- Material Design = Google's design system
- `MaterialButton` = Styled button component
- `TextInputEditText` = Text input field
- `TextInputLayout` = Wrapper that adds label/error to text fields

---

### 2.5 Retrofit Library Imports (Lines 23-25)
```java
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
```

**Explanation:**
- Retrofit = HTTP client library for API calls
- `Call<T>` = Represents an API request
- `Callback<T>` = Handles API response (success/failure)
- `Response<T>` = Contains API response data

---

## 🏗️ **SECTION 3: Class Declaration (Line 27)**

```java
public class MainActivity extends AppCompatActivity {
```

**Explanation:**
- `public` = Access modifier (can be accessed from anywhere)
- `class` = Java keyword to define a class
- `MainActivity` = Class name (must match filename)
- `extends AppCompatActivity` = Inheritance (MainActivity inherits from AppCompatActivity)
- **Purpose:** MainActivity is a screen/activity in the Android app

---

## 🔧 **SECTION 4: Instance Variables (Line 29)**

```java
private TokenManager tokenManager;
```

**Explanation:**
- `private` = Access modifier (only accessible within this class)
- `TokenManager` = Type/class of the variable
- `tokenManager` = Variable name (camelCase convention)
- **Purpose:** Stores authentication token manager instance
- **Scope:** Available to all methods in this class

---

## 🎬 **SECTION 5: onCreate() Method (Lines 31-107)**

### 5.1 Method Declaration (Line 32)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
```

**Explanation:**
- `@Override` = Annotation indicating we're overriding a parent method
- `protected` = Access modifier (accessible in this class and subclasses)
- `void` = Return type (returns nothing)
- `onCreate` = Method name (Android lifecycle method)
- `Bundle savedInstanceState` = Parameter containing saved state (if activity was destroyed)
- **Purpose:** Called when activity is first created (entry point)

---

### 5.2 Super Call (Line 33)
```java
super.onCreate(savedInstanceState);
```

**Explanation:**
- `super` = Reference to parent class (AppCompatActivity)
- `super.onCreate()` = Calls parent's onCreate method
- **Why:** Required to initialize Android framework components
- **Purpose:** Ensures proper activity setup

---

### 5.3 Set Content View (Line 34)
```java
setContentView(R.layout.activity_main);
```

**Explanation:**
- `setContentView()` = Method to set which XML layout to display
- `R.layout.activity_main` = Reference to `activity_main.xml` layout file
- **Purpose:** Loads the login screen UI from XML
- **R class:** Auto-generated from resources (layouts, strings, images)

---

### 5.4 Initialize TokenManager (Line 36)
```java
tokenManager = new TokenManager(this);
```

**Explanation:**
- `tokenManager` = Instance variable (line 29)
- `new TokenManager()` = Creates a new TokenManager object
- `this` = Reference to current MainActivity instance
- **Purpose:** Initializes token storage utility

---

### 5.5 Auto-Login Check (Lines 38-45)
```java
// Check if already logged in
if (tokenManager.isLoggedIn()) {
    Intent intent = new Intent(this, DashboardActivity.class);
    intent.putExtra(DashboardActivity.EXTRA_EMAIL, tokenManager.getEmail());
    startActivity(intent);
    finish();
    return;
}
```

**Explanation:**
- `if (tokenManager.isLoggedIn())` = Conditional check if user has valid token
- `Intent intent = new Intent(...)` = Creates navigation intent
  - `this` = Current activity (MainActivity)
  - `DashboardActivity.class` = Destination activity
- `intent.putExtra(...)` = Adds extra data (email) to intent
- `startActivity(intent)` = Navigates to DashboardActivity
- `finish()` = Closes current activity (MainActivity)
- `return` = Exits method early (skips rest of code)

**Purpose:** If user is already logged in, skip login screen and go to dashboard

---

### 5.6 Find Views (Lines 47-51)
```java
TextInputEditText emailInput = findViewById(R.id.emailInput);
TextInputEditText passwordInput = findViewById(R.id.passwordInput);
TextInputLayout emailInputLayout = findViewById(R.id.emailInputLayout);
MaterialButton loginButton = findViewById(R.id.loginButton);
MaterialButton registerButton = findViewById(R.id.registerButton);
```

**Explanation:**
- `findViewById()` = Method to find UI elements by ID
- `R.id.emailInput` = Reference to `android:id="@+id/emailInput"` in XML
- `TextInputEditText` = Type of view (text input field)
- **Purpose:** Gets references to UI elements so we can interact with them
- **Why:** Java code needs references to manipulate XML-defined views

---

### 5.7 Real-Time Email Validation (Lines 53-85)

#### 5.7.1 Null Check (Line 54)
```java
if (emailInput != null) {
```

**Explanation:**
- `!=` = "Not equal to" operator
- `null` = No object/empty reference
- **Purpose:** Safety check to prevent crashes if view doesn't exist

---

#### 5.7.2 Add Text Watcher (Line 55)
```java
emailInput.addTextChangedListener(new TextWatcher() {
```

**Explanation:**
- `addTextChangedListener()` = Method to listen for text changes
- `new TextWatcher()` = Creates anonymous inner class
- **Purpose:** Triggers code whenever user types in email field

---

#### 5.7.3 TextWatcher Methods (Lines 56-60)
```java
@Override
public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

@Override
public void onTextChanged(CharSequence s, int start, int before, int count) {}
```

**Explanation:**
- `beforeTextChanged()` = Called before text changes (empty - not used)
- `onTextChanged()` = Called during text change (empty - not used)
- `CharSequence` = Type for text strings
- `int start, count, after` = Text change position details
- `{}` = Empty method body (does nothing)

---

#### 5.7.4 afterTextChanged Method (Lines 62-83)
```java
@Override
public void afterTextChanged(Editable s) {
    String email = s.toString().trim();
```

**Explanation:**
- `afterTextChanged()` = Called after text changes (we use this one)
- `Editable s` = The changed text content
- `s.toString()` = Converts Editable to String
- `.trim()` = Removes leading/trailing spaces
- **Purpose:** Validates email as user types

---

#### 5.7.5 Empty Email Check (Lines 65-70)
```java
if (email.isEmpty()) {
    if (emailInputLayout != null) {
        emailInputLayout.setError(null);
    }
    return;
}
```

**Explanation:**
- `email.isEmpty()` = Checks if string is empty
- `emailInputLayout.setError(null)` = Removes error message
- `return` = Exits method early
- **Purpose:** If field is empty, clear any errors

---

#### 5.7.6 Email Validation (Lines 72-82)
```java
EmailValidator.ValidationResult result = EmailValidator.validate(email);
if (!result.isValid() && email.length() > 5) {
    if (emailInputLayout != null) {
        emailInputLayout.setError(result.getMessage());
    }
} else {
    if (emailInputLayout != null) {
        emailInputLayout.setError(null);
    }
}
```

**Explanation:**
- `EmailValidator.validate(email)` = Static method call to validate email
- `ValidationResult result` = Object containing validation result
- `!result.isValid()` = "Not valid" (inverse of isValid)
- `&&` = Logical AND operator
- `email.length() > 5` = Check if email has more than 5 characters
- `result.getMessage()` = Gets error message if invalid
- `emailInputLayout.setError(...)` = Shows error message below input field
- **Purpose:** Shows validation error only if email is invalid AND user typed enough characters

---

### 5.8 Register Button Listener (Lines 87-92)
```java
if (registerButton != null) {
    registerButton.setOnClickListener(v -> {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    });
}
```

**Explanation:**
- `setOnClickListener()` = Method to handle button clicks
- `v -> { ... }` = Lambda expression (Java 8+ syntax)
  - `v` = View parameter (the button that was clicked)
  - `->` = Arrow operator (lambda syntax)
  - `{ ... }` = Code to execute when clicked
- `new Intent(this, RegisterActivity.class)` = Creates navigation to RegisterActivity
- `startActivity(intent)` = Opens RegisterActivity
- **Purpose:** Navigate to registration screen when register button is clicked

---

### 5.9 Login Button Listener (Lines 94-106)
```java
if (loginButton != null) {
    loginButton.setOnClickListener(v -> {
        String email = emailInput != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        login(email, password);
    });
}
```

**Explanation:**
- **Line 96:** `emailInput != null ? ... : ""` = Ternary operator
  - **Syntax:** `condition ? valueIfTrue : valueIfFalse`
  - **Meaning:** If emailInput exists, get text; otherwise use empty string
  - `getText()` = Gets text from input field
  - `.toString()` = Converts to String
  - `.trim()` = Removes spaces

- **Line 97:** Same for password (but no trim for passwords)

- **Line 99:** `email.isEmpty() || password.isEmpty()`
  - `||` = Logical OR operator
  - **Meaning:** If email is empty OR password is empty

- **Line 100:** `Toast.makeText(...).show()`
  - `Toast` = Small popup message
  - `makeText(context, message, duration)` = Creates toast
  - `Toast.LENGTH_SHORT` = Shows for 2 seconds
  - `.show()` = Displays the toast

- **Line 104:** `login(email, password)` = Calls login method (defined below)

**Purpose:** Validates input and calls login function when login button is clicked

---

## 🔐 **SECTION 6: login() Method (Lines 109-186)**

### 6.1 Method Declaration (Line 109)
```java
private void login(String email, String password) {
```

**Explanation:**
- `private` = Only accessible within this class
- `void` = Returns nothing
- `login` = Method name
- `String email, String password` = Parameters (user input)

---

### 6.2 Disable Login Button (Lines 110-114)
```java
final MaterialButton loginButton = findViewById(R.id.loginButton);
if (loginButton != null) {
    loginButton.setEnabled(false);
    loginButton.setText("Logging in...");
}
```

**Explanation:**
- `final` = Variable cannot be reassigned (required for use in inner classes)
- `setEnabled(false)` = Disables button (prevents double-clicking)
- `setText("Logging in...")` = Changes button text to show loading state
- **Purpose:** Visual feedback that login is in progress

---

### 6.3 Create API Service (Lines 116-117)
```java
ApiService apiService = ApiClient.getApiService();
LoginRequest request = new LoginRequest(email, password);
```

**Explanation:**
- `ApiClient.getApiService()` = Gets Retrofit API service instance
- `new LoginRequest(email, password)` = Creates request object with user data
- **Purpose:** Prepares API call

---

### 6.4 Make API Call (Lines 119-120)
```java
Call<LoginResponse> call = apiService.login(request);
call.enqueue(new Callback<LoginResponse>() {
```

**Explanation:**
- `apiService.login(request)` = Creates API call (doesn't execute yet)
- `Call<LoginResponse>` = Generic type (response will be LoginResponse)
- `call.enqueue(...)` = Executes API call asynchronously (non-blocking)
- `new Callback<LoginResponse>()` = Anonymous inner class for handling response
- **Purpose:** Makes HTTP POST request to server
- **Why async:** Doesn't freeze UI thread

---

### 6.5 onResponse Method (Lines 121-161)

#### 6.5.1 Method Declaration (Line 122)
```java
@Override
public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
```

**Explanation:**
- `onResponse` = Called when server responds
- `Response<LoginResponse>` = Contains server response data
- **Purpose:** Handles successful API response

---

#### 6.5.2 Reset Button (Lines 123-128)
```java
runOnUiThread(() -> {
    if (loginButton != null) {
        loginButton.setEnabled(true);
        loginButton.setText("Login");
    }
});
```

**Explanation:**
- `runOnUiThread()` = Executes code on main/UI thread
- `() -> { ... }` = Lambda expression (no parameters)
- **Why:** API call runs on background thread, but UI updates must be on UI thread
- **Purpose:** Re-enable button and restore text

---

#### 6.5.3 Check Response Success (Line 130)
```java
if (response.isSuccessful() && response.body() != null) {
```

**Explanation:**
- `response.isSuccessful()` = Checks if HTTP status is 200-299
- `&&` = Logical AND
- `response.body() != null` = Checks if response has data
- **Purpose:** Validates response is successful and has data

---

#### 6.5.4 Extract Response Data (Line 131)
```java
LoginResponse loginResponse = response.body();
```

**Explanation:**
- `response.body()` = Gets response data (parsed JSON)
- `LoginResponse` = Type of response object
- **Purpose:** Extracts server response

---

#### 6.5.5 Validate Response (Line 132)
```java
if (loginResponse.isSuccess() && loginResponse.getData() != null) {
```

**Explanation:**
- `loginResponse.isSuccess()` = Checks if login was successful (from server)
- `loginResponse.getData() != null` = Checks if response contains user data
- **Purpose:** Validates login success

---

#### 6.5.6 Save Token and User Data (Lines 133-136)
```java
// Save token and user data
tokenManager.saveToken("Bearer " + loginResponse.getData().getToken());
tokenManager.saveEmail(loginResponse.getData().getUser().getEmail());
tokenManager.saveName(loginResponse.getData().getUser().getName());
```

**Explanation:**
- `"Bearer " + ...` = String concatenation (adds "Bearer " prefix)
- `loginResponse.getData()` = Gets data object from response
- `.getToken()` = Gets authentication token
- `.getUser()` = Gets user object
- `.getEmail()` / `.getName()` = Gets user details
- `tokenManager.saveToken(...)` = Saves to SharedPreferences
- **Purpose:** Stores authentication token and user info for future API calls

---

#### 6.5.7 Navigate to Dashboard (Lines 138-144)
```java
// Navigate to dashboard
runOnUiThread(() -> {
    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
    intent.putExtra(DashboardActivity.EXTRA_EMAIL, loginResponse.getData().getUser().getEmail());
    startActivity(intent);
    finish();
});
```

**Explanation:**
- `MainActivity.this` = Explicit reference to MainActivity instance
- `intent.putExtra(...)` = Adds email as extra data
- `startActivity(intent)` = Opens DashboardActivity
- `finish()` = Closes MainActivity (user can't go back)
- **Why runOnUiThread:** Navigation must be on UI thread

---

#### 6.5.8 Handle Login Failure (Lines 145-148)
```java
} else {
    final String message = loginResponse.getMessage() != null ? loginResponse.getMessage() : "Login failed";
    runOnUiThread(() -> showError("Login Failed", message));
}
```

**Explanation:**
- `loginResponse.getMessage()` = Gets error message from server
- Ternary operator: `condition ? valueIfTrue : valueIfFalse`
- `final String message` = Final variable for use in lambda
- `showError(...)` = Shows error dialog (defined below)
- **Purpose:** Displays error if login failed

---

#### 6.5.9 Handle HTTP Error (Lines 149-160)
```java
} else {
    String errorMsg;
    if (response.code() == 401) {
        errorMsg = "Invalid email or password";
    } else if (response.code() == 500) {
        errorMsg = "Server error. Please try again later.";
    } else {
        errorMsg = "Invalid credentials";
    }
    final String finalErrorMsg = errorMsg;
    runOnUiThread(() -> showError("Login Failed", finalErrorMsg));
}
```

**Explanation:**
- `response.code()` = HTTP status code (401 = unauthorized, 500 = server error)
- `==` = Equality operator (comparing integers)
- `else if` = Alternative condition check
- `final String finalErrorMsg` = Final variable (required for lambda)
- **Purpose:** Shows specific error messages based on HTTP status code

---

### 6.6 onFailure Method (Lines 163-184)

#### 6.6.1 Method Declaration (Line 164)
```java
@Override
public void onFailure(Call<LoginResponse> call, Throwable t) {
```

**Explanation:**
- `onFailure` = Called when API call fails (network error, timeout, etc.)
- `Throwable t` = Exception object containing error details
- **Purpose:** Handles network/connection errors

---

#### 6.6.2 Reset Button (Lines 165-170)
```java
runOnUiThread(() -> {
    if (loginButton != null) {
        loginButton.setEnabled(true);
        loginButton.setText("Login");
    }
});
```

**Explanation:** Same as before - re-enables button on error

---

#### 6.6.3 Error Message Handling (Lines 172-181)
```java
String errorMsg = "Connection error";
if (t.getMessage() != null) {
    if (t.getMessage().contains("Failed to connect") || t.getMessage().contains("Unable to resolve host")) {
        errorMsg = "Cannot connect to server.\n\nPlease check:\n1. Laravel server is running\n2. Correct API URL in ApiClient.java\n3. Network connection";
    } else if (t.getMessage().contains("timeout")) {
        errorMsg = "Connection timeout. Server may be slow or unreachable.";
    } else {
        errorMsg = "Error: " + t.getMessage();
    }
}
```

**Explanation:**
- `t.getMessage()` = Gets error message from exception
- `.contains("...")` = Checks if string contains substring
- `||` = Logical OR operator
- `\n` = Newline character (creates line break)
- **Purpose:** Shows helpful error messages based on error type

---

#### 6.6.4 Show Error Dialog (Lines 182-183)
```java
final String finalErrorMsg = errorMsg;
runOnUiThread(() -> showError("Connection Error", finalErrorMsg));
```

**Explanation:** Shows error dialog with connection error message

---

## 💬 **SECTION 7: showError() Method (Lines 188-194)**

```java
private void showError(String title, String message) {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK", null)
        .show();
}
```

**Explanation:**
- `private` = Only accessible within this class
- `String title, String message` = Parameters for dialog content
- `new AlertDialog.Builder(this)` = Creates dialog builder
- `.setTitle(title)` = Sets dialog title
- `.setMessage(message)` = Sets dialog message
- `.setPositiveButton("OK", null)` = Adds OK button (null = no click listener)
- `.show()` = Displays the dialog
- **Method Chaining:** Multiple method calls on same object (builder pattern)

**Purpose:** Shows popup dialog with error message

---

## 📝 **KEY CONCEPTS SUMMARY**

### 1. **Activity Lifecycle**
- `onCreate()` = Called when activity is created
- `finish()` = Destroys activity

### 2. **Threading**
- `runOnUiThread()` = Executes code on UI thread
- API calls run on background thread (async)

### 3. **Navigation**
- `Intent` = Navigation object
- `startActivity()` = Opens new activity
- `putExtra()` = Passes data between activities

### 4. **Null Safety**
- `if (view != null)` = Prevents crashes if view doesn't exist

### 5. **Lambda Expressions**
- `v -> { ... }` = Shorthand for anonymous inner class
- Used for event listeners (button clicks, text changes)

### 6. **API Calls**
- `enqueue()` = Asynchronous API call
- `onResponse()` = Success handler
- `onFailure()` = Error handler

### 7. **Data Storage**
- `TokenManager` = Wrapper for SharedPreferences
- Stores authentication token and user data

---

## 🎯 **FLOW DIAGRAM**

```
MainActivity onCreate()
    ↓
Check if logged in → Yes → Navigate to Dashboard
    ↓ No
Load UI (email, password fields)
    ↓
User types email → Real-time validation
    ↓
User clicks Login → Validate input
    ↓
Call login() method
    ↓
Disable button → Show "Logging in..."
    ↓
Make API call (async)
    ↓
    ├─ Success → Save token → Navigate to Dashboard
    └─ Failure → Show error dialog
```

---

## 📚 **ADDITIONAL NOTES**

1. **Why `final` variables in lambdas?**
   - Java requires variables used in inner classes/lambdas to be final or effectively final
   - Prevents modification after lambda is created

2. **Why `runOnUiThread()`?**
   - Android UI can only be updated from main/UI thread
   - API callbacks run on background thread
   - Must switch to UI thread for UI updates

3. **Why null checks?**
   - `findViewById()` can return null if view doesn't exist
   - Prevents `NullPointerException` crashes

4. **Why `finish()` after navigation?**
   - Prevents user from going back to login screen after successful login
   - Better user experience

5. **Why "Bearer " prefix?**
   - Standard authentication token format
   - Server expects "Bearer <token>" in Authorization header

