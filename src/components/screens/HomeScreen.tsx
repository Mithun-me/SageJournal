import React, { useState } from 'react';
import { JournalEntry, MoodType } from '../../types';
import { Heart, Sparkles, Plus, Check } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';
import { DailyAffirmationWidget } from '../widgets/DailyAffirmationWidget';

interface HomeScreenProps {
  entries: JournalEntry[];
  selectedMood: MoodType | null;
  onSelectMood: (mood: MoodType) => void;
  onOpenNewEntry: () => void;
  onReflectWithQuote?: (quote: string, author: string) => void;
  onViewEntry: (entry: JournalEntry) => void;
  onToggleFavorite: (id: string, e: React.MouseEvent) => void;
  onViewAll: () => void;
  streak: number;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  entries,
  selectedMood,
  onSelectMood,
  onOpenNewEntry,
  onReflectWithQuote,
  onViewEntry,
  onToggleFavorite,
  onViewAll,
  streak,
}) => {
  const [completedCheckins, setCompletedCheckins] = useState(3);
  const [justCheckedIn, setJustCheckedIn] = useState(false);

  const moods: { type: MoodType; emoji: string; label: string; accentColor: string }[] = [
    { type: 'Calm', emoji: '😌', label: 'Calm', accentColor: 'text-[#4fdbc8]' },
    { type: 'Joy', emoji: '✨', label: 'Joy', accentColor: 'text-[#ffb783]' },
    { type: 'Reflective', emoji: '🌿', label: 'Reflective', accentColor: 'text-[#8083ff]' },
    { type: 'Low', emoji: '🌧️', label: 'Low', accentColor: 'text-[#93c5fd]' },
  ];

  const handleMoodClick = (mood: MoodType) => {
    triggerHaptic('medium');
    onSelectMood(mood);
    if (completedCheckins < 4) {
      setCompletedCheckins((prev) => Math.min(prev + 1, 4));
    }
    setJustCheckedIn(true);
    setTimeout(() => setJustCheckedIn(false), 2200);
  };

  const recentEntries = entries.slice(0, 3);
  const progressPercent = (completedCheckins / 4) * 100;
  const strokeDash = `${progressPercent * 0.75}, 100`;

  return (
    <div className="w-full max-w-2xl mx-auto space-y-6 animate-fadeIn pb-12">
      {/* Daily Affirmation Liquid-Glass Widget */}
      <DailyAffirmationWidget
        currentMood={selectedMood}
        onReflectWithQuote={onReflectWithQuote}
      />

      {/* Daily Mood Check-in */}
      <section className="space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-[#e1e1f6] font-['Manrope']">
            How are you feeling today?
          </h2>
          {justCheckedIn && (
            <span className="text-xs font-semibold text-[#4fdbc8] bg-[#4fdbc8]/10 px-2.5 py-1 rounded-full border border-[#4fdbc8]/30 flex items-center gap-1">
              <Check className="w-3 h-3" /> Logged
            </span>
          )}
        </div>

        <div className="grid grid-cols-4 gap-2.5 sm:gap-3">
          {moods.map((m) => {
            const isSelected = selectedMood === m.type;
            return (
              <button
                key={m.type}
                id={`mood-btn-${m.type.toLowerCase()}`}
                onClick={() => handleMoodClick(m.type)}
                className={`py-3 px-2 flex flex-col items-center justify-center gap-1.5 rounded-2xl border transition-all duration-200 active:scale-95 ${
                  isSelected
                    ? 'bg-[#1b1e36] border-[#4fdbc8] shadow-sm text-white'
                    : 'bg-[#0e1022]/70 border-white/[0.08] hover:border-white/20 text-[#c7c4d7]'
                }`}
              >
                <span className="text-2xl sm:text-3xl transition-transform">
                  {m.emoji}
                </span>
                <span className="text-[11px] font-bold tracking-tight font-['Manrope']">
                  {m.label}
                </span>
              </button>
            );
          })}
        </div>
      </section>

      {/* Daily Goal Card */}
      <section
        id="daily-goal-card"
        className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-5 border border-white/[0.08] flex items-center justify-between relative overflow-hidden transition-all duration-200 hover:border-white/15"
      >
        <div className="flex items-center gap-4 relative z-10">
          {/* Progress Ring */}
          <div className="relative w-14 h-14 shrink-0">
            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
              <path
                className="text-white/10"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="currentColor"
                strokeLinecap="round"
                strokeWidth="3"
              />
              <path
                className="text-[#4fdbc8] transition-all duration-500 ease-out"
                d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                fill="none"
                stroke="currentColor"
                strokeDasharray={strokeDash}
                strokeLinecap="round"
                strokeWidth="3"
              />
            </svg>
            <div className="absolute inset-0 flex items-center justify-center text-[#4fdbc8]">
              <span className="text-xs font-bold font-['Manrope']">{Math.round(progressPercent)}%</span>
            </div>
          </div>

          <div>
            <h3 className="text-base font-bold text-[#e1e1f6] font-['Manrope']">
              Daily Mindfulness Goal
            </h3>
            <p className="text-xs text-[#908fa0]">
              {completedCheckins} of 4 mindful check-ins completed
            </p>
          </div>
        </div>

        {/* Action Button */}
        <div className="flex items-center gap-2 relative z-10">
          {completedCheckins < 4 ? (
            <button
              onClick={() => {
                triggerHaptic('light');
                setCompletedCheckins((prev) => Math.min(prev + 1, 4));
              }}
              className="text-xs font-semibold px-3 py-1.5 rounded-full bg-[#4fdbc8]/15 text-[#71f8e4] hover:bg-[#4fdbc8]/25 transition-all active:scale-95 border border-[#4fdbc8]/30"
            >
              + Log
            </button>
          ) : (
            <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-[#005149] text-[#71f8e4] border border-[#4fdbc8]/40">
              Completed ✨
            </span>
          )}
        </div>
      </section>

      {/* Recent Entries Feed */}
      <section className="space-y-3">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold tracking-tight text-[#e1e1f6] font-['Manrope']">
            Recent Entries
          </h2>
          <button
            id="view-all-entries-btn"
            onClick={() => {
              triggerHaptic('light');
              onViewAll();
            }}
            className="text-xs font-bold text-[#4fdbc8] hover:text-[#71f8e4] transition-colors"
          >
            View All ({entries.length})
          </button>
        </div>

        <div className="space-y-3">
          {recentEntries.map((entry) => (
            <article
              key={entry.id}
              id={`recent-entry-${entry.id}`}
              onClick={() => {
                triggerHaptic('light');
                onViewEntry(entry);
              }}
              className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-4 border border-white/[0.08] hover:border-white/20 transition-all duration-200 flex gap-3.5 items-start cursor-pointer active:scale-[0.99]"
            >
              {/* Thumbnail or Mood Indicator */}
              {entry.imageUrl ? (
                <div className="w-16 h-16 rounded-xl overflow-hidden shrink-0 border border-white/10">
                  <img
                    className="w-full h-full object-cover"
                    src={entry.imageUrl}
                    alt={entry.title}
                  />
                </div>
              ) : (
                <div className="w-16 h-16 rounded-xl bg-white/5 border border-white/[0.08] flex items-center justify-center shrink-0 text-2xl">
                  {entry.moodEmoji || '✨'}
                </div>
              )}

              {/* Text Information */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between mb-1">
                  <span className="text-[11px] font-semibold text-[#4fdbc8] tracking-tight">
                    {entry.date} · {entry.time}
                  </span>
                  <button
                    onClick={(e) => {
                      triggerHaptic('light');
                      onToggleFavorite(entry.id, e);
                    }}
                    title={entry.isFavorite ? 'Remove Favorite' : 'Save Favorite'}
                    className="p-1 text-[#908fa0] hover:text-[#ffb783] transition-colors"
                  >
                    <Heart
                      className={`w-3.5 h-3.5 ${
                        entry.isFavorite ? 'fill-[#ffb783] text-[#ffb783]' : ''
                      }`}
                    />
                  </button>
                </div>

                <h4 className="text-sm font-semibold text-[#e1e1f6] mb-1 truncate font-['Manrope']">
                  {entry.title}
                </h4>

                <p className="text-xs text-[#908fa0] line-clamp-2 leading-relaxed">
                  {entry.content}
                </p>
              </div>
            </article>
          ))}
        </div>
      </section>

      {/* Quick Journal Prompt Banner */}
      <section className="p-4 rounded-2xl bg-gradient-to-r from-[#171933] to-[#0f2425] border border-white/10 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="p-2 rounded-xl bg-[#4fdbc8]/15 text-[#4fdbc8]">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <h4 className="text-xs sm:text-sm font-bold text-white font-['Manrope']">
              Pause for a mindful breath
            </h4>
            <p className="text-[11px] text-[#908fa0]">
              Write your thoughts and receive guided reflections.
            </p>
          </div>
        </div>
        <button
          id="home-create-entry-btn"
          onClick={() => {
            triggerHaptic('medium');
            onOpenNewEntry();
          }}
          className="px-3.5 py-2 rounded-xl bg-[#4fdbc8] text-[#003731] font-bold text-xs tracking-tight shadow hover:bg-[#71f8e4] active:scale-95 transition-all flex items-center gap-1.5 shrink-0"
        >
          <Plus className="w-3.5 h-3.5" /> Write
        </button>
      </section>
    </div>
  );
};
