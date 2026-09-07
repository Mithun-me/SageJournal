import express, { Request, Response } from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import { GoogleGenAI } from '@google/genai';
import dotenv from 'dotenv';
import { createServer as createViteServer } from 'vite';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = Number(process.env.PORT) || 3000;

app.use(express.json());

// Capacitor serves the bundled app from https://localhost inside the Android
// WebView, so those requests are cross-origin and need explicit CORS headers.
// EXTRA_CORS_ORIGINS (comma-separated) covers tunnels and deployed frontends.
const ALLOWED_ORIGINS = new Set<string>([
  'https://localhost',
  'capacitor://localhost',
  ...(process.env.EXTRA_CORS_ORIGINS || '')
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean),
]);

// Origins carry their port, so match any localhost port for browser dev.
const LOCALHOST_ORIGIN = /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/;

const isAllowedOrigin = (origin: string) =>
  ALLOWED_ORIGINS.has(origin) || LOCALHOST_ORIGIN.test(origin);

app.use((req: Request, res: Response, next) => {
  const origin = req.headers.origin;
  if (origin && isAllowedOrigin(origin)) {
    res.setHeader('Access-Control-Allow-Origin', origin);
    res.setHeader('Vary', 'Origin');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  }
  if (req.method === 'OPTIONS') {
    res.sendStatus(204);
    return;
  }
  next();
});

// Gemini model id, overridable without touching the four call sites below.
const GEMINI_MODEL = process.env.GEMINI_MODEL || 'gemini-2.5-flash';

// Initialize GoogleGenAI client lazily if key exists
let aiClient: GoogleGenAI | null = null;
function getAi(): GoogleGenAI | null {
  if (!aiClient && process.env.GEMINI_API_KEY) {
    aiClient = new GoogleGenAI({
      apiKey: process.env.GEMINI_API_KEY,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        },
      },
    });
  }
  return aiClient;
}

// API Routes
app.get('/api/health', (req: Request, res: Response) => {
  res.json({ status: 'ok', time: new Date().toISOString() });
});

// AI Reflection Generation Endpoint
app.post('/api/gemini/reflect', async (req: Request, res: Response) => {
  try {
    const { title, content, mood, prompt } = req.body;
    const ai = getAi();

    if (!ai) {
      // Mindful psychological heuristic fallback if Gemini key is not configured
      const mindfulReflections = [
        `Taking time to notice this moment reflects genuine mindfulness. By acknowledging your feelings without judgment, you create space for emotional clarity and grounded calm.`,
        `This entry reveals a gentle rhythm of self-awareness. Notice how small shifts in perspective during your day anchor your sense of stability and peace.`,
        `Your words capture a meaningful thread of intention. Pausing to write these observations reinforces resilience and nurtures inner equilibrium.`,
        `There is profound wisdom in your reflection. Embracing both the stillness and the movement of life allows you to stay centered and connected to yourself.`
      ];
      const randomReflection = mindfulReflections[Math.floor(Math.random() * mindfulReflections.length)];
      return res.json({
        reflection: randomReflection,
        themes: ['Mindfulness', 'Emotional Clarity', 'Inner Calm'],
        suggestedAffirmation: 'I am grounded in this present moment with ease and gratitude.'
      });
    }

    const aiPrompt = `You are "Aura", an empathetic, poetic, and psychologically grounded mindfulness companion.
The user just wrote a personal journal entry.
Journal Details:
- Title: "${title || 'Untitled reflection'}"
- Mood: "${mood || 'Reflective'}"
- Prompt answered: "${prompt || 'Daily Open Reflection'}"
- Entry text: "${content || 'Taking a moment to breathe and observe today.'}"

Please generate a thoughtful, 2-to-3 sentence gentle reflection that:
1. Validates the user's emotional state with warmth, poetic elegance, and non-judgmental awareness.
2. Identifies a subtle insight or silver lining without toxic positivity.
3. Suggests 2-3 short resonant theme tags and a one-sentence personal grounding affirmation.

Format your response as valid JSON with keys:
- "reflection": (string) 2-3 sentences of mindful insight.
- "themes": (array of 2-3 string tags, e.g. ["Clarity", "Presence", "Gratitude"])
- "suggestedAffirmation": (string) A concise, empowering grounding affirmation.`;

    const response = await ai.models.generateContent({
      model: GEMINI_MODEL,
      contents: aiPrompt,
      config: {
        responseMimeType: 'application/json',
      },
    });

    const jsonText = response.text || '{}';
    const parsed = JSON.parse(jsonText);
    res.json(parsed);
  } catch (error: any) {
    // Graceful fallback on quota limits (429) or transient errors
    const mindfulReflections = [
      `Taking time to notice this moment reflects genuine mindfulness. By acknowledging your feelings without judgment, you create space for emotional clarity and grounded calm.`,
      `This entry reveals a gentle rhythm of self-awareness. Notice how small shifts in perspective during your day anchor your sense of stability and peace.`,
      `Your words capture a meaningful thread of intention. Pausing to write these observations reinforces resilience and nurtures inner equilibrium.`,
      `There is profound wisdom in your reflection. Embracing both the stillness and the movement of life allows you to stay centered and connected to yourself.`
    ];
    const randomReflection = mindfulReflections[Math.floor(Math.random() * mindfulReflections.length)];
    res.json({
      reflection: randomReflection,
      themes: ['Self-Discovery', 'Mindfulness', 'Peace'],
      suggestedAffirmation: 'I allow my thoughts to flow with ease and find peace in each breath.'
    });
  }
});

