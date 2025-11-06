# PowerShell script to validate SugboAid branding consistency
# This script checks for proper use of brand colors, typography, and naming

Write-Host "SugboAid Branding Validation Script" -ForegroundColor Green
Write-Host "===================================" -ForegroundColor Green

$errors = @()
$warnings = @()

# Check if required branding files exist
$requiredFiles = @(
    "app\src\main\res\values\colors.xml",
    "app\src\main\res\values\themes.xml",
    "app\src\main\res\values-night\themes.xml",
    "app\src\main\res\values\strings.xml",
    "app\src\main\res\values\brand_guidelines.xml",
    "app\src\main\AndroidManifest.xml",
    "BRANDING_GUIDELINES.md"
)

Write-Host "`nChecking required branding files..." -ForegroundColor Cyan
foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "✓ $file" -ForegroundColor Green
    } else {
        $errors += "Missing required file: $file"
        Write-Host "✗ $file" -ForegroundColor Red
    }
}

# Check app name in strings.xml
Write-Host "`nValidating app name..." -ForegroundColor Cyan
if (Test-Path "app\src\main\res\values\strings.xml") {
    $stringsContent = Get-Content "app\src\main\res\values\strings.xml" -Raw
    if ($stringsContent -match '<string name="app_name">SugboAid</string>') {
        Write-Host "✓ App name is correctly set to 'SugboAid'" -ForegroundColor Green
    } else {
        $errors += "App name is not set to 'SugboAid' in strings.xml"
        Write-Host "✗ App name is not correctly set" -ForegroundColor Red
    }
}

# Check primary brand colors
Write-Host "`nValidating brand colors..." -ForegroundColor Cyan
if (Test-Path "app\src\main\res\values\colors.xml") {
    $colorsContent = Get-Content "app\src\main\res\values\colors.xml" -Raw
    
    $brandColors = @{
        "primary_blue" = "#1E4C82"
        "primary_green" = "#2CB67D"
        "accent_yellow" = "#FDB813"
        "light_blue" = "#2563eb"
    }
    
    foreach ($colorName in $brandColors.Keys) {
        $expectedValue = $brandColors[$colorName]
        if ($colorsContent -match "<color name=`"$colorName`">$expectedValue</color>") {
            Write-Host "✓ $colorName is correctly defined as $expectedValue" -ForegroundColor Green
        } else {
            $errors += "Brand color $colorName is not correctly defined as $expectedValue"
            Write-Host "✗ $colorName is not correctly defined" -ForegroundColor Red
        }
    }
}

# Check app icon configuration
Write-Host "`nValidating app icon configuration..." -ForegroundColor Cyan
if (Test-Path "app\src\main\AndroidManifest.xml") {
    $manifestContent = Get-Content "app\src\main\AndroidManifest.xml" -Raw
    if ($manifestContent -match 'android:icon="@mipmap/ic_launcher"') {
        Write-Host "✓ App icon is correctly configured in AndroidManifest.xml" -ForegroundColor Green
    } else {
        $errors += "App icon is not correctly configured in AndroidManifest.xml"
        Write-Host "✗ App icon configuration is incorrect" -ForegroundColor Red
    }
}

# Check theme configuration
Write-Host "`nValidating theme configuration..." -ForegroundColor Cyan
if (Test-Path "app\src\main\res\values\themes.xml") {
    $themesContent = Get-Content "app\src\main\res\values\themes.xml" -Raw
    if ($themesContent -match '<style name="Theme.SugboAid"') {
        Write-Host "✓ SugboAid theme is properly defined" -ForegroundColor Green
    } else {
        $errors += "SugboAid theme is not properly defined in themes.xml"
        Write-Host "✗ SugboAid theme is not properly defined" -ForegroundColor Red
    }
    
    if ($themesContent -match '<item name="colorPrimary">@color/primary_blue</item>') {
        Write-Host "✓ Primary color is correctly set to primary_blue" -ForegroundColor Green
    } else {
        $warnings += "Primary color may not be set to primary_blue in theme"
        Write-Host "⚠ Primary color configuration needs review" -ForegroundColor Yellow
    }
}

# Check dark theme configuration
Write-Host "`nValidating dark theme configuration..." -ForegroundColor Cyan
if (Test-Path "app\src\main\res\values-night\themes.xml") {
    Write-Host "✓ Dark theme file exists" -ForegroundColor Green
} else {
    $warnings += "Dark theme file is missing"
    Write-Host "⚠ Dark theme file is missing" -ForegroundColor Yellow
}

# Check mipmap directories
Write-Host "`nValidating mipmap directories..." -ForegroundColor Cyan
$mipmapDensities = @("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")
foreach ($density in $mipmapDensities) {
    $mipmapDir = "app\src\main\res\mipmap-$density"
    if (Test-Path $mipmapDir) {
        Write-Host "✓ Mipmap directory exists: $density" -ForegroundColor Green
    } else {
        $warnings += "Mipmap directory missing: $density"
        Write-Host "⚠ Mipmap directory missing: $density" -ForegroundColor Yellow
    }
}

# Check adaptive icon configuration
Write-Host "`nValidating adaptive icon configuration..." -ForegroundColor Cyan
$adaptiveIconFiles = @(
    "app\src\main\res\mipmap-anydpi-v26\ic_launcher.xml",
    "app\src\main\res\mipmap-anydpi-v26\ic_launcher_round.xml"
)

foreach ($file in $adaptiveIconFiles) {
    if (Test-Path $file) {
        Write-Host "✓ Adaptive icon file exists: $(Split-Path $file -Leaf)" -ForegroundColor Green
    } else {
        $warnings += "Adaptive icon file missing: $file"
        Write-Host "⚠ Adaptive icon file missing: $(Split-Path $file -Leaf)" -ForegroundColor Yellow
    }
}

# Summary
Write-Host "`n" + "="*50 -ForegroundColor Green
Write-Host "BRANDING VALIDATION SUMMARY" -ForegroundColor Green
Write-Host "="*50 -ForegroundColor Green

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✅ All branding checks passed!" -ForegroundColor Green
    Write-Host "SugboAid branding is properly implemented." -ForegroundColor Green
} else {
    if ($errors.Count -gt 0) {
        Write-Host "`n❌ ERRORS FOUND ($($errors.Count)):" -ForegroundColor Red
        foreach ($error in $errors) {
            Write-Host "  • $error" -ForegroundColor Red
        }
    }
    
    if ($warnings.Count -gt 0) {
        Write-Host "`n⚠️  WARNINGS ($($warnings.Count)):" -ForegroundColor Yellow
        foreach ($warning in $warnings) {
            Write-Host "  • $warning" -ForegroundColor Yellow
        }
    }
}

Write-Host "`nNext Steps:" -ForegroundColor Cyan
Write-Host "1. Run generate_icons.ps1 to create app icons from SugboAid logo" -ForegroundColor White
Write-Host "2. Test the app in both light and dark modes" -ForegroundColor White
Write-Host "3. Verify color contrast ratios for accessibility" -ForegroundColor White
Write-Host "4. Review BRANDING_GUIDELINES.md for detailed guidelines" -ForegroundColor White

Write-Host "`nBranding validation completed!" -ForegroundColor Green