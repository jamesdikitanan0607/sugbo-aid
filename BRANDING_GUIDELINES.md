# SugboAid Android App - Branding Guidelines

## Overview

This document outlines the official SugboAid branding guidelines for the Android application to ensure consistent visual identity and user experience across all screens and components.

## Brand Identity

### App Name
- **Official Name**: SugboAid
- **Tagline**: "Together, We Rebuild Cebu"
- **Sub-tagline**: "Transparent, Real-Time, Locally-Built Relief"
- **Mission**: Empowering communities through transparent donation management

### Logo Usage
- **Primary Logo**: Located in `app/src/main/res/drawable/ic_launcher_foreground.xml`
- **App Icon**: Configured in `app/src/main/AndroidManifest.xml` as `@mipmap/ic_launcher`
- **Minimum Size**: 48dp for touch targets, 24dp for icons
- **Background**: Uses `@color/primary_blue` as defined in `ic_launcher_background.xml`

## Color Palette

### Primary Colors
```xml
<!-- Primary brand colors -->
<color name="primary_blue">#1E4C82</color>      <!-- Main brand color -->
<color name="light_blue">#2563eb</color>        <!-- Secondary blue -->
<color name="primary_green">#2CB67D</color>     <!-- Success/growth -->
<color name="accent_yellow">#FDB813</color>     <!-- Attention/warning -->
```

### Status Colors
```xml
<color name="success_green">#10b981</color>     <!-- Success states -->
<color name="warning_orange">#f59e0b</color>    <!-- Warning states -->
<color name="error_red">#ef4444</color>         <!-- Error states -->
```

### Glassmorphism Colors
```xml
<!-- Light theme glassmorphism -->
<color name="glass_white_60">#99FFFFFF</color>
<color name="glass_white_40">#66FFFFFF</color>
<color name="glass_white_20">#33FFFFFF</color>
<color name="glass_border">#33FFFFFF</color>

<!-- Dark theme glassmorphism -->
<color name="glass_dark_60">#99000000</color>
<color name="glass_dark_40">#66000000</color>
<color name="glass_dark_20">#33000000</color>
```

## Typography

### Font Families
- **Headlines**: `sans-serif-black` - Used for important headings and statistics
- **Titles**: `sans-serif-medium` - Used for section titles and labels
- **Body Text**: `sans-serif` - Used for regular content and descriptions
- **Captions**: `sans-serif` - Used for small text and metadata

### Text Styles
```xml
<!-- Statistics text styles -->
<style name="StatisticsTitle">
    <item name="android:textSize">14sp</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>

<style name="StatisticsValue">
    <item name="android:textSize">24sp</item>
    <item name="android:fontFamily">sans-serif-black</item>
    <item name="android:textColor">@color/primary_blue</item>
</style>
```

## Visual Design System

### Glassmorphism Effects
- **Background Transparency**: 20-40% opacity
- **Border**: 1dp with glass border color
- **Corner Radius**: 12-16dp for modern appearance
- **Elevation**: 4-8dp for cards, 6dp for FAB

### Gradients
- **Primary Gradient**: `primary_blue` to `light_blue`
- **Success Gradient**: `primary_green` to `success_green`
- **Angle**: 135° for diagonal gradients

### Spacing and Layout
- **Grid System**: 8dp base unit for consistent spacing
- **Card Padding**: 16dp internal padding
- **Button Height**: Minimum 48dp for accessibility
- **Icon Size**: 24dp standard, 48dp for large touch targets

## Component Styles

### Buttons
```xml
<!-- Gradient button style -->
<style name="GradientButton" parent="Widget.MaterialComponents.Button">
    <item name="android:textColor">@color/white</item>
    <item name="android:textAllCaps">false</item>
    <item name="cornerRadius">12dp</item>
    <item name="android:minHeight">48dp</item>
</style>

<!-- Role selection buttons -->
<style name="RoleButton" parent="Widget.MaterialComponents.Button">
    <item name="android:textColor">@color/white</item>
    <item name="android:fontFamily">sans-serif-medium</item>
    <item name="android:minHeight">56dp</item>
    <item name="cornerRadius">16dp</item>
</style>
```

