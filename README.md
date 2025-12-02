# SugboAid - Disaster Relief & Donation Management App

A fully responsive Android application for offline disaster relief and donation management, built with Java and Groovy DSL targeting Android 10 (API 29).

## 🌟 Features

- **Offline-First Architecture**: Uses SharedPreferences with Gson for data persistence
- **Role-Based Access Control**: Supports Donor, Organization, Volunteer, Recipient, and Guest roles
- **Glassmorphic UI Design**: Modern, responsive interface with light/dark mode support
- **QR Code Integration**: Generate and scan QR codes for donation receipts
- **Responsive Design**: Adaptive layouts for phones and tablets
- **Dashboard Analytics**: Real-time statistics and insights
- **Inventory Management**: Track donations and supplies with low-stock alerts
- **Transparency Reports**: Public accountability and reporting

## 🏗️ Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Java
- **Build System**: Groovy DSL (Gradle)
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## 📱 Responsive Design

### Phone Layout
- Bottom Navigation for main sections
- Single-pane layouts
- Optimized touch targets (44dp minimum)

### Tablet Layout (sw600dp+)
- Navigation Drawer for better space utilization
- Two-pane layouts for master-detail views
- Larger text sizes and spacing
- Enhanced card layouts

## 🎨 Design System

### Color Palette
- **Primary**: Cebu Blue (#1E4C82)
- **Secondary**: Emerald Green (#2CB67D)
- **Accent**: Warm Yellow (#FDB813)
- **Background Light**: #F8FAFC
- **Background Dark**: #0F172A

### Typography
- **Font Family**: Inter/Roboto
- **Headline 1**: 28sp, Medium
- **Body Text**: 16sp, Regular
- **Line Height**: 1.5x

### Glassmorphic Effects
- Semi-transparent backgrounds with blur
- Subtle borders and shadows
- Smooth animations and transitions

## 🔐 User Roles & Permissions

| Role | Dashboard | Donations | Inventory | POS | Analytics |
|------|-----------|-----------|-----------|-----|-----------|
| **Donor** | ✅ | View Own | ❌ | ❌ | ❌ |
| **Organization** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Volunteer** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Recipient** | ✅ | View | ❌ | ❌ | ❌ |
| **Guest** | Limited | View Public | ❌ | ❌ | ❌ |

## 🛠️ Dependencies

### Core Libraries
- AndroidX AppCompat & Material Components
- ConstraintLayout & RecyclerView
- Navigation Component
- Lifecycle (ViewModel & LiveData)

### Data & Storage
- Gson for JSON serialization
- SharedPreferences for offline storage

### QR Code & Scanning
- ZXing Android Embedded
- Google ZXing Core

### Charts & Analytics
- MPAndroidChart

### Animations
- Lottie for complex animations
- MotionLayout for transitions

### PDF Generation
- iText7 Core

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or higher (JDK 21 recommended)
- Android SDK with API 34
- Gradle 8.5+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-repo/sugboaid-app.git
   cd sugboaid-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Sync Project**
   - Android Studio will automatically sync Gradle
   - If not, click "Sync Now" in the notification bar

4. **Build the Project**
   ```bash
   ./gradlew build
   ```

### Running the App

#### On Physical Device
1. Enable Developer Options and USB Debugging
2. Connect device via USB
3. Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

#### On Emulator
1. Create an AVD with API 29 (Android 10)
2. Start the emulator
3. Click "Run" in Android Studio

### Demo Login Credentials

The app includes demo accounts for testing:

| Role | Email | Password |
|------|-------|----------|
| **Donor** | donor@sugboaid.org | donor123 |
| **Organization** | org@sugboaid.org | org123 |
| **Volunteer** | volunteer@sugboaid.org | vol123 |
| **Guest** | guest@sugboaid.org | guest123 |

*Note: Any email/password combination will work for demo purposes*

## 📁 Project Structure

```
app/
├── src/main/java/com/sugboaid/app/
│   ├── data/
│   │   ├── model/          # Data models (User, Donation, etc.)
│   │   ├── repository/     # Data repository layer
│   │   └── SharedPrefHelper.java
│   ├── manager/            # Business logic managers
│   │   ├── AuthManager.java
│   │   ├── POSManager.java
│   │   ├── InventoryManager.java
│   │   └── QRCodeManager.java
│   ├── ui/
│   │   ├── activity/       # Activities
│   │   ├── fragment/       # Fragments
│   │   └── adapter/        # RecyclerView adapters
│   ├── util/               # Utility classes
│   └── SugboAidApplication.java
├── src/main/res/
│   ├── layout/             # XML layouts
│   ├── layout-sw600dp/     # Tablet layouts
│   ├── values/             # Default resources
│   ├── values-sw600dp/     # Tablet-specific resources
│   ├── drawable/           # Vector drawables & backgrounds
│   └── menu/               # Navigation menus
└── build.gradle            # Groovy DSL build configuration
```

## 🎯 Key Features Implementation

### Offline Data Persistence
```java
// SharedPreferences with Gson serialization
SharedPrefHelper helper = new SharedPrefHelper(context);
helper.saveObject("donations", donationsList);
List<Donation> donations = helper.getList("donations", type);
```

### Role-Based Access Control
```java
// Check user permissions
if (authManager.canAccessFeature(Constants.FEATURE_DONATION_POS)) {
    // Show donation POS interface
}
```

### Responsive Layouts
```xml
<!-- Phone layout: layout/activity_main.xml -->
<BottomNavigationView ... />

<!-- Tablet layout: layout-sw600dp/activity_main.xml -->
<NavigationView ... />
```

### Theme Switching
```java
// Apply theme based on user preference
ThemeUtils.setThemeMode(context, Constants.THEME_DARK);
ThemeUtils.applyTheme(context);
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- Business logic (Managers)
- Data persistence (SharedPreferences)
- UI navigation and interactions

## 🔧 Troubleshooting

### Common Build Issues

#### Gradle Sync Issues
If you encounter Gradle sync problems:
1. Clean the project: `Build > Clean Project`
2. Invalidate caches: `File > Invalidate Caches and Restart`
3. Check that you have the correct Android SDK and build tools installed
4. Ensure the namespace is properly set in app/build.gradle

#### Namespace Configuration
The project uses the modern namespace configuration:
```gradle
android {
    namespace 'com.sugboaid.app'
    // ... other configurations
}
```

#### Repository Configuration Errors
If you see repository-related errors:
1. Ensure you have internet connection for dependency downloads
2. Check that JitPack.io is accessible from your network
3. Try running with `--refresh-dependencies` flag

#### API Level Compatibility
The app targets API 34 (Android 14) but maintains backward compatibility to API 23. Ensure you have:
- Android SDK Platform 34 installed
- Android SDK Build-Tools 34.0.0 or later
- Google Play Services (for Material Design components)

#### Java/JDK Compatibility
- **Minimum**: JDK 17
- **Recommended**: JDK 21 (current system)
- **Gradle**: 8.5 (compatible with JDK 21)

### Build Commands
```bash
# Clean build
./gradlew clean

# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

#### XML Syntax Errors
If you encounter XML parsing errors:
1. Check for malformed XML in resource files
2. Ensure all XML files have proper opening/closing tags
3. Verify that there's only one root element per XML file
4. Check for special characters that need escaping

#### Recent Build Fixes Applied
- ✅ Fixed malformed XML in themes.xml (multiple root elements)
- ✅ Separated custom attributes into attrs.xml
- ✅ Removed deprecated package attribute from AndroidManifest.xml
- ✅ Created vector drawable launcher icons
- ✅ Updated Java 21 and Gradle 8.5 compatibility
- ✅ Fixed resource linking issues (removed undefined style references)
- ✅ Simplified themes.xml to use only existing resources

## 📊 Performance Considerations

- **Lazy Loading**: RecyclerView with ViewHolder pattern
- **Memory Management**: Proper lifecycle handling in fragments
- **Image Optimization**: Vector drawables for scalability
- **Database Queries**: Efficient SharedPreferences usage
- **UI Responsiveness**: Background thread operations

## 🔒 Security Features

- **Data Validation**: Input sanitization and validation
- **Permission Handling**: Runtime permission requests
- **Secure Storage**: Encrypted SharedPreferences (future enhancement)
- **Role Verification**: Server-side validation (future enhancement)

## 🌐 Localization Support

The app is designed for easy localization:
- String resources in `values/strings.xml`
- RTL layout support
- Date/time formatting based on locale
- Currency formatting (PHP by default)

## 🚧 Future Enhancements

- [ ] Network synchronization
- [ ] Push notifications
- [ ] Advanced analytics with charts
- [ ] PDF report generation
- [ ] Biometric authentication
- [ ] Multi-language support
- [ ] Real-time chat support
- [ ] Geolocation features
- [ ] Advanced QR code features
- [ ] Cloud backup integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Development Team**: SugboAid Development Team
- **Design**: Based on Figma reference design
- **Project Manager**: [Your Name]

## 📞 Support

For support and questions:
- Email: support@sugboaid.org
- GitHub Issues: [Create an issue](https://github.com/your-repo/sugboaid-app/issues)
- Documentation: [Wiki](https://github.com/your-repo/sugboaid-app/wiki)

---

**SugboAid** - Empowering communities through technology-driven disaster relief and donation management.