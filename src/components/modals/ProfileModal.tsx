import React from 'react';
import { X, Flame, Trophy, Star, BookOpen, Sparkles, Heart } from 'lucide-react';
import { JournalEntry } from '../../types';
import { triggerHaptic } from '../../utils/haptics';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  streak: number;
  totalPoints: number;
  entries: JournalEntry[];
  onOpenSettings: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  isOpen,
  onClose,
  streak,
  totalPoints,
  entries,
  onOpenSettings,
}) => {
  if (!isOpen) return null;

  const favoriteCount = entries.filter((e) => e.isFavorite).length;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn">
      <div className="w-full max-w-sm bg-[#0e1022] border border-white/10 rounded-2xl p-5 shadow-2xl relative flex flex-col gap-4">
        {/* Top bar */}
        <div className="flex justify-between items-center">
          <span className="text-xs font-bold text-[#4fdbc8] uppercase tracking-wider font-['Manrope']">
            Profile
          </span>
          <button
            onClick={() => {
              triggerHaptic('light');
              onClose();
            }}
            className="p-1 rounded-lg hover:bg-white/10 text-[#c7c4d7] hover:text-white transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Profile Card */}
        <div className="flex flex-col items-center text-center gap-2">
          <div className="w-16 h-16 rounded-full overflow-hidden border-2 border-[#4fdbc8]/60 shadow">
            <img
              className="w-full h-full object-cover"
              alt="Profile"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuC-6qSnQxQRN0QQMbWZweQyyHKcBx1uscmHDpjFK7MxQkPu2zHKBHsnMkT4JF62G_DkVCym12VwJwpPBW-9kaZqDA_SfZeeiSOwuLwRA3KqOCGniQzD3nY2RQj506BKY9xGaTadIIhqQf251MgrWUc5SbWGoV9SpcDd92mmQ8NKKjGmI6_tz_1KUaxVqTU_bXDmC4IF5wFivaIT9VHmduuKuSmZDgc6XoKsxt8nk-TGB07yzMHdXya5"
            />
          </div>
          <div>
            <h3 className="text-base font-bold text-white font-['Manrope']">
              Maya Lin
            </h3>
            <p className="text-xs text-[#908fa0]">
              Sanctuary Member • 14 Day Practice
            </p>
          </div>
        </div>

        {/* Core Metrics */}
        <div className="grid grid-cols-3 gap-2">
          <div className="p-3 rounded-xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <Flame className="w-3.5 h-3.5 text-[#ffb783] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{streak}d</span>
            <span className="text-[10px] text-[#908fa0] uppercase">Streak</span>
          </div>

          <div className="p-3 rounded-xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <Star className="w-3.5 h-3.5 text-[#4fdbc8] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{totalPoints}</span>
            <span className="text-[10px] text-[#908fa0] uppercase">Points</span>
          </div>

          <div className="p-3 rounded-xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <BookOpen className="w-3.5 h-3.5 text-[#8083ff] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{entries.length}</span>
            <span className="text-[10px] text-[#908fa0] uppercase">Entries</span>
          </div>
        </div>

        {/* Favorite & Routine details */}
        <div className="p-3.5 rounded-xl bg-[#14182e] border border-white/10 space-y-1.5 text-xs">
          <div className="flex justify-between text-[#e1e1f6]">
            <span className="flex items-center gap-1.5"><Heart className="w-3.5 h-3.5 text-[#ffb783]" /> Favorites:</span>
            <span className="font-semibold text-white">{favoriteCount} memories</span>
          </div>
          <div className="flex justify-between text-[#e1e1f6]">
            <span className="flex items-center gap-1.5"><Sparkles className="w-3.5 h-3.5 text-[#4fdbc8]" /> Consistency:</span>
            <span className="font-semibold text-[#4fdbc8]">Optimal</span>
          </div>
        </div>

        {/* Button to Settings */}
        <button
          onClick={() => {
            triggerHaptic('light');
            onClose();
            onOpenSettings();
          }}
          className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold text-white transition-all active:scale-95"
        >
          Preferences &amp; Settings
        </button>
      </div>
    </div>
  );
};
