import request from '../../utils/request.js'

const SPECIAL_REGION_MAP = {
  中国: 'CN',
  中国大陆: 'CN',
  中国香港: 'HK',
  中国澳门: 'MO',
  中国台湾: 'TW',
}

const DEFAULT_FETCH_TIMEOUT_MS = 5000

const STATIC_COUNTRY_CODES = [
  { iso: 'AL', code: 355, name: '阿尔巴尼亚' },
  { iso: 'DZ', code: 213, name: '阿尔及利亚' },
  { iso: 'AF', code: 93, name: '阿富汗' },
  { iso: 'AR', code: 54, name: '阿根廷' },
  { iso: 'AE', code: 971, name: '阿拉伯联合酋长国' },
  { iso: 'AW', code: 297, name: '阿鲁巴' },
  { iso: 'OM', code: 968, name: '阿曼' },
  { iso: 'AZ', code: 994, name: '阿塞拜疆' },
  { iso: 'AC', code: 247, name: '阿森松岛' },
  { iso: 'EG', code: 20, name: '埃及' },
  { iso: 'ET', code: 251, name: '埃塞俄比亚' },
  { iso: 'IE', code: 353, name: '爱尔兰' },
  { iso: 'EE', code: 372, name: '爱沙尼亚' },
  { iso: 'AD', code: 376, name: '安道尔' },
  { iso: 'AO', code: 244, name: '安哥拉' },
  { iso: 'AI', code: 1, name: '安圭拉' },
  { iso: 'AG', code: 1, name: '安提瓜和巴布尔' },
  { iso: 'AT', code: 43, name: '奥地利' },
  { iso: 'AU', code: 61, name: '澳大利亚' },
  { iso: 'MO', code: 853, name: '中国澳门' },
  { iso: 'BB', code: 1, name: '巴巴多斯' },
  { iso: 'PG', code: 675, name: '巴布亚新几内亚' },
  { iso: 'BS', code: 1, name: '巴哈马' },
  { iso: 'PK', code: 92, name: '巴基斯坦' },
  { iso: 'PY', code: 595, name: '巴拉圭' },
  { iso: 'PS', code: 970, name: '巴勒斯坦' },
  { iso: 'BH', code: 973, name: '巴林' },
  { iso: 'PA', code: 507, name: '巴拿马' },
  { iso: 'BR', code: 55, name: '巴西' },
  { iso: 'BY', code: 375, name: '白俄罗斯' },
  { iso: 'BM', code: 1, name: '百慕大' },
  { iso: 'BG', code: 359, name: '保加利亚' },
  { iso: 'MP', code: 1, name: '北马里亚纳群岛' },
  { iso: 'MK', code: 389, name: '北马其顿' },
  { iso: 'BJ', code: 229, name: '贝宁' },
  { iso: 'BE', code: 32, name: '比利时' },
  { iso: 'IS', code: 354, name: '冰岛' },
  { iso: 'PR', code: 1, name: '波多黎各' },
  { iso: 'PL', code: 48, name: '波兰' },
  { iso: 'BA', code: 387, name: '波斯尼亚和黑塞哥维那' },
  { iso: 'BO', code: 591, name: '玻利维亚' },
  { iso: 'BZ', code: 501, name: '伯利兹' },
  { iso: 'BW', code: 267, name: '博茨瓦纳' },
  { iso: 'BT', code: 975, name: '不丹' },
  { iso: 'BF', code: 226, name: '布基纳法索' },
  { iso: 'BI', code: 257, name: '布隆迪' },
  { iso: 'KP', code: 850, name: '朝鲜' },
  { iso: 'GQ', code: 240, name: '赤道几内亚' },
  { iso: 'DK', code: 45, name: '丹麦' },
  { iso: 'DE', code: 49, name: '德国' },
  { iso: 'TL', code: 670, name: '东帝汶' },
  { iso: 'TG', code: 228, name: '多哥' },
  { iso: 'DO', code: 1, name: '多米尼加共和国' },
  { iso: 'DM', code: 1, name: '多米尼克' },
  { iso: 'RU', code: 7, name: '俄罗斯' },
  { iso: 'EC', code: 593, name: '厄瓜多尔' },
  { iso: 'ER', code: 291, name: '厄立特里亚' },
  { iso: 'FR', code: 33, name: '法国' },
  { iso: 'FO', code: 298, name: '法罗群岛' },
  { iso: 'PF', code: 689, name: '法属波利尼西亚' },
  { iso: 'GF', code: 594, name: '法属圭亚那' },
  { iso: 'MF', code: 590, name: '法属圣马丁' },
  { iso: 'VA', code: 39, name: '梵蒂冈' },
  { iso: 'PH', code: 63, name: '菲律宾' },
  { iso: 'FJ', code: 679, name: '斐济' },
  { iso: 'FI', code: 358, name: '芬兰' },
  { iso: 'CV', code: 238, name: '佛得角' },
  { iso: 'FK', code: 500, name: '福克兰群岛（马尔维纳斯群岛）' },
  { iso: 'GM', code: 220, name: '冈比亚' },
  { iso: 'CG', code: 242, name: '刚果（布）' },
  { iso: 'CD', code: 243, name: '刚果（金）' },
  { iso: 'CO', code: 57, name: '哥伦比亚' },
  { iso: 'CR', code: 506, name: '哥斯达黎加' },
  { iso: 'GD', code: 1, name: '格林纳达' },
  { iso: 'GL', code: 299, name: '格陵兰' },
  { iso: 'GE', code: 995, name: '格鲁吉亚' },
  { iso: 'CU', code: 53, name: '古巴' },
  { iso: 'GP', code: 590, name: '瓜德罗普' },
  { iso: 'GU', code: 1, name: '关岛' },
  { iso: 'GF', code: 592, name: '圭亚那' },
  { iso: 'KZ', code: 7, name: '哈萨克斯坦' },
  { iso: 'HT', code: 509, name: '海地' },
  { iso: 'KR', code: 82, name: '韩国' },
  { iso: 'NL', code: 31, name: '荷兰' },
  { iso: 'BQ', code: 599, name: '荷属加勒比区' },
  { iso: 'SX', code: 1, name: '荷属圣马丁' },
  { iso: 'ME', code: 382, name: '黑山' },
  { iso: 'HN', code: 504, name: '洪都拉斯' },
  { iso: 'KI', code: 686, name: '基里巴斯' },
  { iso: 'DJ', code: 253, name: '吉布提' },
  { iso: 'KG', code: 996, name: '吉尔吉斯斯坦' },
  { iso: 'GN', code: 224, name: '几内亚' },
  { iso: 'GW', code: 245, name: '几内亚比绍' },
  { iso: 'CA', code: 1, name: '加拿大' },
  { iso: 'GH', code: 233, name: '加纳' },
  { iso: 'GA', code: 241, name: '加蓬' },
  { iso: 'KH', code: 855, name: '柬埔寨' },
  { iso: 'CZ', code: 420, name: '捷克' },
  { iso: 'ZW', code: 263, name: '津巴布韦' },
  { iso: 'CM', code: 237, name: '喀麦隆' },
  { iso: 'QA', code: 974, name: '卡塔尔' },
  { iso: 'KY', code: 1, name: '开曼群岛' },
  { iso: 'KM', code: 269, name: '科摩罗' },
  { iso: 'XK', code: 383, name: '科索沃' },
  { iso: 'CI', code: 225, name: '科特迪瓦' },
  { iso: 'KW', code: 965, name: '科威特' },
  { iso: 'HR', code: 385, name: '克罗地亚' },
  { iso: 'KE', code: 254, name: '肯尼亚' },
  { iso: 'CK', code: 682, name: '库克群岛' },
  { iso: 'CW', code: 599, name: '库拉索' },
  { iso: 'LV', code: 371, name: '拉脱维亚' },
  { iso: 'LS', code: 266, name: '莱索托' },
  { iso: 'LA', code: 856, name: '老挝' },
  { iso: 'LB', code: 961, name: '黎巴嫩' },
  { iso: 'LT', code: 370, name: '立陶宛' },
  { iso: 'LR', code: 231, name: '利比里亚' },
  { iso: 'LY', code: 218, name: '利巴亚' },
  { iso: 'LI', code: 423, name: '列支敦士登' },
  { iso: 'RE', code: 262, name: '留尼汪' },
  { iso: 'LU', code: 352, name: '卢森堡' },
  { iso: 'RW', code: 250, name: '卢旺达' },
  { iso: 'RO', code: 40, name: '罗马尼亚' },
  { iso: 'MG', code: 261, name: '马达加斯加' },
  { iso: 'MV', code: 960, name: '马尔代夫' },
  { iso: 'MT', code: 356, name: '马耳他' },
  { iso: 'MW', code: 265, name: '马拉维' },
  { iso: 'MY', code: 60, name: '马来西亚' },
  { iso: 'ML', code: 223, name: '马里' },
  { iso: 'MH', code: 692, name: '马绍尔群岛' },
  { iso: 'MQ', code: 596, name: '马提尼克' },
  { iso: 'MU', code: 230, name: '毛里求斯' },
  { iso: 'MR', code: 222, name: '毛里塔尼亚' },
  { iso: 'US', code: 1, name: '美国' },
  { iso: 'AS', code: 1, name: '美属萨摩亚' },
  { iso: 'VI', code: 1, name: '美属维尔京群岛' },
  { iso: 'MN', code: 976, name: '蒙古' },
  { iso: 'MS', code: 1, name: '蒙特塞拉特' },
  { iso: 'BD', code: 880, name: '孟加拉国' },
  { iso: 'PE', code: 51, name: '秘鲁' },
  { iso: 'FM', code: 691, name: '密克罗尼西亚' },
  { iso: 'MM', code: 95, name: '缅甸' },
  { iso: 'MD', code: 373, name: '摩尔多瓦' },
  { iso: 'MA', code: 212, name: '摩洛哥' },
  { iso: 'MC', code: 377, name: '摩纳哥' },
  { iso: 'MZ', code: 258, name: '莫桑比克' },
  { iso: 'MX', code: 52, name: '墨西哥' },
  { iso: 'NA', code: 264, name: '纳米比亚' },
  { iso: 'ZA', code: 27, name: '南非' },
  { iso: 'SS', code: 211, name: '南苏丹' },
  { iso: 'NR', code: 674, name: '瑙鲁' },
  { iso: 'NI', code: 505, name: '尼加拉瓜' },
  { iso: 'NP', code: 977, name: '尼泊尔' },
  { iso: 'NE', code: 227, name: '尼日尔' },
  { iso: 'NG', code: 234, name: '尼日利亚' },
  { iso: 'NU', code: 683, name: '纽埃' },
  { iso: 'NO', code: 47, name: '挪威' },
  { iso: 'NF', code: 672, name: '诺福克岛' },
  { iso: 'PW', code: 680, name: '帕劳' },
  { iso: 'PT', code: 351, name: '葡萄牙' },
  { iso: 'JP', code: 81, name: '日本' },
  { iso: 'SE', code: 46, name: '瑞典' },
  { iso: 'CH', code: 41, name: '瑞士' },
  { iso: 'SV', code: 503, name: '萨尔瓦多' },
  { iso: 'WS', code: 685, name: '萨摩亚' },
  { iso: 'RS', code: 381, name: '塞尔维亚' },
  { iso: 'SL', code: 232, name: '塞拉利昂' },
  { iso: 'SN', code: 221, name: '塞内加尔' },
  { iso: 'CY', code: 357, name: '塞浦路斯' },
  { iso: 'SC', code: 248, name: '塞舌尔' },
  { iso: 'SA', code: 966, name: '沙特阿拉伯' },
  { iso: 'BL', code: 590, name: '圣巴泰勒米' },
  { iso: 'ST', code: 239, name: '圣多美和普林西比' },
  { iso: 'SH', code: 290, name: '圣赫勒拿' },
  { iso: 'KN', code: 1, name: '圣基茨和尼维斯' },
  { iso: 'LC', code: 1, name: '圣卢西亚' },
  { iso: 'SM', code: 378, name: '圣马力诺' },
  { iso: 'PM', code: 508, name: '圣皮埃尔和密克隆群岛' },
  { iso: 'VC', code: 1, name: '圣文森特和格利纳丁斯' },
  { iso: 'LK', code: 94, name: '斯里兰卡' },
  { iso: 'SK', code: 421, name: '斯洛伐克' },
  { iso: 'SI', code: 386, name: '斯洛文尼亚' },
  { iso: 'SZ', code: 268, name: '斯威士兰' },
  { iso: 'SD', code: 249, name: '苏丹' },
  { iso: 'SR', code: 597, name: '苏里南' },
  { iso: 'SB', code: 677, name: '所罗门群岛' },
  { iso: 'SO', code: 252, name: '索马里' },
  { iso: 'TJ', code: 992, name: '塔吉克斯坦' },
  { iso: 'TW', code: 886, name: '中国台湾' },
  { iso: 'TH', code: 66, name: '泰国' },
  { iso: 'TZ', code: 255, name: '坦桑尼亚' },
  { iso: 'TO', code: 676, name: '汤加' },
  { iso: 'TC', code: 1, name: '特克斯和凯科斯群岛' },
  { iso: 'TT', code: 1, name: '特立尼达和多巴哥' },
  { iso: 'TN', code: 216, name: '突尼斯' },
  { iso: 'TV', code: 688, name: '图瓦卢' },
  { iso: 'TR', code: 90, name: '土耳其' },
  { iso: 'TM', code: 993, name: '土库曼斯坦' },
  { iso: 'TK', code: 690, name: '托克劳' },
  { iso: 'WF', code: 681, name: '瓦利斯和富图纳' },
  { iso: 'VU', code: 678, name: '瓦努阿图' },
  { iso: 'GT', code: 502, name: '危地马拉' },
  { iso: 'VE', code: 58, name: '委内瑞拉' },
  { iso: 'BN', code: 673, name: '文莱' },
  { iso: 'UG', code: 256, name: '乌干达' },
  { iso: 'UA', code: 380, name: '乌克兰' },
  { iso: 'UY', code: 598, name: '乌拉圭' },
  { iso: 'UZ', code: 998, name: '乌兹别克斯坦' },
  { iso: 'ES', code: 34, name: '西班牙' },
  { iso: 'GR', code: 30, name: '希腊' },
  { iso: 'HK', code: 852, name: '中国香港' },
  { iso: 'SG', code: 65, name: '新加坡' },
  { iso: 'NC', code: 687, name: '新喀里多尼亚' },
  { iso: 'NZ', code: 64, name: '新西兰' },
  { iso: 'HU', code: 36, name: '匈牙利' },
  { iso: 'SY', code: 963, name: '叙利亚' },
  { iso: 'JM', code: 1, name: '牙买加' },
  { iso: 'AM', code: 374, name: '亚美尼亚' },
  { iso: 'YE', code: 967, name: '也门' },
  { iso: 'IQ', code: 964, name: '伊拉克' },
  { iso: 'IR', code: 98, name: '伊朗' },
  { iso: 'IL', code: 972, name: '以色列' },
  { iso: 'IT', code: 39, name: '意大利' },
  { iso: 'IN', code: 91, name: '印度' },
  { iso: 'ID', code: 62, name: '印度尼西亚' },
  { iso: 'GB', code: 44, name: '英国' },
  { iso: 'VG', code: 1, name: '英属维尔京群岛' },
  { iso: 'JO', code: 962, name: '约旦' },
  { iso: 'VN', code: 84, name: '越南' },
  { iso: 'ZM', code: 260, name: '赞比亚' },
  { iso: 'TD', code: 235, name: '乍得' },
  { iso: 'GI', code: 350, name: '直布罗陀' },
  { iso: 'CL', code: 56, name: '智利' },
  { iso: 'CF', code: 236, name: '中非共和国' },
  { iso: 'CN', code: 86, name: '中国' },
]

