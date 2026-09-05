import React, { useState, useEffect } from 'react';
import { Wifi, BatteryMedium, Sparkles } from 'lucide-react';

interface StatusBarProps {
  deviceType?: 'ios' | 'android' | 'native';
  showIsland?: boolean;
}

export const StatusBar: React.FC<StatusBarProps> = ({ deviceType = 'ios', showIsland = false }) => {
  const [timeStr, setTimeStr] = useState('9:41');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeStr(
        now.toLocaleTimeString([], {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        })
      );
    };
    updateTime();
    const interval = setInterval(updateTime, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div
      id="mobile-status-bar"
      className="w-full select-none z-50 px-6 pt-2 pb-1.5 flex items-center justify-between text-xs font-semibold text-[#e1e1f6]/90 tracking-tight"
    >
      {/* Left: Clock */}
      <span className="font-['Manrope'] font-bold text-[13px] tracking-tight">{timeStr}</span>

      {/* Center: Dynamic Island or Punch hole if in mock mode */}
      {showIsland && (
        <div className="hidden sm:flex items-center justify-center">
          <div className="h-5 w-24 bg-black/90 rounded-full border border-white/10 flex items-center justify-center px-2 gap-1.5 shadow-sm">
            <span className="w-2.5 h-2.5 rounded-full bg-[#4fdbc8]/80 animate-pulse" />
            <span className="text-[10px] text-[#c7c4d7] font-medium truncate">Aura Active</span>
          </div>
        </div>
      )}

      {/* Right: Cellular, Wifi, Battery */}
      <div className="flex items-center gap-1.5 text-[#e1e1f6]/90">
        {/* Cellular Signal bars */}
        <div className="flex items-end gap-[1.5px] h-3">
          <span className="w-[3px] h-1.5 bg-current rounded-[0.5px]" />
          <span className="w-[3px] h-2 bg-current rounded-[0.5px]" />
          <span className="w-[3px] h-2.5 bg-current rounded-[0.5px]" />
          <span className="w-[3px] h-3 bg-current rounded-[0.5px]" />
        </div>

        <Wifi className="w-3.5 h-3.5 ml-0.5" />

        {/* Battery with percentage */}
        <div className="flex items-center gap-1">
          <span className="text-[11px] font-medium">94%</span>
          <div className="w-5 h-2.5 border border-current rounded-[3px] p-[1px] flex items-center relative">
            <div className="h-full w-[85%] bg-current rounded-[1px]" />
            <div className="w-[1.5px] h-1 bg-current absolute -right-[2.5px] top-[2.5px] rounded-r-[0.5px]" />
          </div>
        </div>
      </div>
    </div>
  );
};
