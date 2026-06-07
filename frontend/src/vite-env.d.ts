/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Absolute backend base URL for native (Capacitor) builds; empty on web. */
  readonly VITE_API_BASE?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
