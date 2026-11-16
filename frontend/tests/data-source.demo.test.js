import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  DATA_SOURCE_MODES,
  getDataSourceMode,
  hasValidDemoMockSession,
  isDemoMockBuild,
  setDataSourceMode,
  toggleDataSourceMode,
} from '../src/services/data-source.js'
import { hasToken } from '../src/router/index.js'

describe('demo mock build data-source and session handling', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    localStorage.clear()
  })

  it('defaults to remote mode when VITE_ENABLE_DEMO_MOCK is not enabled', () => {
    expect(isDemoMockBuild()).toBe(false)
    expect(getDataSourceMode()).toBe(DATA_SOURCE_MODES.remote)
    setDataSourceMode(DATA_SOURCE_MODES.mock)
    expect(getDataSourceMode()).toBe(DATA_SOURCE_MODES.mock)
  })

  it('forces mock mode in demo builds even if an old session stored remote mode', () => {
    vi.stubEnv('VITE_ENABLE_DEMO_MOCK', 'true')
    localStorage.setItem('gdei_data_source_mode', DATA_SOURCE_MODES.remote)

    expect(isDemoMockBuild()).toBe(true)
    expect(getDataSourceMode()).toBe(DATA_SOURCE_MODES.mock)
    expect(toggleDataSourceMode()).toBe(DATA_SOURCE_MODES.mock)
  })

  it('clears legacy non-mock tokens in demo builds while keeping active mock sessions', () => {
    vi.stubEnv('VITE_ENABLE_DEMO_MOCK', 'true')

    localStorage.setItem('token', 'legacy-azure-jwt-token')
    expect(hasValidDemoMockSession()).toBe(false)
    expect(hasToken()).toBe(false)
    expect(localStorage.getItem('token')).toBeNull()

    localStorage.setItem('token', 'mock-session-token')
    localStorage.setItem('mockRuntimeState', JSON.stringify({ token: 'mock-session-token' }))
    expect(hasValidDemoMockSession()).toBe(true)
    expect(hasToken()).toBe(true)
  })
})
