// Tactile mobile haptic feedback helper for hybrid apps (iOS WebKit / Android WebView)
export const triggerHaptic = (type: 'light' | 'medium' | 'success' | 'warning' = 'light') => {
  if (typeof window !== 'undefined' && 'navigator' in window && navigator.vibrate) {
    try {
      if (type === 'light') {
        navigator.vibrate(8);
      } else if (type === 'medium') {
        navigator.vibrate(18);
      } else if (type === 'success') {
        navigator.vibrate([12, 40, 12]);
      } else if (type === 'warning') {
        navigator.vibrate([25, 30, 25]);
      }
    } catch (e) {
      // Ignore vibration errors
    }
  }
};
