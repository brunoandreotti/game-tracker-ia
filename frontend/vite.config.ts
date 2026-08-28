import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './vitest.setup.ts',
    passWithNoTests: true,
    env: {
      VITE_API_URL: 'http://localhost:8080',
    },
  },
})
