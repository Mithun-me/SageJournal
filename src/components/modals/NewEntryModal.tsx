import React, { useState, useEffect, useRef } from 'react';
import { JournalEntry, MoodType } from '../../types';
import {
  X,
  Sparkles,
  Image as ImageIcon,
  MapPin,
  CheckCircle,
  RefreshCw,
  Mic,
  MicOff,
  Volume2,
  Radio,
  Trash2,
} from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';

interface NewEntryModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (entry: Omit<JournalEntry, 'id' | 'timestamp'>) => void;
  currentMood?: MoodType;
  initialPrompt?: string;
  initialContent?: string;
}

export const NewEntryModal: React.FC<NewEntryModalProps> = ({
  isOpen,
  onClose,
  onSave,
  currentMood = 'Calm',
  initialPrompt,
  initialContent = '',
}) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState(initialContent);
  const [mood, setMood] = useState<MoodType>(currentMood);
  const [selectedPrompt, setSelectedPrompt] = useState(
    initialPrompt || 'What is a small detail you noticed today that brought you an unexpected sense of calm?'
  );

  useEffect(() => {
    if (initialPrompt) setSelectedPrompt(initialPrompt);
    if (initialContent) setContent(initialContent);
  }, [initialPrompt, initialContent, isOpen]);

  const [isGeneratingPrompt, setIsGeneratingPrompt] = useState(false);
  const [imageUrl, setImageUrl] = useState<string | undefined>(undefined);
  const [location, setLocation] = useState<string>('Home Sanctuary');
  const [hasAudio, setHasAudio] = useState(false);
  const [audioDuration, setAudioDuration] = useState('1:30');

  // Speech-to-Text Transcription State
  const [isTranscribing, setIsTranscribing] = useState(false);
  const [interimTranscript, setInterimTranscript] = useState('');
  const [speechError, setSpeechError] = useState<string | null>(null);
  const recognitionRef = useRef<any>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const [isGeneratingReflection, setIsGeneratingReflection] = useState(false);
  const [generatedReflection, setGeneratedReflection] = useState<{
    reflection: string;
    themes: string[];
    suggestedAffirmation: string;
  } | null>(null);

  // Stop transcription on modal close or unmount
  useEffect(() => {
    return () => {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.stop();
        } catch (e) {
          // ignore
        }
      }
    };
  }, []);

  const startTranscription = () => {
    triggerHaptic('medium');
    setSpeechError(null);

    const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

    if (!SpeechRecognition) {
      setSpeechError('Speech recognition is not supported in this browser.');
      return;
    }

    try {
      if (recognitionRef.current) {
        try {
          recognitionRef.current.stop();
        } catch (e) {}
      }

      const recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'en-US';

      recognition.onstart = () => {
        setIsTranscribing(true);
        setSpeechError(null);
      };

      recognition.onresult = (event: any) => {
        let finalChunk = '';
        let interimChunk = '';

        for (let i = event.resultIndex; i < event.results.length; ++i) {
          const transcriptPiece = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            finalChunk += transcriptPiece;
          } else {
            interimChunk += transcriptPiece;
          }
        }

        if (finalChunk) {
          setContent((prev) => {
            const trimmed = prev.trimEnd();
            const addition = finalChunk.trim();
            if (!trimmed) return addition;
            return `${trimmed} ${addition}`;
          });
          setHasAudio(true);
        }

        setInterimTranscript(interimChunk);
      };

      recognition.onerror = (event: any) => {
        if (event.error === 'not-allowed') {
          setSpeechError('Microphone access was denied. Please allow microphone permissions in your browser.');
        } else if (event.error === 'no-speech') {
          // Soft timeout, keep listening
        } else {
          setSpeechError(`Speech error: ${event.error || 'Unable to capture speech'}`);
        }
        setIsTranscribing(false);
      };

      recognition.onend = () => {
        setIsTranscribing(false);
        setInterimTranscript('');
      };

      recognitionRef.current = recognition;
      recognition.start();
    } catch (err: any) {
      console.error('Speech recognition start error:', err);
      setSpeechError(err.message || 'Could not start microphone');
      setIsTranscribing(false);
    }
  };

  const stopTranscription = () => {
    triggerHaptic('light');
    if (recognitionRef.current) {
      try {
        recognitionRef.current.stop();
      } catch (e) {}
    }
    setIsTranscribing(false);
    setInterimTranscript('');
  };

  const toggleTranscription = () => {
    if (isTranscribing) {
      stopTranscription();
    } else {
      startTranscription();
    }
  };

  if (!isOpen) return null;

  const handleShufflePrompt = async () => {
    triggerHaptic('light');
    setIsGeneratingPrompt(true);
    try {
      const res = await fetch('/api/gemini/prompt', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ currentMood: mood }),
      });
      const data = await res.json();
      if (data.prompt) setSelectedPrompt(data.prompt);
    } catch (e) {
      console.error(e);
    } finally {
      setIsGeneratingPrompt(false);
    }
  };

  const handleGenerateReflection = async () => {
    if (!content.trim() && !title.trim()) {
      alert('Please write a few thoughts first to generate a mindful reflection.');
      return;
    }
    triggerHaptic('medium');
    setIsGeneratingReflection(true);
    try {
      const res = await fetch('/api/gemini/reflect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          content,
          mood,
          prompt: selectedPrompt,
        }),
      });
      const data = await res.json();
      if (data.reflection) {
        setGeneratedReflection({
          reflection: data.reflection,
          themes: data.themes || ['Clarity', 'Mindfulness', 'Presence'],
          suggestedAffirmation: data.suggestedAffirmation || 'I am present with each breath.',
        });
        triggerHaptic('success');
      }
    } catch (e) {
      console.error(e);
    } finally {
      setIsGeneratingReflection(false);
    }
  };

  const handleSave = () => {
    triggerHaptic('success');
    const now = new Date();
    const formattedDate = now.toLocaleDateString('en-US', { month: 'short', day: '2-digit' }).toUpperCase();
    const formattedTime = now.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });

    const moodEmojis: Record<MoodType, string> = {
      Calm: '😌',
      Joy: '✨',
      Reflective: '🤔',
      Low: '🌧️',
      Grounded: '🌱',
      Energetic: '⚡',
    };

    onSave({
      title: title.trim() || 'Mindful Reflection',
      content: content.trim() || 'Reflecting on today in quiet stillness.',
      date: formattedDate,
      time: formattedTime,
      mood,
      moodEmoji: moodEmojis[mood] || '✨',
      imageUrl,
      location,
      isAudio: hasAudio,
      audioDuration: hasAudio ? audioDuration : undefined,
      isFavorite: false,
      tags: generatedReflection?.themes || ['Mindfulness', mood],
      aiReflection: generatedReflection?.reflection,
      aiThemes: generatedReflection?.themes,
      aiAffirmation: generatedReflection?.suggestedAffirmation,
      gridSpan: imageUrl ? 'span-2-row-1' : 'span-1-row-1',
    });

    onClose();
  };

  const imagePresets = [
    { label: 'Morning Tea', url: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBg5MkIGGWxv0cjriTxv3Y6ytbCnpLcNR9o90ftMTUFox1RbNiu1XHcOgLNJ8YD_lZwk75Lb0tKkYB4_qhNpf-dcw2juu47nHsV31lEyKV_XUZLIdzoW4bHLvdw9t1xcJlkrRSg07PgoRVAUV55W3jPzlpOgoY37GHbtaV299NmXutMEu57ZRtr4fsxGItnhVR4iYCjhtIHPamYreO70pRGlZINGoUYUXscWqK1tbkiBjPBrJWH2EY_' },
    { label: 'Sunset Horizon', url: 'https://lh3.googleusercontent.com/aida-public/AB6AXuD4nvJC_3Bc4QZH1mpmrnyL-QmFfR8xLe2SQT9CTEDkUCij3Dk3DbEHVxN4R7RFX_FKJ0x6MhS5jsSTviybFnk06vQ0bmTTJkNW6R-bkfBTY7S3g5HRG5jHhhDg8N8uapH_lGDgSbU4OZIDIPgWibMJwCA39gmSfaetiR6uPT-BD9yHskVIOs87A5k9ipEfhgdgGpDJUFUvymHvOwVA8QJyIP4UA9HAszYxN0KxHC8_vdiseCZQ8Dsb' },
    { label: 'Luminous Glass', url: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAUhwwp6hbAk4364IPz5oswu0UXluCPelA2XTDhRxI16sjx3n7CRbfLYdX3iSetyTUVVWVbBEpqH4uSQYKZjYU6fhbgMDrS8KyhHciXwIOrAnDR14WgIk04PCv8D3GIdYddahYQTeOqzWvUNsKCXqOjyve7ODiaG1ZVEKNA9shfaSftc7sWB0MVPp3aAj2XN3qKVX4Pk_yYcYthAeN9gKVNkK2otRDjtuF-gFTxsaAlKjApBz7z5WY1' },
    { label: 'Workspace', url: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDiy45QgpCe-ThoyuQvc_P9C8VNH-0bmcR9rlA1akq3SJ1JiNim4VGIB0gSDjQ9O-S-Iqci6v7EFCw9Z1lMlSyEHzqpulRddcBrGHK-iF3bo2FW0HkHwLT8nYr7i9kETx3Qx9b5CQrO1Ox2MvZWHOtZFmm8z_TqoK33FUh_mpgJLbjpWnek6xW1dhweoFUPoHayCCPQDwwx7sKkr28Ug97js5y7CdGv7wuzu3OkLXgGnQXfFfBtGGBz' },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-5 bg-black/80 backdrop-blur-md overflow-y-auto animate-fadeIn">
      <div className="w-full max-w-lg bg-[#0e1022] border border-white/10 rounded-2xl p-5 shadow-2xl relative flex flex-col gap-4 my-auto max-h-[90vh] overflow-y-auto no-scrollbar">
        {/* Top Header */}
        <div className="flex justify-between items-center pb-2 border-b border-white/[0.08]">
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                triggerHaptic('light');
                onClose();
              }}
              className="p-1 rounded-lg hover:bg-white/10 text-[#c7c4d7] hover:text-white transition-all"
            >
              <X className="w-5 h-5" />
            </button>
            <h2 className="text-lg font-bold text-white font-['Manrope']">
              New Memory
            </h2>
          </div>

          <div className="flex items-center gap-2">
            <select
              value={mood}
              onChange={(e) => {
                triggerHaptic('light');
                setMood(e.target.value as MoodType);
              }}
              className="bg-[#1b203a] text-xs text-[#71f8e4] font-semibold border border-white/10 rounded-xl px-2.5 py-1.5 focus:outline-none"
            >
              <option value="Calm">😌 Calm</option>
              <option value="Joy">✨ Joy</option>
              <option value="Reflective">🤔 Reflective</option>
              <option value="Low">🌧️ Low</option>
            </select>
          </div>
        </div>

        {/* Daily Prompt */}
        <section className="bg-[#14182e] rounded-xl p-3.5 border border-white/[0.08] flex flex-col gap-1.5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-[#4fdbc8]" />
              <span className="text-[10px] font-bold uppercase tracking-wider text-[#4fdbc8] font-['Manrope']">
                Thought Prompt
              </span>
            </div>
            <button
              onClick={handleShufflePrompt}
              title="Get Fresh Prompt"
              className="text-[11px] text-[#908fa0] hover:text-white flex items-center gap-1 hover:bg-white/5 px-2 py-0.5 rounded-md transition-all"
            >
              <RefreshCw className={`w-3 h-3 ${isGeneratingPrompt ? 'animate-spin' : ''}`} /> Shuffle
            </button>
          </div>
          <p className="text-xs text-[#e1e1f6] leading-relaxed">
            {selectedPrompt}
          </p>
        </section>

        {/* Journal Main Input Container */}
        <section className="bg-[#0a0c1a] border border-white/[0.08] rounded-xl overflow-hidden flex flex-col flex-1 min-h-[220px]">
          {/* Active Voice Dictation Banner */}
          {isTranscribing && (
            <div className="bg-gradient-to-r from-[#4fdbc8]/20 via-[#4fdbc8]/10 to-[#8083ff]/15 px-3.5 py-2 border-b border-[#4fdbc8]/30 flex items-center justify-between animate-fadeIn">
              <div className="flex items-center gap-2">
                <span className="relative flex h-2.5 w-2.5">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#4fdbc8] opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-[#4fdbc8]"></span>
                </span>
                <div className="flex items-center gap-1.5">
                  <span className="text-xs font-semibold text-[#71f8e4]">Listening & Transcribing...</span>
                  {/* Visual Soundwave Bars */}
                  <div className="flex items-center gap-0.5 ml-1">
                    <span className="w-1 h-3 bg-[#4fdbc8] rounded-full animate-bounce [animation-delay:-0.3s]"></span>
                    <span className="w-1 h-4 bg-[#4fdbc8] rounded-full animate-bounce [animation-delay:-0.15s]"></span>
                    <span className="w-1 h-2 bg-[#4fdbc8] rounded-full animate-bounce [animation-delay:-0.45s]"></span>
                    <span className="w-1 h-5 bg-[#4fdbc8] rounded-full animate-bounce [animation-delay:0s]"></span>
                  </div>
                </div>
              </div>
              <button
                type="button"
                id="stop-dictation-btn"
                onClick={stopTranscription}
                className="px-2 py-0.5 rounded-lg bg-[#4fdbc8]/20 hover:bg-[#4fdbc8]/30 text-[#71f8e4] text-[11px] font-bold border border-[#4fdbc8]/40 transition-all flex items-center gap-1"
              >
                <MicOff className="w-3 h-3" />
                <span>Done</span>
              </button>
            </div>
          )}

          {/* Speech Error Banner */}
          {speechError && (
            <div className="bg-red-500/15 border-b border-red-500/30 px-3.5 py-1.5 flex items-center justify-between text-[11px] text-red-200">
              <span>{speechError}</span>
              <button onClick={() => setSpeechError(null)} className="hover:text-white font-bold ml-2">✕</button>
            </div>
          )}

          <div className="p-4 flex-1 flex flex-col">
            <input
              type="text"
              id="entry-title-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Title (Optional)"
              className="w-full bg-transparent border-none text-base font-bold text-white placeholder-[#908fa0] focus:outline-none p-0 mb-2 font-['Manrope']"
            />
            <div className="relative flex-1 flex flex-col">
              <textarea
                ref={textareaRef}
                id="entry-content-textarea"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder={
                  isTranscribing
                    ? 'Speak naturally... your voice will be transcribed here in real-time.'
                    : 'Start writing or tap the microphone below to transcribe your thoughts directly...'
                }
                rows={5}
                className="w-full flex-1 bg-transparent border-none text-xs text-[#e1e1f6] placeholder-[#908fa0] focus:outline-none resize-none p-0 leading-relaxed font-['Be_Vietnam_Pro']"
              />

              {/* Interim Real-time Speech Preview */}
              {isTranscribing && interimTranscript && (
                <div className="mt-1 text-xs text-[#71f8e4]/80 italic bg-white/[0.03] p-1.5 rounded-lg border border-[#4fdbc8]/20 flex items-center gap-1.5">
                  <span className="w-1.5 h-1.5 rounded-full bg-[#4fdbc8] animate-ping shrink-0" />
                  <span className="truncate">"{interimTranscript}"</span>
                </div>
              )}
            </div>
          </div>

          {/* Attached Image Preview */}
          {imageUrl && (
            <div className="px-4 pb-2.5 flex items-center gap-2.5">
              <div className="w-14 h-10 rounded-lg overflow-hidden border border-white/10 relative group">
                <img src={imageUrl} alt="Attached" className="w-full h-full object-cover" />
                <button
                  onClick={() => setImageUrl(undefined)}
                  className="absolute inset-0 bg-black/70 opacity-0 group-hover:opacity-100 flex items-center justify-center text-white text-[10px]"
                >
                  Remove
                </button>
              </div>
              <span className="text-[11px] text-[#908fa0]">Attached Photo</span>
            </div>
          )}

          {/* Audio Note Preview */}
          {hasAudio && (
            <div className="mx-4 mb-2.5 p-2 rounded-lg bg-white/5 border border-white/10 flex items-center justify-between text-xs">
              <div className="flex items-center gap-1.5 text-[#4fdbc8]">
                <Volume2 className="w-3.5 h-3.5" />
                <span className="text-xs">Voice Dictated Entry</span>
              </div>
              <button
                onClick={() => setHasAudio(false)}
                className="text-[11px] text-[#908fa0] hover:text-red-300"
              >
                Remove
              </button>
            </div>
          )}

          {/* Toolbar */}
          <div className="border-t border-white/[0.08] px-3.5 py-2 bg-[#0e1022] flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              {/* Voice Transcription Mic Button */}
              <button
                type="button"
                id="voice-dictate-btn"
                onClick={toggleTranscription}
                title={isTranscribing ? "Stop Voice Transcription" : "Transcribe Speech with Microphone"}
                className={`px-2.5 py-1.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
                  isTranscribing
                    ? 'bg-red-500/20 text-red-300 border border-red-500/40 animate-pulse shadow-[0_0_12px_rgba(239,68,68,0.3)]'
                    : 'bg-[#4fdbc8]/15 hover:bg-[#4fdbc8]/25 text-[#71f8e4] border border-[#4fdbc8]/30 active:scale-95'
                }`}
              >
                {isTranscribing ? (
                  <>
                    <MicOff className="w-3.5 h-3.5 text-red-400" />
                    <span>Listening...</span>
                  </>
                ) : (
                  <>
                    <Mic className="w-3.5 h-3.5 text-[#4fdbc8]" />
                    <span>Transcribe Voice</span>
                  </>
                )}
              </button>

              <div className="relative group">
                <button
                  type="button"
                  title="Attach Photo"
                  className="p-1.5 rounded-lg text-[#908fa0] hover:text-[#4fdbc8] hover:bg-white/5"
                >
                  <ImageIcon className="w-4 h-4" />
                </button>
                <div className="absolute bottom-full left-0 mb-1 hidden group-hover:flex flex-col bg-[#14182e] border border-white/10 p-1.5 rounded-xl shadow-xl z-20 w-36 gap-0.5">
                  <span className="text-[10px] font-bold text-[#908fa0] px-1">Select Photo</span>
                  {imagePresets.map((p, idx) => (
                    <button
                      key={idx}
                      onClick={() => {
                        triggerHaptic('light');
                        setImageUrl(p.url);
                      }}
                      className="text-left text-xs p-1 rounded hover:bg-white/10 text-white truncate"
                    >
                      {p.label}
                    </button>
                  ))}
                </div>
              </div>

              <button
                type="button"
                onClick={() => {
                  const newLoc = prompt('Add Location Tag:', location);
                  if (newLoc) setLocation(newLoc);
                }}
                title="Add Location"
                className="p-1.5 rounded-lg text-[#908fa0] hover:text-[#4fdbc8] hover:bg-white/5"
              >
                <MapPin className="w-4 h-4" />
              </button>
            </div>

            <div className="flex items-center gap-2">
              {content.length > 0 && (
                <span className="text-[10px] text-[#908fa0]">
                  {content.trim().split(/\s+/).filter(Boolean).length} words
                </span>
              )}
              <span className="text-[11px] text-[#908fa0]">
                {location}
              </span>
            </div>
          </div>
        </section>

        {/* Reflection Preview */}
        {generatedReflection && (
          <section className="bg-[#14182e] border border-white/10 rounded-xl p-3.5 space-y-1.5 animate-fadeIn">
            <div className="flex items-center gap-1.5 text-[#4fdbc8]">
              <Sparkles className="w-3.5 h-3.5" />
              <span className="text-[11px] font-bold font-['Manrope'] uppercase tracking-wider">
                Reflection &amp; Insight
              </span>
            </div>
            <p className="text-xs text-[#e1e1f6] leading-relaxed">
              "{generatedReflection.reflection}"
            </p>
            {generatedReflection.suggestedAffirmation && (
              <p className="text-xs text-[#71f8e4] pt-1 border-t border-white/[0.08]">
                Affirmation: {generatedReflection.suggestedAffirmation}
              </p>
            )}
          </section>
        )}

        {/* Bottom Actions */}
        <section className="flex gap-2.5 pt-1">
          <button
            onClick={handleGenerateReflection}
            disabled={isGeneratingReflection}
            className="flex-1 py-2.5 px-4 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-white font-semibold text-xs flex items-center justify-center gap-1.5 transition-all active:scale-95 disabled:opacity-50"
          >
            <Sparkles className={`w-3.5 h-3.5 text-[#4fdbc8] ${isGeneratingReflection ? 'animate-spin' : ''}`} />
            {isGeneratingReflection ? 'Reflecting...' : 'Add Reflection'}
          </button>

          <button
            onClick={handleSave}
            className="flex-1 py-2.5 px-4 rounded-xl bg-[#4fdbc8] text-[#003731] font-bold text-xs flex items-center justify-center gap-1.5 hover:bg-[#71f8e4] active:scale-95 transition-all shadow"
          >
            Save Entry
            <CheckCircle className="w-3.5 h-3.5" />
          </button>
        </section>
      </div>
    </div>
  );
};
