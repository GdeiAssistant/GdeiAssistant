const SPECIAL_REGION_MAP = {
  中国: 'CN',
  中国大陆: 'CN',
  中国香港: 'HK',
  中国澳门: 'MO',
  中国台湾: 'TW',
}

const DEFAULT_FETCH_TIMEOUT_MS = 5000

export function getFlagEmoji(isoCode) {
  if (!isoCode || isoCode.length !== 2) return ''
  const codePoints = isoCode
    .toUpperCase()
    .split('')
    .map((char) => 127397 + char.charCodeAt())
  return String.fromCodePoint(...codePoints)
}

export function inferRegionCodeFromFlag(flag) {
  if (!flag || Array.from(flag).length !== 2) return ''
  const chars = Array.from(flag).map((char) => {
    const codePoint = char.codePointAt(0)
    if (!codePoint) return ''
    const ascii = codePoint - 127397
    if (ascii < 65 || ascii > 90) return ''
    return String.fromCharCode(ascii)
  })
  return chars.every(Boolean) ? chars.join('') : ''
}

function inferRegionCodeFromName(name) {
  return SPECIAL_REGION_MAP[name] || ''
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
    const inferredIso = explicitIso || inferRegionCodeFromFlag(emoji) || inferRegionCodeFromName(rawName)

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

export function parseCountryCodesXml(xmlText) {
  const parser = new DOMParser()
  const xmlDoc = parser.parseFromString(xmlText, 'text/xml')
  const nodes = Array.from(xmlDoc.getElementsByTagName('Attribution'))

  return normalizeCountryCodeItems(nodes.map((node) => ({
    code: node.getAttribute('Code') || '',
    name: node.getAttribute('Name') || '',
    flag: node.getAttribute('Flag') || '',
  })))
}

export function getFallbackCountryCodeItems() {
  return normalizeCountryCodeItems([
    { iso: 'CN', code: 86, flag: getFlagEmoji('CN'), name: '中国大陆' },
  ])
}

async function fetchWithTimeout(fetchImpl, input, init = {}, timeoutMs = DEFAULT_FETCH_TIMEOUT_MS) {
  if (!Number.isFinite(timeoutMs) || timeoutMs <= 0 || typeof AbortController !== 'function') {
    return fetchImpl(input, init)
  }

  const controller = new AbortController()
  const timerId = setTimeout(() => {
    controller.abort()
  }, timeoutMs)

  try {
    return await fetchImpl(input, {
      ...init,
      signal: controller.signal,
    })
  } finally {
    clearTimeout(timerId)
  }
}

export async function loadCountryCodeCatalog({
  apiUrl,
  locale,
  fetchImpl = globalThis.fetch,
  fallbackUrl = '/country_codes.xml',
  timeoutMs = DEFAULT_FETCH_TIMEOUT_MS,
} = {}) {
  if (typeof fetchImpl !== 'function') {
    return getFallbackCountryCodeItems()
  }

  try {
    const response = await fetchWithTimeout(fetchImpl, apiUrl, {
      headers: {
        'Accept-Language': locale,
      },
    }, timeoutMs)
    if (response.ok) {
      const payload = await response.json()
      const items = normalizeCountryCodeItems(payload?.data || [])
      if (items.length > 0) {
        return items
      }
    }
  } catch (error) {
    console.error('Failed to load area codes from API, falling back to XML.', error)
  }

  try {
    const response = await fetchWithTimeout(fetchImpl, fallbackUrl, {}, timeoutMs)
    const text = await response.text()
    const items = parseCountryCodesXml(text)
    if (items.length > 0) {
      return items
    }
  } catch (error) {
    console.error('Failed to load area codes from XML fallback.', error)
  }

  return getFallbackCountryCodeItems()
}
