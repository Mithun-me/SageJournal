import React, { useState } from 'react';
import { AppTheme, JournalEntry } from '../../types';
import { Palette, Sliders, Bell, Download, Trash2, Shield, Sparkles, Check, RefreshCw } from 'lucide-react';

interface SettingsScreenProps {
  theme: AppTheme;
  onSelectTheme: (theme: AppTheme) => void;
  entries: JournalEntry[];
  onResetData: () => void;
  shaderSpeed: number;
  onSetShaderSpeed: (speed: number) => void;
  shaderIntensity: number;
  onSetShaderIntensity: (intensity: number) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  theme,
  onSelectTheme,
  entries,
  onResetData,
  shaderSpeed,
  onSetShaderSpeed,
  shaderIntensity,
  onSetShaderIntensity,
}) => {
  const [dailyReminder, setDailyReminder] = useState(true);
  const [haptics, setHaptics] = useState(true);
  const [exportSuccess, setExportSuccess] = useState(false);

  const handleExportData = () => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(entries, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', `aura-journal-export-${new Date().toISOString().slice(0, 10)}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();

    setExportSuccess(true);
    setTimeout(() => setExportSuccess(false), 3000);
  };

  return (
    <div className="w-full max-w-2xl mx-auto space-y-6 animate-fadeIn pb-16">
      {/* Title */}
      <div>
        <h2 className="text-2xl font-extrabold text-white tracking-tight font-['Manrope']">
          App Settings &amp; Aura Customization
        </h2>
        <p className="text-xs text-[#c7c4d7]">
          Personalize your visual sanctuary, fluid shaders, and data preferences.
        </p>
      </div>

      {/* Visual Identity & Theme */}
      <section className="bg-[#0a0c1a]/60 backdrop-blur-[40px] rounded-3xl p-6 border border-white/15 shadow-[0_8px_32px_rgba(0,0,0,0.15)] space-y-4">
        <div className="flex items-center gap-2 text-[#4fdbc8]">
          <Palette className="w-4 h-4" />
          <h3 className="text-sm font-bold text-white font-['Manrope']">
            Aesthetic Themes
          </h3>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {/* Theme 1 */}
          <button
            onClick={() => onSelectTheme('liquid-glass')}
            className={`p-4 rounded-2xl border text-left transition-all ${
              theme === 'liquid-glass'
                ? 'border-[#4fdbc8] bg-white/10 shadow-[0_0_20px_rgba(79,219,200,0.3)] ring-1 ring-[#4fdbc8]'
                : 'border-white/10 bg-white/5 hover:border-white/20'
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-sm text-white font-['Manrope']">
                Liquid Glass Aura
              </span>
              {theme === 'liquid-glass' && <Check className="w-4 h-4 text-[#4fdbc8]" />}
            </div>
            <p className="text-xs text-[#c7c4d7] leading-relaxed">
              Deep oceanic midnight with luminous electric teal and vibrant indigo liquid refraction.
            </p>
            <div className="flex gap-2 mt-3">
              <div className="w-4 h-4 rounded-full bg-[#101221] border border-white/20" />
              <div className="w-4 h-4 rounded-full bg-[#4f46e5]" />
              <div className="w-4 h-4 rounded-full bg-[#4fdbc8]" />
            </div>
          </button>

          {/* Theme 2 */}
          <button
            onClick={() => onSelectTheme('deep-sea')}
            className={`p-4 rounded-2xl border text-left transition-all ${
              theme === 'deep-sea'
                ? 'border-[#71d7cd] bg-white/10 shadow-[0_0_20px_rgba(113,215,205,0.3)] ring-1 ring-[#71d7cd]'
                : 'border-white/10 bg-white/5 hover:border-white/20'
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-sm text-white font-['Manrope']">
                Deep Sea Hydro
              </span>
              {theme === 'deep-sea' && <Check className="w-4 h-4 text-[#71d7cd]" />}
            </div>
            <p className="text-xs text-[#c7c4d7] leading-relaxed">
              Serene abyssal aqua gradients inspired by restorative coastal deep water and soft sand.
            </p>
            <div className="flex gap-2 mt-3">
              <div className="w-4 h-4 rounded-full bg-[#004f56]" />
              <div className="w-4 h-4 rounded-full bg-[#85d3dd]" />
              <div className="w-4 h-4 rounded-full bg-[#FAF7F2]" />
            </div>
          </button>
        </div>
      </section>

      {/* WebGL Shader Motion Controls */}
      <section className="bg-[#0a0c1a]/60 backdrop-blur-[40px] rounded-3xl p-6 border border-white/15 shadow-[0_8px_32px_rgba(0,0,0,0.15)] space-y-4">
        <div className="flex items-center gap-2 text-[#4fdbc8]">
          <Sliders className="w-4 h-4" />
          <h3 className="text-sm font-bold text-white font-['Manrope']">
            Liquid Canvas Fluidity
          </h3>
        </div>

        <div className="space-y-4">
          <div>
            <div className="flex justify-between text-xs text-[#c7c4d7] mb-1">
              <span>Shader Flow Speed</span>
              <span>{shaderSpeed === 0.5 ? 'Gentle Flow' : shaderSpeed === 1.0 ? 'Balanced' : 'Dynamic'}</span>
            </div>
            <input
              type="range"
              min="0.3"
              max="2.0"
              step="0.1"
              value={shaderSpeed}
              onChange={(e) => onSetShaderSpeed(parseFloat(e.target.value))}
              className="w-full accent-[#4fdbc8] cursor-pointer"
            />
          </div>

          <div>
            <div className="flex justify-between text-xs text-[#c7c4d7] mb-1">
              <span>Wave Distortion Intensity</span>
              <span>{Math.round(shaderIntensity * 100)}%</span>
            </div>
            <input
              type="range"
              min="0.3"
              max="1.8"
              step="0.1"
              value={shaderIntensity}
              onChange={(e) => onSetShaderIntensity(parseFloat(e.target.value))}
              className="w-full accent-[#4fdbc8] cursor-pointer"
            />
          </div>
        </div>
      </section>

      {/* Preferences & Notifications */}
      <section className="bg-[#0a0c1a]/60 backdrop-blur-[40px] rounded-3xl p-6 border border-white/15 shadow-[0_8px_32px_rgba(0,0,0,0.15)] space-y-4">
        <div className="flex items-center gap-2 text-[#4fdbc8]">
          <Bell className="w-4 h-4" />
          <h3 className="text-sm font-bold text-white font-['Manrope']">
            Mindful Routine
          </h3>
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between p-3 rounded-xl bg-white/5 border border-white/10">
            <div>
              <p className="text-xs font-bold text-white">Daily Morning Check-in Prompt</p>
              <p className="text-[11px] text-[#c7c4d7]">Gentle notification at 8:00 AM</p>
            </div>
            <button
              onClick={() => setDailyReminder(!dailyReminder)}
              className={`w-11 h-6 rounded-full transition-colors relative ${
                dailyReminder ? 'bg-[#04b4a2]' : 'bg-white/20'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-white transition-transform absolute top-0.5 ${
                  dailyReminder ? 'left-5.5' : 'left-0.5'
                }`}
              />
            </button>
          </div>

          <div className="flex items-center justify-between p-3 rounded-xl bg-white/5 border border-white/10">
            <div>
              <p className="text-xs font-bold text-white">Micro-Interactions &amp; Haptic Glow</p>
              <p className="text-[11px] text-[#c7c4d7]">Subtle optical feedbacks on touches</p>
            </div>
            <button
              onClick={() => setHaptics(!haptics)}
              className={`w-11 h-6 rounded-full transition-colors relative ${
                haptics ? 'bg-[#04b4a2]' : 'bg-white/20'
              }`}
            >
              <div
                className={`w-5 h-5 rounded-full bg-white transition-transform absolute top-0.5 ${
                  haptics ? 'left-5.5' : 'left-0.5'
                }`}
              />
            </button>
          </div>
        </div>
      </section>

      {/* Data Management & Export */}
      <section className="bg-[#0a0c1a]/60 backdrop-blur-[40px] rounded-3xl p-6 border border-white/15 shadow-[0_8px_32px_rgba(0,0,0,0.15)] space-y-4">
        <div className="flex items-center gap-2 text-[#4fdbc8]">
          <Shield className="w-4 h-4" />
          <h3 className="text-sm font-bold text-white font-['Manrope']">
            Privacy &amp; Data Sanctuary
          </h3>
        </div>

        <p className="text-xs text-[#c7c4d7] leading-relaxed">
          Your reflections are stored locally in your browser sandbox. AI reflections are generated securely without retaining personal training records.
        </p>

        <div className="flex flex-col sm:flex-row gap-3 pt-2">
          <button
            onClick={handleExportData}
            className="flex-1 py-3 px-4 rounded-xl bg-white/10 hover:bg-white/15 border border-white/15 text-xs font-bold text-white flex items-center justify-center gap-2 transition-all"
          >
            {exportSuccess ? <Check className="w-4 h-4 text-[#4fdbc8]" /> : <Download className="w-4 h-4" />}
            {exportSuccess ? 'Exported JSON!' : `Export Memories (${entries.length})`}
          </button>

          <button
            onClick={() => {
              if (confirm('Reset to initial sample entries?')) {
                onResetData();
              }
            }}
            className="py-3 px-4 rounded-xl bg-red-500/10 hover:bg-red-500/20 border border-red-500/30 text-xs font-bold text-red-300 flex items-center justify-center gap-2 transition-all"
          >
            <RefreshCw className="w-4 h-4" /> Reset Sample Data
          </button>
        </div>
      </section>
    </div>
  );
};
