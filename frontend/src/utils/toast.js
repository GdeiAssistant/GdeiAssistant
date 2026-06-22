import i18n from '../i18n'

const t = (key) => i18n.global.t(key)

/**
 * 错误/警告提示：顶部红色条，用于表单校验、接口错误等
 * 纯 DOM 实现，不依赖任何 UI 框架
 * @param {string} message - 友好中文文案
 */
export function showErrorTopTips(message) {
  const safeMessage = String(message || t('common.saveFailed')).replace(/</g, '&lt;')
  const isRawError =
    /status code\s*\d+|network error|timeout|econnaborted|failed to fetch|load failed/i.test(safeMessage)
  const displayText = isRawError ? t('common.networkException') : safeMessage

  if (typeof document === 'undefined' || !document.body) return
  document.querySelector('[data-error-top-tip="true"]')?.remove()

  const isDark = document.documentElement.dataset.theme === 'dark'
  const isMobile = typeof window !== 'undefined' && window.innerWidth <= 767
  const topOffset = isMobile ? 70 : 16
  const maxWidth = isMobile ? 'min(calc(100vw - 32px), 360px)' : 'min(calc(100vw - 24px), 520px)'
  const iconBg = isDark ? 'rgba(255, 232, 163, 0.14)' : 'rgba(255, 237, 213, 0.92)'
  const iconColor = isDark ? '#fcd34d' : '#b45309'
  const tip = document.createElement('div')
  tip.setAttribute('role', 'alert')
  tip.dataset.errorTopTip = 'true'
  tip.style.cssText = [
    'position:fixed',
    `top:${topOffset}px`,
    'left:50%',
    'z-index:99999',
    'display:flex',
    'align-items:center',
    'gap:10px',
    'min-height:52px',
    `max-width:${maxWidth}`,
    'padding:12px 16px',
    'border-radius:20px',
    `border:1px solid ${isDark ? 'rgba(248, 113, 113, 0.24)' : 'rgba(251, 146, 60, 0.22)'}`,
    `box-shadow:${isDark ? '0 18px 40px rgba(15,23,42,0.28)' : '0 18px 34px rgba(148, 64, 18, 0.12)'}`,
    'backdrop-filter:blur(18px)',
    'font-size:14px',
    'font-weight:700',
    'line-height:1.45',
    'text-align:left',
    `color:${isDark ? '#fff1f2' : '#7c2d12'}`,
    `background:${isDark
      ? 'linear-gradient(135deg, rgba(84, 28, 28, 0.92), rgba(63, 24, 34, 0.94))'
      : 'linear-gradient(135deg, rgba(255, 247, 237, 0.96), rgba(255, 241, 242, 0.94))'}`,
    `transform:${'translate(-50%, -10px)'}`,
    'opacity:0',
    'transition:opacity 0.22s ease, transform 0.22s ease'
  ].join(';')

  const icon = document.createElement('span')
  icon.setAttribute('aria-hidden', 'true')
  icon.textContent = '!'
  icon.style.cssText = [
    'display:inline-flex',
    'width:24px',
    'height:24px',
    'align-items:center',
    'justify-content:center',
    'flex:0 0 auto',
    'border-radius:999px',
    `background:${iconBg}`,
    `color:${iconColor}`,
    'font-size:14px',
    'font-weight:900'
  ].join(';')

  const text = document.createElement('span')
  text.style.cssText = 'display:block'
  text.textContent = displayText

  tip.appendChild(icon)
  tip.appendChild(text)
  document.body.appendChild(tip)

  requestAnimationFrame(() => {
    tip.style.opacity = '1'
    tip.style.transform = 'translate(-50%, 0)'
  })

  setTimeout(() => {
    tip.style.opacity = '0'
    tip.style.transform = 'translate(-50%, -10px)'
    setTimeout(() => {
      if (tip.parentNode) tip.parentNode.removeChild(tip)
    }, 220)
  }, 2200)
}
