# Edge-to-Edge Display Guide

The app now supports full-screen edge-to-edge display on Android devices with notches and cutouts.

## What's Changed

### Theme Updates
- Added `windowLayoutInDisplayCutoutMode` to both light and dark themes
- Enabled transparent status and navigation bars
- Disabled status/navigation bar contrast enforcement

### EdgeToEdgeHelper Utility
A new helper class (`app.hub.util.EdgeToEdgeHelper`) provides easy methods to enable edge-to-edge display.

## How to Use in Activities

### Basic Usage - Full Screen (Content behind status bar)

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // Enable edge-to-edge BEFORE setContentView
    EdgeToEdgeHelper.enable(this);
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.your_activity);
    
    // Apply insets to root view - only pads bottom for nav bar
    View rootView = findViewById(R.id.your_root_view);
    EdgeToEdgeHelper.applyInsets(rootView);
}
```

This draws content behind the status bar (time/battery area) for true fullscreen.

### With Status Bar Padding (If you need space at top)

```java
View rootView = findViewById(R.id.your_root_view);
EdgeToEdgeHelper.applyInsetsWithStatusBar(rootView);
```

### Selective Insets (For custom layouts)

If you want different padding on different sides:

```java
View rootView = findViewById(R.id.your_root_view);
EdgeToEdgeHelper.applyInsetsSelective(
    rootView,
    false,  // Don't apply top padding (draw behind status bar)
    true,   // Apply bottom padding (avoid navigation bar)
    false,  // Don't apply left padding
    false   // Don't apply right padding
);
```

### Get Status Bar Height

If you need to manually position elements:

```java
int statusBarHeight = EdgeToEdgeHelper.getStatusBarHeight(this);
// Use this to add margin/padding to specific views
```

## Layout Considerations

### Root View Requirements
Your root layout should have an ID so you can reference it:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:id="@+id/root_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Your content here -->
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Handling Toolbars/AppBars
If using a Toolbar or AppBar, you may want to apply insets only to specific views:

```java
// Apply top inset to toolbar
ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
    v.setPadding(0, systemBars.top, 0, 0);
    return insets;
});

// Apply bottom inset to bottom navigation
ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
    v.setPadding(0, 0, 0, systemBars.bottom);
    return insets;
});
```

## Testing

Test on devices with:
- Notches (Pixel 3 XL, iPhone X-style notches)
- Punch-hole cameras (Samsung Galaxy S10+, Pixel 4a)
- Different screen sizes and aspect ratios
- Both light and dark modes

## Automatic Application

The theme changes apply automatically to all activities. However, to prevent content from being hidden behind system bars, you need to call `EdgeToEdgeHelper` methods in each activity's `onCreate()`.

## Migration Checklist

For each activity you want to update:

1. [ ] Add `EdgeToEdgeHelper.enable(this)` before `setContentView()`
2. [ ] Add an ID to your root layout if it doesn't have one
3. [ ] Call `EdgeToEdgeHelper.applyInsets()` on the root view
4. [ ] Test on a device with a notch or cutout
5. [ ] Verify content isn't hidden behind system bars