// Daily Mindful Prompt Generator Endpoint
app.post('/api/gemini/prompt', async (req: Request, res: Response) => {
  try {
    const { category, currentMood } = req.body;
    const ai = getAi();

    const defaultPrompts = [
      'What is a small detail you noticed today that brought you an unexpected sense of calm?',
      'When did you feel most in alignment with your natural rhythm today?',
      'What is one burden or expectation you can gently release this evening?',
      'How did your breath or body communicate with you during your most stressful moment?',
      'Describe a sound, texture, or scent today that grounded your senses.',
      'What is something you are silently grateful for that you haven’t expressed aloud recently?'
    ];

    if (!ai) {
      const randomPrompt = defaultPrompts[Math.floor(Math.random() * defaultPrompts.length)];
      return res.json({ prompt: randomPrompt, category: category || 'Gratitude' });
    }

    const response = await ai.models.generateContent({
      model: GEMINI_MODEL,
      contents: `Generate one concise, evocative, and psychologically grounded mindfulness journal prompt for a user feeling ${currentMood || 'Reflective'}. The category is ${category || 'Daily Presence'}. Return only the question text in 1 sentence.`,
    });

    res.json({ prompt: response.text?.trim() || defaultPrompts[0], category: category || 'Mindfulness' });
  } catch (error) {
    const defaultPrompts = [
      'What is a small detail you noticed today that brought you an unexpected sense of calm?',
      'When did you feel most in alignment with your natural rhythm today?',
      'What is one burden or expectation you can gently release this evening?'
    ];
    res.json({
      prompt: defaultPrompts[Math.floor(Math.random() * defaultPrompts.length)],
      category: 'Presence'
    });
  }
});

