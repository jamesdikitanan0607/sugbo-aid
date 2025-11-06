import { motion, AnimatePresence } from "motion/react";
import { ArrowLeft, Heart, Package, TrendingUp, AlertCircle, X } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { useState } from "react";

interface NotificationsProps {
  onBack: () => void;
}

export function Notifications({ onBack }: NotificationsProps) {
  const [notifications, setNotifications] = useState([
    {
      id: 1,
      type: "donation",
      title: "New Donation Received",
      message: "Juan Dela Cruz donated ₱5,000 for Earthquake Relief",
      time: "2 min ago",
      read: false,
      icon: Heart,
      color: "from-[#1E4C82] to-[#2563eb]",
    },
    {
      id: 2,
      type: "inventory",
      title: "Low Stock Alert",
      message: "Blankets stock is running low (45/300 remaining)",
      time: "15 min ago",
      read: false,
      icon: AlertCircle,
      color: "from-orange-500 to-red-500",
    },
    {
      id: 3,
      type: "distribution",
      title: "Distribution Complete",
      message: "200 food packs distributed to Lahug families",
      time: "1 hour ago",
      read: true,
      icon: Package,
      color: "from-[#2CB67D] to-[#10b981]",
    },
    {
      id: 4,
      type: "milestone",
      title: "Milestone Achieved! 🎉",
      message: "Reached ₱2M in total donations",
      time: "3 hours ago",
      read: true,
      icon: TrendingUp,
      color: "from-[#FDB813] to-[#f59e0b]",
    },
    {
      id: 5,
      type: "donation",
      title: "In-Kind Donation",
      message: "Relief Org Cebu donated 200 packs of rice",
      time: "5 hours ago",
      read: true,
      icon: Heart,
      color: "from-[#1E4C82] to-[#2563eb]",
    },
  ]);

  const handleDismiss = (id: number) => {
    setNotifications(notifications.filter((n) => n.id !== id));
  };

  const handleMarkAsRead = (id: number) => {
    setNotifications(
      notifications.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-emerald-50 dark:from-slate-900 dark:via-blue-950 dark:to-emerald-950">
      {/* Header */}
      <div className="sticky top-0 z-20 bg-white/60 dark:bg-slate-900/60 backdrop-blur-xl border-b border-slate-200/50 dark:border-slate-700/50">
        <div className="p-4 flex items-center gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={onBack}
            className="rounded-full"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex-1">
            <h2 className="text-xl">Notifications</h2>
            <p className="text-sm text-slate-600 dark:text-slate-400">
              {unreadCount} unread
            </p>
          </div>
          {unreadCount > 0 && (
            <Button
              variant="ghost"
              onClick={() =>
                setNotifications(notifications.map((n) => ({ ...n, read: true })))
              }
              className="text-sm text-[#1E4C82] dark:text-[#3b82f6] hover:bg-[#1E4C82]/10"
            >
              Mark all read
            </Button>
          )}
        </div>
      </div>

      <div className="p-4 pb-24">
        {notifications.length === 0 ? (
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex flex-col items-center justify-center py-20"
          >
            <div className="w-20 h-20 bg-gradient-to-br from-slate-200 to-slate-300 dark:from-slate-700 dark:to-slate-600 rounded-full flex items-center justify-center mb-4">
              <AlertCircle className="w-10 h-10 text-slate-400 dark:text-slate-500" />
            </div>
            <p className="text-slate-600 dark:text-slate-400">No notifications</p>
          </motion.div>
        ) : (
          <div className="space-y-3">
            <AnimatePresence mode="popLayout">
              {notifications.map((notification, index) => (
                <motion.div
                  key={notification.id}
                  layout
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{
                    opacity: 0,
                    x: 100,
                    transition: { duration: 0.2 },
                  }}
                  transition={{ delay: index * 0.05 }}
                  onClick={() => handleMarkAsRead(notification.id)}
                  className="cursor-pointer"
                >
                  <div
                    className={`bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-4 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-[1.02] ${
                      !notification.read
                        ? "border-l-4 border-l-[#1E4C82] dark:border-l-[#3b82f6]"
                        : ""
                    }`}
                  >
                    <div className="flex items-start gap-3">
                      {/* Icon */}
                      <div
                        className={`w-12 h-12 rounded-xl bg-gradient-to-br ${notification.color} flex items-center justify-center flex-shrink-0 shadow-md`}
                      >
                        <notification.icon className="w-6 h-6 text-white" />
                      </div>

                      {/* Content */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between mb-1">
                          <h4 className="pr-2">{notification.title}</h4>
                          {!notification.read && (
                            <Badge className="bg-[#1E4C82]/10 text-[#1E4C82] dark:bg-[#3b82f6]/10 dark:text-[#3b82f6] border-0 rounded-full px-2 py-0.5 flex-shrink-0">
                              New
                            </Badge>
                          )}
                        </div>
                        <p className="text-sm text-slate-600 dark:text-slate-400 mb-2 line-clamp-2">
                          {notification.message}
                        </p>
                        <p className="text-xs text-slate-500 dark:text-slate-500">
                          {notification.time}
                        </p>
                      </div>

                      {/* Dismiss Button */}
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDismiss(notification.id);
                        }}
                        className="rounded-full w-8 h-8 flex-shrink-0 opacity-0 hover:opacity-100 transition-opacity"
                      >
                        <X className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </motion.div>
              ))}
            </AnimatePresence>
          </div>
        )}
      </div>

      {/* Floating Info */}
      {notifications.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50"
        >
          <div className="bg-white/80 dark:bg-slate-800/80 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-full px-6 py-3 shadow-xl">
            <p className="text-sm text-slate-600 dark:text-slate-400">
              Tap to mark as read, swipe to dismiss
            </p>
          </div>
        </motion.div>
      )}
    </div>
  );
}
