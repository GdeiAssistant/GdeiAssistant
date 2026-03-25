import { describe, expect, it } from 'vitest'
import { getDataSourceLabel } from './data-source'
import { resolveLoadingMessage } from '../composables/useToast'

describe('shared ui i18n helpers', () => {
  const messages = {
    'common.loading': 'Loading...',
    'dataSource.mock': 'Mock Data Source',
    'dataSource.remote': 'Remote Data Source'
  }

  const t = (key) => messages[key] ?? key

  it('uses translated loading text when no custom message is passed', () => {
    expect(resolveLoadingMessage(undefined, t)).toBe('Loading...')
    expect(resolveLoadingMessage('Please wait', t)).toBe('Please wait')
  })

  it('resolves the data source label from translation keys', () => {
    expect(getDataSourceLabel(true, t)).toBe('Mock Data Source')
    expect(getDataSourceLabel(false, t)).toBe('Remote Data Source')
  })
})
