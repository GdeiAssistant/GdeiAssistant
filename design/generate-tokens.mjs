import { readFile, writeFile, mkdir } from 'node:fs/promises';

// Paths follow this module, so generation works from the root or frontend/.
const root = new URL('../', import.meta.url);
const source = JSON.parse(await readFile(new URL('tokens.json', import.meta.url), 'utf8'));
const check = process.argv.includes('--check');
if (process.argv.slice(2).some((arg) => arg !== '--check')) throw new Error('Usage: node design/generate-tokens.mjs [--check]');

const colorKeys = ['primary', 'primary-soft', 'bg', 'surface', 'text-1', 'text-2', 'danger', 'warning'];
const fontKeys = ['display', 'title', 'headline', 'body', 'callout', 'caption'];
function expectKeys(value, keys, label) {
  if (!value || Object.keys(value).sort().join(',') !== [...keys].sort().join(',')) {
    throw new Error(`${label}: expected keys ${keys.join(', ')}`);
  }
}
expectKeys(source, ['version', 'colors', 'font', 'radius', 'space'], 'tokens');
if (source.version !== 1) throw new Error('Unsupported token version');
expectKeys(source.colors, colorKeys, 'colors');
expectKeys(source.font, fontKeys, 'font');
expectKeys(source.radius, ['card'], 'radius');
expectKeys(source.space, ['page'], 'space');
for (const [name, pair] of Object.entries(source.colors)) {
  expectKeys(pair, ['light', 'dark'], `colors.${name}`);
  for (const value of Object.values(pair)) {
    if (!/^#[0-9A-F]{6}$/.test(value)) throw new Error(`colors.${name}: expected uppercase #RRGGBB`);
  }
}
for (const group of ['font', 'radius', 'space']) {
  for (const [name, value] of Object.entries(source[group])) {
    if (!Number.isInteger(value) || value <= 0) throw new Error(`${group}.${name}: expected a positive integer`);
  }
}
const header = '// Generated from design/tokens.json. Run node design/generate-tokens.mjs; do not edit.\n';
const cssHeader = '/* Generated from design/tokens.json; do not edit. */\n';
const cssColors = (theme) => colorKeys.map((name) => `  --c-${name}: ${source.colors[name][theme]};`).join('\n');
const cssScale = (factor, unit) => [
  ...fontKeys.map((name) => `  --ds-font-${name}: ${source.font[name] * factor}${unit};`),
  `  --radius-card: ${source.radius.card * factor}${unit};`,
  `  --space-page: ${source.space.page * factor}${unit};`,
].join('\n');
const css = `${cssHeader}:root,\n[data-theme="light"] {\n${cssColors('light')}\n${cssScale(1, 'px')}\n}\n\n[data-theme="dark"] {\n${cssColors('dark')}\n}\n`;
const wxss = `${cssHeader}page,\n.theme-light {\n${cssColors('light')}\n${cssScale(2, 'rpx')}\n}\n\n.theme-dark {\n${cssColors('dark')}\n}\n`;
const nativeName = (name) => name.replace(/-([a-z0-9])/g, (_, value) => value.toUpperCase());
const kotlin = `${header}package cn.gdeiassistant.ui.theme\n\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\n\n// Palette and reference sizes only; keep existing Material color roles and Typography.\nobject GeneratedDesignTokens {\n${['light', 'dark'].map((theme) => `    object ${theme === 'light' ? 'Light' : 'Dark'} {\n${colorKeys.map((name) => `        val ${nativeName(name)} = Color(0xFF${source.colors[name][theme].slice(1)})`).join('\n')}\n    }`).join('\n')}\n${fontKeys.map((name) => `    val ${name}Size = ${source.font[name]}.sp`).join('\n')}\n    val cardRadius = ${source.radius.card}.dp\n    val pageSpacing = ${source.space.page}.dp\n}\n`;
const swiftColor = (hex) => [1, 3, 5].map((start) => (parseInt(hex.slice(start, start + 2), 16) / 255).toFixed(8));
const swift = `${header}import SwiftUI\n\n// Reference sizes only; integration must preserve Dynamic Type and semantic text styles.\nenum GeneratedDesignTokens {\n${['light', 'dark'].map((theme) => `    enum ${theme === 'light' ? 'Light' : 'Dark'} {\n${colorKeys.map((name) => { const [r, g, b] = swiftColor(source.colors[name][theme]); return `        static let ${nativeName(name)} = Color(red: ${r}, green: ${g}, blue: ${b})`; }).join('\n')}\n    }`).join('\n')}\n${fontKeys.map((name) => `    static let ${name}Size: CGFloat = ${source.font[name]}`).join('\n')}\n    static let cardRadius: CGFloat = ${source.radius.card}\n    static let pageSpacing: CGFloat = ${source.space.page}\n}\n`;
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
