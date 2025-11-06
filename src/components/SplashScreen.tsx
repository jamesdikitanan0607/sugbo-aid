import * as React from "react";
import { motion } from "framer-motion";
import { Heart, HandHeart, Users, Package } from "lucide-react";
import { Button } from "./ui/button";

type UserRole = 'Donor' | 'Organization' | 'Volunteer' | 'Recipient' | 'Guest';

interface SplashScreenProps {
  onComplete: (role: UserRole) => void;
}

export function SplashScreen({ onComplete }: SplashScreenProps) {
  const [selectedRole, setSelectedRole] = React.useState<UserRole | null>(null);

  const handleRoleSelect = (role: UserRole) => {
    try {
      console.log('Selected role:', role);
      setSelectedRole(role);
      if (onComplete && typeof onComplete === 'function') {
        onComplete(role);
      } else {
        console.error('onComplete is not a function');
      }
    } catch (error) {
      console.error('Error in handleRoleSelect:', error);
    }
  };
  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-[#1E4C82] via-[#2563eb] to-[#2CB67D]">
      {/* Parallax Background */}
      <motion.div
        className="absolute inset-0 opacity-30"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1701705994021-b21330a838a0?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjZWJ1JTIwcGhpbGlwcGluZXMlMjBza3lsaW5lfGVufDF8fHx8MTc1OTg5NTU4Mnww&ixlib=rb-4.1.0&q=80&w=1080')`,
          backgroundSize: "cover",
          backgroundPosition: "center",
        }}
        animate={{
          scale: [1, 1.1, 1],
          y: [0, -20, 0],
        }}
        transition={{
          duration: 10,
          repeat: Infinity,
          ease: "easeInOut",
        }}
      />

      {/* Animated Waves */}
      <motion.div
        className="absolute bottom-0 left-0 right-0 h-32 opacity-20"
        style={{
          backgroundImage: `url('https://images.unsplash.com/photo-1621002478072-085fefaa529f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxvY2VhbiUyMHdhdmVzJTIwc3Vuc2V0fGVufDF8fHx8MTc1OTg4MDUxMHww&ixlib=rb-4.1.0&q=80&w=1080')`,
          backgroundSize: "cover",
        }}
        animate={{
          x: [-100, 0],
        }}
        transition={{
          duration: 8,
          repeat: Infinity,
          ease: "linear",
        }}
      />

      {/* Frosted Glass Overlay */}
      <div className="absolute inset-0 backdrop-blur-sm bg-gradient-to-b from-transparent via-black/10 to-black/30" />

      {/* Content */}
      <div className="relative z-10 flex flex-col items-center justify-center min-h-screen p-6">
        {/* Logo Animation */}
        <motion.div
          initial={{ scale: 0, rotate: -180 }}
          animate={{ scale: 1, rotate: 0 }}
          transition={{
            type: "spring",
            stiffness: 200,
            damping: 15,
            delay: 0.2,
          }}
          className="mb-8"
        >
          <div className="relative">
            <div className="absolute inset-0 bg-gradient-to-r from-[#FDB813] to-[#2CB67D] rounded-full blur-2xl opacity-50 animate-pulse" />
            <div className="relative bg-white/20 backdrop-blur-md rounded-full p-8 border-2 border-white/30 shadow-2xl">
              <Heart className="w-20 h-20 text-white fill-white" />
            </div>
          </div>
        </motion.div>

        {/* Title */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5, duration: 0.8 }}
          className="text-center mb-4"
        >
          <h1 className="text-5xl mb-2 text-white">SugboAid</h1>
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.8 }}
            className="text-xl text-white/90"
          >
            Together, We Rebuild Cebu.
          </motion.p>
        </motion.div>

        {/* Features */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1, duration: 0.8 }}
          className="grid grid-cols-3 gap-4 mb-12 max-w-md"
        >
          {[
            { icon: HandHeart, label: "Donate" },
            { icon: Package, label: "Track" },
            { icon: Users, label: "Impact" },
          ].map((item, index) => (
            <motion.div
              key={item.label}
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 1.2 + index * 0.1 }}
              className="bg-white/10 backdrop-blur-md rounded-2xl p-4 border border-white/20 text-center"
            >
              <item.icon className="w-8 h-8 text-white mx-auto mb-2" />
              <p className="text-sm text-white/90">{item.label}</p>
            </motion.div>
          ))}
        </motion.div>

        {/* Role Selection */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 1.5, duration: 0.8 }}
          className="w-full max-w-md space-y-3"
        >
          {['Donor', 'Organization', 'Volunteer', 'Recipient'].map((role: UserRole, index) => {
            <motion.div
              key={role}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 1.7 + index * 0.1 }}
            >
              <button 
                onClick={() => handleRoleSelect(role as UserRole)}
                style={{
                  width: '100%',
                  backgroundColor: 'rgba(255, 255, 255, 0.2)',
                  backdropFilter: 'blur(10px)',
                  border: '1px solid rgba(255, 255, 255, 0.3)',
                  color: 'white',
                  borderRadius: '1rem',
                  padding: '1rem',
                  marginBottom: '0.75rem',
                  transition: 'all 0.3s',
                  cursor: 'pointer'
                }}
                onMouseOver={(e) => {
                  e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.3)';
                  e.currentTarget.style.transform = 'scale(1.02)';
                }}
                onMouseOut={(e) => {
                  e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.2)';
                  e.currentTarget.style.transform = 'scale(1)';
                }}
              >
                Continue as {role}
              </button>
            </motion.div>
          ))}

          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 2.2 }}
          >
            <button 
              onClick={() => handleRoleSelect('Guest')}
              style={{
                width: '100%',
                backgroundColor: 'transparent',
                border: 'none',
                color: 'rgba(255, 255, 255, 0.8)',
                padding: '0.75rem',
                cursor: 'pointer',
                transition: 'all 0.3s',
                marginTop: '0.5rem'
              }}
              onMouseOver={(e) => {
                e.currentTarget.style.color = 'white';
                e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.color = 'rgba(255, 255, 255, 0.8)';
                e.currentTarget.style.backgroundColor = 'transparent';
              }}
            >
              Continue as Guest
            </button>
          </motion.div>
        </motion.div>

        {/* Bottom Text */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 2.5 }}
          className="mt-8 text-sm text-white/70 text-center"
        >
          Transparent, Real-Time, Locally-Built Relief
        </motion.p>
      </div>
    </div>
  );
}