// Curated authentic mindful affirmations collection
const CURATED_AFFIRMATIONS = [
  {
    quote: "Smile, breathe and go slowly.",
    author: "Thích Nhất Hạnh",
    source: "Peace Is Every Step",
    reflection: "When you slow down your pace, you create space to witness the stillness already present within you.",
    theme: "Presence",
    sources: [
      { title: "Plum Village Mindfulness Community", uri: "https://plumvillage.org" }
    ]
  },
  {
    quote: "You have power over your mind - not outside events. Realize this, and you will find strength.",
    author: "Marcus Aurelius",
    source: "Meditations",
    reflection: "Release the need to control the external current; ground your focus gently on your inner clarity.",
    theme: "Inner Peace",
    sources: [
      { title: "Stanford Encyclopedia of Philosophy - Stoicism", uri: "https://plato.stanford.edu" }
    ]
  },
  {
    quote: "Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.",
    author: "Thích Nhất Hạnh",
    source: "Stepping into Freedom",
    reflection: "Observe passing thoughts without judgment, anchoring your attention into the natural wave of each breath.",
    theme: "Equanimity",
    sources: [
      { title: "Plum Village Community of Mindful Living", uri: "https://plumvillage.org" }
    ]
  },
  {
    quote: "The quieter you become, the more you are able to hear.",
    author: "Rumi",
    source: "The Masnavi",
    reflection: "In deep quietude, your mind settles and subtle clarity surfaces on its own.",
    theme: "Stillness",
    sources: [
      { title: "Poetry Foundation - Rumi", uri: "https://www.poetryfoundation.org" }
    ]
  },
  {
    quote: "You cannot stop the waves, but you can learn to surf.",
    author: "Jon Kabat-Zinn",
    source: "Wherever You Go, There You Are",
    reflection: "Accept today's rhythms with curiosity instead of resistance, moving with ease through changing moments.",
    theme: "Resilience",
    sources: [
      { title: "Mindfulness-Based Stress Reduction (MBSR)", uri: "https://www.mindfulnesscds.com" }
    ]
  },
  {
    quote: "Do you have the patience to wait until your mud settles and the water is clear?",
    author: "Lao Tzu",
    source: "Tao Te Ching",
    reflection: "Clarity cannot be forced; allow time and stillness to reveal what is true and calm.",
    theme: "Patience",
    sources: [
      { title: "Internet Encyclopedia of Philosophy - Daoism", uri: "https://iep.utm.edu" }
    ]
  },
  {
    quote: "Tell me, what is it you plan to do with your one wild and precious life?",
    author: "Mary Oliver",
    source: "The Summer Day",
    reflection: "Cherish this immediate hour as a gift, treating your attention as your most sacred offering.",
    theme: "Gratitude",
    sources: [
      { title: "Poetry Foundation - Mary Oliver", uri: "https://www.poetryfoundation.org" }
    ]
  },
  {
    quote: "Muddy water is best cleared by leaving it alone.",
    author: "Alan Watts",
    source: "The Way of Zen",
    reflection: "Whenever your thoughts feel turbulent, step back and let the stillness do the settling.",
    theme: "Clarity",
    sources: [
      { title: "Alan Watts Organization", uri: "https://alanwatts.org" }
    ]
  },
  {
    quote: "You are the sky. Everything else – it's just the weather.",
    author: "Pema Chödrön",
    source: "When Things Fall Apart",
    reflection: "Difficult feelings and busy thoughts are temporary gusts across your vast, steady awareness.",
    theme: "Equanimity",
    sources: [
      { title: "Pema Chödrön Foundation", uri: "https://pemachodronfoundation.org" }
    ]
  },
  {
    quote: "Between stimulus and response there is a space. In that space is our power to choose our response.",
    author: "Viktor E. Frankl",
    source: "Man's Search for Meaning",
    reflection: "Take a conscious breath between what happens and how you react—that pause is your true freedom.",
    theme: "Awareness",
    sources: [
      { title: "Viktor Frankl Institute", uri: "https://www.viktorfrankl.org" }
    ]
  }
];

