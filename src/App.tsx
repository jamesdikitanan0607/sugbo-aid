import { useState, useEffect } from "react";
import { SplashScreen } from "./components/SplashScreen";
import { LoginPage } from "./components/LoginPage";
import { SignupPage } from "./components/SignupPage";
import { Dashboard } from "./components/Dashboard";
import { POSDonation } from "./components/POSDonation";
import { InventoryTracker } from "./components/InventoryTracker";
import { TransparencyDashboard } from "./components/TransparencyDashboard";
import { ReportsHistory } from "./components/ReportsHistory";
import { Notifications } from "./components/Notifications";
import { OfflineBanner } from "./components/OfflineBanner";
import { Toaster } from "./components/ui/sonner";
import { useAuth, UserRole } from "./hooks/useAuth";

type Screen =
  | "splash"
  | "login"
  | "signup"
  | "dashboard"
  | "pos"
  | "inventory"
  | "transparency"
  | "reports"
  | "notifications";

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>("splash");
  const [selectedRole, setSelectedRole] = useState<UserRole>('Donor');
  const [isDark, setIsDark] = useState(false);
  const { isAuthenticated, isLoading } = useAuth();

  // Dark mode toggle
  useEffect(() => {
    if (isDark) {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  }, [isDark]);

  // Handle splash screen completion with role selection
  const handleSplashComplete = (role: UserRole) => {
    setSelectedRole(role);
    if (isAuthenticated) {
      setCurrentScreen("dashboard");
    } else if (role === 'Guest') {
      // Handle guest flow if needed
      setCurrentScreen("dashboard");
    } else {
      setCurrentScreen("signup");
    }
  };

  // Handle authentication state changes (e.g., logout)
  useEffect(() => {
    if (!isLoading && !isAuthenticated && currentScreen !== "splash" && currentScreen !== "login" && currentScreen !== "signup") {
      setCurrentScreen("login");
    }
  }, [isAuthenticated, isLoading, currentScreen]);

  const renderScreen = () => {
    switch (currentScreen) {
      case "splash":
        return <SplashScreen onComplete={handleSplashComplete} />;
      case "login":
        return (
          <LoginPage
            onNavigateToSignup={() => setCurrentScreen("signup")}
            onLoginSuccess={() => setCurrentScreen("dashboard")}
          />
        );
      case "signup":
        return (
          <SignupPage
            onNavigateToLogin={() => setCurrentScreen("login")}
            onSignupSuccess={() => setCurrentScreen("dashboard")}
            role={selectedRole}
          />
        );
      case "dashboard":
        return <Dashboard onNavigate={setCurrentScreen} />;
      case "pos":
        return <POSDonation onBack={() => setCurrentScreen("dashboard")} />;
      case "inventory":
        return <InventoryTracker onBack={() => setCurrentScreen("dashboard")} />;
      case "transparency":
        return <TransparencyDashboard onBack={() => setCurrentScreen("dashboard")} />;
      case "reports":
        return <ReportsHistory onBack={() => setCurrentScreen("dashboard")} />;
      case "notifications":
        return <Notifications onBack={() => setCurrentScreen("dashboard")} />;
      default:
        return <Dashboard onNavigate={setCurrentScreen} />;
    }
  };

  return (
    <div className="size-full">
      <OfflineBanner />
      {renderScreen()}
      <Toaster />
      
      {/* Dark Mode Toggle (Only on Dashboard) */}
      {currentScreen === "dashboard" && (
        <button
          onClick={() => setIsDark(!isDark)}
          className="fixed bottom-24 left-6 w-12 h-12 bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-full shadow-lg flex items-center justify-center z-50 hover:scale-110 transition-transform"
          aria-label="Toggle dark mode"
        >
          {isDark ? (
            <span className="text-xl">☀️</span>
          ) : (
            <span className="text-xl">🌙</span>
          )}
        </button>
      )}
    </div>
  );
}
