import React, { useState, useEffect } from 'react';
import { JournalEntry, TabType, AppTheme, MoodType } from './types';
import { INITIAL_ENTRIES, INITIAL_MILESTONES, INITIAL_WEEK_TRENDS } from './data/initialData';
import { LiquidShaderCanvas } from './components/LiquidShaderCanvas';
import { TopAppBar } from './components/TopAppBar';
import { BottomNavBar } from './components/BottomNavBar';
import { HomeScreen } from './components/screens/HomeScreen';
import { ArchiveScreen } from './components/screens/ArchiveScreen';
import { SanctuaryScreen } from './components/screens/SanctuaryScreen';
import { TrendsScreen } from './components/screens/TrendsScreen';
import { SettingsScreen } from './components/screens/SettingsScreen';
import { NewEntryModal } from './components/modals/NewEntryModal';
import { EntryDetailModal } from './components/modals/EntryDetailModal';
import { ProfileModal } from './components/modals/ProfileModal';

export default function App() {
  const [activeTab, setActiveTab] = useState<TabType>('home');
  const [theme, setTheme] = useState<AppTheme>('liquid-glass');
  const [entries, setEntries] = useState<JournalEntry[]>(() => {
    try {
      const saved = localStorage.getItem('aura_entries_v1');
      return saved ? JSON.parse(saved) : INITIAL_ENTRIES;
    } catch {
      return INITIAL_ENTRIES;
    }
  });
  const [totalPoints, setTotalPoints] = useState<number>(() => {
    try {
      const saved = localStorage.getItem('aura_points');
      return saved ? parseInt(saved, 10) : 2450;
    } catch {
      return 2450;
    }
  });
  const [streak, setStreak] = useState<number>(7);
  const [selectedMood, setSelectedMood] = useState<MoodType | null>('Calm');

  // Shader Controls
  const [shaderSpeed, setShaderSpeed] = useState<number>(1.0);
  const [shaderIntensity, setShaderIntensity] = useState<number>(1.0);

  // Modals
  const [isNewEntryOpen, setIsNewEntryOpen] = useState(false);
  const [initialPrompt, setInitialPrompt] = useState<string | undefined>(undefined);
  const [initialContent, setInitialContent] = useState<string | undefined>(undefined);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [viewingEntry, setViewingEntry] = useState<JournalEntry | null>(null);

  const handleOpenNewEntryWithQuote = (quote: string, author: string) => {
    setInitialPrompt(`How does this quote resonate with your current moment?`);
    setInitialContent(`"${quote}" — ${author}\n\n`);
    setIsNewEntryOpen(true);
  };

  const handleOpenStandardNewEntry = () => {
    setInitialPrompt(undefined);
    setInitialContent(undefined);
    setIsNewEntryOpen(true);
  };

  // Save entries to localStorage
  useEffect(() => {
    try {
      localStorage.setItem('aura_entries_v1', JSON.stringify(entries));
    } catch (e) {
      console.warn('Storage error:', e);
    }
  }, [entries]);

  // Save points
  useEffect(() => {
    try {
      localStorage.setItem('aura_points', totalPoints.toString());
    } catch (e) {
      console.warn('Storage error:', e);
    }
  }, [totalPoints]);

  const handleToggleTheme = () => {
    setTheme((prev) => (prev === 'liquid-glass' ? 'deep-sea' : 'liquid-glass'));
  };

  const handleSaveEntry = (entryData: Omit<JournalEntry, 'id' | 'timestamp'>) => {
    const newEntry: JournalEntry = {
      ...entryData,
      id: `entry-${Date.now()}`,
      timestamp: Date.now(),
    };

    setEntries((prev) => [newEntry, ...prev]);
    setTotalPoints((prev) => prev + 50);
  };

  const handleToggleFavorite = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    setEntries((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, isFavorite: !item.isFavorite } : item
      )
    );
  };

  const handleDeleteEntry = (id: string) => {
    setEntries((prev) => prev.filter((item) => item.id !== id));
  };

  const handleResetData = () => {
    setEntries(INITIAL_ENTRIES);
    setTotalPoints(2450);
    setStreak(7);
    localStorage.removeItem('aura_entries_v1');
    localStorage.removeItem('aura_points');
  };

  return (
    <div className={`min-h-screen relative text-[#e1e1f6] ${theme === 'deep-sea' ? 'theme-deep-sea' : ''}`}>
      {/* Real-time WebGL Animated Liquid Canvas */}
      <LiquidShaderCanvas
        theme={theme}
        speed={shaderSpeed}
        intensity={shaderIntensity}
      />

      {/* Top App Bar Header */}
      <TopAppBar
        streak={streak}
        theme={theme}
        onToggleTheme={handleToggleTheme}
        onOpenProfile={() => setIsProfileOpen(true)}
        onOpenNewEntry={handleOpenStandardNewEntry}
      />

      {/* Main Content Area */}
      <main className="pt-24 pb-28 px-4 sm:px-6 relative z-10">
        {activeTab === 'home' && (
          <HomeScreen
            entries={entries}
            selectedMood={selectedMood}
            onSelectMood={(m) => {
              setSelectedMood(m);
              setTotalPoints((p) => p + 10);
            }}
            onOpenNewEntry={handleOpenStandardNewEntry}
            onReflectWithQuote={handleOpenNewEntryWithQuote}
            onViewEntry={(entry) => setViewingEntry(entry)}
            onToggleFavorite={handleToggleFavorite}
            onViewAll={() => setActiveTab('archive')}
            streak={streak}
          />
        )}

        {activeTab === 'archive' && (
          <ArchiveScreen
            entries={entries}
            onViewEntry={(entry) => setViewingEntry(entry)}
            onOpenNewEntry={handleOpenStandardNewEntry}
            onToggleFavorite={handleToggleFavorite}
          />
        )}

        {activeTab === 'sanctuary' && <SanctuaryScreen />}

        {activeTab === 'trends' && (
          <TrendsScreen
            totalPoints={totalPoints}
            streak={streak}
            milestones={INITIAL_MILESTONES}
            weekTrends={INITIAL_WEEK_TRENDS}
          />
        )}

        {activeTab === 'settings' && (
          <SettingsScreen
            theme={theme}
            onSelectTheme={setTheme}
            entries={entries}
            onResetData={handleResetData}
            shaderSpeed={shaderSpeed}
            onSetShaderSpeed={setShaderSpeed}
            shaderIntensity={shaderIntensity}
            onSetShaderIntensity={setShaderIntensity}
          />
        )}
      </main>

      {/* 5-Item Liquid Floating Bottom Navbar */}
      <BottomNavBar
        activeTab={activeTab}
        onSelectTab={setActiveTab}
        onOpenNewEntry={handleOpenStandardNewEntry}
      />

      {/* Modals */}
      <NewEntryModal
        isOpen={isNewEntryOpen}
        onClose={() => setIsNewEntryOpen(false)}
        onSave={handleSaveEntry}
        currentMood={selectedMood || 'Calm'}
        initialPrompt={initialPrompt}
        initialContent={initialContent}
      />

      <EntryDetailModal
        entry={viewingEntry}
        onClose={() => setViewingEntry(null)}
        onToggleFavorite={handleToggleFavorite}
        onDeleteEntry={handleDeleteEntry}
      />

      <ProfileModal
        isOpen={isProfileOpen}
        onClose={() => setIsProfileOpen(false)}
        streak={streak}
        totalPoints={totalPoints}
        entries={entries}
        onOpenSettings={() => setActiveTab('settings')}
      />
    </div>
  );
}
