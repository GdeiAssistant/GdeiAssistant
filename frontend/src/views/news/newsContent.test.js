import { describe, expect, it } from 'vitest'
import { getNewsSourceLabel, getNewsAttachmentTitle } from './newsContent'

describe('news localized content', () => {
  const messages = {
    'news.tab.school': 'Campus News',
    'news.tab.department': 'Department Notices',
    'news.tab.notice': 'Announcements',
    'news.tab.academic': 'Academic Updates',
    'news.defaultTitle': 'News',
    'news.openAttachment': 'Open Attachment'
  }

  const t = (key) => messages[key] ?? key

  it('resolves source labels from translated keys', () => {
    expect(getNewsSourceLabel(1, t)).toBe('Campus News')
    expect(getNewsSourceLabel(4, t)).toBe('Academic Updates')
    expect(getNewsSourceLabel(99, t)).toBe('News')
  })

  it('falls back to translated attachment title', () => {
    expect(getNewsAttachmentTitle('：', t)).toBe('Open Attachment')
  })
})