const CODE_NAME_TO_ISO = new Map()
const CODE_TO_ISO_CANDIDATES = new Map()
for (const entry of STATIC_COUNTRY_CODES) {
  const numericCode = String(entry.code)
  CODE_NAME_TO_ISO.set(`${numericCode}:${entry.name}`, entry.iso)
  if (!CODE_TO_ISO_CANDIDATES.has(numericCode)) {
    CODE_TO_ISO_CANDIDATES.set(numericCode, entry.iso)
  }
}

export function getFlagEmoji(isoCode) {
  if (!isoCode || isoCode.length !== 2) return ''
  const codePoints = isoCode
    .toUpperCase()
    .split('')
    .map((char) => 127397 + char.charCodeAt())
  return String.fromCodePoint(...codePoints)
}

function inferRegionCode(numericCode, rawName) {
  return (
    CODE_NAME_TO_ISO.get(`${numericCode}:${rawName}`) ||
    SPECIAL_REGION_MAP[rawName] ||
    CODE_TO_ISO_CANDIDATES.get(numericCode) ||
    ''
  )
}

function localizeRegionName(regionCode, rawName, locale) {
  if (regionCode && typeof Intl !== 'undefined' && typeof Intl.DisplayNames === 'function') {
    const displayNames = new Intl.DisplayNames([locale], { type: 'region' })
    const localized = displayNames.of(regionCode)
    if (localized) return localized
  }
  return rawName || ''
}

