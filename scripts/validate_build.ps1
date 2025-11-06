# SugboAid Android Build Validation Script
# This script validates the built APK for release readiness

param(
    [Parameter(Mandatory=$false)]
    [string]$ApkPath = "app/build/outputs/apk/production/release/"
)

Write-Host "=== SugboAid Android Build Validation ===" -ForegroundColor Green

# Check if APK exists
$apkFiles = Get-ChildItem $ApkPath -Filter "*.apk" -ErrorAction SilentlyContinue
if (!$apkFiles) {
    Write-Error "No APK files found in $ApkPath"
    exit 1
}

$mainApk = $apkFiles | Where-Object { $_.Name -like "*production-release*.apk" } | Select-Object -First 1
if (!$mainApk) {
    $mainApk = $apkFiles[0]
}

Write-Host "Validating APK: $($mainApk.Name)" -ForegroundColor Yellow

# Check APK size
$apkSizeMB = [math]::Round($mainApk.Length / 1MB, 2)
Write-Host "APK Size: $apkSizeMB MB" -ForegroundColor Cyan

if ($apkSizeMB -gt 100) {
    Write-Warning "APK size is quite large ($apkSizeMB MB). Consider optimizing."
} elseif ($apkSizeMB -gt 50) {
    Write-Warning "APK size is moderate ($apkSizeMB MB). Monitor for future releases."
} else {
    Write-Host "APK size is good ($apkSizeMB MB)" -ForegroundColor Green
}

# Validate APK using aapt (if available)
$aaptPath = "${env:ANDROID_HOME}/build-tools/*/aapt.exe" | Get-ChildItem | Select-Object -Last 1
if ($aaptPath) {
    Write-Host "Running APK validation with aapt..." -ForegroundColor Yellow
    
    # Check APK structure
    $aaptOutput = & $aaptPath.FullName dump badging $mainApk.FullName 2>&1
    
    if ($aaptOutput -match "package: name='([^']+)'") {
        $packageName = $matches[1]
        Write-Host "Package Name: $packageName" -ForegroundColor Cyan
        
        if ($packageName -ne "com.sugboaid.donation") {
            Write-Warning "Package name doesn't match expected: com.sugboaid.donation"
        }
    }
    
    if ($aaptOutput -match "versionCode='([^']+)'") {
        $versionCode = $matches[1]
        Write-Host "Version Code: $versionCode" -ForegroundColor Cyan
    }
    
    if ($aaptOutput -match "versionName='([^']+)'") {
        $versionName = $matches[1]
        Write-Host "Version Name: $versionName" -ForegroundColor Cyan
    }
    
    # Check for required permissions
    $requiredPermissions = @(
        "android.permission.INTERNET",
        "android.permission.ACCESS_NETWORK_STATE",
        "android.permission.CAMERA"
    )
    
    foreach ($permission in $requiredPermissions) {
        if ($aaptOutput -match "uses-permission: name='$permission'") {
            Write-Host "✓ Permission found: $permission" -ForegroundColor Green
        } else {
            Write-Warning "✗ Missing permission: $permission"
        }
    }
    
    # Check for activities
    if ($aaptOutput -match "launchable-activity: name='([^']+)'") {
        $launcherActivity = $matches[1]
        Write-Host "Launcher Activity: $launcherActivity" -ForegroundColor Cyan
    }
    
} else {
    Write-Warning "aapt not found. Skipping detailed APK validation."
    Write-Host "To enable full validation, ensure Android SDK build-tools are installed."
}

# Check signing (basic check)
Write-Host "Checking APK signing..." -ForegroundColor Yellow
$jarsignerPath = "${env:JAVA_HOME}/bin/jarsigner.exe"
if (Test-Path $jarsignerPath) {
    $signingCheck = & $jarsignerPath -verify -verbose $mainApk.FullName 2>&1
    if ($signingCheck -match "jar verified") {
        Write-Host "✓ APK is properly signed" -ForegroundColor Green
    } else {
        Write-Warning "✗ APK signing verification failed"
    }
} else {
    Write-Warning "jarsigner not found. Skipping signing verification."
}

# Security checks
Write-Host "Running security checks..." -ForegroundColor Yellow

# Check for debug information
$debugCheck = Select-String -Path $mainApk.FullName -Pattern "BuildConfig" -Quiet 2>$null
if ($debugCheck) {
    Write-Warning "APK may contain debug information"
} else {
    Write-Host "✓ No obvious debug information found" -ForegroundColor Green
}

# Performance recommendations
Write-Host "Performance Recommendations:" -ForegroundColor Cyan
Write-Host "  - Test on low-end devices (API 21+)" -ForegroundColor White
Write-Host "  - Verify smooth animations and transitions" -ForegroundColor White
Write-Host "  - Test offline functionality" -ForegroundColor White
Write-Host "  - Verify memory usage under load" -ForegroundColor White
Write-Host "  - Test QR scanning in various lighting conditions" -ForegroundColor White

# Release checklist
Write-Host "Pre-Release Checklist:" -ForegroundColor Cyan
Write-Host "  □ All features tested manually" -ForegroundColor White
Write-Host "  □ Automated tests passing" -ForegroundColor White
Write-Host "  □ Performance tested on target devices" -ForegroundColor White
Write-Host "  □ Accessibility features verified" -ForegroundColor White
Write-Host "  □ Dark mode functionality tested" -ForegroundColor White
Write-Host "  □ Offline mode functionality verified" -ForegroundColor White
Write-Host "  □ Data persistence tested" -ForegroundColor White
Write-Host "  □ QR code generation and scanning tested" -ForegroundColor White
Write-Host "  □ Export functionality (PDF/CSV) tested" -ForegroundColor White
Write-Host "  □ Charts and transparency dashboard verified" -ForegroundColor White

Write-Host "=== Build Validation Complete ===" -ForegroundColor Green
Write-Host "APK: $($mainApk.FullName)" -ForegroundColor White
Write-Host "Ready for distribution: Review checklist above" -ForegroundColor Yellow