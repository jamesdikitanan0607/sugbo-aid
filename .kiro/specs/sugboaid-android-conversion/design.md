# Design Document

## Overview

This document outlines the technical design for converting the SugboAid TypeScript React donation management application into a native Android application using Java, Groovy DSL, and targeting Android API level 29. The design maintains all existing functionality while leveraging Android's native capabilities for optimal performance and user experience.

## Architecture

### MVVM Architecture Pattern

The application will follow the Model-View-ViewModel (MVVM) architecture pattern to ensure clean separation of concerns and maintainability:

- **Model Layer**: Data classes, repositories, and SharedPreferences management
- **View Layer**: Activities, Fragments, and XML layouts
- **ViewModel Layer**: Business logic, state management, and LiveData observables

### Project Structure

```
app/
├── src/main/
│   ├── java/com/sugboaid/
│   │   ├── activities/
│   │   │   ├── MainActivity.java
│   │   │   ├── SplashActivity.java
│   │   │   └── BaseActivity.java
│   │   ├── fragments/
│   │   │   ├── DashboardFragment.java
│   │   │   ├── POSDonationFragment.java
│   │   │   ├── InventoryFragment.java
│   │   │   ├── TransparencyFragment.java
│   │   │   ├── ReportsFragment.java
│   │   │   └── NotificationsFragment.java
│   │   ├── viewmodels/
│   │   │   ├── DashboardViewModel.java
│   │   │   ├── DonationViewModel.java
│   │   │   ├── InventoryViewModel.java
│   │   │   └── SharedViewModel.java
│   │   ├── models/
│   │   │   ├── Donation.java
│   │   │   ├── InventoryItem.java
│   │   │   ├── Transaction.java
│   │   │   └── Notification.java
│   │   ├── repositories/
│   │   │   ├── DonationRepository.java
│   │   │   ├── InventoryRepository.java
│   │   │   └── PreferencesRepository.java
│   │   ├── utils/
│   │   │   ├── SharedPreferencesHelper.java
│   │   │   ├── AnimationUtils.java
│   │   │   ├── ThemeUtils.java
│   │   │   └── NetworkUtils.java
│   │   └── adapters/
│   │       ├── DonationAdapter.java
│   │       ├── InventoryAdapter.java
│   │       └── NotificationAdapter.java
│   ├── res/
│   │   ├── layout/
│   │   ├── values/
│   │   ├── drawable/
│   │   ├── mipmap/
│   │   └── anim/
│   └── AndroidManifest.xml
└── build.gradle
```

## Components and Interfaces

### Core Activities

#### SplashActivity
- **Purpose**: Initial loading screen with role selection
- **Features**: 
  - Animated SugboAid logo entrance
  - Role selection buttons (Donor, Organization, Volunteer, Recipient, Guest)
  - Parallax background animation using Android Property Animation API
  - Automatic navigation to MainActivity after role selection

#### MainActivity
- **Purpose**: Main container activity using Navigation Component
- **Features**:
  - Bottom navigation or drawer navigation
  - Fragment container for different screens
  - Dark mode toggle implementation
  - Offline banner management

### Fragment Components

#### DashboardFragment
- **UI Elements**:
  - Statistics cards with gradient backgrounds using GradientDrawable
  - Quick action buttons with custom animations
  - Recent activities RecyclerView
  - Floating Action Button for quick donation
- **Animations**: Property animations for card entrance and button interactions

#### POSDonationFragment
- **UI Elements**:
  - Toggle buttons for cash/goods selection using ToggleButton
  - Amount input with custom number keyboard
  - Quick amount selection grid using GridLayout
  - Goods quantity selectors with increment/decrement buttons
  - Success screen with confetti animation using custom View

#### InventoryFragment
- **UI Elements**:
  - Search functionality using SearchView
  - Inventory items RecyclerView with custom ViewHolder
  - Progress bars for stock levels using ProgressBar
  - Status badges using custom drawable shapes
  - QR scanner integration using ZXing library

#### TransparencyFragment
- **UI Elements**:
  - Tab layout for different views using TabLayout
  - Chart integration using MPAndroidChart library
  - Interactive map using Google Maps API or custom MapView
  - Barangay list with location markers

#### ReportsFragment
- **UI Elements**:
  - Filter buttons using Chip components
  - Transaction list RecyclerView
  - Export functionality using Android's sharing intent
  - Date range picker using DatePickerDialog

#### NotificationsFragment
- **UI Elements**:
  - Notification list RecyclerView with swipe-to-dismiss
  - Mark as read functionality
  - Empty state view
  - Notification badges

### Custom Views and Components

#### GlassmorphicCardView
- **Purpose**: Replicate glassmorphism effects from React app
- **Implementation**: Custom View extending CardView with blur effects using RenderScript

#### AnimatedGradientButton
- **Purpose**: Buttons with animated gradient backgrounds
- **Implementation**: Custom Button with AnimationDrawable for gradient transitions

#### StatisticsCard
- **Purpose**: Dashboard statistics display
- **Implementation**: Custom ViewGroup with animated number counting

## Data Models

### Core Data Classes

```java
public class Donation {
    private String id;
    private String donorName;
    private DonationType type; // CASH, GOODS
    private double amount;
    private String description;
    private long timestamp;
    private String campaign;
    private boolean verified;
    // Getters, setters, constructors
}

public class InventoryItem {
    private String name;
    private int stock;
    private int capacity;
    private String unit;
    private InventoryStatus status; // HEALTHY, MODERATE, LOW, CRITICAL
    private String iconEmoji;
    private String colorGradient;
    // Getters, setters, constructors
}

public class Transaction {
    private String id;
    private String donor;
    private TransactionType type;
    private String amount;
    private Date date;
    private String campaign;
    private boolean verified;
    // Getters, setters, constructors
}

public class AppNotification {
    private int id;
    private NotificationType type;
    private String title;
    private String message;
    private long timestamp;
    private boolean read;
    private String iconResource;
    private String colorGradient;
    // Getters, setters, constructors
}
```

