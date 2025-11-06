# SugboAid Android Release Preparation Script
# This script prepares the app for release by running all necessary checks and builds

param(
    [Parameter(Mandatory=$false)]
    [string]$VersionName,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests,
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipLint
)

Write-Host "=== SugboAid Android Release Preparation ===" -ForegroundColor Green

# Check if we're in the right directory
if (!(Test-Path "app/build.gradle")) {
    Write-Error "Please run this script from the project root directory"
    exit 1
}

# Set version name if provided
if ($VersionName) {
    Write-Host "Setting version name to $VersionName..." -ForegroundColor Yellow
    ./gradlew setVersionName -PversionName=$VersionName
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to set version name"
        exit 1
    }
}

# Clean project
Write-Host "Cleaning project..." -ForegroundColor Yellow
./gradlew clean
if ($LASTEXITCODE -ne 0) {
    Write-Error "Clean failed"
    exit 1
}

# Run lint checks (unless skipped)
if (!$SkipLint) {
    Write-Host "Running lint checks..." -ForegroundColor Yellow
    ./gradlew lintProductionRelease
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Lint checks failed, but continuing..."
    }
}

# Run tests (unless skipped)
if (!$SkipTests) {
    Write-Host "Running unit tests..." -ForegroundColor Yellow
    ./gradlew testProductionReleaseUnitTest
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Unit tests failed"
        exit 1
    }
    
    Write-Host "Running instrumentation tests..." -ForegroundColor Yellow
    ./gradlew connectedProductionDebugAndroidTest
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Instrumentation tests failed, but continuing..."
    }
}

# Build release APK
Write-Host "Building release APK..." -ForegroundColor Yellow
./gradlew createReleaseBuild
if ($LASTEXITCODE -ne 0) {
    Write-Error "Release build failed"
    exit 1
}

# Generate build info
$buildInfo = @{
    "buildTime" = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "gitCommit" = (git rev-parse --short HEAD 2>$null) -or "unknown"
    "gitBranch" = (git rev-parse --abbrev-ref HEAD 2>$null) -or "unknown"
    "buildMachine" = $env:COMPUTERNAME
    "buildUser" = $env:USERNAME
}

$buildInfoJson = $buildInfo | ConvertTo-Json -Depth 2
$buildInfoJson | Out-File -FilePath "app/build/outputs/apk/production/release/build-info.json" -Encoding UTF8

Write-Host "=== Release Preparation Complete ===" -ForegroundColor Green
Write-Host "Build Info:" -ForegroundColor Cyan
Write-Host $buildInfoJson

# Show APK location
$apkPath = "app/build/outputs/apk/production/release/"
if (Test-Path $apkPath) {
    Write-Host "Release APK location: $apkPath" -ForegroundColor Green
    Get-ChildItem $apkPath -Filter "*.apk" | ForEach-Object {
        $size = [math]::Round($_.Length / 1MB, 2)
        Write-Host "  - $($_.Name) ($size MB)" -ForegroundColor White
    }
}

Write-Host "Release preparation completed successfully!" -ForegroundColor Green