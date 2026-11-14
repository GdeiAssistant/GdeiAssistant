const THEME_KEY = 'theme'
const ACCENT_KEY = 'gdei-theme-accent'
const DEFAULT_ACCENT = 'banyan'
const FONT_SCALE_KEY = 'font_scale_step'
const FONT_SCALES = [0.85, 1.0, 1.15, 1.3]

export const THEME_ACCENTS = ['banyan', 'kapok', 'haizhu', 'bauhinia']

function getMediaDark() {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
    return null
  }
  return window.matchMedia('(prefers-color-scheme: dark)')
}

function resolveEffectiveTheme(mode) {
  if (mode === 'light' || mode === 'dark') return mode
  return getMediaDark()?.matches ? 'dark' : 'light'
}

function normalizeAccent(accent) {
  return THEME_ACCENTS.includes(accent) ? accent : DEFAULT_ACCENT
}

function applyTheme(effective) {
  document.documentElement.setAttribute('data-theme', effective)
}

function applyThemeAccent(accent) {
  document.documentElement.dataset.accent = normalizeAccent(accent)
}

export function applyThemePreference(mode = getThemeMode(), accent = getThemeAccent()) {
  applyTheme(resolveEffectiveTheme(mode))
  applyThemeAccent(accent)
}

function applyFontScale(step) {
  const scale = FONT_SCALES[step] || 1.0
  document.documentElement.style.fontSize = (16 * scale) + 'px'
}

export function getThemeMode() {
  try { return localStorage.getItem(THEME_KEY) || 'system' } catch { return 'system' }
}

export function setThemeMode(mode) {
  try { localStorage.setItem(THEME_KEY, mode) } catch {}
  applyTheme(resolveEffectiveTheme(mode))
}

export function getThemeAccent() {
  try {
    return normalizeAccent(localStorage.getItem(ACCENT_KEY))
  } catch {
    return DEFAULT_ACCENT
  }
}

export function setThemeAccent(accent) {
  const resolved = normalizeAccent(accent)
  try { localStorage.setItem(ACCENT_KEY, resolved) } catch {}
  applyThemeAccent(resolved)
  return resolved
}

export function getFontScaleStep() {
  try {
    const raw = localStorage.getItem(FONT_SCALE_KEY)
    if (raw === null) return 1
    const v = parseInt(raw, 10)
    return (v >= 0 && v <= 3) ? v : 1
  } catch { return 1 }
}

export function setFontScaleStep(step) {
  try { localStorage.setItem(FONT_SCALE_KEY, String(step)) } catch {}
  applyFontScale(step)
}

export function initTheme() {
  applyThemePreference(getThemeMode(), getThemeAccent())
  applyFontScale(getFontScaleStep())

  getMediaDark()?.addEventListener?.('change', () => {
    if (getThemeMode() === 'system') {
      applyTheme(resolveEffectiveTheme('system'))
    }
  })
}
