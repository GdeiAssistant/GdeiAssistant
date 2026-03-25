import { describe, expect, it } from 'vitest'
import {
  EXPORT_STATUS,
  getDownloadStatusDescription,
  getDownloadStatusTitle,
} from '../downloadSupport'

describe('downloadSupport', () => {
  it('derives title and description from the active translator instead of caching one locale', () => {
    const en = (key) => ({
      'downloadPage.status.loadingTitle': 'Checking export status',
      'downloadPage.status.exportingTitle': 'Packaging your data',
      'downloadPage.status.exportedTitle': 'Your data is ready',
      'downloadPage.status.idleTitle': 'Get your account data copy',
      'downloadPage.status.loadingDescription': 'Loading the latest export state.',
      'downloadPage.status.exportingDescription': 'Your export request is being prepared.',
      'downloadPage.status.exportedDescription': 'Your export is ready to download.',
      'downloadPage.status.idleDescription': 'You can request a personal data export at any time.',
    }[key])
    const zh = (key) => ({
      'downloadPage.status.loadingTitle': '正在检查导出状态',
      'downloadPage.status.exportingTitle': '正在为您打包数据',
      'downloadPage.status.exportedTitle': '数据副本已可下载',
      'downloadPage.status.idleTitle': '获取您的账号数据副本',
      'downloadPage.status.loadingDescription': '正在加载最新的导出状态。',
      'downloadPage.status.exportingDescription': '导出请求已提交，系统正在打包您的数据。',
      'downloadPage.status.exportedDescription': '您的数据副本已准备就绪，可直接下载。',
      'downloadPage.status.idleDescription': '您可以随时发起个人数据导出请求。',
    }[key])

    expect(getDownloadStatusTitle(en, EXPORT_STATUS.EXPORTING, false)).toBe('Packaging your data')
    expect(getDownloadStatusTitle(zh, EXPORT_STATUS.EXPORTING, false)).toBe('正在为您打包数据')
    expect(getDownloadStatusDescription(en, EXPORT_STATUS.EXPORTED, false)).toBe('Your export is ready to download.')
    expect(getDownloadStatusDescription(zh, EXPORT_STATUS.EXPORTED, false)).toBe('您的数据副本已准备就绪，可直接下载。')
  })
})
