import { describe, expect, it } from 'vitest'
import { ALL_FEATURES, getFeatureDescription, getFeatureName, getLocalizedFeatures } from './features'

describe('feature i18n helpers', () => {
  const messages = {
    'feature.grade.name': 'Grades',
    'feature.grade.description': 'Check grades and GPA',
    'feature.schedule.name': 'Schedule',
    'feature.schedule.description': 'View weekly classes'
  }

  const t = (key) => messages[key] ?? key

  it('stores translation keys instead of hard-coded display text', () => {
    expect(ALL_FEATURES[0].nameKey).toBe('feature.grade.name')
    expect(ALL_FEATURES[0].descriptionKey).toBe('feature.grade.description')
  })

  it('resolves feature name and description through the translator', () => {
    expect(getFeatureName(ALL_FEATURES[0], t)).toBe('Grades')
    expect(getFeatureDescription(ALL_FEATURES[0], t)).toBe('Check grades and GPA')
  })

  it('builds localized feature lists for UI consumers', () => {
    const features = getLocalizedFeatures(ALL_FEATURES.slice(0, 2), t)
    expect(features.map((item) => item.name)).toEqual(['Grades', 'Schedule'])
    expect(features.map((item) => item.description)).toEqual([
      'Check grades and GPA',
      'View weekly classes'
    ])
  })
})
