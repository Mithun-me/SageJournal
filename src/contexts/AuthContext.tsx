import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  User,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInAnonymously,
  signOut as firebaseSignOut,
  onAuthStateChanged,
  updateProfile as updateAuthProfile,
} from 'firebase/auth';
import { auth } from '../lib/firebase';
import { UserProfile } from '../types';
import { fetchUserProfile, saveUserProfile } from '../services/dbService';

interface AuthContextType {
  currentUser: User | null;
  userProfile: UserProfile | null;
  loading: boolean;
  signInWithEmail: (email: string, pass: string) => Promise<void>;
  signUpWithEmail: (
    email: string,
    pass: string,
    displayName: string,
    avatarEmoji: string
  ) => Promise<void>;
  signInAsGuest: (displayName?: string, avatarEmoji?: string) => Promise<void>;
  logOut: () => Promise<void>;
  updateProfileData: (updates: Partial<UserProfile>) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const DEFAULT_AVATARS = ['🌿', '🪷', '🌊', '🌙', '✨', '🌸', '🧘', '🍃', '🕯️', '🪐'];

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  // Monitor Firebase Auth state
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      setCurrentUser(user);

      if (user) {
        // Fetch or initialize non-PII Firestore profile
        try {
          let profile = await fetchUserProfile(user.uid);
          if (!profile) {
            // First time profile creation (Non-PII only: nickname & chosen emoji)
            profile = {
              userId: user.uid,
              displayName: user.displayName || 'Mindful Seeker',
              avatarEmoji: '🌿',
              totalPoints: 2450,
              streak: 7,
              lastActiveDate: new Date().toDateString(),
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString(),
            };
            await saveUserProfile(profile);
          }
          setUserProfile(profile);
        } catch (e) {
          console.warn('Error retrieving profile on auth state change:', e);
        }
      } else {
        setUserProfile(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const signInWithEmail = async (email: string, pass: string) => {
    const userCredential = await signInWithEmailAndPassword(auth, email, pass);
    const profile = await fetchUserProfile(userCredential.user.uid);
    if (profile) {
      setUserProfile(profile);
    }
  };

  const signUpWithEmail = async (
    email: string,
    pass: string,
    displayName: string,
    avatarEmoji: string
  ) => {
    const userCredential = await createUserWithEmailAndPassword(auth, email, pass);
    const user = userCredential.user;

    // Set non-PII display name in Firebase Auth
    await updateAuthProfile(user, {
      displayName: displayName.trim() || 'Mindful Soul',
    });

    // Create non-PII Firestore profile document
    const newProfile: UserProfile = {
      userId: user.uid,
      displayName: displayName.trim() || 'Mindful Soul',
      avatarEmoji: avatarEmoji || '🌿',
      totalPoints: 2500, // Welcome points
      streak: 1,
      lastActiveDate: new Date().toDateString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    await saveUserProfile(newProfile);
    setUserProfile(newProfile);
  };

  const signInAsGuest = async (displayName?: string, avatarEmoji?: string) => {
    const userCredential = await signInAnonymously(auth);
    const user = userCredential.user;

    const guestName = displayName || `Guest #${Math.floor(1000 + Math.random() * 9000)}`;
    const guestEmoji = avatarEmoji || DEFAULT_AVATARS[Math.floor(Math.random() * DEFAULT_AVATARS.length)];

    await updateAuthProfile(user, { displayName: guestName });

    const newProfile: UserProfile = {
      userId: user.uid,
      displayName: guestName,
      avatarEmoji: guestEmoji,
      totalPoints: 2450,
      streak: 7,
      lastActiveDate: new Date().toDateString(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    await saveUserProfile(newProfile);
    setUserProfile(newProfile);
  };

  const logOut = async () => {
    await firebaseSignOut(auth);
    setCurrentUser(null);
    setUserProfile(null);
  };

  const updateProfileData = async (updates: Partial<UserProfile>) => {
    if (!currentUser || !userProfile) return;

    const updated = {
      ...userProfile,
      ...updates,
      updatedAt: new Date().toISOString(),
    };

    setUserProfile(updated);
    await saveUserProfile(updated);
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        userProfile,
        loading,
        signInWithEmail,
        signUpWithEmail,
        signInAsGuest,
        logOut,
        updateProfileData,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
