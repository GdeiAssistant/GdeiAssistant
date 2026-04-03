import { describe, expect, it } from 'vitest'

import {
  getDatingCenterCopy,
  getDatingGradeText,
  getDatingHomeCopy,
  resolveDatingLocale,
} from './datingContent'

describe('datingContent', () => {
  it('normalizes supported locales', () => {
    expect(resolveDatingLocale('en-US')).toBe('en')
    expect(resolveDatingLocale('zh-Hant-HK')).toBe('zh-HK')
    expect(resolveDatingLocale('ja-JP')).toBe('ja')
    expect(resolveDatingLocale('ko-KR')).toBe('ko')
  })

  it('returns localized dating home and center copy', () => {
    expect(getDatingHomeCopy('en-US').myAction).toBe('My')
    expect(getDatingHomeCopy('zh-CN').femaleTab).toBe('小姐姐')
    expect(getDatingCenterCopy('en-US').hideAction).toBe('Hide')
    expect(getDatingCenterCopy('ja-JP').title).toBe('交流センター')
  })

  it('maps grade codes to localized labels', () => {
    const t = (key) => ({
      'grade.year.freshman': 'Freshman',
      'grade.year.sophomore': 'Sophomore',
      'grade.year.junior': 'Junior',
      'grade.year.senior': 'Senior',
      'common.unknown': 'Unknown',
    })[key]

    expect(getDatingGradeText(t, 2)).toBe('Sophomore')
    expect(getDatingGradeText(t, 99)).toBe('Unknown')
  })
})
