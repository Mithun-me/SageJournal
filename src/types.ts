export type MoodType = 'Calm' | 'Joy' | 'Reflective' | 'Low' | 'Grounded' | 'Energetic';

export interface JournalEntry {
  id: string;
  title: string;
  content: string;
  date: string;
  time: string;
  timestamp: number;
  mood: MoodType;
  moodEmoji: string;
  imageUrl?: string;
  audioUrl?: string;
  audioDuration?: string;
  isAudio?: boolean;
  isQuote?: boolean;
  location?: string;
  isFavorite?: boolean;
  tags?: string[];
  aiReflection?: string;
  aiThemes?: string[];
  aiAffirmation?: string;
  gridSpan?: 'span-2-row-1' | 'span-1-row-1' | 'span-1-row-2' | 'span-2-row-2';
}

export interface Milestone {
  id: string;
  title: string;
  subtitle: string;
  icon: string;
  achieved: boolean;
  achievedDate?: string;
  progress?: number;
  maxProgress?: number;
  gradient: string;
}

export interface MoodTrendDay {
  day: string;
  shortDay: string;
  mood: MoodType;
  score: number; // 0 to 100
  heightPercent: number;
  active?: boolean;
  notes?: string;
}

export type TabType = 'home' | 'archive' | 'sanctuary' | 'trends' | 'settings';

export type AppTheme = 'liquid-glass' | 'deep-sea';

export interface GroundingSource {
  title?: string;
  uri: string;
}

export interface DailyAffirmation {
  quote: string;
  author: string;
  source?: string;
  reflection: string;
  theme: string;
  sources?: GroundingSource[];
  fetchedAt?: string;
}
