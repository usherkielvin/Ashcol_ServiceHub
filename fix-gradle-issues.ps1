# PowerShell script to fix Gradle build issues
# This script resolves ClassCastException and Gradle sync issues

param(
    [string]$ProjectPath = $PSScriptRoot
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fixing Gradle Build Issues" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop all Gradle daemons
Write-Host "Step 1: Stopping all Gradle daemons..." -ForegroundColor Yellow
try {
    & "$ProjectPath\gradlew.bat" --stop 2>&1 | Out-Null
    Write-Host "  ✓ Gradle daemons stopped" -ForegroundColor Green
} catch {
    Write-Host "  ⚠ Could not stop Gradle daemons (may not be running)" -ForegroundColor Yellow
}

# Step 2: Clean build directories
Write-Host "`nStep 2: Cleaning build directories..." -ForegroundColor Yellow
$buildPaths = @(
    "$ProjectPath\app\build",
    "$ProjectPath\build",
    "$ProjectPath\.gradle",
    "$ProjectPath\app\.cxx"
)

foreach ($path in $buildPaths) {
    if (Test-Path $path) {
        try {
            Remove-Item -Path $path -Recurse -Force -ErrorAction Stop
            Write-Host "  ✓ Deleted: $path" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠ Could not delete: $path" -ForegroundColor Yellow
            Write-Host "    (This is OK if Android Studio is open)" -ForegroundColor Gray
        }
    }
}

# Step 3: Clean Gradle cache
Write-Host "`nStep 3: Cleaning Gradle cache..." -ForegroundColor Yellow
$gradleCachePaths = @(
    "$env:USERPROFILE\.gradle\caches",
    "$env:USERPROFILE\.gradle\daemon"
)

foreach ($path in $gradleCachePaths) {
    if (Test-Path $path) {
        try {
            # Only delete cache, not the entire .gradle folder
            Get-ChildItem -Path $path -Recurse -Force -ErrorAction SilentlyContinue | 
                Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-1) } | 
                Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "  ✓ Cleaned: $path" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠ Could not clean: $path" -ForegroundColor Yellow
        }
    }
}

# Step 4: Verify Java version
Write-Host "`nStep 4: Checking Java version..." -ForegroundColor Yellow
try {
    $javaVersion = & java -version 2>&1 | Select-Object -First 1
    Write-Host "  ✓ Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Java not found in PATH" -ForegroundColor Red
    Write-Host "    Please ensure Java 11+ is installed" -ForegroundColor Yellow
}

# Step 5: Update Gradle wrapper (if needed)
Write-Host "`nStep 5: Verifying Gradle wrapper..." -ForegroundColor Yellow
if (Test-Path "$ProjectPath\gradlew.bat") {
    Write-Host "  ✓ Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "  ✗ Gradle wrapper not found" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fix Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Close Android Studio completely" -ForegroundColor White
Write-Host "2. Kill any remaining Java processes (if needed)" -ForegroundColor White
Write-Host "3. Reopen Android Studio" -ForegroundColor White
Write-Host "4. Click 'Sync Project with Gradle Files'" -ForegroundColor White
Write-Host "5. If issues persist, click 'Invalidate Caches / Restart'" -ForegroundColor White
Write-Host ""
