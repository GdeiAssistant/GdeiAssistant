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
  const tip = document.createElement('div')
  tip.setAttribute('role', 'alert')
  tip.style.cssText =
    'position:fixed;left:0;right:0;top:0;z-index:99999;padding:10px 16px;font-size:14px;text-align:center;color:#fff;background-color:#ef4444;border-radius:0 0 8px 8px;transition:transform 0.3s ease-out;'
  tip.textContent = displayText
  document.body.appendChild(tip)
  setTimeout(() => {
    if (tip.parentNode) tip.parentNode.removeChild(tip)
  }, 2000)
}
