import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.aura.sagejournal',
  appName: 'Aura',
  webDir: 'dist',
  plugins: {
    StatusBar: {
      // Android 15+ (targetSdk 36) enforces edge-to-edge and offers no opt-out,
      // so the WebView draws under the status bar either way. Insets are handled
      // in CSS via env(safe-area-inset-*) — see the .aura-safe-* classes.
      overlaysWebView: true,
      // "DARK" means light glyphs, which is what Aura's #101221 ground needs.
      style: 'DARK',
      backgroundColor: '#101221',
    },
  },
};

export default config;