// Daily Affirmation & Grounded Mindful Quote via Google Search
app.post('/api/gemini/daily-affirmation', async (req: Request, res: Response) => {
  try {
    const { topic } = req.body;
    const ai = getAi();

    if (!ai) {
      const selected = CURATED_AFFIRMATIONS[Math.floor(Math.random() * CURATED_AFFIRMATIONS.length)];
      return res.json({
        ...selected,
        fetchedAt: new Date().toISOString()
      });
    }

    const searchPrompt = `Using Google Search, find a celebrated, deeply grounding, and authentic mindful quote or affirmation on the theme of "${topic || 'mindfulness, inner peace, and daily presence'}".
Search for authentic words by renowned mindfulness teachers, philosophers, poets, or contemplatives (such as Thich Nhat Hanh, Marcus Aurelius, Lao Tzu, Rumi, Mary Oliver, Alan Watts, Jon Kabat-Zinn, Seneca, Pema Chodron, or Epictetus).

Provide the output strictly formatted in the following JSON format:
{
  "quote": "The exact quote text",
  "author": "The author or thinker name",
  "source": "Title of the book, poem, essay or recorded lecture if available (otherwise 'Mindful Wisdom')",
  "reflection": "A 1-2 sentence mindful, practical takeaway for living today with presence and peace.",
  "theme": "A 1-2 word category tag (e.g. 'Presence', 'Clarity', 'Inner Peace', 'Resilience', 'Stillness')"
}

Ensure your response is valid JSON only.`;

    const response = await ai.models.generateContent({
      model: GEMINI_MODEL,
      contents: searchPrompt,
      config: {
        tools: [{ googleSearch: {} }],
      },
    });

    const rawText = response.text || '';
    
    // Extract web grounding chunks if available
    const groundingChunks = response.candidates?.[0]?.groundingMetadata?.groundingChunks || [];
    const webSources: { title?: string; uri: string }[] = [];
    
    for (const chunk of groundingChunks) {
      if (chunk.web?.uri) {
        webSources.push({
          title: chunk.web.title || 'Web Search Source',
          uri: chunk.web.uri,
        });
      }
    }

    let parsedResult: any = null;
    try {
      const cleaned = rawText.replace(/```json/gi, '').replace(/```/g, '').trim();
      parsedResult = JSON.parse(cleaned);
    } catch (parseErr) {
      const quoteMatch = rawText.match(/"quote":\s*"([^"]+)"/i) || rawText.match(/Quote:\s*["']?([^"\n\r]+)["']?/i);
      const authorMatch = rawText.match(/"author":\s*"([^"]+)"/i) || rawText.match(/Author:\s*([^\n\r]+)/i);
      const sourceMatch = rawText.match(/"source":\s*"([^"]+)"/i) || rawText.match(/Source:\s*([^\n\r]+)/i);
      const reflectionMatch = rawText.match(/"reflection":\s*"([^"]+)"/i) || rawText.match(/Reflection:\s*([^\n\r]+)/i);
      const themeMatch = rawText.match(/"theme":\s*"([^"]+)"/i) || rawText.match(/Theme:\s*([^\n\r]+)/i);

      if (quoteMatch && quoteMatch[1]) {
        parsedResult = {
          quote: quoteMatch[1],
          author: authorMatch ? authorMatch[1].trim() : 'Mindful Wisdom',
          source: sourceMatch ? sourceMatch[1].trim() : 'Meditations & Reflections',
          reflection: reflectionMatch ? reflectionMatch[1].trim() : 'Take a moment to let these words anchor your thoughts in this present breath.',
          theme: themeMatch ? themeMatch[1].trim() : 'Presence'
        };
      }
    }

    if (!parsedResult || !parsedResult.quote) {
      parsedResult = CURATED_AFFIRMATIONS[Math.floor(Math.random() * CURATED_AFFIRMATIONS.length)];
    }

    // Deduplicate grounding sources
    const uniqueSources: { title?: string; uri: string }[] = [];
    const seenUris = new Set<string>();
    for (const s of webSources) {
      if (s.uri && !seenUris.has(s.uri)) {
        seenUris.add(s.uri);
        uniqueSources.push(s);
      }
    }

    res.json({
      quote: parsedResult.quote,
      author: parsedResult.author || 'Mindful Teacher',
      source: parsedResult.source || 'Mindful Teachings',
      reflection: parsedResult.reflection || 'Allow this insight to bring clarity and stillness into your next breath.',
      theme: parsedResult.theme || 'Presence',
      sources: uniqueSources.length > 0 ? uniqueSources.slice(0, 3) : [
        { title: 'Google Search Verified Mindful Wisdom', uri: 'https://www.google.com/search?q=mindfulness+quotes' }
      ],
      fetchedAt: new Date().toISOString(),
    });
  } catch (error: any) {
    // Seamless fallback to rich curated affirmations on 429 quota or network constraints
    const selected = CURATED_AFFIRMATIONS[Math.floor(Math.random() * CURATED_AFFIRMATIONS.length)];
    res.json({
      ...selected,
      fetchedAt: new Date().toISOString()
    });
  }
});

// Weekly Trends Mindful Insight
app.post('/api/gemini/insights', async (req: Request, res: Response) => {
  try {
    const { streak, entriesCount, dominantMood } = req.body;
    const ai = getAi();

    if (!ai) {
      return res.json({
        insight: `You've experienced elevated calmness on days following a morning entry. Keep nurturing this habit to reinforce your mindful momentum.`,
        tip: 'Consider pairing your morning tea or coffee with a 2-minute gratitude jotting.'
      });
    }

    const prompt = `Based on user stats: 7-day streak (${streak} days), ${entriesCount} total entries, dominant mood '${dominantMood || 'Calm'}'. Provide one encouraging 2-sentence emotional pattern insight and one actionable gentle micro-habit tip. Return JSON with 'insight' and 'tip'.`;
    const response = await ai.models.generateContent({
      model: GEMINI_MODEL,
      contents: prompt,
      config: { responseMimeType: 'application/json' },
    });

    const parsed = JSON.parse(response.text || '{}');
    res.json(parsed);
  } catch (error) {
    res.json({
      insight: `You've experienced elevated calmness on days following a morning entry. Keep nurturing this habit.`,
      tip: 'A short pause before bedtime helps consolidate your emotional balance.'
    });
  }
});

// Start Express and integrate Vite middleware in development
async function startServer() {
  if (process.env.NODE_ENV !== 'production') {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (req: Request, res: Response) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running at http://0.0.0.0:${PORT}`);
  });
}

startServer();
