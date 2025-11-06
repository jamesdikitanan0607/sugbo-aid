import { useState } from "react";
import { motion } from "motion/react";
import { ArrowLeft, Minus, Plus, QrCode, Sparkles, Check } from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Badge } from "./ui/badge";
import { Card } from "./ui/card";

interface POSDonationProps {
  onBack: () => void;
}

export function POSDonation({ onBack }: POSDonationProps) {
  const [donationType, setDonationType] = useState<"cash" | "goods">("cash");
  const [amount, setAmount] = useState("");
  const [showReceipt, setShowReceipt] = useState(false);

  const quickAmounts = [100, 500, 1000, 5000];
  
  const goodsCategories = [
    { name: "Rice", unit: "sacks", icon: "🌾" },
    { name: "Water", unit: "bottles", icon: "💧" },
    { name: "Medicine", unit: "boxes", icon: "💊" },
    { name: "Clothes", unit: "pieces", icon: "👕" },
  ];

  const [goods, setGoods] = useState<Record<string, number>>({});

  const handleSubmit = () => {
    // Show confetti and receipt
    setShowReceipt(true);
  };

  if (showReceipt) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-[#1E4C82] via-[#2563eb] to-[#2CB67D] flex items-center justify-center p-4">
        {/* Confetti Animation */}
        {[...Array(30)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-3 h-3 rounded-full"
            style={{
              backgroundColor: ["#FDB813", "#2CB67D", "#1E4C82", "#fff"][i % 4],
              left: `${Math.random() * 100}%`,
              top: -20,
            }}
            animate={{
              y: [0, window.innerHeight + 50],
              x: [0, (Math.random() - 0.5) * 200],
              rotate: [0, 360 * (Math.random() > 0.5 ? 1 : -1)],
              opacity: [1, 0],
            }}
            transition={{
              duration: 2 + Math.random() * 2,
              delay: Math.random() * 0.5,
              ease: "easeOut",
            }}
          />
        ))}

        <motion.div
          initial={{ scale: 0, rotate: -180 }}
          animate={{ scale: 1, rotate: 0 }}
          transition={{ type: "spring", stiffness: 200, damping: 15 }}
          className="w-full max-w-md"
        >
          <div className="bg-white/20 backdrop-blur-2xl border-2 border-white/30 rounded-3xl p-8 shadow-2xl">
            <div className="text-center mb-6">
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ delay: 0.3, type: "spring", stiffness: 300 }}
                className="w-20 h-20 bg-gradient-to-br from-[#2CB67D] to-[#10b981] rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg"
              >
                <Check className="w-10 h-10 text-white" />
              </motion.div>
              <h2 className="text-3xl text-white mb-2">Thank You!</h2>
              <p className="text-white/80">Your donation has been recorded</p>
            </div>

            {/* QR Code */}
            <div className="bg-white rounded-2xl p-6 mb-6">
              <div className="flex items-center justify-center">
                <div className="w-48 h-48 bg-gradient-to-br from-slate-100 to-slate-200 rounded-xl flex items-center justify-center">
                  <QrCode className="w-40 h-40 text-slate-400" />
                </div>
              </div>
              <p className="text-center text-sm text-slate-600 mt-4">Receipt #CEBu-{Date.now().toString().slice(-6)}</p>
            </div>

            {/* Details */}
            <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 mb-6 space-y-2">
              <div className="flex justify-between text-white/90">
                <span>Type:</span>
                <span className="capitalize">{donationType}</span>
              </div>
              <div className="flex justify-between text-white/90">
                <span>Amount:</span>
                <span>{donationType === "cash" ? `₱${amount}` : `${Object.values(goods).reduce((a, b) => a + b, 0)} items`}</span>
              </div>
              <div className="flex justify-between text-white/90">
                <span>Date:</span>
                <span>{new Date().toLocaleDateString()}</span>
              </div>
            </div>

            <Button
              onClick={onBack}
              className="w-full bg-white/20 hover:bg-white/30 backdrop-blur-md border border-white/30 text-white rounded-2xl h-12"
            >
              Back to Dashboard
            </Button>
          </div>
        </motion.div>
      </div>
    );
  }

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
            <h2 className="text-xl">New Donation</h2>
            <p className="text-sm text-slate-600 dark:text-slate-400">POS Checkout</p>
          </div>
        </div>
      </div>

      <div className="p-4 pb-24">
        {/* Donation Type Toggle */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6"
        >
          <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-2 grid grid-cols-2 gap-2 shadow-lg">
            <Button
              onClick={() => setDonationType("cash")}
              className={`rounded-xl h-12 transition-all duration-300 ${
                donationType === "cash"
                  ? "bg-gradient-to-r from-[#1E4C82] to-[#2563eb] text-white shadow-lg"
                  : "bg-transparent text-slate-700 dark:text-slate-300 hover:bg-white/50"
              }`}
            >
              Cash Donation
            </Button>
            <Button
              onClick={() => setDonationType("goods")}
              className={`rounded-xl h-12 transition-all duration-300 ${
                donationType === "goods"
                  ? "bg-gradient-to-r from-[#2CB67D] to-[#10b981] text-white shadow-lg"
                  : "bg-transparent text-slate-700 dark:text-slate-300 hover:bg-white/50"
              }`}
            >
              In-Kind Goods
            </Button>
          </div>
        </motion.div>

        {donationType === "cash" ? (
          <motion.div
            key="cash"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-6"
          >
            {/* Amount Input */}
            <div className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-6 shadow-lg">
              <Label className="mb-3 block">Enter Amount (PHP)</Label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-2xl text-slate-600 dark:text-slate-400">₱</span>
                <Input
                  type="number"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="text-3xl h-16 pl-10 bg-white/50 dark:bg-slate-900/50 border-slate-300 dark:border-slate-600 rounded-xl"
                  placeholder="0.00"
                />
              </div>
            </div>

            {/* Quick Amounts */}
            <div>
              <Label className="mb-3 block">Quick Select</Label>
              <div className="grid grid-cols-2 gap-3">
                {quickAmounts.map((value) => (
                  <Button
                    key={value}
                    onClick={() => setAmount(value.toString())}
                    className="h-16 bg-gradient-to-br from-white/80 to-white/60 dark:from-slate-800/80 dark:to-slate-800/60 backdrop-blur-xl border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-white hover:shadow-xl transition-all duration-300 hover:scale-105 rounded-xl"
                  >
                    ₱{value.toLocaleString()}
                  </Button>
                ))}
              </div>
            </div>
          </motion.div>
        ) : (
          <motion.div
            key="goods"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            className="space-y-4"
          >
            {goodsCategories.map((category) => (
              <div
                key={category.name}
                className="bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-4 shadow-lg"
              >
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-3">
                    <span className="text-3xl">{category.icon}</span>
                    <div>
                      <p>{category.name}</p>
                      <p className="text-sm text-slate-600 dark:text-slate-400">{category.unit}</p>
                    </div>
                  </div>
                  <Badge className="bg-[#2CB67D] text-white rounded-full">
                    {goods[category.name] || 0}
                  </Badge>
                </div>
                <div className="flex items-center gap-3">
                  <Button
                    onClick={() => setGoods({ ...goods, [category.name]: Math.max(0, (goods[category.name] || 0) - 1) })}
                    size="icon"
                    className="rounded-full bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-300 dark:hover:bg-slate-600"
                  >
                    <Minus className="w-4 h-4" />
                  </Button>
                  <div className="flex-1 h-12 bg-white/50 dark:bg-slate-900/50 rounded-xl flex items-center justify-center border border-slate-300 dark:border-slate-600">
                    <span className="text-xl">{goods[category.name] || 0}</span>
                  </div>
                  <Button
                    onClick={() => setGoods({ ...goods, [category.name]: (goods[category.name] || 0) + 1 })}
                    size="icon"
                    className="rounded-full bg-gradient-to-br from-[#2CB67D] to-[#10b981] text-white"
                  >
                    <Plus className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            ))}
          </motion.div>
        )}

        {/* Donor Information */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="mt-6 bg-white/60 dark:bg-slate-800/60 backdrop-blur-xl border border-white/20 dark:border-slate-700/50 rounded-2xl p-6 shadow-lg"
        >
          <Label className="mb-3 block">Donor Name (Optional)</Label>
          <Input
            placeholder="Anonymous"
            className="bg-white/50 dark:bg-slate-900/50 border-slate-300 dark:border-slate-600 rounded-xl h-12"
          />
        </motion.div>

        {/* Submit Button */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mt-6"
        >
          <Button
            onClick={handleSubmit}
            disabled={donationType === "cash" ? !amount : Object.values(goods).every(v => v === 0)}
            className="w-full h-16 bg-gradient-to-r from-[#1E4C82] via-[#2CB67D] to-[#FDB813] hover:shadow-2xl transition-all duration-300 hover:scale-105 rounded-2xl relative overflow-hidden group"
          >
            <motion.div
              className="absolute inset-0 bg-gradient-to-r from-[#FDB813] via-[#2CB67D] to-[#1E4C82]"
              animate={{
                x: ["-100%", "100%"],
              }}
              transition={{
                duration: 3,
                repeat: Infinity,
                ease: "linear",
              }}
            />
            <span className="relative z-10 flex items-center gap-2 text-white">
              <Sparkles className="w-5 h-5" />
              Complete Donation
            </span>
          </Button>
        </motion.div>
      </div>
    </div>
  );
}
