export function createCommunityPullMessages(t) {
  return {
    refreshing: t('communityCommon.refreshing'),
    releaseToRefresh: t('communityCommon.releaseToRefresh'),
    pullToRefresh: t('communityCommon.pullToRefresh'),
    loading: t('communityCommon.loading'),
    noMore: t('communityCommon.noMore')
  }
}

export function createMarketplaceCategoryNames(t) {
  return [
    t('marketplace.category.vehicle'),
    t('marketplace.category.phone'),
    t('marketplace.category.computer'),
    t('marketplace.category.digitalAccessory'),
    t('marketplace.category.digital'),
    t('marketplace.category.appliance'),
    t('marketplace.category.sports'),
    t('marketplace.category.clothing'),
    t('marketplace.category.books'),
    t('marketplace.category.rental'),
    t('marketplace.category.life'),
    t('marketplace.category.other')
  ]
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

export function createLostAndFoundItemTypeNames(t) {
  return [
    t('lostandfound.itemType.phone'),
    t('lostandfound.itemType.campusCard'),
    t('lostandfound.itemType.idCard'),
    t('lostandfound.itemType.bankCard'),
    t('lostandfound.itemType.book'),
    t('lostandfound.itemType.key'),
    t('lostandfound.itemType.bag'),
    t('lostandfound.itemType.clothing'),
    t('lostandfound.itemType.vehicle'),
    t('lostandfound.itemType.sports'),
    t('lostandfound.itemType.digitalAccessory'),
    t('lostandfound.itemType.other')
  ]
}

export function createDeliverySizeOptions(t) {
  return [
    { label: t('delivery.size.small'), value: 'small' },
    { label: t('delivery.size.medium'), value: 'medium' },
    { label: t('delivery.size.large'), value: 'large' }
  ]
}
