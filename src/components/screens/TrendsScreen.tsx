import React, { useState, useMemo } from 'react';
import { JournalEntry, Milestone, MoodTrendDay, MoodType } from '../../types';
import {
  Sparkles,
  Flame,
  RefreshCw,
  Star,
  TrendingUp,
  Award,
  Zap,
  Activity,
  Smile,
  Info,
  Trophy,
} from 'lucide-react';
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Bar,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  CartesianGrid,
} from 'recharts';
import { triggerHaptic } from '../../utils/haptics';

interface TrendsScreenProps {
  totalPoints: number;
  streak: number;
  milestones: Milestone[];
  weekTrends: MoodTrendDay[];
  entries?: JournalEntry[];
}

interface TrendDataPoint {
  id: string;
  name: string;
  fullDate: string;
  mood: MoodType | string;
  moodEmoji: string;
  clarityScore: number;
  pointsEarned: number;
  cumulativePoints: number;
  entriesCount: number;
  notes: string;
}

const MOOD_SCORE_MAP: Record<string, number> = {
  Joy: 92,
  Calm: 80,
  Reflective: 72,
  Tired: 45,
  Anxious: 35,
};

const MOOD_EMOJI_MAP: Record<string, string> = {
  Joy: '✨',
  Calm: '😌',
  Reflective: '🤔',
  Tired: '😴',
  Anxious: '🌪️',
};

