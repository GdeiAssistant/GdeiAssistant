import { getCommunityCatalog } from '../../catalog/communityCatalog'

export function createCommunityPullMessages(t) {
  return {
    refreshing: t('communityCommon.refreshing'),
    releaseToRefresh: t('communityCommon.releaseToRefresh'),
    pullToRefresh: t('communityCommon.pullToRefresh'),
    loading: t('communityCommon.loading'),
    noMore: t('communityCommon.noMore')
  }
}

export function createMarketplaceCategoryNames(locale) {
  const catalog = getCommunityCatalog(locale)
  return Array.from({ length: 12 }, (_, code) => catalog.marketplaceLabel(code))
}

export function createDeliveryStatusMap(t) {
  return {
    0: t('delivery.status.pending'),
    1: t('delivery.status.inTransit'),
    2: t('delivery.status.completed')
  }
}

export function createDeliveryTypeMap(t) {
  return {
    express: t('delivery.type.express'),
    food: t('delivery.type.food'),
    other: t('delivery.type.other')
  }
}

export function createCommunityGenderOptions(t) {
  return [
    { value: '', label: t('communityCommon.select') },
    { value: 'male', label: t('communityCommon.gender.male') },
    { value: 'female', label: t('communityCommon.gender.female') },
    { value: 'secret', label: t('communityCommon.gender.secret') }
  ]
}

export function createLostAndFoundItemTypeNames(locale) {
  const catalog = getCommunityCatalog(locale)
  return Array.from({ length: 12 }, (_, code) => catalog.lostFoundItemLabel(code))
}

export function createDeliverySizeOptions(t) {
  return [
    { label: t('delivery.size.small'), value: 'small' },
    { label: t('delivery.size.medium'), value: 'medium' },
    { label: t('delivery.size.large'), value: 'large' }
  ]
}
