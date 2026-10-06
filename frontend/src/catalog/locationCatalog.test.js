import { describe, expect, it } from 'vitest'
import { getLocationCatalog } from './locationCatalog'
import { LOCATION_CATALOG_DATA } from './locationCatalogData.generated'

const examples = {
  'zh-CN': ['中国 广东 广州', '中国 广东 汕头', '广东'],
  'zh-HK': ['中國 廣東 廣州', '中國 廣東 汕頭', '廣東'],
  'zh-TW': ['中國 廣東 廣州', '中國 廣東 汕頭', '廣東'],
  en: ['Guangzhou, Guangdong, China', 'Shantou, Guangdong, China', 'Guangdong'],
  ja: ['広州, 広東, 中国', '汕頭, 広東, 中国', '広東'],
  ko: ['광저우, 광둥, 중국', '산터우, 광둥, 중국', '광둥']
}
const allNodes = nodes => nodes.flatMap(node => [node, ...allNodes(node.children || [])])

describe('system region labels', () => {
  it.each(Object.entries(examples))('renders location, hometown and known IP areas in %s', (locale, expected) => {
    const catalog = getLocationCatalog(locale)
    expect(catalog.locationLabel('CN', '44', '1')).toBe(expected[0])
    expect(catalog.locationLabel('CN', '44', '5')).toBe(expected[1])
    expect(catalog.systemAreaLabel('广东')).toBe(expected[2])
    expect(catalog.systemAreaLabel('廣東')).toBe(expected[2])
    expect(catalog.systemAreaLabel('Guangdong')).toBe(expected[2])
    for (const raw of ['中国 广东 广州', '中国广东广州', 'Guangzhou, Guangdong, China']) {
      expect(catalog.systemAreaLabel(raw)).toBe(expected[0])
    }
    const partial = locale.startsWith('zh') ? expected[0].replace(/^[^ ]+ /, '') : expected[0].replace(/, [^,]+$/, '')
    expect(catalog.systemAreaLabel('广东 广州')).toBe(partial)
    expect(catalog.systemAreaLabel('Guangdong Guangzhou')).toBe(partial)
  })

  it('includes complete HK/TW labels while retaining every canonical code and source name', () => {
    const nodes = allNodes(LOCATION_CATALOG_DATA)
    expect(nodes).toHaveLength(4279)
    for (const node of nodes) {
      expect(node.localizedNames['zh-HK'], `${node.name}:HK`).toBeTruthy()
      expect(node.localizedNames['zh-TW'], `${node.name}:TW`).toBeTruthy()
    }
    const baselineCodes = allNodes(getLocationCatalog('zh-CN').regions).map(node => node.code)
    for (const locale of Object.keys(examples)) {
      expect(allNodes(getLocationCatalog(locale).regions).map(node => node.code)).toEqual(baselineCodes)
    }
    const raw = LOCATION_CATALOG_DATA.find(node => node.code === 'CN').children.find(node => node.code === '44')
    expect(raw.name).toBe('广东')
    expect(raw.children[0]).toMatchObject({ code: '1', name: '广州' })
  })

  it('uses the same names for picker choices and the selected profile value', () => {
    const codeTree = [{ code: 'CN', children: [{ code: '44', children: [{ code: '1' }, { code: '5' }] }] }]
    for (const locale of Object.keys(examples)) {
      const catalog = getLocationCatalog(locale)
      const tree = catalog.toPickerTree(codeTree)
      const state = tree[0].stateMap['44']
      expect(state.code).toBe('44')
      expect(state.cityMap['1'].code).toBe('1')
      expect(state.name).toBe(catalog.findLocation('CN', '44', '1').state.name)
      expect(state.cityMap['1'].name).toBe(catalog.findLocation('CN', '44', '1').city.name)
      expect(Object.keys(state.cityMap)).toEqual(['1', '5'])
    }
  })

  it('keeps unknown system area text intact and never replaces partial strings', () => {
    const catalog = getLocationCatalog('zh-HK')
    for (const raw of ['广东 广州 / 自定义', 'Guangdong Province', '用户写的中国广东', '未识别地区', 'Shanxi']) {
      expect(catalog.systemAreaLabel(raw)).toBe(raw)
    }
    expect(catalog.locationLabel('unknown', '44', '1')).toBe('')
    expect(catalog.locationLabel('CN', 'unknown', '1')).toBe('')
    expect(catalog.locationLabel('CN', '44', 'unknown')).toBe('')
    expect(catalog.locationLabel('CN', '', '1')).toBe('')
    expect(catalog.locationLabel('CN', '44', '6')).toBe('中國 廣東 佛山')
    expect(getLocationCatalog('en').locationLabel('CN', '44', '6')).toBe('Foshan, Guangdong, China')
  })

  it('resolves Hong Kong and Macau language aliases through the shared locale parser', () => {
    for (const locale of [' ZH_hAnT_MO ;q=0.9,en;q=0.8', 'zh-Hant-HK-u-nu-hanidec']) {
      expect(getLocationCatalog(locale).locationLabel('CN', '44', '1')).toBe('中國 廣東 廣州')
    }
  })
})
