import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 8004,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/users': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:9004',
        changeOrigin: true
      }
    }
  }
})
