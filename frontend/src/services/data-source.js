const STORAGE_KEY = 'gdei_data_source_mode'
const MOCK_RUNTIME_STATE_KEY = 'mockRuntimeState'

export const DATA_SOURCE_MODES = {
  remote: 'remote',
  mock: 'mock'
}

export function isDemoMockBuild() {
  return import.meta.env.VITE_ENABLE_DEMO_MOCK === 'true'
}

export function hasValidDemoMockSession(storage = globalThis?.localStorage) {
  try {
    const token = storage?.getItem?.('token')
    if (!token) return false
    const raw = storage?.getItem?.(MOCK_RUNTIME_STATE_KEY)
    if (!raw) return false
    const state = JSON.parse(raw)
    return Boolean(state && typeof state === 'object' && state.token && state.token === token)
  } catch (error) {
    return false
  }
}

export function getDataSourceMode() {
  if (isDemoMockBuild()) {
    return DATA_SOURCE_MODES.mock
  }

  try {
    const mode = localStorage.getItem(STORAGE_KEY)
    if (mode === DATA_SOURCE_MODES.mock || mode === DATA_SOURCE_MODES.remote) {
      return mode
    }
  } catch (error) {
    // Ignore data source storage read failures.
  }

  return DATA_SOURCE_MODES.remote
}

export function setDataSourceMode(mode) {
  const nextMode = isDemoMockBuild() || mode === DATA_SOURCE_MODES.mock
    ? DATA_SOURCE_MODES.mock
    : DATA_SOURCE_MODES.remote
  try {
    localStorage.setItem(STORAGE_KEY, nextMode)
  } catch (error) {
    // Ignore data source storage write failures.
  }
  return nextMode
}

export function toggleDataSourceMode() {
  return setDataSourceMode(isMockMode() ? DATA_SOURCE_MODES.remote : DATA_SOURCE_MODES.mock)
}

export function isMockMode() {
  return getDataSourceMode() === DATA_SOURCE_MODES.mock
}
