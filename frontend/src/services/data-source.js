const STORAGE_KEY = 'gdei_data_source_mode'

export const DATA_SOURCE_MODES = {
  remote: 'remote',
  mock: 'mock'
}

export function getDataSourceMode() {
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
  const nextMode = mode === DATA_SOURCE_MODES.mock ? DATA_SOURCE_MODES.mock : DATA_SOURCE_MODES.remote
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

export function getDataSourceLabel(isMock = isMockMode(), t = (key) => key) {
  return isMock ? t('dataSource.mock') : t('dataSource.remote')
}
