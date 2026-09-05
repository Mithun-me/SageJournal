import React, { useState, useEffect, useRef } from 'react';
import { Play, Pause, RotateCcw, Wind, Volume2, VolumeX, Send } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';

export const SanctuaryScreen: React.FC = () => {
  const [technique, setTechnique] = useState<'box' | 'calm' | 'relax'>('box');
  const [isActive, setIsActive] = useState(false);
  const [phase, setPhase] = useState<'Inhale' | 'Hold' | 'Exhale' | 'Rest'>('Inhale');
  const [phaseSecondsLeft, setPhaseSecondsLeft] = useState(4);
  const [totalCycles, setTotalCycles] = useState(0);
  const [soundEnabled, setSoundEnabled] = useState(false);
  const [thoughtInput, setThoughtInput] = useState('');
  const [releasedThoughts, setReleasedThoughts] = useState<string[]>([]);

  const audioCtxRef = useRef<AudioContext | null>(null);

  const playChime = (freq = 432) => {
    if (!soundEnabled) return;
    try {
      if (!audioCtxRef.current) {
        const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
        if (AudioCtx) audioCtxRef.current = new AudioCtx();
      }
      const ctx = audioCtxRef.current;
      if (!ctx) return;
      if (ctx.state === 'suspended') ctx.resume();

      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, ctx.currentTime);
      gain.gain.setValueAtTime(0.001, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.15, ctx.currentTime + 0.1);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 1.5);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 1.6);
    } catch (e) {
      // Ignore web audio warnings
    }
  };

  const configs = {
    box: { inhale: 4, hold1: 4, exhale: 4, hold2: 4, label: 'Box Breathing (4-4-4-4)', desc: 'Stabilizes focus and balances the nervous system.' },
    calm: { inhale: 5, hold1: 2, exhale: 6, hold2: 1, label: 'Deep Calm (5-2-6)', desc: 'Lengthens exhale to activate calming vagal response.' },
    relax: { inhale: 4, hold1: 7, exhale: 8, hold2: 0, label: '4-7-8 Deep Rest', desc: 'Promotes somatic relaxation and eases mental tension.' },
  };

  const currentConfig = configs[technique];

  useEffect(() => {
    let timer: any;
    if (isActive) {
      timer = setInterval(() => {
        setPhaseSecondsLeft((prev) => {
          if (prev <= 1) {
            triggerHaptic('light');
            if (phase === 'Inhale') {
              playChime(528);
              setPhase('Hold');
              return currentConfig.hold1 || 1;
            } else if (phase === 'Hold') {
              playChime(396);
              setPhase('Exhale');
              return currentConfig.exhale;
            } else if (phase === 'Exhale') {
              if (currentConfig.hold2 > 0) {
                setPhase('Rest');
                return currentConfig.hold2;
              } else {
                setTotalCycles((c) => c + 1);
                playChime(432);
                setPhase('Inhale');
                return currentConfig.inhale;
              }
            } else {
              setTotalCycles((c) => c + 1);
              playChime(432);
              setPhase('Inhale');
              return currentConfig.inhale;
            }
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => clearInterval(timer);
  }, [isActive, phase, technique, currentConfig, soundEnabled]);

  const handleToggleActive = () => {
    triggerHaptic('medium');
    if (!isActive) {
      setPhase('Inhale');
      setPhaseSecondsLeft(currentConfig.inhale);
      playChime(432);
    }
    setIsActive(!isActive);
  };

  const handleReset = () => {
    triggerHaptic('light');
    setIsActive(false);
    setPhase('Inhale');
    setPhaseSecondsLeft(currentConfig.inhale);
    setTotalCycles(0);
  };

  const handleReleaseThought = (e: React.FormEvent) => {
    e.preventDefault();
    if (!thoughtInput.trim()) return;
    triggerHaptic('success');
    setReleasedThoughts((prev) => [thoughtInput.trim(), ...prev.slice(0, 3)]);
    setThoughtInput('');
    playChime(639);
  };

  const getCircleScale = () => {
    if (!isActive) return 'scale-90';
    if (phase === 'Inhale') return 'scale-110 duration-[4000ms]';
    if (phase === 'Hold') return 'scale-110';
    if (phase === 'Exhale') return 'scale-85 duration-[4000ms]';
    return 'scale-85';
  };

  return (
    <div className="w-full max-w-2xl mx-auto space-y-6 animate-fadeIn pb-16">
      {/* Header Banner */}
      <section className="text-center space-y-1">
        <h2 className="text-2xl font-bold text-white font-['Manrope']">
          Breathwork Sanctuary
        </h2>
        <p className="text-xs text-[#908fa0]">
          Find physical stillness through rhythmic, mindful breathing.
        </p>
      </section>

      {/* Technique Segmented Control */}
      <div className="flex justify-center p-1 bg-[#0e1022] rounded-xl border border-white/[0.08] max-w-sm mx-auto">
        {(['box', 'calm', 'relax'] as const).map((t) => (
          <button
            key={t}
            onClick={() => {
              triggerHaptic('light');
              setTechnique(t);
              handleReset();
            }}
            className={`flex-1 py-1.5 px-3 rounded-lg text-xs font-semibold transition-all ${
              technique === t
                ? 'bg-[#4fdbc8] text-[#003731] shadow'
                : 'text-[#c7c4d7] hover:text-white'
            }`}
          >
            {t === 'box' ? 'Box 4-4' : t === 'calm' ? 'Calm 5-6' : '4-7-8 Flow'}
          </button>
        ))}
      </div>

      {/* Interactive Breath Visualizer */}
      <section className="relative flex flex-col items-center justify-center min-h-[300px] bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-6 border border-white/[0.08]">
        {/* Breathing Orb */}
        <div
          className={`relative w-44 h-44 rounded-full flex flex-col items-center justify-center transition-all ease-in-out border border-white/10 ${getCircleScale()} ${
            phase === 'Inhale'
              ? 'bg-[#1b2238] border-[#4fdbc8]/40'
              : phase === 'Exhale'
              ? 'bg-[#102422] border-[#4fdbc8]/20'
              : 'bg-[#1e1b38] border-[#8083ff]/30'
          }`}
        >
          <div className="w-32 h-32 rounded-full bg-[#0a0c1a]/80 backdrop-blur-md flex flex-col items-center justify-center border border-white/10">
            <span className="text-xs font-bold uppercase tracking-wider text-[#4fdbc8] font-['Manrope']">
              {isActive ? phase : 'Ready'}
            </span>
            <span className="text-3xl font-extrabold text-white my-0.5 font-['Manrope']">
              {isActive ? `${phaseSecondsLeft}s` : '4s'}
            </span>
            <span className="text-[10px] text-[#908fa0] font-medium">
              {isActive ? `Cycle ${totalCycles + 1}` : 'Tap Start'}
            </span>
          </div>
        </div>

        {/* Action Controls */}
        <div className="mt-6 flex items-center gap-3 relative z-10">
          <button
            onClick={handleToggleActive}
            className={`px-6 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 transition-all active:scale-95 ${
              isActive
                ? 'bg-white/10 text-white border border-white/20 hover:bg-white/15'
                : 'bg-[#4fdbc8] text-[#003731] shadow'
            }`}
          >
            {isActive ? (
              <>
                <Pause className="w-3.5 h-3.5" /> Pause
              </>
            ) : (
              <>
                <Play className="w-3.5 h-3.5 fill-current" /> Begin
              </>
            )}
          </button>

          <button
            onClick={handleReset}
            title="Reset Session"
            className="p-2.5 rounded-xl bg-white/5 text-[#c7c4d7] hover:text-white border border-white/[0.08] active:scale-95"
          >
            <RotateCcw className="w-3.5 h-3.5" />
          </button>

          <button
            onClick={() => {
              triggerHaptic('light');
              setSoundEnabled(!soundEnabled);
              if (!soundEnabled) playChime(432);
            }}
            title={soundEnabled ? 'Mute Chime' : 'Enable Chime'}
            className={`p-2.5 rounded-xl border transition-all active:scale-95 ${
              soundEnabled
                ? 'bg-[#4fdbc8]/15 text-[#4fdbc8] border-[#4fdbc8]/40'
                : 'bg-white/5 text-[#908fa0] border-white/[0.08]'
            }`}
          >
            {soundEnabled ? <Volume2 className="w-3.5 h-3.5" /> : <VolumeX className="w-3.5 h-3.5" />}
          </button>
        </div>

        <p className="mt-3 text-[11px] text-[#908fa0] text-center max-w-xs">
          {currentConfig.desc}
        </p>
      </section>

      {/* Thought Release Box */}
      <section className="bg-[#0e1022]/80 backdrop-blur-xl rounded-2xl p-4 border border-white/[0.08] space-y-3">
        <div className="flex items-center gap-2 text-[#4fdbc8]">
          <Wind className="w-4 h-4" />
          <h3 className="text-sm font-bold text-white font-['Manrope']">
            Thought Release Pond
          </h3>
        </div>
        <p className="text-xs text-[#908fa0]">
          Write down any thoughts weighing on your mind to let them go.
        </p>

        <form onSubmit={handleReleaseThought} className="flex gap-2">
          <input
            type="text"
            value={thoughtInput}
            onChange={(e) => setThoughtInput(e.target.value)}
            placeholder="Type a worry or tension to let go..."
            className="flex-1 bg-[#0a0c1a] border border-white/10 rounded-xl px-4 py-2 text-xs text-white placeholder-[#908fa0] focus:outline-none focus:border-[#4fdbc8]"
          />
          <button
            type="submit"
            className="px-4 py-2 rounded-xl bg-[#4fdbc8] text-[#003731] font-bold text-xs hover:bg-[#71f8e4] active:scale-95 transition-all flex items-center gap-1 shrink-0"
          >
            <Send className="w-3 h-3" /> Let Go
          </button>
        </form>

        {releasedThoughts.length > 0 && (
          <div className="flex flex-wrap gap-1.5 pt-1">
            {releasedThoughts.map((thought, idx) => (
              <span
                key={idx}
                className="text-[11px] px-2.5 py-1 rounded-lg bg-white/5 border border-white/[0.06] text-[#c7c4d7]/70 italic"
              >
                🌊 "{thought}" — released
              </span>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};
