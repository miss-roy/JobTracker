import type { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.jobtracker.app',
  appName: 'Job Tracker',
  // Capacitor copies this folder's contents into the native app as the web layer.
  webDir: 'dist',
  plugins: {
    // Route window.fetch / XHR through the native HTTP stack. The app is served
    // from capacitor://localhost (a secure context), so a normal fetch() to
    // http://localhost:8080 is blocked by WebKit as mixed content. CapacitorHttp
    // sends it natively instead (governed by the ATS rules in Info.plist).
    CapacitorHttp: {
      enabled: true,
    },
  },
}

export default config
