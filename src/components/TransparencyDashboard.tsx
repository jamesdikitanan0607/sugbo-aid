import { motion } from "motion/react";
import { ArrowLeft, MapPin, Heart, TrendingUp, Users } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { useState } from "react";

interface TransparencyDashboardProps {
  onBack: () => void;
}

export function TransparencyDashboard({ onBack }: TransparencyDashboardProps) {
  const [activeTab, setActiveTab] = useState<"overview" | "barangay" | "stories">("overview");

  const donationTrend = [
    { month: "Apr", amount: 450000 },
    { month: "May", amount: 680000 },
    { month: "Jun", amount: 920000 },
    { month: "Jul", amount: 1150000 },
    { month: "Aug", amount: 1480000 },
    { month: "Sep", amount: 1820000 },
    { month: "Oct", amount: 2400000 },
  ];

  const distributionData = [
    { category: "Food", amount: 850000 },
    { category: "Medical", amount: 420000 },
    { category: "Shelter", amount: 380000 },
    { category: "Education", amount: 220000 },
    { category: "Hygiene", amount: 530000 },
  ];

  const pieData = [
    { name: "Food & Water", value: 35, color: "#2CB67D" },
    { name: "Medical", value: 25, color: "#1E4C82" },
    { name: "Shelter", value: 20, color: "#FDB813" },
    { name: "Others", value: 20, color: "#60a5fa" },
  ];

  const barangays = [
    { name: "Lahug", families: 234, donations: "₱450K", status: "active", lat: 10.32, lng: 123.88 },
    { name: "Pardo", families: 189, donations: "₱380K", status: "active", lat: 10.28, lng: 123.87 },
    { name: "Guadalupe", families: 312, donations: "₱520K", status: "active", lat: 10.31, lng: 123.89 },
    { name: "Tisa", families: 156, donations: "₱290K", status: "pending", lat: 10.30, lng: 123.90 },
    { name: "Banilad", families: 198, donations: "₱410K", status: "active", lat: 10.33, lng: 123.91 },
  ];

  const stories = [
    {
      family: "Dela Cruz Family",
      barangay: "Lahug",
      story: "Received 2 weeks of food supplies and medical assistance after the earthquake damaged their home.",
      date: "Oct 5, 2025",
      image: "https://images.unsplash.com/photo-1660066522109-82af50d99488?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb21tdW5pdHklMjBoZWxwaW5nJTIwZGlzYXN0ZXJ8ZW58MXx8fHwxNzU5ODk1NTgyfDA&ixlib=rb-4.1.0&q=80&w=1080",
    },
    {
      family: "Santos Family",
      barangay: "Pardo",
      story: "Provided temporary shelter materials and hygiene kits for 5 family members.",
      date: "Oct 3, 2025",
      image: "https://images.unsplash.com/photo-1660066522109-82af50d99488?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb21tdW5pdHklMjBoZWxwaW5nJTIwZGlzYXN0ZXJ8ZW58MXx8fHwxNzU5ODk1NTgyfDA&ixlib=rb-4.1.0&q=80&w=1080",
    },
    {
      family: "Reyes Family",
      barangay: "Guadalupe",
      story: "Children received school supplies and the family got rebuilding materials for their damaged kitchen.",
      date: "Oct 1, 2025",
      image: "https://images.unsplash.com/photo-1660066522109-82af50d99488?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb21tdW5pdHklMjBoZWxwaW5nJTIwZGlzYXN0ZXJ8ZW58MXx8fHwxNzU5ODk1NTgyfDA&ixlib=rb-4.1.0&q=80&w=1080",
    },
  ];

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
          <div>
            <h2 className="text-xl">Transparency Dashboard</h2>
            <p className="text-sm text-slate-600 dark:text-slate-400">Real-time impact tracking</p>
          </div>
        </div>

        {/* Tabs */}
        <div className="px-4 pb-3 flex gap-2 overflow-x-auto">
          {[
            { id: "overview", label: "Overview", icon: TrendingUp },
            { id: "barangay", label: "Barangay Map", icon: MapPin },
            { id: "stories", label: "Impact Stories", icon: Heart },
          ].map((tab) => (
            <Button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`rounded-xl h-10 transition-all duration-300 ${
                activeTab === tab.id
                  ? "bg-gradient-to-r from-[#1E4C82] to-[#2CB67D] text-white shadow-lg"
                  : "bg-white/50 dark:bg-slate-800/50 text-slate-700 dark:text-slate-300 hover:bg-white/70"
              }`}
            >
              <tab.icon className="w-4 h-4 mr-2" />
              {tab.label}
            </Button>
          ))}
        </div>
      </div>

      <div className="p-4 pb-24">
        {activeTab === "overview" && (
          <motion.div
            key="overview"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-6"
          >
            {/* Stats Grid */}
            <div className="grid grid-cols-2 gap-3">
              {[
                { label: "Total Raised", value: "₱2.4M", icon: Heart, color: "from-[#1E4C82] to-[#2563eb]" },
                { label: "Families", value: "1,247", icon: Users, color: "from-[#2CB67D] to-[#10b981]" },
              ].map((stat, index) => (
                <motion.div
                  key={stat.label}
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: index * 0.1 }}
                  className={`bg-gradient-to-br ${stat.color} rounded-2xl p-4 text-white shadow-lg`}
                >
                  <stat.icon className="w-6 h-6 mb-2 opacity-80" />
                  <p className="text-xs opacity-90 mb-1">{stat.label}</p>
                  <p className="text-2xl">{stat.value}</p>
                </motion.div>
              ))}
            </div>

            {/* Donation Trend Chart */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-5 shadow-lg"
            >
              <h3 className="mb-4">Donation Trend</h3>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={donationTrend}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.2)" />
                  <XAxis dataKey="month" stroke="#64748b" style={{ fontSize: "12px" }} />
                  <YAxis stroke="#64748b" style={{ fontSize: "12px" }} tickFormatter={(value) => `₱${value / 1000}K`} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "rgba(255, 255, 255, 0.9)",
                      border: "1px solid rgba(148, 163, 184, 0.2)",
                      borderRadius: "12px",
                      padding: "8px 12px",
                    }}
                    formatter={(value: any) => [`₱${value.toLocaleString()}`, "Amount"]}
                  />
                  <Line
                    type="monotone"
                    dataKey="amount"
                    stroke="#1E4C82"
                    strokeWidth={3}
                    dot={{ fill: "#1E4C82", r: 4 }}
                    activeDot={{ r: 6 }}
                  />
                </LineChart>
              </ResponsiveContainer>
            </motion.div>

            {/* Distribution by Category */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
              className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-5 shadow-lg"
            >
              <h3 className="mb-4">Distribution by Category</h3>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={distributionData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(148, 163, 184, 0.2)" />
                  <XAxis dataKey="category" stroke="#64748b" style={{ fontSize: "11px" }} />
                  <YAxis stroke="#64748b" style={{ fontSize: "12px" }} tickFormatter={(value) => `₱${value / 1000}K`} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "rgba(255, 255, 255, 0.9)",
                      border: "1px solid rgba(148, 163, 184, 0.2)",
                      borderRadius: "12px",
                      padding: "8px 12px",
                    }}
                    formatter={(value: any) => [`₱${value.toLocaleString()}`, "Amount"]}
                  />
                  <Bar dataKey="amount" fill="url(#colorGradient)" radius={[8, 8, 0, 0]} />
                  <defs>
                    <linearGradient id="colorGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#2CB67D" />
                      <stop offset="100%" stopColor="#10b981" />
                    </linearGradient>
                  </defs>
                </BarChart>
              </ResponsiveContainer>
            </motion.div>

            {/* Pie Chart */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-5 shadow-lg"
            >
              <h3 className="mb-4">Distribution Breakdown</h3>
              <div className="flex items-center justify-center">
                <ResponsiveContainer width="100%" height={200}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={80}
                      paddingAngle={5}
                      dataKey="value"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              <div className="grid grid-cols-2 gap-2 mt-4">
                {pieData.map((item) => (
                  <div key={item.name} className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} />
                    <span className="text-sm text-slate-600 dark:text-slate-400">{item.name}</span>
                  </div>
                ))}
              </div>
            </motion.div>
          </motion.div>
        )}

        {activeTab === "barangay" && (
          <motion.div
            key="barangay"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-4"
          >
            {/* Interactive Map Placeholder */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-5 shadow-lg overflow-hidden"
            >
              <h3 className="mb-4">Barangay Distribution Map</h3>
              <div className="relative h-64 bg-gradient-to-br from-blue-100 to-emerald-100 dark:from-blue-900/20 dark:to-emerald-900/20 rounded-xl overflow-hidden">
                {barangays.map((barangay, index) => (
                  <motion.div
                    key={barangay.name}
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ delay: 0.2 + index * 0.1, type: "spring" }}
                    className="absolute"
                    style={{
                      left: `${((barangay.lng - 123.85) / 0.1) * 100}%`,
                      top: `${((10.35 - barangay.lat) / 0.1) * 100}%`,
                    }}
                  >
                    <div className="relative group">
                      <motion.div
                        className="w-8 h-8 bg-gradient-to-br from-[#1E4C82] to-[#2CB67D] rounded-full shadow-lg cursor-pointer"
                        whileHover={{ scale: 1.2 }}
                        animate={{
                          boxShadow: [
                            "0 0 0 0 rgba(30, 76, 130, 0.4)",
                            "0 0 0 10px rgba(30, 76, 130, 0)",
                          ],
                        }}
                        transition={{
                          duration: 2,
                          repeat: Infinity,
                        }}
                      >
                        <MapPin className="w-4 h-4 text-white absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" />
                      </motion.div>
                      <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 opacity-0 group-hover:opacity-100 transition-opacity bg-white dark:bg-slate-800 px-3 py-2 rounded-lg shadow-xl whitespace-nowrap text-sm">
                        <p>{barangay.name}</p>
                        <p className="text-xs text-slate-600 dark:text-slate-400">{barangay.families} families</p>
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            </motion.div>

            {/* Barangay List */}
            {barangays.map((barangay, index) => (
              <motion.div
                key={barangay.name}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 + index * 0.05 }}
                className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-4 shadow-lg"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-[#1E4C82] to-[#2CB67D] rounded-full flex items-center justify-center">
                      <MapPin className="w-5 h-5 text-white" />
                    </div>
                    <div>
                      <h4>{barangay.name}</h4>
                      <p className="text-sm text-slate-600 dark:text-slate-400">{barangay.families} families assisted</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="mb-1">{barangay.donations}</p>
                    <Badge
                      className={`${
                        barangay.status === "active"
                          ? "bg-[#2CB67D]/10 text-[#2CB67D] border-[#2CB67D]/30"
                          : "bg-[#FDB813]/10 text-[#FDB813] border-[#FDB813]/30"
                      } capitalize rounded-full border`}
                    >
                      {barangay.status}
                    </Badge>
                  </div>
                </div>
              </motion.div>
            ))}
          </motion.div>
        )}

        {activeTab === "stories" && (
          <motion.div
            key="stories"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-6"
          >
            {stories.map((story, index) => (
              <motion.div
                key={index}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl overflow-hidden shadow-lg"
              >
                <div className="h-48 overflow-hidden relative">
                  <img
                    src={story.image}
                    alt={story.family}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent" />
                </div>
                <div className="p-5">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <h3 className="mb-1">{story.family}</h3>
                      <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
                        <MapPin className="w-4 h-4" />
                        <span>{story.barangay}</span>
                      </div>
                    </div>
                    <Badge className="bg-[#1E4C82]/10 text-[#1E4C82] dark:bg-[#3b82f6]/10 dark:text-[#3b82f6] border-[#1E4C82]/30 dark:border-[#3b82f6]/30 rounded-full border">
                      {story.date}
                    </Badge>
                  </div>
                  <p className="text-slate-700 dark:text-slate-300 leading-relaxed">{story.story}</p>
                </div>
              </motion.div>
            ))}
          </motion.div>
        )}
      </div>
    </div>
  );
}
