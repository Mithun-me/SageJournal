import React, { useState } from 'react';
import { Milestone, MoodTrendDay } from '../../types';
import { Sparkles, Trophy, Calendar, Lock, Flame, RefreshCw, Star } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';
import { postJson } from '../../utils/api';

interface TrendsScreenProps {
  totalPoints: number;
  streak: number;
  milestones: Milestone[];
  weekTrends: MoodTrendDay[];
}

export const TrendsScreen: React.FC<TrendsScreenProps> = ({
  totalPoints,
  streak,
  milestones,
  weekTrends,
}) => {
  const [selectedDay, setSelectedDay] = useState<MoodTrendDay>(
    weekTrends.find((d) => d.active) || weekTrends[4]
  );
  const [aiInsight, setAiInsight] = useState(
    "You've experienced elevated calmness on days following a morning reflection. Keep nurturing this rhythm."
  );
  const [isRefreshingInsight, setIsRefreshingInsight] = useState(false);

  const handleRefreshInsight = async () => {
    triggerHaptic('medium');
    setIsRefreshingInsight(true);
    try {
      const data = await postJson<{ insight?: string }>('/api/gemini/insights', {
        streak,
        entriesCount: 8,
        dominantMood: 'Calm',
      });
      if (data.insight) {
        setAiInsight(data.insight);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsRefreshingInsight(false);
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto space-y-5 animate-fadeIn pb-16">
      {/* Stats Summary Grid */}
      <section className="grid grid-cols-2 gap-3">
        {/* Points Card */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-4 border border-white/[0.08] flex flex-col justify-between">
          <div className="flex items-center gap-1.5 text-[#908fa0] mb-1">
            <Star className="w-3.5 h-3.5 text-[#8083ff] fill-[#8083ff]" />
            <span className="text-[11px] font-bold uppercase tracking-wider font-['Manrope']">
              Mindful Points
            </span>
          </div>
          <div className="text-3xl font-extrabold text-white font-['Manrope']">
            {totalPoints.toLocaleString()}
          </div>
        </div>

        {/* Streak Card */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-4 border border-white/[0.08] flex items-center justify-between">
          <div>
            <div className="flex items-center gap-1.5 text-[#908fa0] mb-1">
              <Flame className="w-3.5 h-3.5 text-[#ffb783] fill-[#ffb783]" />
              <span className="text-[11px] font-bold uppercase tracking-wider font-['Manrope']">
                Current Streak
              </span>
            </div>
            <div className="text-3xl font-extrabold text-white font-['Manrope']">
              {streak}{' '}
              <span className="text-xs font-medium text-[#908fa0] ml-0.5">
                days
              </span>
            </div>
          </div>

          <div className="w-10 h-10 rounded-full bg-[#ffb783]/15 border border-[#ffb783]/30 flex items-center justify-center text-[#ffb783]">
            <Flame className="w-5 h-5 fill-current" />
          </div>
        </div>
      </section>

      {/* Mood Trends Weekly Chart */}
      <section
        id="mood-trends-card"
        className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-5 border border-white/[0.08] space-y-4"
      >
        <div className="flex justify-between items-start">
          <div>
            <h2 className="text-base font-bold text-[#e1e1f6] font-['Manrope']">
              Emotional Rhythm
            </h2>
            <p className="text-xs text-[#908fa0]">
              Weekly overview of your emotional clarity.
            </p>
          </div>
          <button
            onClick={handleRefreshInsight}
            title="Refresh Insights"
            className="text-[#908fa0] hover:text-white p-1.5 rounded-lg hover:bg-white/5 transition-colors"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isRefreshingInsight ? 'animate-spin' : ''}`} />
          </button>
        </div>

        {/* Weekly Bar Chart */}
        <div className="h-36 flex items-end justify-between gap-2 relative pt-2">
          {weekTrends.map((d, i) => {
            const isSelected = selectedDay.day === d.day;
            const isHigh = d.score >= 70;
            return (
              <button
                key={i}
                onClick={() => {
                  triggerHaptic('light');
                  setSelectedDay(d);
                }}
                className="w-full flex flex-col items-center gap-1.5 group focus:outline-none"
              >
                <div
                  style={{ height: `${Math.max(d.heightPercent, 15)}%` }}
                  className={`w-full rounded-md transition-all duration-200 ${
                    isSelected
                      ? 'bg-[#4fdbc8] shadow'
                      : isHigh
                      ? 'bg-[#4fdbc8]/60 hover:bg-[#4fdbc8]/80'
                      : 'bg-white/15 hover:bg-white/25'
                  }`}
                />
                <span
                  className={`text-[11px] font-bold font-['Manrope'] ${
                    isSelected ? 'text-[#4fdbc8]' : 'text-[#908fa0]'
                  }`}
                >
                  {d.shortDay}
                </span>
              </button>
            );
          })}
        </div>

        {/* Selected Day Detail */}
        {selectedDay && (
          <div className="p-3 rounded-xl bg-[#0a0c1a] border border-white/[0.06] flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5">
              <span className="font-bold text-white font-['Manrope']">
                {selectedDay.day}:
              </span>
              <span className="text-[#4fdbc8] font-semibold">
                {selectedDay.mood} ({selectedDay.score}% clarity)
              </span>
            </div>
            <span className="text-[#908fa0] italic text-[11px]">{selectedDay.notes}</span>
          </div>
        )}

        {/* Mindful Summary Card */}
        <div className="bg-[#121629] border border-white/10 rounded-xl p-3.5 flex gap-3 items-start">
          <div className="p-1.5 rounded-lg bg-[#4fdbc8]/15 text-[#4fdbc8] shrink-0 mt-0.5">
            <Sparkles className="w-3.5 h-3.5" />
          </div>
          <p className="text-xs text-[#e1e1f6] leading-relaxed">
            {aiInsight}
          </p>
        </div>
      </section>

      {/* Milestones Grid */}
      <section className="space-y-3">
        <div className="flex justify-between items-center px-1">
          <h2 className="text-base font-bold text-[#e1e1f6] font-['Manrope']">
            Milestones &amp; Habits
          </h2>
          <span className="text-xs text-[#908fa0]">
            {milestones.filter((m) => m.achieved).length} of {milestones.length} unlocked
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
          {milestones.map((m) => {
            const isUnlocked = m.achieved;
            return (
              <div
                key={m.id}
                id={`milestone-${m.id}`}
                className={`rounded-2xl p-4 border transition-all flex flex-col items-center text-center gap-2 ${
                  isUnlocked
                    ? 'bg-[#0e1022]/80 border-white/[0.08]'
                    : 'bg-[#0a0c1a]/40 border-white/[0.04] opacity-50'
                }`}
              >
                <div
                  className={`w-11 h-11 rounded-full flex items-center justify-center ${
                    isUnlocked
                      ? 'bg-gradient-to-br from-[#1d2340] to-[#122e2b] text-[#4fdbc8] border border-[#4fdbc8]/30'
                      : 'bg-white/5 text-[#908fa0] border border-white/[0.08]'
                  }`}
                >
                  <span className="material-symbols-outlined text-[22px]">
                    {m.icon}
                  </span>
                </div>

                <div>
                  <h3 className="text-xs font-bold text-white font-['Manrope']">
                    {m.title}
                  </h3>
                  <p className="text-[10px] text-[#908fa0] mt-0.5">
                    {m.subtitle}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </section>
    </div>
  );
};
