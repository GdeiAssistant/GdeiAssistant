export function createCardActions(t) {
  return [
    {
      id: 'info',
      title: t('card.action.info.title'),
      description: t('card.action.info.description'),
      path: '/card/info',
      badge: t('card.action.info.badge')
    },
    {
      id: 'records',
      title: t('card.action.records.title'),
      description: t('card.action.records.description'),
      path: '/card/records',
      badge: t('card.action.records.badge')
    }
  ]
}

export function formatCardAmount(amount, t) {
  const parsed = parseFloat(amount)
  if (Number.isNaN(parsed)) {
    return String(amount)
  }

  const prefix = parsed >= 0 ? '+' : ''
  return `${prefix}${parsed}${t('card.amountUnit')}`
}
