import React from 'react';
import { Sparkles, Flame, Moon, Sun } from 'lucide-react';
import { AppTheme } from '../types';
import { StatusBar } from './mobile/StatusBar';
import { triggerHaptic } from '../utils/haptics';
import { isNative } from '../utils/platform';

interface TopAppBarProps {
  streak: number;
  theme: AppTheme;
  onToggleTheme: () => void;
  onOpenProfile: () => void;
  onOpenNewEntry: () => void;
  showIsland?: boolean;
}

export const TopAppBar: React.FC<TopAppBarProps> = ({
  streak,
  theme,
  onToggleTheme,
  onOpenProfile,
  showIsland = false,
}) => {
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

          {/* User Profile Avatar */}
          <button
            id="user-profile-btn"
            onClick={() => {
              triggerHaptic('light');
              onOpenProfile();
            }}
            className="flex items-center justify-center p-0.5 rounded-full hover:bg-white/10 transition-all active:scale-95 cursor-pointer border border-white/15"
          >
            <div className="w-7 h-7 rounded-full overflow-hidden shrink-0">
              <img
                className="w-full h-full object-cover"
                alt="User profile"
                src="https://lh3.googleusercontent.com/aida-public/AB6AXuC-6qSnQxQRN0QQMbWZweQyyHKcBx1uscmHDpjFK7MxQkPu2zHKBHsnMkT4JF62G_DkVCym12VwJwpPBW-9kaZqDA_SfZeeiSOwuLwRA3KqOCGniQzD3nY2RQj506BKY9xGaTadIIhqQf251MgrWUc5SbWGoV9SpcDd92mmQ8NKKjGmI6_tz_1KUaxVqTU_bXDmC4IF5wFivaIT9VHmduuKuSmZDgc6XoKsxt8nk-TGB07yzMHdXya5"
              />
            </div>
          </button>
        </div>
      </div>
    </header>
  );
};
