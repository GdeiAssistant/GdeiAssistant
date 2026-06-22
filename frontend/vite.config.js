import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

// https://vite.dev/config/
const backendProxyTarget = process.env.DEV_BACKEND_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    clearMocks: true,
    restoreMocks: true,
    setupFiles: ['./test/setup.js'],
    exclude: ['e2e/**', 'node_modules/**'],
  },
  server: {
    // /api 代理到 Java 后端
    proxy: {
      '/api': {
        target: backendProxyTarget,
        changeOrigin: true
      }
    }
  }
})
