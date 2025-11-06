import { motion } from "motion/react";
import { ArrowLeft, Filter, Download, QrCode, Calendar, DollarSign, Package } from "lucide-react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { useState } from "react";

interface ReportsHistoryProps {
  onBack: () => void;
}

export function ReportsHistory({ onBack }: ReportsHistoryProps) {
  const [filter, setFilter] = useState<"all" | "cash" | "goods">("all");

  const transactions = [
    {
      id: "CEBU-001234",
      donor: "Juan Dela Cruz",
      type: "cash",
      amount: "₱5,000",
      date: "Oct 8, 2025",
      time: "10:30 AM",
      campaign: "Earthquake Relief 2025",
      verified: true,
    },
    {
      id: "CEBU-001233",
      donor: "Relief Org Cebu",
      type: "goods",
      amount: "200 packs rice",
      date: "Oct 8, 2025",
      time: "09:15 AM",
      campaign: "Food Distribution",
      verified: true,
    },
    {
      id: "CEBU-001232",
      donor: "Anonymous",
      type: "cash",
      amount: "₱10,000",
      date: "Oct 7, 2025",
      time: "04:45 PM",
      campaign: "Medical Supplies",
      verified: true,
    },
    {
      id: "CEBU-001231",
      donor: "Maria Santos",
      type: "goods",
      amount: "50 blankets",
      date: "Oct 7, 2025",
      time: "02:20 PM",
      campaign: "Shelter Support",
      verified: true,
    },
    {
      id: "CEBU-001230",
      donor: "ABC Foundation",
      type: "cash",
      amount: "₱50,000",
      date: "Oct 6, 2025",
      time: "11:00 AM",
      campaign: "Community Rebuilding",
      verified: true,
    },
    {
      id: "CEBU-001229",
      donor: "Pedro Reyes",
      type: "goods",
      amount: "100 bottles water",
      date: "Oct 6, 2025",
      time: "09:30 AM",
      campaign: "Water Distribution",
      verified: true,
    },
  ];

  const filteredTransactions = transactions.filter(
    (t) => filter === "all" || t.type === filter
  );

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
            <h2 className="text-xl">Reports & History</h2>
            <p className="text-sm text-slate-600 dark:text-slate-400">Transaction records</p>
          </div>
          <Button
            size="icon"
            className="rounded-full bg-gradient-to-br from-[#1E4C82] to-[#2563eb]"
          >
            <Download className="w-5 h-5" />
          </Button>
        </div>
      </div>

      <div className="p-4 pb-24">
        {/* Summary Cards */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="grid grid-cols-2 gap-3 mb-6"
        >
          {[
            { label: "Total Transactions", value: "1,247", icon: Calendar, color: "from-[#1E4C82] to-[#2563eb]" },
            { label: "Total Value", value: "₱2.4M", icon: DollarSign, color: "from-[#2CB67D] to-[#10b981]" },
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
        </motion.div>

        {/* Filters */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="mb-6"
        >
          <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-2 grid grid-cols-3 gap-2 shadow-lg">
            {[
              { id: "all", label: "All", icon: Filter },
              { id: "cash", label: "Cash", icon: DollarSign },
              { id: "goods", label: "Goods", icon: Package },
            ].map((item) => (
              <Button
                key={item.id}
                onClick={() => setFilter(item.id as any)}
                className={`rounded-xl h-10 transition-all duration-300 ${
                  filter === item.id
                    ? "bg-gradient-to-r from-[#1E4C82] to-[#2CB67D] text-white shadow-lg"
                    : "bg-transparent text-slate-700 dark:text-slate-300 hover:bg-white/50"
                }`}
              >
                <item.icon className="w-4 h-4 mr-2" />
                {item.label}
              </Button>
            ))}
          </div>
        </motion.div>

        {/* Transaction List */}
        <div className="space-y-3">
          {filteredTransactions.map((transaction, index) => (
            <motion.div
              key={transaction.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 + index * 0.05 }}
              className="group"
            >
              <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-4 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-[1.02]">
                {/* Header */}
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <div
                      className={`w-12 h-12 rounded-xl flex items-center justify-center ${
                        transaction.type === "cash"
                          ? "bg-gradient-to-br from-[#1E4C82] to-[#2563eb]"
                          : "bg-gradient-to-br from-[#2CB67D] to-[#10b981]"
                      } shadow-md`}
                    >
                      {transaction.type === "cash" ? (
                        <DollarSign className="w-6 h-6 text-white" />
                      ) : (
                        <Package className="w-6 h-6 text-white" />
                      )}
                    </div>
                    <div>
                      <h4 className="mb-1">{transaction.donor}</h4>
                      <p className="text-sm text-slate-600 dark:text-slate-400">
                        {transaction.campaign}
                      </p>
                    </div>
                  </div>
                  <Button
                    size="icon"
                    variant="ghost"
                    className="rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                  >
                    <QrCode className="w-5 h-5" />
                  </Button>
                </div>

                {/* Details */}
                <div className="grid grid-cols-2 gap-4 mb-3">
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mb-1">Amount</p>
                    <p>{transaction.amount}</p>
                  </div>
                  <div>
                    <p className="text-xs text-slate-600 dark:text-slate-400 mb-1">Receipt ID</p>
                    <p className="text-sm">{transaction.id}</p>
                  </div>
                </div>

                {/* Footer */}
                <div className="flex items-center justify-between pt-3 border-t border-slate-200 dark:border-slate-700">
                  <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400">
                    <Calendar className="w-4 h-4" />
                    <span>{transaction.date} • {transaction.time}</span>
                  </div>
                  <Badge
                    className={`${
                      transaction.verified
                        ? "bg-[#2CB67D]/10 text-[#2CB67D] border-[#2CB67D]/30"
                        : "bg-slate-500/10 text-slate-500 border-slate-500/30"
                    } rounded-full border flex items-center gap-1`}
                  >
                    <span className="w-2 h-2 rounded-full bg-current" />
                    Verified
                  </Badge>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Export Buttons */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.8 }}
          className="mt-6 grid grid-cols-2 gap-3"
        >
          <Button className="h-14 bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 text-slate-700 dark:text-slate-300 hover:bg-white/80 dark:hover:bg-slate-800/80 rounded-2xl">
            <Download className="w-5 h-5 mr-2" />
            Export PDF
          </Button>
          <Button className="h-14 bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 text-slate-700 dark:text-slate-300 hover:bg-white/80 dark:hover:bg-slate-800/80 rounded-2xl">
            <Download className="w-5 h-5 mr-2" />
            Export CSV
          </Button>
        </motion.div>
      </div>
    </div>
  );
}
