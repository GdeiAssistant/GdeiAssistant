import { getProfileCatalog } from './profileCatalog'

export function getCommunityCatalog(locale) {
  const catalog = getProfileCatalog(locale)
  return {
    marketplaceLabel(code) {
      return catalog.dictionaryLabel('marketplaceItemTypes', code)
    },
    lostFoundItemLabel(code) {
      return catalog.dictionaryLabel('lostFoundItemTypes', code)
    },
    lostFoundModeLabel(code) {
      return catalog.dictionaryLabel('lostFoundModes', code)
    },
    otherLabel: catalog.otherLabel,
  }
}
