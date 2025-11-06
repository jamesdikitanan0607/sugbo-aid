import { motion } from "motion/react";
import { Heart, Package, Users, Plus, TrendingUp, MapPin, Bell, LogOut, User } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "./ui/alert-dialog";
import { useAuth } from "../hooks/useAuth";

interface DashboardProps {
  onNavigate: (screen: string) => void;
}

export function Dashboard({ onNavigate }: DashboardProps) {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    onNavigate("login");
  };
  const stats = [
    {
      title: "Total Donations",
      value: "₱2.4M",
      change: "+12.5%",
      icon: Heart,
      color: "from-[#1E4C82] to-[#2563eb]",
    },
    {
      title: "Distributed Items",
      value: "8,432",
      change: "+8.2%",
      icon: Package,
      color: "from-[#2CB67D] to-[#10b981]",
    },
    {
      title: "Families Helped",
      value: "1,247",
      change: "+15.3%",
      icon: Users,
      color: "from-[#FDB813] to-[#f59e0b]",
    },
  ];

  const recentActivities = [
    { donor: "Juan Dela Cruz", amount: "₱5,000", time: "5 min ago", type: "cash" },
    { donor: "Relief Org Cebu", amount: "200 packs rice", time: "12 min ago", type: "goods" },
    { donor: "Anonymous", amount: "₱10,000", time: "23 min ago", type: "cash" },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-emerald-50 dark:from-slate-900 dark:via-blue-950 dark:to-emerald-950">
      {/* Parallax Background */}
      <motion.div
        className="fixed inset-0 opacity-20 pointer-events-none"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1701705994021-b21330a838a0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjZWJ1JTIwcGhpbGlwcGluZXMlMjBza3lsaW5lfGVufDF8fHx8MTc1OTg5NTU4Mnww&ixlib=rb-4.1.0&q=80&w=1080')`,
          backgroundSize: "cover",
          backgroundPosition: "center",
        }}
        animate={{
          scale: [1, 1.05, 1],
        }}
        transition={{
          duration: 20,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />

      {/* Animated Waves at Bottom */}
      <div className="fixed bottom-0 left-0 right-0 h-48 overflow-hidden pointer-events-none">
        <motion.div
          className="absolute bottom-0 left-0 right-0 h-full opacity-10"
          style={{
            backgroundImage: `url('https://images.unsplash.com/photo-1621002478072-085fefaa529f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvY2VhbiUyMHdhdmVzJTIwc3Vuc2V0fGVufDF8fHx8MTc1OTg4MDUxMHww&ixlib=rb-4.1.0&q=80&w=1080')`,
            backgroundSize: "cover",
            backgroundPosition: "top",
          }}
          animate={{
            x: [0, -100, 0],
          }}
          transition={{
            duration: 15,
            repeat: Infinity,
            ease: "linear",
          }}
        />
      </div>

      {/* Content */}
      <div className="relative z-10 p-4 pb-24">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-3xl text-[#1E4C82] dark:text-white mb-1">SugboAid</h1>
            <p className="text-sm text-slate-600 dark:text-slate-400">Dashboard</p>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onNavigate("notifications")}
              className="relative rounded-full"
            >
              <Bell className="w-5 h-5" />
              <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />
            </Button>
            
            {/* User Menu */}
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon"
                  className="rounded-full bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50"
                >
                  <User className="w-5 h-5" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56 bg-white/90 dark:bg-slate-800/90 backdrop-blur-xl border border-white/20 dark:border-slate-700/50">
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm font-medium leading-none">{user?.name}</p>
                    <p className="text-xs leading-none text-muted-foreground">{user?.email}</p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <DropdownMenuItem onSelect={(e) => e.preventDefault()}>
                      <LogOut className="mr-2 h-4 w-4" />
                      <span>Log out</span>
                    </DropdownMenuItem>
                  </AlertDialogTrigger>
                  <AlertDialogContent className="bg-white/90 dark:bg-slate-800/90 backdrop-blur-xl border border-white/20 dark:border-slate-700/50">
                    <AlertDialogHeader>
                      <AlertDialogTitle>Confirm Logout</AlertDialogTitle>
                      <AlertDialogDescription>
                        Are you sure you want to log out? You will need to sign in again to access your dashboard.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Cancel</AlertDialogCancel>
                      <AlertDialogAction onClick={handleLogout} className="bg-gradient-to-r from-[#1E4C82] to-[#2CB67D]">
                        Log out
                      </AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 gap-4 mb-6">
          {stats.map((stat, index) => (
            <motion.div
              key={stat.title}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              className="group"
            >
              <div className="relative overflow-hidden rounded-3xl">
                {/* Glassmorphic Card */}
                <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 p-6 shadow-xl hover:shadow-2xl transition-all duration-300 group-hover:scale-[1.02]">
                  {/* Gradient Background */}
                  <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${stat.color} opacity-10 rounded-full blur-2xl`} />
                  
                  <div className="relative flex items-start justify-between">
                    <div className="flex-1">
                      <p className="text-sm text-slate-600 dark:text-slate-400 mb-2">{stat.title}</p>
                      <p className="text-4xl mb-2">{stat.value}</p>
                      <div className="flex items-center gap-2">
                        <TrendingUp className="w-4 h-4 text-[#2CB67D]" />
                        <span className="text-sm text-[#2CB67D]">{stat.change}</span>
                        <span className="text-xs text-slate-500 dark:text-slate-400">vs last week</span>
                      </div>
                    </div>
                    <div className={`bg-gradient-to-br ${stat.color} p-4 rounded-2xl shadow-lg`}>
                      <stat.icon className="w-8 h-8 text-white" />
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Quick Actions */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mb-6"
        >
          <h3 className="mb-4 text-slate-700 dark:text-slate-300">Quick Actions</h3>
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: "New Donation", screen: "pos", icon: Heart, gradient: "from-[#1E4C82] to-[#2563eb]" },
              { label: "Inventory", screen: "inventory", icon: Package, gradient: "from-[#2CB67D] to-[#10b981]" },
              { label: "Transparency", screen: "transparency", icon: MapPin, gradient: "from-[#FDB813] to-[#f59e0b]" },
              { label: "Reports", screen: "reports", icon: TrendingUp, gradient: "from-purple-500 to-pink-500" },
            ].map((action, index) => (
              <motion.div
                key={action.label}
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.4 + index * 0.05 }}
              >
                <Button
                  onClick={() => onNavigate(action.screen)}
                  className={`w-full h-28 bg-gradient-to-br ${action.gradient} hover:shadow-2xl transition-all duration-300 hover:scale-105 rounded-2xl flex flex-col items-center justify-center gap-2 border-0`}
                >
                  <action.icon className="w-8 h-8 text-white" />
                  <span className="text-white">{action.label}</span>
                </Button>
              </motion.div>
            ))}
          </div>
        </motion.div>

        {/* Recent Activity */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
        >
          <h3 className="mb-4 text-slate-700 dark:text-slate-300">Recent Activity</h3>
          <div className="space-y-3">
            {recentActivities.map((activity, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.7 + index * 0.1 }}
                className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 p-4 rounded-2xl shadow-lg"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="mb-1">{activity.donor}</p>
                    <p className="text-sm text-slate-600 dark:text-slate-400">{activity.amount}</p>
                  </div>
                  <div className="text-right">
                    <Badge
                      variant={activity.type === "cash" ? "default" : "secondary"}
                      className="mb-1 rounded-full"
                    >
                      {activity.type}
                    </Badge>
                    <p className="text-xs text-slate-500 dark:text-slate-400">{activity.time}</p>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Floating Action Button */}
      <motion.div
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ delay: 0.8, type: "spring", stiffness: 200 }}
        className="fixed bottom-6 right-6 z-50"
      >
        <Button
          onClick={() => onNavigate("pos")}
          size="lg"
          className="w-16 h-16 rounded-full bg-gradient-to-r from-[#1E4C82] to-[#2CB67D] hover:shadow-2xl shadow-lg transition-all duration-300 hover:scale-110 border-4 border-white/30 p-0"
        >
          <div className="relative">
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-[#FDB813] to-[#2CB67D] rounded-full blur-lg opacity-50"
              animate={{
                scale: [1, 1.2, 1],
                opacity: [0.5, 0.8, 0.5],
              }}
              transition={{
                duration: 2,
                repeat: Infinity,
                ease: "easeInOut",
              }}
            />
            <Plus className="w-8 h-8 text-white relative z-10" />
          </div>
        </Button>
      </motion.div>
    </div>
  );
}
