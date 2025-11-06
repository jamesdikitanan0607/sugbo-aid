import { motion } from "motion/react";
import { ArrowLeft, Package, TrendingUp, TrendingDown, AlertTriangle, QrCode, Search } from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";

interface InventoryTrackerProps {
  onBack: () => void;
}

export function InventoryTracker({ onBack }: InventoryTrackerProps) {
  const inventory = [
    {
      name: "Rice Sacks",
      stock: 450,
      capacity: 500,
      unit: "sacks",
      status: "healthy",
      trend: "up",
      icon: "🌾",
      color: "from-[#2CB67D] to-[#10b981]",
    },
    {
      name: "Bottled Water",
      stock: 280,
      capacity: 1000,
      unit: "bottles",
      status: "low",
      trend: "down",
      icon: "💧",
      color: "from-[#FDB813] to-[#f59e0b]",
    },
    {
      name: "Medical Supplies",
      stock: 85,
      capacity: 100,
      unit: "boxes",
      status: "healthy",
      trend: "up",
      icon: "💊",
      color: "from-[#1E4C82] to-[#2563eb]",
    },
    {
      name: "Blankets",
      stock: 45,
      capacity: 300,
      unit: "pieces",
      status: "critical",
      trend: "down",
      icon: "🛏️",
      color: "from-red-500 to-orange-500",
    },
    {
      name: "Canned Goods",
      stock: 520,
      capacity: 600,
      unit: "cans",
      status: "healthy",
      trend: "up",
      icon: "🥫",
      color: "from-[#2CB67D] to-[#10b981]",
    },
    {
      name: "Hygiene Kits",
      stock: 150,
      capacity: 200,
      unit: "kits",
      status: "moderate",
      trend: "up",
      icon: "🧼",
      color: "from-purple-500 to-pink-500",
    },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "healthy":
        return "text-[#2CB67D]";
      case "moderate":
        return "text-[#FDB813]";
      case "low":
        return "text-orange-500";
      case "critical":
        return "text-red-500";
      default:
        return "text-slate-500";
    }
  };

  const getStatusBg = (status: string) => {
    switch (status) {
      case "healthy":
        return "bg-[#2CB67D]/10 border-[#2CB67D]/30";
      case "moderate":
        return "bg-[#FDB813]/10 border-[#FDB813]/30";
      case "low":
        return "bg-orange-500/10 border-orange-500/30";
      case "critical":
        return "bg-red-500/10 border-red-500/30";
      default:
        return "bg-slate-500/10 border-slate-500/30";
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-emerald-50 dark:from-slate-900 dark:via-blue-950 dark:to-emerald-950">
      {/* Blurred Map Background */}
      <div className="fixed inset-0 opacity-5 pointer-events-none">
        <svg className="w-full h-full" viewBox="0 0 100 100">
          <circle cx="30" cy="30" r="20" fill="currentColor" className="text-[#1E4C82]" />
          <circle cx="70" cy="50" r="15" fill="currentColor" className="text-[#2CB67D]" />
          <circle cx="50" cy="70" r="18" fill="currentColor" className="text-[#FDB813]" />
        </svg>
      </div>

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
            <h2 className="text-xl">Inventory Tracker</h2>
            <p className="text-sm text-slate-600 dark:text-slate-400">Real-time stock monitoring</p>
          </div>
          <Button
            size="icon"
            className="rounded-full bg-gradient-to-br from-[#1E4C82] to-[#2563eb]"
          >
            <QrCode className="w-5 h-5" />
          </Button>
        </div>
      </div>

      <div className="p-4 pb-24">
        {/* Search Bar */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6"
        >
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
            <Input
              placeholder="Search inventory..."
              className="pl-12 h-12 bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-xl"
            />
          </div>
        </motion.div>

        {/* Summary Cards */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="grid grid-cols-3 gap-3 mb-6"
        >
          {[
            { label: "Total Items", value: "1,530", color: "from-[#1E4C82] to-[#2563eb]" },
            { label: "Categories", value: "6", color: "from-[#2CB67D] to-[#10b981]" },
            { label: "Low Stock", value: "2", color: "from-orange-500 to-red-500" },
          ].map((stat, index) => (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.2 + index * 0.05 }}
              className={`bg-gradient-to-br ${stat.color} rounded-2xl p-4 text-white shadow-lg`}
            >
              <p className="text-xs opacity-90 mb-1">{stat.label}</p>
              <p className="text-2xl">{stat.value}</p>
            </motion.div>
          ))}
        </motion.div>

        {/* Inventory List */}
        <div className="space-y-4">
          {inventory.map((item, index) => {
            const percentage = (item.stock / item.capacity) * 100;
            return (
              <motion.div
                key={item.name}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.3 + index * 0.05 }}
                className="group"
              >
                <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-5 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-[1.02]">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3 flex-1">
                      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${item.color} flex items-center justify-center text-2xl shadow-md`}>
                        {item.icon}
                      </div>
                      <div className="flex-1">
                        <h3 className="mb-1">{item.name}</h3>
                        <p className="text-sm text-slate-600 dark:text-slate-400">
                          {item.stock} / {item.capacity} {item.unit}
                        </p>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                      <Badge
                        className={`${getStatusBg(item.status)} ${getStatusColor(item.status)} capitalize rounded-full border`}
                      >
                        {item.status}
                      </Badge>
                      {item.trend === "up" ? (
                        <div className="flex items-center gap-1 text-[#2CB67D] text-sm">
                          <TrendingUp className="w-4 h-4" />
                          <span>+12%</span>
                        </div>
                      ) : (
                        <div className="flex items-center gap-1 text-orange-500 text-sm">
                          <TrendingDown className="w-4 h-4" />
                          <span>-8%</span>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm text-slate-600 dark:text-slate-400">
                      <span>Stock Level</span>
                      <span>{percentage.toFixed(0)}%</span>
                    </div>
                    <div className="relative h-2 bg-slate-200 dark:bg-slate-700 rounded-full overflow-hidden">
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${percentage}%` }}
                        transition={{ delay: 0.5 + index * 0.05, duration: 0.8, ease: "easeOut" }}
                        className={`absolute inset-y-0 left-0 bg-gradient-to-r ${item.color} rounded-full`}
                      />
                    </div>
                  </div>

                  {/* Alert for Low Stock */}
                  {(item.status === "low" || item.status === "critical") && (
                    <motion.div
                      initial={{ opacity: 0, height: 0 }}
                      animate={{ opacity: 1, height: "auto" }}
                      transition={{ delay: 0.7 + index * 0.05 }}
                      className="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700"
                    >
                      <div className="flex items-center gap-2 text-sm text-orange-600 dark:text-orange-400">
                        <AlertTriangle className="w-4 h-4" />
                        <span>Restock needed soon</span>
                      </div>
                    </motion.div>
                  )}
                </div>
              </motion.div>
            );
          })}
        </div>

        {/* QR Scanner Button */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.8 }}
          className="mt-6"
        >
          <Button className="w-full h-16 bg-gradient-to-r from-[#1E4C82] to-[#2CB67D] hover:shadow-2xl transition-all duration-300 hover:scale-105 rounded-2xl">
            <QrCode className="w-5 h-5 mr-2" />
            Scan QR to Update Stock
          </Button>
        </motion.div>
      </div>
    </div>
  );
}
