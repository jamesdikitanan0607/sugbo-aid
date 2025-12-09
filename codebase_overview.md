# SugboAid Codebase Overview

This document provides a clear, concise explanation of your project structure, file responsibilities, and the logical flow of the application. Your project is a **hybrid repository** containing both a **Web Application** (React/Vite) and a **Native Android Application** (Java).

---

## 🏗️ Project Architecture

*   **Root Directory (`/`)**: Contains the **Web Application**.
*   **`app/` Directory**: Contains the **Android Application**.

---

## 🌐 Web Application (React + Vite)
**Location**: `src/`

This is a Single Page Application (SPA) built with React. Access is controlled via `App.tsx` which acts as a manual router shifting between different "screens" (components).

### Key Files & Responsibilities

| File / Folder | Purpose & Responsibilities |
| :--- | :--- |
| **`src/main.tsx`** | **Entry Point**. It mounts the React application into the DOM (the web page). It wraps everything in `StrictMode`. |
| **`src/App.tsx`** | **Main Controller**. Acts as the "Brain" of the web app. <br>• Manages global state: `currentScreen`, `isDark` (theme), `selectedRole`. <br>• Handles "Navigation" by conditionally rendering components (e.g., if set to 'login', it shows `<LoginPage />`). <br>• Handles global logic like verifying if a user is logged in (`useAuth`). |
| **`src/index.css`** | **Global Styles**. Contains Tailwind CSS imports and global variables (colors, fonts). |
| **`src/components/`** | **UI Screens**. Each file here corresponds to a full "page" or a major widget in the app. |
| —— `SplashScreen.tsx` | First screen user sees. Selects "Role" (Donor/Volunteer) before proceeding. |
| —— `LoginPage.tsx` | Handles user sign-in. Calls `onLoginSuccess` to tell `App.tsx` to switch to Dashboard. |
| —— `SignupPage.tsx` | Handles account creation. |
| —— `Dashboard.tsx` | The main hub after logging in. Shows stats and navigation to other tools. |
| —— `POSDonation.tsx` | Point-of-Sale interface for processing donations. |
| —— `InventoryTracker.tsx` | Manages tracking of relief goods. |
| **`src/hooks/useAuth.tsx`** | **Logic Hook**. Manages authentication state (Is user logged in? Who are they?). Likely connects to a backend (Supabase/Firebase) to validate sessions. |

### 🔄 Web App "Step-by-Step" Flow
1.  **Launch**: Browser loads `index.html`, which runs `main.tsx`.
2.  **Initialization**: `main.tsx` renders `<App />`.
3.  **Router Start**: `App` component initializes with `currentScreen = "splash"`.
4.  **User Action**: User sees `SplashScreen`, clicks a role.
    *   `App.tsx` updates `currentScreen` to `"login"` (or `"signup"`).
5.  **Authentication**: User logs in on `LoginPage`. On success, it calls a callback that sets `currentScreen = "dashboard"`.
6.  **Main Usage**: `App` renders `<Dashboard />`. Navigation buttons inside Dashboard (like "Inventory") call `onNavigate`, which updates `currentScreen` in `App`, causing it to swap the visible component to `<InventoryTracker />`.

---

## 📱 Android Application (Java)
**Location**: `app/src/main/java/com/sugboaid/donation/`

This is a standard Native Android app using the **MVVM (Model-View-ViewModel)** architecture and **Jetpack Navigation**.

### Key Files & Responsibilities

#### 1. Activities (Containers)
| File | Purpose |
| :--- | :--- |
| **`SimpleSplashActivity.java`** | **Launcher**. The very first file executed by Android. It shows a branding screen and then immediately launches `MainActivity`. |
| **`MainActivity.java`** | **The Implementation Hub**. <br>• Hosts the `NavHostFragment` (which swaps screens). <br>• Manages the efficient `BottomNavigationView` tabs. <br>• Handles "Offline Mode" syncing logic (`OfflineQueueManager`). <br>• **Critical Logic**: It listens for `NavController` changes to hide/show the bottom bar (e.g., hides it on Login screen). |

#### 2. Fragments (The Screens)
| File | Purpose |
| :--- | :--- |
| **`DashboardFragment.java`** | Main home screen. Observes `DashboardViewModel` to update UI stats. |
| **`TransparencyMapFragment.java`** | Displays the map. Likely uses a WebView or Google Maps SDK to show donation transparency data geographically. |
| **`InventoryFragment.java`** | Shows lists of items. Interacts with `InventoryViewModel` to load/save data. |
| **`AdminDashboardFragment.java`** | Restricted screen for Admin users. |

#### 3. ViewModels (The Logic/Data)
*Note: These files detach the UI (Fragment) from the Data.*
| File | Purpose |
| :--- | :--- |
| **`DashboardViewModel.java`** | Holds data for the Dashboard (e.g., "Total Donations", "Recent Activity"). It survives screen rotations and fetches data from repositories. |
| **`TransparencyViewModel.java`** | Manages data for the transparency features (Map/Stories). |

#### 4. Utils (Helpers)
| File | Purpose |
| :--- | :--- |
| **`OfflineQueueManager.java`** | **Critical Feature**. If internet is lost, it queues actions (like "Add Donation"). When internet returns, `MainActivity` uses this to sync data. |
| **`DiagnosticLogger.java`** | Helps you debug by writing formatted logs to the console/file. |

### 🔄 Android App "Step-by-Step" Flow
1.  **Launch**: User taps icon -> Android Manifest reads `<intent-filter>` -> Starts `SimpleSplashActivity`.
2.  **Transition**: Splash screen finishes -> Starts `MainActivity`.
3.  **Setup**: `MainActivity.onCreate()` runs:
    *   Initializes `NavHostFragment` (The empty frame where screens go).
    *   Sets up `BottomNavigationView`.
    *   Checks for Internet/Offline actions.
4.  **Navigation**: The `NavHostFragment` loads the default start destination (usually `DashboardFragment` or `LoginFragment` depending on `navigation/nav_graph.xml`).
5.  **User Logic**:
    *   User clicks "Inventory" tab -> `MainActivity` tells `NavHost` to replace the current fragment with `InventoryFragment`.
    *   `InventoryFragment` starts -> asks `InventoryViewModel` for data.
    *   `InventoryViewModel` loads data (from Database or API) -> gives it to Fragment to show.

---

## 🧩 How They Connect

Since this is a hybrid repository, these two apps likely share:
1.  **Backend/API**: Both probably connect to the same server/database (e.g., Firebase, Supabase, or a custom REST API).
2.  **Design Tokens**: They share visual branding (colors, icons), likely enforced manually or via shared asset files.
3.  **Business Logic**: The logic in `InventoryViewModel.java` (Android) mirrors the logic in `InventoryTracker.tsx` (Web), ensuring consistency across platforms.

### 💡 Tips for Debugging
*   **Web Issue?** Look at `App.tsx` first. If navigation is broken, it's usually the `currentScreen` state.
*   **Android Issue?** Check `MainActivity.java`. If the app crashes on launch, check `initViews` or `setupNavigation`. If a screen is blank, check the corresponding `ViewModel`.
