/**
 * 错误/警告提示：顶部红色条（无打勾图标），用于表单校验、接口错误等
 * 优先使用 weui.topTips，否则原生 DOM 挂载到 body
 * @param {string} message - 友好中文文案
 */
export function showErrorTopTips(message) {
  const safeMessage = String(message || '请求失败').replace(/</g, '&lt;')
  const isRawError =
    /status code\s*\d+|network error|timeout|econnaborted|failed to fetch|load failed/i.test(safeMessage)
  const displayText = isRawError ? '网络连接异常，请稍后重试' : safeMessage

  try {
    const weui = typeof window !== 'undefined' && window.weui
    if (weui && typeof weui.topTips === 'function') {
      weui.topTips(displayText)
      return
    }
  } catch (_) {}

  if (typeof document === 'undefined' || !document.body) return
  const tip = document.createElement('div')
  tip.setAttribute('role', 'alert')
  tip.className = 'weui-toptips weui-toptips_warn'
  tip.style.cssText =
    'position:fixed;left:0;right:0;top:0;z-index:99999;padding:10px 16px;font-size:14px;text-align:center;color:#fff;background-color:#fa5151;transition:transform 0.3s ease-out;'
  tip.textContent = displayText
  document.body.appendChild(tip)
  setTimeout(() => {
    if (tip.parentNode) tip.parentNode.removeChild(tip)
  }, 2000)
}
