# PowerShell script to generate Android app icons from SugboAid logo
# This script requires ImageMagick to be installed

# Define icon sizes for different densities
$iconSizes = @{
    "mdpi" = 48
    "hdpi" = 72
    "xhdpi" = 96
    "xxhdpi" = 144
    "xxxhdpi" = 192
}

# Source logo file
$sourceLogo = "sugboaid logo.png"

# Check if source file exists
if (-not (Test-Path $sourceLogo)) {
    Write-Host "Error: Source logo file '$sourceLogo' not found!" -ForegroundColor Red
    Write-Host "Please ensure the SugboAid logo PNG file is in the current directory." -ForegroundColor Yellow
    exit 1
}

# Check if ImageMagick is installed
try {
    magick -version | Out-Null
} catch {
    Write-Host "Error: ImageMagick is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install ImageMagick from https://imagemagick.org/script/download.php#windows" -ForegroundColor Yellow
    exit 1
}

Write-Host "Generating Android app icons from SugboAid logo..." -ForegroundColor Green

# Create directories and generate icons
foreach ($density in $iconSizes.Keys) {
    $size = $iconSizes[$density]
    $outputDir = "app\src\main\res\mipmap-$density"
    
    # Create directory if it doesn't exist
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }
    
    # Generate ic_launcher.png
    $outputFile = "$outputDir\ic_launcher.png"
    Write-Host "Generating $outputFile (${size}x${size}px)..." -ForegroundColor Cyan
    
    # Use ImageMagick to resize and optimize the logo
    magick "$sourceLogo" -resize "${size}x${size}" -background transparent -gravity center -extent "${size}x${size}" "$outputFile"
    
    # Generate ic_launcher_round.png (same as regular for now)
    $roundOutputFile = "$outputDir\ic_launcher_round.png"
    Copy-Item $outputFile $roundOutputFile
    
    Write-Host "Generated icons for $density density" -ForegroundColor Green
}

Write-Host "`nIcon generation completed!" -ForegroundColor Green
Write-Host "Generated icons for densities: $($iconSizes.Keys -join ', ')" -ForegroundColor Yellow
Write-Host "`nNote: If you don't have ImageMagick installed, you can:" -ForegroundColor Cyan
Write-Host "1. Install ImageMagick from https://imagemagick.org/script/download.php#windows" -ForegroundColor White
Write-Host "2. Or manually resize the 'sugboaid logo.png' file to the following sizes:" -ForegroundColor White
foreach ($density in $iconSizes.Keys) {
    $size = $iconSizes[$density]
    Write-Host "   - $density`: ${size}x${size}px -> app\src\main\res\mipmap-$density\ic_launcher.png" -ForegroundColor White
}