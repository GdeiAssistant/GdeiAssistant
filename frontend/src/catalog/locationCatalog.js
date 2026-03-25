import i18n from '../i18n'
import { localizeMockValue } from '../mock/mock-i18n'
import { LOCATION_CATALOG_DATA } from './locationCatalogData.generated'
import { normalizeCatalogLocale } from './profileCatalog'

function localizeName(name, locale) {
  const normalizedLocale = normalizeCatalogLocale(locale || i18n.global?.locale?.value)
  if (normalizedLocale === 'en') {
    return localizeMockValue(name, 'en')
  }
  return name
}

function createMaps(locale) {
  const regionMap = new Map()

  const regions = LOCATION_CATALOG_DATA.map((region) => {
    const regionNode = {
      code: region.code,
      name: localizeName(region.name, locale),
      rawName: region.name,
      children: (region.children || []).map((state) => {
        const stateNode = {
          code: state.code,
          name: localizeName(state.name, locale),
          rawName: state.name,
          children: (state.children || []).map((city) => ({
            code: city.code,
            name: localizeName(city.name, locale),
            rawName: city.name,
          })),
        }
        return stateNode
      }),
    }
    regionMap.set(regionNode.code, regionNode)
    return regionNode
  })

  return { regions, regionMap }
}

export function getLocationCatalog(locale) {
  const { regions, regionMap } = createMaps(locale)

  function findLocation(regionCode, stateCode, cityCode) {
    const region = regionMap.get(regionCode)
    if (!region) return null
    const state = (region.children || []).find((item) => item.code === stateCode) || null
    const city = (state?.children || []).find((item) => item.code === cityCode) || null
    return { region, state, city }
  }

  return {
    regions,
    findLocation,
    locationLabel(regionCode, stateCode, cityCode) {
      const value = findLocation(regionCode, stateCode, cityCode)
      if (!value) return ''
      const rawLabel = [value.region?.rawName, value.state?.rawName, value.city?.rawName]
        .filter((item, index, list) => item && item !== list[index - 1])
        .join(' ')
      const localizedRawLabel = localizeName(rawLabel, locale)
      if (localizedRawLabel && localizedRawLabel !== rawLabel) {
        return localizedRawLabel
      }
      return [value.region?.name, value.state?.name, value.city?.name]
        .filter((item, index, list) => item && item !== list[index - 1])
        .join(' ')
    },
    toPickerTree(codeTree) {
      const sourceRegions = Array.isArray(codeTree) && codeTree.length ? codeTree : regions
      return sourceRegions.map((regionNode) => {
        const localizedRegion = regionMap.get(regionNode.code)
        const stateMap = {}
        const sourceStates = Array.isArray(regionNode.children) ? regionNode.children : []
        sourceStates.forEach((stateNode) => {
          const localizedState = (localizedRegion?.children || []).find((item) => item.code === stateNode.code)
          const cityMap = {}
          const sourceCities = Array.isArray(stateNode.children) ? stateNode.children : []
          sourceCities.forEach((cityNode) => {
            const localizedCity = (localizedState?.children || []).find((item) => item.code === cityNode.code)
            cityMap[cityNode.code] = {
              code: cityNode.code,
              name: localizedCity?.name || cityNode.code,
            }
          })
          stateMap[stateNode.code] = {
            code: stateNode.code,
            name: localizedState?.name || stateNode.code,
            cityMap,
          }
        })
        return {
          code: regionNode.code,
          name: localizedRegion?.name || regionNode.code,
          stateMap,
        }
      })
    },
  }
}
