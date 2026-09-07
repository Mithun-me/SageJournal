/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Absolute base URL of the Aura backend. Required for native Android builds. */
  readonly VITE_API_BASE_URL?: string;
}
