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
  it.each([
    ['en', 'New York City', 'London', 'Paris', 'Los Angeles', 'Tochigi'],
    ['ja', 'ニューヨーク', 'ロンドン', 'パリ', 'ロサンゼルス', '栃木県'],
    ['ko', '뉴욕', '런던', '파리', '로스앤젤레스', '도치기 현']
  ])('uses international city names rather than Chinese pinyin in %s', (locale, newYork, london, paris, losAngeles, tochigi) => {
    const catalog = getLocationCatalog(locale)
    expect(catalog.findLocation('USA', 'NY', 'QEE').city.name).toBe(newYork)
    expect(catalog.findLocation('GBR', 'ENG', 'LND').city.name).toBe(london)
    expect(catalog.findLocation('FRA', 'FRA', 'PAR').city.name).toBe(paris)
    expect(catalog.findLocation('USA', 'CA', 'LAX').city.name).toBe(losAngeles)
    expect(catalog.findLocation('JPN', 'JPN', '9').city.name).toBe(tochigi)
    expect(catalog.systemAreaLabel('美国 纽约 纽约市')).toBe(catalog.locationLabel('USA', 'NY', 'QEE'))
    const tree = catalog.toPickerTree([{ code: 'USA', children: [{ code: 'NY', children: [{ code: 'QEE' }] }] }])
    expect(tree[0].stateMap.NY.cityMap.QEE).toMatchObject({ code: 'QEE', name: newYork })
    expect(catalog.systemAreaLabel('纽约市 / 用户自定义')).toBe('纽约市 / 用户自定义')
  })

  it('distinguishes French Guiana from Guyana without guessing ambiguous legacy text', () => {
    const frenchGuiana = LOCATION_CATALOG_DATA.find(node => node.code === 'GUF')
    expect(frenchGuiana).toMatchObject({ name: '圭亚那', iso: 'GF', aliasesName: '法属圭亚那' })
    const names = { 'zh-CN': '法属圭亚那', 'zh-HK': '法屬圭亞那', 'zh-TW': '法屬圭亞那', en: 'French Guiana', ja: '仏領ギアナ', ko: '프랑스령 기아나' }
    for (const [locale, name] of Object.entries(names)) {
      const catalog = getLocationCatalog(locale)
      expect(catalog.findLocation('GUF', '', '').region.name).toBe(name)
      expect(catalog.systemAreaLabel('法属圭亚那')).toBe(name)
    }
    expect(getLocationCatalog('en').systemAreaLabel('圭亚那')).toBe('圭亚那')
    expect(getLocationCatalog('en').locationLabel('USA', 'NY', 'QEE')).toBe('New York City, New York, United States')
  })

  it('covers all 47 Japanese prefectures without changing their stored identity', () => {
    const raw = LOCATION_CATALOG_DATA.find(node => node.code === 'JPN').children[0].children
    expect(raw).toHaveLength(47)
    for (const node of raw) {
      for (const locale of ['en', 'ja', 'ko']) expect(node.localizedNames[locale], `${node.code}:${locale}`).toBeTruthy()
    }
    const tochigi = raw.find(node => node.code === '9')
    expect(tochigi.name).toBe('枥木')
    for (const locale of ['zh-CN', 'zh-HK', 'zh-TW']) {
      expect(getLocationCatalog(locale).findLocation('JPN', 'JPN', '9').city.name).toBe('栃木')
    }
  })

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
