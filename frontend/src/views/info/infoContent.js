export function normalizeInfoModule(module) {
  if (!module) return null

  const normalized = String(module).trim()
  if (normalized === 'ershou' || normalized === 'secondhand') return 'marketplace'
  if (normalized === 'lost_found' || normalized === 'lostfound') return 'lostandfound'
  if (normalized === 'roommate') return 'dating'
  return normalized
}

export function getInfoModuleLabel(module, t) {
  const normalized = normalizeInfoModule(module)

  if (normalized === 'marketplace') return t('info.moduleMarketplace')
  if (normalized === 'lostandfound') return t('info.moduleLostAndFound')
  if (normalized === 'dating') return t('info.moduleDating')
  if (normalized === 'delivery') return t('info.moduleDelivery')
  if (normalized === 'secret') return t('info.moduleSecret')
  if (normalized === 'express') return t('info.moduleExpress')
  if (normalized === 'topic') return t('info.moduleTopic')
  if (normalized === 'photograph') return t('info.modulePhotograph')
  return t('info.moduleDefault')
}

export function getInteractionLoadMoreLabel(loadingMore, t) {
  return loadingMore ? t('info.loadingMore') : t('info.loadMore')
}
