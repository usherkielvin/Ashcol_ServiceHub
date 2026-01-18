# Fix Android Studio IDE Sync Issues
# Run this if you see red errors in Android Studio but Gradle builds successfully

Write-Host "🔧 Fixing Android Studio IDE Sync..." -ForegroundColor Cyan
Write-Host ""

# Stop Gradle
Write-Host "1. Stopping Gradle daemons..." -ForegroundColor Yellow
& .\gradlew.bat --stop
Write-Host "   ✓ Daemons stopped" -ForegroundColor Green
Write-Host ""

# Clean Android Studio caches
Write-Host "2. Cleaning Android Studio caches..." -ForegroundColor Yellow
Remove-Item -Path ".idea\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".idea\libraries" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "   ✓ Caches cleaned" -ForegroundColor Green
Write-Host ""

# Build project to ensure everything compiles
Write-Host "3. Building project..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug --quiet
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Build successful" -ForegroundColor Green
} else {
    Write-Host "   ✗ Build failed" -ForegroundColor Red
    exit 1
}
Write-Host ""

Write-Host "✅ All fixed! Now do this in Android Studio:" -ForegroundColor Green
Write-Host ""
Write-Host "   1. File → Invalidate Caches... → Invalidate and Restart" -ForegroundColor White
Write-Host "   2. Wait for restart and indexing to complete" -ForegroundColor White
Write-Host "   3. All red errors should disappear!" -ForegroundColor White
Write-Host ""
Write-Host "If errors persist:" -ForegroundColor Yellow
Write-Host "   - File → Sync Project with Gradle Files" -ForegroundColor White
Write-Host "   - Build → Rebuild Project" -ForegroundColor White
Write-Host ""
