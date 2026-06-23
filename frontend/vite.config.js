import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Frontend builds into ../src/main/resources/static so Spring Boot serves the SPA.
export default defineConfig({
  plugins: [react()],
  base: '/',
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
