import { defineConfig } from '@playwright/test'

const chromeExecutablePath = process.env.PLAYWRIGHT_CHROME_EXECUTABLE_PATH

export default defineConfig({
  testDir: '.',
  use: {
    baseURL: 'http://localhost:5173',
    ...(chromeExecutablePath
      ? { launchOptions: { executablePath: chromeExecutablePath } }
      : {})
  },
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true
  }
})
