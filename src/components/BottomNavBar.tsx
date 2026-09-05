import React from 'react';
import { TabType } from '../types';
import { triggerHaptic } from '../utils/haptics';

interface BottomNavBarProps {
  activeTab: TabType;
  onSelectTab: (tab: TabType) => void;
  onOpenNewEntry: () => void;
}

export const BottomNavBar: React.FC<BottomNavBarProps> = ({
  activeTab,
  onSelectTab,
  onOpenNewEntry,
}) => {
  const tabs = [
    {
      id: 'home' as TabType,
      label: 'Home',
      icon: 'auto_stories',
    },
    {
      id: 'archive' as TabType,
      label: 'Archive',
      icon: 'collections_bookmark',
    },
    {
      id: 'sanctuary' as TabType,
      label: 'Sanctuary',
      icon: 'spa',
    },
    {
      id: 'trends' as TabType,
      label: 'Insights',
      icon: 'auto_graph',
    },
    {
      id: 'settings' as TabType,
      label: 'Settings',
      icon: 'settings',
    },
  ];

  const handleTabClick = (tabId: TabType) => {
    triggerHaptic('light');
    onSelectTab(tabId);
  };

  const handleNewEntryClick = () => {
    triggerHaptic('medium');
    onOpenNewEntry();
  };

  return (
    <nav
      id="bottom-nav-bar"
      className="fixed bottom-0 left-0 right-0 z-40 bg-[#0a0c1a]/90 backdrop-blur-2xl border-t border-white/[0.08] px-3 pt-2 pb-5 transition-all duration-300"
    >
      <div className="max-w-md mx-auto flex items-center justify-around relative">
        {tabs.slice(0, 2).map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              id={`tab-btn-${tab.id}`}
              onClick={() => handleTabClick(tab.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-2xl transition-all duration-200 active:scale-90 ${
                isActive
                  ? 'text-[#4fdbc8]'
                  : 'text-[#908fa0] hover:text-[#e1e1f6]'
              }`}
            >
              <span
                className={`material-symbols-outlined text-[22px] transition-transform ${
                  isActive ? 'filled scale-110' : ''
                }`}
              >
                {tab.icon}
              </span>
              <span
                className={`text-[10px] font-semibold mt-0.5 tracking-tight font-['Manrope'] ${
                  isActive ? 'text-[#4fdbc8]' : 'text-[#908fa0]'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}

        {/* Center Sanctuary Tab */}
        <button
          key="sanctuary"
          id="tab-btn-sanctuary"
          onClick={() => handleTabClick('sanctuary')}
          className={`flex flex-col items-center justify-center py-1 px-3 rounded-2xl transition-all duration-200 active:scale-90 ${
            activeTab === 'sanctuary'
              ? 'text-[#4fdbc8]'
              : 'text-[#908fa0] hover:text-[#e1e1f6]'
          }`}
        >
          <span
            className={`material-symbols-outlined text-[22px] transition-transform ${
              activeTab === 'sanctuary' ? 'filled scale-110' : ''
            }`}
          >
            spa
          </span>
          <span
            className={`text-[10px] font-semibold mt-0.5 tracking-tight font-['Manrope'] ${
              activeTab === 'sanctuary' ? 'text-[#4fdbc8]' : 'text-[#908fa0]'
            }`}
          >
            Sanctuary
          </span>
        </button>

        {tabs.slice(3).map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              id={`tab-btn-${tab.id}`}
              onClick={() => handleTabClick(tab.id)}
              className={`flex flex-col items-center justify-center py-1 px-3 rounded-2xl transition-all duration-200 active:scale-90 ${
                isActive
                  ? 'text-[#4fdbc8]'
                  : 'text-[#908fa0] hover:text-[#e1e1f6]'
              }`}
            >
              <span
                className={`material-symbols-outlined text-[22px] transition-transform ${
                  isActive ? 'filled scale-110' : ''
                }`}
              >
                {tab.icon}
              </span>
              <span
                className={`text-[10px] font-semibold mt-0.5 tracking-tight font-['Manrope'] ${
                  isActive ? 'text-[#4fdbc8]' : 'text-[#908fa0]'
                }`}
              >
                {tab.label}
              </span>
            </button>
          );
        })}

        {/* Floating Quick New Entry Action Button */}
        <button
          id="quick-new-entry-btn"
          onClick={handleNewEntryClick}
          title="Write New Journal Entry"
          className="absolute -top-6 right-4 sm:right-6 w-11 h-11 rounded-full bg-gradient-to-tr from-[#4f46e5] to-[#4fdbc8] text-white shadow-lg shadow-teal-500/20 hover:scale-105 active:scale-95 transition-all duration-200 flex items-center justify-center border border-white/20"
        >
          <span className="material-symbols-outlined text-[24px]">add</span>
          <span className="sr-only">New Entry</span>
        </button>
      </div>
    </nav>
  );
};
