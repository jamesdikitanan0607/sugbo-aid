# SugboAid Android Icon Generation Guide

## Current Status
The adaptive icon configuration has been updated to prevent cropping issues:
- Added missing `ic_launcher_background` color (white)
- Updated adaptive icon XML with `scaleType="fitCenter"`
- Added 20dp padding to foreground layer-list
- Created placeholder round icon assets

## Required Icon Sizes
To properly implement the icon assets, the following sizes should be generated from the source "sugboaid logo.png":

### Density-Specific Sizes
- **mdpi**: 48x48px → `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- **hdpi**: 72x72px → `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- **xhdpi**: 96x96px → `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- **xxhdpi**: 144x144px → `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- **xxxhdpi**: 192x192px → `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

### Round Icon Assets
Create identical copies for round icons:
- Copy each `ic_launcher.png` to `ic_launcher_round.png` in the same directory

## Recommended Source Preparation
1. Start with a 512x512px version of the SugboAid logo
2. Add 20% padding (approximately 102px on each side)
3. Ensure the logo is centered within the 512x512px canvas
4. Use this prepared source to generate all density variants

## Automated Generation
If ImageMagick is available, run:
```powershell
.\generate_icons.ps1
```

## Manual Generation
If ImageMagick is not available:
1. Use any image editing software (GIMP, Photoshop, etc.)
2. Resize the prepared 512x512px source to each required size
3. Save as PNG with transparency preserved
4. Place in the appropriate mipmap-* directories

## Verification
After generating new icons:
1. Clean and rebuild the project: `gradlew clean build`
2. Install on device/emulator
3. Check home screen and app drawer for proper icon display
4. Verify no cropping occurs on different launcher shapes