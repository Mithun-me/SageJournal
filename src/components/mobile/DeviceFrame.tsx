import React from 'react';
import { Smartphone, Monitor, Sparkles } from 'lucide-react';
import { triggerHaptic } from '../../utils/haptics';

export type DeviceMode = 'responsive' | 'iphone' | 'pixel';

interface DeviceFrameProps {
  deviceMode: DeviceMode;
  onSelectMode: (mode: DeviceMode) => void;
  children: React.ReactNode;
}

export const DeviceFrame: React.FC<DeviceFrameProps> = ({
  deviceMode,
  onSelectMode,
  children,
}) => {
  const handleSelect = (mode: DeviceMode) => {
    triggerHaptic('light');
    onSelectMode(mode);
  };

  if (deviceMode === 'responsive') {
    return (
      <div className="w-full min-h-screen relative flex flex-col">
        {/* Device Switcher Ribbon on desktop top-right (hidden on small touch screens) */}
        <div className="fixed top-2 right-2 z-50 hidden lg:flex items-center gap-1 bg-[#0a0c1a]/85 backdrop-blur-xl border border-white/10 rounded-full p-1 shadow-lg text-xs">
          <span className="text-[10px] text-[#908fa0] px-2 font-medium">Hybrid Preview:</span>
          <button
            onClick={() => handleSelect('responsive')}
            className={`px-2.5 py-1 rounded-full flex items-center gap-1 transition-all ${
              deviceMode === 'responsive'
                ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow-sm'
                : 'text-[#c7c4d7] hover:text-white'
            }`}
          >
            <Monitor className="w-3 h-3" /> Full
          </button>
          <button
            onClick={() => handleSelect('iphone')}
            className={`px-2.5 py-1 rounded-full flex items-center gap-1 transition-all ${
              deviceMode === 'iphone'
                ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow-sm'
                : 'text-[#c7c4d7] hover:text-white'
            }`}
          >
            <Smartphone className="w-3 h-3" /> iOS
          </button>
          <button
            onClick={() => handleSelect('pixel')}
            className={`px-2.5 py-1 rounded-full flex items-center gap-1 transition-all ${
              deviceMode === 'pixel'
                ? 'bg-[#4fdbc8] text-[#003731] font-bold shadow-sm'
                : 'text-[#c7c4d7] hover:text-white'
            }`}
          >
            <Smartphone className="w-3 h-3" /> Android
          </button>
        </div>

        {children}
      </div>
    );
  }

  const isIphone = deviceMode === 'iphone';

  return (
    <div className="w-full min-h-screen bg-[#06070e] flex flex-col items-center justify-center p-2 sm:p-6 select-none">
      {/* Device Switcher Ribbon */}
      <div className="mb-3 flex items-center gap-1 bg-[#0a0c1a]/90 backdrop-blur-xl border border-white/15 rounded-full p-1.5 shadow-xl text-xs z-50">
        <span className="text-[11px] text-[#908fa0] px-2 font-semibold">Device Canvas:</span>
        <button
          onClick={() => handleSelect('responsive')}
          className="px-3 py-1 rounded-full text-[#c7c4d7] hover:text-white flex items-center gap-1 transition-all"
        >
          <Monitor className="w-3.5 h-3.5" /> Full Width
        </button>
        <button
          onClick={() => handleSelect('iphone')}
          className={`px-3 py-1 rounded-full flex items-center gap-1 transition-all font-semibold ${
            isIphone
              ? 'bg-[#4fdbc8] text-[#003731] shadow-md'
              : 'text-[#c7c4d7] hover:text-white'
          }`}
        >
          <Smartphone className="w-3.5 h-3.5" /> iPhone 16 Pro
        </button>
        <button
          onClick={() => handleSelect('pixel')}
          className={`px-3 py-1 rounded-full flex items-center gap-1 transition-all font-semibold ${
            !isIphone
              ? 'bg-[#4fdbc8] text-[#003731] shadow-md'
              : 'text-[#c7c4d7] hover:text-white'
          }`}
        >
          <Smartphone className="w-3.5 h-3.5" /> Pixel 9 Pro
        </button>
      </div>

      {/* Realistic Mobile Device Mockup Container */}
      <div
        className={`relative w-full max-w-[410px] h-[840px] max-h-[92vh] bg-[#101221] shadow-[0_25px_80px_rgba(0,0,0,0.85)] overflow-hidden flex flex-col border transition-all duration-300 ${
          isIphone
            ? 'rounded-[50px] border-[#383a48] ring-4 ring-[#1f202b]'
            : 'rounded-[40px] border-[#2d2f3c] ring-4 ring-[#191a24]'
        }`}
      >
        {/* Hardware details: Side buttons simulation in frame */}
        <div className="absolute -left-[5px] top-28 w-[4px] h-12 bg-[#2d2f3c] rounded-l" />
        <div className="absolute -left-[5px] top-44 w-[4px] h-12 bg-[#2d2f3c] rounded-l" />
        <div className="absolute -right-[5px] top-32 w-[4px] h-16 bg-[#2d2f3c] rounded-r" />

        {/* Screen inner container */}
        <div className="w-full h-full relative overflow-y-auto overflow-x-hidden flex flex-col no-scrollbar">
          {children}

          {/* iOS Bottom Home Bar Indicator / Android Gesture Bar */}
          <div className="fixed bottom-1 left-1/2 -translate-x-1/2 w-32 h-1 bg-white/40 rounded-full z-50 pointer-events-none" />
        </div>
      </div>
    </div>
  );
};
