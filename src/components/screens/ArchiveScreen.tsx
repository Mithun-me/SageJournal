import React, { useState, useMemo } from 'react';
import { JournalEntry, MoodType } from '../../types';
import { Search, Volume2, Mic, Heart, MapPin, Smile, LayoutGrid, List, Calendar, X, Filter, Sparkles } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';

interface ArchiveScreenProps {
  entries: JournalEntry[];
  onViewEntry: (entry: JournalEntry) => void;
  onOpenNewEntry: () => void;
  onToggleFavorite: (id: string, e: React.MouseEvent) => void;
}

export const ArchiveScreen: React.FC<ArchiveScreenProps> = ({
  entries,
  onViewEntry,
  onOpenNewEntry,
  onToggleFavorite,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDate, setSelectedDate] = useState<string>(''); // YYYY-MM-DD
  const [showDatePicker, setShowDatePicker] = useState<boolean>(false);
  const [activeFilter, setActiveFilter] = useState<'all' | 'moods' | 'audio' | 'favorites'>('all');
  const [selectedMoodFilter, setSelectedMoodFilter] = useState<MoodType | 'all'>('all');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [playingAudioId, setPlayingAudioId] = useState<string | null>(null);

  const filteredEntries = useMemo(() => {
    return entries.filter((entry) => {
      // 1. Keyword search against title, content, tags, location, date text, reflection, affirmation, mood
      if (searchQuery.trim() !== '') {
        const query = searchQuery.toLowerCase().trim();
        const entryDate = entry.date ? entry.date.toLowerCase() : '';
        const entryTime = entry.time ? entry.time.toLowerCase() : '';
        
        // Also check if timestamp matches month name or full year/month
        let fullDateString = '';
        if (entry.timestamp) {
          const d = new Date(entry.timestamp);
          fullDateString = `${d.toLocaleString('en-US', { month: 'long' })} ${d.getDate()} ${d.getFullYear()} ${d.toLocaleDateString()}`.toLowerCase();
        }

        const matchesKeyword =
          entry.title.toLowerCase().includes(query) ||
          entry.content.toLowerCase().includes(query) ||
          entryDate.includes(query) ||
          entryTime.includes(query) ||
          fullDateString.includes(query) ||
          entry.mood.toLowerCase().includes(query) ||
          entry.tags?.some((t) => t.toLowerCase().includes(query)) ||
          entry.location?.toLowerCase().includes(query) ||
          entry.aiReflection?.toLowerCase().includes(query) ||
          entry.aiAffirmation?.toLowerCase().includes(query);

        if (!matchesKeyword) return false;
      }

      // 2. Specific Date Picker Filter (YYYY-MM-DD)
      if (selectedDate) {
        if (entry.timestamp) {
          const entryDateObj = new Date(entry.timestamp);
          const entryYear = entryDateObj.getFullYear();
          const entryMonth = String(entryDateObj.getMonth() + 1).padStart(2, '0');
          const entryDay = String(entryDateObj.getDate()).padStart(2, '0');
          const entryDateStr = `${entryYear}-${entryMonth}-${entryDay}`;
          if (entryDateStr !== selectedDate) return false;
        } else {
          // If no timestamp, try parsing date string
          return false;
        }
      }

      // 3. Tab Categories
      if (activeFilter === 'favorites' && !entry.isFavorite) return false;
      if (activeFilter === 'audio' && !entry.isAudio) return false;
      if (activeFilter === 'moods' && selectedMoodFilter !== 'all' && entry.mood !== selectedMoodFilter) return false;

      return true;
    });
  }, [entries, searchQuery, selectedDate, activeFilter, selectedMoodFilter]);

  const handleAudioToggle = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    triggerHaptic('light');
    if (playingAudioId === id) {
      setPlayingAudioId(null);
    } else {
      setPlayingAudioId(id);
      setTimeout(() => {
        setPlayingAudioId((curr) => (curr === id ? null : curr));
      }, 5000);
    }
  };

  const handleClearAllFilters = () => {
    triggerHaptic('light');
    setSearchQuery('');
    setSelectedDate('');
    setActiveFilter('all');
    setSelectedMoodFilter('all');
  };

  const hasActiveFilters = searchQuery.trim() !== '' || selectedDate !== '' || activeFilter !== 'all';

  return (
    <div className="w-full max-w-2xl mx-auto space-y-4 animate-fadeIn pb-16">
      {/* Sticky Search & Filter Controls */}
      <section className="space-y-3 sticky top-[60px] z-30 bg-[#0a0c1a]/95 backdrop-blur-xl py-2 px-1 rounded-b-xl border-b border-white/[0.08]">
        {/* Search Bar, Date Picker Button & View Mode Toggle */}
        <div className="flex items-center gap-2">
          {/* Main Keyword / Date Search Input */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-[#4fdbc8]" />
            <input
              id="archive-search-input"
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by keyword, mood, or date (e.g. 'Oct 24', 'calm')..."
              className="w-full bg-[#0e1022] border border-white/10 rounded-xl py-2 pl-9 pr-8 text-xs text-[#e1e1f6] placeholder-[#908fa0] focus:outline-none focus:border-[#4fdbc8] focus:ring-1 focus:ring-[#4fdbc8]/30 transition-all font-['Manrope']"
            />
            {searchQuery && (
              <button
                onClick={() => {
                  triggerHaptic('light');
                  setSearchQuery('');
                }}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[10px] text-[#908fa0] hover:text-white bg-white/10 p-1 rounded-md"
                title="Clear Search"
              >
                <X className="w-3 h-3" />
              </button>
            )}
          </div>

          {/* Date Picker Filter Toggle */}
          <div className="relative">
            <button
              id="archive-date-filter-btn"
              onClick={() => {
                triggerHaptic('light');
                setShowDatePicker(!showDatePicker);
              }}
              title="Filter by Specific Date"
              className={`p-2 rounded-xl border transition-all flex items-center gap-1 text-xs ${
                selectedDate
                  ? 'bg-[#4fdbc8]/20 text-[#71f8e4] border-[#4fdbc8]/50'
                  : showDatePicker
                  ? 'bg-white/15 text-white border-white/20'
                  : 'bg-[#0e1022] hover:bg-white/5 text-[#908fa0] border-white/10'
              }`}
            >
              <Calendar className="w-4 h-4" />
            </button>
          </div>

          {/* Grid / List View Toggle */}
          <div className="flex items-center bg-[#0e1022] border border-white/10 rounded-xl p-0.5">
            <button
              id="archive-view-grid-btn"
              onClick={() => {
                triggerHaptic('light');
                setViewMode('grid');
              }}
              title="Grid View"
              className={`p-1.5 rounded-lg transition-all ${
                viewMode === 'grid' ? 'bg-[#4fdbc8]/20 text-[#4fdbc8]' : 'text-[#908fa0]'
              }`}
            >
              <LayoutGrid className="w-4 h-4" />
            </button>
            <button
              id="archive-view-list-btn"
              onClick={() => {
                triggerHaptic('light');
                setViewMode('list');
              }}
              title="List View"
              className={`p-1.5 rounded-lg transition-all ${
                viewMode === 'list' ? 'bg-[#4fdbc8]/20 text-[#4fdbc8]' : 'text-[#908fa0]'
              }`}
            >
              <List className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Date Picker Expanded Panel */}
        {showDatePicker && (
          <div className="p-3 rounded-xl bg-[#0e1022] border border-white/10 flex flex-wrap items-center justify-between gap-2 text-xs animate-fadeIn">
            <div className="flex items-center gap-2">
              <Calendar className="w-3.5 h-3.5 text-[#4fdbc8]" />
              <span className="text-[#c7c4d7] font-medium">Filter by Date:</span>
              <input
                id="archive-date-input"
                type="date"
                value={selectedDate}
                onChange={(e) => {
                  triggerHaptic('light');
                  setSelectedDate(e.target.value);
                }}
                className="bg-[#14182e] text-[#e1e1f6] border border-white/15 rounded-lg px-2.5 py-1 text-xs focus:outline-none focus:border-[#4fdbc8]"
              />
            </div>
            {selectedDate && (
              <button
                onClick={() => {
                  triggerHaptic('light');
                  setSelectedDate('');
                }}
                className="text-[11px] text-[#ffb783] hover:underline"
              >
                Clear Date
              </button>
            )}
          </div>
        )}

        {/* Filter Chips Bar */}
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar pb-0.5 text-xs">
          <button
            id="filter-all"
            onClick={() => {
              triggerHaptic('light');
              setActiveFilter('all');
              setSelectedMoodFilter('all');
            }}
            className={`whitespace-nowrap px-3 py-1.5 rounded-full font-medium transition-all ${
              activeFilter === 'all'
                ? 'bg-[#4fdbc8] text-[#003731] font-bold'
                : 'bg-white/5 text-[#c7c4d7] border border-white/[0.08]'
            }`}
          >
            All ({entries.length})
          </button>

          <button
            id="filter-favorites"
            onClick={() => {
              triggerHaptic('light');
              setActiveFilter(activeFilter === 'favorites' ? 'all' : 'favorites');
            }}
            className={`whitespace-nowrap px-3 py-1.5 rounded-full font-medium flex items-center gap-1 transition-all ${
              activeFilter === 'favorites'
                ? 'bg-[#ffb783] text-[#331c00] font-bold'
                : 'bg-white/5 text-[#c7c4d7] border border-white/[0.08]'
            }`}
          >
            <Heart className="w-3 h-3" /> Favorites
          </button>

          <button
            id="filter-audio"
            onClick={() => {
              triggerHaptic('light');
              setActiveFilter(activeFilter === 'audio' ? 'all' : 'audio');
            }}
            className={`whitespace-nowrap px-3 py-1.5 rounded-full font-medium flex items-center gap-1 transition-all ${
              activeFilter === 'audio'
                ? 'bg-[#8083ff] text-white font-bold'
                : 'bg-white/5 text-[#c7c4d7] border border-white/[0.08]'
            }`}
          >
            <Mic className="w-3 h-3" /> Voice Notes
          </button>

          <button
            id="filter-moods"
            onClick={() => {
              triggerHaptic('light');
              setActiveFilter(activeFilter === 'moods' ? 'all' : 'moods');
            }}
            className={`whitespace-nowrap px-3 py-1.5 rounded-full font-medium flex items-center gap-1 transition-all ${
              activeFilter === 'moods'
                ? 'bg-[#4fdbc8] text-[#003731] font-bold'
                : 'bg-white/5 text-[#c7c4d7] border border-white/[0.08]'
            }`}
          >
            <Smile className="w-3 h-3" /> Moods
          </button>

          {hasActiveFilters && (
            <button
              onClick={handleClearAllFilters}
              className="whitespace-nowrap px-2.5 py-1 rounded-full text-[11px] text-[#ffb783] hover:bg-white/5 border border-[#ffb783]/30 transition-all flex items-center gap-1 ml-auto"
            >
              <X className="w-3 h-3" /> Reset
            </button>
          )}
        </div>

        {/* Sub-Filter for Moods */}
        {activeFilter === 'moods' && (
          <div className="flex gap-1.5 pt-1 border-t border-white/[0.08] overflow-x-auto no-scrollbar">
            {(['all', 'Calm', 'Joy', 'Reflective', 'Low'] as (MoodType | 'all')[]).map((mood) => (
              <button
                key={mood}
                onClick={() => {
                  triggerHaptic('light');
                  setSelectedMoodFilter(mood);
                }}
                className={`text-[11px] px-2.5 py-1 rounded-full border transition-all ${
                  selectedMoodFilter === mood
                    ? 'bg-[#4fdbc8] text-[#003731] font-bold border-[#4fdbc8]'
                    : 'bg-white/5 text-[#c7c4d7] border-white/[0.08]'
                }`}
              >
                {mood === 'all' ? 'All Moods' : mood}
              </button>
            ))}
          </div>
        )}

        {/* Results summary header when filtering */}
        {hasActiveFilters && (
          <div className="flex items-center justify-between text-[11px] text-[#908fa0] px-1 pt-0.5">
            <span>
              Showing {filteredEntries.length} of {entries.length} memories
              {searchQuery && ` for "${searchQuery}"`}
              {selectedDate && ` on ${selectedDate}`}
            </span>
          </div>
        )}
      </section>

      {/* Grid or List View */}
      {filteredEntries.length === 0 ? (
        <div className="p-8 text-center bg-[#0e1022]/60 rounded-2xl border border-white/[0.08] space-y-2">
          <p className="text-sm font-semibold text-[#e1e1f6]">No memories found</p>
          <p className="text-xs text-[#908fa0]">
            {hasActiveFilters
              ? 'Try changing your search keywords or clearing date/mood filters.'
              : 'Start journaling to create your first archive reflection.'}
          </p>
          <div className="flex items-center justify-center gap-2 pt-2">
            {hasActiveFilters && (
              <button
                onClick={handleClearAllFilters}
                className="px-3.5 py-2 rounded-xl bg-white/10 hover:bg-white/15 text-white font-medium text-xs transition-all"
              >
                Clear Search &amp; Filters
              </button>
            )}
            <button
              onClick={() => {
                triggerHaptic('medium');
                onOpenNewEntry();
              }}
              className="px-4 py-2 rounded-xl bg-[#4fdbc8] text-[#003731] font-bold text-xs"
            >
              Create New Memory
            </button>
          </div>
        </div>
      ) : viewMode === 'grid' ? (
        <section className="grid grid-cols-2 gap-3">
          {filteredEntries.map((entry) => {
            const isPlaying = playingAudioId === entry.id;

            return (
              <article
                key={entry.id}
                id={`archive-card-${entry.id}`}
                onClick={() => {
                  triggerHaptic('light');
                  onViewEntry(entry);
                }}
                className="bg-[#0e1022]/80 rounded-2xl overflow-hidden border border-white/[0.08] hover:border-white/20 transition-all cursor-pointer flex flex-col justify-between group active:scale-[0.98]"
              >
                {/* Image Cover if available */}
                {entry.imageUrl ? (
                  <div className="h-28 w-full relative overflow-hidden bg-black/40">
                    <img
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      src={entry.imageUrl}
                      alt={entry.title}
                    />
                    <div className="absolute top-2 right-2">
                      <span className="w-6 h-6 rounded-full bg-black/60 backdrop-blur-md flex items-center justify-center text-xs">
                        {entry.moodEmoji || '😌'}
                      </span>
                    </div>
                  </div>
                ) : (
                  <div className="p-3 pb-0 flex justify-between items-center">
                    <span className="text-xl">{entry.moodEmoji || '✨'}</span>
                    {entry.isFavorite && (
                      <Heart className="w-3.5 h-3.5 fill-[#ffb783] text-[#ffb783]" />
                    )}
                  </div>
                )}

                {/* Body Content */}
                <div className="p-3 flex-1 flex flex-col justify-between">
                  <div>
                    <span className="text-[10px] font-semibold text-[#4fdbc8] tracking-tight block mb-0.5">
                      {entry.date} {entry.time && `· ${entry.time}`}
                    </span>
                    <h4 className="text-xs font-bold text-white line-clamp-1 mb-1 font-['Manrope']">
                      {entry.title}
                    </h4>
                    <p className="text-[11px] text-[#908fa0] line-clamp-2 leading-relaxed">
                      {entry.content}
                    </p>
                  </div>

                  {entry.isAudio && (
                    <div className="mt-2 pt-2 border-t border-white/[0.06] flex items-center justify-between">
                      <button
                        onClick={(e) => handleAudioToggle(entry.id, e)}
                        className="flex items-center gap-1 text-[10px] font-semibold text-[#4fdbc8] bg-[#4fdbc8]/15 px-2 py-0.5 rounded-full"
                      >
                        <Volume2 className={`w-3 h-3 ${isPlaying ? 'animate-bounce' : ''}`} />
                        {isPlaying ? 'Playing' : entry.audioDuration || '0:45'}
                      </button>
                    </div>
                  )}
                </div>
              </article>
            );
          })}
        </section>
      ) : (
        /* List Mode */
        <section className="space-y-2.5">
          {filteredEntries.map((entry) => (
            <article
              key={entry.id}
              id={`archive-list-item-${entry.id}`}
              onClick={() => {
                triggerHaptic('light');
                onViewEntry(entry);
              }}
              className="bg-[#0e1022]/80 rounded-xl p-3 border border-white/[0.08] hover:border-white/20 transition-all cursor-pointer flex items-center gap-3 active:scale-[0.99]"
            >
              {entry.imageUrl ? (
                <div className="w-12 h-12 rounded-lg overflow-hidden shrink-0 border border-white/10">
                  <img className="w-full h-full object-cover" src={entry.imageUrl} alt={entry.title} />
                </div>
              ) : (
                <div className="w-12 h-12 rounded-lg bg-white/5 border border-white/[0.08] flex items-center justify-center shrink-0 text-xl">
                  {entry.moodEmoji || '📝'}
                </div>
              )}

              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <h4 className="text-xs font-bold text-white truncate font-['Manrope']">
                    {entry.title}
                  </h4>
                  <span className="text-[10px] text-[#908fa0] shrink-0 ml-2">
                    {entry.date} {entry.time && `· ${entry.time}`}
                  </span>
                </div>
                <p className="text-[11px] text-[#908fa0] line-clamp-1 mt-0.5">
                  {entry.content}
                </p>
              </div>

              {entry.isFavorite && (
                <Heart className="w-3.5 h-3.5 fill-[#ffb783] text-[#ffb783] shrink-0" />
              )}
            </article>
          ))}
        </section>
      )}
    </div>
  );
};

