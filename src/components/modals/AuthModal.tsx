import React, { useState } from 'react';
import {
  X,
  Sparkles,
  Lock,
  Mail,
  User,
  ArrowRight,
  ShieldCheck,
  Eye,
  EyeOff,
  Radio,
} from 'lucide-react';
import { useAuth, DEFAULT_AVATARS } from '../../contexts/AuthContext';
import { triggerHaptic } from '../../utils/haptics';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialMode?: 'signin' | 'signup';
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  onClose,
  initialMode = 'signin',
}) => {
  const { signInWithEmail, signUpWithEmail, signInAsGuest } = useAuth();

  const [mode, setMode] = useState<'signin' | 'signup'>(initialMode);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [selectedAvatar, setSelectedAvatar] = useState('🌿');

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    triggerHaptic('medium');
    setErrorMessage(null);
    setSuccessMessage(null);
    setIsLoading(true);

    try {
      if (mode === 'signup') {
        if (!displayName.trim()) {
          setErrorMessage('Please choose a mindful nickname or alias.');
          setIsLoading(false);
          return;
        }
        if (password.length < 6) {
          setErrorMessage('Password must be at least 6 characters.');
          setIsLoading(false);
          return;
        }
        await signUpWithEmail(email, password, displayName, selectedAvatar);
        triggerHaptic('success');
        setSuccessMessage('Welcome to Aura! Your cloud sanctuary is now active.');
        setTimeout(() => {
          onClose();
        }, 1200);
      } else {
        await signInWithEmail(email, password);
        triggerHaptic('success');
        setSuccessMessage('Welcome back to your mindful space.');
        setTimeout(() => {
          onClose();
        }, 1000);
      }
    } catch (err: any) {
      triggerHaptic('warning');
      console.error('Auth error:', err);
      const code = err.code || '';
      if (code === 'auth/user-not-found' || code === 'auth/wrong-password' || code === 'auth/invalid-credential') {
        setErrorMessage('Invalid email or password. Please verify your credentials.');
      } else if (code === 'auth/email-already-in-use') {
        setErrorMessage('An account with this email already exists. Try signing in instead.');
      } else if (code === 'auth/weak-password') {
        setErrorMessage('Password is too weak. Please use at least 6 characters.');
      } else if (code === 'auth/invalid-email') {
        setErrorMessage('Please enter a valid email address.');
      } else {
        setErrorMessage(err.message || 'Authentication failed. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleGuestLogin = async () => {
    triggerHaptic('medium');
    setIsLoading(true);
    setErrorMessage(null);
    try {
      await signInAsGuest();
      triggerHaptic('success');
      setSuccessMessage('Guest sanctuary ready. Your reflections are stored in Firestore.');
      setTimeout(() => {
        onClose();
      }, 1000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Could not initiate guest session.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      id="auth-modal-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fadeIn"
    >
      <div
        id="auth-modal-card"
        className="w-full max-w-md bg-[#0e1022] border border-white/[0.12] rounded-3xl p-6 sm:p-7 shadow-[0_20px_50px_rgba(0,0,0,0.7)] relative flex flex-col gap-4 overflow-hidden"
      >
        {/* Subtle background glow */}
        <div className="absolute -top-20 -right-20 w-48 h-48 rounded-full bg-[#4fdbc8]/10 blur-3xl pointer-events-none" />
        <div className="absolute -bottom-20 -left-20 w-48 h-48 rounded-full bg-[#8083ff]/10 blur-3xl pointer-events-none" />

        {/* Top Header */}
        <div className="flex justify-between items-center relative z-10">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-[#4f46e5] to-[#4fdbc8] flex items-center justify-center shadow-md">
              <Sparkles className="w-4 h-4 text-white" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-white font-['Manrope']">
                {mode === 'signup' ? 'Create Mindful Space' : 'Welcome to Aura'}
              </h2>
              <p className="text-[11px] text-[#908fa0]">
                {mode === 'signup' ? 'Begin your cloud-synced practice' : 'Access your reflections & cloud history'}
              </p>
            </div>
          </div>
          <button
            id="close-auth-modal-btn"
            onClick={() => {
              triggerHaptic('light');
              onClose();
            }}
            className="p-1.5 rounded-full hover:bg-white/10 text-[#c7c4d7] hover:text-white transition-all active:scale-95"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab Switcher */}
        <div className="flex bg-[#060814] p-1 rounded-xl border border-white/[0.08] relative z-10">
          <button
            type="button"
            id="tab-signin-btn"
            onClick={() => {
              triggerHaptic('light');
              setMode('signin');
              setErrorMessage(null);
            }}
            className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
              mode === 'signin'
                ? 'bg-[#4fdbc8]/20 text-[#71f8e4] shadow-sm border border-[#4fdbc8]/30'
                : 'text-[#908fa0] hover:text-white'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            id="tab-signup-btn"
            onClick={() => {
              triggerHaptic('light');
              setMode('signup');
              setErrorMessage(null);
            }}
            className={`flex-1 py-2 text-xs font-semibold rounded-lg transition-all ${
              mode === 'signup'
                ? 'bg-[#4fdbc8]/20 text-[#71f8e4] shadow-sm border border-[#4fdbc8]/30'
                : 'text-[#908fa0] hover:text-white'
            }`}
          >
            Create Account
          </button>
        </div>

        {/* Status Alerts */}
        {errorMessage && (
          <div className="p-3 rounded-xl bg-red-500/15 border border-red-500/30 text-xs text-red-200 flex items-start gap-2 relative z-10 animate-fadeIn">
            <span className="text-red-400 font-bold shrink-0">!</span>
            <span>{errorMessage}</span>
          </div>
        )}

        {successMessage && (
          <div className="p-3 rounded-xl bg-emerald-500/15 border border-emerald-500/30 text-xs text-emerald-200 flex items-center gap-2 relative z-10 animate-fadeIn">
            <Sparkles className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{successMessage}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-3.5 relative z-10">
          {mode === 'signup' && (
            <>
              {/* Display Nickname (Non-PII) */}
              <div>
                <label className="block text-[11px] font-semibold text-[#c7c4d7] mb-1">
                  Mindful Alias / Nickname <span className="text-[#908fa0] font-normal">(Non-PII)</span>
                </label>
                <div className="relative flex items-center">
                  <User className="w-4 h-4 text-[#908fa0] absolute left-3 pointer-events-none" />
                  <input
                    type="text"
                    id="signup-alias-input"
                    value={displayName}
                    onChange={(e) => setDisplayName(e.target.value)}
                    placeholder="e.g. Mountain Solitude, Lotus Seeker"
                    required
                    className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-[#0a0c1a] border border-white/10 text-xs text-white placeholder-[#606175] focus:outline-none focus:border-[#4fdbc8]/50 transition-colors"
                  />
                </div>
              </div>

              {/* Mindful Avatar Picker */}
              <div>
                <label className="block text-[11px] font-semibold text-[#c7c4d7] mb-1.5">
                  Choose Mindful Avatar
                </label>
                <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
                  {DEFAULT_AVATARS.map((emoji) => (
                    <button
                      key={emoji}
                      type="button"
                      onClick={() => {
                        triggerHaptic('light');
                        setSelectedAvatar(emoji);
                      }}
                      className={`w-9 h-9 rounded-xl flex items-center justify-center text-lg transition-all shrink-0 ${
                        selectedAvatar === emoji
                          ? 'bg-[#4fdbc8]/25 border-2 border-[#4fdbc8] scale-105 shadow-[0_0_12px_rgba(79,219,200,0.3)]'
                          : 'bg-white/5 border border-white/10 hover:bg-white/10'
                      }`}
                    >
                      {emoji}
                    </button>
                  ))}
                </div>
              </div>
            </>
          )}

          {/* Email */}
          <div>
            <label className="block text-[11px] font-semibold text-[#c7c4d7] mb-1">
              Email Address
            </label>
            <div className="relative flex items-center">
              <Mail className="w-4 h-4 text-[#908fa0] absolute left-3 pointer-events-none" />
              <input
                type="email"
                id="auth-email-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@sanctuary.io"
                required
                className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-[#0a0c1a] border border-white/10 text-xs text-white placeholder-[#606175] focus:outline-none focus:border-[#4fdbc8]/50 transition-colors"
              />
            </div>
          </div>

          {/* Password */}
          <div>
            <label className="block text-[11px] font-semibold text-[#c7c4d7] mb-1">
              Password
            </label>
            <div className="relative flex items-center">
              <Lock className="w-4 h-4 text-[#908fa0] absolute left-3 pointer-events-none" />
              <input
                type={showPassword ? 'text' : 'password'}
                id="auth-password-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                minLength={6}
                className="w-full pl-9 pr-10 py-2.5 rounded-xl bg-[#0a0c1a] border border-white/10 text-xs text-white placeholder-[#606175] focus:outline-none focus:border-[#4fdbc8]/50 transition-colors"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 text-[#908fa0] hover:text-white p-1"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            id="auth-submit-btn"
            disabled={isLoading}
            className="w-full mt-2 py-3 rounded-xl bg-gradient-to-r from-[#4fdbc8] to-[#6366f1] text-[#050814] text-xs font-bold shadow-[0_8px_20px_rgba(79,219,200,0.3)] hover:opacity-95 transition-all active:scale-[0.98] disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {isLoading ? (
              <span className="animate-spin text-sm">✦</span>
            ) : (
              <>
                <span>{mode === 'signup' ? 'Create Account & Sync DB' : 'Sign In to Sanctuary'}</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </>
            )}
          </button>
        </form>

        {/* Anonymous / Guest Option */}
        <div className="relative z-10 pt-2 border-t border-white/[0.08] flex flex-col gap-2">
          <button
            type="button"
            id="guest-signin-btn"
            onClick={handleGuestLogin}
            disabled={isLoading}
            className="w-full py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold text-[#c7c4d7] hover:text-white transition-all flex items-center justify-center gap-2 active:scale-98"
          >
            <Radio className="w-3.5 h-3.5 text-[#4fdbc8]" />
            <span>Continue as Guest (No Password Required)</span>
          </button>

          {/* Non-PII & Privacy Sacred Badge */}
          <div className="p-2.5 rounded-xl bg-[#060814]/70 border border-white/[0.06] flex items-start gap-2 text-[10px] text-[#908fa0] leading-relaxed">
            <ShieldCheck className="w-4 h-4 text-[#4fdbc8] shrink-0 mt-0.5" />
            <div>
              <span className="text-[#c7c4d7] font-semibold block">Privacy First (Non-PII Storage)</span>
              Aura stores only non-personally identifiable information (your chosen nickname, avatar emoji, and encrypted journal entries) securely in your private Firestore database.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
