import i18n from '../i18n'
import { LOCATION_CATALOG_DATA } from './locationCatalogData.generated'
import { normalizeCatalogLocale } from './profileCatalog'

function localizeName(node, locale) {
  const localizedNames = node?.localizedNames || {}
  if (localizedNames[locale]) {
    return localizedNames[locale]
  }
  if (locale === 'en' || locale === 'ja' || locale === 'ko') {
    if (node?.latinName) {
      return node.latinName
    }
  }
  return node?.name || ''
}

function formatLocationLabel(parts, locale) {
  const compactParts = parts.filter((item, index, list) => item && item !== list[index - 1])
  if (locale === 'en' || locale === 'ja' || locale === 'ko') {
    return [...compactParts].reverse().join(', ')
  }
  return compactParts.join(' ')
}

let systemAreaAliases
function getSystemAreaAliases() {
  if (systemAreaAliases) return systemAreaAliases
  systemAreaAliases = new Map()
  const locales = ['zh-CN', 'zh-HK', 'zh-TW', 'en', 'ja', 'ko']
  function visit(nodes, ancestors = []) {
    for (const node of nodes) {
      const path = [...ancestors, node]
      for (let start = 0; start < path.length; start++) {
        const suffix = path.slice(start)
        const aliases = new Set([suffix.map(item => item.name).join(' '), suffix.map(item => item.name).join('')])
        const latinNames = suffix.map(item => item.latinName || item.name)
        aliases.add(latinNames.join(' '))
        aliases.add(formatLocationLabel(latinNames, 'en'))
        for (const language of locales) {
          const names = suffix.map(item => localizeName(item, language))
          aliases.add(formatLocationLabel(names, language))
          aliases.add(names.join(' '))
          if (language.startsWith('zh')) aliases.add(names.join(''))
        }
        for (const alias of aliases) {
          if (!alias) continue
          const matches = systemAreaAliases.get(alias) || []
          matches.push(suffix)
          systemAreaAliases.set(alias, matches)
        }
      }
      visit(node.children || [], path)
    }
  }
  visit(LOCATION_CATALOG_DATA)
  return systemAreaAliases
}

function createMaps(locale) {
  const regionMap = new Map()

  const regions = LOCATION_CATALOG_DATA.map((region) => {
    const regionNode = {
      code: region.code,
      name: localizeName(region, locale),
      rawName: region.name,
      children: (region.children || []).map((state) => {
        const stateNode = {
          code: state.code,
          name: localizeName(state, locale),
          rawName: state.name,
          children: (state.children || []).map((city) => ({
            code: city.code,
            name: localizeName(city, locale),
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

export function getLocationCatalog(requestedLocale) {
  const locale = normalizeCatalogLocale(requestedLocale || i18n.global?.locale?.value)
  const { regions, regionMap } = createMaps(locale)

  function findLocation(regionCode, stateCode, cityCode) {
    const region = regionMap.get(regionCode)
    if (!region || (!stateCode && cityCode)) return null
    const state = (region.children || []).find((item) => item.code === stateCode) || null
    if (stateCode && !state) return null
    const city = (state?.children || []).find((item) => item.code === cityCode) || null
    if (cityCode && !city) return null
    return { region, state, city }
  }

  return {
    regions,
    findLocation,
    systemAreaLabel(value) {
      const raw = typeof value === 'string' ? value : ''
      const key = raw.trim()
      if (!key) return raw
      const paths = getSystemAreaAliases().get(key) || []
      const matches = new Set(paths.map(path => formatLocationLabel(path.map(node => localizeName(node, locale)), locale)))
      return matches.size === 1 ? [...matches][0] : raw
    },
    locationLabel(regionCode, stateCode, cityCode) {
      const value = findLocation(regionCode, stateCode, cityCode)
      if (!value) return ''
      return formatLocationLabel([value.region?.name, value.state?.name, value.city?.name], locale)
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
