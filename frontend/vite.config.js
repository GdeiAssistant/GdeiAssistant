import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

// https://vite.dev/config/
const backendProxyTarget = process.env.DEV_BACKEND_PROXY_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue(), tailwindcss(), {
    name: 'release-version',
    generateBundle() {
      this.emitFile({ type: 'asset', fileName: 'version.json', source: JSON.stringify({ revision: process.env.GITHUB_SHA || process.env.GDEI_BUILD_REVISION || 'local' }) })
    }
  }],
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
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{js,vue}'],
      reporter: ['text-summary', 'json-summary', 'html'],
      thresholds: {
        'src/composables/{useLatestRequest,useScrollLoad}.js': { perFile: true, lines: 75, branches: 60 },
        'src/views/{grade/Grade,schedule/Schedule}.vue': { perFile: true, lines: 75, branches: 60 }
      }
    },
  },
  server: {
    // /api 代理到 Java 后端（含 WebSocket）
    proxy: {
      '/api': {
        target: backendProxyTarget,
        changeOrigin: true,
        ws: true
      }
    }
  }
})
