const THEME_KEY = 'theme'
const FONT_SCALE_KEY = 'font_scale_step'
const FONT_SCALES = [0.85, 1.0, 1.15, 1.3]

function getMediaDark() {
  return window.matchMedia('(prefers-color-scheme: dark)')
}

function resolveEffectiveTheme(mode) {
  if (mode === 'light' || mode === 'dark') return mode
  return getMediaDark().matches ? 'dark' : 'light'
}

function applyTheme(effective) {
  document.documentElement.setAttribute('data-theme', effective)
}

function applyFontScale(step) {
  const scale = FONT_SCALES[step] || 1.0
  document.documentElement.style.fontSize = (16 * scale) + 'px'
}

export function getThemeMode() {
  try { return localStorage.getItem(THEME_KEY) || 'system' } catch { return 'system' }
}

export function setThemeMode(mode) {
  localStorage.setItem(THEME_KEY, mode)
  applyTheme(resolveEffectiveTheme(mode))
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
  localStorage.setItem(FONT_SCALE_KEY, String(step))
  applyFontScale(step)
}

export function initTheme() {
  const mode = getThemeMode()
  applyTheme(resolveEffectiveTheme(mode))
  applyFontScale(getFontScaleStep())

  getMediaDark().addEventListener('change', () => {
    if (getThemeMode() === 'system') {
      applyTheme(resolveEffectiveTheme('system'))
    }
  })
}
