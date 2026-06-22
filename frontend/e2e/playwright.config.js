import { defineConfig } from '@playwright/test'

const chromeExecutablePath = process.env.PLAYWRIGHT_CHROME_EXECUTABLE_PATH

// Playwright worker processes force colored output; inheriting NO_COLOR from the
// parent shell makes Node print a warning for every worker.
delete process.env.NO_COLOR

export default defineConfig({
  testDir: '.',
  outputDir: '../test-results',
  expect: {
    timeout: 10000
  },
  reporter: process.env.CI
    ? [['list'], ['html', { outputFolder: '../playwright-report', open: 'never' }]]
    : 'list',
  use: {
    baseURL: 'http://localhost:5173',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    ...(chromeExecutablePath
      ? { launchOptions: { executablePath: chromeExecutablePath } }
      : {})
  },
  workers: process.env.CI ? 2 : undefined,
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: true
  }
})
