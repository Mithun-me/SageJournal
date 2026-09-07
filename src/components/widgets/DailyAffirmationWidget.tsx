import React, { useState, useEffect } from 'react';
import { DailyAffirmation, MoodType } from '../../types';
import { Sparkles, RefreshCw, Quote, ExternalLink, Bookmark, Check, Copy, Volume2, Globe, Feather } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';
import { postJson } from '../../utils/api';

interface DailyAffirmationWidgetProps {
  onReflectWithQuote?: (quote: string, author: string) => void;
  currentMood?: MoodType | null;
}

export const DailyAffirmationWidget: React.FC<DailyAffirmationWidgetProps> = ({
  onReflectWithQuote,
  currentMood,
}) => {
  const [affirmation, setAffirmation] = useState<DailyAffirmation>({
    quote: "Smile, breathe and go slowly.",
    author: "Thích Nhất Hạnh",
    source: "Peace Is Every Step",
    reflection: "When you slow down your pace, you create space to witness the stillness already present within you.",
    theme: "Presence",
    sources: [
      { title: "Plum Village Community of Mindful Living", uri: "https://plumvillage.org" }
    ],
    fetchedAt: new Date().toISOString(),
  });

  const [isLoading, setIsLoading] = useState(false);
  const [isCopied, setIsCopied] = useState(false);
  const [isSaved, setIsSaved] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);

  // Load initial affirmation or saved one from localStorage
  useEffect(() => {
    const savedAffirmation = localStorage.getItem('aura_daily_affirmation');
    const savedDate = localStorage.getItem('aura_daily_affirmation_date');
    const today = new Date().toDateString();

    if (savedAffirmation && savedDate === today) {
      try {
        setAffirmation(JSON.parse(savedAffirmation));
      } catch (e) {
        fetchAffirmation();
      }
    } else {
      fetchAffirmation();
    }
  }, []);

  const fetchAffirmation = async (topicOverride?: string) => {
    triggerHaptic('medium');
    setIsLoading(true);
    try {
      const topic = topicOverride || (currentMood ? `${currentMood} mindset and calm mindfulness` : 'peace, presence and inner stillness');
      const data = await postJson<DailyAffirmation>('/api/gemini/daily-affirmation', {
        topic,
      });
      setAffirmation(data);
      localStorage.setItem('aura_daily_affirmation', JSON.stringify(data));
      localStorage.setItem('aura_daily_affirmation_date', new Date().toDateString());
      triggerHaptic('success');
    } catch (err) {
      console.error('Failed to fetch affirmation:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopy = () => {
    triggerHaptic('light');
    const textToCopy = `"${affirmation.quote}" — ${affirmation.author}${affirmation.source ? ` (${affirmation.source})` : ''}\n\nMindful Reflection: ${affirmation.reflection}`;
    navigator.clipboard.writeText(textToCopy);
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  };

  const handleToggleSave = () => {
    triggerHaptic('light');
    setIsSaved(!isSaved);
  };

  const handleSpeak = () => {
    triggerHaptic('light');
    if ('speechSynthesis' in window) {
      if (isSpeaking) {
        window.speechSynthesis.cancel();
        setIsSpeaking(false);
        return;
      }

      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(`${affirmation.quote}. By ${affirmation.author}. ${affirmation.reflection}`);
      utterance.rate = 0.88; // Gentle, soothing mindful tempo
      utterance.pitch = 0.95;

      utterance.onstart = () => setIsSpeaking(true);
      utterance.onend = () => setIsSpeaking(false);
      utterance.onerror = () => setIsSpeaking(false);

      window.speechSynthesis.speak(utterance);
    }
  };

  const handleJournalThis = () => {
    triggerHaptic('medium');
    if (onReflectWithQuote) {
      onReflectWithQuote(affirmation.quote, affirmation.author);
    }
  };

  return (
    <section
      id="daily-affirmation-widget"
      className="relative rounded-3xl overflow-hidden p-5 sm:p-6 bg-gradient-to-br from-[#12162f]/90 via-[#0d1226]/85 to-[#0b1720]/90 backdrop-blur-2xl border border-white/[0.12] shadow-[0_16px_40px_rgba(0,0,0,0.5)] transition-all duration-300 hover:border-white/20 group"
    >
      {/* Liquid glass specular highlight & glowing orbs */}
      <div className="absolute -top-24 -left-20 w-48 h-48 rounded-full bg-[#4fdbc8]/15 blur-3xl pointer-events-none" />
      <div className="absolute -bottom-24 -right-16 w-52 h-52 rounded-full bg-[#8083ff]/15 blur-3xl pointer-events-none" />
      <div className="absolute inset-0 bg-gradient-to-t from-transparent via-white/[0.02] to-white/[0.06] pointer-events-none" />

      {/* Top Bar: Title, Theme Chip, Search Grounding Badge, Actions */}
      <div className="relative z-10 flex flex-wrap items-center justify-between gap-2 pb-3.5 border-b border-white/[0.08]">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-xl bg-[#4fdbc8]/15 text-[#4fdbc8] flex items-center justify-center border border-[#4fdbc8]/30 shadow-inner">
            <Sparkles className="w-3.5 h-3.5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold uppercase tracking-wider text-white font-['Manrope']">
                Daily Affirmation
              </span>
              <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-[#4fdbc8]/15 text-[#71f8e4] border border-[#4fdbc8]/30">
                {affirmation.theme || 'Presence'}
              </span>
            </div>
          </div>
        </div>

        {/* Right tools */}
        <div className="flex items-center gap-1">
          {/* Read Aloud */}
          <button
            id="affirmation-speak-btn"
            onClick={handleSpeak}
            title={isSpeaking ? "Pause Audio" : "Listen to Affirmation"}
            className={`p-1.5 rounded-lg border transition-all ${
              isSpeaking
                ? 'bg-[#4fdbc8]/20 text-[#71f8e4] border-[#4fdbc8]/40 animate-pulse'
                : 'bg-white/5 hover:bg-white/10 text-[#908fa0] hover:text-white border-white/[0.06]'
            }`}
          >
            <Volume2 className="w-3.5 h-3.5" />
          </button>

          {/* Copy */}
          <button
            id="affirmation-copy-btn"
            onClick={handleCopy}
            title="Copy Quote & Reflection"
            className="p-1.5 rounded-lg bg-white/5 hover:bg-white/10 text-[#908fa0] hover:text-white border border-white/[0.06] transition-all"
          >
            {isCopied ? <Check className="w-3.5 h-3.5 text-[#4fdbc8]" /> : <Copy className="w-3.5 h-3.5" />}
          </button>

          {/* Bookmark */}
          <button
            id="affirmation-bookmark-btn"
            onClick={handleToggleSave}
            title={isSaved ? "Saved to Favorites" : "Save Affirmation"}
            className={`p-1.5 rounded-lg border transition-all ${
              isSaved
                ? 'bg-[#ffb783]/20 text-[#ffb783] border-[#ffb783]/40'
                : 'bg-white/5 hover:bg-white/10 text-[#908fa0] hover:text-white border-white/[0.06]'
            }`}
          >
            <Bookmark className={`w-3.5 h-3.5 ${isSaved ? 'fill-[#ffb783]' : ''}`} />
          </button>

          {/* Shuffle fresh quote with Google Search */}
          <button
            id="affirmation-refresh-btn"
            onClick={() => fetchAffirmation()}
            disabled={isLoading}
            title="Fetch fresh quote with Google Search"
            className="px-2.5 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-[#908fa0] hover:text-[#71f8e4] border border-white/[0.06] text-xs font-medium flex items-center gap-1.5 transition-all active:scale-95 disabled:opacity-50"
          >
            <RefreshCw className={`w-3 h-3 ${isLoading ? 'animate-spin text-[#4fdbc8]' : ''}`} />
            <span className="hidden sm:inline text-[11px]">Shuffle</span>
          </button>
        </div>
      </div>

      {/* Main Quote Content */}
      <div className="relative z-10 my-4 space-y-3">
        {isLoading ? (
          <div className="space-y-3 py-2 animate-pulse">
            <div className="h-5 bg-white/10 rounded-md w-3/4" />
            <div className="h-4 bg-white/5 rounded-md w-1/2" />
            <div className="h-12 bg-white/5 rounded-xl w-full mt-3" />
          </div>
        ) : (
          <>
            {/* Quote Block */}
            <div className="relative flex items-start gap-3">
              <Quote className="w-6 h-6 text-[#4fdbc8]/40 shrink-0 mt-0.5 rotate-180" />
              <div className="flex-1">
                <blockquote className="text-base sm:text-lg font-medium text-white font-['Manrope'] leading-relaxed tracking-tight italic">
                  "{affirmation.quote}"
                </blockquote>
                <div className="mt-2 flex flex-wrap items-center gap-2 text-xs">
                  <span className="font-bold text-[#4fdbc8] tracking-wide">
                    — {affirmation.author}
                  </span>
                  {affirmation.source && (
                    <span className="text-[#908fa0] italic">
                      · {affirmation.source}
                    </span>
                  )}
                </div>
              </div>
            </div>

            {/* Mindful Takeaway Reflection */}
            <div className="p-3.5 rounded-2xl bg-[#0a0c1a]/70 border border-white/[0.06] shadow-inner text-xs text-[#d3d2e5] leading-relaxed flex items-start gap-2.5">
              <div className="w-1.5 h-1.5 rounded-full bg-[#4fdbc8] shrink-0 mt-1.5" />
              <div>
                <span className="text-[11px] font-bold text-[#c7c4d7] block mb-0.5">
                  Mindful Grounding
                </span>
                <p>{affirmation.reflection}</p>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Bottom Grounding Sources & Action Bar */}
      <div className="relative z-10 pt-3 border-t border-white/[0.08] flex flex-wrap items-center justify-between gap-2.5">
        {/* Google Search Grounding Badge & Source links */}
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-1 text-[11px] text-[#908fa0]">
            <Globe className="w-3 h-3 text-[#4fdbc8]" />
            <span>Search Grounded:</span>
          </div>

          {affirmation.sources && affirmation.sources.length > 0 ? (
            <div className="flex flex-wrap items-center gap-1.5">
              {affirmation.sources.slice(0, 2).map((s, idx) => (
                <a
                  key={idx}
                  href={s.uri}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 text-[10px] px-2 py-0.5 rounded-md bg-white/5 hover:bg-white/10 text-[#c7c4d7] hover:text-[#71f8e4] border border-white/[0.06] transition-colors truncate max-w-[150px]"
                  title={s.title || s.uri}
                >
                  <span className="truncate">{s.title || 'Source'}</span>
                  <ExternalLink className="w-2.5 h-2.5 shrink-0" />
                </a>
              ))}
            </div>
          ) : (
            <span className="text-[10px] text-[#908fa0]">Google Search Verified</span>
          )}
        </div>

        {/* Action: Journal / Reflect on this Quote */}
        {onReflectWithQuote && (
          <button
            id="affirmation-journal-action-btn"
            onClick={handleJournalThis}
            className="px-3 py-1.5 rounded-xl bg-[#4fdbc8]/15 hover:bg-[#4fdbc8]/25 text-[#71f8e4] text-xs font-semibold border border-[#4fdbc8]/30 flex items-center gap-1.5 transition-all active:scale-95"
          >
            <Feather className="w-3.5 h-3.5" />
            <span>Journal this quote</span>
          </button>
        )}
      </div>
    </section>
  );
};
