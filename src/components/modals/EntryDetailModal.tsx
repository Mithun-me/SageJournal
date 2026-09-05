import React, { useState } from 'react';
import { JournalEntry } from '../../types';
import { X, Heart, Sparkles, MapPin, Calendar, Clock, Trash2, Volume2 } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';

interface EntryDetailModalProps {
  entry: JournalEntry | null;
  onClose: () => void;
  onToggleFavorite: (id: string, e: React.MouseEvent) => void;
  onDeleteEntry: (id: string) => void;
}

export const EntryDetailModal: React.FC<EntryDetailModalProps> = ({
  entry,
  onClose,
  onToggleFavorite,
  onDeleteEntry,
}) => {
  const [isPlaying, setIsPlaying] = useState(false);

  if (!entry) return null;

  const handleAudioPlay = () => {
    triggerHaptic('medium');
    setIsPlaying(true);
    setTimeout(() => setIsPlaying(false), 4000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-black/80 backdrop-blur-md overflow-y-auto animate-fadeIn">
      <div className="w-full max-w-lg bg-[#0e1022] border border-white/10 rounded-2xl p-5 shadow-2xl relative flex flex-col gap-4 my-auto max-h-[90vh] overflow-y-auto no-scrollbar">
        {/* Top bar */}
        <div className="flex justify-between items-center pb-2 border-b border-white/[0.08]">
          <button
            onClick={() => {
              triggerHaptic('light');
              onClose();
            }}
            className="p-1 rounded-lg hover:bg-white/10 text-[#c7c4d7] hover:text-white transition-all"
          >
            <X className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-1.5">
            <button
              onClick={(e) => {
                triggerHaptic('light');
                onToggleFavorite(entry.id, e);
              }}
              className="p-1.5 rounded-lg hover:bg-white/10 text-[#c7c4d7] transition-all"
            >
              <Heart className={`w-4 h-4 ${entry.isFavorite ? 'fill-[#ffb783] text-[#ffb783]' : ''}`} />
            </button>
            <button
              onClick={() => {
                triggerHaptic('medium');
                if (confirm('Delete this memory?')) {
                  onDeleteEntry(entry.id);
                  onClose();
                }
              }}
              className="p-1.5 rounded-lg hover:bg-red-500/20 text-red-400 transition-all"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Hero Image if present */}
        {entry.imageUrl && (
          <div className="w-full h-48 sm:h-56 rounded-xl overflow-hidden shadow border border-white/10 relative">
            <img src={entry.imageUrl} alt={entry.title} className="w-full h-full object-cover" />
            <div className="absolute bottom-2.5 left-2.5 bg-[#0a0c1a]/80 backdrop-blur-md px-2.5 py-0.5 rounded-lg border border-white/10 text-[11px] text-[#4fdbc8] font-bold">
              {entry.moodEmoji} {entry.mood}
            </div>
          </div>
        )}

        {/* Meta Info */}
        <div className="flex flex-wrap items-center gap-2.5 text-[11px] text-[#908fa0]">
          <span className="flex items-center gap-1">
            <Calendar className="w-3 h-3 text-[#8083ff]" /> {entry.date}
          </span>
          <span className="flex items-center gap-1">
            <Clock className="w-3 h-3 text-[#8083ff]" /> {entry.time}
          </span>
          {entry.location && (
            <span className="flex items-center gap-1">
              <MapPin className="w-3 h-3 text-[#4fdbc8]" /> {entry.location}
            </span>
          )}
        </div>

        {/* Title */}
        <h2 className="text-xl sm:text-2xl font-bold text-white font-['Manrope']">
          {entry.title}
        </h2>

        {/* Audio Note player */}
        {entry.isAudio && (
          <div className="p-3 rounded-xl bg-[#14182e] border border-white/10 flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <button
                onClick={handleAudioPlay}
                className="w-8 h-8 rounded-lg bg-[#4fdbc8] text-[#003731] flex items-center justify-center font-bold transition-all active:scale-95"
              >
                <Volume2 className="w-4 h-4" />
              </button>
              <div>
                <p className="text-xs font-bold text-white">Voice Note ({entry.audioDuration || '2:14'})</p>
                <p className="text-[10px] text-[#908fa0]">{isPlaying ? 'Playing note...' : 'Tap to listen'}</p>
              </div>
            </div>
            <div className="h-1.5 w-20 bg-white/10 rounded-full overflow-hidden">
              <div className={`h-full bg-[#4fdbc8] ${isPlaying ? 'w-full animate-pulse' : 'w-1/2'}`} />
            </div>
          </div>
        )}

        {/* Body Content */}
        <p className="text-xs sm:text-sm text-[#e1e1f6] leading-relaxed font-['Be_Vietnam_Pro'] whitespace-pre-wrap">
          {entry.content}
        </p>

        {/* Reflection Box */}
        {entry.aiReflection && (
          <section className="bg-[#14182e] border border-white/10 rounded-xl p-3.5 space-y-2">
            <div className="flex items-center gap-1.5 text-[#4fdbc8]">
              <Sparkles className="w-3.5 h-3.5" />
              <h4 className="text-[11px] font-bold uppercase tracking-wider font-['Manrope']">
                Mindful Synthesis
              </h4>
            </div>
            <p className="text-xs text-[#e1e1f6] leading-relaxed italic">
              "{entry.aiReflection}"
            </p>
            {entry.aiAffirmation && (
              <p className="text-xs text-[#71f8e4] pt-1.5 border-t border-white/[0.08]">
                Affirmation: {entry.aiAffirmation}
              </p>
            )}
            {entry.tags && entry.tags.length > 0 && (
              <div className="flex flex-wrap gap-1 pt-1">
                {entry.tags.map((tag, i) => (
                  <span
                    key={i}
                    className="text-[10px] px-2 py-0.5 rounded-md bg-white/5 text-[#c7c4d7] border border-white/[0.06]"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            )}
          </section>
        )}
      </div>
    </div>
  );
};
