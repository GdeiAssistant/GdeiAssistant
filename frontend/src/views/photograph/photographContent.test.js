import { describe, expect, it } from 'vitest'
import { getPhotographCopy, resolvePhotographLocale } from './photographContent'

describe('photographContent', () => {
  it('normalizes locale variants', () => {
    expect(resolvePhotographLocale('zh-CN')).toBe('zh-CN')
    expect(resolvePhotographLocale('zh-Hant-HK')).toBe('zh-HK')
    expect(resolvePhotographLocale('zh-Hant-TW')).toBe('zh-TW')
    expect(resolvePhotographLocale('en-US')).toBe('en')
    expect(resolvePhotographLocale('ja-JP')).toBe('ja')
    expect(resolvePhotographLocale('ko-KR')).toBe('ko')
  })

  it('returns localized English copy instead of Chinese fallbacks', () => {
    const copy = getPhotographCopy('en-US')
    expect(copy.lifeTab).toBe('Best Daily Photos')
    expect(copy.publishAction).toBe('Post Photos')
    expect(copy.formatLikeMetric(12)).toBe('12 Likes')
    expect(copy.formatContentCount(8)).toBe('8/150')
  })

  it('keeps Chinese labels for simplified Chinese locale', () => {
    const copy = getPhotographCopy('zh-CN')
    expect(copy.detailTitle).toBe('作品详情')
    expect(copy.submitAction).toBe('确认提交')
    expect(copy.formatImageBadge(3)).toBe('3图')
  })
})
