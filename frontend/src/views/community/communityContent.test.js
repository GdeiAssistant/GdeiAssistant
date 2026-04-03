import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'
import {
  createCommunityGenderOptions,
  createDeliverySizeOptions,
  createCommunityPullMessages,
  createLostAndFoundItemTypeNames,
  createMarketplaceCategoryNames,
  createDeliveryStatusMap,
  createDeliveryTypeMap
} from './communityContent'

describe('community localized content', () => {
  const messages = {
    'communityCommon.refreshing': 'Refreshing...',
    'communityCommon.releaseToRefresh': 'Release to refresh',
    'communityCommon.pullToRefresh': 'Pull to refresh',
    'communityCommon.loading': 'Loading',
    'communityCommon.noMore': 'No more',
    'communityCommon.select': 'Please select',
    'communityCommon.gender.male': 'Male',
    'communityCommon.gender.female': 'Female',
    'communityCommon.gender.secret': 'Other / Private',
    'delivery.status.pending': 'Pending',
    'delivery.status.inTransit': 'In Transit',
    'delivery.status.completed': 'Completed',
    'delivery.type.express': 'Parcel Pickup',
    'delivery.type.food': 'Meal Errand',
    'delivery.type.other': 'Errand',
    'delivery.size.small': 'Small',
    'delivery.size.medium': 'Medium',
    'delivery.size.large': 'Large'
  }

  const t = (key) => messages[key] ?? key

  it('builds shared pull messages', () => {
    expect(createCommunityPullMessages(t)).toEqual({
      refreshing: 'Refreshing...',
      releaseToRefresh: 'Release to refresh',
      pullToRefresh: 'Pull to refresh',
      loading: 'Loading',
      noMore: 'No more'
    })
  })

  it('builds marketplace category names', () => {
    const categories = createMarketplaceCategoryNames('en')
    expect(categories[0]).toBe('Campus Transportation')
    expect(categories.at(-1)).toBe('Other')
  })

  it('keeps zh-CN marketplace category order aligned with backend enums', () => {
    const javaPath = path.resolve(process.cwd(), '../src/main/java/cn/gdeiassistant/common/constant/OptionConstantUtils.java')
    const source = fs.readFileSync(javaPath, 'utf8')
    const match = source.match(/MARKETPLACE_ITEM_TYPE_OPTIONS\s*=\s*\{([\s\S]*?)\};/)
    const backendLabels = (match?.[1] || '')
      .split(',')
      .map((item) => item.replace(/["\s\n\r]/g, ''))
      .filter(Boolean)

    expect(createMarketplaceCategoryNames('zh-CN')).toEqual(backendLabels)
  })

  it('builds delivery maps', () => {
    expect(createDeliveryStatusMap(t)[1]).toBe('In Transit')
    expect(createDeliveryTypeMap(t).express).toBe('Parcel Pickup')
  })

  it('builds community gender options', () => {
    expect(createCommunityGenderOptions(t)).toEqual([
      { value: '', label: 'Please select' },
      { value: 'male', label: 'Male' },
      { value: 'female', label: 'Female' },
      { value: 'secret', label: 'Other / Private' }
    ])
  })

  it('builds lost and found item types', () => {
    const itemTypes = createLostAndFoundItemTypeNames('en')
    expect(itemTypes[0]).toBe('Phone')
    expect(itemTypes[1]).toBe('Campus Card')
    expect(itemTypes.at(-1)).toBe('Other')
  })

  it('builds delivery size options', () => {
    expect(createDeliverySizeOptions(t)).toEqual([
      { label: 'Small', value: 'small' },
      { label: 'Medium', value: 'medium' },
      { label: 'Large', value: 'large' }
    ])
  })
})