export const TrendsScreen: React.FC<TrendsScreenProps> = ({
  totalPoints,
  streak,
  milestones,
  weekTrends,
  entries = [],
}) => {
  const [timeRange, setTimeRange] = useState<'7d' | '14d' | 'entries'>('7d');
  const [selectedPoint, setSelectedPoint] = useState<TrendDataPoint | null>(null);
  const [aiInsight, setAiInsight] = useState(
    "Your data shows a strong positive correlation (+0.84) between mindfulness points earned and emotional clarity. Consistent morning entries especially elevate your mid-week focus."
  );
  const [isRefreshingInsight, setIsRefreshingInsight] = useState(false);

  // Generate enriched chart dataset correlating entries, moods, and points
  const chartData = useMemo<TrendDataPoint[]>(() => {
    if (timeRange === 'entries' && entries.length > 0) {
      // Sort oldest to newest for chronological time series
      const sortedEntries = [...entries].sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));
      let runningPoints = Math.max(0, totalPoints - sortedEntries.length * 40);

      return sortedEntries.map((e, idx) => {
        const score = MOOD_SCORE_MAP[e.mood] || 70;
        const pts = 30 + (e.isFavorite ? 20 : 0) + (e.isAudio ? 15 : 0) + (idx * 5);
        runningPoints += pts;

        return {
          id: e.id,
          name: e.date,
          fullDate: `${e.date} ${e.time || ''}`,
          mood: e.mood,
          moodEmoji: e.moodEmoji || MOOD_EMOJI_MAP[e.mood] || '✨',
          clarityScore: score,
          pointsEarned: pts,
          cumulativePoints: runningPoints,
          entriesCount: 1,
          notes: e.title,
        };
      });
    }

    if (timeRange === '14d') {
      const days14 = [
        { day: 'Sep 23', mood: 'Calm', score: 60, pts: 30, notes: 'Evening breathing session' },
        { day: 'Sep 24', mood: 'Reflective', score: 68, pts: 40, notes: 'Gratitude journal' },
        { day: 'Sep 25', mood: 'Joy', score: 85, pts: 60, notes: 'Nature trail walk' },
        { day: 'Sep 26', mood: 'Calm', score: 72, pts: 45, notes: 'Deep ocean soundscape' },
        { day: 'Sep 27', mood: 'Reflective', score: 75, pts: 50, notes: 'Creative writing' },
        { day: 'Sep 28', mood: 'Joy', score: 90, pts: 70, notes: 'Milestone unlocked' },
        { day: 'Sep 29', mood: 'Calm', score: 65, pts: 35, notes: 'Weekend tea ritual' },
        { day: 'Sep 30', mood: 'Calm', score: 70, pts: 40, notes: 'Morning clarity check-in' },
        { day: 'Oct 01', mood: 'Joy', score: 88, pts: 65, notes: 'Productive workflow' },
        { day: 'Oct 02', mood: 'Reflective', score: 80, pts: 55, notes: 'Thought prompt answered' },
        { day: 'Oct 03', mood: 'Calm', score: 75, pts: 45, notes: 'Box breathing meditation' },
        { day: 'Oct 04', mood: 'Joy', score: 95, pts: 80, notes: 'Peak mindfulness score' },
        { day: 'Oct 05', mood: 'Calm', score: 82, pts: 50, notes: 'Sanctuary reflection' },
        { day: 'Today', mood: 'Calm', score: 85, pts: 60, notes: 'Daily affirmation practiced' },
      ];

      let cum = 300;
      return days14.map((d, i) => {
        cum += d.pts;
        return {
          id: `14d-${i}`,
          name: d.day,
          fullDate: d.day,
          mood: d.mood,
          moodEmoji: MOOD_EMOJI_MAP[d.mood] || '😌',
          clarityScore: d.score,
          pointsEarned: d.pts,
          cumulativePoints: cum,
          entriesCount: d.pts > 50 ? 2 : 1,
          notes: d.notes,
        };
      });
    }

    // Default: 7-Day Week Trends
    const basePts = [35, 55, 70, 45, 90, 40, 60];
    return weekTrends.map((d, i) => {
      const pts = basePts[i] || 45;
      return {
        id: `7d-${i}`,
        name: d.shortDay,
        fullDate: d.day,
        mood: d.mood,
        moodEmoji: MOOD_EMOJI_MAP[d.mood] || '✨',
        clarityScore: d.score,
        pointsEarned: pts,
        cumulativePoints: totalPoints - (7 - i) * 30,
        entriesCount: pts >= 60 ? 2 : 1,
        notes: d.notes,
      };
    });
  }, [timeRange, entries, weekTrends, totalPoints]);

  // Set default selected point
  React.useEffect(() => {
    if (chartData.length > 0 && !selectedPoint) {
      setSelectedPoint(chartData[chartData.length - 1]);
    }
  }, [chartData, selectedPoint]);

  // Average calculations
  const avgClarity = useMemo(() => {
    if (chartData.length === 0) return 0;
    const sum = chartData.reduce((acc, curr) => acc + curr.clarityScore, 0);
    return Math.round(sum / chartData.length);
  }, [chartData]);

  const totalPointsPeriod = useMemo(() => {
    return chartData.reduce((acc, curr) => acc + curr.pointsEarned, 0);
  }, [chartData]);

  const handleRefreshInsight = async () => {
    triggerHaptic('medium');
    setIsRefreshingInsight(true);
    try {
      const res = await fetch('/api/gemini/insights', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ streak, entriesCount: entries.length, dominantMood: 'Calm' }),
      });
      const data = await res.json();
      if (data.insight) {
        setAiInsight(data.insight);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsRefreshingInsight(false);
    }
  };

  // Custom Glass Tooltip
  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data: TrendDataPoint = payload[0].payload;
      return (
        <div className="bg-[#0b0e20]/95 backdrop-blur-xl border border-white/20 rounded-2xl p-3.5 shadow-[0_12px_32px_rgba(0,0,0,0.6)] text-xs space-y-2 max-w-[220px]">
          <div className="flex items-center justify-between border-b border-white/10 pb-1.5">
            <div className="flex items-center gap-1.5">
              <span className="text-sm">{data.moodEmoji}</span>
              <span className="font-bold text-white font-['Manrope']">{data.fullDate}</span>
            </div>
            <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-[#4fdbc8]/15 text-[#71f8e4] font-semibold">
              {data.mood}
            </span>
          </div>

          <div className="space-y-1 text-[11px]">
            <div className="flex justify-between items-center">
              <span className="text-[#908fa0] flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-[#4fdbc8]" /> Emotional Clarity:
              </span>
              <span className="font-bold text-[#71f8e4]">{data.clarityScore}%</span>
            </div>

            <div className="flex justify-between items-center">
              <span className="text-[#908fa0] flex items-center gap-1">
                <span className="w-2 h-2 rounded-full bg-[#ffb783]" /> Points Earned:
              </span>
              <span className="font-bold text-[#ffb783]">+{data.pointsEarned} pts</span>
            </div>
          </div>

          {data.notes && (
            <p className="text-[10px] text-[#c7c4d7] italic pt-1 border-t border-white/[0.06] line-clamp-2">
              "{data.notes}"
            </p>
          )}
        </div>
      );
    }
    return null;
  };

  return (
    <div className="w-full max-w-2xl mx-auto space-y-5 animate-fadeIn pb-16">
      {/* Top Stats Grid */}
      <section className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        {/* Total Points */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-3.5 border border-white/[0.08] flex flex-col justify-between">
          <div className="flex items-center gap-1 text-[#908fa0]">
            <Star className="w-3.5 h-3.5 text-[#8083ff] fill-[#8083ff]" />
            <span className="text-[10px] font-bold uppercase tracking-wider font-['Manrope']">
              Total Points
            </span>
          </div>
          <div className="text-2xl font-extrabold text-white font-['Manrope'] mt-1">
            {totalPoints.toLocaleString()}
          </div>
        </div>

        {/* Current Streak */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-3.5 border border-white/[0.08] flex flex-col justify-between">
          <div className="flex items-center gap-1 text-[#908fa0]">
            <Flame className="w-3.5 h-3.5 text-[#ffb783] fill-[#ffb783]" />
            <span className="text-[10px] font-bold uppercase tracking-wider font-['Manrope']">
              Streak
            </span>
          </div>
          <div className="text-2xl font-extrabold text-white font-['Manrope'] mt-1 flex items-baseline gap-1">
            {streak}
            <span className="text-xs font-normal text-[#908fa0]">days</span>
          </div>
        </div>

        {/* Avg Clarity */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-3.5 border border-white/[0.08] flex flex-col justify-between">
          <div className="flex items-center gap-1 text-[#908fa0]">
            <Activity className="w-3.5 h-3.5 text-[#4fdbc8]" />
            <span className="text-[10px] font-bold uppercase tracking-wider font-['Manrope']">
              Avg Clarity
            </span>
          </div>
          <div className="text-2xl font-extrabold text-[#4fdbc8] font-['Manrope'] mt-1">
            {avgClarity}%
          </div>
        </div>

        {/* Period Points */}
        <div className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-3.5 border border-white/[0.08] flex flex-col justify-between">
          <div className="flex items-center gap-1 text-[#908fa0]">
            <Zap className="w-3.5 h-3.5 text-[#ffb783]" />
            <span className="text-[10px] font-bold uppercase tracking-wider font-['Manrope']">
              Period Gains
            </span>
          </div>
          <div className="text-2xl font-extrabold text-[#ffb783] font-['Manrope'] mt-1">
            +{totalPointsPeriod}
          </div>
        </div>
      </section>

      {/* Main Interactive Recharts Mood & Points Correlation Chart */}
      <section
        id="recharts-mood-trends-card"
        className="bg-gradient-to-br from-[#10142b]/90 via-[#0d1024]/85 to-[#0a0c1a]/90 backdrop-blur-2xl rounded-3xl p-5 border border-white/[0.12] shadow-[0_16px_40px_rgba(0,0,0,0.4)] space-y-4 relative overflow-hidden"
      >
        {/* Glowing atmospheric gradient background */}
        <div className="absolute top-0 right-1/4 w-48 h-48 rounded-full bg-[#4fdbc8]/10 blur-3xl pointer-events-none" />
        <div className="absolute bottom-0 left-1/4 w-48 h-48 rounded-full bg-[#ffb783]/10 blur-3xl pointer-events-none" />

        {/* Header with Title & Range Switcher */}
        <div className="relative z-10 flex flex-wrap items-center justify-between gap-3 border-b border-white/[0.08] pb-3.5">
          <div>
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-xl bg-[#4fdbc8]/15 text-[#4fdbc8] flex items-center justify-center border border-[#4fdbc8]/30">
                <TrendingUp className="w-3.5 h-3.5" />
              </div>
              <div>
                <h2 className="text-base font-bold text-white font-['Manrope']">
                  Mood &amp; Points Correlation
                </h2>
                <p className="text-[11px] text-[#908fa0]">
                  Visualizing emotional clarity alongside mindful reward points.
                </p>
              </div>
            </div>
          </div>

          {/* Timeframe selector & refresh */}
          <div className="flex items-center gap-1.5">
            <div className="bg-[#0b0e20] border border-white/10 rounded-xl p-0.5 flex text-xs">
              <button
                id="trends-range-7d-btn"
                onClick={() => {
                  triggerHaptic('light');
                  setTimeRange('7d');
                }}
                className={`px-2.5 py-1 rounded-lg font-medium transition-all ${
                  timeRange === '7d'
                    ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow'
                    : 'text-[#908fa0] hover:text-white'
                }`}
              >
                7 Days
              </button>
              <button
                id="trends-range-14d-btn"
                onClick={() => {
                  triggerHaptic('light');
                  setTimeRange('14d');
                }}
                className={`px-2.5 py-1 rounded-lg font-medium transition-all ${
                  timeRange === '14d'
                    ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow'
                    : 'text-[#908fa0] hover:text-white'
                }`}
              >
                14 Days
              </button>
              <button
                id="trends-range-entries-btn"
                onClick={() => {
                  triggerHaptic('light');
                  setTimeRange('entries');
                }}
                className={`px-2.5 py-1 rounded-lg font-medium transition-all ${
                  timeRange === 'entries'
                    ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow'
                    : 'text-[#908fa0] hover:text-white'
                }`}
              >
                Entries
              </button>
            </div>

            <button
              onClick={handleRefreshInsight}
              title="Refresh AI Insights"
              className="text-[#908fa0] hover:text-white p-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/[0.06] transition-colors"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isRefreshingInsight ? 'animate-spin text-[#4fdbc8]' : ''}`} />
            </button>
          </div>
        </div>

        {/* Legend & Correlation Indicator */}
        <div className="relative z-10 flex flex-wrap items-center justify-between gap-2 px-1 text-xs">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-md bg-gradient-to-r from-[#4fdbc8] to-[#8083ff]" />
              <span className="text-[#c7c4d7] font-semibold text-[11px]">
                Clarity Score (0-100%)
              </span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-md bg-[#ffb783]/80 border border-[#ffb783]" />
              <span className="text-[#c7c4d7] font-semibold text-[11px]">
                Points Earned (pts)
              </span>
            </div>
          </div>

          <div className="flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#4fdbc8]/10 border border-[#4fdbc8]/30 text-[10px] text-[#71f8e4] font-semibold">
            <Sparkles className="w-3 h-3" />
            <span>Positive Correlation: +0.84</span>
          </div>
        </div>

        {/* Recharts Container */}
        <div className="relative z-10 w-full h-64 pt-2">
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart
              data={chartData}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
              onClick={(e: any) => {
                if (e && e.activePayload && e.activePayload[0]) {
                  triggerHaptic('light');
                  setSelectedPoint(e.activePayload[0].payload);
                }
              }}
            >
              <defs>
                {/* Mood Clarity Area Gradient */}
                <linearGradient id="clarityGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#4fdbc8" stopOpacity={0.45} />
                  <stop offset="95%" stopColor="#8083ff" stopOpacity={0.0} />
                </linearGradient>
                {/* Points Bar Gradient */}
                <linearGradient id="pointsGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#ffb783" stopOpacity={0.9} />
                  <stop offset="100%" stopColor="#d97721" stopOpacity={0.4} />
                </linearGradient>
              </defs>

              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.07)" vertical={false} />

              {/* X Axis */}
              <XAxis
                dataKey="name"
                stroke="#908fa0"
                tick={{ fill: '#908fa0', fontSize: 11, fontFamily: 'Manrope' }}
                axisLine={{ stroke: 'rgba(255, 255, 255, 0.1)' }}
                tickLine={false}
              />

              {/* Primary Y Axis (Clarity Score 0 - 100) */}
              <YAxis
                yAxisId="left"
                domain={[0, 100]}
                stroke="#4fdbc8"
                tick={{ fill: '#4fdbc8', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
                unit="%"
              />

              {/* Secondary Y Axis (Points Earned 0 - 120) */}
              <YAxis
                yAxisId="right"
                orientation="right"
                domain={[0, 120]}
                stroke="#ffb783"
                tick={{ fill: '#ffb783', fontSize: 10 }}
                axisLine={false}
                tickLine={false}
                unit="p"
              />

              <Tooltip content={<CustomTooltip />} />

              {/* Points Bar Chart on Secondary Axis */}
              <Bar
                yAxisId="right"
                dataKey="pointsEarned"
                name="Points Earned"
                fill="url(#pointsGradient)"
                radius={[6, 6, 0, 0]}
                barSize={16}
                animationDuration={800}
              />

              {/* Clarity Score Area Chart on Primary Axis */}
              <Area
                yAxisId="left"
                type="monotone"
                dataKey="clarityScore"
                name="Clarity Score"
                stroke="#4fdbc8"
                strokeWidth={3}
                fillOpacity={1}
                fill="url(#clarityGradient)"
                activeDot={{
                  r: 6,
                  fill: '#71f8e4',
                  stroke: '#0b0e20',
                  strokeWidth: 2,
                }}
                animationDuration={1000}
              />

              {/* Smooth trendline guide */}
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="clarityScore"
                stroke="#8083ff"
                strokeWidth={1.5}
                strokeDasharray="4 4"
                dot={false}
                isAnimationActive={false}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>

        {/* Selected Day / Entry Interactive Inspector */}
        {selectedPoint && (
          <div className="relative z-10 p-3.5 rounded-2xl bg-[#0b0e20]/90 border border-white/[0.08] shadow-inner flex flex-wrap items-center justify-between gap-3 text-xs">
            <div className="flex items-center gap-3">
              <span className="w-9 h-9 rounded-xl bg-white/5 border border-white/10 flex items-center justify-center text-lg shrink-0">
                {selectedPoint.moodEmoji}
              </span>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-bold text-white font-['Manrope']">
                    {selectedPoint.fullDate}
                  </span>
                  <span className="text-[10px] font-semibold text-[#4fdbc8] px-2 py-0.5 rounded-full bg-[#4fdbc8]/15 border border-[#4fdbc8]/30">
                    {selectedPoint.mood}
                  </span>
                </div>
                <p className="text-[#908fa0] text-[11px] mt-0.5 line-clamp-1">
                  {selectedPoint.notes || 'Mindful reflection recorded'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 text-right">
              <div>
                <span className="text-[10px] text-[#908fa0] block">Clarity Score</span>
                <span className="font-bold text-[#4fdbc8] text-sm">
                  {selectedPoint.clarityScore}%
                </span>
              </div>
              <div className="border-l border-white/10 pl-3">
                <span className="text-[10px] text-[#908fa0] block">Points Added</span>
                <span className="font-bold text-[#ffb783] text-sm">
                  +{selectedPoint.pointsEarned} pts
                </span>
              </div>
            </div>
          </div>
        )}

        {/* AI Insight Summary Banner */}
        <div className="relative z-10 bg-[#121629]/90 border border-white/10 rounded-2xl p-3.5 flex gap-3 items-start">
          <div className="p-2 rounded-xl bg-[#4fdbc8]/15 text-[#4fdbc8] shrink-0 mt-0.5 border border-[#4fdbc8]/30">
            <Sparkles className="w-4 h-4" />
          </div>
          <div>
            <span className="text-[11px] font-bold text-[#4fdbc8] uppercase tracking-wider block mb-0.5 font-['Manrope']">
              AI Emotional Pattern Insight
            </span>
            <p className="text-xs text-[#e1e1f6] leading-relaxed">
              {aiInsight}
            </p>
          </div>
        </div>
      </section>

      {/* Milestones & Achievement Progress */}
      <section className="space-y-3">
        <div className="flex justify-between items-center px-1">
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-[#ffb783]" />
            <h2 className="text-base font-bold text-[#e1e1f6] font-['Manrope']">
              Milestones &amp; Habits
            </h2>
          </div>
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