### Cards
```xml
<!-- Glassmorphic card style -->
<style name="GlassmorphicCard" parent="Widget.MaterialComponents.CardView">
    <item name="cardBackgroundColor">?attr/colorGlassBackground</item>
    <item name="cardCornerRadius">16dp</item>
    <item name="cardElevation">8dp</item>
    <item name="strokeColor">?attr/colorGlassBorder</item>
    <item name="strokeWidth">1dp</item>
</style>
```

## Theme Implementation

### Light Theme
- **Primary**: `@color/primary_blue`
- **Secondary**: `@color/primary_green`
- **Background**: `@color/light_gray`
- **Surface**: `@color/white`
- **Text**: `@color/dark_gray`

### Dark Theme
- **Primary**: `@color/light_blue`
- **Secondary**: `@color/success_green`
- **Background**: `@color/dark_background`
- **Surface**: `@color/dark_surface`
- **Text**: `@color/white`

## Icon Guidelines

### App Icons
- **Densities Required**: mdpi (48px), hdpi (72px), xhdpi (96px), xxhdpi (144px), xxxhdpi (192px)
- **Format**: PNG with transparency support
- **Background**: SugboAid primary blue
- **Foreground**: SugboAid logo with proper contrast

### Vector Icons
- **Format**: Vector drawables (XML) preferred for scalability
- **Size**: 24dp standard, 48dp for primary actions
- **Color**: Use theme colors for automatic dark mode support
- **Style**: Outlined style with 2dp stroke width

## Accessibility

### Color Contrast
- **Minimum Ratio**: 4.5:1 for normal text
- **Large Text**: 3:1 for text 18sp+ or bold 14sp+
- **Interactive Elements**: Ensure sufficient contrast in both themes

### Content Descriptions
- All interactive elements must have meaningful content descriptions
- Use `@string/cd_*` resources for consistent descriptions
- Include context for screen readers

### Touch Targets
- **Minimum Size**: 48dp x 48dp for all interactive elements
- **Spacing**: Minimum 8dp between adjacent touch targets
- **Visual Feedback**: Ripple effects and state changes

## Implementation Files

### Key Resource Files
- **Colors**: `app/src/main/res/values/colors.xml`
- **Themes**: `app/src/main/res/values/themes.xml`
- **Dark Themes**: `app/src/main/res/values-night/themes.xml`
- **Strings**: `app/src/main/res/values/strings.xml`
- **Brand Guidelines**: `app/src/main/res/values/brand_guidelines.xml`

### Icon Generation
- **Script**: `generate_icons.ps1` - PowerShell script for generating app icons
- **Source**: `sugboaid logo.png` - Original logo file
- **Output**: Mipmap directories with appropriate densities

## Usage Examples

### Applying Brand Colors
```xml
<!-- Use theme attributes for automatic dark mode support -->
<TextView
    android:textColor="?attr/colorTextPrimary"
    android:background="?attr/colorSurfacePrimary" />

<!-- Use direct colors for brand-specific elements -->
<Button
    android:backgroundTint="@color/primary_blue"
    android:textColor="@color/white" />
```

### Glassmorphic Components
```xml
<androidx.cardview.widget.CardView
    style="@style/GlassmorphicCard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <!-- Card content -->
</androidx.cardview.widget.CardView>
```

## Quality Assurance

### Brand Compliance Checklist
- [ ] All colors use defined brand palette
- [ ] Typography follows established hierarchy
- [ ] Glassmorphism effects are consistent
- [ ] Icons maintain proper contrast and sizing
- [ ] Both light and dark themes are supported
- [ ] Accessibility guidelines are followed
- [ ] SugboAid branding is prominent and consistent

### Testing
- Test app in both light and dark modes
- Verify color contrast ratios
- Check icon clarity at different densities
- Validate accessibility with TalkBack
- Ensure consistent spacing and alignment

## Maintenance

### Updates
- Any brand color changes must be updated in both light and dark themes
- New components should follow established patterns
- Icon updates require regeneration for all densities
- Documentation should be updated with any changes

### Review Process
- All UI changes should be reviewed for brand compliance
- New components should be added to this documentation
- Regular audits to ensure consistency across the app