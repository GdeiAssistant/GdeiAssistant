import { readFile, writeFile, mkdir } from 'node:fs/promises';

// Paths follow this module, so generation works from the root or frontend/.
const root = new URL('../', import.meta.url);
const source = JSON.parse(await readFile(new URL('tokens.json', import.meta.url), 'utf8'));
const check = process.argv.includes('--check');
if (process.argv.slice(2).some((arg) => arg !== '--check')) throw new Error('Usage: node design/generate-tokens.mjs [--check]');

const colorKeys = [
  'bg', 'surface', 'surface-sunken', 'surface-elevated', 'border', 'divider',
  'text-1', 'text-2', 'text-3',
  'primary', 'primary-hover', 'primary-soft', 'primary-ink', 'on-primary', 'band', 'on-band',
  'danger', 'danger-ink', 'danger-soft',
  'warning', 'warning-ink', 'warning-soft',
  'success', 'success-ink', 'success-soft',
];
// Per-accent overrides; banyan is the base palette above.
const accentKeys = ['primary', 'primary-hover', 'primary-soft', 'primary-ink', 'on-primary', 'band'];
const accentNames = ['kapok', 'haizhu', 'bauhinia'];
const courseIds = ['1', '2', '3', '4', '5', '6', '7', '8'];
const courseParts = ['bg', 'ink', 'stripe'];
const fontKeys = ['display', 'title', 'headline', 'body', 'callout', 'caption'];
function expectKeys(value, keys, label) {
  if (!value || Object.keys(value).sort().join(',') !== [...keys].sort().join(',')) {
    throw new Error(`${label}: expected keys ${keys.join(', ')}`);
  }
}
expectKeys(source, ['version', 'colors', 'accents', 'course', 'font', 'radius', 'space'], 'tokens');
if (source.version !== 2) throw new Error('Unsupported token version');
expectKeys(source.colors, colorKeys, 'colors');
expectKeys(source.accents, accentNames, 'accents');
expectKeys(source.course, courseIds, 'course');
expectKeys(source.font, fontKeys, 'font');
expectKeys(source.radius, ['card'], 'radius');
expectKeys(source.space, ['page'], 'space');
const checkPair = (pair, label) => {
  expectKeys(pair, ['light', 'dark'], label);
  for (const value of Object.values(pair)) {
    if (!/^#[0-9A-F]{6}$/.test(value)) throw new Error(`${label}: expected uppercase #RRGGBB`);
  }
};
for (const [name, pair] of Object.entries(source.colors)) checkPair(pair, `colors.${name}`);
for (const [accent, roles] of Object.entries(source.accents)) {
  expectKeys(roles, accentKeys, `accents.${accent}`);
  for (const [name, pair] of Object.entries(roles)) checkPair(pair, `accents.${accent}.${name}`);
}
for (const [id, parts] of Object.entries(source.course)) {
  expectKeys(parts, courseParts, `course.${id}`);
  for (const [name, pair] of Object.entries(parts)) checkPair(pair, `course.${id}.${name}`);
}
for (const group of ['font', 'radius', 'space']) {
  for (const [name, value] of Object.entries(source[group])) {
    if (!Number.isInteger(value) || value <= 0) throw new Error(`${group}.${name}: expected a positive integer`);
  }
}
// Flat role tables: base palette plus course-N-bg/ink/stripe, and one table per accent.
const courseRoles = courseIds.flatMap((id) => courseParts.map((part) => [`course-${id}-${part}`, source.course[id][part]]));
const baseRoles = [...colorKeys.map((name) => [name, source.colors[name]]), ...courseRoles];
const accentTables = [
  ['banyan', accentKeys.map((name) => [name, source.colors[name]])],
  ...accentNames.map((accent) => [accent, accentKeys.map((name) => [name, source.accents[accent][name]])]),
];
const header = '// Generated from design/tokens.json. Run node design/generate-tokens.mjs; do not edit.\n';
const cssHeader = '/* Generated from design/tokens.json; do not edit. */\n';
const cssVars = (roles, theme) => roles.map(([name, pair]) => `  --c-${name}: ${pair[theme]};`).join('\n');
const cssScale = (factor, unit) => [
  ...fontKeys.map((name) => `  --ds-font-${name}: ${source.font[name] * factor}${unit};`),
  `  --radius-card: ${source.radius.card * factor}${unit};`,
  `  --space-page: ${source.space.page * factor}${unit};`,
].join('\n');
// Accent blocks: light selectors precede dark ones so dark wins at equal specificity.
const webAccents = (theme) => accentTables.map(([accent, roles]) => `${theme === 'light' ? `:root[data-accent="${accent}"],\n[data-theme="light"][data-accent="${accent}"]` : `[data-theme="dark"][data-accent="${accent}"]`} {\n${cssVars(roles, theme)}\n}\n`).join('\n');
const wxssAccents = (theme) => accentTables.map(([accent, roles]) => `${theme === 'light' ? `.accent-${accent}` : `.theme-dark.accent-${accent}`} {\n${cssVars(roles, theme)}\n}\n`).join('\n');
const css = `${cssHeader}:root,\n[data-theme="light"] {\n${cssVars(baseRoles, 'light')}\n${cssScale(1, 'px')}\n}\n\n[data-theme="dark"] {\n${cssVars(baseRoles, 'dark')}\n}\n\n${webAccents('light')}\n${webAccents('dark')}`;
const wxss = `${cssHeader}page,\n.theme-light {\n${cssVars(baseRoles, 'light')}\n${cssScale(2, 'rpx')}\n}\n\n.theme-dark {\n${cssVars(baseRoles, 'dark')}\n}\n\n${wxssAccents('light')}\n${wxssAccents('dark')}`;
const nativeName = (name) => name.replace(/-([a-z0-9])/g, (_, value) => value.toUpperCase());
const themeLabel = (theme) => (theme === 'light' ? 'Light' : 'Dark');
const accentLabel = (accent) => accent[0].toUpperCase() + accent.slice(1);
const swiftColor = (hex) => [1, 3, 5].map((start) => (parseInt(hex.slice(start, start + 2), 16) / 255).toFixed(8));
const nativeObjects = (kind, indent, valueOf) => (roles) => ['light', 'dark'].map((theme) => `${indent}${kind} ${themeLabel(theme)} {\n${roles.map(([name, pair]) => `${indent}    ${valueOf(nativeName(name), pair[theme])}`).join('\n')}\n${indent}}`).join('\n');
const ktValue = (name, hex) => `val ${name} = Color(0xFF${hex.slice(1)})`;
const swValue = (name, hex) => { const [r, g, b] = swiftColor(hex); return `static let ${name} = Color(red: ${r}, green: ${g}, blue: ${b})`; };
const ktObjects = nativeObjects('object', '    ', ktValue);
const swObjects = nativeObjects('enum', '    ', swValue);
const ktAccents = accentTables.map(([accent, roles]) => `        object ${accentLabel(accent)} {\n${nativeObjects('object', '            ', ktValue)(roles)}\n        }`).join('\n');
const swAccents = accentTables.map(([accent, roles]) => `        enum ${accentLabel(accent)} {\n${nativeObjects('enum', '            ', swValue)(roles)}\n        }`).join('\n');
const kotlin = `${header}package cn.gdeiassistant.ui.theme\n\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\n\n// Palette and reference sizes only; keep existing Material color roles and Typography.\nobject GeneratedDesignTokens {\n${ktObjects(baseRoles)}\n    object Accent {\n${ktAccents}\n    }\n${fontKeys.map((name) => `    val ${name}Size = ${source.font[name]}.sp`).join('\n')}\n    val cardRadius = ${source.radius.card}.dp\n    val pageSpacing = ${source.space.page}.dp\n}\n`;
const swift = `${header}import SwiftUI\n\n// Reference sizes only; integration must preserve Dynamic Type and semantic text styles.\nenum GeneratedDesignTokens {\n${swObjects(baseRoles)}\n    enum Accent {\n${swAccents}\n    }\n${fontKeys.map((name) => `    static let ${name}Size: CGFloat = ${source.font[name]}`).join('\n')}\n    static let cardRadius: CGFloat = ${source.radius.card}\n    static let pageSpacing: CGFloat = ${source.space.page}\n}\n`;
const outputs = new Map([
  ['frontend/src/styles/design-tokens.generated.css', css],
  ['design/generated/GeneratedDesignTokens.kt', kotlin],
  ['design/generated/GeneratedDesignTokens.swift', swift],
  ['design/generated/design-tokens.wxss', wxss],
]);
let drift = false;
for (const [path, contents] of outputs) {
  const file = new URL(path, root);
  if (check) {
    const current = await readFile(file, 'utf8').catch((error) => { if (error.code === 'ENOENT') return null; throw error; });
    if (current !== contents) { console.error(`Generated token drift: ${path}`); drift = true; }
  } else {
    await mkdir(new URL('./', file), { recursive: true });
    await writeFile(file, contents, 'utf8');
  }
}
if (drift) process.exitCode = 1;
else console.log(check ? 'Generated design tokens are current.' : 'Generated design tokens for Web, Compose, SwiftUI and WXSS.');