export function normalizeCountryCodeItems(items) {
  const normalized = []
  const seenCodes = new Set()

  for (const item of items || []) {
    const numericCode = String(item?.code ?? '').replace(/[^\d]/g, '')
    if (!numericCode || seenCodes.has(numericCode)) continue
    const rawName = String(item?.rawName || item?.name || '').trim()
    const explicitIso = String(item?.iso || '').trim().toUpperCase()
    const emoji = String(item?.emoji || item?.flag || '').trim()
    const inferredIso = explicitIso || inferRegionCode(numericCode, rawName)

    seenCodes.add(numericCode)
    normalized.push({
      iso: inferredIso,
      code: `+${numericCode}`,
      emoji: emoji || (inferredIso ? getFlagEmoji(inferredIso) : ''),
      rawName,
    })
  }

  return normalized
}

export function localizeCountryCodes(items, locale) {
  return normalizeCountryCodeItems(items)
    .map((item) => ({
      ...item,
      name: localizeRegionName(item.iso, item.rawName, locale) || item.code,
    }))
    .sort((left, right) => left.name.localeCompare(right.name, locale))
}

export function getFallbackCountryCodeItems() {
  return normalizeCountryCodeItems(STATIC_COUNTRY_CODES)
}

export async function loadCountryCodeCatalog({
  skipRemote = false,
  locale,
  requestClient = request,
  timeoutMs = DEFAULT_FETCH_TIMEOUT_MS,
} = {}) {
  if (skipRemote || typeof requestClient?.get !== 'function') {
    return getFallbackCountryCodeItems()
  }

  try {
    const payload = await requestClient.get('/phone/attribution', {
      headers: locale ? { 'Accept-Language': locale } : undefined,
      timeout: timeoutMs,
      skipErrorTip: true,
    })
    const items = normalizeCountryCodeItems(payload?.data || [])
    if (items.length > 0) {
      return items
    }
  } catch (_) {}

  return getFallbackCountryCodeItems()
}
