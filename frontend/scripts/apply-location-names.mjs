import { readFile, writeFile } from 'node:fs/promises'
import { fileURLToPath } from 'node:url'
import { LOCATION_CATALOG_DATA } from '../src/catalog/locationCatalogData.generated.js'

// Apply the reviewed offline snapshot. No network requests or runtime translation service.
const source = JSON.parse(await readFile(new URL('./location-names-geonames.json', import.meta.url), 'utf8'))
for (const entry of source.entries) {
  let nodes = LOCATION_CATALOG_DATA
  let node
  for (const code of entry.path.split('/')) {
    node = nodes.find(candidate => candidate.code === code)
    if (!node) throw new Error(`Unknown location path: ${entry.path}`)
    nodes = node.children || []
  }
  if (entry.name && node.name !== entry.name) throw new Error(`Source name changed: ${entry.path}`)
  if (entry.correctedIso) {
    if (![entry.previousIso, entry.correctedIso].includes(node.iso)) throw new Error(`Source ISO changed: ${entry.path}`)
    node.iso = entry.correctedIso
  }
  node.localizedNames ||= {}
  for (const [locale, label] of Object.entries(entry.labels)) {
    if (!label.trim()) throw new Error(`Empty label: ${entry.path}/${locale}`)
    // The snapshot includes the existing reviewed overrides and the Tochigi typo correction.
    node.localizedNames[locale] = label
  }
}
const target = new URL('../src/catalog/locationCatalogData.generated.js', import.meta.url)
const expected = '// Generated from src/main/resources/location.xml; existing OpenCC4j zh-HK/zh-TW labels retained.\n' +
  '// International names: reviewed GeoNames 2026-10-06 snapshot (CC BY 4.0); see docs/i18n/location-names.md.\n' +
  '// Codes, canonical source names, hierarchy, order and legacy aliases are unchanged.\n' +
  `export const LOCATION_CATALOG_DATA = ${JSON.stringify(LOCATION_CATALOG_DATA, null, 2)}\n`
if (process.argv.includes('--check')) {
  if (await readFile(target, 'utf8') !== expected) throw new Error(`Generated catalog is stale: ${fileURLToPath(target)}`)
} else {
  await writeFile(target, expected)
}
