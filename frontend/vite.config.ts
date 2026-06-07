import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// During `npm run dev` the browser calls relative URLs like /api/jobs.
// This proxy forwards them to the API gateway, so there is NO CORS to deal
// with in development. In Docker, nginx does the same forwarding (see nginx.conf).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
