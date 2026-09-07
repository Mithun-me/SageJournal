import { Capacitor } from '@capacitor/core';

/** True inside the Capacitor WebView (Android/iOS), false in a normal browser. */
export const isNative = Capacitor.isNativePlatform();

/** 'android' | 'ios' | 'web' */
export const platform = Capacitor.getPlatform();
