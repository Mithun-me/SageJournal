import React, { useState } from 'react';
import {
  X,
  Flame,
  Star,
  BookOpen,
  Sparkles,
  Heart,
  Cloud,
  LogOut,
  LogIn,
  Edit2,
  Check,
  ShieldCheck,
} from 'lucide-react';
import { JournalEntry } from '../../types';
import { triggerHaptic } from '../../utils/haptics';
import { useAuth, DEFAULT_AVATARS } from '../../contexts/AuthContext';

interface ProfileModalProps {
  isOpen: boolean;
  onClose: () => void;
  streak: number;
  totalPoints: number;
  entries: JournalEntry[];
  onOpenSettings: () => void;
  onOpenAuth?: () => void;
}

export const ProfileModal: React.FC<ProfileModalProps> = ({
  isOpen,
  onClose,
  streak,
  totalPoints,
  entries,
  onOpenSettings,
  onOpenAuth,
}) => {
  const { currentUser, userProfile, logOut, updateProfileData } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [editName, setEditName] = useState(userProfile?.displayName || '');
  const [editAvatar, setEditAvatar] = useState(userProfile?.avatarEmoji || '🌿');

  if (!isOpen) return null;

  const favoriteCount = entries.filter((e) => e.isFavorite).length;

  const handleSaveProfile = async () => {
    triggerHaptic('medium');
    if (editName.trim()) {
      await updateProfileData({
        displayName: editName.trim(),
        avatarEmoji: editAvatar,
      });
    }
    setIsEditing(false);
  };

  const handleSignOut = async () => {
    triggerHaptic('medium');
    await logOut();
    onClose();
  };

  return (
    <div
      id="profile-modal-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn"
    >
      <div
        id="profile-modal-card"
        className="w-full max-w-sm bg-[#0e1022] border border-white/10 rounded-3xl p-5 sm:p-6 shadow-2xl relative flex flex-col gap-4 overflow-hidden"
      >
        {/* Ambient glow */}
        <div className="absolute -top-20 -right-20 w-44 h-44 rounded-full bg-[#4fdbc8]/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-20 -left-20 w-44 h-44 rounded-full bg-[#8083ff]/10 blur-3xl pointer-events-none" />

        {/* Top bar */}
        <div className="flex justify-between items-center relative z-10">
          <div className="flex items-center gap-1.5">
            <span className="text-xs font-bold text-[#4fdbc8] uppercase tracking-wider font-['Manrope']">
              Mindful Profile
            </span>
            {currentUser && (
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/15 text-emerald-300 border border-emerald-500/30 flex items-center gap-1">
                <Cloud className="w-2.5 h-2.5" />
                <span>Cloud Synced</span>
              </span>
            )}
          </div>
          <button
            id="close-profile-btn"
            onClick={() => {
              triggerHaptic('light');
              onClose();
            }}
            className="p-1.5 rounded-full hover:bg-white/10 text-[#c7c4d7] hover:text-white transition-all active:scale-95"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Profile Identity Card */}
        <div className="flex flex-col items-center text-center gap-2 relative z-10">
          {/* Avatar with edit capability */}
          <div className="relative group">
            <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-[#1a2040] to-[#252f5a] border-2 border-[#4fdbc8]/50 flex items-center justify-center text-3xl shadow-lg transition-transform group-hover:scale-105">
              {isEditing ? editAvatar : userProfile?.avatarEmoji || '🌿'}
            </div>
            {currentUser && !isEditing && (
              <button
                onClick={() => {
                  triggerHaptic('light');
                  setEditName(userProfile?.displayName || '');
                  setEditAvatar(userProfile?.avatarEmoji || '🌿');
                  setIsEditing(true);
                }}
                title="Edit nickname & avatar"
                className="absolute -bottom-1 -right-1 p-1.5 rounded-full bg-[#4fdbc8] text-[#0a0c1a] shadow hover:opacity-90 transition-all active:scale-90"
              >
                <Edit2 className="w-3 h-3" />
              </button>
            )}
          </div>

          {/* Name & Account Type */}
          {isEditing ? (
            <div className="w-full space-y-2 my-1">
              <input
                type="text"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                placeholder="Enter mindful alias"
                className="w-full text-center py-1.5 px-3 rounded-xl bg-[#060814] border border-[#4fdbc8]/40 text-sm font-bold text-white focus:outline-none"
              />
              <div className="flex items-center justify-center gap-1 overflow-x-auto py-1">
                {DEFAULT_AVATARS.map((emoji) => (
                  <button
                    key={emoji}
                    type="button"
                    onClick={() => {
                      triggerHaptic('light');
                      setEditAvatar(emoji);
                    }}
                    className={`w-7 h-7 rounded-lg text-sm flex items-center justify-center transition-all ${
                      editAvatar === emoji
                        ? 'bg-[#4fdbc8]/30 border border-[#4fdbc8] scale-110'
                        : 'bg-white/5 border border-white/10 hover:bg-white/10'
                    }`}
                  >
                    {emoji}
                  </button>
                ))}
              </div>
              <button
                type="button"
                onClick={handleSaveProfile}
                className="w-full py-1.5 rounded-xl bg-[#4fdbc8] text-[#0a0c1a] text-xs font-bold flex items-center justify-center gap-1.5 hover:opacity-90 transition-all"
              >
                <Check className="w-3.5 h-3.5" />
                <span>Save Profile</span>
              </button>
            </div>
          ) : (
            <div>
              <h3 className="text-lg font-bold text-white font-['Manrope'] flex items-center justify-center gap-1.5">
                <span>{userProfile?.displayName || 'Mindful Seeker'}</span>
              </h3>
              <p className="text-xs text-[#908fa0]">
                {currentUser?.isAnonymous
                  ? 'Guest Practice • Firestore DB Active'
                  : currentUser
                  ? 'Sanctuary Member • Cloud Synced'
                  : 'Local Sanctuary • Offline Mode'}
              </p>
            </div>
          )}
        </div>

        {/* Core Metrics Grid */}
        <div className="grid grid-cols-3 gap-2 relative z-10">
          <div className="p-3 rounded-2xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <Flame className="w-3.5 h-3.5 text-[#ffb783] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{streak}d</span>
            <span className="text-[10px] text-[#908fa0] uppercase tracking-wide">Streak</span>
          </div>

          <div className="p-3 rounded-2xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <Star className="w-3.5 h-3.5 text-[#4fdbc8] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{totalPoints}</span>
            <span className="text-[10px] text-[#908fa0] uppercase tracking-wide">Points</span>
          </div>

          <div className="p-3 rounded-2xl bg-[#0a0c1a] border border-white/[0.06] flex flex-col items-center text-center">
            <BookOpen className="w-3.5 h-3.5 text-[#8083ff] mb-0.5" />
            <span className="text-base font-bold text-white font-['Manrope']">{entries.length}</span>
            <span className="text-[10px] text-[#908fa0] uppercase tracking-wide">Entries</span>
          </div>
        </div>

        {/* Cloud Sync & Non-PII Safeguard Panel */}
        <div className="p-3.5 rounded-2xl bg-[#14182e]/80 border border-white/10 space-y-2 text-xs relative z-10">
          <div className="flex justify-between items-center text-[#e1e1f6]">
            <span className="flex items-center gap-1.5 text-[#c7c4d7]">
              <Heart className="w-3.5 h-3.5 text-[#ffb783]" /> Saved Favorites:
            </span>
            <span className="font-semibold text-white">{favoriteCount} memories</span>
          </div>

          <div className="flex justify-between items-center text-[#e1e1f6]">
            <span className="flex items-center gap-1.5 text-[#c7c4d7]">
              <Cloud className="w-3.5 h-3.5 text-[#4fdbc8]" /> Database Sync:
            </span>
            <span className="font-semibold text-[#71f8e4]">
              {currentUser ? 'Firestore Connected' : 'Local Only'}
            </span>
          </div>

          <div className="pt-2 border-t border-white/[0.08] flex items-center gap-1.5 text-[10px] text-[#908fa0]">
            <ShieldCheck className="w-3 h-3 text-[#4fdbc8] shrink-0" />
            <span>Non-PII Encrypted: Only your nickname and reflections are stored.</span>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="space-y-2 relative z-10 pt-1">
          {currentUser ? (
            <button
              id="profile-signout-btn"
              onClick={handleSignOut}
              className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-red-500/15 border border-white/10 hover:border-red-500/30 text-xs font-semibold text-[#c7c4d7] hover:text-red-300 transition-all flex items-center justify-center gap-2 active:scale-95"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Sign Out of Sanctuary</span>
            </button>
          ) : (
            <button
              id="profile-signin-prompt-btn"
              onClick={() => {
                triggerHaptic('light');
                onClose();
                if (onOpenAuth) onOpenAuth();
              }}
              className="w-full py-2.5 rounded-xl bg-gradient-to-r from-[#4fdbc8] to-[#6366f1] text-[#0a0c1a] text-xs font-bold shadow-md hover:opacity-90 transition-all flex items-center justify-center gap-2 active:scale-95"
            >
              <LogIn className="w-3.5 h-3.5" />
              <span>Sign In / Create Cloud Account</span>
            </button>
          )}

          <button
            onClick={() => {
              triggerHaptic('light');
              onClose();
              onOpenSettings();
            }}
            className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold text-[#c7c4d7] hover:text-white transition-all active:scale-95 text-center"
          >
            Preferences &amp; Settings
          </button>
        </div>
      </div>
    </div>
  );
};
