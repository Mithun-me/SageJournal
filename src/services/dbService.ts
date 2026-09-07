import {
  collection,
  doc,
  getDoc,
  getDocs,
  setDoc,
  deleteDoc,
  updateDoc,
  query,
  orderBy,
  onSnapshot,
} from 'firebase/firestore';
import { db } from '../lib/firebase';
import { JournalEntry, UserProfile } from '../types';

/**
 * Fetch Non-PII User Profile from Firestore
 */
export async function fetchUserProfile(userId: string): Promise<UserProfile | null> {
  try {
    const userDocRef = doc(db, 'users', userId);
    const snap = await getDoc(userDocRef);
    if (snap.exists()) {
      return snap.data() as UserProfile;
    }
    return null;
  } catch (error) {
    console.warn('Error fetching user profile from Firestore:', error);
    return null;
  }
}

/**
 * Save or Update Non-PII User Profile in Firestore
 */
export async function saveUserProfile(profile: UserProfile): Promise<void> {
  try {
    const userDocRef = doc(db, 'users', profile.userId);
    await setDoc(
      userDocRef,
      {
        ...profile,
        updatedAt: new Date().toISOString(),
      },
      { merge: true }
    );
  } catch (error) {
    console.warn('Error saving user profile to Firestore:', error);
  }
}

/**
 * Fetch all Journal Entries for a User
 */
export async function fetchUserEntries(userId: string): Promise<JournalEntry[]> {
  try {
    const entriesRef = collection(db, 'users', userId, 'entries');
    const q = query(entriesRef, orderBy('timestamp', 'desc'));
    const snapshot = await getDocs(q);

    const results: JournalEntry[] = [];
    snapshot.forEach((docSnap) => {
      results.push(docSnap.data() as JournalEntry);
    });
    return results;
  } catch (error) {
    console.warn('Error fetching user entries from Firestore:', error);
    return [];
  }
}

/**
 * Subscribe to real-time updates of User's Journal Entries
 */
export function subscribeToUserEntries(
  userId: string,
  onUpdate: (entries: JournalEntry[]) => void,
  onError?: (err: any) => void
): () => void {
  try {
    const entriesRef = collection(db, 'users', userId, 'entries');
    const q = query(entriesRef, orderBy('timestamp', 'desc'));

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const entries: JournalEntry[] = [];
        snapshot.forEach((docSnap) => {
          entries.push(docSnap.data() as JournalEntry);
        });
        onUpdate(entries);
      },
      (error) => {
        console.warn('Firestore snapshot error on entries:', error);
        if (onError) onError(error);
      }
    );

    return unsubscribe;
  } catch (error) {
    console.warn('Error setting up entries snapshot:', error);
    return () => {};
  }
}

/**
 * Save a single Journal Entry to Firestore
 */
export async function saveUserEntry(userId: string, entry: JournalEntry): Promise<void> {
  try {
    const entryDocRef = doc(db, 'users', userId, 'entries', entry.id);
    await setDoc(entryDocRef, {
      ...entry,
      userId,
      updatedAt: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Error saving entry to Firestore:', error);
    throw error;
  }
}

/**
 * Update an existing Journal Entry (e.g., toggle favorite)
 */
export async function updateUserEntry(
  userId: string,
  entryId: string,
  updates: Partial<JournalEntry>
): Promise<void> {
  try {
    const entryDocRef = doc(db, 'users', userId, 'entries', entryId);
    await updateDoc(entryDocRef, {
      ...updates,
      updatedAt: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Error updating entry in Firestore:', error);
    throw error;
  }
}

/**
 * Delete an entry from Firestore
 */
export async function deleteUserEntry(userId: string, entryId: string): Promise<void> {
  try {
    const entryDocRef = doc(db, 'users', userId, 'entries', entryId);
    await deleteDoc(entryDocRef);
  } catch (error) {
    console.error('Error deleting entry in Firestore:', error);
    throw error;
  }
}

/**
 * Batch seed local entries to user's Firestore cloud storage upon sign-up / first sign-in
 */
export async function seedLocalEntriesToFirestore(
  userId: string,
  localEntries: JournalEntry[]
): Promise<void> {
  try {
    for (const entry of localEntries) {
      const entryDocRef = doc(db, 'users', userId, 'entries', entry.id);
      await setDoc(entryDocRef, {
        ...entry,
        userId,
        createdAt: new Date(entry.timestamp || Date.now()).toISOString(),
        updatedAt: new Date().toISOString(),
      });
    }
  } catch (error) {
    console.warn('Error seeding initial entries to Firestore:', error);
  }
}