### Repository Pattern

#### SharedPreferencesHelper
```java
public class SharedPreferencesHelper {
    private static final String PREF_NAME = "SugboAidPrefs";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_DONATIONS = "donations_json";
    
    public void saveDarkModePreference(boolean isDarkMode);
    public boolean getDarkModePreference();
    public void saveDonations(List<Donation> donations);
    public List<Donation> getDonations();
    // Additional methods for all data persistence
}
```

#### DonationRepository
```java
public class DonationRepository {
    private SharedPreferencesHelper prefsHelper;
    private MutableLiveData<List<Donation>> donationsLiveData;
    
    public LiveData<List<Donation>> getDonations();
    public void addDonation(Donation donation);
    public void updateDonation(Donation donation);
    public LiveData<Double> getTotalDonations();
    public LiveData<Integer> getTotalFamiliesHelped();
}
```

## Error Handling

### Network Connectivity
- **Offline Detection**: NetworkCallback implementation to monitor connectivity
- **Offline Banner**: Custom View that appears when network is unavailable
- **Data Synchronization**: Queue offline actions for sync when connectivity returns

### Data Validation
- **Input Validation**: Custom validators for donation amounts and form inputs
- **Error States**: Consistent error messaging using Snackbar and Toast
- **Graceful Degradation**: App continues to function with cached data when services are unavailable

### Exception Handling
- **Global Exception Handler**: UncaughtExceptionHandler for crash reporting
- **Try-Catch Blocks**: Comprehensive error handling in all data operations
- **User Feedback**: Clear error messages with actionable solutions

## Testing Strategy

### Unit Testing
- **ViewModels**: Test business logic and LiveData transformations
- **Repositories**: Test data operations and SharedPreferences interactions
- **Utilities**: Test helper classes and utility functions
- **Framework**: JUnit 4 with Mockito for mocking dependencies

### UI Testing
- **Espresso Tests**: Test user interactions and navigation flows
- **Fragment Testing**: Test individual fragment functionality
- **Integration Tests**: Test complete user workflows
- **Accessibility Testing**: Ensure proper content descriptions and navigation

### Performance Testing
- **Memory Leaks**: LeakCanary integration for memory leak detection
- **Animation Performance**: Profile animation smoothness and frame rates
- **Data Loading**: Test large dataset handling and pagination

## Theme and Styling System

### Color Scheme
```xml
<!-- colors.xml -->
<color name="primary_blue">#1E4C82</color>
<color name="primary_green">#2CB67D</color>
<color name="accent_yellow">#FDB813</color>
<color name="light_blue">#2563eb</color>
<color name="success_green">#10b981</color>
<color name="warning_orange">#f59e0b</color>
<color name="error_red">#ef4444</color>

<!-- Glassmorphism colors -->
<color name="glass_white_60">#99FFFFFF</color>
<color name="glass_white_20">#33FFFFFF</color>
<color name="glass_dark_60">#99000000</color>
<color name="glass_border">#33FFFFFF</color>
```

### Gradient Definitions
```xml
<!-- gradients.xml -->
<gradient name="primary_gradient">
    <item android:color="@color/primary_blue" android:offset="0.0" />
    <item android:color="@color/light_blue" android:offset="1.0" />
</gradient>

<gradient name="success_gradient">
    <item android:color="@color/primary_green" android:offset="0.0" />
    <item android="@color/success_green" android:offset="1.0" />
</gradient>
```

### Dark Mode Support
- **Theme Variants**: Separate themes for light and dark modes
- **Dynamic Colors**: Use theme attributes for consistent color application
- **System Integration**: Follow system dark mode settings with manual override

## Animation Framework

### Property Animations
- **Entrance Animations**: Slide, fade, and scale animations for screen transitions
- **Micro-interactions**: Button press feedback, card hover effects
- **Loading States**: Shimmer effects and progress indicators

### Custom Animations
- **Confetti Animation**: Custom View with particle system for donation success
- **Gradient Animation**: Animated gradient backgrounds using ValueAnimator
- **Chart Animations**: Smooth data visualization transitions

### Performance Optimization
- **Hardware Acceleration**: Enable for smooth animations
- **Animation Duration**: Consistent timing following Material Design guidelines
- **Memory Management**: Proper cleanup of animation resources

## Integration Points

### External Libraries
- **MPAndroidChart**: For transparency dashboard charts and graphs
- **ZXing**: QR code scanning for inventory management
- **Gson**: JSON serialization for SharedPreferences data storage
- **Glide**: Image loading and caching (if needed for future features)

### Android System Integration
- **Notifications**: Local notifications for app alerts and updates
- **Sharing**: Android sharing intent for report exports
- **Accessibility**: TalkBack support and content descriptions
- **Permissions**: Camera permission for QR scanning

### Build Configuration
```groovy
// build.gradle (app level)
android {
    compileSdkVersion 29
    defaultConfig {
        applicationId "com.sugboaid.donation"
        minSdkVersion 21
        targetSdkVersion 29
        versionCode 1
        versionName "1.0"
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.3.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.4.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.4.0'
    implementation 'androidx.navigation:navigation-fragment:2.3.5'
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.google.code.gson:gson:2.8.8'
    
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:3.12.4'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
}
```

This design ensures a faithful conversion of the React TypeScript application to native Android while maintaining all functionality, visual fidelity, and user experience expectations. The architecture supports future enhancements and follows Android development best practices.