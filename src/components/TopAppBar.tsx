import React from 'react';
import { Sparkles, Flame, Moon, Sun, Cloud, UserCheck, LogIn } from 'lucide-react';
import { AppTheme } from '../types';
import { StatusBar } from './mobile/StatusBar';
import { triggerHaptic } from '../utils/haptics';
import { isNative } from '../utils/platform';
import { useAuth } from '../contexts/AuthContext';

interface TopAppBarProps {
  streak: number;
  theme: AppTheme;
  onToggleTheme: () => void;
  onOpenProfile: () => void;
  onOpenAuth?: () => void;
  onOpenNewEntry: () => void;
  showIsland?: boolean;
}

export const TopAppBar: React.FC<TopAppBarProps> = ({
  streak,
  theme,
  onToggleTheme,
  onOpenProfile,
  onOpenAuth,
  showIsland = false,
}) => {
  const { currentUser, userProfile } = useAuth();

  return (
    <header
      id="top-app-bar"
      className="aura-safe-top fixed top-0 left-0 w-full z-40 bg-[#0a0c1a]/80 backdrop-blur-2xl border-b border-white/[0.08] transition-all duration-300"
    >
      {/* Simulated iOS status bar — browser previews only. On a real device the
          OS draws its own, so rendering this too would duplicate the clock. */}
      {!isNative && <StatusBar showIsland={showIsland} />}

      {/* Main Header Content */}
      <div className="flex justify-between items-center px-4 sm:px-6 py-2.5">
        {/* Brand */}
        <div
          onClick={() => {
            triggerHaptic('light');
            onOpenProfile();
          }}
          className="flex items-center gap-2.5 hover:bg-white/5 transition-all px-2 py-1 rounded-full cursor-pointer active:scale-95"
        >
          <div className="w-7 h-7 rounded-full bg-gradient-to-tr from-[#4f46e5] to-[#4fdbc8] flex items-center justify-center shadow-sm">
            <Sparkles className="w-3.5 h-3.5 text-white" />
          </div>
          <div className="flex items-baseline gap-1.5">
            <h1 className="text-xl font-extrabold tracking-tight text-white font-['Manrope']">
              Aura
            </h1>
            <span className="text-[10px] font-medium text-[#4fdbc8] uppercase tracking-wider hidden xs:inline">
              Journal
            </span>
          </div>
        </div>

        {/* Action Items */}
        <div className="flex items-center gap-2">
          {/* Streak Pill */}
          <div className="flex items-center gap-1.5 bg-white/5 border border-white/10 px-2.5 py-1 rounded-full backdrop-blur-md">
            <Flame className="w-3.5 h-3.5 text-[#ffb783]" />
            <span className="text-xs font-bold text-[#ffdcc5] tracking-wide font-['Manrope']">
              {streak}d
            </span>
          </div>

          {/* Theme Toggle */}
          <button
            id="theme-toggle-btn"
            onClick={() => {
              triggerHaptic('light');
              onToggleTheme();
            }}
            title="Switch Theme"
            className="p-1.5 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-[#c7c4d7] hover:text-white transition-all active:scale-95"
          >
            {theme === 'liquid-glass' ? (
              <Sun className="w-4 h-4 text-[#4fdbc8]" />
            ) : (
              <Moon className="w-4 h-4 text-[#c0c1ff]" />
            )}
          </button>

          {/* User Profile / Auth Button */}
          {currentUser ? (
            <button
              id="user-profile-btn"
              onClick={() => {
                triggerHaptic('light');
                onOpenProfile();
              }}
              title={`Signed in as ${userProfile?.displayName || 'Seeker'}`}
              className="flex items-center gap-1.5 px-2 py-1 rounded-full bg-white/5 hover:bg-white/10 border border-[#4fdbc8]/30 transition-all active:scale-95 cursor-pointer"
            >
              <span className="text-sm">{userProfile?.avatarEmoji || '🌿'}</span>
              <Cloud className="w-3 h-3 text-[#4fdbc8]" />
            </button>
          ) : (
            <button
              id="header-signin-btn"
              onClick={() => {
                triggerHaptic('light');
                if (onOpenAuth) onOpenAuth();
                else onOpenProfile();
              }}
              className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-gradient-to-r from-[#4fdbc8]/20 to-[#6366f1]/20 hover:from-[#4fdbc8]/30 hover:to-[#6366f1]/30 border border-[#4fdbc8]/40 text-[#71f8e4] text-xs font-bold transition-all active:scale-95 shadow-sm"
            >
              <LogIn className="w-3.5 h-3.5" />
              <span>Sign In</span>
            </button>
          )}
        </div>
      </div>
    </header>
  );
};
