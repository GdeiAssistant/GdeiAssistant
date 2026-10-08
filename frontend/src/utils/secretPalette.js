/**
 * 树洞便签色板（单一事实源）。
 *
 * 浅色色值对齐 iOS SecretThemeStyle（theme 1-12），超出范围的 id 回退到
 * 中性表面色（对应 iOS 的 default 分支）。深色模式在同色相上降低明度，
 * 文字按 WCAG AA 4.5:1 选取：浅色便签用深色墨、深色便签用白字。
 */

const INK = '#11201C'
const INK_DARK = '#E6EEEB'
const WHITE = '#FFFFFF'

// light: true 表示浅色底配深色墨；false 表示深色底配白字
export const NOTE_THEMES = {
  1: { bg: '#F5F0DE', bgDark: '#666860', light: true },
  2: { bg: '#D7858F', bgDark: '#5A4042', light: true },
  3: { bg: '#8FADC9', bgDark: '#3F4F58', light: true },
  4: { bg: '#E8AB7D', bgDark: '#614E3B', light: true },
  5: { bg: '#8ABCA0', bgDark: '#3D5449', light: true },
  6: { bg: '#A38AC4', bgDark: '#474156', light: true },
  7: { bg: '#5EA1BA', bgDark: '#2C4A52', light: true },
  8: { bg: '#ED8470', bgDark: '#633F36', light: true },
  9: { bg: '#F2BA63', bgDark: '#655431', light: true },
  10: { bg: '#546B9E', bgDark: '#3C4D6D', light: false },
  11: { bg: '#427A73', bgDark: '#305751', light: false },
  12: { bg: '#856147', bgDark: '#5B4635', light: false }
}

const NEUTRAL_BG = 'var(--c-surface)'

export function resolveNoteTheme(theme) {
  return NOTE_THEMES[Number(theme)] || null
}

export function getThemeBg(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return NEUTRAL_BG
  return isDark ? note.bgDark : note.bg
}

export function getThemeTextColor(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return isDark ? 'var(--c-text-1)' : 'var(--c-text-1)'
  if (note.light) return isDark ? INK_DARK : INK
  return WHITE
}

export function getThemeMutedTextColor(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return isDark ? 'var(--c-text-2)' : 'var(--c-text-2)'
  if (note.light) return isDark ? 'rgba(230, 238, 235, 0.72)' : 'rgba(17, 32, 28, 0.62)'
  return 'rgba(255, 255, 255, 0.75)'
}

export function getThemeAccentColor(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return 'var(--c-primary)'
  return getThemeTextColor(theme, isDark)
}

export function getThemeCardChrome(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return isDark ? 'var(--c-border-light)' : 'var(--c-border-light)'
  return note.light
    ? (isDark ? 'rgba(230, 238, 235, 0.22)' : 'rgba(17, 32, 28, 0.14)')
    : 'rgba(255, 255, 255, 0.18)'
}

export function getFooterBg(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return isDark ? 'var(--c-fill-2)' : 'var(--c-fill-2)'
  return note.light
    ? (isDark ? 'rgba(0, 0, 0, 0.16)' : 'rgba(17, 32, 28, 0.06)')
    : 'rgba(0, 0, 0, 0.12)'
}

export function getFooterTextColor(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return isDark ? 'var(--c-text-2)' : 'var(--c-text-2)'
  if (note.light) return isDark ? 'rgba(230, 238, 235, 0.65)' : 'rgba(17, 32, 28, 0.55)'
  return 'rgba(255, 255, 255, 0.7)'
}

/** 浅色便签上的语音图标用深色资源，深色便签及深色模式用白色资源 */
export function useLightVoiceAsset(theme, isDark = false) {
  const note = resolveNoteTheme(theme)
  if (!note) return !isDark
  return !isDark && note.light
}
